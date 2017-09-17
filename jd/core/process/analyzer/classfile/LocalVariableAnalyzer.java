package jd.core.process.analyzer.classfile;

import java.util.ArrayList;
import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.attribute.AttributeSignature;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.ExceptionLoad;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.ILoad;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.MonitorEnter;
import jd.core.model.instruction.bytecode.instruction.MonitorExit;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.process.analyzer.classfile.visitor.AddCheckCastVisitor;
import jd.core.process.analyzer.classfile.visitor.SearchInstructionByOffsetVisitor;
import jd.core.process.analyzer.instruction.bytecode.util.ByteCodeUtil;
import jd.core.process.analyzer.util.InstructionUtil;
import jd.core.process.analyzer.variable.DefaultVariableNameGenerator;
import jd.core.util.SignatureUtil;

public class LocalVariableAnalyzer {
   private static final int UNDEFINED_TYPE = -1;
   private static final int NUMBER_TYPE = -2;
   private static final int OBJECT_TYPE = -3;

   public static void Analyze(ClassFile classFile, Method method, DefaultVariableNameGenerator variableNameGenerator, List<Instruction> list, List<Instruction> listForAnalyze) {
      ConstantPool constants = classFile.getConstantPool();
      variableNameGenerator.clearLocalNames();
      byte[] code = method.getCode();
      int codeLength = code == null?0:code.length;
      LocalVariables localVariables = method.getLocalVariables();
      String methodSignature1;
      int indexOfFirstLocalVariable1;
      if(localVariables == null) {
         localVariables = new LocalVariables();
         method.setLocalVariables(localVariables);
         int returnedSignature;
         if((method.access_flags & 8) == 0) {
            returnedSignature = constants.addConstantUtf8("this");
            int methodSignature = constants.addConstantUtf8(classFile.getInternalClassName());
            LocalVariable indexOfFirstLocalVariable = new LocalVariable(0, codeLength, returnedSignature, methodSignature, 0);
            localVariables.add(indexOfFirstLocalVariable);
         }

         if(method.name_index == constants.instanceConstructorIndex && classFile.isAInnerClass() && (classFile.access_flags & 8) == 0) {
            returnedSignature = constants.addConstantUtf8("this$1");
            methodSignature1 = classFile.getInternalClassName();
            indexOfFirstLocalVariable1 = methodSignature1.lastIndexOf(36);
            String internalOuterClassName = methodSignature1.substring(0, indexOfFirstLocalVariable1) + ';';
            int signatureIndex = constants.addConstantUtf8(internalOuterClassName);
            LocalVariable lv = new LocalVariable(0, codeLength, returnedSignature, signatureIndex, 1);
            localVariables.add(lv);
         }

         AnalyzeMethodParameter(classFile, constants, method, localVariables, variableNameGenerator, codeLength);
         localVariables.setIndexOfFirstLocalVariable(localVariables.size());
         if(code != null) {
            GenerateMissingMonitorLocalVariables(constants, localVariables, listForAnalyze);
         }
      } else {
         AttributeSignature returnedSignature1 = method.getAttributeSignature();
         methodSignature1 = constants.getConstantUtf8(returnedSignature1 == null?method.descriptor_index:returnedSignature1.signature_index);
         indexOfFirstLocalVariable1 = ((method.access_flags & 8) == 0?1:0) + SignatureUtil.GetParameterSignatureCount(methodSignature1);
         if(indexOfFirstLocalVariable1 > localVariables.size()) {
            AnalyzeMethodParameter(classFile, constants, method, localVariables, variableNameGenerator, codeLength);
         }

         localVariables.setIndexOfFirstLocalVariable(indexOfFirstLocalVariable1);
         if(code != null) {
            GenerateMissingMonitorLocalVariables(constants, localVariables, listForAnalyze);
            CheckLocalVariableRanges(constants, code, localVariables, variableNameGenerator, listForAnalyze);
         }
      }

      if(code != null) {
         String returnedSignature2 = GetReturnedSignature(classFile, method);
         AnalyzeMethodCode(constants, localVariables, list, listForAnalyze, returnedSignature2);
         SetConstantTypes(classFile, constants, method, localVariables, list, listForAnalyze, returnedSignature2);
         InitialyzeExceptionLoad(listForAnalyze, localVariables);
      }

      GenerateLocalVariableNames(constants, localVariables, variableNameGenerator);
   }

   private static void AnalyzeMethodParameter(ClassFile classFile, ConstantPool constants, Method method, LocalVariables localVariables, DefaultVariableNameGenerator variableNameGenerator, int codeLength) {
      AttributeSignature as = method.getAttributeSignature();
      boolean descriptorFlag = as == null;
      String methodSignature = constants.getConstantUtf8(descriptorFlag?method.descriptor_index:as.signature_index);
      ArrayList parameterTypes = SignatureUtil.GetParameterSignatures(methodSignature);
      if(parameterTypes != null) {
         boolean staticMethodFlag = (method.access_flags & 8) != 0;
         int variableIndex = staticMethodFlag?0:1;
         byte firstVisibleParameterCounter = 0;
         if(method.name_index == constants.instanceConstructorIndex) {
            if((classFile.access_flags & 16384) != 0) {
               if(descriptorFlag) {
                  firstVisibleParameterCounter = 2;
               } else {
                  variableIndex = 3;
               }
            } else if(classFile.isAInnerClass() && (classFile.access_flags & 8) == 0) {
               firstVisibleParameterCounter = 1;
            }
         }

         int anonymousClassDepth = 0;

         for(ClassFile anonymousClassFile = classFile; anonymousClassFile != null && anonymousClassFile.getInternalAnonymousClassName() != null; anonymousClassFile = anonymousClassFile.getOuterClass()) {
            ++anonymousClassDepth;
         }

         int length = parameterTypes.size();
         int varargsParameterIndex;
         if((method.access_flags & 128) == 0) {
            varargsParameterIndex = Integer.MAX_VALUE;
         } else {
            varargsParameterIndex = length - 1;
         }

         for(int parameterIndex = 0; parameterIndex < length; ++parameterIndex) {
            String signature = (String)parameterTypes.get(parameterIndex);
            if(localVariables.getLocalVariableWithIndexAndOffset(variableIndex, 0) == null) {
               boolean firstChar = SignatureAppearsOnceInParameters(parameterTypes, firstVisibleParameterCounter, length, signature);
               String name = variableNameGenerator.generateParameterNameFromSignature(signature, firstChar, parameterIndex == varargsParameterIndex, anonymousClassDepth);
               int nameIndex = constants.addConstantUtf8(name);
               int signatureIndex = constants.addConstantUtf8(signature);
               LocalVariable lv = new LocalVariable(0, codeLength, nameIndex, signatureIndex, variableIndex);
               localVariables.add(lv);
            }

            char var24 = signature.charAt(0);
            variableIndex += var24 != 68 && var24 != 74?1:2;
         }
      }

   }

   private static void GenerateMissingMonitorLocalVariables(ConstantPool constants, LocalVariables localVariables, List<Instruction> listForAnalyze) {
      int length = listForAnalyze.size();

      for(int i = 1; i < length; ++i) {
         Instruction instruction = (Instruction)listForAnalyze.get(i);
         if(instruction.opcode == 194) {
            MonitorEnter mEnter = (MonitorEnter)instruction;
            boolean monitorLocalVariableIndex = false;
            boolean monitorLocalVariableOffset = false;
            int monitorLocalVariableLenght = 1;
            int var14;
            int var15;
            if(mEnter.objectref.opcode == 263) {
               instruction = (Instruction)listForAnalyze.get(i - 1);
               if(instruction.opcode != 58) {
                  continue;
               }

               AStore monitorExitCount = (AStore)instruction;
               if(monitorExitCount.valueref.opcode != 263) {
                  continue;
               }

               DupLoad j = (DupLoad)mEnter.objectref;
               DupLoad lv = (DupLoad)monitorExitCount.valueref;
               if(j.dupStore != lv.dupStore) {
                  continue;
               }

               var14 = monitorExitCount.index;
               var15 = monitorExitCount.offset;
            } else {
               if(mEnter.objectref.opcode != 25) {
                  continue;
               }

               ALoad var16 = (ALoad)mEnter.objectref;
               instruction = (Instruction)listForAnalyze.get(i - 1);
               if(instruction.opcode != 58) {
                  continue;
               }

               AStore var18 = (AStore)instruction;
               if(var18.index != var16.index) {
                  continue;
               }

               var14 = var18.index;
               var15 = var18.offset;
            }

            int var17 = 0;
            int var19 = i;

            while(true) {
               ++var19;
               ALoad var20;
               if(var19 >= length) {
                  if(var17 == 1) {
                     var19 = i;

                     while(var19-- > 0) {
                        instruction = (Instruction)listForAnalyze.get(var19);
                        if(instruction.opcode == 195 && ((MonitorExit)instruction).objectref.opcode == 25) {
                           var20 = (ALoad)((MonitorExit)instruction).objectref;
                           if(var20.index == var14) {
                              monitorLocalVariableLenght += var15 - var20.offset;
                              var15 = var20.offset;
                              ++var17;
                              break;
                           }
                        }
                     }
                  }

                  if(var17 >= 2) {
                     LocalVariable var21 = localVariables.getLocalVariableWithIndexAndOffset(var14, var15);
                     if(var21 == null || var21.start_pc + var21.length < var15 + monitorLocalVariableLenght) {
                        int signatureIndex = constants.addConstantUtf8("Ljava/lang/Object;");
                        localVariables.add(new LocalVariable(var15, monitorLocalVariableLenght, signatureIndex, signatureIndex, var14));
                     }
                  }
                  break;
               }

               instruction = (Instruction)listForAnalyze.get(var19);
               if(instruction.opcode == 195 && ((MonitorExit)instruction).objectref.opcode == 25) {
                  var20 = (ALoad)((MonitorExit)instruction).objectref;
                  if(var20.index == var14) {
                     monitorLocalVariableLenght = var20.offset - var15;
                     ++var17;
                  }
               }
            }
         }
      }

   }

   private static void CheckLocalVariableRanges(ConstantPool constants, byte[] code, LocalVariables localVariables, DefaultVariableNameGenerator variableNameGenerator, List<Instruction> listForAnalyze) {
      int length = localVariables.size();

      int i;
      for(i = localVariables.getIndexOfFirstLocalVariable(); i < length; ++i) {
         localVariables.getLocalVariableAt(i).length = 1;
      }

      length = listForAnalyze.size();

      for(i = 0; i < length; ++i) {
         Instruction instruction = (Instruction)listForAnalyze.get(i);
         switch(instruction.opcode) {
         case 21:
         case 25:
         case 54:
         case 132:
         case 268:
         case 269:
            CheckLocalVariableRangesForIndexInstruction(code, localVariables, (IndexInstruction)instruction);
            break;
         case 58:
            AStore astore = (AStore)instruction;
            int signatureIndex;
            if(astore.valueref.opcode == 270) {
               ExceptionLoad var15 = (ExceptionLoad)astore.valueref;
               if(var15.exceptionNameIndex != 0) {
                  LocalVariable var16 = localVariables.getLocalVariableWithIndexAndOffset(astore.index, astore.offset);
                  if(var16 == null) {
                     signatureIndex = ByteCodeUtil.NextInstructionOffset(code, astore.offset);
                     var16 = localVariables.getLocalVariableWithIndexAndOffset(astore.index, signatureIndex);
                     if(var16 == null) {
                        var16 = new LocalVariable(astore.offset, 1, -1, var15.exceptionNameIndex, astore.index, true);
                        localVariables.add(var16);
                        String signature = constants.getConstantUtf8(var15.exceptionNameIndex);
                        boolean appearsOnce = SignatureAppearsOnceInLocalVariables(localVariables, localVariables.size(), var15.exceptionNameIndex);
                        String name = variableNameGenerator.generateLocalVariableNameFromSignature(signature, appearsOnce);
                        var16.name_index = constants.addConstantUtf8(name);
                     } else {
                        var16.updateRange(astore.offset);
                     }
                  }
               }
            } else {
               if(i + 1 < length && astore.valueref.opcode == 263 && ((Instruction)listForAnalyze.get(i + 1)).opcode == 194) {
                  LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(astore.index, astore.offset);
                  if(lv != null) {
                     continue;
                  }

                  MonitorEnter me = (MonitorEnter)listForAnalyze.get(i + 1);
                  if(me.objectref.opcode == 263 && ((DupLoad)astore.valueref).dupStore == ((DupLoad)me.objectref).dupStore) {
                     signatureIndex = constants.addConstantUtf8("Ljava/lang/Object;");
                     localVariables.add(new LocalVariable(astore.offset, 1, signatureIndex, signatureIndex, astore.index));
                     continue;
                  }

                  CheckLocalVariableRangesForIndexInstruction(code, localVariables, astore);
                  continue;
               }

               CheckLocalVariableRangesForIndexInstruction(code, localVariables, astore);
            }
            break;
         case 277:
         case 278:
            instruction = ((IncInstruction)instruction).value;
            if(instruction.opcode == 21 || instruction.opcode == 268) {
               CheckLocalVariableRangesForIndexInstruction(code, localVariables, (IndexInstruction)instruction);
            }
         }
      }

   }

   private static void CheckLocalVariableRangesForIndexInstruction(byte[] code, LocalVariables localVariables, IndexInstruction ii) {
      LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(ii.index, ii.offset);
      if(lv == null) {
         int nextOffset = ByteCodeUtil.NextInstructionOffset(code, ii.offset);
         lv = localVariables.getLocalVariableWithIndexAndOffset(ii.index, nextOffset);
         if(lv != null) {
            lv.updateRange(ii.offset);
         } else {
            lv = localVariables.searchLocalVariableWithIndexAndOffset(ii.index, ii.offset);
            if(lv != null) {
               lv.updateRange(ii.offset);
            }
         }
      } else {
         lv.updateRange(ii.offset);
      }

   }

   private static void AnalyzeMethodCode(ConstantPool constants, LocalVariables localVariables, List<Instruction> list, List<Instruction> listForAnalyze, String returnedSignature) {
      int length = listForAnalyze.size();
      int change = 0;

      while(change < length) {
         Instruction internalObjectSignatureIndex = (Instruction)listForAnalyze.get(change);
         switch(internalObjectSignatureIndex.opcode) {
         case 21:
         case 25:
         case 54:
         case 58:
         case 132:
         case 268:
         case 269:
            SubAnalyzeMethodCode(constants, localVariables, list, listForAnalyze, ((IndexInstruction)internalObjectSignatureIndex).index, change, returnedSignature);
         default:
            ++change;
         }
      }

      boolean var11;
      int var12;
      do {
         var11 = false;

         for(var12 = 0; var12 < length; ++var12) {
            Instruction i = (Instruction)listForAnalyze.get(var12);
            LoadInstruction load;
            switch(i.opcode) {
            case 54:
               StoreInstruction var15 = (StoreInstruction)i;
               if(var15.valueref.opcode == 21) {
                  var11 |= ReverseAnalyzeIStore(localVariables, var15);
               }
               break;
            case 179:
               PutStatic var14 = (PutStatic)i;
               switch(var14.valueref.opcode) {
               case 21:
               case 25:
                  load = (LoadInstruction)var14.valueref;
                  var11 |= ReverseAnalyzePutStaticPutField(constants, localVariables, var14, load);
               case 22:
               case 23:
               case 24:
               default:
                  continue;
               }
            case 181:
               PutField lv = (PutField)i;
               switch(lv.valueref.opcode) {
               case 21:
               case 25:
                  load = (LoadInstruction)lv.valueref;
                  var11 |= ReverseAnalyzePutStaticPutField(constants, localVariables, lv, load);
               case 22:
               case 23:
               case 24:
               }
            }
         }
      } while(var11);

      var12 = constants.addConstantUtf8("Ljava/lang/Object;");
      length = localVariables.size();

      int var13;
      LocalVariable var16;
      for(var13 = 0; var13 < length; ++var13) {
         var16 = localVariables.getLocalVariableAt(var13);
         switch(var16.signature_index) {
         case -3:
            var16.signature_index = var12;
            break;
         case -2:
            var16.signature_index = constants.addConstantUtf8(SignatureUtil.GetSignatureFromTypesBitField(var16.typesBitField));
            break;
         case -1:
            var16.signature_index = constants.addConstantUtf8("Ljava/lang/Object;");
         }
      }

      for(var13 = 0; var13 < length; ++var13) {
         var16 = localVariables.getLocalVariableAt(var13);
         if(var16.signature_index == var12) {
            AddCastInstruction(constants, list, localVariables, var16);
         }
      }

   }

   private static void SubAnalyzeMethodCode(ConstantPool constants, LocalVariables localVariables, List<Instruction> list, List<Instruction> listForAnalyze, int varIndex, int startIndex, String returnedSignature) {
      IndexInstruction firstInstruction = (IndexInstruction)listForAnalyze.get(startIndex);
      LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(firstInstruction.index, firstInstruction.offset);
      if(lv != null) {
         if(firstInstruction.opcode == 58) {
            AStore var14 = (AStore)firstInstruction;
            if(var14.valueref.opcode == 270) {
               lv.exceptionOrReturnAddress = true;
            }
         }

      } else {
         int length = listForAnalyze.size();

         for(int i = startIndex; i < length; ++i) {
            Instruction instruction = (Instruction)listForAnalyze.get(i);
            switch(instruction.opcode) {
            case 25:
               if(((IndexInstruction)instruction).index == varIndex) {
                  AnalyzeALoad(localVariables, instruction);
               }
               break;
            case 54:
               if(((IndexInstruction)instruction).index == varIndex) {
                  AnalyzeIStore(constants, localVariables, instruction);
               }
               break;
            case 58:
               if(((IndexInstruction)instruction).index == varIndex) {
                  AnalyzeAStore(constants, localVariables, instruction);
               }
               break;
            case 182:
            case 183:
            case 184:
            case 185:
               AnalyzeInvokeInstruction(constants, localVariables, instruction, varIndex);
               break;
            case 261:
               IfCmp ic = (IfCmp)instruction;
               AnalyzeBinaryOperator(constants, localVariables, instruction, ic.value1, ic.value2, varIndex);
               break;
            case 267:
               BinaryOperatorInstruction boi = (BinaryOperatorInstruction)instruction;
               AnalyzeBinaryOperator(constants, localVariables, instruction, boi.value1, boi.value2, varIndex);
               break;
            case 268:
            case 270:
               if(((IndexInstruction)instruction).index == varIndex) {
                  AnalyzeLoad(localVariables, instruction);
               }
               break;
            case 269:
               if(((IndexInstruction)instruction).index == varIndex) {
                  AnalyzeStore(constants, localVariables, instruction);
               }
               break;
            case 273:
               AnalyzeReturnInstruction(constants, localVariables, instruction, varIndex, returnedSignature);
               break;
            case 277:
            case 278:
               instruction = ((IncInstruction)instruction).value;
               if(instruction.opcode != 21 && instruction.opcode != 268) {
                  break;
               }
            case 21:
            case 132:
               if(((IndexInstruction)instruction).index == varIndex) {
                  AnalyzeILoad(localVariables, instruction);
               }
            }
         }

      }
   }

   private static void AnalyzeIStore(ConstantPool constants, LocalVariables localVariables, Instruction instruction) {
      StoreInstruction store = (StoreInstruction)instruction;
      int index = store.index;
      int offset = store.offset;
      LocalVariable lv = localVariables.searchLocalVariableWithIndexAndOffset(index, offset);
      String signature = store.getReturnedSignature(constants, localVariables);
      int typesBitField;
      if(lv == null) {
         if(signature == null) {
            if(store.valueref.opcode == 21) {
               ILoad signatureLV = (ILoad)store.valueref;
               lv = localVariables.getLocalVariableWithIndexAndOffset(signatureLV.index, signatureLV.offset);
               typesBitField = lv == null?31:lv.typesBitField;
            } else {
               typesBitField = 31;
            }
         } else {
            typesBitField = SignatureUtil.CreateTypesBitField(signature);
         }

         localVariables.add(new LocalVariable(offset, 1, -1, -2, index, typesBitField));
      } else if(signature == null) {
         lv.updateRange(offset);
      } else {
         typesBitField = SignatureUtil.CreateTypesBitField(signature);
         switch(lv.signature_index) {
         case -3:
         case -1:
            localVariables.add(new LocalVariable(offset, 1, -1, -2, index, typesBitField));
            break;
         case -2:
            if((typesBitField & lv.typesBitField) != 0) {
               lv.typesBitField &= typesBitField;
               lv.updateRange(offset);
            } else {
               localVariables.add(new LocalVariable(offset, 1, -1, -2, index, typesBitField));
            }
            break;
         default:
            String signatureLV1 = constants.getConstantUtf8(lv.signature_index);
            int typesBitFieldLV = SignatureUtil.CreateTypesBitField(signatureLV1);
            if((typesBitField & typesBitFieldLV) != 0) {
               lv.updateRange(offset);
            } else {
               localVariables.add(new LocalVariable(offset, 1, -1, -2, index, typesBitField));
            }
         }
      }

   }

   private static void AnalyzeILoad(LocalVariables localVariables, Instruction instruction) {
      IndexInstruction load = (IndexInstruction)instruction;
      int index = load.index;
      int offset = load.offset;
      LocalVariable lv = localVariables.searchLocalVariableWithIndexAndOffset(index, offset);
      if(lv == null) {
         localVariables.add(new LocalVariable(offset, 1, -1, -2, index, 31));
      } else {
         lv.updateRange(offset);
      }

   }

   private static void AnalyzeLoad(LocalVariables localVariables, Instruction instruction) {
      IndexInstruction load = (IndexInstruction)instruction;
      int index = load.index;
      int offset = load.offset;
      LocalVariable lv = localVariables.searchLocalVariableWithIndexAndOffset(index, offset);
      if(lv == null) {
         localVariables.add(new LocalVariable(offset, 1, -1, -1, index));
      } else {
         lv.updateRange(offset);
      }

   }

   private static void AnalyzeALoad(LocalVariables localVariables, Instruction instruction) {
      IndexInstruction load = (IndexInstruction)instruction;
      int index = load.index;
      int offset = load.offset;
      LocalVariable lv = localVariables.searchLocalVariableWithIndexAndOffset(index, offset);
      if(lv == null) {
         localVariables.add(new LocalVariable(offset, 1, -1, -1, index));
      } else {
         lv.updateRange(offset);
      }

   }

   private static void AnalyzeInvokeInstruction(ConstantPool constants, LocalVariables localVariables, Instruction instruction, int varIndex) {
      InvokeInstruction invokeInstruction = (InvokeInstruction)instruction;
      List args = invokeInstruction.args;
      List argSignatures = invokeInstruction.getListOfParameterSignatures(constants);
      int nbrOfArgs = args.size();

      for(int j = 0; j < nbrOfArgs; ++j) {
         AnalyzeArgOrReturnedInstruction(constants, localVariables, (Instruction)args.get(j), varIndex, (String)argSignatures.get(j));
      }

   }

   private static void AnalyzeArgOrReturnedInstruction(ConstantPool constants, LocalVariables localVariables, Instruction instruction, int varIndex, String signature) {
      LoadInstruction li;
      LocalVariable lv;
      switch(instruction.opcode) {
      case 21:
         li = (LoadInstruction)instruction;
         if(li.index == varIndex) {
            lv = localVariables.searchLocalVariableWithIndexAndOffset(li.index, li.offset);
            if(lv != null) {
               lv.typesBitField &= SignatureUtil.CreateArgOrReturnBitFields(signature);
            }
         }
      case 22:
      case 23:
      case 24:
      default:
         break;
      case 25:
         li = (LoadInstruction)instruction;
         if(li.index == varIndex) {
            lv = localVariables.searchLocalVariableWithIndexAndOffset(li.index, li.offset);
            if(lv != null) {
               switch(lv.signature_index) {
               case -2:
                  (new Throwable("type inattendu")).printStackTrace();
                  break;
               case -1:
                  lv.signature_index = constants.addConstantUtf8(signature);
               }
            }
         }
      }

   }

   private static void AnalyzeBinaryOperator(ConstantPool constants, LocalVariables localVariables, Instruction instruction, Instruction i1, Instruction i2, int varIndex) {
      if(i1.opcode == 21 && ((ILoad)i1).index == varIndex || i2.opcode == 21 && ((ILoad)i2).index == varIndex) {
         LocalVariable lv1 = i1.opcode == 21?localVariables.searchLocalVariableWithIndexAndOffset(((ILoad)i1).index, i1.offset):null;
         LocalVariable lv2 = i2.opcode == 21?localVariables.searchLocalVariableWithIndexAndOffset(((ILoad)i2).index, i2.offset):null;
         String signature;
         int type;
         if(lv1 != null) {
            lv1.updateRange(instruction.offset);
            if(lv2 != null) {
               lv2.updateRange(instruction.offset);
            }

            if(lv1.signature_index == -2) {
               if(lv2 != null) {
                  if(lv2.signature_index == -2) {
                     lv1.typesBitField &= lv2.typesBitField;
                     lv2.typesBitField &= lv1.typesBitField;
                  } else {
                     lv1.signature_index = lv2.signature_index;
                  }
               } else {
                  signature = i2.getReturnedSignature(constants, localVariables);
                  if(SignatureUtil.IsIntegerSignature(signature)) {
                     type = SignatureUtil.CreateTypesBitField(signature);
                     if(type != 0) {
                        lv1.typesBitField &= type;
                     }
                  }
               }
            } else if(lv2 != null && lv2.signature_index == -2) {
               lv2.signature_index = lv1.signature_index;
            }
         } else if(lv2 != null) {
            lv2.updateRange(instruction.offset);
            if(lv2.signature_index == -2) {
               signature = i1.getReturnedSignature(constants, localVariables);
               if(SignatureUtil.IsIntegerSignature(signature)) {
                  type = SignatureUtil.CreateTypesBitField(signature);
                  if(type != 0) {
                     lv2.typesBitField &= type;
                  }
               }
            }
         }

      }
   }

   private static void AnalyzeReturnInstruction(ConstantPool constants, LocalVariables localVariables, Instruction instruction, int varIndex, String returnedSignature) {
      ReturnInstruction ri = (ReturnInstruction)instruction;
      AnalyzeArgOrReturnedInstruction(constants, localVariables, ri.valueref, varIndex, returnedSignature);
   }

   private static void AnalyzeStore(ConstantPool constants, LocalVariables localVariables, Instruction instruction) {
      StoreInstruction store = (StoreInstruction)instruction;
      int index = store.index;
      int offset = store.offset;
      LocalVariable lv = localVariables.searchLocalVariableWithIndexAndOffset(index, offset);
      String signature = instruction.getReturnedSignature(constants, localVariables);
      int signatureIndex = signature != null?constants.addConstantUtf8(signature):-1;
      if(lv == null) {
         localVariables.add(new LocalVariable(offset, 1, -1, signatureIndex, index));
      } else if(lv.signature_index == signatureIndex) {
         lv.updateRange(offset);
      } else {
         localVariables.add(new LocalVariable(offset, 1, -1, signatureIndex, index));
      }

   }

   private static void AnalyzeAStore(ConstantPool constants, LocalVariables localVariables, Instruction instruction) {
      StoreInstruction store = (StoreInstruction)instruction;
      int index = store.index;
      int offset = store.offset;
      LocalVariable lv = localVariables.searchLocalVariableWithIndexAndOffset(index, offset);
      String signatureInstruction = instruction.getReturnedSignature(constants, localVariables);
      int signatureInstructionIndex = signatureInstruction != null?constants.addConstantUtf8(signatureInstruction):-1;
      boolean isExceptionOrReturnAddress = store.valueref.opcode == 270 || store.valueref.opcode == 279;
      if(lv == null || lv.exceptionOrReturnAddress || isExceptionOrReturnAddress && lv.start_pc + lv.length < offset) {
         localVariables.add(new LocalVariable(offset, 1, -1, signatureInstructionIndex, index, isExceptionOrReturnAddress));
      } else if(!isExceptionOrReturnAddress) {
         if(lv.signature_index == -1) {
            lv.signature_index = signatureInstructionIndex;
            lv.updateRange(offset);
         } else if(lv.signature_index == -2) {
            localVariables.add(new LocalVariable(offset, 1, -1, signatureInstructionIndex, index));
         } else if(lv.signature_index != signatureInstructionIndex && lv.signature_index != -3) {
            String signatureLV = constants.getConstantUtf8(lv.signature_index);
            if(SignatureUtil.IsPrimitiveSignature(signatureLV)) {
               localVariables.add(new LocalVariable(offset, 1, -1, signatureInstructionIndex, index));
            } else if(signatureInstructionIndex != -1) {
               lv.signature_index = -3;
               lv.updateRange(offset);
            } else {
               lv.updateRange(offset);
            }
         } else {
            lv.updateRange(offset);
         }
      }

   }

   private static void SetConstantTypes(ClassFile classFile, ConstantPool constants, Method method, LocalVariables localVariables, List<Instruction> list, List<Instruction> listForAnalyze, String returnedSignature) {
      int length = listForAnalyze.size();

      int i;
      Instruction instruction;
      for(i = 0; i < length; ++i) {
         instruction = (Instruction)listForAnalyze.get(i);
         switch(instruction.opcode) {
         case 54:
            SetConstantTypesIStore(constants, localVariables, instruction);
            break;
         case 179:
            PutStatic var13 = (PutStatic)instruction;
            SetConstantTypesPutFieldAndPutStatic(constants, localVariables, var13.valueref, var13.index);
            break;
         case 181:
            PutField var12 = (PutField)instruction;
            SetConstantTypesPutFieldAndPutStatic(constants, localVariables, var12.valueref, var12.index);
            break;
         case 182:
         case 183:
         case 184:
         case 185:
         case 274:
            SetConstantTypesInvokeInstruction(constants, instruction);
            break;
         case 261:
            IfCmp var11 = (IfCmp)instruction;
            SetConstantTypesBinaryOperator(constants, localVariables, var11.value1, var11.value2);
            break;
         case 267:
            BinaryOperatorInstruction tos = (BinaryOperatorInstruction)instruction;
            SetConstantTypesBinaryOperator(constants, localVariables, tos.value1, tos.value2);
            break;
         case 272:
            SetConstantTypesArrayStore(constants, localVariables, (ArrayStoreInstruction)instruction);
            break;
         case 273:
            SetConstantTypesXReturn(instruction, returnedSignature);
         }
      }

      for(i = 0; i < length; ++i) {
         instruction = (Instruction)listForAnalyze.get(i);
         if(instruction.opcode == 280) {
            TernaryOpStore var14 = (TernaryOpStore)instruction;
            SetConstantTypesTernaryOpStore(constants, localVariables, list, var14);
         }
      }

   }

   private static void SetConstantTypesInvokeInstruction(ConstantPool constants, Instruction instruction) {
      InvokeInstruction invokeInstruction = (InvokeInstruction)instruction;
      List args = invokeInstruction.args;
      List types = invokeInstruction.getListOfParameterSignatures(constants);
      int nbrOfArgs = args.size();
      int j = 0;

      while(j < nbrOfArgs) {
         Instruction arg = (Instruction)args.get(j);
         switch(arg.opcode) {
         case 16:
         case 17:
         case 256:
            ((IConst)arg).setReturnedSignature((String)types.get(j));
         default:
            ++j;
         }
      }

   }

   private static void SetConstantTypesPutFieldAndPutStatic(ConstantPool constants, LocalVariables localVariables, Instruction valueref, int index) {
      switch(valueref.opcode) {
      case 16:
      case 17:
      case 256:
         ConstantFieldref cfr = constants.getConstantFieldref(index);
         ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
         String signature = constants.getConstantUtf8(cnat.descriptor_index);
         ((IConst)valueref).setReturnedSignature(signature);
      default:
      }
   }

   private static void SetConstantTypesTernaryOpStore(ConstantPool constants, LocalVariables localVariables, List<Instruction> list, TernaryOpStore tos) {
      switch(tos.objectref.opcode) {
      case 16:
      case 17:
      case 256:
         int index = InstructionUtil.getIndexForOffset(list, tos.ternaryOp2ndValueOffset);
         if(index != -1) {
            for(int length = list.size(); index < length; ++index) {
               Instruction result = SearchInstructionByOffsetVisitor.visit((Instruction)list.get(index), tos.ternaryOp2ndValueOffset);
               if(result != null) {
                  String signature = result.getReturnedSignature(constants, localVariables);
                  ((IConst)tos.objectref).setReturnedSignature(signature);
                  break;
               }
            }
         }
      default:
      }
   }

   private static void SetConstantTypesArrayStore(ConstantPool constants, LocalVariables localVariables, ArrayStoreInstruction asi) {
      switch(asi.valueref.opcode) {
      case 16:
      case 17:
      case 256:
         switch(asi.arrayref.opcode) {
         case 25:
            ALoad ii1 = (ALoad)asi.arrayref;
            LocalVariable cfr1 = localVariables.getLocalVariableWithIndexAndOffset(ii1.index, ii1.offset);
            if(cfr1 == null) {
               (new Throwable("lv is null. index=" + ii1.index)).printStackTrace();
               return;
            } else {
               String cnat1 = constants.getConstantUtf8(cfr1.signature_index);
               ((IConst)asi.valueref).setReturnedSignature(SignatureUtil.CutArrayDimensionPrefix(cnat1));
               break;
            }
         case 178:
         case 180:
            IndexInstruction ii = (IndexInstruction)asi.arrayref;
            ConstantFieldref cfr = constants.getConstantFieldref(ii.index);
            ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
            String signature = constants.getConstantUtf8(cnat.descriptor_index);
            ((IConst)asi.valueref).setReturnedSignature(SignatureUtil.CutArrayDimensionPrefix(signature));
         }
      default:
      }
   }

   private static void SetConstantTypesIStore(ConstantPool constants, LocalVariables localVariables, Instruction instruction) {
      StoreInstruction store = (StoreInstruction)instruction;
      switch(store.valueref.opcode) {
      case 16:
      case 17:
      case 256:
         LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(store.index, store.offset);
         String signature = constants.getConstantUtf8(lv.signature_index);
         ((IConst)store.valueref).setReturnedSignature(signature);
      default:
      }
   }

   private static void SetConstantTypesBinaryOperator(ConstantPool constants, LocalVariables localVariables, Instruction i1, Instruction i2) {
      String signature;
      switch(i1.opcode) {
      case 16:
      case 17:
      case 256:
         switch(i2.opcode) {
         case 16:
         case 17:
         case 256:
            return;
         default:
            signature = i2.getReturnedSignature(constants, localVariables);
            if(signature != null) {
               ((IConst)i1).setReturnedSignature(signature);
            }

            return;
         }
      default:
         switch(i2.opcode) {
         case 16:
         case 17:
         case 256:
            signature = i1.getReturnedSignature(constants, localVariables);
            if(signature != null) {
               ((IConst)i2).setReturnedSignature(signature);
            }
         }
      }

   }

   private static void SetConstantTypesXReturn(Instruction instruction, String returnedSignature) {
      ReturnInstruction ri = (ReturnInstruction)instruction;
      int opcode = ri.valueref.opcode;
      if(opcode == 17 || opcode == 16 || opcode == 256) {
         ((IConst)ri.valueref).signature = returnedSignature;
      }
   }

   private static String GetReturnedSignature(ClassFile classFile, Method method) {
      AttributeSignature as = method.getAttributeSignature();
      int signatureIndex = as == null?method.descriptor_index:as.signature_index;
      String signature = classFile.getConstantPool().getConstantUtf8(signatureIndex);
      return SignatureUtil.GetMethodReturnedSignature(signature);
   }

   private static void InitialyzeExceptionLoad(List<Instruction> listForAnalyze, LocalVariables localVariables) {
      int length = listForAnalyze.size();

      int index;
      Instruction i;
      for(index = 0; index < length; ++index) {
         i = (Instruction)listForAnalyze.get(index);
         if(i.opcode == 58) {
            AStore el = (AStore)i;
            if(el.valueref.opcode == 270) {
               ExceptionLoad varIndex = (ExceptionLoad)el.valueref;
               if(varIndex.index == -1) {
                  varIndex.index = el.index;
               }
            }
         }
      }

      for(index = 0; index < length; ++index) {
         i = (Instruction)listForAnalyze.get(index);
         if(i.opcode == 270) {
            ExceptionLoad var8 = (ExceptionLoad)i;
            if(var8.index == -1 && var8.exceptionNameIndex > 0) {
               int var9 = localVariables.size();
               LocalVariable localVariable = new LocalVariable(var8.offset, 1, -1, var8.exceptionNameIndex, var9, true);
               localVariables.add(localVariable);
               var8.index = var9;
            }
         }
      }

   }

   private static void GenerateLocalVariableNames(ConstantPool constants, LocalVariables localVariables, DefaultVariableNameGenerator variableNameGenerator) {
      int length = localVariables.size();

      for(int i = localVariables.getIndexOfFirstLocalVariable(); i < length; ++i) {
         LocalVariable lv = localVariables.getLocalVariableAt(i);
         if(lv != null && lv.name_index <= 0) {
            String signature = constants.getConstantUtf8(lv.signature_index);
            boolean appearsOnce = SignatureAppearsOnceInLocalVariables(localVariables, length, lv.signature_index);
            String name = variableNameGenerator.generateLocalVariableNameFromSignature(signature, appearsOnce);
            lv.name_index = constants.addConstantUtf8(name);
         }
      }

   }

   private static boolean SignatureAppearsOnceInParameters(List<String> parameterTypes, int firstIndex, int length, String signature) {
      int counter = 0;

      for(int i = firstIndex; i < length && counter < 2; ++i) {
         if(signature.equals(parameterTypes.get(i))) {
            ++counter;
         }
      }

      return counter <= 1;
   }

   private static boolean SignatureAppearsOnceInLocalVariables(LocalVariables localVariables, int length, int signature_index) {
      int counter = 0;

      for(int i = localVariables.getIndexOfFirstLocalVariable(); i < length && counter < 2; ++i) {
         LocalVariable lv = localVariables.getLocalVariableAt(i);
         if(lv != null && lv.signature_index == signature_index) {
            ++counter;
         }
      }

      return counter == 1;
   }

   private static boolean ReverseAnalyzeIStore(LocalVariables localVariables, StoreInstruction si) {
      LoadInstruction load = (LoadInstruction)si.valueref;
      LocalVariable lvLoad = localVariables.getLocalVariableWithIndexAndOffset(load.index, load.offset);
      if(lvLoad != null && lvLoad.signature_index == -2) {
         LocalVariable lvStore = localVariables.getLocalVariableWithIndexAndOffset(si.index, si.offset);
         if(lvStore == null) {
            return false;
         } else if(lvStore.signature_index == -2) {
            int old = lvLoad.typesBitField;
            lvLoad.typesBitField &= lvStore.typesBitField;
            return old != lvLoad.typesBitField;
         } else if(lvStore.signature_index >= 0 && lvStore.signature_index != lvLoad.signature_index) {
            lvLoad.signature_index = lvStore.signature_index;
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean ReverseAnalyzePutStaticPutField(ConstantPool constants, LocalVariables localVariables, IndexInstruction ii, LoadInstruction load) {
      LocalVariable lvLoad = localVariables.getLocalVariableWithIndexAndOffset(load.index, load.offset);
      if(lvLoad != null) {
         ConstantFieldref cfr = constants.getConstantFieldref(ii.index);
         ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
         if(lvLoad.signature_index == -2) {
            String descriptor = constants.getConstantUtf8(cnat.descriptor_index);
            int typesBitField = SignatureUtil.CreateArgOrReturnBitFields(descriptor);
            int old = lvLoad.typesBitField;
            lvLoad.typesBitField &= typesBitField;
            if(old != lvLoad.typesBitField) {
               return true;
            }

            return false;
         }

         if(lvLoad.signature_index == -1) {
            lvLoad.signature_index = cnat.descriptor_index;
            return true;
         }
      }

      return false;
   }

   private static void AddCastInstruction(ConstantPool constants, List<Instruction> list, LocalVariables localVariables, LocalVariable lv) {
      AddCheckCastVisitor visitor = new AddCheckCastVisitor(constants, localVariables, lv);
      int length = list.size();

      for(int i = 0; i < length; ++i) {
         visitor.visit((Instruction)list.get(i));
      }

   }
}
