package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;

public class IfFactory extends InstructionFactory {
   protected int cmp;

   public IfFactory(int cmp) {
      this.cmp = cmp;
   }

   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      short branch = (short)((code[offset + 1] & 255) << 8 | code[offset + 2] & 255);
      Instruction value = (Instruction)stack.pop();
      list.add(new IfInstruction(260, offset, lineNumber, this.cmp, value, branch));
      return ByteCodeConstants.NO_OF_OPERANDS[opcode];
   }
}
