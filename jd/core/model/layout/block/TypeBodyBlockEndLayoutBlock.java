package jd.core.model.layout.block;

import jd.core.model.layout.block.BlockLayoutBlock;

public class TypeBodyBlockEndLayoutBlock extends BlockLayoutBlock {
   public TypeBodyBlockEndLayoutBlock() {
      this(1);
   }

   public TypeBodyBlockEndLayoutBlock(int preferedLineCount) {
      super(14, 0, 1, preferedLineCount);
   }
}
