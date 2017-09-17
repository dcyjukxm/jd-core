package jd.core.model.layout.block;

import jd.core.model.layout.block.BlockLayoutBlock;

public class StatementsBlockStartLayoutBlock extends BlockLayoutBlock {
   public StatementsBlockStartLayoutBlock() {
      this(2);
   }

   public StatementsBlockStartLayoutBlock(int preferedLineCount) {
      super(25, 0, Integer.MAX_VALUE, preferedLineCount);
   }

   public void transformToStartEndBlock(int preferedLineCount) {
      this.tag = 27;
      this.preferedLineCount = this.lineCount = preferedLineCount;
   }
}
