package jd.core.model.layout.block;

import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.LayoutBlock;

public class CommentDeprecatedLayoutBlock extends LayoutBlock {
   public CommentDeprecatedLayoutBlock() {
      super(39, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 3, 3);
   }
}
