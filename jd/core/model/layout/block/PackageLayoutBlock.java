package jd.core.model.layout.block;

import jd.core.model.classfile.ClassFile;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.LayoutBlock;

public class PackageLayoutBlock extends LayoutBlock {
   public ClassFile classFile;

   public PackageLayoutBlock(ClassFile classFile) {
      super(1, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 0, 0);
      this.classFile = classFile;
   }
}
