package jd.core.model.layout.block;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.LayoutBlock;

public class MethodNameLayoutBlock extends LayoutBlock {
   public ClassFile classFile;
   public Method method;
   public String signature;
   public boolean descriptorFlag;
   public boolean nullCodeFlag;

   public MethodNameLayoutBlock(ClassFile classFile, Method method, String signature, boolean descriptorFlag, boolean nullCodeFlag) {
      super(52, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 0, 0);
      this.classFile = classFile;
      this.method = method;
      this.signature = signature;
      this.descriptorFlag = descriptorFlag;
      this.nullCodeFlag = nullCodeFlag;
   }
}
