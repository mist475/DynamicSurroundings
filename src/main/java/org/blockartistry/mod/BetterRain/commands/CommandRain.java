/*
 * This file is part of BetterRain, licensed under the MIT License (MIT).
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

package org.blockartistry.mod.BetterRain.commands;

import java.text.DecimalFormat;
import java.util.List;

import org.blockartistry.mod.BetterRain.data.RainData;

import com.google.common.collect.ImmutableList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class CommandRain extends CommandBase {

	private static final List<String> ALIAS = ImmutableList.<String> builder().add("r", "br", "betterrain").build();
	private static final DecimalFormat FORMATTER = new DecimalFormat("0.0");

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandName() {
		return "rain";
	}

	@Override
	public List<String> getCommandAliases() {
		return ALIAS;
	}

	@Override
	public String getCommandUsage(final ICommandSender p_71518_1_) {
		return "/rain <status | reset | 1-100 | <<setmax|setmin> 0-100>";
	}

	@Override
	public void processCommand(final ICommandSender sender, final String[] parms) {

		final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
		final World world = player.worldObj;
		final RainData data = RainData.get(world);

		if (parms.length == 1) {
			if ("status".compareToIgnoreCase(parms[0]) == 0) {
				// Dump out some diagnostics for the current dimension
				final float minutes = (world.getWorldInfo().getRainTime() / 20.0F) / 60.0F;
				final StringBuilder builder = new StringBuilder();
				builder.append(data.toString());
				builder.append("; isRaining: ").append(Boolean.toString(world.isRaining()));
				builder.append("; isSurface: ").append(Boolean.toString(world.provider.isSurfaceWorld()));
				builder.append("; strength: ").append(FORMATTER.format(world.getRainStrength(1.0F) * 100));
				builder.append("; timer: ").append(FORMATTER.format(minutes)).append(" minutes");
				player.addChatMessage(new ChatComponentText(builder.toString()));
			} else if ("reset".compareToIgnoreCase(parms[0]) == 0) {
				world.provider.resetRainAndThunder();
				player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("msg.RainReset")));
			} else {
				final double d = parseDoubleBounded(sender, parms[0], 0.0D, 100.0D) / 100.0D;
				data.setRainIntensity((float) d);
				player.addChatMessage(new ChatComponentText(StatCollector.translateToLocalFormatted(
						"msg.RainIntensitySet", FORMATTER.format(data.getRainIntensity() * 100))));
			}
		} else if (parms.length == 2) {
			if ("setmin".compareToIgnoreCase(parms[0]) == 0) {
				final double d = parseDoubleBounded(sender, parms[1], 0.0D, 100.0D) / 100.0D;
				data.setMinRainIntensity((float) d);
				player.addChatMessage(new ChatComponentText(StatCollector.translateToLocalFormatted(
						"msg.MinRainIntensitySet", FORMATTER.format(data.getMinRainIntensity() * 100))));
			} else if ("setmax".compareToIgnoreCase(parms[0]) == 0) {
				final double d = parseDoubleBounded(sender, parms[1], 0.0D, 100.0D) / 100.0D;
				data.setMaxRainIntensity((float) d);
				player.addChatMessage(new ChatComponentText(StatCollector.translateToLocalFormatted(
						"msg.MaxRainIntensitySet", FORMATTER.format(data.getMaxRainIntensity() * 100))));
			} else {
				throw new CommandException(getCommandUsage(sender));
			}
		} else {
			player.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
		}
	}
}