package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;

public class ExceptionLoad extends IndexInstruction {
   public final int exceptionNameIndex;

   public ExceptionLoad(int opcode, int offset, int lineNumber, int signatureIndex) {
      super(opcode, offset, lineNumber, -1);
      this.exceptionNameIndex = signatureIndex;
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      return constants != null && this.exceptionNameIndex != 0?constants.getConstantUtf8(this.exceptionNameIndex):null;
   }
}
