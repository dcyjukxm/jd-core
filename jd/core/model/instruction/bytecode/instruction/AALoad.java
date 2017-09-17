package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class AALoad extends ArrayLoadInstruction {
   public AALoad(int opcode, int offset, int lineNumber, Instruction arrayref, Instruction indexref) {
      super(opcode, offset, lineNumber, arrayref, indexref, (String)null);
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      String signature = this.arrayref.getReturnedSignature(constants, localVariables);
      return signature != null && signature.length() != 0 && signature.charAt(0) == 91?signature.substring(1):null;
   }
}
