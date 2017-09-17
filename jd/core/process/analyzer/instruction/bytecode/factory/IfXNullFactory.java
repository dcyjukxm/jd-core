package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;

public class IfXNullFactory extends InstructionFactory {
   public int cmp;

   public IfXNullFactory(int cmp) {
      this.cmp = cmp;
   }

   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      short branch = (short)((code[offset + 1] & 255) << 8 | code[offset + 2] & 255);
      list.add(new IfInstruction(262, offset, lineNumber, this.cmp, (Instruction)stack.pop(), branch));
      if(!stack.isEmpty()) {
         Instruction instruction = (Instruction)stack.lastElement();
         if(instruction.opcode == 263) {
            int nextOffset = offset + ByteCodeConstants.NO_OF_OPERANDS[opcode] + 1;
            if(nextOffset < code.length) {
               switch(code[nextOffset] & 255) {
               case 87:
               case 176:
                  DupLoad dp = (DupLoad)instruction;
                  stack.push(new DupLoad(dp.opcode, dp.offset, dp.lineNumber, dp.dupStore));
               }
            }
         }
      }

      return ByteCodeConstants.NO_OF_OPERANDS[opcode];
   }
}
