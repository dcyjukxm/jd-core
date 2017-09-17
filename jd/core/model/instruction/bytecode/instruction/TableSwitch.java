package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Switch;

public class TableSwitch extends Switch {
   public int low;
   public int high;

   public TableSwitch(int opcode, int offset, int lineNumber, Instruction key, int defaultOffset, int[] offsets, int low, int high) {
      super(opcode, offset, lineNumber, key, defaultOffset, offsets);
      this.low = low;
      this.high = high;
   }
}
