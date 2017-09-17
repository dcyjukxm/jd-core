package jd.core.process.layouter;

import java.util.Comparator;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.layout.block.LayoutBlock;

public class LayoutBlockComparator implements Comparator<LayoutBlock> {
   public int compare(LayoutBlock lb1, LayoutBlock lb2) {
      return lb1.lastLineNumber == Instruction.UNKNOWN_LINE_NUMBER?(lb2.lastLineNumber == Instruction.UNKNOWN_LINE_NUMBER?lb1.index - lb2.index:1):(lb2.lastLineNumber == Instruction.UNKNOWN_LINE_NUMBER?-1:lb1.lastLineNumber - lb2.lastLineNumber);
   }
}
