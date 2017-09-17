package jd.core.process.layouter.visitor;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.layout.block.InstructionLayoutBlock;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.preferences.Preferences;
import jd.core.process.layouter.ClassFileLayouter;
import jd.core.process.layouter.visitor.BaseInstructionSplitterVisitor;
import jd.core.process.layouter.visitor.MaxLineNumberVisitor;
import jd.core.process.layouter.visitor.MinLineNumberVisitor;

public class InstructionSplitterVisitor extends BaseInstructionSplitterVisitor {
   protected Preferences preferences;
   protected List<LayoutBlock> layoutBlockList;
   protected Method method;
   protected Instruction instruction;
   protected int firstLineNumber;
   protected int offset1;

   public void start(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, Instruction instruction) {
      super.start(classFile);
      this.preferences = preferences;
      this.layoutBlockList = layoutBlockList;
      this.method = method;
      this.instruction = instruction;
      this.firstLineNumber = MinLineNumberVisitor.visit(instruction);
      this.offset1 = 0;
   }

   public void end() {
      if(this.offset1 == 0 || this.offset1 != this.instruction.offset) {
         int lastLineNumber = MaxLineNumberVisitor.visit(this.instruction);
         int preferedLineNumber;
         if(this.firstLineNumber != Instruction.UNKNOWN_LINE_NUMBER && lastLineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
            preferedLineNumber = lastLineNumber - this.firstLineNumber;
         } else {
            preferedLineNumber = Integer.MAX_VALUE;
         }

         this.layoutBlockList.add(new InstructionLayoutBlock(54, this.firstLineNumber, lastLineNumber, preferedLineNumber, preferedLineNumber, preferedLineNumber, this.classFile, this.method, this.instruction, this.offset1, this.instruction.offset));
      }

   }

   public void visitAnonymousNewInvoke(Instruction parent, InvokeNew in, ClassFile innerClassFile) {
      int lastLineNumber = MaxLineNumberVisitor.visit(in);
      int preferedLineNumber;
      if(this.firstLineNumber != Instruction.UNKNOWN_LINE_NUMBER && lastLineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
         preferedLineNumber = lastLineNumber - this.firstLineNumber;
      } else {
         preferedLineNumber = Integer.MAX_VALUE;
      }

      this.layoutBlockList.add(new InstructionLayoutBlock(54, this.firstLineNumber, lastLineNumber, preferedLineNumber, preferedLineNumber, preferedLineNumber, this.classFile, this.method, this.instruction, this.offset1, in.offset));
      this.firstLineNumber = parent.lineNumber;
      this.offset1 = in.offset;
      ClassFileLayouter.CreateBlocksForBodyOfAnonymousClass(this.preferences, innerClassFile, this.layoutBlockList);
   }
}
