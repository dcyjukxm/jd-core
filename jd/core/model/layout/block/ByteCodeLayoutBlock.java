package jd.core.model.layout.block;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.LayoutBlock;

public class ByteCodeLayoutBlock extends LayoutBlock {
   public ClassFile classFile;
   public Method method;

   public ByteCodeLayoutBlock(ClassFile classFile, Method method) {
      super(56, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 0, 0);
      this.classFile = classFile;
      this.method = method;
   }
}
