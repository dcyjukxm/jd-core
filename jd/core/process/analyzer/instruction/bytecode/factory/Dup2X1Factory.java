package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;

public class Dup2X1Factory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      Instruction i1 = (Instruction)stack.pop();
      Instruction i2 = (Instruction)stack.pop();
      String signature1 = i1.getReturnedSignature(classFile.getConstantPool(), method.getLocalVariables());
      if(!"J".equals(signature1) && !"D".equals(signature1)) {
         Instruction i31 = (Instruction)stack.pop();
         DupStore dupStore1 = new DupStore(264, offset, lineNumber, i1);
         DupStore dupStore2 = new DupStore(264, offset, lineNumber, i2);
         list.add(dupStore1);
         list.add(dupStore2);
         stack.push(dupStore2.getDupLoad1());
         stack.push(dupStore1.getDupLoad1());
         stack.push(i31);
         stack.push(dupStore2.getDupLoad2());
         stack.push(dupStore1.getDupLoad2());
      } else {
         DupStore i3 = new DupStore(264, offset, lineNumber, i1);
         list.add(i3);
         stack.push(i3.getDupLoad1());
         stack.push(i2);
         stack.push(i3.getDupLoad2());
      }

      return ByteCodeConstants.NO_OF_OPERANDS[opcode];
   }
}
