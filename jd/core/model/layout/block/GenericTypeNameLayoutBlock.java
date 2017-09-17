package jd.core.model.layout.block;

import jd.core.model.classfile.ClassFile;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.TypeNameLayoutBlock;

public class GenericTypeNameLayoutBlock extends TypeNameLayoutBlock {
   public String signature;

   public GenericTypeNameLayoutBlock(ClassFile classFile, String signature) {
      super(46, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 0, 0, classFile);
      this.signature = signature;
   }
}
