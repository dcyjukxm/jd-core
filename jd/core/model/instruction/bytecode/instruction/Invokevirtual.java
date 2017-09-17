package jd.core.model.instruction.bytecode.instruction;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;

public class Invokevirtual extends InvokeNoStaticInstruction {
   public Invokevirtual(int opcode, int offset, int lineNumber, int index, Instruction objectref, List<Instruction> args) {
      super(opcode, offset, lineNumber, index, objectref, args);
   }
}
