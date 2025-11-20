/*
 * Copyright (c) 2018, TheLonelyDev <https://github.com/TheLonelyDev>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(SailingTileMarkerConfig.CONFIG_GROUP)
public interface SailingTileMarkerConfig extends Config
{
	String CONFIG_GROUP = "sailingTileMarker";
	String SHOW_IMPORT_EXPORT_KEY_NAME = "showImportExport";

	@Alpha
	@ConfigItem(
		keyName = "markerColor",
		name = "Default marker color",
		description = "Configures the default color of sailing tile markers",
		position = 0
	)
	default Color markerColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
		keyName = "rememberTileColors",
		name = "Remember tile colors",
		description = "Color tiles using the color from the color picker",
		position = 1
	)
	default boolean rememberTileColors()
	{
		return false;
	}

	@ConfigItem(
		keyName = "borderWidth",
		name = "Border width",
		description = "Configures the width of the marked tile border",
		position = 2
	)
	default double borderWidth()
	{
		return 2;
	}

	@ConfigItem(
		keyName = "fillOpacity",
		name = "Fill opacity",
		description = "Configures the opacity of the marked tile fill color",
		position = 3
	)
	@Range(max = 255)
	default int fillOpacity()
	{
		return 50;
	}

	@ConfigItem(
		keyName = "drawOnMinimap",
		name = "Draw tiles on minimap",
		description = "Configures whether marked sailing tiles should also be drawn on the minimap",
		position = 4
	)
	default boolean drawTileOnMinimmap()
	{
		return false;
	}

	@ConfigItem(
		keyName = SHOW_IMPORT_EXPORT_KEY_NAME,
		name = "Show Import/Export options",
		description = "Show Import/Export/Clear options on the world map orb for sharing markers with friends",
		position = 5
	)
	default boolean showImportExport()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showLabels",
		name = "Show labels",
		description = "Configures whether to show labels for marked tiles",
		position = 6
	)
	default boolean showLabels()
	{
		return true;
	}
}
