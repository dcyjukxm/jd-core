package jd.core.model.layout.block;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.model.layout.block.LayoutBlock;

public class FastCatchLayoutBlock extends LayoutBlock {
   public ClassFile classFile;
   public Method method;
   public FastTry.FastCatch fc;

   public FastCatchLayoutBlock(ClassFile classFile, Method method, FastTry.FastCatch fc) {
      super(68, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 0, 0);
      this.classFile = classFile;
      this.method = method;
      this.fc = fc;
   }
}
