package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class ReturnAddressLoad extends Instruction {
   public ReturnAddressLoad(int opcode, int offset, int lineNumber) {
      super(opcode, offset, lineNumber);
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      return null;
   }
}
