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

package org.blockartistry.mod.DynSurround.client.weather;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.Module;
import org.blockartistry.mod.DynSurround.client.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.data.DimensionEffectData;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public enum Weather {

	VANILLA, NONE(0.0F, "calm"), CALM(0.1F, "calm"), LIGHT(0.33F, "light"), NORMAL(0.66F, "normal"), HEAVY(1.0F,
			"heavy");

	private static float intensityLevel = 0.0F;
	private static Weather intensity = VANILLA;

	private final float level;
	private final ResourceLocation rainTexture;
	private final ResourceLocation snowTexture;
	private final ResourceLocation dustTexture;
	private final String rainSound;
	private final String dustSound;

	Weather() {
		this.level = -10.0F;
		this.rainTexture = EntityRenderer.locationRainPng;
		this.snowTexture = EntityRenderer.locationSnowPng;
		this.dustTexture = new ResourceLocation(Module.MOD_ID, "textures/environment/dust_calm.png");
		this.rainSound = String.format("%s:%s", Module.MOD_ID, "rain");
		this.dustSound = String.format("%s:%s", Module.MOD_ID, "dust");
	}

	Weather(final float level, final String intensity) {
		this.level = level;
		this.rainTexture = new ResourceLocation(Module.MOD_ID,
				String.format("textures/environment/rain_%s.png", intensity));
		this.snowTexture = new ResourceLocation(Module.MOD_ID,
				String.format("textures/environment/snow_%s.png", intensity));
		this.dustTexture = new ResourceLocation(Module.MOD_ID,
				String.format("textures/environment/dust_%s.png", intensity));
		this.rainSound = String.format("%s:%s", Module.MOD_ID, "rain");
		this.dustSound = String.format("%s:%s", Module.MOD_ID, "dust");
	}

	private static World getWorld() {
		return Minecraft.getMinecraft().theWorld;
	}

	public static Weather getIntensity() {
		return intensity;
	}

	public static float getIntensityLevel() {
		return Math.min(getWorld().rainingStrength, intensityLevel);
	}

	public static float getMaxIntensityLevel() {
		return intensityLevel;
	}

	public static boolean isRaining() {
		return getIntensityLevel() > 0F;
	}

	public static float getRainStrength() {
		return getWorld().rainingStrength;
	}

	public static float getThunderStrength() {
		return getWorld().thunderingStrength;
	}

	public String getStormSound() {
		return this.rainSound;
	}

	public String getDustSound() {
		return this.dustSound;
	}

	public static float getCurrentVolume() {
		return (doVanilla() ? 0.66F : intensityLevel) * ModOptions.soundLevel;
	}

	public static ResourceLocation getCurrentStormSound() {
		return new ResourceLocation(intensity.rainSound);
	}

	public static ResourceLocation getCurrentDustSound() {
		return new ResourceLocation(intensity.dustSound);
	}

	public static boolean doVanilla() {
		return intensity == VANILLA || ModOptions.disableWeatherEffects;
	}

	/**
	 * Sets the rain intensity based on the intensityLevel level provided. This is
	 * called by the packet handler when the server wants to set the intensity level
	 * on the client.
	 */
	public static void setIntensity(float level) {

		// If the level is Vanilla it means that
		// the rainfall in the dimension is to be
		// that of Vanilla.
		if (level == VANILLA.level) {
			intensity = VANILLA;
			intensityLevel = 0.0F;
			setTextures();
			return;
		}

		level = MathHelper.clamp_float(level, DimensionEffectData.MIN_INTENSITY, DimensionEffectData.MAX_INTENSITY);

		if (intensityLevel != level) {
			intensityLevel = level;
			if (level > 0) {
				level += 0.01;
			}
			if (intensityLevel <= NONE.level)
				intensity = NONE;
			else if (intensityLevel < CALM.level)
				intensity = CALM;
			else if (intensityLevel < LIGHT.level)
				intensity = LIGHT;
			else if (intensityLevel < NORMAL.level)
				intensity = NORMAL;
			else
				intensity = HEAVY;
		}
	}

	/**
	 * Set precipitation textures based on the currentAurora intensity. This is
	 * invoked before rendering takes place.
	 */
	public static void setTextures() {
		// AT transform removed final and made public.
		StormRenderer.locationRainPng = intensity.rainTexture;
		StormRenderer.locationSnowPng = intensity.snowTexture;
		StormRenderer.locationDustPng = intensity.dustTexture;
	}

	public static String diagnostic() {
        return "Storm: " + intensity.name() +
            " level:" + intensityLevel +
            " str:" + EnvironState.getWorld().getRainStrength(1.0F);
	}

}
