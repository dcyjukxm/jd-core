package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;

public class ALoadFactory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      int index;
      if(opcode == 25) {
         index = code[offset + 1] & 255;
      } else {
         index = opcode - 42;
      }

      ALoad instruction = new ALoad(25, offset, lineNumber, index);
      stack.push(instruction);
      listForAnalyze.add(instruction);
      return ByteCodeConstants.NO_OF_OPERANDS[opcode];
   }
}
