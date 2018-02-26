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
package org.blockartistry.mod.DynSurround.client.fog;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.storm.StormProperties;
import org.blockartistry.mod.DynSurround.data.BiomeRegistry;
import org.blockartistry.mod.DynSurround.util.MathStuff;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.EntityViewRenderEvent;

/**
 * Scans the biome area around the player to determine the fog parameters.
 */
@SideOnly(Side.CLIENT)
public class BiomeFogRangeCalculator extends VanillaFogRangeCalculator {

	protected static final int DISTANCE = 20;
	protected static final float DUST_FOG_IMPACT = 0.9F;

	private static class Context {
		public int posX;
		public int posZ;
		public float rain;
		public float lastFarPlane;
		public boolean doScan = true;
		public final FogResult cached = new FogResult();

		public boolean returnCached(final int pX, final int pZ, final float r,
				@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
			return !this.doScan && pX == this.posX && pZ == this.posZ && r == this.rain
					&& this.lastFarPlane == event.farPlaneDistance && this.cached.isValid(event);
		}
	}

	protected final Context[] context = { new Context(), new Context() };

	public BiomeFogRangeCalculator() {

	}

	@Override
	@Nonnull
	public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {

		final EntityLivingBase player = EnvironState.getPlayer();
		final World world = EnvironState.getWorld();
		final int playerX = MathStuff.floor(player.posX);
		final int playerZ = MathStuff.floor(player.posZ);
		final float rainStr = StormProperties.getIntensityLevel();

		final Context ctx = this.context[event.fogMode == -1 ? 0 : 1];

		if (ctx.returnCached(playerX, playerZ, rainStr, event))
			return ctx.cached;

		float fpDistanceBiomeFog = 0F;
		float weightBiomeFog = 0;

		final boolean isRaining = StormProperties.isRaining();
		ctx.rain = rainStr;
		ctx.doScan = false;

		for (int x = -DISTANCE; x <= DISTANCE; ++x) {
			for (int z = -DISTANCE; z <= DISTANCE; ++z) {
				final BiomeGenBase biome = world.getBiomeGenForCoords(playerX + x, playerZ + z);
				float distancePart = 1F;
				final float weightPart = 1;

				if (isRaining && BiomeRegistry.hasDust(biome)) {
					distancePart = 1F - DUST_FOG_IMPACT * rainStr;
				} else if (BiomeRegistry.hasFog(biome)) {
					distancePart = BiomeRegistry.getFogDensity(biome);
				}

				fpDistanceBiomeFog += distancePart;
				weightBiomeFog += weightPart;
			}
		}

		// If we have to scan again just return what Forge wants. It's going to update
		// in the next render pass or two.
		if (ctx.doScan) {
			ctx.cached.set(event);
		} else {
			final float weightMixed = (DISTANCE * 2 + 1) * (DISTANCE * 2 + 1);
			final float weightDefault = weightMixed - weightBiomeFog;

			final float fpDistanceBiomeFogAvg = (weightBiomeFog == 0) ? 0 : fpDistanceBiomeFog / weightBiomeFog;

			float farPlaneDistance = (fpDistanceBiomeFog * 240 + event.farPlaneDistance * weightDefault) / weightMixed;
			final float farPlaneDistanceScaleBiome = (0.1f * (1 - fpDistanceBiomeFogAvg)
					+ 0.75f * fpDistanceBiomeFogAvg);
			final float farPlaneDistanceScale = (farPlaneDistanceScaleBiome * weightBiomeFog + 0.75f * weightDefault)
					/ weightMixed;

			ctx.posX = playerX;
			ctx.posZ = playerZ;
			ctx.lastFarPlane = event.farPlaneDistance;
			farPlaneDistance = Math.min(farPlaneDistance, event.farPlaneDistance);

			ctx.cached.set(event.fogMode, farPlaneDistance, farPlaneDistanceScale);
		}
		return ctx.cached;
	}
}