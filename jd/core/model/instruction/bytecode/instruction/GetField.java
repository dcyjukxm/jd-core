package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class GetField extends IndexInstruction {
   public Instruction objectref;

   public GetField(int opcode, int offset, int lineNumber, int index, Instruction objectref) {
      super(opcode, offset, lineNumber, index);
      this.objectref = objectref;
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
