package jd.core.process.analyzer.instruction.fast.reconstructor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.bytecode.ComparisonInstructionAnalyzer;

public class IfGotoToIfReconstructor {
   public static void Reconstruct(List<Instruction> list) {
      int length = list.size();
      if(length >= 3) {
         int index = length - 2;

         while(index-- > 0) {
            Instruction i = (Instruction)list.get(index);
            switch(i.opcode) {
            case 260:
            case 261:
            case 262:
            case 284:
               BranchInstruction bi = (BranchInstruction)i;
               i = (Instruction)list.get(index + 1);
               if(i.opcode == 167) {
                  Goto g = (Goto)i;
                  i = (Instruction)list.get(index + 2);
                  int jumpOffset = bi.GetJumpOffset();
                  if(jumpOffset > g.offset && i.offset >= jumpOffset) {
                     bi.branch = g.GetJumpOffset() - bi.offset;
                     ComparisonInstructionAnalyzer.InverseComparison(bi);
                     list.remove(index + 1);
                  }
               }
            }
         }

      }
   }
}
