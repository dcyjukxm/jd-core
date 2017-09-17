package jd.core.model.layout.block;

import jd.core.model.layout.block.BlockLayoutBlock;

public class MethodBodyBlockStartLayoutBlock extends BlockLayoutBlock {
   public MethodBodyBlockStartLayoutBlock() {
      this(2);
   }

   public MethodBodyBlockStartLayoutBlock(int preferedLineCount) {
      super(19, 0, Integer.MAX_VALUE, preferedLineCount);
   }

   public void transformToStartEndBlock(int preferedLineCount) {
      this.tag = 21;
      this.preferedLineCount = this.lineCount = preferedLineCount;
   }

   public void transformToSingleLineBlock() {
      this.tag = 22;
   }
}
