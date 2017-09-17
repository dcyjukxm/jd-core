package jd.core.model.instruction.fast.instruction;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.fast.instruction.FastList;

public class FastSynchronized extends FastList {
   public Instruction monitor = null;

   public FastSynchronized(int opcode, int offset, int lineNumber, int branch, List<Instruction> instructions) {
      super(opcode, offset, lineNumber, branch, instructions);
   }
}
