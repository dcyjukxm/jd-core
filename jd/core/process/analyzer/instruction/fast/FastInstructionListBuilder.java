package jd.core.process.analyzer.instruction.fast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.attribute.AttributeSignature;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.BIPush;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.ConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.ExceptionLoad;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.IInc;
import jd.core.model.instruction.bytecode.instruction.ILoad;
import jd.core.model.instruction.bytecode.instruction.IStore;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.model.instruction.bytecode.instruction.Jsr;
import jd.core.model.instruction.bytecode.instruction.Ldc;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.LookupSwitch;
import jd.core.model.instruction.bytecode.instruction.MonitorEnter;
import jd.core.model.instruction.bytecode.instruction.MonitorExit;
import jd.core.model.instruction.bytecode.instruction.Return;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.TableSwitch;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.instruction.fast.instruction.FastFor;
import jd.core.model.instruction.fast.instruction.FastForEach;
import jd.core.model.instruction.fast.instruction.FastInstruction;
import jd.core.model.instruction.fast.instruction.FastLabel;
import jd.core.model.instruction.fast.instruction.FastList;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.model.instruction.fast.instruction.FastSynchronized;
import jd.core.model.instruction.fast.instruction.FastTest2Lists;
import jd.core.model.instruction.fast.instruction.FastTestList;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.reconstructor.AssignmentOperatorReconstructor;
import jd.core.process.analyzer.classfile.visitor.SearchInstructionByOpcodeVisitor;
import jd.core.process.analyzer.instruction.bytecode.ComparisonInstructionAnalyzer;
import jd.core.process.analyzer.instruction.bytecode.reconstructor.AssertInstructionReconstructor;
import jd.core.process.analyzer.instruction.bytecode.util.ByteCodeUtil;
import jd.core.process.analyzer.instruction.fast.FastCodeExceptionAnalyzer;
import jd.core.process.analyzer.instruction.fast.SingleDupLoadAnalyzer;
import jd.core.process.analyzer.instruction.fast.UnexpectedElementException;
import jd.core.process.analyzer.instruction.fast.UnexpectedInstructionException;
import jd.core.process.analyzer.instruction.fast.reconstructor.DotClass118BReconstructor;
import jd.core.process.analyzer.instruction.fast.reconstructor.DotClassEclipseReconstructor;
import jd.core.process.analyzer.instruction.fast.reconstructor.EmptySynchronizedBlockReconstructor;
import jd.core.process.analyzer.instruction.fast.reconstructor.IfGotoToIfReconstructor;
import jd.core.process.analyzer.instruction.fast.reconstructor.InitArrayInstructionReconstructor;
import jd.core.process.analyzer.instruction.fast.reconstructor.RemoveDupConstantsAttributes;
import jd.core.process.analyzer.instruction.fast.reconstructor.TernaryOpInReturnReconstructor;
import jd.core.process.analyzer.instruction.fast.reconstructor.TernaryOpReconstructor;
import jd.core.process.analyzer.util.InstructionUtil;
import jd.core.util.IntSet;
import jd.core.util.SignatureUtil;

public class FastInstructionListBuilder {
   private static final boolean DECLARED = true;
   private static final boolean NOT_DECLARED = false;

   public static void Build(ReferenceMap referenceMap, ClassFile classFile, Method method, List<Instruction> list) throws Exception {
      if(list != null && !list.isEmpty()) {
         List lfce = FastCodeExceptionAnalyzer.AggregateCodeExceptions(method, list);
         LocalVariables localVariables = method.getLocalVariables();
         InitDelcarationFlags(localVariables);
         IntSet offsetLabelSet = new IntSet();
         int returnOffset = -1;
         if(list.size() > 0) {
            Instruction i = (Instruction)list.get(list.size() - 1);
            if(i.opcode == 177) {
               returnOffset = i.offset;
            }
         }

         if(lfce != null) {
            for(int var10 = lfce.size() - 1; var10 >= 0; --var10) {
               FastCodeExceptionAnalyzer.FastCodeException fce = (FastCodeExceptionAnalyzer.FastCodeException)lfce.get(var10);
               if(fce.synchronizedFlag) {
                  CreateSynchronizedBlock(referenceMap, classFile, list, localVariables, fce);
               } else {
                  CreateFastTry(referenceMap, classFile, method, list, localVariables, fce, returnOffset);
               }
            }
         }

         ExecuteReconstructors(referenceMap, classFile, list, localVariables);
         AnalyzeList(classFile, method, list, localVariables, offsetLabelSet, -1, -1, -1, -1, -1, -1, returnOffset);
         if(offsetLabelSet.size() > 0) {
            AddLabels(list, offsetLabelSet);
         }

      }
   }

   private static void InitDelcarationFlags(LocalVariables localVariables) {
      int nbrOfLocalVariables = localVariables.size();
      int indexOfFirstLocalVariable = localVariables.getIndexOfFirstLocalVariable();

      int i;
      for(i = 0; i < indexOfFirstLocalVariable && i < nbrOfLocalVariables; ++i) {
         localVariables.getLocalVariableAt(i).declarationFlag = true;
      }

      for(i = indexOfFirstLocalVariable; i < nbrOfLocalVariables; ++i) {
         LocalVariable lv = localVariables.getLocalVariableAt(i);
         lv.declarationFlag = lv.exceptionOrReturnAddress;
      }

   }

   private static void CreateSynchronizedBlock(ReferenceMap referenceMap, ClassFile classFile, List<Instruction> list, LocalVariables localVariables, FastCodeExceptionAnalyzer.FastCodeException fce) {
      int index = InstructionUtil.getIndexForOffset(list, fce.tryFromOffset);
      Instruction instruction = (Instruction)list.get(index);
      boolean synchronizedBlockJumpOffset = true;
      int lastOffset;
      int i;
      int finallyFromOffset;
      ArrayList exceptionLoadIndex;
      int instructions;
      Instruction lastInstruction;
      int lineNumber;
      int lenght;
      int var20;
      if(fce.type == 2) {
         lastOffset = index;
         index = InstructionUtil.getIndexForOffset(list, fce.finallyFromOffset);
         i = ((Instruction)list.get(index + 2)).offset;

         while(index-- > lastOffset) {
            instruction = (Instruction)list.get(index);
            if(instruction.opcode == 168) {
               finallyFromOffset = ((Jsr)instruction).GetJumpOffset();
               list.remove(index);
               if(finallyFromOffset == i) {
                  break;
               }
            }
         }

         finallyFromOffset = fce.finallyFromOffset;
         index = InstructionUtil.getIndexForOffset(list, fce.afterOffset);
         if(index == -1) {
            index = list.size() - 1;

            while(((Instruction)list.get(index)).offset >= finallyFromOffset) {
               list.remove(index--);
            }
         } else if(index > 0) {
            --index;

            while(((Instruction)list.get(index)).offset >= finallyFromOffset) {
               list.remove(index--);
            }
         }

         exceptionLoadIndex = new ArrayList();
         if(index > 0) {
            instructions = fce.tryFromOffset;

            while(((Instruction)list.get(index)).offset >= instructions) {
               exceptionLoadIndex.add((Instruction)list.remove(index--));
            }
         }

         if(exceptionLoadIndex.size() > 0) {
            lastInstruction = (Instruction)exceptionLoadIndex.get(0);
            instructions = lastInstruction.offset;
         } else {
            instructions = -1;
         }

         var20 = SearchMinusJumpOffset(exceptionLoadIndex, 0, exceptionLoadIndex.size(), fce.tryFromOffset, fce.afterOffset);
         Collections.reverse(exceptionLoadIndex);
         ExecuteReconstructors(referenceMap, classFile, exceptionLoadIndex, localVariables);
         MonitorEnter var34 = (MonitorEnter)list.remove(index--);
         lineNumber = var34.lineNumber;
         if(var34.objectref.opcode != 25) {
            throw new UnexpectedInstructionException();
         }

         lenght = ((IndexInstruction)var34.objectref).index;
         localVariables.removeLocalVariableWithIndexAndOffset(lenght, var34.offset);
         AStore monitorLocalVariableIndex = (AStore)list.get(index);
         Instruction branch = monitorLocalVariableIndex.valueref;
         int fastSynchronized = 1;
         if(instructions != -1 && var20 != -1) {
            fastSynchronized = var20 - instructions;
         }

         FastSynchronized aload = new FastSynchronized(319, instructions, lineNumber, fastSynchronized, exceptionLoadIndex);
         aload.monitor = branch;
         list.set(index, aload);
      } else {
         AStore var28;
         Instruction var32;
         int var35;
         if(fce.type == 6) {
            ArrayList var21 = new ArrayList();
            instruction = (Instruction)list.remove(index);
            i = instruction.offset;
            var21.add(instruction);
            var20 = SearchMinusJumpOffset(var21, 0, var21.size(), fce.tryFromOffset, fce.afterOffset);
            MonitorEnter var26 = (MonitorEnter)list.remove(index - 1);
            var28 = (AStore)list.get(index - 2);
            var32 = var28.valueref;
            var35 = var28.index;
            localVariables.removeLocalVariableWithIndexAndOffset(var35, var26.offset);
            lineNumber = 1;
            if(var20 != -1) {
               lineNumber = var20 - i;
            }

            FastSynchronized var40 = new FastSynchronized(319, i, var26.lineNumber, lineNumber, var21);
            var40.monitor = var32;
            list.set(index - 2, var40);
         } else {
            Instruction var25;
            if(instruction.opcode == 195) {
               --index;
               FastSynchronized var37;
               if(((Instruction)list.get(index)).opcode == 194) {
                  MonitorEnter var23 = (MonitorEnter)list.remove(index);
                  Instruction var22;
                  if(var23.objectref.opcode == 265) {
                     AssignmentInstruction var27 = (AssignmentInstruction)var23.objectref;
                     var28 = (AStore)var27.value1;
                     var22 = var27.value2;
                     localVariables.removeLocalVariableWithIndexAndOffset(var28.index, var28.offset);
                     list.remove(index);
                  } else {
                     list.remove(index);
                     --index;
                     AStore var29 = (AStore)list.remove(index);
                     var22 = var29.valueref;
                     localVariables.removeLocalVariableWithIndexAndOffset(var29.index, var29.offset);
                  }

                  ArrayList var30 = new ArrayList();
                  Instruction var31 = (Instruction)list.remove(index);
                  if(var31.opcode != 167 || ((Goto)var31).GetJumpOffset() != fce.afterOffset) {
                     var30.add(var31);
                  }

                  if(((Instruction)list.get(index)).opcode == 58) {
                     list.remove(index);
                  }

                  var32 = (Instruction)list.remove(index);
                  ExecuteReconstructors(referenceMap, classFile, var30, localVariables);
                  var37 = new FastSynchronized(319, var32.offset, instruction.lineNumber, 1, var30);
                  var37.monitor = var22;
                  list.set(index, var37);
               } else {
                  list.remove(index);
                  list.remove(index);
                  list.remove(index);
                  instruction = (Instruction)list.remove(index);
                  MonitorEnter var24;
                  switch(instruction.opcode) {
                  case 58:
                     var24 = (MonitorEnter)list.remove(index);
                     var28 = (AStore)instruction;
                     finallyFromOffset = var28.index;
                     var25 = var28.valueref;
                     break;
                  case 194:
                     var24 = (MonitorEnter)instruction;
                     AssignmentInstruction var36 = (AssignmentInstruction)var24.objectref;
                     var28 = (AStore)var36.value1;
                     finallyFromOffset = var28.index;
                     var25 = var36.value2;
                     break;
                  default:
                     throw new UnexpectedInstructionException();
                  }

                  localVariables.removeLocalVariableWithIndexAndOffset(finallyFromOffset, var24.offset);
                  exceptionLoadIndex = new ArrayList();

                  while(true) {
                     instruction = (Instruction)list.get(index);
                     if(instruction.opcode == 195) {
                        MonitorExit var38 = (MonitorExit)instruction;
                        if(var38.objectref.opcode == 25) {
                           LoadInstruction var39 = (LoadInstruction)var38.objectref;
                           if(var39.index == finallyFromOffset) {
                              if(index + 1 < list.size() && ((Instruction)list.get(index + 1)).opcode == 273) {
                                 var32 = (Instruction)list.get(index);
                                 lastInstruction = ((ReturnInstruction)list.get(index + 1)).valueref;
                                 if(var32.offset > lastInstruction.offset) {
                                    exceptionLoadIndex.add((Instruction)list.remove(index + 1));
                                 }
                              }

                              ExecuteReconstructors(referenceMap, classFile, exceptionLoadIndex, localVariables);
                              var20 = SearchMinusJumpOffset(exceptionLoadIndex, 0, exceptionLoadIndex.size(), fce.tryFromOffset, fce.afterOffset);
                              instructions = 1;
                              if(var20 != -1) {
                                 instructions = var20 - instruction.offset;
                              }

                              var37 = new FastSynchronized(319, instruction.offset, var24.lineNumber, instructions, exceptionLoadIndex);
                              var37.monitor = var25;
                              list.set(index, var37);
                              break;
                           }
                        }
                     }

                     exceptionLoadIndex.add((Instruction)list.remove(index));
                  }
               }
            } else {
               if(fce.afterOffset > ((Instruction)list.get(list.size() - 1)).offset) {
                  index = list.size();
               } else {
                  index = InstructionUtil.getIndexForOffset(list, fce.afterOffset);
               }

               --index;
               lastOffset = ((Instruction)list.get(index)).offset;
               var25 = null;

               for(finallyFromOffset = fce.finallyFromOffset; ((Instruction)list.get(index)).offset >= finallyFromOffset; var25 = (Instruction)list.remove(index--)) {
                  ;
               }

               int var33 = -1;
               if(var25 != null && var25.opcode == 58) {
                  AStore var41 = (AStore)var25;
                  if(var41.valueref.opcode == 270) {
                     var33 = var41.index;
                  }
               }

               ArrayList var42 = new ArrayList();
               var25 = null;
               if(index > 0) {
                  var35 = fce.tryFromOffset;
                  var25 = (Instruction)list.get(index);
                  if(var25.offset >= var35) {
                     var42.add(var25);

                     while(index-- > 0) {
                        var25 = (Instruction)list.get(index);
                        if(var25.offset < var35) {
                           break;
                        }

                        list.remove(index + 1);
                        var42.add(var25);
                     }

                     list.set(index + 1, (Object)null);
                  }
               }

               lastInstruction = (Instruction)var42.get(0);
               var20 = SearchMinusJumpOffset(var42, 0, var42.size(), fce.tryFromOffset, fce.afterOffset);
               Collections.reverse(var42);
               if(var25 == null) {
                  lineNumber = Instruction.UNKNOWN_LINE_NUMBER;
               } else {
                  lineNumber = var25.lineNumber;
               }

               lenght = var42.size();
               int var43 = GetMonitorLocalVariableIndex(list, index);
               int var44;
               if(lenght > 0) {
                  lastInstruction = (Instruction)var42.get(lenght - 1);
                  switch(lastInstruction.opcode) {
                  case 167:
                     --lenght;
                     var42.remove(lenght);
                     if(lenght > 0) {
                        lastInstruction = (Instruction)var42.get(lenght - 1);
                     }
                     break;
                  case 273:
                     --lenght;
                     if(lenght > 0) {
                        lastInstruction = (Instruction)var42.get(lenght - 1);
                     }
                  }

                  RemoveAllMonitorExitInstructions(var42, lenght, var43);
                  var44 = list.size() - 1;
                  var25 = (Instruction)list.get(var44);
                  if(var25 != null && var25.opcode == 191) {
                     AThrow var46 = (AThrow)list.get(var44);
                     switch(var46.value.opcode) {
                     case 25:
                        ALoad var49 = (ALoad)var46.value;
                        if(var49.index == var33) {
                           list.remove(var44);
                        }
                        break;
                     case 270:
                        ExceptionLoad var48 = (ExceptionLoad)var46.value;
                        if(var48.exceptionNameIndex == 0) {
                           list.remove(var44);
                        }
                     }
                  }
               }

               if(var43 != -1) {
                  MonitorEnter var45 = (MonitorEnter)list.get(index);
                  localVariables.removeLocalVariableWithIndexAndOffset(var43, var45.offset);
               }

               var44 = 1;
               if(var20 != -1) {
                  var44 = var20 - lastOffset;
               }

               FastSynchronized var47 = new FastSynchronized(319, lastOffset, lineNumber, var44, var42);
               ExecuteReconstructors(referenceMap, classFile, var42, localVariables);
               list.set(index + 1, var47);
               var47.monitor = FormatAndExtractMonitor(list, index);
            }
         }
      }

   }

   private static Instruction FormatAndExtractMonitor(List<Instruction> list, int index) {
      MonitorEnter menter = (MonitorEnter)list.remove(index--);
      switch(menter.objectref.opcode) {
      case 25:
         AStore astore = (AStore)list.remove(index);
         return astore.valueref;
      case 263:
         list.remove(index--);
         DupStore dupstore = (DupStore)list.remove(index);
         return dupstore.objectref;
      case 265:
         return ((AssignmentInstruction)menter.objectref).value2;
      default:
         return null;
      }
   }

   private static void RemoveAllMonitorExitInstructions(List<Instruction> instructions, int lenght, int monitorLocalVariableIndex) {
      int index = lenght;

      while(true) {
         while(index-- > 0) {
            Instruction instruction = (Instruction)instructions.get(index);
            int i;
            if(instruction.opcode == 195) {
               MonitorExit var9 = (MonitorExit)instruction;
               if(var9.objectref.opcode == 25) {
                  i = ((ALoad)var9.objectref).index;
                  if(i == monitorLocalVariableIndex) {
                     instructions.remove(index);
                  }
               }
            } else if(instruction.opcode != 318) {
               if(instruction.opcode == 319) {
                  FastSynchronized var8 = (FastSynchronized)instruction;
                  RemoveAllMonitorExitInstructions(var8.instructions, var8.instructions.size(), monitorLocalVariableIndex);
               }
            } else {
               FastTry fsy = (FastTry)instruction;
               RemoveAllMonitorExitInstructions(fsy.instructions, fsy.instructions.size(), monitorLocalVariableIndex);
               i = fsy.catches.size();

               while(i-- > 0) {
                  FastTry.FastCatch fc = (FastTry.FastCatch)fsy.catches.get(i);
                  RemoveAllMonitorExitInstructions(fc.instructions, fc.instructions.size(), monitorLocalVariableIndex);
               }

               if(fsy.finallyInstructions != null) {
                  RemoveAllMonitorExitInstructions(fsy.finallyInstructions, fsy.finallyInstructions.size(), monitorLocalVariableIndex);
               }
            }
         }

         return;
      }
   }

   private static int GetMonitorLocalVariableIndex(List<Instruction> list, int index) {
      MonitorEnter menter = (MonitorEnter)list.get(index);
      switch(menter.objectref.opcode) {
      case 25:
         return ((ALoad)menter.objectref).index;
      case 263:
         return ((AStore)list.get(index - 1)).index;
      case 265:
         Instruction i = ((AssignmentInstruction)menter.objectref).value1;
         if(i.opcode == 25) {
            return ((ALoad)i).index;
         }
      default:
         return -1;
      }
   }

   private static void CreateFastTry(ReferenceMap referenceMap, ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, FastCodeExceptionAnalyzer.FastCodeException fce, int returnOffset) throws Exception {
      int afterListOffset = fce.afterOffset;
      int tryJumpOffset = -1;
      int lastIndex = list.size() - 1;
      int index;
      if(afterListOffset != -1 && afterListOffset <= ((Instruction)list.get(lastIndex)).offset) {
         index = InstructionUtil.getIndexForOffset(list, afterListOffset);

         assert index != -1;

         --index;
      } else {
         index = lastIndex;
      }

      int lastOffset = ((Instruction)list.get(index)).offset;
      ArrayList finallyInstructions = null;
      int tryInstructions;
      int tryFromOffset;
      if(fce.finallyFromOffset > 0) {
         int catches = fce.finallyFromOffset;
         finallyInstructions = new ArrayList();

         while(((Instruction)list.get(index)).offset >= catches) {
            finallyInstructions.add((Instruction)list.remove(index--));
         }

         if(finallyInstructions.size() == 0) {
            throw new RuntimeException("Unexpected structure for finally block");
         }

         Collections.reverse(finallyInstructions);
         tryInstructions = ((Instruction)finallyInstructions.get(0)).offset;
         tryFromOffset = SearchMinusJumpOffset(finallyInstructions, 0, finallyInstructions.size(), tryInstructions, afterListOffset);
         afterListOffset = tryInstructions;
         if(tryFromOffset != -1 && tryInstructions > tryFromOffset) {
            afterListOffset = tryFromOffset;
         }
      }

      ArrayList var25 = null;
      int lineNumber;
      int length;
      if(fce.catches != null) {
         tryInstructions = fce.catches.size();
         var25 = new ArrayList(tryInstructions);

         while(tryInstructions-- > 0) {
            FastCodeExceptionAnalyzer.FastCodeExceptionCatch var27 = (FastCodeExceptionAnalyzer.FastCodeExceptionCatch)fce.catches.get(tryInstructions);
            var27.toOffset = afterListOffset;
            int i = var27.fromOffset;

            ArrayList tryJumpOffsetTmp;
            for(tryJumpOffsetTmp = new ArrayList(); ((Instruction)list.get(index)).offset >= i; --index) {
               tryJumpOffsetTmp.add((Instruction)list.remove(index));
               if(index == 0) {
                  break;
               }
            }

            lineNumber = tryJumpOffsetTmp.size();
            if(lineNumber <= 0) {
               throw new RuntimeException("Empty catch block");
            }

            Instruction fastTry = (Instruction)tryJumpOffsetTmp.get(0);
            length = SearchMinusJumpOffset(tryJumpOffsetTmp, 0, lineNumber, fce.tryFromOffset, fce.afterOffset);
            if(length != -1) {
               if(tryJumpOffset == -1) {
                  tryJumpOffset = length;
               } else if(tryJumpOffset > length) {
                  tryJumpOffset = length;
               }
            }

            Collections.reverse(tryJumpOffsetTmp);
            ExceptionLoad j = SearchExceptionLoadInstruction(tryJumpOffsetTmp);
            if(j == null) {
               throw new UnexpectedInstructionException();
            }

            int fc = fastTry.offset;
            var25.add(0, new FastTry.FastCatch(fc, j.offset, var27.type, var27.otherTypes, j.index, tryJumpOffsetTmp));
            int catchInstructions = ((Instruction)tryJumpOffsetTmp.get(0)).offset;
            int minimalJumpOffset = SearchMinusJumpOffset(tryJumpOffsetTmp, 0, tryJumpOffsetTmp.size(), catchInstructions, fc);
            if(afterListOffset > catchInstructions) {
               afterListOffset = catchInstructions;
            }

            if(minimalJumpOffset != -1 && afterListOffset > minimalJumpOffset) {
               afterListOffset = minimalJumpOffset;
            }
         }
      }

      ArrayList var26 = new ArrayList();
      if(fce.tryToOffset < afterListOffset) {
         index = FastCodeExceptionAnalyzer.ComputeTryToIndex(list, fce, index, afterListOffset);
      }

      tryFromOffset = fce.tryFromOffset;
      Instruction var28 = (Instruction)list.get(index);
      if(var28.offset >= tryFromOffset) {
         var26.add(var28);

         while(index-- > 0) {
            var28 = (Instruction)list.get(index);
            if(var28.offset < tryFromOffset) {
               break;
            }

            list.remove(index + 1);
            var26.add(var28);
         }

         list.set(index + 1, (Object)null);
      }

      int var29 = SearchMinusJumpOffset(var26, 0, var26.size(), fce.tryFromOffset, fce.tryToOffset);
      if(var29 != -1) {
         if(tryJumpOffset == -1) {
            tryJumpOffset = var29;
         } else if(tryJumpOffset > var29) {
            tryJumpOffset = var29;
         }
      }

      Collections.reverse(var26);
      lineNumber = ((Instruction)var26.get(0)).lineNumber;
      if(tryJumpOffset == -1) {
         tryJumpOffset = lastOffset + 1;
      }

      FastTry var30 = new FastTry(318, lastOffset, lineNumber, tryJumpOffset - lastOffset, var26, var25, finallyInstructions);
      FastCodeExceptionAnalyzer.FormatFastTry(localVariables, fce, var30, returnOffset);
      ExecuteReconstructors(referenceMap, classFile, var26, localVariables);
      if(var25 != null) {
         length = var25.size();

         for(int var31 = 0; var31 < length; ++var31) {
            FastTry.FastCatch var32 = (FastTry.FastCatch)var25.get(var31);
            List var33 = var32.instructions;
            ExecuteReconstructors(referenceMap, classFile, var33, localVariables);
         }
      }

      if(finallyInstructions != null) {
         ExecuteReconstructors(referenceMap, classFile, finallyInstructions, localVariables);
      }

      list.set(index + 1, var30);
   }

   private static ExceptionLoad SearchExceptionLoadInstruction(List<Instruction> instructions) throws Exception {
      int length = instructions.size();

      for(int i = 0; i < length; ++i) {
         Instruction instruction = SearchInstructionByOpcodeVisitor.visit((Instruction)((Instruction)instructions.get(i)), 270);
         if(instruction != null) {
            return (ExceptionLoad)instruction;
         }
      }

      return null;
   }

   private static void ExecuteReconstructors(ReferenceMap referenceMap, ClassFile classFile, List<Instruction> list, LocalVariables localVariables) {
      EmptySynchronizedBlockReconstructor.Reconstruct(localVariables, list);
      DotClass118BReconstructor.Reconstruct(referenceMap, classFile, list);
      DotClassEclipseReconstructor.Reconstruct(referenceMap, classFile, list);
      IfGotoToIfReconstructor.Reconstruct(list);
      ComparisonInstructionAnalyzer.Aggregate(list);
      AssertInstructionReconstructor.Reconstruct(classFile, list);
      TernaryOpReconstructor.Reconstruct(list);
      InitArrayInstructionReconstructor.Reconstruct(list);
      AssignmentOperatorReconstructor.Reconstruct(list);
      RemoveDupConstantsAttributes.Reconstruct(list);
   }

   private static void RemoveNoJumpGotoInstruction(List<Instruction> list, int afterListOffset) {
      int index = list.size();
      if(index != 0) {
         --index;
         Instruction instruction = (Instruction)list.get(index);
         int lastInstructionOffset = instruction.offset;
         int branch;
         if(instruction.opcode == 167) {
            branch = ((Goto)instruction).branch;
            if(branch >= 0 && instruction.offset + branch <= afterListOffset) {
               list.remove(index);
            }
         }

         for(; index-- > 0; lastInstructionOffset = instruction.offset) {
            instruction = (Instruction)list.get(index);
            if(instruction.opcode == 167) {
               branch = ((Goto)instruction).branch;
               if(branch >= 0 && instruction.offset + branch <= lastInstructionOffset) {
                  list.remove(index);
               }
            }
         }

      }
   }

   private static void RemoveSyntheticReturn(List<Instruction> list, int afterListOffset, int returnOffset) {
      if(afterListOffset == returnOffset) {
         int index = list.size();
         if(index == 1) {
            --index;
            RemoveSyntheticReturn(list, index);
         } else if(index-- > 1 && ((Instruction)list.get(index)).lineNumber < ((Instruction)list.get(index - 1)).lineNumber) {
            RemoveSyntheticReturn(list, index);
         }
      }

   }

   private static void RemoveSyntheticReturn(List<Instruction> list, int index) {
      switch(((Instruction)list.get(index)).opcode) {
      case 177:
         list.remove(index);
         break;
      case 320:
         FastLabel fl = (FastLabel)list.get(index);
         if(fl.instruction.opcode == 177) {
            fl.instruction = null;
         }
      }

   }

   private static void AddCastInstructionOnReturn(ClassFile classFile, Method method, List<Instruction> list) {
      ConstantPool constants = classFile.getConstantPool();
      LocalVariables localVariables = method.getLocalVariables();
      AttributeSignature as = method.getAttributeSignature();
      int signatureIndex = as == null?method.descriptor_index:as.signature_index;
      String signature = constants.getConstantUtf8(signatureIndex);
      String methodReturnedSignature = SignatureUtil.GetMethodReturnedSignature(signature);
      int index = list.size();

      while(index-- > 0) {
         Instruction instruction = (Instruction)list.get(index);
         if(instruction.opcode == 273) {
            ReturnInstruction ri = (ReturnInstruction)instruction;
            String returnedSignature = ri.valueref.getReturnedSignature(constants, localVariables);
            if(returnedSignature != null && returnedSignature.equals("Ljava/lang/Object;") && !methodReturnedSignature.equals("Ljava/lang/Object;")) {
               signatureIndex = constants.addConstantUtf8(methodReturnedSignature);
               if(ri.valueref.opcode == 192) {
                  ((CheckCast)ri.valueref).index = signatureIndex;
               } else {
                  ri.valueref = new CheckCast(192, ri.valueref.offset, ri.valueref.lineNumber, signatureIndex, ri.valueref);
               }
            }
         }
      }

   }

   private static void AnalyzeList(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeLoopEntryOffset, int loopEntryOffset, int afterBodyLoopOffset, int beforeListOffset, int afterListOffset, int breakOffset, int returnOffset) {
      CreateLoops(classFile, method, list, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, beforeListOffset, afterListOffset, returnOffset);
      CreateSwitch(classFile, method, list, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, afterListOffset, returnOffset);
      AnalyzeTryAndSynchronized(classFile, method, list, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, beforeListOffset, afterListOffset, breakOffset, returnOffset);
      TernaryOpInReturnReconstructor.Reconstruct(list);
      CreateIfElse(classFile, method, list, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, afterListOffset, breakOffset, returnOffset);
      RemoveNopGoto(list);
      AddDeclarations(list, localVariables, beforeListOffset);
      RemoveNoJumpGotoInstruction(list, afterListOffset);
      CreateBreakAndContinue(method, list, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, afterListOffset, breakOffset, returnOffset);
      SingleDupLoadAnalyzer.Cleanup(list);
      RemoveSyntheticReturn(list, afterListOffset, returnOffset);
      AddCastInstructionOnReturn(classFile, method, list);
   }

   private static void AnalyzeTryAndSynchronized(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeLoopEntryOffset, int loopEntryOffset, int afterBodyLoopOffset, int beforeListOffset, int afterListOffset, int breakOffset, int returnOffset) {
      Instruction instruction;
      for(int index = list.size(); index-- > 0; afterListOffset = instruction.offset) {
         instruction = (Instruction)list.get(index);
         int tmpBeforeListOffset;
         switch(instruction.opcode) {
         case 194:
         case 195:
            list.remove(index);
            break;
         case 318:
            FastTry var18 = (FastTry)instruction;
            tmpBeforeListOffset = index > 0?((Instruction)list.get(index - 1)).offset:beforeListOffset;
            AnalyzeList(classFile, method, var18.instructions, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, tmpBeforeListOffset, afterListOffset, breakOffset, returnOffset);
            int length = var18.catches.size();

            for(int i = 0; i < length; ++i) {
               AnalyzeList(classFile, method, ((FastTry.FastCatch)var18.catches.get(i)).instructions, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, tmpBeforeListOffset, afterListOffset, breakOffset, returnOffset);
            }

            if(var18.finallyInstructions != null) {
               AnalyzeList(classFile, method, var18.finallyInstructions, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, tmpBeforeListOffset, afterListOffset, breakOffset, returnOffset);
            }
            break;
         case 319:
            FastSynchronized fs = (FastSynchronized)instruction;
            tmpBeforeListOffset = index > 0?((Instruction)list.get(index - 1)).offset:beforeListOffset;
            AnalyzeList(classFile, method, fs.instructions, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, tmpBeforeListOffset, afterListOffset, breakOffset, returnOffset);
         }
      }

   }

   private static void RemoveNopGoto(List<Instruction> list) {
      int length = list.size();
      if(length > 1) {
         int nextOffset = ((Instruction)list.get(length - 1)).offset;

         for(int index = length - 2; index >= 0; --index) {
            Instruction instruction = (Instruction)list.get(index);
            if(instruction.opcode == 167) {
               Goto gi = (Goto)instruction;
               if(gi.branch >= 0 && gi.GetJumpOffset() <= nextOffset) {
                  list.remove(index);
               }
            }

            nextOffset = instruction.offset;
         }
      }

   }

   private static void AddDeclarations(List<Instruction> list, LocalVariables localVariables, int beforeListOffset) {
      int length = list.size();
      if(length > 0) {
         int lastOffset = ((Instruction)list.get(length - 1)).offset;

         LocalVariable lv;
         int lvLength;
         for(lvLength = 0; lvLength < length; ++lvLength) {
            Instruction i = (Instruction)list.get(lvLength);
            StoreInstruction si;
            switch(i.opcode) {
            case 54:
            case 58:
            case 269:
               si = (StoreInstruction)i;
               lv = localVariables.getLocalVariableWithIndexAndOffset(si.index, si.offset);
               if(lv != null && !lv.declarationFlag && beforeListOffset < lv.start_pc && lv.start_pc + lv.length - 1 <= lastOffset) {
                  list.set(lvLength, new FastDeclaration(317, si.offset, si.lineNumber, si.index, si));
                  lv.declarationFlag = true;
                  UpdateNewAndInitArrayInstruction(si);
               }
               break;
            case 304:
               FastFor indexForNewDeclaration = (FastFor)i;
               if(indexForNewDeclaration.init != null) {
                  switch(indexForNewDeclaration.init.opcode) {
                  case 54:
                  case 58:
                  case 269:
                     si = (StoreInstruction)indexForNewDeclaration.init;
                     lv = localVariables.getLocalVariableWithIndexAndOffset(si.index, si.offset);
                     if(lv != null && !lv.declarationFlag && beforeListOffset < lv.start_pc && lv.start_pc + lv.length - 1 <= lastOffset) {
                        indexForNewDeclaration.init = new FastDeclaration(317, si.offset, si.lineNumber, si.index, si);
                        lv.declarationFlag = true;
                        UpdateNewAndInitArrayInstruction(si);
                     }
                  }
               }
            }
         }

         lvLength = localVariables.size();

         for(int var10 = 0; var10 < lvLength; ++var10) {
            lv = localVariables.getLocalVariableAt(var10);
            if(!lv.declarationFlag && beforeListOffset < lv.start_pc && lv.start_pc + lv.length - 1 <= lastOffset) {
               int var11 = InstructionUtil.getIndexForOffset(list, lv.start_pc);
               if(var11 == -1) {
                  var11 = 0;
               }

               list.add(var11, new FastDeclaration(317, lv.start_pc, Instruction.UNKNOWN_LINE_NUMBER, lv.index, (Instruction)null));
               lv.declarationFlag = true;
            }
         }
      }

   }

   private static void UpdateNewAndInitArrayInstruction(Instruction instruction) {
      switch(instruction.opcode) {
      case 58:
         Instruction valueref = ((StoreInstruction)instruction).valueref;
         if(valueref.opcode == 283) {
            valueref.opcode = 282;
         }
      default:
      }
   }

   private static void CreateBreakAndContinue(Method method, List<Instruction> list, IntSet offsetLabelSet, int beforeLoopEntryOffset, int loopEntryOffset, int afterBodyLoopOffset, int afterListOffset, int breakOffset, int returnOffset) {
      int length = list.size();

      for(int index = 0; index < length; ++index) {
         Instruction instruction = (Instruction)list.get(index);
         int jumpOffset;
         switch(instruction.opcode) {
         case 167:
            Goto var19 = (Goto)instruction;
            jumpOffset = var19.GetJumpOffset();
            int var21 = var19.lineNumber;
            if(index == 0 || ((Instruction)list.get(index - 1)).lineNumber == var21) {
               var21 = Instruction.UNKNOWN_LINE_NUMBER;
            }

            if(beforeLoopEntryOffset < jumpOffset && jumpOffset <= loopEntryOffset) {
               if(afterListOffset == afterBodyLoopOffset && index + 1 == length) {
                  list.remove(index);
               } else {
                  list.set(index, new FastInstruction(311, var19.offset, var21, (Instruction)null));
               }
            } else if(ByteCodeUtil.JumpTo(method.getCode(), breakOffset, jumpOffset)) {
               list.set(index, new FastInstruction(312, var19.offset, var21, (Instruction)null));
            } else if(ByteCodeUtil.JumpTo(method.getCode(), jumpOffset, returnOffset)) {
               list.set(index, new Return(177, var19.offset, var21));
            } else {
               byte[] var22 = method.getCode();
               if(var22.length == jumpOffset + 2) {
                  LoadInstruction var23 = DuplicateLoadInstruction(var22[jumpOffset] & 255, var19.offset, var21);
                  if(var23 != null) {
                     ReturnInstruction var24 = DuplacateReturnInstruction(var22[jumpOffset + 1] & 255, var19.offset, var21, var23);
                     if(var24 != null) {
                        if(index > 0) {
                           instruction = (Instruction)list.get(index - 1);
                           if(var23.lineNumber == instruction.lineNumber && 54 <= instruction.opcode && instruction.opcode <= 78 && var23.index == ((StoreInstruction)instruction).index) {
                              StoreInstruction si = (StoreInstruction)instruction;
                              var24.valueref = si.valueref;
                              --index;
                              list.remove(index);
                              --length;
                           }
                        }

                        list.set(index, var24);
                        break;
                     }
                  }
               }

               offsetLabelSet.add(jumpOffset);
               list.set(index, new FastInstruction(313, var19.offset, var21, var19));
            }
            break;
         case 260:
         case 261:
         case 262:
         case 284:
            BranchInstruction g = (BranchInstruction)instruction;
            jumpOffset = g.GetJumpOffset();
            if(beforeLoopEntryOffset < jumpOffset && jumpOffset <= loopEntryOffset) {
               list.set(index, new FastInstruction(308, g.offset, g.lineNumber, g));
            } else if(ByteCodeUtil.JumpTo(method.getCode(), breakOffset, jumpOffset)) {
               list.set(index, new FastInstruction(309, g.offset, g.lineNumber, g));
            } else if(ByteCodeUtil.JumpTo(method.getCode(), jumpOffset, returnOffset)) {
               ArrayList var20 = new ArrayList(1);
               var20.add(new Return(177, g.offset, Instruction.UNKNOWN_LINE_NUMBER));
               list.set(index, new FastTestList(306, g.offset, g.lineNumber, jumpOffset - g.offset, g, var20));
            } else {
               byte[] lineNumber = method.getCode();
               if(lineNumber.length == jumpOffset + 2) {
                  LoadInstruction code = DuplicateLoadInstruction(lineNumber[jumpOffset] & 255, g.offset, Instruction.UNKNOWN_LINE_NUMBER);
                  if(code != null) {
                     ReturnInstruction load = DuplacateReturnInstruction(lineNumber[jumpOffset + 1] & 255, g.offset, Instruction.UNKNOWN_LINE_NUMBER, code);
                     if(load != null) {
                        ArrayList ri = new ArrayList(1);
                        ri.add(load);
                        list.set(index, new FastTestList(306, g.offset, g.lineNumber, jumpOffset - g.offset, g, ri));
                        continue;
                     }
                  }
               }

               offsetLabelSet.add(jumpOffset);
               list.set(index, new FastInstruction(310, g.offset, g.lineNumber, g));
            }
         }
      }

   }

   private static LoadInstruction DuplicateLoadInstruction(int opcode, int offset, int lineNumber) {
      switch(opcode) {
      case 21:
         return new ILoad(21, offset, lineNumber, 0);
      case 22:
         return new LoadInstruction(268, offset, lineNumber, 0, "J");
      case 23:
         return new LoadInstruction(268, offset, lineNumber, 0, "F");
      case 24:
         return new LoadInstruction(268, offset, lineNumber, 0, "D");
      case 25:
         return new ALoad(25, offset, lineNumber, 0);
      case 26:
      case 27:
      case 28:
      case 29:
         return new ILoad(21, offset, lineNumber, opcode - 26);
      case 30:
      case 31:
      case 32:
      case 33:
         return new LoadInstruction(268, offset, lineNumber, opcode - 30, "J");
      case 34:
      case 35:
      case 36:
      case 37:
         return new LoadInstruction(268, offset, lineNumber, opcode - 34, "F");
      case 38:
      case 39:
      case 40:
      case 41:
         return new LoadInstruction(268, offset, lineNumber, opcode - 38, "D");
      case 42:
      case 43:
      case 44:
      case 45:
         return new ALoad(25, offset, lineNumber, opcode - 42);
      default:
         return null;
      }
   }

   private static ReturnInstruction DuplacateReturnInstruction(int opcode, int offset, int lineNumber, Instruction instruction) {
      switch(opcode) {
      case 172:
      case 173:
      case 174:
      case 175:
      case 176:
         return new ReturnInstruction(273, offset, lineNumber, instruction);
      default:
         return null;
      }
   }

   private static int UnoptimizeIfElseInLoop(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeListOffset, int afterListOffset, int returnOffset, int offset, int jumpOffset, int index) {
      int firstLoopInstructionIndex = InstructionUtil.getIndexForOffset(list, jumpOffset);
      if(firstLoopInstructionIndex != -1) {
         int length = list.size();
         if(index + 1 < length) {
            int afterLoopInstructionOffset = ((Instruction)list.get(index + 1)).offset;
            Instruction firstLoopInstruction = (Instruction)list.get(firstLoopInstructionIndex);
            int afterLoopJumpOffset;
            switch(firstLoopInstruction.opcode) {
            case 167:
            case 260:
            case 261:
            case 262:
            case 284:
            case 318:
            case 319:
               BranchInstruction afterLoopInstructionIndex = (BranchInstruction)firstLoopInstruction;
               afterLoopJumpOffset = afterLoopInstructionIndex.GetJumpOffset();
               break;
            default:
               afterLoopJumpOffset = -1;
            }

            if(afterLoopJumpOffset > afterLoopInstructionOffset) {
               int afterLoopInstructionIndex1 = InstructionUtil.getIndexForOffset(list, afterLoopJumpOffset);
               if(afterLoopInstructionIndex1 == -1 && afterLoopJumpOffset <= afterListOffset) {
                  afterLoopInstructionIndex1 = length;
               }

               if(afterLoopInstructionIndex1 != -1) {
                  int lastInstructionoffset = ((Instruction)list.get(afterLoopInstructionIndex1 - 1)).offset;
                  if(InstructionUtil.CheckNoJumpToInterval(list, 0, firstLoopInstructionIndex, offset, lastInstructionoffset) && InstructionUtil.CheckNoJumpToInterval(list, afterLoopInstructionIndex1, list.size(), offset, lastInstructionoffset)) {
                     Instruction lastInstruction = (Instruction)list.get(afterLoopInstructionIndex1 - 1);
                     Goto newGi = new Goto(167, lastInstruction.offset, Instruction.UNKNOWN_LINE_NUMBER, jumpOffset - lastInstruction.offset);
                     list.add(afterLoopInstructionIndex1, newGi);
                     return AnalyzeBackGoto(classFile, method, list, localVariables, offsetLabelSet, beforeListOffset, afterLoopJumpOffset, returnOffset, afterLoopInstructionIndex1, newGi, jumpOffset);
                  }
               }
            }
         }
      }

      return -1;
   }

   private static int UnoptimizeIfiniteLoop(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeListOffset, int afterListOffset, int returnOffset, BranchInstruction bi, int jumpOffset, int jumpIndex) {
      int length = list.size();
      if(jumpIndex + 1 >= length) {
         return -1;
      } else {
         Instruction instruction = (Instruction)list.get(jumpIndex + 1);
         if(instruction.opcode != 167) {
            return -1;
         } else {
            int afterGotoOffset = jumpIndex + 2 >= length?afterListOffset:((Instruction)list.get(jumpIndex + 2)).offset;
            Goto g = (Goto)instruction;
            int jumpGotoOffset = g.GetJumpOffset();
            if(g.offset < jumpGotoOffset && jumpGotoOffset <= afterGotoOffset) {
               int newGotoOffset = g.offset + 1;
               bi.SetJumpOffset(newGotoOffset);
               Goto newGoto = new Goto(167, newGotoOffset, Instruction.UNKNOWN_LINE_NUMBER, jumpOffset - newGotoOffset);
               list.add(jumpIndex + 2, newGoto);
               return AnalyzeBackGoto(classFile, method, list, localVariables, offsetLabelSet, beforeListOffset, jumpGotoOffset, returnOffset, jumpIndex + 2, newGoto, jumpOffset);
            } else {
               return -1;
            }
         }
      }
   }

   private static void CreateLoops(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeLoopEntryOffset, int loopEntryOffset, int beforeListOffset, int afterListOffset, int returnOffset) {
      int index = list.size();

      Instruction instruction;
      while(index-- > 0) {
         instruction = (Instruction)list.get(index);
         switch(instruction.opcode) {
         case 167:
         case 260:
         case 261:
         case 262:
         case 284:
            if(UnoptimizeLoopInLoop(list, beforeListOffset, index, instruction)) {
               ++index;
            }
         }
      }

      index = list.size();

      while(true) {
         FastList fl;
         int jumpOffset;
         do {
            int previousOffset;
            do {
               do {
                  do {
                     label79:
                     do {
                        while(index-- > 0) {
                           instruction = (Instruction)list.get(index);
                           switch(instruction.opcode) {
                           case 167:
                              Goto var17 = (Goto)instruction;
                              if(var17.branch >= 0) {
                                 break;
                              }

                              previousOffset = var17.GetJumpOffset();
                              if(beforeListOffset < previousOffset && (beforeLoopEntryOffset >= previousOffset || previousOffset > loopEntryOffset)) {
                                 jumpOffset = UnoptimizeIfElseInLoop(classFile, method, list, localVariables, offsetLabelSet, beforeListOffset, afterListOffset, returnOffset, var17.offset, previousOffset, index);
                                 if(jumpOffset == -1) {
                                    index = AnalyzeBackGoto(classFile, method, list, localVariables, offsetLabelSet, beforeListOffset, var17.offset, returnOffset, index, var17, previousOffset);
                                 } else {
                                    index = jumpOffset;
                                 }
                              }
                              break;
                           case 260:
                           case 261:
                           case 262:
                           case 284:
                              BranchInstruction var16 = (BranchInstruction)instruction;
                              if(var16.branch >= 0) {
                                 break;
                              }

                              previousOffset = var16.GetJumpOffset();
                              if(beforeListOffset >= previousOffset || beforeLoopEntryOffset < previousOffset && previousOffset <= loopEntryOffset) {
                                 break;
                              }

                              jumpOffset = UnoptimizeIfElseInLoop(classFile, method, list, localVariables, offsetLabelSet, beforeListOffset, afterListOffset, returnOffset, var16.offset, previousOffset, index);
                              if(jumpOffset == -1) {
                                 jumpOffset = UnoptimizeIfiniteLoop(classFile, method, list, localVariables, offsetLabelSet, beforeListOffset, afterListOffset, returnOffset, var16, previousOffset, index);
                              }

                              if(jumpOffset == -1) {
                                 index = AnalyzeBackIf(classFile, method, list, localVariables, offsetLabelSet, beforeListOffset, returnOffset, index, var16);
                              } else {
                                 index = jumpOffset;
                              }
                              break;
                           case 318:
                           case 319:
                              fl = (FastList)instruction;
                              continue label79;
                           }
                        }

                        return;
                     } while(fl.instructions.size() <= 0);

                     previousOffset = index > 0?((Instruction)list.get(index - 1)).offset:beforeListOffset;
                     jumpOffset = fl.GetJumpOffset();
                  } while(jumpOffset == -1);
               } while(previousOffset < jumpOffset);
            } while(beforeListOffset >= jumpOffset);
         } while(beforeLoopEntryOffset < jumpOffset && jumpOffset <= loopEntryOffset);

         fl.branch = 1;
         int afterSubListOffset = index + 1 < list.size()?((Instruction)list.get(index + 1)).offset:afterListOffset;
         index = AnalyzeBackGoto(classFile, method, list, localVariables, offsetLabelSet, beforeListOffset, afterSubListOffset, returnOffset, index, fl, jumpOffset);
      }
   }

   private static boolean UnoptimizeLoopInLoop(List<Instruction> list, int beforeListOffset, int index, Instruction instruction) {
      BranchInstruction bi = (BranchInstruction)instruction;
      if(bi.branch >= 0) {
         return false;
      } else {
         int jumpOffset = bi.GetJumpOffset();
         if(jumpOffset <= beforeListOffset) {
            return false;
         } else {
            int indexBi = index;

            while(true) {
               while(index != 0) {
                  --index;
                  instruction = (Instruction)list.get(index);
                  int jumpOffset2;
                  if(instruction.offset <= jumpOffset) {
                     instruction = (Instruction)list.get(index + 1);
                     if(bi == instruction) {
                        return false;
                     }

                     switch(instruction.opcode) {
                     case 260:
                     case 261:
                     case 262:
                     case 284:
                        BranchInstruction var13 = (BranchInstruction)instruction;
                        if(var13.branch >= 0) {
                           return false;
                        }

                        Switch target;
                        int i;
                        for(jumpOffset2 = 0; jumpOffset2 < index; ++jumpOffset2) {
                           instruction = (Instruction)list.get(jumpOffset2);
                           switch(instruction.opcode) {
                           case 170:
                           case 171:
                              target = (Switch)instruction;
                              if(target.offset + target.defaultOffset > var13.offset) {
                                 return false;
                              }

                              i = target.offsets.length;

                              while(i-- > 0) {
                                 if(target.offset + target.offsets[i] > var13.offset) {
                                    return false;
                                 }
                              }
                           }
                        }

                        jumpOffset2 = var13.GetJumpOffset();

                        while(true) {
                           while(index != 0) {
                              --index;
                              instruction = (Instruction)list.get(index);
                              if(instruction.offset <= jumpOffset2) {
                                 Instruction var14 = (Instruction)list.get(index + 1);
                                 if(var13 == var14) {
                                    return false;
                                 }

                                 i = 0;

                                 while(i < index) {
                                    instruction = (Instruction)list.get(i);
                                    switch(instruction.opcode) {
                                    case 170:
                                    case 171:
                                       Switch s = (Switch)instruction;
                                       if(s.offset + s.defaultOffset > var13.offset) {
                                          return false;
                                       }

                                       int j = s.offsets.length;

                                       while(j-- > 0) {
                                          if(s.offset + s.offsets[j] > var13.offset) {
                                             return false;
                                          }
                                       }
                                    default:
                                       ++i;
                                    }
                                 }

                                 if(bi.opcode == 167) {
                                    list.add(indexBi + 1, new Goto(167, bi.offset + 1, Instruction.UNKNOWN_LINE_NUMBER, jumpOffset2 - bi.offset - 1));
                                    var13.SetJumpOffset(bi.offset + 1);
                                    return false;
                                 }

                                 if(var14.opcode == 167 && ((Goto)var14).GetJumpOffset() == jumpOffset2) {
                                    bi.SetJumpOffset(jumpOffset2);
                                    return false;
                                 }

                                 list.add(index + 1, new Goto(167, jumpOffset2 - 1, Instruction.UNKNOWN_LINE_NUMBER, jumpOffset - jumpOffset2 + 1));
                                 bi.SetJumpOffset(jumpOffset2 - 1);
                                 return true;
                              }

                              switch(instruction.opcode) {
                              case 170:
                              case 171:
                                 target = (Switch)instruction;
                                 if(target.offset + target.defaultOffset > bi.offset) {
                                    return false;
                                 }

                                 i = target.offsets.length;

                                 while(i-- > 0) {
                                    if(target.offset + target.offsets[i] > bi.offset) {
                                       return false;
                                    }
                                 }
                              }
                           }

                           return false;
                        }
                     default:
                        return false;
                     }
                  }

                  switch(instruction.opcode) {
                  case 170:
                  case 171:
                     Switch bi2 = (Switch)instruction;
                     if(bi2.offset + bi2.defaultOffset > bi.offset) {
                        return false;
                     }

                     jumpOffset2 = bi2.offsets.length;

                     while(jumpOffset2-- > 0) {
                        if(bi2.offset + bi2.offsets[jumpOffset2] > bi.offset) {
                           return false;
                        }
                     }
                  }
               }

               return false;
            }
         }
      }
   }

   private static void CreateIfElse(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeLoopEntryOffset, int loopEntryOffset, int afterBodyLoopOffset, int afterListOffset, int breakOffset, int returnOffset) {
      int length = list.size();
      int index = 0;

      while(index < length) {
         Instruction instruction = (Instruction)list.get(index);
         switch(instruction.opcode) {
         case 260:
         case 261:
         case 262:
         case 284:
            AnalyzeIfAndIfElse(classFile, method, list, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, afterListOffset, breakOffset, returnOffset, index, (ConditionalBranchInstruction)instruction);
            length = list.size();
         default:
            ++index;
         }
      }

   }

   private static void CreateSwitch(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeLoopEntryOffset, int loopEntryOffset, int afterBodyLoopOffset, int afterListOffset, int returnOffset) {
      for(int index = 0; index < list.size(); ++index) {
         Instruction instruction = (Instruction)list.get(index);
         switch(instruction.opcode) {
         case 170:
            index = AnalyzeTableSwitch(classFile, method, list, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, afterListOffset, returnOffset, index, (TableSwitch)instruction);
            break;
         case 171:
            AnalyzeLookupSwitch(classFile, method, list, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, afterListOffset, returnOffset, index, (LookupSwitch)instruction);
         }
      }

   }

   private static void RemoveLocalVariable(Method method, IndexInstruction ii) {
      LocalVariable lv = method.getLocalVariables().searchLocalVariableWithIndexAndOffset(ii.index, ii.offset);
      if(lv != null && ii.offset == lv.start_pc) {
         method.getLocalVariables().removeLocalVariableWithIndexAndOffset(ii.index, ii.offset);
      }

   }

   private static int AnalyzeBackIf(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeListOffset, int returnOffset, int testIndex, Instruction test) {
      int index = testIndex - 1;
      ArrayList subList = new ArrayList();
      int firstOffset = ((BranchInstruction)test).GetJumpOffset();
      int beforeLoopEntryOffset = index >= 0?((Instruction)list.get(index)).offset:beforeListOffset;

      while(index >= 0 && ((Instruction)list.get(index)).offset >= firstOffset) {
         subList.add((Instruction)list.remove(index--));
      }

      int subListLength = subList.size();
      if(index >= 0) {
         beforeListOffset = ((Instruction)list.get(index)).offset;
      }

      int breakOffset = SearchMinusJumpOffset(subList, 0, subListLength, beforeListOffset, test.offset);
      BranchInstruction jumpInstructionBeforeLoop = null;
      int branch;
      Instruction lastBodyLoop;
      int typeLoop;
      int branch1;
      if(index >= 0) {
         branch = index + 1;

         while(branch-- > 0) {
            lastBodyLoop = (Instruction)list.get(branch);
            switch(lastBodyLoop.opcode) {
            case 167:
            case 260:
            case 261:
            case 262:
            case 284:
            case 318:
            case 319:
               BranchInstruction beforeLastBodyLoop = (BranchInstruction)lastBodyLoop;
               typeLoop = beforeLastBodyLoop.GetJumpOffset();
               branch1 = subList.size() > 0?((Instruction)subList.get(0)).offset:beforeLastBodyLoop.offset;
               if(branch1 < typeLoop && typeLoop <= test.offset) {
                  jumpInstructionBeforeLoop = beforeLastBodyLoop;
                  branch = 0;
               }
            }
         }
      }

      if(jumpInstructionBeforeLoop != null) {
         if(jumpInstructionBeforeLoop.opcode == 167) {
            list.remove(index--);
         }

         Instruction var21 = index >= 0 && index < list.size()?(Instruction)list.get(index):null;
         lastBodyLoop = null;
         Instruction var22 = null;
         if(subListLength > 0) {
            lastBodyLoop = (Instruction)subList.get(0);
            if(subListLength > 1) {
               var22 = (Instruction)subList.get(1);
               if(!InstructionUtil.CheckNoJumpToInterval(subList, 0, subListLength, lastBodyLoop.offset, test.offset)) {
                  lastBodyLoop = null;
                  var22 = null;
               }
            }
         }

         typeLoop = GetLoopType(var21, test, var22, lastBodyLoop);
         switch(typeLoop) {
         case 2:
            if(subListLength > 0) {
               Collections.reverse(subList);
               AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeLoopEntryOffset, test.offset, test.offset, jumpInstructionBeforeLoop.offset, test.offset, breakOffset, returnOffset);
            }

            branch1 = 1;
            if(breakOffset != -1) {
               branch1 = breakOffset - test.offset;
            }

            ++index;
            list.set(index, new FastTestList(301, test.offset, test.lineNumber, branch1, test, subList));
            break;
         case 3:
            list.remove(index);
            if(subListLength > 0) {
               Collections.reverse(subList);
               AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeLoopEntryOffset, test.offset, test.offset, jumpInstructionBeforeLoop.offset, test.offset, breakOffset, returnOffset);
            }

            CreateForLoopCase1(classFile, method, list, index, var21, test, subList, breakOffset);
            break;
         case 4:
         case 5:
         default:
            throw new UnexpectedElementException("AnalyzeBackIf");
         case 6:
            if(subListLength > 1) {
               Collections.reverse(subList);
               --subListLength;
               subList.remove(subListLength);
               AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, var22.offset, lastBodyLoop.offset, lastBodyLoop.offset, jumpInstructionBeforeLoop.offset, lastBodyLoop.offset, breakOffset, returnOffset);
               branch1 = 1;
               if(breakOffset != -1) {
                  branch1 = breakOffset - test.offset;
               }

               ++index;
               list.set(index, new FastFor(304, test.offset, test.lineNumber, branch1, (Instruction)null, test, lastBodyLoop, subList));
            } else {
               if(subListLength == 1) {
                  AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeLoopEntryOffset, test.offset, test.offset, jumpInstructionBeforeLoop.offset, test.offset, breakOffset, returnOffset);
               }

               branch1 = 1;
               if(breakOffset != -1) {
                  branch1 = breakOffset - test.offset;
               }

               ++index;
               list.set(index, new FastTestList(301, test.offset, test.lineNumber, branch1, test, subList));
            }
            break;
         case 7:
            if(subListLength > 0) {
               list.remove(index);
               Collections.reverse(subList);
               --subListLength;
               subList.remove(subListLength);
               if(subListLength > 0) {
                  AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, var22.offset, lastBodyLoop.offset, lastBodyLoop.offset, jumpInstructionBeforeLoop.offset, lastBodyLoop.offset, breakOffset, returnOffset);
               }
            }

            index = CreateForLoopCase3(classFile, method, list, index, var21, test, lastBodyLoop, subList, breakOffset);
         }
      } else if(subListLength > 0) {
         Collections.reverse(subList);
         AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeLoopEntryOffset, test.offset, test.offset, beforeListOffset, test.offset, breakOffset, returnOffset);
         branch = 1;
         if(breakOffset != -1) {
            branch = breakOffset - test.offset;
         }

         ++index;
         list.set(index, new FastTestList(302, test.offset, Instruction.UNKNOWN_LINE_NUMBER, branch, test, subList));
      } else {
         ++index;
         list.set(index, new FastTestList(301, test.offset, test.lineNumber, 1, test, (List)null));
      }

      return index;
   }

   private static int SearchMinusJumpOffset(List<Instruction> list, int fromIndex, int toIndex, int beforeListOffset, int lastListOffset) {
      int breakOffset = -1;
      int index = toIndex;

      while(true) {
         int jumpOffset;
         do {
            do {
               do {
                  FastTry ft;
                  List fs;
                  label172:
                  do {
                     label126:
                     while(index-- > fromIndex) {
                        Instruction instruction = (Instruction)list.get(index);
                        List instructions;
                        int i;
                        switch(instruction.opcode) {
                        case 167:
                        case 260:
                        case 261:
                        case 262:
                        case 284:
                           BranchInstruction bi = (BranchInstruction)instruction;
                           jumpOffset = bi.GetJumpOffset();
                           if(jumpOffset != -1 && (jumpOffset <= beforeListOffset || lastListOffset < jumpOffset) && (breakOffset == -1 || breakOffset > jumpOffset)) {
                              breakOffset = jumpOffset;
                           }
                           break;
                        case 301:
                        case 302:
                        case 304:
                        case 305:
                        case 319:
                           FastList fl = (FastList)instruction;
                           instructions = fl.instructions;
                           if(instructions == null) {
                              break;
                           }

                           jumpOffset = SearchMinusJumpOffset(instructions, 0, instructions.size(), beforeListOffset, lastListOffset);
                           if(jumpOffset != -1 && (jumpOffset <= beforeListOffset || lastListOffset < jumpOffset) && (breakOffset == -1 || breakOffset > jumpOffset)) {
                              breakOffset = jumpOffset;
                           }
                           break;
                        case 314:
                        case 315:
                        case 316:
                           FastSwitch var16 = (FastSwitch)instruction;
                           jumpOffset = var16.GetJumpOffset();
                           if(jumpOffset != -1 && (jumpOffset <= beforeListOffset || lastListOffset < jumpOffset) && (breakOffset == -1 || breakOffset > jumpOffset)) {
                              breakOffset = jumpOffset;
                           }

                           i = var16.pairs.length;

                           while(true) {
                              do {
                                 do {
                                    do {
                                       List caseInstructions;
                                       do {
                                          if(i-- <= 0) {
                                             continue label126;
                                          }

                                          caseInstructions = var16.pairs[i].getInstructions();
                                       } while(caseInstructions == null);

                                       jumpOffset = SearchMinusJumpOffset(caseInstructions, 0, caseInstructions.size(), beforeListOffset, lastListOffset);
                                    } while(jumpOffset == -1);
                                 } while(jumpOffset > beforeListOffset && lastListOffset >= jumpOffset);
                              } while(breakOffset != -1 && breakOffset <= jumpOffset);

                              breakOffset = jumpOffset;
                           }
                        case 318:
                           ft = (FastTry)instruction;
                           jumpOffset = ft.GetJumpOffset();
                           if(jumpOffset != -1 && (jumpOffset <= beforeListOffset || lastListOffset < jumpOffset) && (breakOffset == -1 || breakOffset > jumpOffset)) {
                              breakOffset = jumpOffset;
                           }

                           instructions = ft.instructions;
                           jumpOffset = SearchMinusJumpOffset(instructions, 0, instructions.size(), beforeListOffset, lastListOffset);
                           if(jumpOffset != -1 && (jumpOffset <= beforeListOffset || lastListOffset < jumpOffset) && (breakOffset == -1 || breakOffset > jumpOffset)) {
                              breakOffset = jumpOffset;
                           }

                           i = ft.catches.size();

                           while(true) {
                              do {
                                 do {
                                    do {
                                       if(i-- <= 0) {
                                          continue label172;
                                       }

                                       fs = ((FastTry.FastCatch)ft.catches.get(i)).instructions;
                                       jumpOffset = SearchMinusJumpOffset(fs, 0, fs.size(), beforeListOffset, lastListOffset);
                                    } while(jumpOffset == -1);
                                 } while(jumpOffset > beforeListOffset && lastListOffset >= jumpOffset);
                              } while(breakOffset != -1 && breakOffset <= jumpOffset);

                              breakOffset = jumpOffset;
                           }
                        }
                     }

                     return breakOffset;
                  } while(ft.finallyInstructions == null);

                  fs = ft.finallyInstructions;
                  jumpOffset = SearchMinusJumpOffset(fs, 0, fs.size(), beforeListOffset, lastListOffset);
               } while(jumpOffset == -1);
            } while(jumpOffset > beforeListOffset && lastListOffset >= jumpOffset);
         } while(breakOffset != -1 && breakOffset <= jumpOffset);

         breakOffset = jumpOffset;
      }
   }

   private static int GetMaxOffset(Instruction beforeWhileLoop, Instruction test) {
      return beforeWhileLoop.offset > test.offset?beforeWhileLoop.offset:test.offset;
   }

   private static int GetMaxOffset(Instruction beforeWhileLoop, Instruction test, Instruction lastBodyWhileLoop) {
      int offset = GetMaxOffset(beforeWhileLoop, test);
      return offset > lastBodyWhileLoop.offset?offset:lastBodyWhileLoop.offset;
   }

   private static Instruction CreateForEachVariableInstruction(Instruction i) {
      switch(i.opcode) {
      case 54:
         return new ILoad(21, i.offset, i.lineNumber, ((IStore)i).index);
      case 58:
         return new ALoad(25, i.offset, i.lineNumber, ((AStore)i).index);
      case 269:
         return new LoadInstruction(268, i.offset, i.lineNumber, ((StoreInstruction)i).index, ((StoreInstruction)i).getReturnedSignature((ConstantPool)null, (LocalVariables)null));
      case 317:
         ((FastDeclaration)i).instruction = null;
         return i;
      default:
         return i;
      }
   }

   private static void CreateForLoopCase1(ClassFile classFile, Method method, List<Instruction> list, int beforeWhileLoopIndex, Instruction beforeWhileLoop, Instruction test, List<Instruction> subList, int breakOffset) {
      int forLoopOffset = GetMaxOffset(beforeWhileLoop, test);
      int branch = 1;
      if(breakOffset != -1) {
         branch = breakOffset - forLoopOffset;
      }

      if(IsAForEachIteratorPattern(classFile, method, beforeWhileLoop, test, subList)) {
         Instruction variable = CreateForEachVariableInstruction((Instruction)subList.remove(0));
         InvokeNoStaticInstruction insi = (InvokeNoStaticInstruction)((AStore)beforeWhileLoop).valueref;
         Instruction values = insi.objectref;
         RemoveLocalVariable(method, (StoreInstruction)beforeWhileLoop);
         list.set(beforeWhileLoopIndex, new FastForEach(305, forLoopOffset, beforeWhileLoop.lineNumber, branch, variable, values, subList));
      } else {
         list.set(beforeWhileLoopIndex, new FastFor(304, forLoopOffset, beforeWhileLoop.lineNumber, branch, beforeWhileLoop, test, (Instruction)null, subList));
      }

   }

   private static int CreateForLoopCase3(ClassFile classFile, Method method, List<Instruction> list, int beforeWhileLoopIndex, Instruction beforeWhileLoop, Instruction test, Instruction lastBodyWhileLoop, List<Instruction> subList, int breakOffset) {
      int forLoopOffset = GetMaxOffset(beforeWhileLoop, test, lastBodyWhileLoop);
      int branch = 1;
      if(breakOffset != -1) {
         branch = breakOffset - forLoopOffset;
      }

      Instruction variable;
      StoreInstruction siIndex;
      StoreInstruction siTmpArray;
      Instruction values;
      switch(GetForEachArrayPatternType(classFile, beforeWhileLoop, test, lastBodyWhileLoop, list, beforeWhileLoopIndex, subList)) {
      case 1:
         variable = CreateForEachVariableInstruction((Instruction)subList.remove(0));
         --beforeWhileLoopIndex;
         siIndex = (StoreInstruction)list.remove(beforeWhileLoopIndex);
         AssignmentInstruction var15 = (AssignmentInstruction)((ArrayLength)siIndex.valueref).arrayref;
         values = var15.value2;
         RemoveLocalVariable(method, siIndex);
         RemoveLocalVariable(method, (StoreInstruction)beforeWhileLoop);
         RemoveLocalVariable(method, (AStore)var15.value1);
         list.set(beforeWhileLoopIndex, new FastForEach(305, forLoopOffset, variable.lineNumber, branch, variable, values, subList));
         break;
      case 2:
         variable = CreateForEachVariableInstruction((Instruction)subList.remove(0));
         --beforeWhileLoopIndex;
         siIndex = (StoreInstruction)list.remove(beforeWhileLoopIndex);
         --beforeWhileLoopIndex;
         siTmpArray = (StoreInstruction)list.remove(beforeWhileLoopIndex);
         values = siTmpArray.valueref;
         RemoveLocalVariable(method, siIndex);
         RemoveLocalVariable(method, (StoreInstruction)beforeWhileLoop);
         RemoveLocalVariable(method, siTmpArray);
         list.set(beforeWhileLoopIndex, new FastForEach(305, forLoopOffset, variable.lineNumber, branch, variable, values, subList));
         break;
      case 3:
         variable = CreateForEachVariableInstruction((Instruction)subList.remove(0));
         --beforeWhileLoopIndex;
         siIndex = (StoreInstruction)list.remove(beforeWhileLoopIndex);
         --beforeWhileLoopIndex;
         siTmpArray = (StoreInstruction)list.remove(beforeWhileLoopIndex);
         values = siTmpArray.valueref;
         RemoveLocalVariable(method, (StoreInstruction)beforeWhileLoop);
         RemoveLocalVariable(method, siIndex);
         RemoveLocalVariable(method, siTmpArray);
         list.set(beforeWhileLoopIndex, new FastForEach(305, forLoopOffset, variable.lineNumber, branch, variable, values, subList));
         break;
      default:
         list.set(beforeWhileLoopIndex, new FastFor(304, forLoopOffset, beforeWhileLoop.lineNumber, branch, beforeWhileLoop, test, lastBodyWhileLoop, subList));
      }

      return beforeWhileLoopIndex;
   }

   private static boolean IsAForEachIteratorPattern(ClassFile classFile, Method method, Instruction init, Instruction test, List<Instruction> subList) {
      if(classFile.getMajorVersion() >= 49 && subList.size() != 0) {
         Instruction firstInstruction = (Instruction)subList.get(0);
         if(test.lineNumber != firstInstruction.lineNumber) {
            return false;
         } else if(init.opcode != 58) {
            return false;
         } else {
            AStore astoreIterator = (AStore)init;
            if(astoreIterator.valueref.opcode != 185 && astoreIterator.valueref.opcode != 182) {
               return false;
            } else {
               LocalVariable lv = method.getLocalVariables().getLocalVariableWithIndexAndOffset(astoreIterator.index, astoreIterator.offset);
               if(lv != null && lv.signature_index != 0) {
                  ConstantPool constants = classFile.getConstantPool();
                  InvokeNoStaticInstruction insi = (InvokeNoStaticInstruction)astoreIterator.valueref;
                  ConstantMethodref cmr = constants.getConstantMethodref(insi.index);
                  ConstantNameAndType cnat = constants.getConstantNameAndType(cmr.name_and_type_index);
                  String iteratorMethodName = constants.getConstantUtf8(cnat.name_index);
                  if(!"iterator".equals(iteratorMethodName)) {
                     return false;
                  } else {
                     String iteratorMethodDescriptor = constants.getConstantUtf8(cnat.descriptor_index);
                     if(!"()Ljava/util/Iterator;".equals(iteratorMethodDescriptor)) {
                        return false;
                     } else if(test.opcode != 260) {
                        return false;
                     } else {
                        IfInstruction ifi = (IfInstruction)test;
                        if(ifi.value.opcode != 185) {
                           return false;
                        } else {
                           insi = (InvokeNoStaticInstruction)ifi.value;
                           if(insi.objectref.opcode == 25 && ((ALoad)insi.objectref).index == astoreIterator.index) {
                              cmr = constants.getConstantMethodref(insi.index);
                              cnat = constants.getConstantNameAndType(cmr.name_and_type_index);
                              String hasNextMethodName = constants.getConstantUtf8(cnat.name_index);
                              if(!"hasNext".equals(hasNextMethodName)) {
                                 return false;
                              } else {
                                 String hasNextMethodDescriptor = constants.getConstantUtf8(cnat.descriptor_index);
                                 if(!"()Z".equals(hasNextMethodDescriptor)) {
                                    return false;
                                 } else if(firstInstruction.opcode != 317) {
                                    return false;
                                 } else {
                                    FastDeclaration declaration = (FastDeclaration)firstInstruction;
                                    if(declaration.instruction == null) {
                                       return false;
                                    } else if(declaration.instruction.opcode != 58) {
                                       return false;
                                    } else {
                                       AStore astoreVariable = (AStore)declaration.instruction;
                                       if(astoreVariable.valueref.opcode == 192) {
                                          CheckCast nextMethodName = (CheckCast)astoreVariable.valueref;
                                          if(nextMethodName.objectref.opcode != 185) {
                                             return false;
                                          }

                                          insi = (InvokeNoStaticInstruction)nextMethodName.objectref;
                                       } else {
                                          if(astoreVariable.valueref.opcode != 185) {
                                             return false;
                                          }

                                          insi = (InvokeNoStaticInstruction)astoreVariable.valueref;
                                       }

                                       if(insi.objectref.opcode == 25 && ((ALoad)insi.objectref).index == astoreIterator.index) {
                                          cmr = constants.getConstantMethodref(insi.index);
                                          cnat = constants.getConstantNameAndType(cmr.name_and_type_index);
                                          String nextMethodName1 = constants.getConstantUtf8(cnat.name_index);
                                          if(!"next".equals(nextMethodName1)) {
                                             return false;
                                          } else {
                                             String nextMethodDescriptor = constants.getConstantUtf8(cnat.descriptor_index);
                                             return "()Ljava/lang/Object;".equals(nextMethodDescriptor);
                                          }
                                       } else {
                                          return false;
                                       }
                                    }
                                 }
                              }
                           } else {
                              return false;
                           }
                        }
                     }
                  }
               } else {
                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   private static int GetForEachArraySun15PatternType(Instruction init, Instruction test, Instruction inc, Instruction firstInstruction, StoreInstruction siLenght) {
      ArrayLength al = (ArrayLength)siLenght.valueref;
      if(al.arrayref.opcode != 265) {
         return 0;
      } else {
         AssignmentInstruction ai = (AssignmentInstruction)al.arrayref;
         if(ai.operator.equals("=") && ai.value1.opcode == 58) {
            StoreInstruction siTmpArray = (StoreInstruction)ai.value1;
            if(init.opcode != 54) {
               return 0;
            } else {
               StoreInstruction siIndex = (StoreInstruction)init;
               if(siIndex.valueref.opcode != 256) {
                  return 0;
               } else {
                  IConst iconst = (IConst)siIndex.valueref;
                  if(iconst.value == 0 && iconst.signature.equals("I")) {
                     if(test.opcode != 261) {
                        return 0;
                     } else {
                        IfCmp ifcmp = (IfCmp)test;
                        if(ifcmp.value1.opcode == 21 && ifcmp.value2.opcode == 21 && ((ILoad)ifcmp.value1).index == siIndex.index && ((ILoad)ifcmp.value2).index == siLenght.index) {
                           if(inc.opcode == 132 && ((IInc)inc).index == siIndex.index && ((IInc)inc).count == 1) {
                              if(firstInstruction.opcode == 317) {
                                 FastDeclaration siVariable = (FastDeclaration)firstInstruction;
                                 if(siVariable.instruction == null) {
                                    return 0;
                                 }

                                 firstInstruction = siVariable.instruction;
                              }

                              if(firstInstruction.opcode != 269 && firstInstruction.opcode != 58 && firstInstruction.opcode != 54) {
                                 return 0;
                              } else {
                                 StoreInstruction siVariable1 = (StoreInstruction)firstInstruction;
                                 if(siVariable1.valueref.opcode != 271) {
                                    return 0;
                                 } else {
                                    ArrayLoadInstruction ali = (ArrayLoadInstruction)siVariable1.valueref;
                                    return ali.arrayref.opcode == 25 && ali.indexref.opcode == 21 && ((ALoad)ali.arrayref).index == siTmpArray.index && ((ILoad)ali.indexref).index == siIndex.index?1:0;
                                 }
                              }
                           } else {
                              return 0;
                           }
                        } else {
                           return 0;
                        }
                     }
                  } else {
                     return 0;
                  }
               }
            }
         } else {
            return 0;
         }
      }
   }

   private static int GetForEachArraySun16PatternType(Instruction init, Instruction test, Instruction inc, Instruction firstInstruction, StoreInstruction siLenght, Instruction beforeBeforeForInstruction) {
      ArrayLength al = (ArrayLength)siLenght.valueref;
      if(al.arrayref.opcode != 25) {
         return 0;
      } else if(beforeBeforeForInstruction.opcode != 58) {
         return 0;
      } else {
         StoreInstruction siTmpArray = (StoreInstruction)beforeBeforeForInstruction;
         if(siTmpArray.index != ((IndexInstruction)al.arrayref).index) {
            return 0;
         } else if(init.opcode != 54) {
            return 0;
         } else {
            StoreInstruction siIndex = (StoreInstruction)init;
            if(siIndex.valueref.opcode != 256) {
               return 0;
            } else {
               IConst iconst = (IConst)siIndex.valueref;
               if(iconst.value == 0 && iconst.signature.equals("I")) {
                  if(test.opcode != 261) {
                     return 0;
                  } else {
                     IfCmp ifcmp = (IfCmp)test;
                     if(ifcmp.value1.opcode == 21 && ifcmp.value2.opcode == 21 && ((ILoad)ifcmp.value1).index == siIndex.index && ((ILoad)ifcmp.value2).index == siLenght.index) {
                        if(inc.opcode == 132 && ((IInc)inc).index == siIndex.index && ((IInc)inc).count == 1) {
                           if(firstInstruction.opcode == 317) {
                              FastDeclaration siVariable = (FastDeclaration)firstInstruction;
                              if(siVariable.instruction == null) {
                                 return 0;
                              }

                              firstInstruction = siVariable.instruction;
                           }

                           if(firstInstruction.opcode != 269 && firstInstruction.opcode != 58 && firstInstruction.opcode != 54) {
                              return 0;
                           } else {
                              StoreInstruction siVariable1 = (StoreInstruction)firstInstruction;
                              if(siVariable1.valueref.opcode != 271) {
                                 return 0;
                              } else {
                                 ArrayLoadInstruction ali = (ArrayLoadInstruction)siVariable1.valueref;
                                 return ali.arrayref.opcode == 25 && ali.indexref.opcode == 21 && ((ALoad)ali.arrayref).index == siTmpArray.index && ((ILoad)ali.indexref).index == siIndex.index?2:0;
                              }
                           }
                        } else {
                           return 0;
                        }
                     } else {
                        return 0;
                     }
                  }
               } else {
                  return 0;
               }
            }
         }
      }
   }

   private static int GetForEachArrayIbmPatternType(ClassFile classFile, Instruction init, Instruction test, Instruction inc, List<Instruction> list, int beforeWhileLoopIndex, Instruction firstInstruction, StoreInstruction siIndex) {
      IConst icont = (IConst)siIndex.valueref;
      if(icont.value != 0) {
         return 0;
      } else if(beforeWhileLoopIndex < 2) {
         return 0;
      } else {
         Instruction beforeBeforeForInstruction = (Instruction)list.get(beforeWhileLoopIndex - 2);
         if(test.lineNumber != beforeBeforeForInstruction.lineNumber) {
            return 0;
         } else if(beforeBeforeForInstruction.opcode != 58) {
            return 0;
         } else {
            StoreInstruction siTmpArray = (StoreInstruction)beforeBeforeForInstruction;
            if(init.opcode != 54) {
               return 0;
            } else {
               StoreInstruction siLenght = (StoreInstruction)init;
               if(siLenght.valueref.opcode != 190) {
                  return 0;
               } else {
                  ArrayLength al = (ArrayLength)siLenght.valueref;
                  if(al.arrayref.opcode != 25) {
                     return 0;
                  } else if(((ALoad)al.arrayref).index != siTmpArray.index) {
                     return 0;
                  } else if(test.opcode != 261) {
                     return 0;
                  } else {
                     IfCmp ifcmp = (IfCmp)test;
                     if(ifcmp.value1.opcode == 21 && ifcmp.value2.opcode == 21 && ((ILoad)ifcmp.value1).index == siIndex.index && ((ILoad)ifcmp.value2).index == siLenght.index) {
                        if(inc.opcode == 132 && ((IInc)inc).index == siIndex.index && ((IInc)inc).count == 1) {
                           if(firstInstruction.opcode != 317) {
                              return 0;
                           } else {
                              FastDeclaration declaration = (FastDeclaration)firstInstruction;
                              if(declaration.instruction == null) {
                                 return 0;
                              } else if(declaration.instruction.opcode != 269 && declaration.instruction.opcode != 58 && declaration.instruction.opcode != 54) {
                                 return 0;
                              } else {
                                 StoreInstruction siVariable = (StoreInstruction)declaration.instruction;
                                 if(siVariable.valueref.opcode != 271) {
                                    return 0;
                                 } else {
                                    ArrayLoadInstruction ali = (ArrayLoadInstruction)siVariable.valueref;
                                    return ali.arrayref.opcode == 25 && ali.indexref.opcode == 21 && ((ALoad)ali.arrayref).index == siTmpArray.index && ((ILoad)ali.indexref).index == siIndex.index?3:0;
                                 }
                              }
                           }
                        } else {
                           return 0;
                        }
                     } else {
                        return 0;
                     }
                  }
               }
            }
         }
      }
   }

   private static int GetForEachArrayPatternType(ClassFile classFile, Instruction init, Instruction test, Instruction inc, List<Instruction> list, int beforeWhileLoopIndex, List<Instruction> subList) {
      if(classFile.getMajorVersion() >= 49 && beforeWhileLoopIndex != 0 && subList.size() != 0) {
         Instruction firstInstruction = (Instruction)subList.get(0);
         if(test.lineNumber != firstInstruction.lineNumber) {
            return 0;
         } else {
            Instruction beforeForInstruction = (Instruction)list.get(beforeWhileLoopIndex - 1);
            if(test.lineNumber != beforeForInstruction.lineNumber) {
               return 0;
            } else if(beforeForInstruction.opcode != 54) {
               return 0;
            } else {
               StoreInstruction si = (StoreInstruction)beforeForInstruction;
               if(si.valueref.opcode == 190) {
                  ArrayLength al = (ArrayLength)si.valueref;
                  if(al.arrayref.opcode == 265) {
                     return GetForEachArraySun15PatternType(init, test, inc, firstInstruction, si);
                  }

                  if(beforeWhileLoopIndex > 1) {
                     Instruction beforeBeforeForInstruction = (Instruction)list.get(beforeWhileLoopIndex - 2);
                     return GetForEachArraySun16PatternType(init, test, inc, firstInstruction, si, beforeBeforeForInstruction);
                  }
               }

               return si.valueref.opcode == 256?GetForEachArrayIbmPatternType(classFile, init, test, inc, list, beforeWhileLoopIndex, firstInstruction, si):0;
            }
         }
      } else {
         return 0;
      }
   }

   private static int GetLoopType(Instruction beforeLoop, Instruction test, Instruction beforeLastBodyLoop, Instruction lastBodyLoop) {
      if(beforeLoop == null) {
         return test == null?(lastBodyLoop == null?0:(beforeLastBodyLoop != null && beforeLastBodyLoop.lineNumber > lastBodyLoop.lineNumber?4:0)):(lastBodyLoop == null?2:(test.lineNumber == Instruction.UNKNOWN_LINE_NUMBER?2:(test.lineNumber == lastBodyLoop.lineNumber?6:2)));
      } else {
         if(beforeLoop.opcode == 265) {
            beforeLoop = ((AssignmentInstruction)beforeLoop).value1;
         }

         if(test != null) {
            if(lastBodyLoop == null) {
               return beforeLoop.lineNumber == Instruction.UNKNOWN_LINE_NUMBER?2:(beforeLoop.lineNumber == test.lineNumber?3:2);
            } else {
               if(lastBodyLoop.opcode == 265) {
                  lastBodyLoop = ((AssignmentInstruction)lastBodyLoop).value1;
               }

               return beforeLoop.lineNumber == Instruction.UNKNOWN_LINE_NUMBER?(CheckBeforeLoopAndLastBodyLoop(beforeLoop, lastBodyLoop)?7:2):(beforeLastBodyLoop == null?(beforeLoop.lineNumber == test.lineNumber?(beforeLoop.lineNumber == lastBodyLoop.lineNumber?7:3):(test.lineNumber == lastBodyLoop.lineNumber?6:2)):(beforeLastBodyLoop.lineNumber >= lastBodyLoop.lineNumber?(beforeLoop.lineNumber == test.lineNumber?7:(CheckBeforeLoopAndLastBodyLoop(beforeLoop, lastBodyLoop)?7:6)):(beforeLoop.lineNumber == test.lineNumber?3:2)));
            }
         } else if(lastBodyLoop == null) {
            return 0;
         } else {
            if(lastBodyLoop.opcode == 265) {
               lastBodyLoop = ((AssignmentInstruction)lastBodyLoop).value1;
            }

            return beforeLoop.lineNumber == Instruction.UNKNOWN_LINE_NUMBER?(CheckBeforeLoopAndLastBodyLoop(beforeLoop, lastBodyLoop)?5:0):(beforeLoop.lineNumber == lastBodyLoop.lineNumber?5:(beforeLastBodyLoop != null && beforeLastBodyLoop.lineNumber > lastBodyLoop.lineNumber?4:0));
         }
      }
   }

   private static boolean CheckBeforeLoopAndLastBodyLoop(Instruction beforeLoop, Instruction lastBodyLoop) {
      switch(beforeLoop.opcode) {
      case 25:
      case 58:
      case 178:
      case 179:
      case 180:
      case 181:
      case 268:
      case 269:
         switch(lastBodyLoop.opcode) {
         case 25:
         case 58:
         case 178:
         case 179:
         case 180:
         case 181:
         case 268:
         case 269:
            if(((IndexInstruction)beforeLoop).index == ((IndexInstruction)lastBodyLoop).index) {
               return true;
            }

            return false;
         default:
            return false;
         }
      case 54:
         if(beforeLoop.opcode == lastBodyLoop.opcode || lastBodyLoop.opcode == 132) {
            return ((IndexInstruction)beforeLoop).index == ((IndexInstruction)lastBodyLoop).index;
         }
      }

      return false;
   }

   private static int AnalyzeBackGoto(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeListOffset, int afterSubListOffset, int returnOffset, int jumpInstructionIndex, Instruction jumpInstruction, int firstOffset) {
      ArrayList subList = new ArrayList();
      int index = jumpInstructionIndex - 1;
      switch(jumpInstruction.opcode) {
      case 318:
      case 319:
         subList.add((Instruction)list.get(jumpInstructionIndex));
         list.set(jumpInstructionIndex, (Object)null);
      }

      while(index >= 0 && ((Instruction)list.get(index)).offset >= firstOffset) {
         subList.add((Instruction)list.remove(index--));
      }

      int subListLength = subList.size();
      if(subListLength > 0) {
         Instruction beforeLoop = index >= 0?(Instruction)list.get(index):null;
         if(beforeLoop != null) {
            beforeListOffset = beforeLoop.offset;
         }

         Instruction instruction = (Instruction)subList.get(subListLength - 1);
         int breakOffset = SearchMinusJumpOffset(subList, 0, subListLength, beforeListOffset, jumpInstruction.offset);
         BranchInstruction test = null;
         switch(instruction.opcode) {
         case 260:
         case 261:
         case 262:
         case 284:
            BranchInstruction lastBodyLoop = (BranchInstruction)instruction;
            if(lastBodyLoop.GetJumpOffset() == breakOffset) {
               test = lastBodyLoop;
            }
         default:
            Instruction var23 = null;
            Instruction beforeLastBodyLoop = null;
            if(subListLength > 0) {
               var23 = (Instruction)subList.get(0);
               if(var23 == test) {
                  var23 = null;
               } else if(subListLength > 1) {
                  beforeLastBodyLoop = (Instruction)subList.get(1);
                  if(beforeLastBodyLoop == test) {
                     beforeLastBodyLoop = null;
                  }

                  if(!InstructionUtil.CheckNoJumpToInterval(subList, 0, subListLength, var23.offset, jumpInstruction.offset)) {
                     var23 = null;
                     beforeLastBodyLoop = null;
                  } else if(!InstructionUtil.CheckNoJumpToInterval(subList, 0, subListLength, beforeListOffset, firstOffset)) {
                     var23 = null;
                     beforeLastBodyLoop = null;
                  }
               }
            }

            int typeLoop = GetLoopType(beforeLoop, test, beforeLastBodyLoop, var23);
            int branch;
            int beforeTestOffset;
            Instruction var24;
            switch(typeLoop) {
            case 0:
               Collections.reverse(subList);
               var24 = (Instruction)subList.get(0);
               AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeListOffset, var24.offset, afterSubListOffset, beforeListOffset, afterSubListOffset, breakOffset, returnOffset);
               beforeTestOffset = 1;
               if(breakOffset != -1) {
                  beforeTestOffset = breakOffset - jumpInstruction.offset;
               }

               ++index;
               list.set(index, new FastList(303, jumpInstruction.offset, Instruction.UNKNOWN_LINE_NUMBER, beforeTestOffset, subList));
               break;
            case 1:
               Collections.reverse(subList);
               var24 = (Instruction)subList.get(0);
               AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeLoop.offset, var24.offset, afterSubListOffset, beforeListOffset, afterSubListOffset, breakOffset, returnOffset);
               beforeTestOffset = 1;
               if(breakOffset != -1) {
                  beforeTestOffset = breakOffset - jumpInstruction.offset;
               }

               ++index;
               list.set(index, new FastList(303, jumpInstruction.offset, Instruction.UNKNOWN_LINE_NUMBER, beforeTestOffset, subList));
               break;
            case 2:
               --subListLength;
               subList.remove(subListLength);
               if(subListLength > 0) {
                  Collections.reverse(subList);
                  if(beforeLoop == null) {
                     branch = beforeListOffset == -1?-1:beforeListOffset;
                  } else {
                     branch = beforeLoop.offset;
                  }

                  AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, branch, test.offset, afterSubListOffset, test.offset, afterSubListOffset, breakOffset, returnOffset);
               }

               branch = 1;
               if(breakOffset != -1) {
                  branch = breakOffset - jumpInstruction.offset;
               }

               ComparisonInstructionAnalyzer.InverseComparison(test);
               ++index;
               list.set(index, new FastTestList(301, jumpInstruction.offset, test.lineNumber, branch, test, subList));
               break;
            case 3:
               list.remove(index);
               --subListLength;
               subList.remove(subListLength);
               if(subListLength > 0) {
                  var23 = (Instruction)subList.get(0);
                  Collections.reverse(subList);
                  AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeLoop.offset, test.offset, afterSubListOffset, test.offset, afterSubListOffset, breakOffset, returnOffset);
               }

               ComparisonInstructionAnalyzer.InverseComparison(test);
               CreateForLoopCase1(classFile, method, list, index, beforeLoop, test, subList, breakOffset);
               break;
            case 4:
               Collections.reverse(subList);
               --subListLength;
               subList.remove(subListLength);
               if(subListLength > 0) {
                  --subListLength;
                  beforeLastBodyLoop = (Instruction)subList.get(subListLength);
                  AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeLastBodyLoop.offset, var23.offset, var23.offset, beforeListOffset, afterSubListOffset, breakOffset, returnOffset);
               }

               branch = 1;
               if(breakOffset != -1) {
                  branch = breakOffset - jumpInstruction.offset;
               }

               ++index;
               list.set(index, new FastFor(304, jumpInstruction.offset, var23.lineNumber, branch, (Instruction)null, (Instruction)null, var23, subList));
               break;
            case 5:
               list.remove(index);
               Collections.reverse(subList);
               --subListLength;
               subList.remove(subListLength);
               if(subListLength > 0) {
                  --subListLength;
                  beforeLastBodyLoop = (Instruction)subList.get(subListLength);
                  AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeLastBodyLoop.offset, var23.offset, var23.offset, beforeListOffset, afterSubListOffset, breakOffset, returnOffset);
               }

               branch = 1;
               if(breakOffset != -1) {
                  branch = breakOffset - jumpInstruction.offset;
               }

               list.set(index, new FastFor(304, jumpInstruction.offset, var23.lineNumber, branch, beforeLoop, (Instruction)null, var23, subList));
               break;
            case 6:
               --subListLength;
               subList.remove(subListLength);
               if(subListLength > 1) {
                  Collections.reverse(subList);
                  --subListLength;
                  subList.remove(subListLength);
                  if(subListLength > 0) {
                     beforeLastBodyLoop = (Instruction)subList.get(subListLength - 1);
                     AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeLastBodyLoop.offset, var23.offset, var23.offset, test.offset, afterSubListOffset, breakOffset, returnOffset);
                  }

                  branch = 1;
                  if(breakOffset != -1) {
                     branch = breakOffset - jumpInstruction.offset;
                  }

                  ComparisonInstructionAnalyzer.InverseComparison(test);
                  ++index;
                  list.set(index, new FastFor(304, jumpInstruction.offset, var23.lineNumber, branch, (Instruction)null, test, var23, subList));
               } else {
                  if(subListLength == 1) {
                     if(beforeLoop == null) {
                        beforeTestOffset = beforeListOffset == -1?-1:beforeListOffset;
                     } else {
                        beforeTestOffset = beforeLoop.offset;
                     }

                     AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeTestOffset, test.offset, var23.offset, test.offset, afterSubListOffset, breakOffset, returnOffset);
                  }

                  branch = 1;
                  if(breakOffset != -1) {
                     branch = breakOffset - jumpInstruction.offset;
                  }

                  ComparisonInstructionAnalyzer.InverseComparison(test);
                  ++index;
                  list.set(index, new FastTestList(301, jumpInstruction.offset, test.lineNumber, branch, test, subList));
               }
               break;
            case 7:
               list.remove(index);
               --subListLength;
               subList.remove(subListLength);
               Collections.reverse(subList);
               --subListLength;
               subList.remove(subListLength);
               if(subListLength > 0) {
                  beforeLastBodyLoop = (Instruction)subList.get(subListLength - 1);
                  AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeLastBodyLoop.offset, var23.offset, var23.offset, test.offset, afterSubListOffset, breakOffset, returnOffset);
               }

               ComparisonInstructionAnalyzer.InverseComparison(test);
               index = CreateForLoopCase3(classFile, method, list, index, beforeLoop, test, var23, subList, breakOffset);
            }
         }
      } else {
         ++index;
         list.set(index, new FastList(303, jumpInstruction.offset, Instruction.UNKNOWN_LINE_NUMBER, 0, subList));
      }

      return index;
   }

   private static void AnalyzeIfAndIfElse(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeLoopEntryOffset, int loopEntryOffset, int afterBodyLoopOffset, int afterListOffset, int breakOffset, int returnOffset, int testIndex, ConditionalBranchInstruction test) {
      int length = list.size();
      if(length != 0) {
         int elseOffset = test.GetJumpOffset();
         if(test.branch < 0 && beforeLoopEntryOffset < elseOffset && elseOffset <= loopEntryOffset && afterBodyLoopOffset == afterListOffset) {
            elseOffset = afterListOffset;
         }

         if(elseOffset > test.offset && (afterListOffset == -1 || elseOffset <= afterListOffset)) {
            int index = testIndex + 1;
            if(index < length) {
               ArrayList subList = new ArrayList();
               length = ExtrackBlock(list, subList, index, length, elseOffset);
               int subListLength = subList.size();
               if(subListLength == 0) {
                  ComparisonInstructionAnalyzer.InverseComparison(test);
                  list.set(testIndex, new FastTestList(306, test.offset, test.lineNumber, elseOffset - test.offset, test, (List)null));
                  return;
               }

               int beforeSubListOffset = test.offset;
               Instruction beforeElseBlock = (Instruction)subList.get(subListLength - 1);
               int minusJumpOffset = SearchMinusJumpOffset(subList, 0, subListLength, test.offset, beforeElseBlock.offset);
               int lastListOffset = ((Instruction)list.get(length - 1)).offset;
               if(minusJumpOffset == -1 && subListLength > 1 && beforeElseBlock.opcode == 177 && (afterListOffset == -1 || afterListOffset == returnOffset || ByteCodeUtil.JumpTo(method.getCode(), ByteCodeUtil.NextInstructionOffset(method.getCode(), lastListOffset), returnOffset))) {
                  if(((Instruction)subList.get(subListLength - 2)).lineNumber > beforeElseBlock.lineNumber) {
                     minusJumpOffset = returnOffset == -1?lastListOffset + 1:returnOffset;
                  } else if(index < length && ((Instruction)list.get(index)).lineNumber < beforeElseBlock.lineNumber) {
                     minusJumpOffset = returnOffset == -1?lastListOffset + 1:returnOffset;
                  }
               }

               if(minusJumpOffset != -1) {
                  if(subListLength == 1 && beforeElseBlock.opcode == 167) {
                     CreateBreakAndContinue(method, subList, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, afterListOffset, breakOffset, returnOffset);
                     ComparisonInstructionAnalyzer.InverseComparison(test);
                     list.set(testIndex, new FastTestList(306, beforeElseBlock.offset, test.lineNumber, elseOffset - beforeElseBlock.offset, test, subList));
                     return;
                  }

                  int afterIfElseOffset;
                  if(minusJumpOffset < test.offset && beforeLoopEntryOffset < minusJumpOffset && minusJumpOffset <= loopEntryOffset) {
                     int subElseList = SearchMinusJumpOffset(subList, 0, subListLength, -1, beforeElseBlock.offset);
                     if((subElseList == -1 || subElseList >= afterListOffset) && afterBodyLoopOffset == afterListOffset) {
                        afterIfElseOffset = afterListOffset;
                     } else {
                        afterIfElseOffset = subElseList;
                     }
                  } else {
                     afterIfElseOffset = minusJumpOffset;
                  }

                  if(afterIfElseOffset > elseOffset && (afterListOffset == -1 || afterIfElseOffset <= afterListOffset || ByteCodeUtil.JumpTo(method.getCode(), ByteCodeUtil.NextInstructionOffset(method.getCode(), lastListOffset), afterIfElseOffset))) {
                     if(beforeElseBlock.opcode == 167 && ((Goto)beforeElseBlock).GetJumpOffset() == minusJumpOffset || beforeElseBlock.opcode == 177) {
                        subList.remove(subListLength - 1);
                     }

                     ArrayList subElseList1 = new ArrayList();
                     ExtrackBlock(list, subElseList1, index, length, afterIfElseOffset);
                     if(subElseList1.size() > 0) {
                        AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, beforeSubListOffset, afterIfElseOffset, breakOffset, returnOffset);
                        beforeSubListOffset = beforeElseBlock.offset;
                        AnalyzeList(classFile, method, subElseList1, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, beforeSubListOffset, afterIfElseOffset, breakOffset, returnOffset);
                        int subElseListLength = subElseList1.size();
                        int lastIfElseOffset = subElseListLength > 0?((Instruction)subElseList1.get(subElseListLength - 1)).offset:beforeSubListOffset;
                        ComparisonInstructionAnalyzer.InverseComparison(test);
                        list.set(testIndex, new FastTest2Lists(307, lastIfElseOffset, test.lineNumber, afterIfElseOffset - lastIfElseOffset, test, subList, subElseList1));
                        return;
                     }
                  }
               }

               AnalyzeList(classFile, method, subList, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, beforeSubListOffset, elseOffset, breakOffset, returnOffset);
               ComparisonInstructionAnalyzer.InverseComparison(test);
               list.set(testIndex, new FastTestList(306, beforeElseBlock.offset, test.lineNumber, elseOffset - beforeElseBlock.offset, test, subList));
            } else if(elseOffset == breakOffset) {
               list.set(testIndex, new FastInstruction(309, test.offset, test.lineNumber, test));
            } else {
               list.set(testIndex, new FastTestList(306, test.offset, test.lineNumber, elseOffset - test.offset, test, (List)null));
            }

         }
      }
   }

   private static int ExtrackBlock(List<Instruction> list, List<Instruction> subList, int index, int length, int endOffset) {
      while(index < length && ((Instruction)list.get(index)).offset < endOffset) {
         subList.add((Instruction)list.remove(index));
         --length;
      }

      return length;
   }

   private static void AnalyzeLookupSwitch(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeLoopEntryOffset, int loopEntryOffset, int afterBodyLoopOffset, int afterListOffset, int returnOffset, int switchIndex, LookupSwitch ls) {
      int pairLength = ls.keys.length;
      FastSwitch.Pair[] pairs = new FastSwitch.Pair[pairLength + 1];
      boolean defaultFlag = true;
      int pairIndex = 0;

      int switchOpcode;
      for(switchOpcode = 0; switchOpcode < pairLength; ++switchOpcode) {
         if(defaultFlag && ls.offsets[switchOpcode] > ls.defaultOffset) {
            pairs[pairIndex++] = new FastSwitch.Pair(true, 0, ls.offset + ls.defaultOffset);
            defaultFlag = false;
         }

         pairs[pairIndex++] = new FastSwitch.Pair(false, ls.keys[switchOpcode], ls.offset + ls.offsets[switchOpcode]);
      }

      if(defaultFlag) {
         pairs[pairIndex++] = new FastSwitch.Pair(true, 0, ls.offset + ls.defaultOffset);
      }

      switchOpcode = AnalyzeSwitchType(classFile, ls.key);
      if(classFile.getMajorVersion() >= 51 && switchOpcode == 314 && ls.key.opcode == 21 && switchIndex > 2 && AnalyzeSwitchString(classFile, localVariables, list, switchIndex, ls, pairs)) {
         --switchIndex;
         list.remove(switchIndex);
         --switchIndex;
         list.remove(switchIndex);
         --switchIndex;
         list.remove(switchIndex);
         switchOpcode = 316;
      }

      AnalyzeSwitch(classFile, method, list, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, afterListOffset, returnOffset, switchIndex, switchOpcode, ls.offset, ls.lineNumber, ls.key, pairs, pairLength);
   }

   private static int AnalyzeSwitchType(ClassFile classFile, Instruction i) {
      if(i.opcode == 271) {
         ArrayLoadInstruction ali = (ArrayLoadInstruction)i;
         if(ali.indexref.opcode == 182) {
            ConstantPool constants;
            ConstantNameAndType cnat;
            Invokevirtual iv;
            if(ali.arrayref.opcode == 178) {
               GetStatic is = (GetStatic)ali.arrayref;
               constants = classFile.getConstantPool();
               ConstantFieldref cmr = constants.getConstantFieldref(is.index);
               cnat = constants.getConstantNameAndType(cmr.name_and_type_index);
               if(classFile.getSwitchMaps().containsKey(Integer.valueOf(cnat.name_index))) {
                  iv = (Invokevirtual)ali.indexref;
                  if(iv.args.size() == 0) {
                     ConstantMethodref cmr1 = constants.getConstantMethodref(iv.index);
                     cnat = constants.getConstantNameAndType(cmr1.name_and_type_index);
                     if("ordinal".equals(constants.getConstantUtf8(cnat.name_index))) {
                        return 315;
                     }
                  }
               }
            } else if(ali.arrayref.opcode == 184) {
               Invokestatic is1 = (Invokestatic)ali.arrayref;
               if(is1.args.size() == 0) {
                  constants = classFile.getConstantPool();
                  ConstantMethodref cmr2 = constants.getConstantMethodref(is1.index);
                  if(cmr2.class_index == classFile.getThisClassIndex()) {
                     cnat = constants.getConstantNameAndType(cmr2.name_and_type_index);
                     if(classFile.getSwitchMaps().containsKey(Integer.valueOf(cnat.name_index))) {
                        iv = (Invokevirtual)ali.indexref;
                        if(iv.args.size() == 0) {
                           cmr2 = constants.getConstantMethodref(iv.index);
                           cnat = constants.getConstantNameAndType(cmr2.name_and_type_index);
                           if("ordinal".equals(constants.getConstantUtf8(cnat.name_index))) {
                              return 315;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return 314;
   }

   private static int AnalyzeTableSwitch(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeLoopEntryOffset, int loopEntryOffset, int afterBodyLoopOffset, int afterListOffset, int returnOffset, int switchIndex, TableSwitch ts) {
      int pairLength = ts.offsets.length;
      FastSwitch.Pair[] pairs = new FastSwitch.Pair[pairLength + 1];
      boolean defaultFlag = true;
      int pairIndex = 0;

      int switchOpcode;
      for(switchOpcode = 0; switchOpcode < pairLength; ++switchOpcode) {
         if(defaultFlag && ts.offsets[switchOpcode] > ts.defaultOffset) {
            pairs[pairIndex++] = new FastSwitch.Pair(true, 0, ts.offset + ts.defaultOffset);
            defaultFlag = false;
         }

         pairs[pairIndex++] = new FastSwitch.Pair(false, ts.low + switchOpcode, ts.offset + ts.offsets[switchOpcode]);
      }

      if(defaultFlag) {
         pairs[pairIndex++] = new FastSwitch.Pair(true, 0, ts.offset + ts.defaultOffset);
      }

      switchOpcode = AnalyzeSwitchType(classFile, ts.key);
      if(classFile.getMajorVersion() >= 51 && switchOpcode == 314 && ts.key.opcode == 21 && switchIndex > 2 && AnalyzeSwitchString(classFile, localVariables, list, switchIndex, ts, pairs)) {
         --switchIndex;
         list.remove(switchIndex);
         --switchIndex;
         list.remove(switchIndex);
         --switchIndex;
         list.remove(switchIndex);
         switchOpcode = 316;
      }

      AnalyzeSwitch(classFile, method, list, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, afterListOffset, returnOffset, switchIndex, switchOpcode, ts.offset, ts.lineNumber, ts.key, pairs, pairLength);
      return switchIndex;
   }

   private static boolean AnalyzeSwitchString(ClassFile classFile, LocalVariables localVariables, List<Instruction> list, int switchIndex, Switch s, FastSwitch.Pair[] pairs) {
      Instruction instruction = (Instruction)list.get(switchIndex - 3);
      if(instruction.opcode == 58 && instruction.lineNumber == s.key.lineNumber) {
         AStore astore = (AStore)instruction;
         instruction = (Instruction)list.get(switchIndex - 2);
         if(instruction.opcode == 54 && instruction.lineNumber == astore.lineNumber) {
            instruction = (Instruction)list.get(switchIndex - 1);
            if(instruction.opcode == 314 && instruction.lineNumber == astore.lineNumber) {
               FastSwitch previousSwitch = (FastSwitch)instruction;
               if(previousSwitch.test.opcode != 182) {
                  return false;
               } else {
                  Invokevirtual iv = (Invokevirtual)previousSwitch.test;
                  if(iv.objectref.opcode == 25 && iv.args.size() == 0) {
                     ConstantPool constants = classFile.getConstantPool();
                     ConstantMethodref cmr = constants.getConstantMethodref(iv.index);
                     if(!cmr.getReturnedSignature().equals("I")) {
                        return false;
                     } else {
                        String className = constants.getConstantClassName(cmr.class_index);
                        if(!className.equals("java/lang/String")) {
                           return false;
                        } else {
                           ConstantNameAndType cnat = constants.getConstantNameAndType(cmr.name_and_type_index);
                           String descriptorName = constants.getConstantUtf8(cnat.descriptor_index);
                           if(!descriptorName.equals("()I")) {
                              return false;
                           } else {
                              String methodName = constants.getConstantUtf8(cnat.name_index);
                              if(!methodName.equals("hashCode")) {
                                 return false;
                              } else {
                                 FastSwitch.Pair[] previousPairs = previousSwitch.pairs;
                                 int i = previousPairs.length;
                                 if(i == 0) {
                                    return false;
                                 } else {
                                    int tsKeyIloadIndex = ((ILoad)s.key).index;
                                    int previousSwitchAloadIndex = ((ALoad)iv.objectref).index;
                                    HashMap stringIndexes = new HashMap();

                                    label106:
                                    while(true) {
                                       FastSwitch.Pair pair;
                                       do {
                                          if(i-- <= 0) {
                                             i = pairs.length;

                                             while(i-- > 0) {
                                                pair = pairs[i];
                                                if(!pair.isDefault()) {
                                                   pair.setKey(((Integer)stringIndexes.get(Integer.valueOf(pair.getKey()))).intValue());
                                                }
                                             }

                                             localVariables.removeLocalVariableWithIndexAndOffset(tsKeyIloadIndex, s.key.offset);
                                             localVariables.removeLocalVariableWithIndexAndOffset(astore.index, astore.offset);
                                             s.key = astore.valueref;
                                             return true;
                                          }

                                          pair = previousPairs[i];
                                       } while(pair.isDefault());

                                       List instructions = pair.getInstructions();

                                       while(true) {
                                          int length = instructions.size();
                                          if(length == 0) {
                                             return false;
                                          }

                                          instruction = (Instruction)instructions.get(0);
                                          if(instruction.opcode == 306) {
                                             switch(length) {
                                             case 2:
                                                if(((Instruction)instructions.get(1)).opcode != 312) {
                                                   break;
                                                }
                                             case 1:
                                                FastTestList var25 = (FastTestList)instruction;
                                                if(var25.instructions.size() == 1 && AnalyzeSwitchStringTestInstructions(constants, cmr, tsKeyIloadIndex, previousSwitchAloadIndex, stringIndexes, var25.test, (Instruction)var25.instructions.get(0), 7)) {
                                                   continue label106;
                                                }

                                                return false;
                                             }

                                             return false;
                                          }

                                          if(instruction.opcode != 307) {
                                             return false;
                                          }

                                          if(length != 1) {
                                             return false;
                                          }

                                          FastTest2Lists ft2l = (FastTest2Lists)instruction;
                                          if(ft2l.instructions.size() != 1 || !AnalyzeSwitchStringTestInstructions(constants, cmr, tsKeyIloadIndex, previousSwitchAloadIndex, stringIndexes, ft2l.test, (Instruction)ft2l.instructions.get(0), 7)) {
                                             return false;
                                          }

                                          instructions = ft2l.instructions2;
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  } else {
                     return false;
                  }
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean AnalyzeSwitchStringTestInstructions(ConstantPool constants, ConstantMethodref cmr, int tsKeyIloadIndex, int previousSwitchAloadIndex, HashMap<Integer, Integer> stringIndexes, Instruction test, Instruction value, int cmp) {
      if(test.opcode != 260) {
         return false;
      } else if(value.opcode != 54) {
         return false;
      } else {
         IStore istore = (IStore)value;
         if(istore.index != tsKeyIloadIndex) {
            return false;
         } else {
            int opcode = istore.valueref.opcode;
            int index;
            if(opcode == 16) {
               index = ((BIPush)istore.valueref).value;
            } else {
               if(opcode != 256) {
                  return false;
               }

               index = ((IConst)istore.valueref).value;
            }

            IfInstruction ii = (IfInstruction)test;
            if(ii.cmp == cmp && ii.value.opcode == 182) {
               Invokevirtual ivTest = (Invokevirtual)ii.value;
               if(ivTest.args.size() == 1 && ivTest.objectref.opcode == 25 && ((ALoad)ivTest.objectref).index == previousSwitchAloadIndex && ((Instruction)ivTest.args.get(0)).opcode == 18) {
                  ConstantMethodref cmrTest = constants.getConstantMethodref(ivTest.index);
                  if(cmr.class_index != cmrTest.class_index) {
                     return false;
                  } else {
                     ConstantNameAndType cnatTest = constants.getConstantNameAndType(cmrTest.name_and_type_index);
                     String descriptorNameTest = constants.getConstantUtf8(cnatTest.descriptor_index);
                     if(!descriptorNameTest.equals("(Ljava/lang/Object;)Z")) {
                        return false;
                     } else {
                        String methodNameTest = constants.getConstantUtf8(cnatTest.name_index);
                        if(!methodNameTest.equals("equals")) {
                           return false;
                        } else {
                           stringIndexes.put(Integer.valueOf(index), Integer.valueOf(((Ldc)ivTest.args.get(0)).index));
                           return true;
                        }
                     }
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }
   }

   private static void AnalyzeSwitch(ClassFile classFile, Method method, List<Instruction> list, LocalVariables localVariables, IntSet offsetLabelSet, int beforeLoopEntryOffset, int loopEntryOffset, int afterBodyLoopOffset, int afterListOffset, int returnOffset, int switchIndex, int switchOpcode, int switchOffset, int switchLineNumber, Instruction test, FastSwitch.Pair[] pairs, int pairLength) {
      int breakOffset = -1;
      Arrays.sort(pairs);
      int lastSwitchOffset = switchOffset;
      int index = switchIndex + 1;
      int branch;
      if(index < list.size()) {
         int instructions;
         int instruction;
         int jumpOffset;
         for(branch = 0; branch < pairLength; ++branch) {
            ArrayList i = null;

            Instruction nbrInstrucrions;
            for(instructions = pairs[branch + 1].getOffset(); index < list.size(); lastSwitchOffset = nbrInstrucrions.offset) {
               nbrInstrucrions = (Instruction)list.get(index);
               if(nbrInstrucrions.offset >= instructions) {
                  if(i == null) {
                     break;
                  }

                  instruction = i.size();
                  if(instruction <= 0) {
                     break;
                  }

                  jumpOffset = SearchMinusJumpOffset(i, 0, instruction, lastSwitchOffset, lastSwitchOffset);
                  if(jumpOffset != -1 && (breakOffset == -1 || breakOffset > jumpOffset)) {
                     breakOffset = jumpOffset;
                  }

                  nbrInstrucrions = (Instruction)i.get(instruction - 1);
                  if(nbrInstrucrions.opcode != 167) {
                     break;
                  }

                  int lineNumber = nbrInstrucrions.lineNumber;
                  if(instruction <= 1 || ((Instruction)i.get(instruction - 2)).lineNumber == lineNumber) {
                     lineNumber = Instruction.UNKNOWN_LINE_NUMBER;
                  }

                  i.set(instruction - 1, new FastInstruction(312, nbrInstrucrions.offset, lineNumber, (Instruction)null));
                  break;
               }

               if(i == null) {
                  i = new ArrayList();
               }

               list.remove(index);
               i.add(nbrInstrucrions);
            }

            pairs[branch].setInstructions(i);
         }

         int var32;
         if(breakOffset != -1) {
            branch = breakOffset >= switchOffset?breakOffset:((Instruction)list.get(list.size() - 1)).offset + 1;
            int pair = switchIndex;

            Instruction var28;
            while(pair-- > 0) {
               var28 = (Instruction)list.get(pair);
               switch(var28.opcode) {
               case 167:
               case 260:
               case 261:
               case 262:
               case 314:
               case 315:
               case 316:
                  instructions = ((BranchInstruction)var28).GetJumpOffset();
                  if(lastSwitchOffset < instructions && instructions < branch) {
                     branch = instructions;
                  }
               }
            }

            pair = list.size();

            while(pair-- > 0) {
               var28 = (Instruction)list.get(pair);
               switch(var28.opcode) {
               case 167:
               case 260:
               case 261:
               case 262:
               case 314:
               case 315:
               case 316:
                  instructions = ((BranchInstruction)var28).GetJumpOffset();
                  if(lastSwitchOffset < instructions && instructions < branch) {
                     branch = instructions;
                  }
               }

               if(var28.offset <= branch || var28.offset <= lastSwitchOffset) {
                  break;
               }
            }

            ArrayList var31;
            for(var31 = null; index < list.size(); lastSwitchOffset = var28.offset) {
               var28 = (Instruction)list.get(index);
               if(var28.offset >= branch) {
                  if(var31 != null) {
                     var32 = var31.size();
                     if(var32 > 0) {
                        var28 = (Instruction)var31.get(var32 - 1);
                        if(var28.opcode == 167) {
                           instruction = var28.lineNumber;
                           if(var32 <= 1 || ((Instruction)var31.get(var32 - 2)).lineNumber == instruction) {
                              instruction = Instruction.UNKNOWN_LINE_NUMBER;
                           }

                           var31.set(var32 - 1, new FastInstruction(312, var28.offset, var28.lineNumber, (Instruction)null));
                        }
                     }
                  }
                  break;
               }

               if(var31 == null) {
                  var31 = new ArrayList();
               }

               list.remove(index);
               var31.add(var28);
            }

            pairs[pairLength].setInstructions(var31);
         }

         branch = test.offset;
         if(index < list.size()) {
            afterListOffset = ((Instruction)list.get(index)).offset;
         }

         for(int var29 = 0; var29 <= pairLength; ++var29) {
            FastSwitch.Pair var30 = pairs[var29];
            List var33 = var30.getInstructions();
            if(var33 != null) {
               var32 = var33.size();
               if(var32 > 0) {
                  Instruction var34 = (Instruction)var33.get(var32 - 1);
                  if(var34.opcode == 312) {
                     var33.remove(var32 - 1);
                     AnalyzeList(classFile, method, var33, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, branch, afterListOffset, breakOffset, returnOffset);
                     var33.add(var34);
                  } else {
                     AnalyzeList(classFile, method, var33, localVariables, offsetLabelSet, beforeLoopEntryOffset, loopEntryOffset, afterBodyLoopOffset, branch, afterListOffset, breakOffset, returnOffset);
                     var32 = var33.size();
                     if(var32 > 0) {
                        var34 = (Instruction)var33.get(var32 - 1);
                        switch(var34.opcode) {
                        case 167:
                        case 260:
                        case 261:
                        case 262:
                        case 306:
                        case 307:
                        case 314:
                        case 315:
                        case 316:
                           jumpOffset = ((BranchInstruction)var34).GetJumpOffset();
                           if(jumpOffset < switchOffset || lastSwitchOffset < jumpOffset) {
                              var33.add(new FastInstruction(312, lastSwitchOffset + 1, Instruction.UNKNOWN_LINE_NUMBER, (Instruction)null));
                           }
                        }
                     }
                  }

                  branch = var34.offset;
               }
            }
         }
      }

      branch = breakOffset == -1?1:breakOffset - lastSwitchOffset;
      list.set(switchIndex, new FastSwitch(switchOpcode, lastSwitchOffset, switchLineNumber, branch, test, pairs));
   }

   private static void AddLabels(List<Instruction> list, IntSet offsetLabelSet) {
      for(int i = offsetLabelSet.size() - 1; i >= 0; --i) {
         SearchInstructionAndAddLabel(list, offsetLabelSet.get(i));
      }

   }

   private static boolean SearchInstructionAndAddLabel(List<Instruction> list, int labelOffset) {
      int index = InstructionUtil.getIndexForOffset(list, labelOffset);
      if(index < 0) {
         return false;
      } else {
         boolean found = false;
         Instruction instruction = (Instruction)list.get(index);
         switch(instruction.opcode) {
         case 301:
         case 302:
         case 306:
            FastTestList var15 = (FastTestList)instruction;
            if(labelOffset >= var15.test.offset && var15.instructions != null) {
               found = SearchInstructionAndAddLabel(var15.instructions, labelOffset);
            }
            break;
         case 303:
            List var13 = ((FastList)instruction).instructions;
            if(var13 != null) {
               found = SearchInstructionAndAddLabel(var13, labelOffset);
            }
            break;
         case 304:
            FastFor var12 = (FastFor)instruction;
            if((var12.init == null || labelOffset >= var12.init.offset) && var12.instructions != null) {
               found = SearchInstructionAndAddLabel(var12.instructions, labelOffset);
            }
         case 305:
         case 308:
         case 309:
         case 310:
         case 311:
         case 312:
         case 313:
         case 317:
         default:
            break;
         case 307:
            FastTest2Lists var11 = (FastTest2Lists)instruction;
            if(labelOffset >= var11.test.offset) {
               found = SearchInstructionAndAddLabel(var11.instructions, labelOffset) || SearchInstructionAndAddLabel(var11.instructions2, labelOffset);
            }
            break;
         case 314:
         case 315:
         case 316:
            FastSwitch var10 = (FastSwitch)instruction;
            if(labelOffset >= var10.test.offset) {
               FastSwitch.Pair[] var14 = var10.pairs;
               if(var14 != null) {
                  for(int i1 = var14.length - 1; i1 >= 0 && !found; --i1) {
                     List instructions = var14[i1].getInstructions();
                     if(instructions != null) {
                        found = SearchInstructionAndAddLabel(instructions, labelOffset);
                     }
                  }
               }
            }
            break;
         case 318:
            FastTry var9 = (FastTry)instruction;
            found = SearchInstructionAndAddLabel(var9.instructions, labelOffset);
            if(!found && var9.catches != null) {
               for(int i = var9.catches.size() - 1; i >= 0 && !found; --i) {
                  found = SearchInstructionAndAddLabel(((FastTry.FastCatch)var9.catches.get(i)).instructions, labelOffset);
               }
            }

            if(!found && var9.finallyInstructions != null) {
               found = SearchInstructionAndAddLabel(var9.finallyInstructions, labelOffset);
            }
            break;
         case 319:
            FastSynchronized ft = (FastSynchronized)instruction;
            if(labelOffset >= ft.monitor.offset && ft.instructions != null) {
               found = SearchInstructionAndAddLabel(ft.instructions, labelOffset);
            }
         }

         if(!found) {
            list.set(index, new FastLabel(320, labelOffset, instruction.lineNumber, instruction));
         }

         return true;
      }
   }
}
