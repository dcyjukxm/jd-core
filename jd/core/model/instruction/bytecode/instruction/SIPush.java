package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.instruction.bytecode.instruction.IConst;

public class SIPush extends IConst {
   public SIPush(int opcode, int offset, int lineNumber, int value) {
      super(opcode, offset, lineNumber, value);
   }
}
