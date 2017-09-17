package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.constant.ConstantValue;
import jd.core.model.instruction.bytecode.instruction.LdcInstruction;

public class Ldc2W extends LdcInstruction {
   public Ldc2W(int opcode, int offset, int lineNumber, int index) {
      super(opcode, offset, lineNumber, index);
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      if(constants == null) {
         return null;
      } else {
         ConstantValue cv = constants.getConstantValue(this.index);
         return cv == null?null:(cv.tag == 6?"D":"J");
      }
   }
}
