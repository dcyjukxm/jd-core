package jd.core.model.layout.block;

import jd.core.model.layout.section.LayoutSection;

public class LayoutBlock {
   public byte tag;
   public int firstLineNumber;
   public int lastLineNumber;
   public int minimalLineCount;
   public int maximalLineCount;
   public int preferedLineCount;
   public int lineCount;
   public int index;
   public LayoutSection section;

   public LayoutBlock(byte tag, int firstLineNumber, int lastLineNumber, int lineCount) {
      this.tag = tag;
      this.firstLineNumber = firstLineNumber;
      this.lastLineNumber = lastLineNumber;
      this.minimalLineCount = lineCount;
      this.maximalLineCount = lineCount;
      this.preferedLineCount = lineCount;
      this.lineCount = lineCount;
      this.index = 0;
      this.section = null;
   }

   public LayoutBlock(byte tag, int firstLineNumber, int lastLineNumber, int minimalLineCount, int maximalLineCount, int preferedLineCount) {
      this.tag = tag;
      this.firstLineNumber = firstLineNumber;
      this.lastLineNumber = lastLineNumber;
      this.minimalLineCount = minimalLineCount;
      this.maximalLineCount = maximalLineCount;
      this.preferedLineCount = preferedLineCount;
      this.lineCount = preferedLineCount;
      this.index = 0;
      this.section = null;
   }
}
