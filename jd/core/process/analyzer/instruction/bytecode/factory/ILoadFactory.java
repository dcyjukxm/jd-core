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

public class ILoadFactory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      int index;
      if(opcode == 21) {
         index = code[offset + 1] & 255;
      } else {
         index = opcode - 26;
      }

      Object instruction = new ILoad(21, offset, lineNumber, index);
      if(!stack.isEmpty() && !jumps[offset]) {
         Instruction last = (Instruction)stack.lastElement();
         if(last.opcode == 132) {
            if(((IInc)last).index == index) {
               listForAnalyze.add(instruction);
               IInc iinc = (IInc)last;
               stack.pop();
               stack.push(instruction = new IncInstruction(277, iinc.offset, iinc.lineNumber, (Instruction)instruction, iinc.count));
            } else {
               stack.pop();
               list.add(last);
               listForAnalyze.add(last);
               stack.push(instruction);
            }
         } else {
            stack.push(instruction);
         }
      } else {
         stack.push(instruction);
      }

      listForAnalyze.add(instruction);
      return ByteCodeConstants.NO_OF_OPERANDS[opcode];
   }
}
