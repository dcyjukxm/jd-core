package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.instruction.bytecode.instruction.Instruction;

public abstract class BranchInstruction extends Instruction {
   public int branch;

   public BranchInstruction(int opcode, int offset, int lineNumber, int branch) {
      super(opcode, offset, lineNumber);
      this.branch = branch;
   }

   public int GetJumpOffset() {
      return this.offset + this.branch;
   }

   public void SetJumpOffset(int jumpOffset) {
      this.branch = jumpOffset - this.offset;
   }
}
