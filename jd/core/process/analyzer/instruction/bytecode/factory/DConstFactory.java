package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.DConst;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;

public class DConstFactory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      int index = opcode - 14;
      stack.push(new DConst(259, offset, lineNumber, index));
      return ByteCodeConstants.NO_OF_OPERANDS[opcode];
   }
}
