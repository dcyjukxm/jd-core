package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;
import jd.core.util.InvalidParameterException;

public class InvokestaticFactory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      int index = (code[offset + 1] & 255) << 8 | code[offset + 2] & 255;
      ConstantMethodref cmr = classFile.getConstantPool().getConstantMethodref(index);
      if(cmr == null) {
         throw new InvalidParameterException("Invalid ConstantMethodref index");
      } else {
         int nbrOfParameters = cmr.getNbrOfParameters();
         ArrayList args = new ArrayList(nbrOfParameters);

         for(int instruction = nbrOfParameters; instruction > 0; --instruction) {
            args.add((Instruction)stack.pop());
         }

         Collections.reverse(args);
         Invokestatic var16 = new Invokestatic(opcode, offset, lineNumber, index, args);
         if(cmr.returnAResult()) {
            stack.push(var16);
         } else {
            list.add(var16);
         }

         listForAnalyze.add(var16);
         return ByteCodeConstants.NO_OF_OPERANDS[opcode];
      }
   }
}
