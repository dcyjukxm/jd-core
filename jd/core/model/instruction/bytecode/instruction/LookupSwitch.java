package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Switch;

public class LookupSwitch extends Switch {
   public int[] keys;

   public LookupSwitch(int opcode, int offset, int lineNumber, Instruction key, int defaultOffset, int[] offsets, int[] keys) {
      super(opcode, offset, lineNumber, key, defaultOffset, offsets);
      this.keys = keys;
   }
}
