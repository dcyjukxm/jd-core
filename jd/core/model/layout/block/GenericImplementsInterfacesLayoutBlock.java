package jd.core.model.layout.block;

import jd.core.model.classfile.ClassFile;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.ImplementsInterfacesLayoutBlock;

public class GenericImplementsInterfacesLayoutBlock extends ImplementsInterfacesLayoutBlock {
   public char[] caSignature;
   public int signatureIndex;

   public GenericImplementsInterfacesLayoutBlock(ClassFile classFile, char[] caSignature, int signatureIndex) {
      super(49, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 1, 1, classFile);
      this.caSignature = caSignature;
      this.signatureIndex = signatureIndex;
   }
}
