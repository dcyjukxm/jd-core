package jd.core.model.layout.block;

import jd.core.model.classfile.ClassFile;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.ExtendsSuperTypeLayoutBlock;

public class GenericExtendsSuperTypeLayoutBlock extends ExtendsSuperTypeLayoutBlock {
   public char[] caSignature;
   public int signatureIndex;

   public GenericExtendsSuperTypeLayoutBlock(ClassFile classFile, char[] caSignature, int signatureIndex) {
      super(47, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 1, 1, classFile);
      this.caSignature = caSignature;
      this.signatureIndex = signatureIndex;
   }
}
