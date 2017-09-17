package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.IInc;
import jd.core.model.instruction.bytecode.instruction.ILoad;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;

public class IIncFactory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      byte index = code[offset + 1];
      byte count = code[offset + 2];
      IInc instruction;
      if(!stack.isEmpty() && !jumps[offset]) {
         Instruction instruction1 = (Instruction)stack.lastElement();
         if(instruction1.opcode == 21 && ((ILoad)instruction1).index == index) {
            stack.pop();
            IncInstruction instruction2;
            stack.push(instruction2 = new IncInstruction(278, offset, lineNumber, instruction1, count));
            listForAnalyze.add(instruction2);
         } else if(count != -1 && count != 1) {
            list.add(instruction = new IInc(opcode, offset, lineNumber, index, count));
            listForAnalyze.add(instruction);
         } else {
            stack.push(new IInc(opcode, offset, lineNumber, index, count));
         }
      } else {
         list.add(instruction = new IInc(opcode, offset, lineNumber, index, count));
         listForAnalyze.add(instruction);
      }

      return ByteCodeConstants.NO_OF_OPERANDS[opcode];
   }
}
