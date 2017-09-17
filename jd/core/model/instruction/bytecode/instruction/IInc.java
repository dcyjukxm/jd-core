package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;

public class IInc extends IndexInstruction {
   public int count;

   public IInc(int opcode, int offset, int lineNumber, int index, int count) {
      super(opcode, offset, lineNumber, index);
      this.count = count;
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      if(constants != null && localVariables != null) {
         LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(this.index, this.offset);
         return lv != null && lv.signature_index > 0?constants.getConstantUtf8(lv.signature_index):null;
      } else {
         return null;
      }
   }

   public int getPriority() {
      return this.count != 1 && this.count != -1?14:2;
   }
}
