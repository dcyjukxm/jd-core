package jd.core.model.layout.block;

import jd.core.model.layout.block.BlockLayoutBlock;

public class InnerTypeBodyBlockStartLayoutBlock extends BlockLayoutBlock {
   public InnerTypeBodyBlockStartLayoutBlock() {
      super(16, 0, Integer.MAX_VALUE, 2);
   }

   public void transformToStartEndBlock() {
      this.tag = 18;
      this.preferedLineCount = this.lineCount = 0;
   }
}
