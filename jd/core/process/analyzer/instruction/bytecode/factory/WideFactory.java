package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.IInc;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.Ret;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;
import jd.core.process.analyzer.instruction.bytecode.factory.UnexpectedOpcodeException;

public class WideFactory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset + 1] & 255;
      int index = (code[offset + 2] & 255) << 8 | code[offset + 3] & 255;
      if(opcode == 132) {
         short instruction2 = (short)((code[offset + 4] & 255) << 8 | code[offset + 5] & 255);
         IInc instruction1 = new IInc(opcode, offset, lineNumber, index, instruction2);
         list.add(instruction1);
         listForAnalyze.add(instruction1);
         return 5;
      } else {
         if(opcode == 169) {
            list.add(new Ret(opcode, offset, lineNumber, index));
         } else {
            Object instruction = null;
            switch(opcode) {
            case 21:
               instruction = new LoadInstruction(21, offset, lineNumber, index, "I");
               stack.push(instruction);
               break;
            case 22:
               instruction = new LoadInstruction(268, offset, lineNumber, index, "J");
               stack.push(instruction);
               break;
            case 23:
               instruction = new LoadInstruction(268, offset, lineNumber, index, "F");
               stack.push(instruction);
               break;
            case 24:
               instruction = new LoadInstruction(268, offset, lineNumber, index, "D");
               stack.push(instruction);
               break;
            case 25:
               instruction = new ALoad(25, offset, lineNumber, index);
               stack.push(instruction);
               break;
            case 54:
               instruction = new StoreInstruction(54, offset, lineNumber, index, "I", (Instruction)stack.pop());
               list.add(instruction);
               break;
            case 55:
               instruction = new StoreInstruction(269, offset, lineNumber, index, "J", (Instruction)stack.pop());
               list.add(instruction);
               break;
            case 56:
               instruction = new StoreInstruction(269, offset, lineNumber, index, "F", (Instruction)stack.pop());
               list.add(instruction);
               break;
            case 57:
               instruction = new StoreInstruction(269, offset, lineNumber, index, "D", (Instruction)stack.pop());
               list.add(instruction);
               break;
            case 58:
               instruction = new AStore(58, offset, lineNumber, index, (Instruction)stack.pop());
               list.add(instruction);
               break;
            default:
               throw new UnexpectedOpcodeException(opcode);
            }

            listForAnalyze.add(instruction);
         }

         return 2;
      }
   }
}
