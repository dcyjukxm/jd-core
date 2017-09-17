package jd.core.model.instruction.fast.instruction;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.fast.instruction.FastTestList;

public class FastFor extends FastTestList {
   public Instruction init;
   public Instruction inc;

   public FastFor(int opcode, int offset, int lineNumber, int branch, Instruction init, Instruction test, Instruction inc, List<Instruction> instructions) {
      super(opcode, offset, lineNumber, branch, test, instructions);
      this.init = init;
      this.inc = inc;
   }
}
