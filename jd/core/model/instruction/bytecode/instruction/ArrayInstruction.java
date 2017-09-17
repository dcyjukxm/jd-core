package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.instruction.bytecode.instruction.Instruction;

public abstract class ArrayInstruction extends Instruction {
   public Instruction arrayref;

   public ArrayInstruction(int opcode, int offset, int lineNumber, Instruction arrayref) {
      super(opcode, offset, lineNumber);
      this.arrayref = arrayref;
   }
}
