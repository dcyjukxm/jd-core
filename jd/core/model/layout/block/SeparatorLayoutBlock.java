package jd.core.model.layout.block;

import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.LayoutBlock;

public class SeparatorLayoutBlock extends LayoutBlock {
   public SeparatorLayoutBlock(byte tag, int preferedLineCount) {
      super(tag, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, Integer.MAX_VALUE, preferedLineCount);
   }
}
