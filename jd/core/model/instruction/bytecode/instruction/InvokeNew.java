package jd.core.model.instruction.bytecode.instruction;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;

public class InvokeNew extends InvokeInstruction {
   public int enumValueFieldRefIndex = 0;

   public InvokeNew(int opcode, int offset, int lineNumber, int index, List<Instruction> args) {
      super(opcode, offset, lineNumber, index, args);
   }

   public void transformToEnumValue(GetStatic getStatic) {
      this.opcode = 321;
      this.enumValueFieldRefIndex = getStatic.index;
   }
}
