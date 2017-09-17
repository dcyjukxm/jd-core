package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;

public class GetStatic extends IndexInstruction {
   public GetStatic(int opcode, int offset, int lineNumber, int index) {
      super(opcode, offset, lineNumber, index);
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      if(constants == null) {
         return null;
      } else {
         ConstantFieldref cfr = constants.getConstantFieldref(this.index);
         if(cfr == null) {
            return null;
         } else {
            ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
            return cnat == null?null:constants.getConstantUtf8(cnat.descriptor_index);
         }
      }
   }
}
