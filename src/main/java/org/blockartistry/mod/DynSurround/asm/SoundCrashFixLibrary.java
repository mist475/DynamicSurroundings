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

package org.blockartistry.mod.DynSurround.asm;

import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

//Based on patches by CreativeMD
public class SoundCrashFixLibrary extends Transmorgrifier {

	private static final String[] classNames = { "paulscode.sound.Library" };

	public SoundCrashFixLibrary() {
		super(classNames);
	}

	@Override
	public String name() {
		return "removeSource";
	}

	@Override
	public boolean transmorgrify(final ClassNode cn) {
		MethodNode m = findMethod(cn, "removeSource", "(Ljava/lang/String;)V");
		if (m != null) {
			for (Iterator<?> iterator = m.instructions.iterator(); iterator.hasNext();) {
				final AbstractInsnNode insn = (AbstractInsnNode) iterator.next();
				if (insn instanceof MethodInsnNode && ((MethodInsnNode) insn).owner.equals("paulscode/sound/Source")
						&& ((MethodInsnNode) insn).name.equals("cleanup")) {
					m.instructions.insertBefore(insn,
							new MethodInsnNode(Opcodes.INVOKESTATIC,
									"org/blockartistry/mod/DynSurround/client/sound/fix/SoundFixMethods", "cleanupSource",
									"(Lpaulscode/sound/Source;)V", false));
					m.instructions.remove(insn);
					return true;
				}
			}
		}

		return false;
	}

}
