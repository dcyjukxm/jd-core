package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public abstract class InstructionFactory {
   public abstract int create(ClassFile var1, Method var2, List<Instruction> var3, List<Instruction> var4, Stack<Instruction> var5, byte[] var6, int var7, int var8, boolean[] var9);
}
