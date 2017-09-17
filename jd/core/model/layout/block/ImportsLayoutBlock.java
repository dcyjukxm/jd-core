package jd.core.model.layout.block;

import jd.core.model.classfile.ClassFile;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.LayoutBlock;

public class ImportsLayoutBlock extends LayoutBlock {
   public ClassFile classFile;

   public ImportsLayoutBlock(ClassFile classFile, int maxLineCount) {
      super(6, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, maxLineCount, maxLineCount);
      this.classFile = classFile;
   }
}
