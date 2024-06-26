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

package org.blockartistry.mod.DynSurround.client.footsteps.game.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.blockartistry.mod.DynSurround.compat.MCHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;

@SideOnly(Side.CLIENT)
public class GenerateBlockReport {
	private final List<String> justNames;
	private final List<String> results;

	public GenerateBlockReport() {
		this.justNames = new ArrayList<>();
		this.results = new ArrayList<>();

		for (final Object o : Block.blockRegistry) {
			final Block block = (Block) o;
			final String name = MCHelper.nameOf(block);

			// stepSound.stepSoundName
			String soundName;
			if (block.stepSound == null) {
				soundName = "NO_STEP";
			} else if (block.stepSound.soundName == null) {
				soundName = "NO_SOUND";
			} else {
				soundName = block.stepSound.soundName;
			}

			if (block instanceof BlockLiquid) {
				soundName += "," + "EXTENDS_LIQUID";
			}
			if (block instanceof BlockBush) {
				soundName += "," + "EXTENDS_BUSH";
			}
			if (block instanceof BlockDoublePlant) {
				soundName += "," + "EXTENDS_DOUBLE_PLANT";
			}
			if (block instanceof BlockCrops) {
				soundName += "," + "EXTENDS_CROPS";
			}
			if (block instanceof BlockContainer) {
				soundName += "," + "EXTENDS_CONTAINER";
			}
			if (block instanceof BlockLeavesBase) {
				soundName += "," + "EXTENDS_LEAVES";
			}
			if (block instanceof BlockRailBase) {
				soundName += "," + "EXTENDS_RAIL";
			}
			if (block instanceof BlockSlab) {
				soundName += "," + "EXTENDS_SLAB";
			}
			if (block instanceof BlockStairs) {
				soundName += "," + "EXTENDS_STAIRS";
			}
			if (block instanceof BlockBreakable) {
				soundName += "," + "EXTENDS_BREAKABLE";
			}
			if (block instanceof BlockFalling) {
				soundName += "," + "EXTENDS_PHYSICALLY_FALLING";
			}
			if (block instanceof BlockPane) {
				soundName += "," + "EXTENDS_PANE";
			}
			if (block instanceof BlockRotatedPillar) {
				soundName += "," + "EXTENDS_PILLAR";
			}
			if (block instanceof BlockTorch) {
				soundName += "," + "EXTENDS_TORCH";
			}
			/*
			 * if (!block.func_149662_c()) { soundName += "," + "FUNC_POPPABLE"; }
			 */
			if (!block.isOpaqueCube()) {
				soundName += "," + "HITBOX";
			}

			this.justNames.add(name);
			this.results.add(name + " = " + soundName);
		}

		Collections.sort(this.justNames);
		Collections.sort(this.results);
	}

	public List<String> getResults() {
		return this.results;
	}

	public List<String> getBlockNames() {
		return this.justNames;
	}

}
