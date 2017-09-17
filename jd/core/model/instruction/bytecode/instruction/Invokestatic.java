package jd.core.model.instruction.bytecode.instruction;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;

public class Invokestatic extends InvokeInstruction {
   public Invokestatic(int opcode, int offset, int lineNumber, int index, List<Instruction> args) {
      super(opcode, offset, lineNumber, index, args);
   }
}
