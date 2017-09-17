package jd.core.process.analyzer.instruction.bytecode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;

public class ComparisonInstructionAnalyzer {
   public static void Aggregate(List<Instruction> list) {
      int afterOffest = -1;

      Instruction instruction;
      for(int index = list.size(); index-- > 0; afterOffest = instruction.offset) {
         instruction = (Instruction)list.get(index);
         switch(instruction.opcode) {
         case 260:
         case 261:
         case 262:
            if(index > 0) {
               Instruction prevI = (Instruction)list.get(index - 1);
               switch(prevI.opcode) {
               case 167:
               case 260:
               case 261:
               case 262:
                  BranchInstruction bi = (BranchInstruction)instruction;
                  BranchInstruction prevBi = (BranchInstruction)prevI;
                  int prevBiJumpOffset = prevBi.GetJumpOffset();
                  if(prevBiJumpOffset == bi.GetJumpOffset() || prevBi.branch > 0 && prevBiJumpOffset <= afterOffest) {
                     index = AnalyzeIfInstructions(list, index, bi, afterOffest);
                  }
               }
            }
         }
      }

   }

   private static int AnalyzeIfInstructions(List<Instruction> list, int index, BranchInstruction lastBi, int afterOffest) {
      int arrayLength = ((Instruction)list.get(list.size() - 1)).offset;
      boolean[] offsetToPreviousGotoFlag = new boolean[arrayLength];
      boolean[] inversedTernaryOpLogic = new boolean[arrayLength];
      int firstIndex = SearchFirstIndex(list, index, lastBi, afterOffest, offsetToPreviousGotoFlag, inversedTernaryOpLogic);
      firstIndex = ReduceFirstIndex(list, firstIndex, index);
      if(firstIndex < index) {
         ArrayList branchInstructions = new ArrayList(index - firstIndex + 1);
         branchInstructions.add(lastBi);

         while(index > firstIndex) {
            --index;
            branchInstructions.add((Instruction)list.remove(index));
         }

         Collections.reverse(branchInstructions);
         list.set(index, CreateIfInstructions(offsetToPreviousGotoFlag, inversedTernaryOpLogic, branchInstructions, lastBi));
      }

      return index;
   }

   private static int ReduceFirstIndex(List<Instruction> list, int firstIndex, int lastIndex) {
      int firstOffset = firstIndex == 0?0:((Instruction)list.get(firstIndex - 1)).offset;
      int newFirstOffset = firstOffset;
      int lastOffset = ((Instruction)list.get(lastIndex)).offset;
      int index = firstIndex;

      Instruction i;
      int jumpOffset;
      while(index-- > 0) {
         i = (Instruction)list.get(index);
         switch(i.opcode) {
         case 167:
         case 260:
         case 261:
         case 262:
            jumpOffset = ((BranchInstruction)i).GetJumpOffset();
            if(newFirstOffset < jumpOffset && jumpOffset <= lastOffset) {
               newFirstOffset = jumpOffset;
            }
         }
      }

      index = list.size();

      while(true) {
         --index;
         if(index <= lastIndex) {
            if(newFirstOffset != firstOffset) {
               for(index = firstIndex; index <= lastIndex; ++index) {
                  i = (Instruction)list.get(index);
                  if(i.offset > newFirstOffset) {
                     firstIndex = index;
                     break;
                  }
               }
            }

            return firstIndex;
         }

         i = (Instruction)list.get(index);
         switch(i.opcode) {
         case 167:
         case 260:
         case 261:
         case 262:
            jumpOffset = ((BranchInstruction)i).GetJumpOffset();
            if(newFirstOffset < jumpOffset && jumpOffset <= lastOffset) {
               newFirstOffset = jumpOffset;
            }
         }
      }
   }

   private static int SearchFirstIndex(List<Instruction> list, int lastIndex, BranchInstruction lastBi, int afterOffest, boolean[] offsetToPreviousGotoFlag, boolean[] inversedTernaryOpLogic) {
      int index = lastIndex;
      int lastBiJumpOffset = lastBi.GetJumpOffset();

      Instruction instruction;
      for(Object nextInstruction = lastBi; index-- > 0; nextInstruction = instruction) {
         instruction = (Instruction)list.get(index);
         int opcode = instruction.opcode;
         int jumpOffset;
         int nextOffset;
         if(opcode != 260 && opcode != 261 && opcode != 262) {
            if(opcode != 167) {
               break;
            }

            Goto var19 = (Goto)instruction;
            jumpOffset = var19.GetJumpOffset();
            if(jumpOffset != lastBiJumpOffset && (jumpOffset <= ((Instruction)nextInstruction).offset || jumpOffset > afterOffest) || index <= 0) {
               break;
            }

            Instruction var20 = (Instruction)list.get(index - 1);
            opcode = var20.opcode;
            if(opcode != 260 && opcode != 261 && opcode != 262) {
               break;
            }

            int var21 = ((BranchInstruction)var20).GetJumpOffset();
            if(var19.offset < var21 && var21 <= lastBi.offset) {
               break;
            }

            Instruction var22 = (Instruction)list.get(lastIndex);

            int var23;
            for(var23 = lastIndex - 1; var23 > index; --var23) {
               Instruction var24 = (Instruction)list.get(var23);
               if(jumpOffset > var24.offset) {
                  var22 = var24;
                  break;
               }
            }

            opcode = var22.opcode;
            if(opcode != 260 && opcode != 261 && opcode != 262) {
               break;
            }

            var23 = ((BranchInstruction)var22).GetJumpOffset();
            int j;
            if(var21 == var23) {
               nextOffset = ((Instruction)nextInstruction).offset;

               for(j = var19.offset + 1; j < nextOffset; ++j) {
                  offsetToPreviousGotoFlag[j] = true;
               }
            } else {
               if(jumpOffset != var23) {
                  break;
               }

               nextOffset = ((Instruction)nextInstruction).offset;

               for(j = var19.offset + 1; j < nextOffset; ++j) {
                  offsetToPreviousGotoFlag[j] = true;
               }

               inversedTernaryOpLogic[var19.offset] = true;
            }
         } else {
            BranchInstruction g = (BranchInstruction)instruction;
            jumpOffset = g.GetJumpOffset();
            if(jumpOffset == lastBiJumpOffset) {
               if(g.branch > 0 && instruction.lineNumber != Instruction.UNKNOWN_LINE_NUMBER && ((Instruction)nextInstruction).lineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
                  if(instruction.lineNumber + 2 <= ((Instruction)nextInstruction).lineNumber) {
                     break;
                  }

                  int lastInstructionValue1 = list.size();
                  boolean jumpOffsetValue1 = false;

                  for(int lastInstructionValue2 = lastIndex + 1; lastInstructionValue2 < lastInstructionValue1; ++lastInstructionValue2) {
                     Instruction jumpOffsetValue2 = (Instruction)list.get(lastInstructionValue2);
                     if(jumpOffsetValue2.opcode == 132) {
                        nextOffset = jumpOffsetValue2.lineNumber;
                        if(nextOffset != Instruction.UNKNOWN_LINE_NUMBER && instruction.lineNumber <= nextOffset && nextOffset < ((Instruction)nextInstruction).lineNumber) {
                           jumpOffsetValue1 = true;
                           break;
                        }
                     }
                  }

                  if(jumpOffsetValue1) {
                     break;
                  }
               }
            } else if(jumpOffset != lastBiJumpOffset && (g.branch <= 0 || jumpOffset > afterOffest)) {
               break;
            }
         }
      }

      return index + 1;
   }

   private static ComplexConditionalBranchInstruction CreateIfInstructions(boolean[] offsetToPreviousGotoFlag, boolean[] inversedTernaryOpLogic, List<Instruction> branchInstructions, BranchInstruction lastBi) {
      ReconstructTernaryOperators(offsetToPreviousGotoFlag, inversedTernaryOpLogic, branchInstructions, lastBi);
      ComplexConditionalBranchInstruction cbl = AssembleAndCreateIfInstructions(branchInstructions, lastBi);
      SetOperator(cbl, lastBi, false);
      return cbl;
   }

   private static void ReconstructTernaryOperators(boolean[] offsetToPreviousGotoFlag, boolean[] inversedTernaryOpLogic, List<Instruction> branchInstructions, BranchInstruction lastBi) {
      if(branchInstructions.size() > 1) {
         int index = branchInstructions.size() - 1;

         Instruction i;
         for(int nextOffest = ((Instruction)branchInstructions.get(index)).offset; index-- > 0; nextOffest = i.offset) {
            i = (Instruction)branchInstructions.get(index);
            switch(i.opcode) {
            case 260:
            case 261:
            case 262:
               BranchInstruction lastTernaryOpTestBi = (BranchInstruction)i;
               int lastTernaryOpTestBiJumpOffset = lastTernaryOpTestBi.GetJumpOffset();
               if(lastTernaryOpTestBiJumpOffset >= 0 && lastBi.offset >= lastTernaryOpTestBiJumpOffset && offsetToPreviousGotoFlag[lastTernaryOpTestBiJumpOffset]) {
                  ArrayList ternaryOpTestInstructions = new ArrayList();
                  ternaryOpTestInstructions.add(lastTernaryOpTestBi);

                  int gotoJumpOffset;
                  while(index > 0) {
                     --index;
                     Instruction test = (Instruction)branchInstructions.get(index);
                     int ternaryOpValue1Instructions = test.opcode;
                     if(ternaryOpValue1Instructions != 260 && ternaryOpValue1Instructions != 261 && ternaryOpValue1Instructions != 262 && ternaryOpValue1Instructions != 167) {
                        ++index;
                        break;
                     }

                     BranchInstruction g = (BranchInstruction)test;
                     int value1 = g.branch;
                     gotoJumpOffset = g.offset + value1;
                     if(gotoJumpOffset != lastTernaryOpTestBiJumpOffset && (value1 <= 0 || gotoJumpOffset > nextOffest)) {
                        ++index;
                        break;
                     }

                     branchInstructions.remove(index);
                     ternaryOpTestInstructions.add(test);
                  }

                  Object var20;
                  if(ternaryOpTestInstructions.size() > 1) {
                     Collections.reverse(ternaryOpTestInstructions);
                     var20 = CreateIfInstructions(offsetToPreviousGotoFlag, inversedTernaryOpLogic, ternaryOpTestInstructions, lastTernaryOpTestBi);
                  } else {
                     var20 = lastTernaryOpTestBi;
                  }

                  InverseComparison((Instruction)var20);
                  ArrayList var21 = new ArrayList();
                  ++index;

                  while(index < branchInstructions.size()) {
                     Instruction var22 = (Instruction)branchInstructions.get(index);
                     if(var22.offset >= lastTernaryOpTestBiJumpOffset) {
                        break;
                     }

                     var21.add(var22);
                     branchInstructions.remove(index);
                  }

                  Goto var23 = (Goto)var21.remove(var21.size() - 1);
                  Object var24;
                  if(var21.size() > 1) {
                     BranchInstruction var25 = (BranchInstruction)var21.get(var21.size() - 1);
                     var24 = AssembleAndCreateIfInstructions(var21, var25);
                  } else {
                     var24 = (BranchInstruction)var21.get(var21.size() - 1);
                  }

                  if(inversedTernaryOpLogic[var23.offset]) {
                     gotoJumpOffset = ((BranchInstruction)var24).GetJumpOffset();
                     InverseComparison((Instruction)var24);
                  } else {
                     gotoJumpOffset = var23.GetJumpOffset();
                  }

                  ArrayList ternaryOpValue2Instructions = new ArrayList();

                  while(index < branchInstructions.size()) {
                     Instruction value2 = (Instruction)branchInstructions.get(index);
                     if(value2.opcode == 167 || value2.offset >= gotoJumpOffset) {
                        break;
                     }

                     ternaryOpValue2Instructions.add(value2);
                     branchInstructions.remove(index);
                  }

                  Object var26;
                  if(ternaryOpValue2Instructions.size() > 1) {
                     BranchInstruction to = (BranchInstruction)ternaryOpValue2Instructions.get(ternaryOpValue2Instructions.size() - 1);
                     var26 = AssembleAndCreateIfInstructions(ternaryOpValue2Instructions, to);
                  } else {
                     var26 = (BranchInstruction)ternaryOpValue2Instructions.get(ternaryOpValue2Instructions.size() - 1);
                  }

                  --index;
                  TernaryOperator var27 = new TernaryOperator(281, ((BranchInstruction)var26).offset, ((Instruction)var20).lineNumber, (Instruction)var20, (Instruction)var24, (Instruction)var26);
                  ArrayList instructions = new ArrayList(1);
                  instructions.add(var27);
                  ComplexConditionalBranchInstruction cbl = new ComplexConditionalBranchInstruction(284, ((BranchInstruction)var26).offset, ((Instruction)var20).lineNumber, 1, instructions, ((BranchInstruction)var26).branch);
                  branchInstructions.set(index, cbl);
               }
            }
         }

      }
   }

   private static ComplexConditionalBranchInstruction AssembleAndCreateIfInstructions(List<Instruction> branchInstructions, BranchInstruction lastBi) {
      int length = branchInstructions.size();
      int lastBiOffset = lastBi.offset;

      int lineNumber;
      for(lineNumber = 0; lineNumber < length; ++lineNumber) {
         BranchInstruction branchInstruction = (BranchInstruction)branchInstructions.get(lineNumber);
         int jumpOffset = branchInstruction.GetJumpOffset();
         if(branchInstruction.branch > 0 && jumpOffset < lastBiOffset) {
            BranchInstruction subLastBi = lastBi;
            ArrayList subBranchInstructions = new ArrayList();
            subBranchInstructions.add(branchInstruction);
            ++lineNumber;

            while(lineNumber < length) {
               branchInstruction = (BranchInstruction)branchInstructions.get(lineNumber);
               if(branchInstruction.offset >= jumpOffset) {
                  break;
               }

               subBranchInstructions.add(branchInstruction);
               subLastBi = branchInstruction;
               branchInstructions.remove(lineNumber);
               --length;
            }

            --lineNumber;
            if(subBranchInstructions.size() > 1) {
               branchInstructions.set(lineNumber, AssembleAndCreateIfInstructions(subBranchInstructions, subLastBi));
            }
         }
      }

      AnalyzeLastTestBlock(branchInstructions);
      lineNumber = ((Instruction)branchInstructions.get(0)).lineNumber;
      return new ComplexConditionalBranchInstruction(284, lastBi.offset, lineNumber, 1, branchInstructions, lastBi.branch);
   }

   private static void AnalyzeLastTestBlock(List<Instruction> branchInstructions) {
      int length = branchInstructions.size();
      if(length > 1) {
         --length;
         BranchInstruction branchInstruction = (BranchInstruction)branchInstructions.get(0);
         int firstJumpOffset = branchInstruction.GetJumpOffset();

         for(int i = 1; i < length; ++i) {
            ConditionalBranchInstruction var9 = (ConditionalBranchInstruction)branchInstructions.get(i);
            int jumpOffset = var9.GetJumpOffset();
            if(firstJumpOffset != jumpOffset) {
               Object subLastBi = var9;
               ArrayList subJumpInstructions = new ArrayList(length);
               subJumpInstructions.add(var9);
               ++i;

               while(i <= length) {
                  subLastBi = (BranchInstruction)branchInstructions.remove(i);
                  subJumpInstructions.add(subLastBi);
                  --length;
               }

               AnalyzeLastTestBlock(subJumpInstructions);
               int lineNumber = ((Instruction)branchInstructions.get(0)).lineNumber;
               --i;
               branchInstructions.set(i, new ComplexConditionalBranchInstruction(284, ((BranchInstruction)subLastBi).offset, lineNumber, 1, subJumpInstructions, ((BranchInstruction)subLastBi).branch));
            }
         }
      }

   }

   private static void SetOperator(ComplexConditionalBranchInstruction cbl, BranchInstruction lastBi, boolean inverse) {
      List instructions = cbl.instructions;
      int lastIndex = instructions.size() - 1;
      BranchInstruction firstBi = (BranchInstruction)instructions.get(0);
      if(firstBi.GetJumpOffset() == lastBi.GetJumpOffset()) {
         cbl.cmp = inverse?0:2;

         for(int tmpInverse = 0; tmpInverse <= lastIndex; ++tmpInverse) {
            SetOperator((Instruction)instructions.get(tmpInverse), inverse);
         }
      } else {
         cbl.cmp = inverse?2:0;
         boolean var8 = !inverse;
         int i = 0;

         while(i < lastIndex) {
            SetOperator((Instruction)instructions.get(i++), var8);
         }

         SetOperator((Instruction)instructions.get(i), inverse);
      }

   }

   private static void SetOperator(Instruction instruction, boolean inverse) {
      switch(instruction.opcode) {
      case 281:
         TernaryOperator cbi1 = (TernaryOperator)instruction;
         SetOperator(cbi1.value1, inverse);
         SetOperator(cbi1.value2, inverse);
         break;
      case 282:
      case 283:
      default:
         if(inverse) {
            ConditionalBranchInstruction cbi2 = (ConditionalBranchInstruction)instruction;
            cbi2.cmp = 7 - cbi2.cmp;
         }
         break;
      case 284:
         ComplexConditionalBranchInstruction cbi = (ComplexConditionalBranchInstruction)instruction;
         int length = cbi.instructions.size();
         if(length == 1) {
            SetOperator((Instruction)cbi.instructions.get(0), inverse);
         } else if(length > 1) {
            SetOperator(cbi, (BranchInstruction)cbi.instructions.get(length - 1), inverse);
         }
      }

   }

   public static void InverseComparison(Instruction instruction) {
      switch(instruction.opcode) {
      case 260:
      case 261:
      case 262:
         ConditionalBranchInstruction cbi = (ConditionalBranchInstruction)instruction;
         cbi.cmp = 7 - cbi.cmp;
         break;
      case 281:
         TernaryOperator var4 = (TernaryOperator)instruction;
         InverseComparison(var4.value1);
         InverseComparison(var4.value2);
         break;
      case 284:
         ComplexConditionalBranchInstruction ccbi = (ComplexConditionalBranchInstruction)instruction;
         ccbi.cmp = 2 - ccbi.cmp;

         for(int to = ccbi.instructions.size() - 1; to >= 0; --to) {
            InverseComparison((Instruction)ccbi.instructions.get(to));
         }
      }

   }

   public static int GetLastIndex(List<Instruction> list, int firstIndex) {
      int lenght = list.size();

      int index;
      for(index = firstIndex + 1; index < lenght; ++index) {
         Instruction dummy = (Instruction)list.get(index);
         int lastBi = dummy.opcode;
         if(lastBi != 260 && lastBi != 261 && lastBi != 262 && lastBi != 167) {
            break;
         }
      }

      if(index - 1 == firstIndex) {
         return firstIndex;
      } else {
         boolean[] var8 = new boolean[((Instruction)list.get(lenght - 1)).offset];

         int firstIndexTmp;
         do {
            --index;
            if(index <= firstIndex) {
               break;
            }

            BranchInstruction var9 = (BranchInstruction)list.get(index);
            int afterOffest = index + 1 < lenght?((Instruction)list.get(index + 1)).offset:-1;
            firstIndexTmp = SearchFirstIndex(list, index, var9, afterOffest, var8, var8);
            firstIndexTmp = ReduceFirstIndex(list, firstIndexTmp, index);
         } while(firstIndex != firstIndexTmp);

         return index;
      }
   }
}
