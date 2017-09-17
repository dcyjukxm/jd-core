package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;

public class AStoreFactory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      int index;
      if(opcode == 58) {
         index = code[offset + 1] & 255;
      } else {
         index = (code[offset] & 255) - 75;
      }

      AStore instruction = new AStore(58, offset, lineNumber, index, (Instruction)stack.pop());
      list.add(instruction);
      listForAnalyze.add(instruction);
      return ByteCodeConstants.NO_OF_OPERANDS[opcode];
   }
}
