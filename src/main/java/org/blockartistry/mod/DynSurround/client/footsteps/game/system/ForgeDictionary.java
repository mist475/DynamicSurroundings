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

package org.blockartistry.mod.DynSurround.client.footsteps.game.system;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IBlockMap;
import org.blockartistry.mod.DynSurround.compat.MCHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

@SideOnly(Side.CLIENT)
public final class ForgeDictionary {

	private static final String[] oreBlocks = { "oreIron", "oreGold", "oreCopper", "oreTin", "oreSilver", "oreLead",
			"oreNickle", "orePlatinum", "oreManaInfused", "oreElectrum", "oreInvar", "oreBronze", "oreSignalum",
			"oreEnderium", "oreLapis", "oreDiamond", "oreEmerald", "oreRedstone", "oreQuartz", "oreCoal", "oreNickel",
			"oreChimerite", "oreBlueTopaz", "oreMoonstone", "oreVinteum", "oreTitanium", "oreOsmium", "denseoreLapis",
			"oreUranium", "denseoreIron", "denseoreGold", "denseoreRedstone", "denseoreDiamond", "denseoreEmerald",
			"denseoreCoal", "denseoreCopper", "denseoreTin", "denseoreSilver", "denseoreLead", "denseoreNickel",
			"denseorePlatinum", "denseoreMithril", "oreRuby", "oreSapphire", "oreEmery", "oreAluminum", "oreJade",
			"oreApatite", "oreSalt", "oreZinc", "orePeridot", "oreSilicon", "oreMoldavite", "oreBloodstone",
			"oreCinnabar", "oreYellorite", "oreTemporal", "oreAmethyst", "oreAmber", "oreMalachite", "oreTanzanite",
			"oreTritanium", "oreTungsten", "oreTelsalite", "oreCheese", "denseorePeridot", "denseoreZinc",
			"denseoreRuby", "denseoreSapphire", "denseoreAmethyst", "denseoreTungsten", "oreHeeEndium", "oreStarSteel",
			"oreColdIron", "oreAdamantine", "oreMercury", "oreFossil" };

	private static final String[] metalBlocks = { "blockIron", "blockGold", "blockCopper", "blockTin", "blockSilver",
			"blockLead", "blockNickle", "blockPlatinum", "blockMithril", "blockElectrum", "blockInvar", "blockBronze",
			"blockSignalum", "blockLumium", "blockEnderium", "blockSteel", "blockNickel", "blockTitanium",
			"blockOsmium", "blockUranium", "blockBrass", "blockZinc", "blockConstantan", "slabConstantan", "slabCopper",
			"slabSilver", "slabElectrum", "slabNickel", "slabAluminum", "blockAluminum", "blockYellorium",
			"blockCyanite", "blockBlutonium", "blockLudicrite", "blockTemporal", "blockTritanium", "blockTungsten",
			"blockHeeEndium", "blockPsiMetal", "blockStarsteel", "blockAdamantine", "blockColdiron", "blockAquarium",
			"plateAdamantine", "plateBrass", "plateAquarium", "plateBronze", "plateStarsteel", "plateCopper",
			"plateGold", "plateElectrum", "plateGold", "plateInvar", "plateIron", "plateLead", "plateMithril",
			"plateNickel", "plateTin", "plateSteel", "plateSilver", "plateZinc", "bars", "trapdoorAdamantine",
			"trapdoorMithril", "trapdoorSilver", "trapdoorAquarium", "trapdoorInvar", "trapdoorBrass", "trapdoorBronze",
			"trapdoorColdiron", "trapdoorElectrum", "trapdoorLead", "trapdoorNickel", "trapdoorStarsteel",
			"trapdoorTin", "trapdoorSteel", "doorAdamantine", "doorAquarium", "doorBrass", "doorBronze", "doorColdiron",
			"doorCopper", "doorElectrum", "doorInvar", "doorLead", "doorMithril", "doorNickel", "doorSilver",
			"doorStarsteel", "doorSteel", "doorTin", "blockWroughtIron" };

	private static final String[] woodBlocks = { "logWood", "planksWood", "slabWood", "stairWood", "plankBamboo",
			"slabBamboo", "stairBamboo", "craftingTableWood", "plankWood" };

	private static final String[] saplings = { "treeSaplings", "saplingTree" };

	private static final String[] glassBlocks = { "blockGlass" };

	private static final String[] leafBlocks = { "treeLeaves", "leavesTree", "treeBambooLeaves" };

	private static final String[] stoneBlocks = { "stone", "cobblestone", "blockFuelCoke", "concrete", "blockCoal",
			"andesite", "blockAndesite", "stoneAndesite", "stoneDiorite", "diorite", "blockDiorite", "blockGranite",
			"stoneGranite", "blockCharcoal" };

	private static final String[] sandstoneBlocks = { "sandstone", "blockPrismarine", "limestone", "stoneLimestone",
			"blockLimestone" };

	private static final String[] sandBlocks = { "sand", "blockSalt", "blockPsiDust" };

	private static final String[] woodChests = { "chestWood", "chestTrapped" };

	private static final String[] rugBlocks = { "wool", "blockClothRock", "materialBedding" };

	private static final String[] fenceBlocks = { "fenceWood", "fenceGateWood" };

	private static final String[] mudBlocks = { "blockSlime", "blockCheese" };

	private static final String[] obsidianBlocks = { "oreSunstone", "blockGraphite", "basalt", "stoneBasalt",
			"blockBasalt", "blockBloodstone" };

	private static final String[] compositeBlocks = { "blockDiamond", "blockEmerald", "blockPeridot", "blockRuby",
			"blockSapphire", "blockVinteum", "blockChimerite", "blockBlueTopaz", "blockMoonstone", "blockSunstone",
			"blockAmethyst", "blockMoldavite", "blockAmber", "blockTanzanite", "blockMalachite", "blockChalcedony",
			"blockTeslaite", "blockJade", "blockPsiGem" };

	private static final String[] marbleBlocks = { "blockQuartz", "marble", "stoneMarble", "blockMarble" };

	private static final Map<String, String[]> dictionaryMaps = new HashMap<>();

	static {
		dictionaryMaps.put("ore", oreBlocks);
		dictionaryMaps.put("hardmetal", metalBlocks);
		dictionaryMaps.put("wood", woodBlocks);
		dictionaryMaps.put("glass", glassBlocks);
		dictionaryMaps.put("#sapling", saplings);
		dictionaryMaps.put("leaves", leafBlocks);
		dictionaryMaps.put("stone", stoneBlocks);
		dictionaryMaps.put("sandstone", sandstoneBlocks);
		dictionaryMaps.put("sand", sandBlocks);
		dictionaryMaps.put("squeakywood", woodChests);
		dictionaryMaps.put("rug", rugBlocks);
		dictionaryMaps.put("#fence", fenceBlocks);
		dictionaryMaps.put("mud", mudBlocks);
		dictionaryMaps.put("obsidian", obsidianBlocks);
		dictionaryMaps.put("composite", compositeBlocks);
		dictionaryMaps.put("marble", marbleBlocks);
	}

	private ForgeDictionary() {

	}

	public static void dumpOreNames() {
		ModLog.debug("**** FORGE ORE DICTIONARY NAMES ****");

        Arrays.stream(OreDictionary.getOreNames())
            .sorted(String::compareToIgnoreCase)
            .forEach(ModLog::debug)
        ;

		ModLog.debug("************************************");
	}

	public static void initialize(final IBlockMap blockMap) {
		for (final Entry<String, String[]> entry : dictionaryMaps.entrySet()) {
			final String value = entry.getKey();
			for (final String oreName : entry.getValue()) {
				final List<ItemStack> stacks = OreDictionary.getOres(oreName, false);
				for (final ItemStack stack : stacks) {
					final Block block = Block.getBlockFromItem(stack.getItem());
					if (block != null) {
						String blockName = MCHelper.nameOf(block);
						if (stack.getHasSubtypes() && stack.getItemDamage() != OreDictionary.WILDCARD_VALUE)
							blockName += "^" + stack.getItemDamage();
						blockMap.register(blockName, value);
					}
				}
			}
		}
	}

}
