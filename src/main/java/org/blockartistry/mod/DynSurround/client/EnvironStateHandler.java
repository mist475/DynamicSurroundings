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

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.client.sound.SoundManager;
import org.blockartistry.mod.DynSurround.client.weather.Weather;
import org.blockartistry.mod.DynSurround.data.BiomeRegistry;
import org.blockartistry.mod.DynSurround.data.DimensionRegistry;
import org.blockartistry.mod.DynSurround.event.DiagnosticEvent;
import org.blockartistry.mod.DynSurround.util.PlayerUtils;
import org.blockartistry.mod.DynSurround.util.random.XorShiftRandom;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;

@SideOnly(Side.CLIENT)
public class EnvironStateHandler implements IClientEffectHandler {

	private static final SoundEffect JUMP;
	private static final SoundEffect SWORD;
	private static final SoundEffect AXE;
	private static final SoundEffect CRAFTING;
	private static final SoundEffect BOW_PULL;

	static {
		if (ModOptions.enableJumpSound)
			JUMP = new SoundEffect("dsurround:jump", 0.2F, 1.0F, true);
		else
			JUMP = null;

		if (ModOptions.enableSwingSound) {
			SWORD = new SoundEffect("dsurround:swoosh", 1.0F, 1.0F);
			AXE = new SoundEffect("dsurround:swoosh", 1.0F, 0.5F);
		} else {
			SWORD = null;
			AXE = null;
		}

		if (ModOptions.enableCraftingSound)
			CRAFTING = new SoundEffect("dsurround:crafting");
		else
			CRAFTING = null;

		if (ModOptions.enableBowPullSound)
			BOW_PULL = new SoundEffect("dsurround:bowpull");
		else
			BOW_PULL = null;
	}

	// Diagnostic strings to display in the debug HUD
	private List<String> diagnostics;

	public static class EnvironState {

		public static final Random RANDOM = new XorShiftRandom();

		// State that is gathered from the various sources
		// to avoid requery. Used during the tick.
		private static String conditions = "";
		private static String biomeName = "";
		private static BiomeGenBase playerBiome = null;
		private static DimensionRegistry dimensionInfo = null;
		private static int dimensionId;
		private static String dimensionName;
		private static EntityPlayer player;
		private static boolean freezing;
		private static boolean fog;
		private static boolean humid;
		private static boolean dry;
		private static String temperatureCategory = "";
		private static boolean inside;

		private static int tickCounter;

		private static final String CONDITION_TOKEN_HURT = "hurt";
		private static final String CONDITION_TOKEN_HUNGRY = "hungry";
		private static final String CONDITION_TOKEN_BURNING = "burning";
		private static final String CONDITION_TOKEN_NOAIR = "noair";
		private static final String CONDITION_TOKEN_FLYING = "flying";
		private static final String CONDITION_TOKEN_SPRINTING = "sprinting";
		private static final String CONDITION_TOKEN_INLAVA = "inlava";
		private static final String CONDITION_TOKEN_INWATER = "inwater";
		private static final String CONDITION_TOKEN_INVISIBLE = "invisible";
		private static final String CONDITION_TOKEN_BLIND = "blind";
		private static final String CONDITION_TOKEN_MINECART = "ridingminecart";
		private static final String CONDITION_TOKEN_HORSE = "ridinghorse";
		private static final String CONDITION_TOKEN_BOAT = "ridingboat";
		private static final String CONDITION_TOKEN_PIG = "ridingpig";
		private static final String CONDITION_TOKEN_RIDING = "riding";
		private static final String CONDITION_TOKEN_FREEZING = "freezing";
		private static final String CONDITION_TOKEN_FOG = "fog";
		private static final String CONDITION_TOKEN_HUMID = "humid";
		private static final String CONDITION_TOKEN_DRY = "dry";
		private static final String CONDITION_TOKEN_INSIDE = "inside";
		private static final char CONDITION_SEPARATOR = '#';

		private static String getPlayerConditions(final EntityPlayer player) {
			final StringBuilder builder = new StringBuilder();
			if (isPlayerHurt())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_HURT);
			if (isPlayerHungry())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_HUNGRY);
			if (isPlayerBurning())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_BURNING);
			if (isPlayerSuffocating())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_NOAIR);
			if (isPlayerFlying())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_FLYING);
			if (isPlayerSprinting())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_SPRINTING);
			if (isPlayerInLava())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_INLAVA);
			if (isPlayerInvisible())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_INVISIBLE);
			if (isPlayerBlind())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_BLIND);
			if (isPlayerInWater())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_INWATER);
			if (isFreezing())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_FREEZING);
			if (isFoggy())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_FOG);
			if (isHumid())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_HUMID);
			if (isDry())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_DRY);
			if (isPlayerInside())
				builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_INSIDE);
			if (isPlayerRiding()) {
				builder.append(CONDITION_SEPARATOR);
				if (player.ridingEntity instanceof EntityMinecart)
					builder.append(CONDITION_TOKEN_MINECART);
				else if (player.ridingEntity instanceof EntityHorse)
					builder.append(CONDITION_TOKEN_HORSE);
				else if (player.ridingEntity instanceof EntityBoat)
					builder.append(CONDITION_TOKEN_BOAT);
				else if (player.ridingEntity instanceof EntityPig)
					builder.append(CONDITION_TOKEN_PIG);
				else
					builder.append(CONDITION_TOKEN_RIDING);
			}
			builder.append(CONDITION_SEPARATOR).append(temperatureCategory);
			builder.append(CONDITION_SEPARATOR);

			return builder.toString();
		}

		private static void tick(final World world, final EntityPlayer player) {
			EnvironState.player = player;
			EnvironState.conditions = DimensionRegistry.getConditions(world) + getPlayerConditions(player);
			EnvironState.playerBiome = PlayerUtils.getPlayerBiome(player, false);
			EnvironState.biomeName = BiomeRegistry.resolveName(EnvironState.playerBiome);
			EnvironState.dimensionInfo = DimensionRegistry.getData(player.worldObj);
			EnvironState.dimensionId = world.provider.dimensionId;
			EnvironState.dimensionName = world.provider.getDimensionName();
			EnvironState.inside = PlayerUtils.isReallyInside(EnvironState.player);

			final int posX = MathHelper.floor_double(player.posX);
			final int posY = MathHelper.floor_double(player.posY);
			final int posZ = MathHelper.floor_double(player.posZ);
			final BiomeGenBase trueBiome = PlayerUtils.getPlayerBiome(player, true);
			EnvironState.freezing = trueBiome.getFloatTemperature(posX, posY, posZ) < 0.15F;
			EnvironState.temperatureCategory = "tc" + trueBiome.getTempCategory().name().toLowerCase();
			EnvironState.humid = trueBiome.isHighHumidity();
			EnvironState.dry = trueBiome.getFloatRainfall() == 0;

			if (!Minecraft.getMinecraft().isGamePaused())
				EnvironState.tickCounter++;
		}

		public static String getConditions() {
			return conditions;
		}

		public static BiomeGenBase getPlayerBiome() {
			return playerBiome;
		}

		public static String getBiomeName() {
			return biomeName;
		}

		public static DimensionRegistry getDimensionInfo() {
			return dimensionInfo;
		}

		public static int getDimensionId() {
			return dimensionId;
		}

		public static String getDimensionName() {
			return dimensionName;
		}

		public static EntityPlayer getPlayer() {
			if (player == null)
				player = Minecraft.getMinecraft().thePlayer;
			return player;
		}

		public static boolean isPlayer(final Entity entity) {
			if (entity instanceof EntityPlayer ep) {
                return ep.getUniqueID().equals(getPlayer().getUniqueID());
			}
			return false;
		}

		public static boolean isPlayer(final UUID id) {
			return getPlayer().getUniqueID().equals(id);
		}

		public static boolean isCreative() {
			return getPlayer().capabilities.isCreativeMode;
		}

		public static boolean isPlayerHurt() {
			return !isCreative() && getPlayer().getHealth() <= ModOptions.playerHurtThreshold;
		}

		public static boolean isPlayerHungry() {
			return !isCreative() && getPlayer().getFoodStats().getFoodLevel() <= ModOptions.playerHungerThreshold;
		}

		public static boolean isPlayerBurning() {
			return getPlayer().isBurning();
		}

		public static boolean isPlayerSuffocating() {
			return getPlayer().getAir() <= 0;
		}

		public static boolean isPlayerFlying() {
			return getPlayer().capabilities.isFlying;
		}

		public static boolean isPlayerSprinting() {
			return getPlayer().isSprinting();
		}

		public static boolean isPlayerInLava() {
			return getPlayer().worldObj.isMaterialInBB(
					getPlayer().boundingBox.expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D),
					Material.lava);
		}

		public static boolean isPlayerInvisible() {
			return getPlayer().isInvisible();
		}

		public static boolean isPlayerBlind() {
			return getPlayer().isPotionActive(Potion.blindness);
		}

		public static boolean isPlayerInWater() {
			return getPlayer().isInWater();
		}

		public static boolean isPlayerRiding() {
			return getPlayer().isRiding();
		}

		public static boolean isPlayerOnGround() {
			return getPlayer().onGround;
		}

		public static boolean isPlayerMoving() {
			return getPlayer().distanceWalkedModified != player.prevDistanceWalkedModified;
		}

		public static boolean isPlayerInside() {
			return inside;
		}

		public static boolean isPlayerUnderground() {
			return playerBiome == BiomeRegistry.UNDERGROUND;
		}

		public static boolean isPlayerInSpace() {
			return playerBiome == BiomeRegistry.OUTERSPACE;
		}

		public static boolean isPlayerInClouds() {
			return playerBiome == BiomeRegistry.CLOUDS;
		}

		public static boolean isFreezing() {
			return freezing;
		}

		public static boolean isFoggy() {
			return fog;
		}

		public static boolean isHumid() {
			return humid;
		}

		public static boolean isDry() {
			return dry;
		}

		public static World getWorld() {
			return getPlayer().worldObj;
		}

		public static int getTickCounter() {
			return tickCounter;
		}

		public static double distanceToPlayer(final double x, final double y, final double z) {
			if (player == null)
				return Double.MAX_VALUE;
			return player.getDistanceSq(x, y, z);
		}
	}

	@Override
	public void process(final World world, final EntityPlayer player) {
		EnvironState.tick(world, player);

		// Gather diagnostics if needed
		if (Minecraft.getMinecraft().gameSettings.showDebugInfo && ModOptions.enableDebugLogging) {
			final DiagnosticEvent.Gather gather = new DiagnosticEvent.Gather(world, player);
			MinecraftForge.EVENT_BUS.post(gather);
			this.diagnostics = gather.output;
		} else {
			this.diagnostics = null;
		}
	}

	@Override
	public boolean hasEvents() {
		return true;
	}

	@SubscribeEvent
	public void onJump(final LivingJumpEvent event) {
		if (JUMP == null || event.entity == null || event.entity.worldObj == null)
			return;

		if (event.entity.worldObj.isRemote && EnvironState.isPlayer(event.entity))
			SoundManager.playSoundAtPlayer(JUMP);
	}

	@SubscribeEvent
	public void onItemUse(final AttackEntityEvent event) {
		if (SWORD == null || event.entityPlayer == null || event.entityPlayer.worldObj == null)
			return;

		if (event.entityPlayer.worldObj.isRemote && EnvironState.isPlayer(event.entityPlayer)) {
			final ItemStack currentItem = event.entityPlayer.getCurrentEquippedItem();
			if (currentItem != null) {
				SoundEffect sound = null;
				final Item item = currentItem.getItem();
				if (item instanceof ItemSword)
					sound = SWORD;
				else if (item instanceof ItemAxe)
					sound = AXE;

				if (sound != null)
					SoundManager.playSoundAtPlayer(sound);
			}
		}
	}

	private int craftSoundThrottle = 0;

	@SubscribeEvent
	public void onCrafting(final ItemCraftedEvent event) {
		if (CRAFTING == null || event.player == null || event.player.worldObj == null)
			return;

		if (event.player.worldObj.isRemote && EnvironState.isPlayer(event.player)) {
			if (this.craftSoundThrottle < (EnvironState.getTickCounter() - 30)) {
				this.craftSoundThrottle = EnvironState.getTickCounter();
				SoundManager.playSoundAtPlayer(CRAFTING);
			}
		}

	}

	@SubscribeEvent
	public void onItemUse(final PlayerUseItemEvent.Start event) {
		if (BOW_PULL == null || event.entityPlayer == null || event.entityPlayer.worldObj == null || event.item == null
				|| event.item.getItem() == null)
			return;

		if (event.entityPlayer.worldObj.isRemote && event.item.getItem() instanceof ItemBow) {
			SoundManager.playSoundAtPlayer(BOW_PULL);
		}
	}

	@SubscribeEvent
	public void onGatherText(@Nonnull final RenderGameOverlayEvent.Text event) {
		if (this.diagnostics != null && !this.diagnostics.isEmpty()) {
			event.left.add("");
			event.left.addAll(this.diagnostics);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void diagnostics(final DiagnosticEvent.Gather event) {
		final EntityPlayer player = EnvironState.getPlayer();
		event.output.add("Dim: " + EnvironState.getDimensionId() + "/" + EnvironState.getDimensionName());
		event.output.add("Player: h " + player.getHealth() + "/" + player.getMaxHealth() + "; f "
				+ player.getFoodStats().getFoodLevel() + "; s " + player.getFoodStats().getSaturationLevel());
		event.output.add(Weather.diagnostic());
		event.output.add("Biome: " + EnvironState.getBiomeName());
		event.output.add("Conditions: " + EnvironState.getConditions());
	}

}
