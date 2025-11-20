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

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class SailingTileMarkerOverlay extends Overlay
{
	private static final int MAX_DRAW_DISTANCE = 32;

	private final Client client;
	private final SailingTileMarkerConfig config;
	private final SailingTileMarkerPlugin plugin;

	@Inject
	private SailingTileMarkerOverlay(Client client, SailingTileMarkerConfig config, SailingTileMarkerPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Multimap<WorldView, ColorTileMarker> points = plugin.getPoints();
		if (points.isEmpty())
		{
			return null;
		}

		Stroke stroke = new BasicStroke((float) config.borderWidth());
		for (WorldView worldView : points.keySet())
		{
			for (final ColorTileMarker point : points.get(worldView))
			{
				WorldPoint worldPoint = point.getWorldPoint();
				if (worldPoint.getPlane() != worldView.getPlane())
				{
					continue;
				}

				Color tileColor = point.getColor();
				if (tileColor == null || !config.rememberTileColors())
				{
					// Use default color from config
					tileColor = config.markerColor();
				}

				drawTile(graphics, worldView, worldPoint, tileColor, point.getLabel(), stroke);
			}
		}

		return null;
	}

	private void drawTile(Graphics2D graphics, WorldView worldView, WorldPoint point, Color color, String label, Stroke borderStroke)
	{
		// Don't draw tiles too far away from the player in the top level world view
		if (worldView.isTopLevel())
		{
			WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
			if (point.distanceTo(playerLocation) >= MAX_DRAW_DISTANCE)
			{
				return;
			}
		}

		LocalPoint lp = LocalPoint.fromWorld(worldView, point);
		if (lp == null)
		{
			return;
		}

		Polygon poly = Perspective.getCanvasTilePoly(client, lp);
		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, color, new Color(0, 0, 0, config.fillOpacity()), borderStroke);
		}

		if (!Strings.isNullOrEmpty(label) && config.showLabels())
		{
			Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, label, 0);
			if (canvasTextLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, canvasTextLocation, label, color);
			}
		}
	}
}
