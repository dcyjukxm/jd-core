package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.util.SignatureUtil;

public class New extends IndexInstruction {
   public New(int opcode, int offset, int lineNumber, int index) {
      super(opcode, offset, lineNumber, index);
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      return constants == null?null:SignatureUtil.CreateTypeName(constants.getConstantClassName(this.index));
   }
}
