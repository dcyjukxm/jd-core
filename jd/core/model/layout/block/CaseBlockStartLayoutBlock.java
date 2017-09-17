package jd.core.model.layout.block;

import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.LayoutBlock;

public class CaseBlockStartLayoutBlock extends LayoutBlock {
   public CaseBlockStartLayoutBlock() {
      super(33, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 1, 1);
   }
}
