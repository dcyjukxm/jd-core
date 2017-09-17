package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.instruction.bytecode.instruction.ConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class IfCmp extends ConditionalBranchInstruction {
   public Instruction value1;
   public Instruction value2;

   public IfCmp(int opcode, int offset, int lineNumber, int cmp, Instruction value1, Instruction value2, int branch) {
      super(opcode, offset, lineNumber, cmp, branch);
      this.value1 = value1;
      this.value2 = value2;
   }

   public int getPriority() {
      switch(this.cmp) {
      case 0:
      case 7:
         return 7;
      default:
         return 6;
      }
   }
}
