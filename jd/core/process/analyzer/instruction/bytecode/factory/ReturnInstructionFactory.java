package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;

public class ReturnInstructionFactory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      ReturnInstruction ri = new ReturnInstruction(273, offset, lineNumber, (Instruction)stack.pop());
      list.add(ri);
      listForAnalyze.add(ri);
      return ByteCodeConstants.NO_OF_OPERANDS[opcode];
   }
}
