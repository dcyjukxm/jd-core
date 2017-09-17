package jd.core.process.analyzer.instruction.fast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.attribute.CodeException;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Jsr;
import jd.core.model.instruction.bytecode.instruction.MonitorEnter;
import jd.core.model.instruction.bytecode.instruction.MonitorExit;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.fast.instruction.FastSynchronized;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.process.analyzer.instruction.bytecode.ComparisonInstructionAnalyzer;
import jd.core.process.analyzer.instruction.fast.UnexpectedInstructionException;
import jd.core.process.analyzer.instruction.fast.visitor.CheckLocalVariableUsedVisitor;
import jd.core.process.analyzer.instruction.fast.visitor.FastCompareInstructionVisitor;
import jd.core.process.analyzer.util.InstructionUtil;
import jd.core.util.IntSet;

public class FastCodeExceptionAnalyzer {
   public static List<FastCodeExceptionAnalyzer.FastCodeException> AggregateCodeExceptions(Method method, List<Instruction> list) {
      CodeException[] arrayOfCodeException = method.getCodeExceptions();
      if(arrayOfCodeException != null && arrayOfCodeException.length != 0) {
         ArrayList fastAggregatedCodeExceptions = new ArrayList(arrayOfCodeException.length);
         PopulateListOfFastAggregatedCodeException(method, list, fastAggregatedCodeExceptions);
         int length = fastAggregatedCodeExceptions.size();
         ArrayList fastCodeExceptions = new ArrayList(length);
         fastCodeExceptions.add(NewFastCodeException(list, (FastCodeExceptionAnalyzer.FastAggregatedCodeException)fastAggregatedCodeExceptions.get(0)));

         int switchCaseOffsets;
         for(switchCaseOffsets = 1; switchCaseOffsets < length; ++switchCaseOffsets) {
            FastCodeExceptionAnalyzer.FastAggregatedCodeException i = (FastCodeExceptionAnalyzer.FastAggregatedCodeException)fastAggregatedCodeExceptions.get(switchCaseOffsets);
            if(!UpdateFastCodeException(fastCodeExceptions, i)) {
               fastCodeExceptions.add(NewFastCodeException(list, i));
            }
         }

         Collections.sort(fastCodeExceptions);

         FastCodeExceptionAnalyzer.FastCodeException fce;
         for(switchCaseOffsets = fastCodeExceptions.size() - 1; switchCaseOffsets >= 1; --switchCaseOffsets) {
            FastCodeExceptionAnalyzer.FastCodeException var11 = (FastCodeExceptionAnalyzer.FastCodeException)fastCodeExceptions.get(switchCaseOffsets);
            fce = (FastCodeExceptionAnalyzer.FastCodeException)fastCodeExceptions.get(switchCaseOffsets - 1);
            if(var11.tryFromOffset == fce.tryFromOffset && var11.tryToOffset == fce.tryToOffset && var11.synchronizedFlag == fce.synchronizedFlag && (var11.afterOffset == -1 || var11.afterOffset > fce.maxOffset) && (fce.afterOffset == -1 || fce.afterOffset > var11.maxOffset)) {
               fce.catches.addAll(var11.catches);
               Collections.sort(fce.catches);
               if(fce.nbrFinally == 0) {
                  fce.finallyFromOffset = var11.finallyFromOffset;
                  fce.nbrFinally = var11.nbrFinally;
               }

               if(fce.maxOffset < var11.maxOffset) {
                  fce.maxOffset = var11.maxOffset;
               }

               if(fce.afterOffset == -1 || var11.afterOffset != -1 && var11.afterOffset < fce.afterOffset) {
                  fce.afterOffset = var11.afterOffset;
               }

               fastCodeExceptions.remove(switchCaseOffsets);
            }
         }

         ArrayList var10 = SearchSwitchCaseOffsets(list);

         for(int var12 = fastCodeExceptions.size() - 1; var12 >= 0; --var12) {
            fce = (FastCodeExceptionAnalyzer.FastCodeException)fastCodeExceptions.get(var12);
            DefineType(list, fce);
            if(fce.type == 0) {
               System.err.println("Undefined type catch");
            }

            ComputeAfterOffset(method, list, var10, fastCodeExceptions, fce, var12);
            length = list.size();
            if(fce.afterOffset == -1 && length > 0) {
               Instruction lastInstruction = (Instruction)list.get(length - 1);
               fce.afterOffset = lastInstruction.offset;
               if(lastInstruction.opcode != 177 && lastInstruction.opcode != 273) {
                  ++fce.afterOffset;
               }
            }
         }

         Collections.sort(fastCodeExceptions);
         return fastCodeExceptions;
      } else {
         return null;
      }
   }

   private static void PopulateListOfFastAggregatedCodeException(Method method, List<Instruction> list, List<FastCodeExceptionAnalyzer.FastAggregatedCodeException> fastAggregatedCodeExceptions) {
      int length = method.getCode().length;
      if(length != 0) {
         FastCodeExceptionAnalyzer.FastAggregatedCodeException[] array = new FastCodeExceptionAnalyzer.FastAggregatedCodeException[length];
         CodeException[] arrayOfCodeException = method.getCodeExceptions();
         length = arrayOfCodeException.length;

         int i;
         for(i = 0; i < length; ++i) {
            CodeException face = arrayOfCodeException[i];
            FastCodeExceptionAnalyzer.FastAggregatedCodeException face1;
            if(array[face.handler_pc] == null) {
               face1 = new FastCodeExceptionAnalyzer.FastAggregatedCodeException(i, face.start_pc, face.end_pc, face.handler_pc, face.catch_type);
               fastAggregatedCodeExceptions.add(face1);
               array[face.handler_pc] = face1;
            } else {
               face1 = array[face.handler_pc];
               if(face1.catch_type == 0) {
                  ++face1.nbrFinally;
               } else if(IsNotAlreadyStored(face1, face.catch_type)) {
                  if(face1.otherCatchTypes == null) {
                     face1.otherCatchTypes = new int[length];
                  }

                  face1.otherCatchTypes[i] = face.catch_type;
               }
            }
         }

         i = fastAggregatedCodeExceptions.size();

         while(i-- > 0) {
            FastCodeExceptionAnalyzer.FastAggregatedCodeException var9 = (FastCodeExceptionAnalyzer.FastAggregatedCodeException)fastAggregatedCodeExceptions.get(i);
            if(var9.catch_type == 0 && IsASynchronizedBlock(list, var9)) {
               var9.synchronizedFlag = true;
            }
         }

      }
   }

   private static boolean IsNotAlreadyStored(FastCodeExceptionAnalyzer.FastAggregatedCodeException face, int catch_type) {
      if(face.catch_type == catch_type) {
         return false;
      } else {
         if(face.otherCatchTypes != null) {
            int i = face.otherCatchTypes.length;

            while(i-- > 0) {
               if(face.otherCatchTypes[i] == catch_type) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   private static boolean IsASynchronizedBlock(List<Instruction> list, FastCodeExceptionAnalyzer.FastAggregatedCodeException face) {
      int index = InstructionUtil.getIndexForOffset(list, face.start_pc);
      if(index == -1) {
         return false;
      } else if(((Instruction)list.get(index)).opcode == 195) {
         return true;
      } else if(index < 1) {
         return false;
      } else {
         Instruction instruction = (Instruction)list.get(index - 1);
         if(instruction.opcode != 194) {
            return false;
         } else {
            MonitorEnter monitorEnter = (MonitorEnter)instruction;
            int varMonitorIndex;
            switch(monitorEnter.objectref.opcode) {
            case 25:
               if(index < 2) {
                  return false;
               }

               instruction = (Instruction)list.get(index - 2);
               if(instruction.opcode != 58) {
                  return false;
               }

               AStore var9 = (AStore)instruction;
               varMonitorIndex = var9.index;
               break;
            case 265:
               AssignmentInstruction checkMonitorExit = (AssignmentInstruction)monitorEnter.objectref;
               if(checkMonitorExit.value1.opcode != 25) {
                  return false;
               }

               ALoad length = (ALoad)checkMonitorExit.value1;
               varMonitorIndex = length.index;
               break;
            default:
               return false;
            }

            boolean var10 = false;
            int var11 = list.size();
            index = InstructionUtil.getIndexForOffset(list, face.handler_pc);

            while(index < var11) {
               instruction = (Instruction)list.get(index++);
               switch(instruction.opcode) {
               case 177:
               case 191:
               case 273:
                  return false;
               case 195:
                  var10 = true;
                  MonitorExit monitorExit = (MonitorExit)instruction;
                  if(monitorExit.objectref.opcode == 25 && ((ALoad)monitorExit.objectref).index == varMonitorIndex) {
                     return true;
                  }
               }
            }

            if(!var10 && index == var11) {
               return true;
            } else {
               return false;
            }
         }
      }
   }

   private static boolean UpdateFastCodeException(List<FastCodeExceptionAnalyzer.FastCodeException> fastCodeExceptions, FastCodeExceptionAnalyzer.FastAggregatedCodeException fastAggregatedCodeException) {
      int length = fastCodeExceptions.size();
      if(fastAggregatedCodeException.catch_type == 0) {
         int i;
         FastCodeExceptionAnalyzer.FastCodeException fce;
         for(i = 0; i < length; ++i) {
            fce = (FastCodeExceptionAnalyzer.FastCodeException)fastCodeExceptions.get(i);
            if(fce.finallyFromOffset == -1 && fastAggregatedCodeException.start_pc == fce.tryFromOffset && fastAggregatedCodeException.end_pc == fce.tryToOffset && fastAggregatedCodeException.handler_pc > fce.maxOffset && !fastAggregatedCodeException.synchronizedFlag && (fce.afterOffset == -1 || fastAggregatedCodeException.end_pc < fce.afterOffset && fastAggregatedCodeException.handler_pc < fce.afterOffset)) {
               fce.maxOffset = fastAggregatedCodeException.handler_pc;
               fce.finallyFromOffset = fastAggregatedCodeException.handler_pc;
               fce.nbrFinally += fastAggregatedCodeException.nbrFinally;
               return true;
            }
         }

         for(i = 0; i < length; ++i) {
            fce = (FastCodeExceptionAnalyzer.FastCodeException)fastCodeExceptions.get(i);
            if(fce.finallyFromOffset == -1 && fastAggregatedCodeException.start_pc == fce.tryFromOffset && fastAggregatedCodeException.end_pc >= fce.tryToOffset && fastAggregatedCodeException.handler_pc > fce.maxOffset && !fastAggregatedCodeException.synchronizedFlag && (fce.afterOffset == -1 || fastAggregatedCodeException.end_pc < fce.afterOffset && fastAggregatedCodeException.handler_pc < fce.afterOffset)) {
               fce.maxOffset = fastAggregatedCodeException.handler_pc;
               fce.finallyFromOffset = fastAggregatedCodeException.handler_pc;
               fce.nbrFinally += fastAggregatedCodeException.nbrFinally;
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static FastCodeExceptionAnalyzer.FastCodeException NewFastCodeException(List<Instruction> list, FastCodeExceptionAnalyzer.FastAggregatedCodeException fastCodeException) {
      FastCodeExceptionAnalyzer.FastCodeException fce = new FastCodeExceptionAnalyzer.FastCodeException(fastCodeException.start_pc, fastCodeException.end_pc, fastCodeException.handler_pc, fastCodeException.synchronizedFlag);
      if(fastCodeException.catch_type == 0) {
         fce.finallyFromOffset = fastCodeException.handler_pc;
         fce.nbrFinally += fastCodeException.nbrFinally;
      } else {
         fce.catches.add(new FastCodeExceptionAnalyzer.FastCodeExceptionCatch(fastCodeException.catch_type, fastCodeException.otherCatchTypes, fastCodeException.handler_pc));
      }

      fce.afterOffset = SearchAfterOffset(list, fastCodeException.handler_pc);
      return fce;
   }

   private static int SearchAfterOffset(List<Instruction> list, int offset) {
      int index = InstructionUtil.getIndexForOffset(list, offset);
      if(index <= 0) {
         return offset;
      } else {
         --index;
         Instruction i = (Instruction)list.get(index);
         switch(i.opcode) {
         case 167:
            int branch = ((Goto)i).branch;
            if(branch < 0) {
               return -1;
            }

            int jumpOffset = i.offset + branch;
            index = InstructionUtil.getIndexForOffset(list, jumpOffset);
            if(index <= 0) {
               return -1;
            }

            i = (Instruction)list.get(index);
            if(i.opcode != 168) {
               return jumpOffset;
            }

            branch = ((Jsr)i).branch;
            if(branch > 0) {
               return i.offset + branch;
            }

            return jumpOffset + 1;
         case 168:
         default:
            break;
         case 169:
            while(true) {
               --index;
               if(index < 3) {
                  break;
               }

               if(((Instruction)list.get(index)).opcode == 191 && ((Instruction)list.get(index - 1)).opcode == 168 && ((Instruction)list.get(index - 2)).opcode == 58 && ((Instruction)list.get(index - 3)).opcode == 167) {
                  Goto g = (Goto)list.get(index - 3);
                  return g.GetJumpOffset();
               }
            }
         }

         return -1;
      }
   }

   private static ArrayList<int[]> SearchSwitchCaseOffsets(List<Instruction> list) {
      ArrayList switchCaseOffsets = new ArrayList();
      int i = list.size();

      while(true) {
         while(i-- > 0) {
            Instruction instruction = (Instruction)list.get(i);
            switch(instruction.opcode) {
            case 170:
            case 171:
               Switch s = (Switch)instruction;
               int j = s.offsets.length;
               int[] offsets = new int[j + 1];

               for(offsets[j] = s.offset + s.defaultOffset; j-- > 0; offsets[j] = s.offset + s.offsets[j]) {
                  ;
               }

               Arrays.sort(offsets);
               switchCaseOffsets.add(offsets);
            }
         }

         return switchCaseOffsets;
      }
   }

   private static void DefineType(List<Instruction> list, FastCodeExceptionAnalyzer.FastCodeException fastCodeException) {
      int uniqueJumpAddressFlag;
      Instruction uniqueJumpAddress;
      int g;
      int var11;
      Goto var12;
      switch(fastCodeException.nbrFinally) {
      case 0:
         fastCodeException.type = 1;
         break;
      case 1:
         if(fastCodeException.catches != null && fastCodeException.catches.size() != 0) {
            uniqueJumpAddressFlag = InstructionUtil.getIndexForOffset(list, ((FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fastCodeException.catches.get(0)).fromOffset);
            if(uniqueJumpAddressFlag < 0) {
               return;
            }

            --uniqueJumpAddressFlag;
            uniqueJumpAddress = (Instruction)list.get(uniqueJumpAddressFlag);
            if(uniqueJumpAddress.opcode == 167) {
               var12 = (Goto)uniqueJumpAddress;
               --uniqueJumpAddressFlag;
               uniqueJumpAddress = (Instruction)list.get(uniqueJumpAddressFlag);
               if(uniqueJumpAddress.opcode == 168) {
                  fastCodeException.type = 9;
               } else {
                  uniqueJumpAddressFlag = InstructionUtil.getIndexForOffset(list, var12.GetJumpOffset());
                  uniqueJumpAddress = (Instruction)list.get(uniqueJumpAddressFlag);
                  if(uniqueJumpAddress.opcode == 168) {
                     fastCodeException.type = 7;
                  } else {
                     uniqueJumpAddress = (Instruction)list.get(uniqueJumpAddressFlag - 1);
                     if(uniqueJumpAddress.opcode == 191) {
                        fastCodeException.type = 14;
                     } else {
                        fastCodeException.type = 8;
                     }
                  }
               }
            } else if(uniqueJumpAddress.opcode == 169) {
               fastCodeException.type = 7;
            } else {
               --uniqueJumpAddressFlag;
               uniqueJumpAddress = (Instruction)list.get(uniqueJumpAddressFlag);
               if(uniqueJumpAddress.opcode == 168) {
                  fastCodeException.type = 9;
               }
            }
         } else {
            uniqueJumpAddressFlag = InstructionUtil.getIndexForOffset(list, fastCodeException.finallyFromOffset);
            if(uniqueJumpAddressFlag < 0) {
               return;
            }

            uniqueJumpAddress = (Instruction)list.get(uniqueJumpAddressFlag - 1);
            switch(uniqueJumpAddress.opcode) {
            case 167:
               if(TryBlockContainsJsr(list, fastCodeException)) {
                  fastCodeException.type = 2;
                  return;
               } else {
                  switch(((Instruction)list.get(uniqueJumpAddressFlag - 2)).opcode) {
                  case 195:
                     fastCodeException.type = 5;
                     return;
                  default:
                     var11 = ((Goto)uniqueJumpAddress).GetJumpOffset();
                     uniqueJumpAddress = InstructionUtil.getInstructionAt(list, var11);
                     if(uniqueJumpAddress.opcode == 168) {
                        fastCodeException.type = 3;
                     } else {
                        fastCodeException.type = 13;
                     }

                     return;
                  }
               }
            case 169:
               fastCodeException.type = 6;
               return;
            case 177:
            case 273:
               if(TryBlockContainsJsr(list, fastCodeException)) {
                  fastCodeException.type = 2;
                  return;
               } else {
                  switch(((Instruction)list.get(uniqueJumpAddressFlag - 2)).opcode) {
                  case 195:
                     fastCodeException.type = 5;
                     return;
                  default:
                     Instruction index = (Instruction)list.get(uniqueJumpAddressFlag + 1);
                     int instruction = ((AStore)list.get(uniqueJumpAddressFlag)).index;
                     g = list.size();

                     while(true) {
                        ++uniqueJumpAddressFlag;
                        if(uniqueJumpAddressFlag >= g) {
                           break;
                        }

                        uniqueJumpAddress = (Instruction)list.get(uniqueJumpAddressFlag);
                        if(uniqueJumpAddress.opcode == 191) {
                           AThrow firstFinallyInstruction = (AThrow)uniqueJumpAddress;
                           if(firstFinallyInstruction.value.opcode == 25 && ((ALoad)firstFinallyInstruction.value).index == instruction) {
                              break;
                           }
                        }
                     }

                     ++uniqueJumpAddressFlag;
                     if(uniqueJumpAddressFlag >= g) {
                        fastCodeException.type = 10;
                     } else {
                        uniqueJumpAddress = (Instruction)list.get(uniqueJumpAddressFlag);
                        fastCodeException.type = uniqueJumpAddress.opcode == index.opcode && index.lineNumber != Instruction.UNKNOWN_LINE_NUMBER && index.lineNumber == uniqueJumpAddress.lineNumber?13:10;
                     }

                     return;
                  }
               }
            case 191:
               switch(((Instruction)list.get(uniqueJumpAddressFlag + 1)).opcode) {
               case 168:
                  fastCodeException.type = 4;
                  return;
               default:
                  if(((Instruction)list.get(uniqueJumpAddressFlag)).opcode == 195) {
                     fastCodeException.type = 2;
                  } else {
                     fastCodeException.type = 11;
                  }
               }
            }
         }
         break;
      default:
         if(fastCodeException.catches != null && fastCodeException.catches.size() != 0) {
            boolean var13 = true;
            int var16 = -1;
            int exceptionIndex;
            int lenght;
            Instruction var18;
            if(fastCodeException.catches != null) {
               for(var11 = fastCodeException.catches.size() - 1; var11 >= 0; --var11) {
                  FastCodeExceptionAnalyzer.FastCodeExceptionCatch var14 = (FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fastCodeException.catches.get(var11);
                  g = InstructionUtil.getIndexForOffset(list, var14.fromOffset);
                  if(g != -1) {
                     var18 = (Instruction)list.get(g - 1);
                     if(var18.opcode == 167) {
                        exceptionIndex = ((Goto)var18).branch;
                        if(exceptionIndex > 0) {
                           lenght = var18.offset + exceptionIndex;
                           if(var16 == -1) {
                              var16 = lenght;
                           } else if(var16 != lenght) {
                              var13 = false;
                              break;
                           }
                        }
                     }
                  }
               }
            }

            var11 = InstructionUtil.getIndexForOffset(list, fastCodeException.finallyFromOffset);
            if(var11 < 0) {
               return;
            }

            --var11;
            Instruction var15 = (Instruction)list.get(var11);
            if(var13 && var15.opcode == 167) {
               g = ((Goto)var15).branch;
               if(g > 0) {
                  int var19 = var15.offset + g;
                  if(var16 == -1) {
                     var16 = var19;
                  } else if(var16 != var19) {
                     var13 = false;
                  }
               }
            }

            if(!var13) {
               fastCodeException.type = 14;
               return;
            }

            var11 = InstructionUtil.getIndexForOffset(list, fastCodeException.tryToOffset);
            if(var11 < 0) {
               return;
            }

            var15 = (Instruction)list.get(var11);
            switch(var15.opcode) {
            case 58:
               DefineTypeJikes122Or142(list, fastCodeException, ((AStore)var15).valueref, var11);
               break;
            case 87:
               DefineTypeJikes122Or142(list, fastCodeException, ((Pop)var15).objectref, var11);
               break;
            case 167:
               Goto var17 = (Goto)var15;
               var15 = InstructionUtil.getInstructionAt(list, var17.GetJumpOffset());
               if(var15 == null) {
                  return;
               }

               if(var15.opcode == 168 && ((Jsr)var15).branch < 0) {
                  fastCodeException.type = 12;
               } else if(var11 > 0 && ((Instruction)list.get(var11 - 1)).opcode == 168) {
                  fastCodeException.type = 9;
               } else {
                  fastCodeException.type = 10;
               }
               break;
            case 168:
               fastCodeException.type = 9;
               break;
            case 177:
            case 273:
               var15 = InstructionUtil.getInstructionAt(list, var16);
               if(var15 != null && var15.opcode == 168 && ((Jsr)var15).branch < 0) {
                  fastCodeException.type = 12;
               } else if(var11 > 0 && ((Instruction)list.get(var11 - 1)).opcode == 168) {
                  fastCodeException.type = 9;
               } else {
                  fastCodeException.type = 10;
               }
               break;
            case 191:
               fastCodeException.type = 12;
               break;
            default:
               var11 = InstructionUtil.getIndexForOffset(list, fastCodeException.finallyFromOffset);
               var18 = (Instruction)list.get(var11 + 1);
               if(var18.opcode != 58) {
                  fastCodeException.type = 10;
               } else {
                  exceptionIndex = ((AStore)list.get(var11)).index;
                  lenght = list.size();

                  while(true) {
                     ++var11;
                     if(var11 >= lenght) {
                        break;
                     }

                     var15 = (Instruction)list.get(var11);
                     if(var15.opcode == 191) {
                        AThrow athrow = (AThrow)var15;
                        if(athrow.value.opcode == 25 && ((ALoad)athrow.value).index == exceptionIndex) {
                           break;
                        }
                     }
                  }

                  ++var11;
                  if(var11 >= lenght) {
                     fastCodeException.type = 10;
                  } else {
                     var15 = (Instruction)list.get(var11);
                     fastCodeException.type = var15.opcode == var18.opcode && var18.lineNumber != Instruction.UNKNOWN_LINE_NUMBER && var18.lineNumber == var15.lineNumber?14:10;
                  }
               }
            }
         } else {
            uniqueJumpAddressFlag = InstructionUtil.getIndexForOffset(list, fastCodeException.tryToOffset);
            if(uniqueJumpAddressFlag < 0) {
               return;
            }

            uniqueJumpAddress = (Instruction)list.get(uniqueJumpAddressFlag);
            switch(uniqueJumpAddress.opcode) {
            case 58:
               DefineTypeJikes122Or142(list, fastCodeException, ((AStore)uniqueJumpAddress).valueref, uniqueJumpAddressFlag);
               break;
            case 87:
               DefineTypeJikes122Or142(list, fastCodeException, ((Pop)uniqueJumpAddress).objectref, uniqueJumpAddressFlag);
               break;
            case 167:
               var12 = (Goto)uniqueJumpAddress;
               uniqueJumpAddress = InstructionUtil.getInstructionAt(list, var12.GetJumpOffset());
               if(uniqueJumpAddress == null) {
                  return;
               }

               if(uniqueJumpAddress.opcode == 168 && ((Jsr)uniqueJumpAddress).branch < 0) {
                  fastCodeException.type = 12;
               } else if(uniqueJumpAddressFlag > 0 && ((Instruction)list.get(uniqueJumpAddressFlag - 1)).opcode == 168) {
                  fastCodeException.type = 9;
               } else {
                  fastCodeException.type = 10;
               }
               break;
            case 168:
               fastCodeException.type = 9;
               break;
            case 177:
            case 273:
               if(uniqueJumpAddressFlag > 0 && ((Instruction)list.get(uniqueJumpAddressFlag - 1)).opcode == 168) {
                  fastCodeException.type = 9;
               } else {
                  fastCodeException.type = 10;
               }
               break;
            case 191:
               fastCodeException.type = 12;
               break;
            default:
               fastCodeException.type = 10;
            }
         }
      }

   }

   private static boolean TryBlockContainsJsr(List<Instruction> list, FastCodeExceptionAnalyzer.FastCodeException fastCodeException) {
      int index = InstructionUtil.getIndexForOffset(list, fastCodeException.tryToOffset);
      if(index != -1) {
         int tryFromOffset = fastCodeException.tryFromOffset;

         while(true) {
            Instruction instruction = (Instruction)list.get(index);
            if(instruction.offset <= tryFromOffset) {
               break;
            }

            if(instruction.opcode == 168 && ((Jsr)instruction).GetJumpOffset() > fastCodeException.finallyFromOffset) {
               return true;
            }

            if(index == 0) {
               break;
            }

            --index;
         }
      }

      return false;
   }

   private static void DefineTypeJikes122Or142(List<Instruction> list, FastCodeExceptionAnalyzer.FastCodeException fastCodeException, Instruction instruction, int index) {
      if(instruction.opcode == 270) {
         --index;
         instruction = (Instruction)list.get(index);
         if(instruction.opcode == 167) {
            int jumpAddress = ((Goto)instruction).GetJumpOffset();
            instruction = InstructionUtil.getInstructionAt(list, jumpAddress);
            if(instruction != null && instruction.opcode == 168) {
               fastCodeException.type = 12;
               return;
            }
         }
      }

      fastCodeException.type = 10;
   }

   private static void ComputeAfterOffset(Method method, List<Instruction> list, ArrayList<int[]> switchCaseOffsets, ArrayList<FastCodeExceptionAnalyzer.FastCodeException> fastCodeExceptions, FastCodeExceptionAnalyzer.FastCodeException fastCodeException, int fastCodeExceptionIndex) {
      int length;
      Instruction afterOffset;
      int tryFromOffset;
      int tryIndex;
      int maxIndex;
      AThrow index;
      int instruction;
      int s;
      int maxOffset;
      int var18;
      Instruction var20;
      int var24;
      switch(fastCodeException.type) {
      case 2:
         length = InstructionUtil.getIndexForOffset(list, fastCodeException.finallyFromOffset);
         if(length <= 0) {
            return;
         }

         var18 = list.size();
         tryFromOffset = ((Instruction)list.get(var18 - 1)).offset + 1;
         boolean[] var22 = new boolean[tryFromOffset];
         maxIndex = 0;

         while(true) {
            ++length;
            if(length >= var18) {
               return;
            }

            Instruction var25 = (Instruction)list.get(length);
            if(var22[var25.offset]) {
               ++maxIndex;
            }

            if(var25.opcode == 168) {
               instruction = ((Jsr)var25).GetJumpOffset();
               if(instruction < tryFromOffset) {
                  var22[instruction] = true;
               }
            } else if(var25.opcode == 169) {
               if(maxIndex <= 1) {
                  fastCodeException.afterOffset = var25.offset + 1;
                  return;
               }

               --maxIndex;
            }
         }
      case 3:
         length = InstructionUtil.getIndexForOffset(list, fastCodeException.afterOffset);
         if(length < 0 || length >= list.size()) {
            return;
         }

         ++length;
         afterOffset = (Instruction)list.get(length);
         if(afterOffset.opcode != 167) {
            return;
         }

         fastCodeException.afterOffset = ((Goto)afterOffset).GetJumpOffset();
         break;
      case 4:
      case 9:
         length = InstructionUtil.getIndexForOffset(list, fastCodeException.finallyFromOffset);
         if(length <= 0) {
            return;
         }

         var18 = list.size();

         do {
            ++length;
            if(length >= var18) {
               return;
            }

            var20 = (Instruction)list.get(length);
         } while(var20.opcode != 169);

         ++length;
         fastCodeException.afterOffset = length < var18?((Instruction)list.get(length)).offset:var20.offset + 1;
         break;
      case 5:
      case 6:
      case 10:
      case 11:
      default:
         length = list.size();
         var18 = fastCodeException.afterOffset;
         if(var18 == -1) {
            var18 = ((Instruction)list.get(length - 1)).offset + 1;
         }

         var18 = ReduceAfterOffsetWithBranchInstructions(list, fastCodeException, fastCodeException.maxOffset, var18);
         if(!fastCodeException.synchronizedFlag) {
            var18 = ReduceAfterOffsetWithLineNumbers(list, fastCodeException, var18);
         }

         var18 = ReduceAfterOffsetWithSwitchInstructions(switchCaseOffsets, fastCodeException.tryFromOffset, fastCodeException.maxOffset, var18);
         fastCodeException.afterOffset = var18 = ReduceAfterOffsetWithExceptions(fastCodeExceptions, fastCodeException.tryFromOffset, fastCodeException.maxOffset, var18);
         tryFromOffset = Integer.MAX_VALUE;

         for(tryIndex = fastCodeExceptionIndex + 1; tryIndex < fastCodeExceptions.size(); ++tryIndex) {
            maxIndex = ((FastCodeExceptionAnalyzer.FastCodeException)fastCodeExceptions.get(tryIndex)).tryFromOffset;
            if(maxIndex > fastCodeException.maxOffset) {
               tryFromOffset = maxIndex;
               break;
            }
         }

         maxIndex = InstructionUtil.getIndexForOffset(list, fastCodeException.maxOffset);
         var24 = maxIndex;

         while(true) {
            label348:
            while(var24 < length) {
               Instruction var27 = (Instruction)list.get(var24);
               if(var27.offset >= var18) {
                  return;
               }

               if(var27.offset > tryFromOffset) {
                  FastCodeExceptionAnalyzer.FastCodeException var28 = (FastCodeExceptionAnalyzer.FastCodeException)fastCodeExceptions.get(tryIndex);
                  s = var28.afterOffset;

                  while(true) {
                     ++tryIndex;
                     if(tryIndex >= fastCodeExceptions.size()) {
                        tryFromOffset = Integer.MAX_VALUE;
                     } else {
                        maxOffset = ((FastCodeExceptionAnalyzer.FastCodeException)fastCodeExceptions.get(tryIndex)).tryFromOffset;
                        if(var28.tryFromOffset == maxOffset) {
                           FastCodeExceptionAnalyzer.FastCodeException var32 = (FastCodeExceptionAnalyzer.FastCodeException)fastCodeExceptions.get(tryIndex);
                           if(s < var32.afterOffset) {
                              s = var32.afterOffset;
                           }
                           continue;
                        }

                        tryFromOffset = maxOffset;
                     }

                     while(true) {
                        if(var24 >= length || ((Instruction)list.get(var24)).offset >= s) {
                           continue label348;
                        }

                        ++var24;
                     }
                  }
               } else {
                  int var26;
                  label341:
                  switch(var27.opcode) {
                  case 167:
                  case 260:
                  case 261:
                  case 262:
                     if(var27.opcode == 167) {
                        var26 = ((BranchInstruction)var27).GetJumpOffset();
                     } else {
                        var24 = ComparisonInstructionAnalyzer.GetLastIndex(list, var24);
                        BranchInstruction var30 = (BranchInstruction)list.get(var24);
                        var26 = var30.GetJumpOffset();
                     }

                     if(var26 > var27.offset) {
                        if(var26 >= var18) {
                           if(var27.opcode == 167 || var26 != var18) {
                              s = InstructionUtil.getIndexForOffset(list, fastCodeException.tryFromOffset);
                              maxOffset = var24 == 0?0:((Instruction)list.get(var24 - 1)).offset;
                              if(InstructionUtil.CheckNoJumpToInterval(list, s, maxIndex, maxOffset, var27.offset)) {
                                 fastCodeException.afterOffset = var27.offset + 1;
                              } else {
                                 fastCodeException.afterOffset = var27.offset;
                              }
                           }

                           return;
                        }

                        do {
                           ++var24;
                           if(var24 >= length) {
                              break label341;
                           }
                        } while(((Instruction)list.get(var24)).offset < var26);

                        --var24;
                     } else if(var26 <= fastCodeException.tryFromOffset) {
                        if(var24 > 0 && var27.lineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
                           Instruction var31 = (Instruction)list.get(var24 - 1);
                           if(var27.lineNumber == var31.lineNumber) {
                              if(var31.opcode == 58 && ((AStore)var31).valueref.opcode == 270) {
                                 fastCodeException.afterOffset = var27.offset;
                              } else if(var31.opcode == 87 && ((Pop)var31).objectref.opcode == 270) {
                                 fastCodeException.afterOffset = var27.offset;
                              } else {
                                 fastCodeException.afterOffset = var31.offset;
                              }

                              return;
                           }
                        }

                        fastCodeException.afterOffset = var27.offset;
                        return;
                     }
                     break;
                  case 170:
                  case 171:
                     Switch var29 = (Switch)var27;
                     maxOffset = var29.defaultOffset;
                     int i = var29.offsets.length;

                     while(i-- > 0) {
                        int offset = var29.offsets[i];
                        if(maxOffset < offset) {
                           maxOffset = offset;
                        }
                     }

                     if(maxOffset < var18) {
                        do {
                           ++var24;
                           if(var24 >= length) {
                              break label341;
                           }
                        } while(((Instruction)list.get(var24)).offset < maxOffset);

                        --var24;
                     }
                     break;
                  case 177:
                  case 191:
                  case 273:
                     if(CheckLocalVariableUsedVisitor.Visit(method.getLocalVariables(), fastCodeException.maxOffset, var27)) {
                        fastCodeException.afterOffset = var27.offset + 1;
                     } else if(CheckTernaryOperator(list, var24)) {
                        fastCodeException.afterOffset = var27.offset + 1;
                     } else if(var24 + 1 >= length) {
                        if(var27.opcode == 191) {
                           fastCodeException.afterOffset = var27.offset + 1;
                        } else {
                           fastCodeException.afterOffset = var27.offset;
                        }
                     } else {
                        var26 = InstructionUtil.getIndexForOffset(list, fastCodeException.tryFromOffset);
                        s = var24 == 0?0:((Instruction)list.get(var24 - 1)).offset;
                        if(InstructionUtil.CheckNoJumpToInterval(list, var26, maxIndex, s, var27.offset)) {
                           fastCodeException.afterOffset = var27.offset + 1;
                        } else {
                           fastCodeException.afterOffset = var27.offset;
                        }
                     }

                     return;
                  }

                  ++var24;
               }
            }

            return;
         }
      case 7:
         length = InstructionUtil.getIndexForOffset(list, fastCodeException.afterOffset);
         if(length < 0 || length >= list.size()) {
            return;
         }

         var18 = list.size();
         IntSet var21 = new IntSet();
         tryIndex = 0;

         while(true) {
            ++length;
            if(length >= var18) {
               return;
            }

            Instruction var23 = (Instruction)list.get(length);
            switch(var23.opcode) {
            case 168:
               var21.add(((Jsr)var23).GetJumpOffset());
               break;
            case 169:
               if(var21.size() == tryIndex) {
                  fastCodeException.afterOffset = var23.offset + 1;
                  return;
               }

               ++tryIndex;
            }
         }
      case 8:
         Instruction var19 = InstructionUtil.getInstructionAt(list, fastCodeException.afterOffset);
         if(var19 == null) {
            return;
         }

         fastCodeException.afterOffset = var19.offset + 1;
      case 12:
         break;
      case 13:
         length = InstructionUtil.getIndexForOffset(list, fastCodeException.finallyFromOffset);
         if(length < 0) {
            return;
         }

         var18 = list.size();
         var20 = (Instruction)list.get(length);
         switch(var20.opcode) {
         case 58:
            tryIndex = length + 1;
            maxIndex = ((AStore)var20).index;

            while(true) {
               ++length;
               if(length >= var18) {
                  break;
               }

               var20 = (Instruction)list.get(length);
               if(var20.opcode == 191) {
                  index = (AThrow)var20;
                  if(index.value.opcode == 25 && ((ALoad)index.value).index == maxIndex) {
                     break;
                  }
               }
            }

            length += length - tryIndex + 1;
            if(length < var18) {
               fastCodeException.afterOffset = ((Instruction)list.get(length)).offset;
            }

            return;
         case 87:
            do {
               ++length;
               if(length >= var18) {
                  return;
               }

               var20 = (Instruction)list.get(length);
            } while(var20.opcode != 191);

            fastCodeException.afterOffset = var20.offset + 1;
            return;
         default:
            return;
         }
      case 14:
         length = InstructionUtil.getIndexForOffset(list, fastCodeException.finallyFromOffset);
         if(length < 0) {
            return;
         }

         afterOffset = (Instruction)list.get(length);
         if(afterOffset.opcode != 58) {
            return;
         }

         tryFromOffset = length + 1;
         tryIndex = ((AStore)afterOffset).index;
         maxIndex = list.size();

         while(true) {
            ++length;
            if(length >= maxIndex) {
               break;
            }

            afterOffset = (Instruction)list.get(length);
            if(afterOffset.opcode == 191) {
               index = (AThrow)afterOffset;
               if(index.value.opcode == 25 && ((ALoad)index.value).index == tryIndex) {
                  break;
               }
            }
         }

         var24 = length - tryFromOffset;
         length += var24 + 1;
         instruction = ((Instruction)list.get(length)).offset;
         if(length < maxIndex && ((Instruction)list.get(length)).opcode == 167) {
            Goto jumpOffsetTmp = (Goto)list.get(length);
            s = jumpOffsetTmp.GetJumpOffset();
            maxOffset = length + var24 + 1;
            if(maxOffset < maxIndex && ((Instruction)list.get(maxOffset - 1)).offset < s && s <= ((Instruction)list.get(maxOffset)).offset) {
               instruction = ReduceAfterOffsetWithBranchInstructions(list, fastCodeException, fastCodeException.finallyFromOffset, ((Instruction)list.get(maxOffset)).offset);
               if(!fastCodeException.synchronizedFlag) {
                  instruction = ReduceAfterOffsetWithLineNumbers(list, fastCodeException, instruction);
               }

               instruction = ReduceAfterOffsetWithExceptions(fastCodeExceptions, fastCodeException.tryFromOffset, fastCodeException.finallyFromOffset, instruction);
            }
         }

         fastCodeException.afterOffset = instruction;
      }

   }

   private static boolean CheckTernaryOperator(List<Instruction> list, int index) {
      if(index > 2 && ((Instruction)list.get(index - 1)).opcode == 167 && ((Instruction)list.get(index - 2)).opcode == 280) {
         Goto g = (Goto)list.get(index - 1);
         int jumpOffset = g.GetJumpOffset();
         int returnOffset = ((Instruction)list.get(index)).offset;
         if(g.offset < jumpOffset && jumpOffset < returnOffset) {
            return true;
         }
      }

      return false;
   }

   private static int ReduceAfterOffsetWithBranchInstructions(List<Instruction> list, FastCodeExceptionAnalyzer.FastCodeException fastCodeException, int firstOffset, int afterOffset) {
      int index = InstructionUtil.getIndexForOffset(list, fastCodeException.tryFromOffset);
      Instruction instruction;
      int jumpOffset;
      if(index != -1) {
         while(index-- > 0) {
            instruction = (Instruction)list.get(index);
            switch(instruction.opcode) {
            case 167:
            case 260:
            case 261:
            case 262:
               jumpOffset = ((BranchInstruction)instruction).GetJumpOffset();
               if(firstOffset < jumpOffset && jumpOffset < afterOffset) {
                  afterOffset = jumpOffset;
               }
            }
         }
      }

      index = list.size();

      do {
         --index;
         instruction = (Instruction)list.get(index);
         switch(instruction.opcode) {
         case 167:
         case 260:
         case 261:
         case 262:
            jumpOffset = ((BranchInstruction)instruction).GetJumpOffset();
            if(firstOffset < jumpOffset && jumpOffset < afterOffset) {
               afterOffset = jumpOffset;
            }
         }
      } while(instruction.offset > afterOffset);

      return afterOffset;
   }

   private static int ReduceAfterOffsetWithLineNumbers(List<Instruction> list, FastCodeExceptionAnalyzer.FastCodeException fastCodeException, int afterOffset) {
      int fromIndex = InstructionUtil.getIndexForOffset(list, fastCodeException.tryFromOffset);
      int index = fromIndex;
      if(fromIndex != -1) {
         int lenght = list.size();
         int firstLineNumber = Instruction.UNKNOWN_LINE_NUMBER;

         Instruction instruction;
         do {
            instruction = (Instruction)list.get(index++);
            if(instruction.lineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
               firstLineNumber = instruction.lineNumber;
               break;
            }
         } while(instruction.offset < afterOffset && index < lenght);

         if(firstLineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
            int maxOffset = fastCodeException.maxOffset;
            index = InstructionUtil.getIndexForOffset(list, afterOffset);
            if(index != -1) {
               for(; index-- > 0; afterOffset = instruction.offset) {
                  instruction = (Instruction)list.get(index);
                  if(instruction.offset <= maxOffset || instruction.lineNumber != Instruction.UNKNOWN_LINE_NUMBER && instruction.lineNumber >= firstLineNumber) {
                     break;
                  }

                  int maxIndex;
                  if(instruction.opcode == 167) {
                     maxIndex = ((Goto)instruction).GetJumpOffset();
                     if(!InstructionUtil.CheckNoJumpToInterval(list, fromIndex, index, maxIndex - 1, maxIndex)) {
                        break;
                     }
                  }

                  if(instruction.opcode == 177) {
                     maxIndex = InstructionUtil.getIndexForOffset(list, maxOffset);
                     if(((Instruction)list.get(maxIndex - 1)).opcode == instruction.opcode) {
                        break;
                     }
                  }
               }
            }
         }
      }

      return afterOffset;
   }

   private static int ReduceAfterOffsetWithSwitchInstructions(ArrayList<int[]> switchCaseOffsets, int firstOffset, int lastOffset, int afterOffset) {
      int i = switchCaseOffsets.size();

      while(true) {
         int[] offsets;
         int j;
         do {
            if(i-- <= 0) {
               return afterOffset;
            }

            offsets = (int[])switchCaseOffsets.get(i);
            j = offsets.length;
         } while(j <= 1);

         --j;

         int offset1;
         for(int offset2 = offsets[j]; j-- > 0; offset2 = offset1) {
            offset1 = offsets[j];
            if(offset1 != -1 && offset1 <= firstOffset && lastOffset < offset2 && (afterOffset == -1 || afterOffset > offset2)) {
               afterOffset = offset2;
            }
         }
      }
   }

   private static int ReduceAfterOffsetWithExceptions(ArrayList<FastCodeExceptionAnalyzer.FastCodeException> fastCodeExceptions, int fromOffset, int maxOffset, int afterOffset) {
      int i = fastCodeExceptions.size();

      while(true) {
         int toOffset;
         do {
            FastCodeExceptionAnalyzer.FastCodeException fastCodeException;
            do {
               do {
                  if(i-- <= 0) {
                     return afterOffset;
                  }

                  fastCodeException = (FastCodeExceptionAnalyzer.FastCodeException)fastCodeExceptions.get(i);
                  toOffset = fastCodeException.finallyFromOffset;
                  FastCodeExceptionAnalyzer.FastCodeExceptionCatch fcec;
                  if(fastCodeException.catches != null) {
                     for(int j = fastCodeException.catches.size(); j-- > 0; toOffset = fcec.fromOffset) {
                        fcec = (FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fastCodeException.catches.get(j);
                        if(toOffset != -1 && fcec.fromOffset <= fromOffset && maxOffset < toOffset && (afterOffset == -1 || afterOffset > toOffset)) {
                           afterOffset = toOffset;
                        }
                     }
                  }
               } while(fastCodeException.tryFromOffset > fromOffset);
            } while(maxOffset >= toOffset);
         } while(afterOffset != -1 && afterOffset <= toOffset);

         afterOffset = toOffset;
      }
   }

   public static void FormatFastTry(LocalVariables localVariables, FastCodeExceptionAnalyzer.FastCodeException fce, FastTry fastTry, int returnOffset) {
      switch(fce.type) {
      case 1:
         FormatCatch(localVariables, fce, fastTry);
         break;
      case 2:
         Format118Finally(localVariables, fce, fastTry);
         break;
      case 3:
         Format118Finally2(fce, fastTry);
         break;
      case 4:
         Format118FinallyThrow(fastTry);
      case 5:
      case 6:
      default:
         break;
      case 7:
         Format118CatchFinally(fce, fastTry);
         break;
      case 8:
         Format118CatchFinally2(fce, fastTry);
         break;
      case 9:
         Format131CatchFinally(localVariables, fce, fastTry);
         break;
      case 10:
         Format142(localVariables, fce, fastTry);
         break;
      case 11:
         Format142FinallyThrow(fastTry);
         break;
      case 12:
         FormatJikes122(localVariables, fce, fastTry, returnOffset);
         break;
      case 13:
         FormatEclipse677Finally(fce, fastTry);
         break;
      case 14:
         FormatEclipse677CatchFinally(fce, fastTry, returnOffset);
      }

   }

   private static void FormatCatch(LocalVariables localVariables, FastCodeExceptionAnalyzer.FastCodeException fce, FastTry fastTry) {
      List tryInstructions = fastTry.instructions;
      int jumpOffset = -1;
      int i;
      int lastIndex;
      if(tryInstructions.size() > 0) {
         i = tryInstructions.size() - 1;
         Instruction catchInstructions = (Instruction)tryInstructions.get(i);
         if(catchInstructions.opcode == 167) {
            lastIndex = ((Goto)catchInstructions).GetJumpOffset();
            if(lastIndex < fce.tryFromOffset || catchInstructions.offset < lastIndex) {
               jumpOffset = lastIndex;
               fce.tryToOffset = catchInstructions.offset;
               tryInstructions.remove(i);
            }
         }
      }

      FormatFastTry_RemoveJsrInstructionAndCompactStoreReturn(tryInstructions, localVariables, Instruction.UNKNOWN_LINE_NUMBER);
      i = fastTry.catches.size();

      while(true) {
         List var10;
         do {
            if(i-- <= 0) {
               return;
            }

            var10 = ((FastTry.FastCatch)fastTry.catches.get(i)).instructions;
            if(FormatCatch_RemoveFirstCatchInstruction((Instruction)var10.get(0))) {
               var10.remove(0);
            }
         } while(var10.size() <= 0);

         lastIndex = var10.size() - 1;
         Instruction instruction = (Instruction)var10.get(lastIndex);
         if(instruction.opcode == 167) {
            int tmpJumpOffset = ((Goto)instruction).GetJumpOffset();
            if(tmpJumpOffset < fce.tryFromOffset || instruction.offset < tmpJumpOffset) {
               if(jumpOffset == -1) {
                  jumpOffset = tmpJumpOffset;
                  ((FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fce.catches.get(i)).toOffset = instruction.offset;
                  var10.remove(lastIndex);
               } else if(jumpOffset == tmpJumpOffset) {
                  ((FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fce.catches.get(i)).toOffset = instruction.offset;
                  var10.remove(lastIndex);
               }
            }
         }

         FormatFastTry_RemoveJsrInstructionAndCompactStoreReturn(var10, localVariables, Instruction.UNKNOWN_LINE_NUMBER);
      }
   }

   private static boolean FormatCatch_RemoveFirstCatchInstruction(Instruction instruction) {
      switch(instruction.opcode) {
      case 58:
         if(((AStore)instruction).valueref.opcode == 270) {
            return true;
         }

         return false;
      case 87:
         if(((Pop)instruction).objectref.opcode == 270) {
            return true;
         }

         return false;
      default:
         return false;
      }
   }

   private static void Format118Finally(LocalVariables localVariables, FastCodeExceptionAnalyzer.FastCodeException fce, FastTry fastTry) {
      List tryInstructions = fastTry.instructions;
      int length = tryInstructions.size();
      --length;
      if(((Instruction)tryInstructions.get(length)).opcode == 167) {
         Goto finallyInstructitonsLineNumber = (Goto)tryInstructions.remove(length);
         fce.tryToOffset = finallyInstructitonsLineNumber.offset;
      }

      --length;
      if(((Instruction)tryInstructions.get(length)).opcode != 168) {
         throw new UnexpectedInstructionException();
      } else {
         tryInstructions.remove(length);
         int var6 = ((Instruction)fastTry.finallyInstructions.get(0)).lineNumber;
         FormatFastTry_RemoveJsrInstructionAndCompactStoreReturn(tryInstructions, localVariables, var6);
         Format118FinallyThrow(fastTry);
      }
   }

   private static void Format118Finally2(FastCodeExceptionAnalyzer.FastCodeException fce, FastTry fastTry) {
      List tryInstructions = fastTry.instructions;
      int tryInstructionsLength = tryInstructions.size();
      if(((Instruction)tryInstructions.get(tryInstructionsLength - 1)).opcode == 167) {
         --tryInstructionsLength;
         Goto finallyInstructions = (Goto)tryInstructions.remove(tryInstructionsLength);
         fce.tryToOffset = finallyInstructions.offset;
      }

      List var10 = fastTry.finallyInstructions;
      int finallyInstructionsLength = var10.size();
      if(finallyInstructionsLength > 5) {
         int firstFinallyOffset = ((Instruction)var10.get(0)).offset;
         int lastFinallyOffset = ((Instruction)var10.get(5)).offset;

         while(tryInstructionsLength-- > 0) {
            Instruction instruction = (Instruction)tryInstructions.get(tryInstructionsLength);
            int jumpOffset;
            switch(instruction.opcode) {
            case 167:
               jumpOffset = ((Goto)instruction).GetJumpOffset();
               if(firstFinallyOffset < jumpOffset && jumpOffset <= lastFinallyOffset) {
                  ((Goto)instruction).branch = firstFinallyOffset - instruction.offset;
               }
               break;
            case 260:
            case 262:
               jumpOffset = ((IfInstruction)instruction).GetJumpOffset();
               if(firstFinallyOffset < jumpOffset && jumpOffset <= lastFinallyOffset) {
                  ((IfInstruction)instruction).branch = firstFinallyOffset - instruction.offset;
               }
               break;
            case 261:
               jumpOffset = ((IfCmp)instruction).GetJumpOffset();
               if(firstFinallyOffset < jumpOffset && jumpOffset <= lastFinallyOffset) {
                  ((IfCmp)instruction).branch = firstFinallyOffset - instruction.offset;
               }
               break;
            case 284:
               jumpOffset = ((BranchInstruction)instruction).GetJumpOffset();
               if(firstFinallyOffset < jumpOffset && jumpOffset <= lastFinallyOffset) {
                  ((ComplexConditionalBranchInstruction)instruction).branch = firstFinallyOffset - instruction.offset;
               }
            }
         }
      }

      var10.remove(finallyInstructionsLength - 1);
      var10.remove(0);
      var10.remove(0);
      var10.remove(0);
      var10.remove(0);
      var10.remove(0);
      var10.remove(0);
   }

   private static void Format118FinallyThrow(FastTry fastTry) {
      List finallyInstructions = fastTry.finallyInstructions;
      int length = finallyInstructions.size();
      --length;
      Instruction i = (Instruction)finallyInstructions.get(length);
      if(i.opcode != 169) {
         throw new UnexpectedInstructionException();
      } else {
         finallyInstructions.remove(length);
         finallyInstructions.remove(0);
         finallyInstructions.remove(0);
         finallyInstructions.remove(0);
         finallyInstructions.remove(0);
      }
   }

   private static void Format118CatchFinally(FastCodeExceptionAnalyzer.FastCodeException fce, FastTry fastTry) {
      List tryInstructions = fastTry.instructions;
      int tryInstructionsLength = tryInstructions.size();
      --tryInstructionsLength;
      if(((Instruction)tryInstructions.get(tryInstructionsLength)).opcode == 167) {
         Goto i = (Goto)tryInstructions.remove(tryInstructionsLength);
         fce.tryToOffset = i.offset;
      }

      int var8 = fastTry.catches.size() - 1;
      List finallyInstructions;
      int finallyInstructionsLength;
      if(var8 >= 0) {
         finallyInstructions = ((FastTry.FastCatch)fastTry.catches.get(var8)).instructions;
         finallyInstructionsLength = finallyInstructions.size();
         --finallyInstructionsLength;
         switch(((Instruction)finallyInstructions.get(finallyInstructionsLength)).opcode) {
         case 167:
            finallyInstructions.remove(finallyInstructionsLength);
            --finallyInstructionsLength;
            finallyInstructions.remove(finallyInstructionsLength);
            break;
         case 177:
         case 273:
            --finallyInstructionsLength;
            finallyInstructions.remove(finallyInstructionsLength);
            if(finallyInstructionsLength > 0 && ((Instruction)finallyInstructions.get(finallyInstructionsLength - 1)).opcode == 191) {
               finallyInstructions.remove(finallyInstructionsLength);
            }
         }

         finallyInstructions.remove(0);

         for(; var8-- > 0; finallyInstructions.remove(0)) {
            finallyInstructions = ((FastTry.FastCatch)fastTry.catches.get(var8)).instructions;
            finallyInstructionsLength = finallyInstructions.size();
            --finallyInstructionsLength;
            switch(((Instruction)finallyInstructions.get(finallyInstructionsLength)).opcode) {
            case 167:
               Instruction in = (Instruction)finallyInstructions.remove(finallyInstructionsLength);
               ((FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fce.catches.get(var8)).toOffset = in.offset;
               break;
            case 177:
            case 273:
               --finallyInstructionsLength;
               finallyInstructions.remove(finallyInstructionsLength);
            }
         }
      }

      finallyInstructions = fastTry.finallyInstructions;
      finallyInstructionsLength = finallyInstructions.size();
      --finallyInstructionsLength;
      finallyInstructions.remove(finallyInstructionsLength);
      finallyInstructions.remove(0);
      finallyInstructions.remove(0);
      finallyInstructions.remove(0);
      finallyInstructions.remove(0);
   }

   private static void Format118CatchFinally2(FastCodeExceptionAnalyzer.FastCodeException fce, FastTry fastTry) {
      List tryInstructions = fastTry.instructions;
      int tryInstructionsLength = tryInstructions.size();
      --tryInstructionsLength;
      if(((Instruction)tryInstructions.get(tryInstructionsLength)).opcode == 167) {
         Goto i = (Goto)tryInstructions.remove(tryInstructionsLength);
         fce.tryToOffset = i.offset;
      }

      int var8 = fastTry.catches.size();

      List finallyInstructions;
      while(var8-- > 0) {
         finallyInstructions = ((FastTry.FastCatch)fastTry.catches.get(var8)).instructions;
         int catchInstructionsLength = finallyInstructions.size();
         Instruction in = (Instruction)finallyInstructions.remove(catchInstructionsLength - 1);
         ((FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fce.catches.get(var8)).toOffset = in.offset;
         finallyInstructions.remove(0);
      }

      finallyInstructions = fastTry.finallyInstructions;
      finallyInstructions.remove(0);
   }

   private static void Format131CatchFinally(LocalVariables localVariables, FastCodeExceptionAnalyzer.FastCodeException fce, FastTry fastTry) {
      List tryInstructions = fastTry.instructions;
      int length = tryInstructions.size();
      --length;
      if(((Instruction)tryInstructions.get(length)).opcode == 167) {
         Goto finallyInstructitonsLineNumber = (Goto)tryInstructions.remove(length);
         fce.tryToOffset = finallyInstructitonsLineNumber.offset;
      }

      int var11 = ((Instruction)fastTry.finallyInstructions.get(0)).lineNumber;
      int jumpOffset = FormatFastTry_RemoveJsrInstructionAndCompactStoreReturn(tryInstructions, localVariables, var11);
      length = tryInstructions.size();
      --length;
      if(((Instruction)tryInstructions.get(length)).opcode == 168) {
         Jsr finallyInstructions = (Jsr)tryInstructions.remove(length);
         jumpOffset = finallyInstructions.GetJumpOffset();
      }

      if(jumpOffset == -1) {
         throw new UnexpectedInstructionException();
      } else {
         List var12 = fastTry.finallyInstructions;
         int i;
         List catchInstructions;
         Goto var13;
         if(jumpOffset < ((Instruction)var12.get(0)).offset) {
            i = fastTry.catches.size();

            while(i-- > 0) {
               catchInstructions = ((FastTry.FastCatch)fastTry.catches.get(i)).instructions;
               if(catchInstructions.size() != 0 && ((Instruction)catchInstructions.get(0)).offset <= jumpOffset) {
                  int g = InstructionUtil.getIndexForOffset(catchInstructions, jumpOffset);
                  var12.clear();

                  while(((Instruction)catchInstructions.get(g)).opcode != 169) {
                     var12.add((Instruction)catchInstructions.remove(g));
                  }

                  if(((Instruction)catchInstructions.get(g)).opcode == 169) {
                     var12.add((Instruction)catchInstructions.remove(g));
                  }
                  break;
               }
            }

            i = fastTry.catches.size();

            while(i-- > 0) {
               catchInstructions = ((FastTry.FastCatch)fastTry.catches.get(i)).instructions;
               length = catchInstructions.size();
               --length;
               if(((Instruction)catchInstructions.get(length)).opcode == 167) {
                  var13 = (Goto)catchInstructions.remove(length);
                  ((FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fce.catches.get(i)).toOffset = var13.offset;
               }

               --length;
               if(((Instruction)catchInstructions.get(length)).opcode == 168) {
                  catchInstructions.remove(length);
               }

               FormatFastTry_RemoveJsrInstructionAndCompactStoreReturn(catchInstructions, localVariables, var11);
               catchInstructions.remove(0);
            }

            length = var12.size();
            --length;
            var12.remove(length);
            var12.remove(0);
         } else {
            i = fastTry.catches.size();

            while(i-- > 0) {
               catchInstructions = ((FastTry.FastCatch)fastTry.catches.get(i)).instructions;
               length = catchInstructions.size();
               --length;
               if(((Instruction)catchInstructions.get(length)).opcode == 167) {
                  var13 = (Goto)catchInstructions.remove(length);
                  ((FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fce.catches.get(i)).toOffset = var13.offset;
               }

               --length;
               if(((Instruction)catchInstructions.get(length)).opcode == 168) {
                  catchInstructions.remove(length);
               }

               FormatFastTry_RemoveJsrInstructionAndCompactStoreReturn(catchInstructions, localVariables, var11);
               catchInstructions.remove(0);
            }

            length = var12.size();
            --length;
            var12.remove(length);
            var12.remove(0);
            var12.remove(0);
            var12.remove(0);
            var12.remove(0);
         }

      }
   }

   private static void Format142(LocalVariables localVariables, FastCodeExceptionAnalyzer.FastCodeException fce, FastTry fastTry) {
      List finallyInstructions = fastTry.finallyInstructions;
      int finallyInstructitonsSize = finallyInstructions.size();
      if(((Instruction)finallyInstructions.get(finallyInstructitonsSize - 1)).opcode == 191) {
         finallyInstructions.remove(finallyInstructitonsSize - 1);
      }

      switch(((Instruction)finallyInstructions.get(0)).opcode) {
      case 58:
      case 87:
         finallyInstructions.remove(0);
      }

      finallyInstructitonsSize = finallyInstructions.size();
      if(finallyInstructitonsSize > 0) {
         FastCompareInstructionVisitor visitor = new FastCompareInstructionVisitor();
         List tryInstructions = fastTry.instructions;
         int length = tryInstructions.size();
         switch(((Instruction)tryInstructions.get(length - 1)).opcode) {
         case 167:
            --length;
            Goto i = (Goto)tryInstructions.get(length);
            if(i.branch > 0) {
               tryInstructions.remove(length);
               fce.tryToOffset = i.offset;
            }
         }

         Format142_RemoveFinallyInstructionsBeforeReturnAndCompactStoreReturn(localVariables, visitor, tryInstructions, finallyInstructions);
         if(fastTry.catches != null) {
            int var11 = fastTry.catches.size();

            while(var11-- > 0) {
               List catchInstructions = ((FastTry.FastCatch)fastTry.catches.get(var11)).instructions;
               length = catchInstructions.size();
               switch(((Instruction)catchInstructions.get(length - 1)).opcode) {
               case 167:
                  --length;
                  Goto g = (Goto)catchInstructions.get(length);
                  if(g.branch > 0) {
                     catchInstructions.remove(length);
                     ((FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fce.catches.get(var11)).toOffset = g.offset;
                  }
               default:
                  Format142_RemoveFinallyInstructionsBeforeReturnAndCompactStoreReturn(localVariables, visitor, catchInstructions, finallyInstructions);
                  if(catchInstructions.size() > 0) {
                     catchInstructions.remove(0);
                  }
               }
            }
         }
      }

   }

   private static void Format142_RemoveFinallyInstructionsBeforeReturnAndCompactStoreReturn(LocalVariables localVariables, FastCompareInstructionVisitor visitor, List<Instruction> instructions, List<Instruction> finallyInstructions) {
      int index = instructions.size();
      int finallyInstructitonsSize = finallyInstructions.size();
      int finallyInstructitonsLineNumber = ((Instruction)finallyInstructions.get(0)).lineNumber;
      boolean match = index >= finallyInstructitonsSize && visitor.visit(instructions, finallyInstructions, index - finallyInstructitonsSize, 0, finallyInstructitonsSize);
      if(match) {
         for(int instruction = 0; instruction < finallyInstructitonsSize && index > 0; ++instruction) {
            --index;
            instructions.remove(index);
         }
      }

      while(true) {
         while(index-- > 0) {
            Instruction var11 = (Instruction)instructions.get(index);
            int var13;
            switch(var11.opcode) {
            case 177:
            case 191:
               match = index >= finallyInstructitonsSize && visitor.visit(instructions, finallyInstructions, index - finallyInstructitonsSize, 0, finallyInstructitonsSize);
               if(match) {
                  for(var13 = 0; var13 < finallyInstructitonsSize && index > 0; ++var13) {
                     --index;
                     instructions.remove(index);
                  }
               }

               if(var11.lineNumber != Instruction.UNKNOWN_LINE_NUMBER && var11.lineNumber >= finallyInstructitonsLineNumber) {
                  var11.lineNumber = Instruction.UNKNOWN_LINE_NUMBER;
               }
               break;
            case 273:
               match = index >= finallyInstructitonsSize && visitor.visit(instructions, finallyInstructions, index - finallyInstructitonsSize, 0, finallyInstructitonsSize);
               if(match) {
                  for(var13 = 0; var13 < finallyInstructitonsSize && index > 0; ++var13) {
                     --index;
                     instructions.remove(index);
                  }
               }

               ReturnInstruction var14 = (ReturnInstruction)var11;
               if(var14.lineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
                  switch(var14.valueref.opcode) {
                  case 21:
                     if(((Instruction)instructions.get(index - 1)).opcode == 54) {
                        index = CompactStoreReturn(instructions, localVariables, var14, index, finallyInstructitonsLineNumber);
                     }
                     continue;
                  case 25:
                     if(((Instruction)instructions.get(index - 1)).opcode == 58) {
                        index = CompactStoreReturn(instructions, localVariables, var14, index, finallyInstructitonsLineNumber);
                     }
                     continue;
                  case 268:
                     if(((Instruction)instructions.get(index - 1)).opcode == 269) {
                        index = CompactStoreReturn(instructions, localVariables, var14, index, finallyInstructitonsLineNumber);
                     }
                  }
               }
               break;
            case 318:
               FastTry var12 = (FastTry)var11;
               Format142_RemoveFinallyInstructionsBeforeReturnAndCompactStoreReturn(localVariables, visitor, var12.instructions, finallyInstructions);
               if(var12.catches != null) {
                  int i = var12.catches.size();

                  while(i-- > 0) {
                     Format142_RemoveFinallyInstructionsBeforeReturnAndCompactStoreReturn(localVariables, visitor, ((FastTry.FastCatch)var12.catches.get(i)).instructions, finallyInstructions);
                  }
               }

               if(var12.finallyInstructions != null) {
                  Format142_RemoveFinallyInstructionsBeforeReturnAndCompactStoreReturn(localVariables, visitor, var12.finallyInstructions, finallyInstructions);
               }
               break;
            case 319:
               FastSynchronized fs = (FastSynchronized)var11;
               Format142_RemoveFinallyInstructionsBeforeReturnAndCompactStoreReturn(localVariables, visitor, fs.instructions, finallyInstructions);
            }
         }

         return;
      }
   }

   private static int CompactStoreReturn(List<Instruction> instructions, LocalVariables localVariables, ReturnInstruction ri, int index, int finallyInstructitonsLineNumber) {
      IndexInstruction load = (IndexInstruction)ri.valueref;
      StoreInstruction store = (StoreInstruction)instructions.get(index - 1);
      if(load.index == store.index && (load.lineNumber <= store.lineNumber || load.lineNumber >= finallyInstructitonsLineNumber)) {
         LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(store.index, store.offset);
         if(lv != null && lv.start_pc == store.offset && lv.start_pc + lv.length <= ri.offset) {
            localVariables.removeLocalVariableWithIndexAndOffset(store.index, store.offset);
         }

         ri.valueref = store.valueref;
         if(ri.lineNumber > store.lineNumber) {
            ri.lineNumber = store.lineNumber;
         }

         --index;
         instructions.remove(index);
      }

      return index;
   }

   private static void Format142FinallyThrow(FastTry fastTry) {
      fastTry.finallyInstructions.remove(fastTry.finallyInstructions.size() - 1);
      fastTry.finallyInstructions.remove(0);
   }

   private static void FormatJikes122(LocalVariables localVariables, FastCodeExceptionAnalyzer.FastCodeException fce, FastTry fastTry, int returnOffset) {
      List tryInstructions = fastTry.instructions;
      int lastIndex = tryInstructions.size() - 1;
      Instruction lastTryInstruction = (Instruction)tryInstructions.get(lastIndex);
      int lastTryInstructionOffset = lastTryInstruction.offset;
      if(((Instruction)tryInstructions.get(lastIndex)).opcode == 167) {
         Goto finallyInstructitonsLineNumber = (Goto)tryInstructions.remove(lastIndex);
         fce.tryToOffset = finallyInstructitonsLineNumber.offset;
      }

      int var12 = ((Instruction)fastTry.finallyInstructions.get(0)).lineNumber;
      FormatFastTry_RemoveJsrInstructionAndCompactStoreReturn(tryInstructions, localVariables, var12);
      int i = fastTry.catches.size();

      List finallyInstructions;
      while(i-- > 0) {
         finallyInstructions = ((FastTry.FastCatch)fastTry.catches.get(i)).instructions;
         lastIndex = finallyInstructions.size() - 1;
         if(((Instruction)finallyInstructions.get(lastIndex)).opcode == 167) {
            Goto length = (Goto)finallyInstructions.remove(lastIndex);
            ((FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fce.catches.get(i)).toOffset = length.offset;
         }

         FormatFastTry_RemoveJsrInstructionAndCompactStoreReturn(finallyInstructions, localVariables, var12);
         FormatFastTry_FormatNegativeJumpOffset(finallyInstructions, lastTryInstructionOffset, returnOffset);
         finallyInstructions.remove(0);
      }

      finallyInstructions = fastTry.finallyInstructions;
      int var13 = finallyInstructions.size();
      --var13;
      finallyInstructions.remove(var13);
      --var13;
      finallyInstructions.remove(var13);
      finallyInstructions.remove(0);
      if(((Instruction)finallyInstructions.get(0)).opcode == 168) {
         finallyInstructions.remove(0);
      }

      if(((Instruction)finallyInstructions.get(0)).opcode == 191) {
         finallyInstructions.remove(0);
      }

      if(((Instruction)finallyInstructions.get(0)).opcode == 58) {
         finallyInstructions.remove(0);
      }

   }

   private static int FormatFastTry_RemoveJsrInstructionAndCompactStoreReturn(List<Instruction> instructions, LocalVariables localVariables, int finallyInstructitonsLineNumber) {
      int jumpOffset = -1;
      int index = instructions.size();

      while(index-- > 1) {
         if(((Instruction)instructions.get(index)).opcode == 168) {
            Jsr instruction = (Jsr)instructions.remove(index);
            jumpOffset = instruction.GetJumpOffset();
         }
      }

      index = instructions.size();

      while(index-- > 1) {
         Instruction var7 = (Instruction)instructions.get(index);
         if(var7.opcode == 273) {
            ReturnInstruction ri = (ReturnInstruction)var7;
            if(ri.lineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
               switch(ri.valueref.opcode) {
               case 21:
                  if(((Instruction)instructions.get(index - 1)).opcode == 54) {
                     index = CompactStoreReturn(instructions, localVariables, ri, index, finallyInstructitonsLineNumber);
                  }
                  break;
               case 25:
                  if(((Instruction)instructions.get(index - 1)).opcode == 58) {
                     index = CompactStoreReturn(instructions, localVariables, ri, index, finallyInstructitonsLineNumber);
                  }
                  break;
               case 268:
                  if(((Instruction)instructions.get(index - 1)).opcode == 269) {
                     index = CompactStoreReturn(instructions, localVariables, ri, index, finallyInstructitonsLineNumber);
                  }
               }
            }
         }
      }

      return jumpOffset;
   }

   private static void FormatFastTry_FormatNegativeJumpOffset(List<Instruction> instructions, int lastTryInstructionOffset, int returnOffset) {
      int i = instructions.size();

      while(i-- > 0) {
         Instruction instruction = (Instruction)instructions.get(i);
         switch(instruction.opcode) {
         case 167:
            Goto g = (Goto)instruction;
            int jumpOffset = g.GetJumpOffset();
            if(jumpOffset < lastTryInstructionOffset) {
               g.branch = returnOffset - g.offset;
            }
         }
      }

   }

   private static void FormatEclipse677Finally(FastCodeExceptionAnalyzer.FastCodeException fce, FastTry fastTry) {
      List finallyInstructions = fastTry.finallyInstructions;
      Instruction instruction = (Instruction)finallyInstructions.get(0);
      int index;
      switch(instruction.opcode) {
      case 58:
         int var12 = ((AStore)instruction).index;
         index = finallyInstructions.size();
         int var13 = -1;
         int afterAthrowOffset = -1;

         while(index-- > 0) {
            instruction = (Instruction)finallyInstructions.get(index);
            if(instruction.opcode == 191) {
               AThrow astore = (AThrow)instruction;
               if(astore.value.opcode == 25 && ((ALoad)astore.value).index == var12) {
                  var13 = instruction.offset;
                  finallyInstructions.remove(index);
                  break;
               }
            }

            afterAthrowOffset = instruction.offset;
            finallyInstructions.remove(index);
         }

         Instruction var14 = (Instruction)finallyInstructions.remove(0);
         List tryInstructions = fastTry.instructions;
         int lastIndex = tryInstructions.size() - 1;
         if(((Instruction)tryInstructions.get(lastIndex)).opcode == 167) {
            Goto finallyInstructitonsSize = (Goto)tryInstructions.remove(lastIndex);
            fce.tryToOffset = finallyInstructitonsSize.offset;
         }

         int var15 = finallyInstructions.size();
         FormatEclipse677Finally_RemoveFinallyInstructionsBeforeReturn(tryInstructions, var15);
         FormatEclipse677Finally_FormatIfInstruction(tryInstructions, var13, afterAthrowOffset, var14.offset);
         break;
      case 87:
         finallyInstructions.remove(0);
         List exceptionIndex = fastTry.instructions;
         index = exceptionIndex.size() - 1;
         if(((Instruction)exceptionIndex.get(index)).opcode == 167) {
            Goto athrowOffset = (Goto)exceptionIndex.remove(index);
            fce.tryToOffset = athrowOffset.offset;
         }
      }

   }

   private static void FormatEclipse677Finally_FormatIfInstruction(List<Instruction> instructions, int athrowOffset, int afterAthrowOffset, int afterTryOffset) {
      int i = instructions.size();

      while(i-- > 0) {
         Instruction instruction = (Instruction)instructions.get(i);
         switch(instruction.opcode) {
         case 260:
         case 262:
         case 284:
            IfInstruction ifi = (IfInstruction)instruction;
            int jumpOffset = ifi.GetJumpOffset();
            if(athrowOffset < jumpOffset && jumpOffset <= afterAthrowOffset) {
               ifi.branch = afterTryOffset - ifi.offset;
            }
         }
      }

   }

   private static void FormatEclipse677Finally_RemoveFinallyInstructionsBeforeReturn(List<Instruction> instructions, int finallyInstructitonsSize) {
      int i = instructions.size();

      while(true) {
         while(i-- > 0) {
            switch(((Instruction)instructions.get(i)).opcode) {
            case 177:
            case 273:
               for(int j = 0; j < finallyInstructitonsSize && i > 0; ++j) {
                  --i;
                  instructions.remove(i);
               }
            }
         }

         return;
      }
   }

   private static void FormatEclipse677CatchFinally(FastCodeExceptionAnalyzer.FastCodeException fce, FastTry fastTry, int returnOffset) {
      List finallyInstructions = fastTry.finallyInstructions;
      int exceptionIndex = ((AStore)finallyInstructions.get(0)).index;
      int index = finallyInstructions.size();
      int athrowOffset = -1;
      int afterAthrowOffset = -1;

      while(index-- > 0) {
         Instruction tryInstructions = (Instruction)finallyInstructions.get(index);
         if(tryInstructions.opcode == 191) {
            AThrow lastIndex = (AThrow)tryInstructions;
            if(lastIndex.value.opcode == 25 && ((ALoad)lastIndex.value).index == exceptionIndex) {
               athrowOffset = ((Instruction)finallyInstructions.remove(index)).offset;
               break;
            }
         }

         afterAthrowOffset = tryInstructions.offset;
         finallyInstructions.remove(index);
      }

      finallyInstructions.remove(0);
      List var21 = fastTry.instructions;
      int var22 = var21.size() - 1;
      Instruction lastTryInstruction = (Instruction)var21.get(var22);
      int lastTryInstructionOffset = lastTryInstruction.offset;
      if(lastTryInstruction.opcode == 167) {
         Goto finallyInstructitonsSize = (Goto)var21.remove(var22);
         fce.tryToOffset = finallyInstructitonsSize.offset;
      }

      int var23 = finallyInstructions.size();
      FormatEclipse677Finally_RemoveFinallyInstructionsBeforeReturn(var21, var23);
      FormatEclipse677Finally_FormatIfInstruction(var21, athrowOffset, afterAthrowOffset, lastTryInstructionOffset + 1);
      int i = fastTry.catches.size();

      while(i-- > 0) {
         FastTry.FastCatch fastCatch = (FastTry.FastCatch)fastTry.catches.get(i);
         List catchInstructions = fastCatch.instructions;
         index = catchInstructions.size();
         Instruction lastInstruction = (Instruction)catchInstructions.get(index - 1);
         int lastInstructionOffset = lastInstruction.offset;
         if(lastInstruction.opcode == 167) {
            --index;
            Goto g = (Goto)catchInstructions.remove(index);
            ((FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fce.catches.get(i)).toOffset = g.offset;
            int jumpOffset = g.GetJumpOffset();
            if(jumpOffset > fastTry.offset) {
               for(int j = var23; j > 0; --j) {
                  --index;
                  catchInstructions.remove(index);
               }
            }
         }

         FormatEclipse677Finally_RemoveFinallyInstructionsBeforeReturn(catchInstructions, var23);
         FormatEclipse677Finally_FormatIfInstruction(catchInstructions, athrowOffset, afterAthrowOffset, lastInstructionOffset + 1);
         FormatFastTry_FormatNegativeJumpOffset(catchInstructions, lastTryInstructionOffset, returnOffset);
         catchInstructions.remove(0);
      }

   }

   public static int ComputeTryToIndex(List<Instruction> instructions, FastCodeExceptionAnalyzer.FastCodeException fce, int lastIndex, int maxOffset) {
      int beforeMaxOffset = fce.tryFromOffset;

      int index;
      for(index = InstructionUtil.getIndexForOffset(instructions, fce.tryFromOffset); index <= lastIndex; ++index) {
         Instruction instruction = (Instruction)instructions.get(index);
         if(instruction.offset > maxOffset) {
            return index - 1;
         }

         int maxSitchOffset;
         switch(instruction.opcode) {
         case 167:
            int var12 = ((BranchInstruction)instruction).GetJumpOffset();
            if(var12 > instruction.offset) {
               if(var12 < maxOffset) {
                  if(beforeMaxOffset < var12) {
                     beforeMaxOffset = var12;
                  }
               } else if(instruction.offset >= beforeMaxOffset) {
                  return index;
               }
            } else if(var12 < fce.tryFromOffset && instruction.offset >= beforeMaxOffset) {
               return index;
            }
            break;
         case 170:
         case 171:
            Switch var11 = (Switch)instruction;
            maxSitchOffset = var11.defaultOffset;
            int i = var11.offsets.length;

            while(i-- > 0) {
               int offset = var11.offsets[i];
               if(maxSitchOffset < offset) {
                  maxSitchOffset = offset;
               }
            }

            maxSitchOffset += var11.offset;
            if(maxSitchOffset > instruction.offset && maxSitchOffset < maxOffset && beforeMaxOffset < maxSitchOffset) {
               beforeMaxOffset = maxSitchOffset;
            }
            break;
         case 177:
         case 191:
         case 273:
            if(instruction.offset >= beforeMaxOffset) {
               return index;
            }
            break;
         case 260:
         case 261:
         case 262:
            index = ComparisonInstructionAnalyzer.GetLastIndex(instructions, index);
            BranchInstruction s = (BranchInstruction)instructions.get(index);
            maxSitchOffset = s.GetJumpOffset();
            if(maxSitchOffset > instruction.offset && maxSitchOffset < maxOffset && beforeMaxOffset < maxSitchOffset) {
               beforeMaxOffset = maxSitchOffset;
            }
         }
      }

      return index;
   }

   public static class FastAggregatedCodeException extends CodeException {
      public int[] otherCatchTypes = null;
      public int nbrFinally;
      public boolean synchronizedFlag = false;

      public FastAggregatedCodeException(int index, int start_pc, int end_pc, int handler_pc, int catch_type) {
         super(index, start_pc, end_pc, handler_pc, catch_type);
         this.nbrFinally = catch_type == 0?1:0;
      }
   }

   public static class FastCodeException implements Comparable<FastCodeExceptionAnalyzer.FastCodeException> {
      public int tryFromOffset;
      public int tryToOffset;
      public List<FastCodeExceptionAnalyzer.FastCodeExceptionCatch> catches;
      public int finallyFromOffset;
      public int nbrFinally;
      public int maxOffset;
      public int afterOffset;
      public int type;
      public boolean synchronizedFlag;

      FastCodeException(int tryFromOffset, int tryToOffset, int maxOffset, boolean synchronizedFlag) {
         this.tryFromOffset = tryFromOffset;
         this.tryToOffset = tryToOffset;
         this.catches = new ArrayList();
         this.finallyFromOffset = -1;
         this.nbrFinally = 0;
         this.maxOffset = maxOffset;
         this.afterOffset = -1;
         this.type = 0;
         this.synchronizedFlag = synchronizedFlag;
      }

      public int compareTo(FastCodeExceptionAnalyzer.FastCodeException other) {
         return this.tryFromOffset != other.tryFromOffset?this.tryFromOffset - other.tryFromOffset:(this.maxOffset != other.maxOffset?other.maxOffset - this.maxOffset:other.tryToOffset - this.tryToOffset);
      }
   }

   public static class FastCodeExceptionCatch implements Comparable<FastCodeExceptionAnalyzer.FastCodeExceptionCatch> {
      public int type;
      public int[] otherTypes;
      public int fromOffset;
      public int toOffset;

      public FastCodeExceptionCatch(int type, int[] otherCatchTypes, int fromOffset) {
         this.type = type;
         this.otherTypes = otherCatchTypes;
         this.fromOffset = fromOffset;
         this.toOffset = -1;
      }

      public int compareTo(FastCodeExceptionAnalyzer.FastCodeExceptionCatch other) {
         return this.fromOffset - other.fromOffset;
      }
   }
}
