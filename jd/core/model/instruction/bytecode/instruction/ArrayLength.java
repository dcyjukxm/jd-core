package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.ArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class ArrayLength extends ArrayInstruction {
   public ArrayLength(int opcode, int offset, int lineNumber, Instruction arrayref) {
      super(opcode, offset, lineNumber, arrayref);
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      return "I";
   }
}
