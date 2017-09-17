package jd.core.model.layout.block;

import jd.core.model.classfile.ClassFile;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.LayoutBlock;

public class ExtendsSuperTypeLayoutBlock extends LayoutBlock {
   public ClassFile classFile;

   public ExtendsSuperTypeLayoutBlock(ClassFile classFile) {
      this(43, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 1, 1, classFile);
   }

   protected ExtendsSuperTypeLayoutBlock(byte tag, int firstLineNumber, int lastLineNumber, int minimalLineCount, int maximalLineCount, int preferedLineCount, ClassFile classFile) {
      super(tag, firstLineNumber, lastLineNumber, minimalLineCount, maximalLineCount, preferedLineCount);
      this.classFile = classFile;
   }
}
