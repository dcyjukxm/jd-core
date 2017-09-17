package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.instruction.bytecode.instruction.Instruction;

public abstract class ConstInstruction extends Instruction {
   public int value;

   public ConstInstruction(int opcode, int offset, int lineNumber, int value) {
      super(opcode, offset, lineNumber);
      this.value = value;
   }
}
