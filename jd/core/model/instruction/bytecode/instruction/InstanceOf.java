package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class InstanceOf extends IndexInstruction {
   public Instruction objectref;

   public InstanceOf(int opcode, int offset, int lineNumber, int index, Instruction objectref) {
      super(opcode, offset, lineNumber, index);
      this.objectref = objectref;
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      return "Z";
   }

   public int getPriority() {
      return 6;
   }
}
