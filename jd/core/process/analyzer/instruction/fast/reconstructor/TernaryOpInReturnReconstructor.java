package jd.core.process.analyzer.instruction.fast.reconstructor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.process.analyzer.instruction.bytecode.ComparisonInstructionAnalyzer;

public class TernaryOpInReturnReconstructor {
   public static void Reconstruct(List<Instruction> list) {
      for(int index = list.size() - 1; index >= 0; --index) {
         if(((Instruction)list.get(index)).opcode == 273) {
            ReturnInstruction ri1 = (ReturnInstruction)list.get(index);
            int opcode = ri1.valueref.opcode;
            if(opcode == 17 || opcode == 16 || opcode == 256) {
               IConst iConst1 = (IConst)ri1.valueref;
               if("Z".equals(iConst1.signature) && index > 0) {
                  int index2 = index - 1;
                  if(((Instruction)list.get(index2)).opcode == 273) {
                     ReturnInstruction ri2 = (ReturnInstruction)list.get(index2);
                     if(ri1.lineNumber == Instruction.UNKNOWN_LINE_NUMBER || ri1.lineNumber <= ri2.lineNumber) {
                        opcode = ri2.valueref.opcode;
                        if(opcode == 17 || opcode == 16 || opcode == 256) {
                           IConst iConst2 = (IConst)ri2.valueref;
                           if("Z".equals(iConst2.signature) && index2 > 0) {
                              --index2;
                              Instruction instruction = (Instruction)list.get(index2);
                              opcode = instruction.opcode;
                              if(opcode == 260 || opcode == 261 || opcode == 262 || opcode == 284) {
                                 BranchInstruction bi = (BranchInstruction)instruction;
                                 int offset = bi.GetJumpOffset();
                                 if(ri2.offset < offset && offset <= ri1.offset) {
                                    boolean found = false;
                                    int i = index2;

                                    while(i-- > 0) {
                                       instruction = (Instruction)list.get(i);
                                       opcode = instruction.opcode;
                                       int jumpOffset;
                                       if(opcode == 167) {
                                          jumpOffset = ((Goto)instruction).GetJumpOffset();
                                          if(ri2.offset < jumpOffset && jumpOffset <= ri1.offset) {
                                             found = true;
                                             break;
                                          }
                                       } else if(opcode == 260 || opcode == 261 || opcode == 262 || opcode == 284) {
                                          jumpOffset = ((BranchInstruction)instruction).GetJumpOffset();
                                          if(ri2.offset < jumpOffset && jumpOffset <= ri1.offset) {
                                             found = true;
                                             break;
                                          }
                                       }
                                    }

                                    if(!found) {
                                       if(iConst2.value == 1) {
                                          ComparisonInstructionAnalyzer.InverseComparison(bi);
                                       }

                                       list.remove(index);
                                       list.remove(index2);
                                       ri2.valueref = bi;
                                       index -= 3;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }
}
