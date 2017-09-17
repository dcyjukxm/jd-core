package jd.core.process.analyzer.util;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class InstructionUtil {
   public static Instruction getInstructionAt(List<Instruction> list, int offset) {
      if(list != null && list.size() != 0) {
         if(((Instruction)list.get(0)).offset >= offset) {
            return (Instruction)list.get(0);
         } else {
            int length = list.size();
            if(length == 1) {
               return null;
            } else if(((Instruction)list.get(length - 1)).offset < offset) {
               return null;
            } else {
               int firstIndex = 0;
               int lastIndex = length - 1;

               while(true) {
                  while(true) {
                     int medIndex = (lastIndex + firstIndex) / 2;
                     Instruction i = (Instruction)list.get(medIndex);
                     if(i.offset < offset) {
                        firstIndex = medIndex + 1;
                     } else {
                        if(((Instruction)list.get(medIndex - 1)).offset < offset) {
                           return i;
                        }

                        lastIndex = medIndex - 1;
                     }
                  }
               }
            }
         }
      } else {
         return null;
      }
   }

   public static int getIndexForOffset(List<Instruction> list, int offset) {
      if(offset < 0) {
         throw new RuntimeException("offset=" + offset);
      } else if(list != null && list.size() != 0) {
         if(((Instruction)list.get(0)).offset >= offset) {
            return 0;
         } else {
            int length = list.size();
            if(length == 1) {
               return -1;
            } else if(((Instruction)list.get(length - 1)).offset < offset) {
               return -1;
            } else {
               int firstIndex = 0;
               int lastIndex = length - 1;

               while(true) {
                  while(true) {
                     int medIndex = (lastIndex + firstIndex) / 2;
                     Instruction i = (Instruction)list.get(medIndex);
                     if(i.offset < offset) {
                        firstIndex = medIndex + 1;
                     } else {
                        if(((Instruction)list.get(medIndex - 1)).offset < offset) {
                           return medIndex;
                        }

                        lastIndex = medIndex - 1;
                     }
                  }
               }
            }
         }
      } else {
         return -1;
      }
   }

   public static boolean CheckNoJumpToInterval(List<Instruction> list, int firstIndex, int afterIndex, int firstOffset, int lastOffset) {
      int index = firstIndex;

      while(index < afterIndex) {
         Instruction i = (Instruction)list.get(index);
         switch(i.opcode) {
         case 167:
         case 260:
         case 261:
         case 262:
            int jumpOffset = ((BranchInstruction)i).GetJumpOffset();
            if(firstOffset < jumpOffset && jumpOffset <= lastOffset) {
               return false;
            }
         default:
            ++index;
         }
      }

      return true;
   }
}
