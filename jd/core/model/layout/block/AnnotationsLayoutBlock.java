package jd.core.model.layout.block;

import java.util.ArrayList;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.attribute.Annotation;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.LayoutBlock;

public class AnnotationsLayoutBlock extends LayoutBlock {
   public ClassFile classFile;
   public ArrayList<Annotation> annotations;

   public AnnotationsLayoutBlock(ClassFile classFile, ArrayList<Annotation> annotations) {
      this(classFile, annotations, annotations.size());
   }

   private AnnotationsLayoutBlock(ClassFile classFile, ArrayList<Annotation> annotations, int maxLineCount) {
      super(41, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, maxLineCount, maxLineCount);
      this.classFile = classFile;
      this.annotations = annotations;
   }
}
