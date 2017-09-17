package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class ANewArray extends IndexInstruction {
   public Instruction dimension;

   public ANewArray(int opcode, int offset, int lineNumber, int index, Instruction dimension) {
      super(opcode, offset, lineNumber, index);
      this.dimension = dimension;
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      if(constants == null) {
         return null;
      } else {
         String str = constants.getConstantClassName(this.index);
         return str.length() == 0?null:(str.charAt(0) == 91?"[" + constants.getConstantClassName(this.index):"[L" + constants.getConstantClassName(this.index) + ';');
      }
   }
}
