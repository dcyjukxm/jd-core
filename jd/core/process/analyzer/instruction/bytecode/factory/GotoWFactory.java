package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;

public class GotoWFactory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      int value = (code[offset + 1] & 255) << 24 | (code[offset + 2] & 255) << 16 | (code[offset + 3] & 255) << 8 | code[offset + 4] & 255;
      list.add(new Goto(167, offset, lineNumber, value));
      return ByteCodeConstants.NO_OF_OPERANDS[opcode];
   }
}
