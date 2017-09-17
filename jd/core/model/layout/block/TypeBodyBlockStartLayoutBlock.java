package jd.core.model.layout.block;

import jd.core.model.layout.block.BlockLayoutBlock;

public class TypeBodyBlockStartLayoutBlock extends BlockLayoutBlock {
   public TypeBodyBlockStartLayoutBlock() {
      this(2);
   }

   public TypeBodyBlockStartLayoutBlock(int preferedLineCount) {
      super(13, 0, 2, preferedLineCount);
   }

   public void transformToStartEndBlock(int preferedLineCount) {
      this.tag = 15;
      this.preferedLineCount = this.lineCount = preferedLineCount;
   }
}
