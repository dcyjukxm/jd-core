package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.constant.ConstantInterfaceMethodref;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokeinterface;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;
import jd.core.util.InvalidParameterException;

public class InvokeinterfaceFactory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      int index = (code[offset + 1] & 255) << 8 | code[offset + 2] & 255;
      ConstantInterfaceMethodref cimr = classFile.getConstantPool().getConstantInterfaceMethodref(index);
      if(cimr == null) {
         throw new InvalidParameterException("Invalid ConstantInterfaceMethodref index");
      } else {
         int nbrOfParameters = cimr.getNbrOfParameters();
         ArrayList args = new ArrayList(nbrOfParameters);

         for(int objectref = nbrOfParameters; objectref > 0; --objectref) {
            args.add((Instruction)stack.pop());
         }

         Collections.reverse(args);
         Instruction var17 = (Instruction)stack.pop();
         Invokeinterface instruction = new Invokeinterface(opcode, offset, lineNumber, index, var17, args);
         if(cimr.returnAResult()) {
            stack.push(instruction);
         } else {
            list.add(instruction);
         }

         listForAnalyze.add(instruction);
         return ByteCodeConstants.NO_OF_OPERANDS[opcode];
      }
   }
}
