package jd.core.model.layout.block;

import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.LayoutBlock;

public class CaseBlockEndLayoutBlock extends LayoutBlock {
   public CaseBlockEndLayoutBlock() {
      super(34, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, Integer.MAX_VALUE, 1);
   }
}
