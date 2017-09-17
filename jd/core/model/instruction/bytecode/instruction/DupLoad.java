package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class DupLoad extends Instruction {
   public DupStore dupStore;

   public DupLoad(int opcode, int offset, int lineNumber, DupStore dupStore) {
      super(opcode, offset, lineNumber);
      this.dupStore = dupStore;
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      if(this.dupStore == null) {
         throw new RuntimeException("DupLoad without DupStore");
      } else {
         return this.dupStore.getReturnedSignature(constants, localVariables);
      }
   }
}
