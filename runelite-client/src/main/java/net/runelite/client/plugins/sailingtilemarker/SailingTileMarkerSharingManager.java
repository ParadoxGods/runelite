/*
 * Copyright (c) 2021, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.sailingtilemarker;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Runnables;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;

@Slf4j
class SailingTileMarkerSharingManager
{
	private static final String SAILING_MARK = "Sailing Mark";
	private static final String EXPORT_SAILING_MARKERS_OPTION = "Export Sailing Markers";
	private static final String IMPORT_SAILING_MARKERS_OPTION = "Import Sailing Markers";
	private static final String CLEAR_SAILING_MARKERS_OPTION = "Clear Sailing Markers";

	@Inject
	private Client client;

	@Inject
	private SailingTileMarkerPlugin plugin;

	@Inject
	private SailingTileMarkerConfig config;

	@Inject
	private Gson gson;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private MenuManager menuManager;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	void addImportExportMenuOptions()
	{
		menuManager.addManagedCustomMenu(EXPORT_SAILING_MARKERS_OPTION, this::exportSailingMarkers);
		menuManager.addManagedCustomMenu(IMPORT_SAILING_MARKERS_OPTION, this::promptForImport);
	}

	void addClearMenuOption()
	{
		menuManager.addManagedCustomMenu(CLEAR_SAILING_MARKERS_OPTION, this::promptForClear);
	}

	void removeMenuOptions()
	{
		menuManager.removeManagedCustomMenu(EXPORT_SAILING_MARKERS_OPTION);
		menuManager.removeManagedCustomMenu(IMPORT_SAILING_MARKERS_OPTION);
		menuManager.removeManagedCustomMenu(CLEAR_SAILING_MARKERS_OPTION);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!config.showImportExport())
		{
			return;
		}

		final boolean isWorldMapOrbOption = event.getOption().equals("Floating World Map")
			&& event.getTarget().isEmpty();

		if (isWorldMapOrbOption)
		{
			client.createMenuEntry(-1)
				.setOption(EXPORT_SAILING_MARKERS_OPTION)
				.setTarget("")
				.setType(MenuAction.RUNELITE)
				.onClick(this::exportSailingMarkers);

			client.createMenuEntry(-1)
				.setOption(IMPORT_SAILING_MARKERS_OPTION)
				.setTarget("")
				.setType(MenuAction.RUNELITE)
				.onClick(this::promptForImport);

			client.createMenuEntry(-1)
				.setOption(CLEAR_SAILING_MARKERS_OPTION)
				.setTarget("")
				.setType(MenuAction.RUNELITE)
				.onClick(this::promptForClear);
		}
	}

	private void exportSailingMarkers(MenuEntry menuEntry)
	{
		int[] regions = client.getMapRegions();
		if (regions == null)
		{
			return;
		}

		// Collect all markers from loaded regions
		List<SailingTileMarkerPoint> activeMarkerPoints = Arrays.stream(regions)
			.mapToObj(regionId -> plugin.getPoints(regionId).stream())
			.flatMap(Function.identity())
			.collect(Collectors.toList());

		if (activeMarkerPoints.isEmpty())
		{
			sendChatMessage("You have no sailing markers to export in your currently loaded regions.");
			return;
		}

		final String exportDump = gson.toJson(activeMarkerPoints);

		Toolkit.getDefaultToolkit()
			.getSystemClipboard()
			.setContents(new StringSelection(exportDump), null);

		sendChatMessage(activeMarkerPoints.size() + " sailing markers were copied to your clipboard.");
	}

	private void promptForImport(MenuEntry menuEntry)
	{
		final String clipboardText;
		try
		{
			clipboardText = Toolkit.getDefaultToolkit()
				.getSystemClipboard()
				.getData(DataFlavor.stringFlavor)
				.toString();
		}
		catch (IOException | UnsupportedFlavorException ex)
		{
			sendChatMessage("Unable to read system clipboard.");
			log.warn("error reading clipboard", ex);
			return;
		}

		if (Strings.isNullOrEmpty(clipboardText))
		{
			sendChatMessage("You do not have any sailing markers copied in your clipboard.");
			return;
		}

		List<SailingTileMarkerPoint> importMarkers;
		try
		{
			importMarkers = gson.fromJson(clipboardText, new TypeToken<List<SailingTileMarkerPoint>>(){}.getType());
		}
		catch (Exception e)
		{
			sendChatMessage("The clipboard data does not contain valid sailing markers.");
			log.debug("error importing sailing markers", e);
			return;
		}

		if (importMarkers.isEmpty())
		{
			sendChatMessage("The clipboard data does not contain any sailing markers.");
			return;
		}

		importSailingMarkers(importMarkers);
	}

	private void importSailingMarkers(Collection<SailingTileMarkerPoint> importPoints)
	{
		// Group markers by region
		Map<Integer, List<SailingTileMarkerPoint>> regionGroupedMarkers = importPoints.stream()
			.collect(Collectors.groupingBy(SailingTileMarkerPoint::getRegionId));

		int importedCount = 0;

		// Import each region's markers
		for (Map.Entry<Integer, List<SailingTileMarkerPoint>> entry : regionGroupedMarkers.entrySet())
		{
			int regionId = entry.getKey();
			List<SailingTileMarkerPoint> groupedMarkers = entry.getValue();

			// Get existing markers for this region
			Collection<SailingTileMarkerPoint> existingMarkers = plugin.getPoints(regionId);

			// Create merged list
			List<SailingTileMarkerPoint> mergedList = new ArrayList<>(existingMarkers.size() + groupedMarkers.size());
			mergedList.addAll(existingMarkers);

			// Add new markers, avoiding duplicates
			for (SailingTileMarkerPoint marker : groupedMarkers)
			{
				if (!mergedList.contains(marker))
				{
					mergedList.add(marker);
					importedCount++;
				}
			}

			// Save the merged list
			plugin.savePoints(regionId, mergedList);
		}

		// Reload all points to update the display
		plugin.loadPoints();

		sendChatMessage(importedCount + " sailing markers were imported from the clipboard.");
	}

	private void promptForClear(MenuEntry menuEntry)
	{
		int[] regions = client.getMapRegions();
		if (regions == null)
		{
			return;
		}

		// Count total markers in loaded regions
		long markerCount = Arrays.stream(regions)
			.mapToObj(regionId -> plugin.getPoints(regionId))
			.mapToLong(Collection::size)
			.sum();

		if (markerCount == 0)
		{
			sendChatMessage("You have no sailing markers to clear in your currently loaded regions.");
			return;
		}

		clearSailingMarkers();
	}

	private void clearSailingMarkers()
	{
		int[] regions = client.getMapRegions();
		if (regions == null)
		{
			return;
		}

		int clearedCount = 0;

		// Clear markers from all loaded regions
		for (int regionId : regions)
		{
			Collection<SailingTileMarkerPoint> regionMarkers = plugin.getPoints(regionId);
			clearedCount += regionMarkers.size();
			plugin.savePoints(regionId, null);
		}

		// Reload to update display
		plugin.loadPoints();

		sendChatMessage(clearedCount + " sailing markers were cleared from your currently loaded regions.");
	}

	private void sendChatMessage(final String message)
	{
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(message)
			.build());
	}
}
