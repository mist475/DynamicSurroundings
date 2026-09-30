/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.DynSurround.client;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.weather.Weather;
import org.blockartistry.mod.DynSurround.compat.ISeasonalTemperatureCompat;
import org.blockartistry.mod.DynSurround.data.BiomeRegistry;

import cpw.mods.fml.common.Loader;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class WeatherUtils {

	private static ISeasonalTemperatureCompat seasonalHorizonsCompat;

	public static void initSeasonalHorizonsCompat() {
		if (!Loader.isModLoaded("seasonalhorizons"))
			return;

		try {
			seasonalHorizonsCompat = Class.forName("org.blockartistry.mod.DynSurround.compat.SeasonalHorizonsCompat")
				.asSubclass(ISeasonalTemperatureCompat.class).getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException | LinkageError e) {
			ModLog.warn("Could not load Seasonal Horizons weather compatibility: %s", e);
		}
	}

	public static float getTemperature(final World world, final BiomeGenBase biome, final int x, final int y,
		final int z) {
		return seasonalHorizonsCompat == null ? biome.getFloatTemperature(x, y, z)
			: seasonalHorizonsCompat.getTemperature(world, biome, x, y, z);
	}

	public static float getPrecipitationTemperature(final World world, final BiomeGenBase biome, final int x,
		final int renderY, final int z, final int precipitationY) {
		return seasonalHorizonsCompat == null
			? world.getWorldChunkManager().getTemperatureAtHeight(biome.getFloatTemperature(x, renderY, z), precipitationY)
			: seasonalHorizonsCompat.getTemperature(world, biome, x, precipitationY, z);
	}

	public static boolean biomeHasDust(final BiomeGenBase biome) {
		return ModOptions.allowDesertFog && BiomeRegistry.hasDust(biome) && !Weather.doVanilla();
	}
}
