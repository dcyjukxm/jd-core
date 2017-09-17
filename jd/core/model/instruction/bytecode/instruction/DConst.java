package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.ConstInstruction;

public class DConst extends ConstInstruction {
   public DConst(int opcode, int offset, int lineNumber, int value) {
      super(opcode, offset, lineNumber, value);
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      return "D";
   }
}
