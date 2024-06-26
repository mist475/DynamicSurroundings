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

package org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.implem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.implem.BlockInfo.BlockInfoMutable;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IBlockMap;
import org.blockartistry.mod.DynSurround.compat.MCHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

@SideOnly(Side.CLIENT)
public class BasicBlockMap implements IBlockMap {
	private static final Pattern pattern = Pattern.compile("([^:]+:[^^+]+)\\^?(\\d+)?\\+?(\\w+)?");

	private final BlockInfoMutable mutable = new BlockInfoMutable();
	private final Map<BlockInfo, String> metaMap = new HashMap<>();
	private final Map<Substrate, Map<BlockInfo, String>> substrateMap = new EnumMap<>(
        Substrate.class);

	private static class MacroEntry {
		public final int meta;
		public final String substrate;
		public final String value;

		public MacroEntry(final String substrate, final String value) {
			this(-1, substrate, value);
		}

		public MacroEntry(final int meta, final String substrate, final String value) {
			this.meta = meta;
			this.substrate = substrate;
			this.value = value;
		}
	}

	private static final Map<String, List<MacroEntry>> macros = new LinkedHashMap<>();

	static {
		List<MacroEntry> entries = new ArrayList<>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry("foliage", "straw"));
		macros.put("#sapling", entries);
		macros.put("#reed", entries);

		entries = new ArrayList<>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry(0, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(1, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(2, "foliage", "brush"));
		entries.add(new MacroEntry(3, "foliage", "brush"));
		entries.add(new MacroEntry(4, "foliage", "brush_straw_transition"));
		entries.add(new MacroEntry(5, "foliage", "brush_straw_transition"));
		entries.add(new MacroEntry(6, "foliage", "straw"));
		entries.add(new MacroEntry(7, "foliage", "straw"));
		macros.put("#wheat", entries);

		entries = new ArrayList<>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry(0, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(1, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(2, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(3, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(4, "foliage", "brush"));
		entries.add(new MacroEntry(5, "foliage", "brush"));
		entries.add(new MacroEntry(6, "foliage", "brush"));
		entries.add(new MacroEntry(7, "foliage", "brush"));
		macros.put("#crop", entries);

		entries = new ArrayList<>();
		entries.add(new MacroEntry("bigger", "bluntwood"));
		macros.put("#fence", entries);
	}

	public BasicBlockMap() {
		// Air is not an emitter, always!
		put(Blocks.air, -1, null, "NOT_EMITTER");
	}

	@Override
	public String getBlockMap(final Block block, final int meta) {
		this.mutable.setBlock(block).setMeta(meta);
		String acoustic = this.metaMap.get(this.mutable);
		if (acoustic == null)
			acoustic = this.metaMap.get(this.mutable.asGeneric());
		return acoustic;
	}

	@Override
	public String getBlockMapSubstrate(final Block block, final int meta, final Substrate substrate) {
		final Map<BlockInfo, String> sub = this.substrateMap.get(substrate);
		if (sub != null) {
			this.mutable.setBlock(block).setMeta(meta);
			String result = sub.get(this.mutable);
			if (result == null)
				result = sub.get(this.mutable.asGeneric());
			return result;
		}
		return null;
	}

	private void put(final Block block, final int meta, final String substrate, final String value) {
		final BlockInfo info = new BlockInfo(block, meta);
		if (StringUtils.isEmpty(substrate)) {
			this.metaMap.put(info, value);
		} else {
			final Substrate s = Substrate.get(substrate);
            Map<BlockInfo, String> sub = this.substrateMap.computeIfAbsent(s, k -> new HashMap<>());
            sub.put(info, value);
		}
	}

	private void expand(final Block block, final String value) {
		final List<MacroEntry> macro = macros.get(value);
		if (macro != null) {
			for (final MacroEntry entry : macro)
				put(block, entry.meta, entry.substrate, entry.value);
		} else {
			ModLog.debug("Unknown macro '%s'", value);
		}
	}

	@Override
	public void register(final String key, final String value) {
		final Matcher matcher = pattern.matcher(key);
		if (matcher.matches()) {
			final String blockName = matcher.group(1);
			final Block block = MCHelper.getBlockNameRaw(blockName);
			if (block != null && block != Blocks.air) {
				final int meta = matcher.group(2) == null ? -1 : Integer.parseInt(matcher.group(2));
				final String substrate = matcher.group(3);
				if (value.startsWith("#"))
					expand(block, value);
				else
					put(block, meta, substrate, value);
			} else {
				ModLog.debug("Unable to locate block for blockmap '%s'", blockName);
			}
		} else {
			ModLog.debug("Malformed key in blockmap '%s'", key);
		}
	}

	@Override
	public void collectData(final Block block, final int meta, final List<String> data) {

	}
}
