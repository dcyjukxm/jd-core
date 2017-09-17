package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.instruction.bytecode.instruction.IndexInstruction;

public abstract class LdcInstruction extends IndexInstruction {
   public LdcInstruction(int opcode, int offset, int lineNumber, int index) {
      super(opcode, offset, lineNumber, index);
   }
}
