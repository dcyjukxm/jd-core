package jd.core.process.layouter.visitor;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.layout.block.InstructionsLayoutBlock;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.preferences.Preferences;
import jd.core.process.layouter.ClassFileLayouter;
import jd.core.process.layouter.visitor.BaseInstructionSplitterVisitor;
import jd.core.process.layouter.visitor.MaxLineNumberVisitor;

public class InstructionsSplitterVisitor extends BaseInstructionSplitterVisitor {
   protected Preferences preferences;
   protected List<LayoutBlock> layoutBlockList;
   protected Method method;
   protected List<Instruction> list;
   protected int firstLineNumber;
   protected int maxLineNumber;
   protected int initialIndex1;
   protected int index1;
   protected int index2;
   protected int offset1;

   public void start(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, List<Instruction> list, int index1) {
      super.start(classFile);
      this.preferences = preferences;
      this.layoutBlockList = layoutBlockList;
      this.method = method;
      this.list = list;
      this.firstLineNumber = this.maxLineNumber = Instruction.UNKNOWN_LINE_NUMBER;
      this.initialIndex1 = this.index1 = index1;
      this.offset1 = 0;
   }

   public void end() {
      int lastOffset = ((Instruction)this.list.get(this.index2)).offset;
      if(this.index1 != this.index2 || this.offset1 != lastOffset) {
         int lastLineNumber = Instruction.UNKNOWN_LINE_NUMBER;

         for(int j = this.index2; j >= this.index1; --j) {
            Instruction instruction = (Instruction)this.list.get(j);
            if(instruction.lineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
               lastLineNumber = MaxLineNumberVisitor.visit(instruction);
               break;
            }
         }

         this.addInstructionsLayoutBlock(lastLineNumber, lastOffset);
      }

   }

   public void setIndex2(int index2) {
      this.index2 = index2;
   }

   public void visit(Instruction instruction) {
      if(this.firstLineNumber == Instruction.UNKNOWN_LINE_NUMBER) {
         int initialFirstLineNumber = ((Instruction)this.list.get(this.initialIndex1)).lineNumber;
         if(initialFirstLineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
            if(initialFirstLineNumber < instruction.lineNumber) {
               this.firstLineNumber = instruction.lineNumber - 1;
            } else {
               this.firstLineNumber = instruction.lineNumber;
            }
         }
      }

      super.visit((Instruction)null, instruction);
   }

   protected void visit(Instruction parent, Instruction instruction) {
      if(instruction.lineNumber == Instruction.UNKNOWN_LINE_NUMBER) {
         instruction.lineNumber = this.maxLineNumber;
      } else if(this.maxLineNumber == Instruction.UNKNOWN_LINE_NUMBER) {
         this.maxLineNumber = instruction.lineNumber;
      } else if(instruction.lineNumber < this.maxLineNumber) {
         instruction.lineNumber = this.maxLineNumber;
      }

      if(this.firstLineNumber == Instruction.UNKNOWN_LINE_NUMBER) {
         this.firstLineNumber = instruction.lineNumber;
      }

      super.visit(parent, instruction);
   }

   public void visitAnonymousNewInvoke(Instruction parent, InvokeNew in, ClassFile innerClassFile) {
      this.addInstructionsLayoutBlock(in.lineNumber, in.offset);
      this.maxLineNumber = ClassFileLayouter.CreateBlocksForBodyOfAnonymousClass(this.preferences, innerClassFile, this.layoutBlockList);
      this.firstLineNumber = Instruction.UNKNOWN_LINE_NUMBER;
      this.index1 = this.index2;
      this.offset1 = in.offset;
   }

   protected void addInstructionsLayoutBlock(int lastLineNumber, int lastOffset) {
      int preferedLineCount;
      if(this.firstLineNumber != Instruction.UNKNOWN_LINE_NUMBER && lastLineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
         if(lastLineNumber < this.firstLineNumber) {
            lastLineNumber = this.firstLineNumber;
         }

         preferedLineCount = lastLineNumber - this.firstLineNumber;
      } else {
         preferedLineCount = Integer.MAX_VALUE;
      }

      this.layoutBlockList.add(new InstructionsLayoutBlock(this.firstLineNumber, lastLineNumber, preferedLineCount, preferedLineCount, preferedLineCount, this.classFile, this.method, this.list, this.index1, this.index2, this.offset1, lastOffset));
   }
}
