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
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Serialization model for storing sailing tile markers in configuration.
 * Stored on a per-region basis for efficient loading.
 */
@Value
@EqualsAndHashCode(exclude = {"color", "label"})
class SailingTileMarkerPoint
{
	/**
	 * The region ID where this marker is located
	 */
	private int regionId;

	/**
	 * The X coordinate within the region (0-63)
	 */
	private int regionX;

	/**
	 * The Y coordinate within the region (0-63)
	 */
	private int regionY;

	/**
	 * The plane/Z level (0-3)
	 */
	private int z;

	/**
	 * Optional custom color for this marker. If null, uses default config color.
	 */
	@Nullable
	private Color color;

	/**
	 * Optional label text to display at this marker
	 */
	@Nullable
	private String label;
}
