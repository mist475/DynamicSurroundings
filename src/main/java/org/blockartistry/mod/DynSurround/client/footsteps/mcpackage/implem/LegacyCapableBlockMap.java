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

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.compat.MCHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;

@SideOnly(Side.CLIENT)
public class LegacyCapableBlockMap extends BasicBlockMap {
	@Override
	public void register(final String key, final String value) {
		try {
			int endOfNumber = key.indexOf('^');
			if (endOfNumber == -1) {
				endOfNumber = key.indexOf('.');
			}
			if (endOfNumber == -1) {
				endOfNumber = key.length();
			}
			final String number = key.substring(0, endOfNumber);
			final int id = Integer.parseInt(number);
			final Object o = Block.blockRegistry.getObjectById(id);
			if (o instanceof Block) {
				final String fullKeyRebuild = MCHelper.nameOf((Block) o)
						+ (endOfNumber == key.length() ? "" : key.substring(endOfNumber));
				super.register(fullKeyRebuild, value);
				ModLog.debug("Adding legacy key: " + fullKeyRebuild + " for " + key);
			} else {
				super.register(key, value);
			}
		} catch (final NumberFormatException e) {
			super.register(key, value);
		}
	}
}
