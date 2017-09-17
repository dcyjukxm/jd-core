package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;

public class DupX2Factory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      Instruction i1 = (Instruction)stack.pop();
      Instruction i2 = (Instruction)stack.pop();
      DupStore dupStore1 = new DupStore(264, offset, lineNumber, i1);
      list.add(dupStore1);
      String signature2 = i2.getReturnedSignature(classFile.getConstantPool(), (LocalVariables)null);
      if(!"J".equals(signature2) && !"D".equals(signature2)) {
         Instruction i3 = (Instruction)stack.pop();
         stack.push(dupStore1.getDupLoad1());
         stack.push(i3);
         stack.push(i2);
         stack.push(dupStore1.getDupLoad2());
      } else {
         stack.push(dupStore1.getDupLoad1());
         stack.push(i2);
         stack.push(dupStore1.getDupLoad2());
      }

      return ByteCodeConstants.NO_OF_OPERANDS[opcode];
   }
}
