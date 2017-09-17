package jd.core.process.analyzer.classfile.reconstructor;

import java.util.ArrayList;
import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokespecial;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.process.analyzer.classfile.visitor.CompareInstructionVisitor;
import jd.core.process.analyzer.classfile.visitor.SearchInstructionByOpcodeVisitor;

public class InitInstanceFieldsReconstructor {
   public static void Reconstruct(ClassFile classFile) {
      ArrayList putFieldList = new ArrayList();
      ConstantPool constants = classFile.getConstantPool();
      Method[] methods = classFile.getMethods();
      if(methods != null) {
         int methodIndex = methods.length;
         Method putFieldListMethod = null;

         int fieldLength;
         int putFieldListIndex;
         Instruction index;
         int var27;
         int var29;
         int var32;
         while(methodIndex > 0) {
            --methodIndex;
            Method visitor = methods[methodIndex];
            if((visitor.access_flags & 4160) == 0 && visitor.getCode() != null && visitor.getFastNodes() != null && !visitor.containsError() && visitor.name_index == constants.instanceConstructorIndex) {
               List putFieldListLength = visitor.getFastNodes();
               if(putFieldListLength != null) {
                  int fields = putFieldListLength.size();
                  if(fields <= 0) {
                     break;
                  }

                  fieldLength = GetSuperCallIndex(classFile, constants, putFieldListLength);
                  if(fieldLength >= 0) {
                     ++fieldLength;
                     putFieldListIndex = fieldLength > 0?((Instruction)putFieldListLength.get(fieldLength - 1)).lineNumber:Instruction.UNKNOWN_LINE_NUMBER;

                     Instruction method;
                     for(method = null; fieldLength < fields; putFieldListMethod = visitor) {
                        method = (Instruction)putFieldListLength.get(fieldLength++);
                        if(method.opcode != 181) {
                           break;
                        }

                        PutField list = (PutField)method;
                        ConstantFieldref length = constants.getConstantFieldref(list.index);
                        if(length.class_index != classFile.getThisClassIndex() || list.objectref.opcode != 25) {
                           break;
                        }

                        ALoad putFieldIndex = (ALoad)list.objectref;
                        if(putFieldIndex.index != 0) {
                           break;
                        }

                        index = SearchInstructionByOpcodeVisitor.visit((Instruction)list.valueref, 25);
                        if(index != null && ((ALoad)index).index != 0 || SearchInstructionByOpcodeVisitor.visit((Instruction)list.valueref, 268) != null || SearchInstructionByOpcodeVisitor.visit((Instruction)list.valueref, 21) != null) {
                           break;
                        }

                        putFieldList.add(list);
                     }

                     if(putFieldListIndex != Instruction.UNKNOWN_LINE_NUMBER && method != null) {
                        var27 = putFieldList.size();
                        var29 = method.lineNumber;
                        if(method.opcode != 177 || fieldLength != fields || var27 == 0 || var29 != ((PutField)putFieldList.get(var27 - 1)).lineNumber) {
                           while(var27-- > 0) {
                              var32 = ((PutField)putFieldList.get(var27)).lineNumber;
                              if(putFieldListIndex <= var32 && var32 <= var29) {
                                 putFieldList.remove(var27);
                              }
                           }
                        }
                     }
                     break;
                  }
               }
            }
         }

         CompareInstructionVisitor var20 = new CompareInstructionVisitor();

         while(true) {
            while(true) {
               List var23;
               do {
                  do {
                     Method var21;
                     do {
                        do {
                           do {
                              if(methodIndex <= 0) {
                                 int var22 = putFieldList.size();
                                 Field[] var24 = classFile.getFields();
                                 if(var22 > 0 && var24 != null) {
                                    fieldLength = var24.length;
                                    putFieldListIndex = var22;

                                    Instruction instruction;
                                    while(putFieldListIndex-- > 0) {
                                       PutField var26 = (PutField)putFieldList.get(putFieldListIndex);
                                       ConstantFieldref var30 = constants.getConstantFieldref(var26.index);
                                       ConstantNameAndType var31 = constants.getConstantNameAndType(var30.name_and_type_index);

                                       for(var32 = 0; var32 < fieldLength; ++var32) {
                                          Field var35 = var24[var32];
                                          if(var31.name_index == var35.name_index && var31.descriptor_index == var35.descriptor_index && (var35.access_flags & 8) == 0) {
                                             instruction = var26.valueref;
                                             var35.setValueAndMethod(instruction, putFieldListMethod);
                                             if(instruction.opcode == 283) {
                                                instruction.opcode = 282;
                                             }
                                             break;
                                          }
                                       }

                                       if(var32 == fieldLength) {
                                          putFieldList.remove(putFieldListIndex);
                                          --var22;
                                       }
                                    }

                                    if(var22 > 0) {
                                       methodIndex = methods.length;

                                       while(true) {
                                          List var33;
                                          do {
                                             Method var28;
                                             do {
                                                do {
                                                   do {
                                                      if(methodIndex-- <= 0) {
                                                         return;
                                                      }

                                                      var28 = methods[methodIndex];
                                                   } while((var28.access_flags & 4160) != 0);
                                                } while(var28.getCode() == null);
                                             } while(var28.name_index != constants.instanceConstructorIndex);

                                             var33 = var28.getFastNodes();
                                             var29 = var33.size();
                                          } while(var29 <= 0);

                                          putFieldListIndex = 0;
                                          var32 = ((PutField)putFieldList.get(putFieldListIndex)).index;

                                          for(int var36 = 0; var36 < var29; ++var36) {
                                             instruction = (Instruction)var33.get(var36);
                                             if(instruction.opcode == 181) {
                                                PutField putField = (PutField)instruction;
                                                if(putField.index == var32) {
                                                   ConstantFieldref cfr = constants.getConstantFieldref(putField.index);
                                                   if(cfr.class_index == classFile.getThisClassIndex() && putField.objectref.opcode == 25) {
                                                      ALoad aLaod = (ALoad)putField.objectref;
                                                      if(aLaod.index == 0) {
                                                         var33.remove(var36--);
                                                         --var29;
                                                         ++putFieldListIndex;
                                                         if(putFieldListIndex >= var22) {
                                                            break;
                                                         }

                                                         var32 = ((PutField)putFieldList.get(putFieldListIndex)).index;
                                                      }
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }

                                 return;
                              }

                              --methodIndex;
                              var21 = methods[methodIndex];
                           } while((var21.access_flags & 4160) != 0);
                        } while(var21.getCode() == null);
                     } while(var21.name_index != constants.instanceConstructorIndex);

                     var23 = var21.getFastNodes();
                     fieldLength = var23.size();
                  } while(fieldLength <= 0);

                  putFieldListIndex = GetSuperCallIndex(classFile, constants, var23);
               } while(putFieldListIndex < 0);

               int var25 = putFieldListIndex + 1;
               var27 = putFieldList.size();

               while(var25 + var27 > fieldLength) {
                  --var27;
                  putFieldList.remove(var27);
               }

               for(var29 = 0; var29 < var27; ++var29) {
                  Instruction var34 = (Instruction)putFieldList.get(var29);
                  index = (Instruction)var23.get(var25 + var29);
                  if(var34.lineNumber != index.lineNumber || !var20.visit(var34, index)) {
                     while(var29 < var27) {
                        --var27;
                        putFieldList.remove(var27);
                     }
                     break;
                  }
               }
            }
         }
      }
   }

   private static int GetSuperCallIndex(ClassFile classFile, ConstantPool constants, List<Instruction> list) {
      int length = list.size();

      for(int i = 0; i < length; ++i) {
         Instruction instruction = (Instruction)list.get(i);
         if(instruction.opcode == 183) {
            Invokespecial is = (Invokespecial)instruction;
            if(is.objectref.opcode == 25 && ((ALoad)is.objectref).index == 0) {
               ConstantMethodref cmr = constants.getConstantMethodref(is.index);
               ConstantNameAndType cnat = constants.getConstantNameAndType(cmr.name_and_type_index);
               if(cnat.name_index == constants.instanceConstructorIndex) {
                  if(cmr.class_index == classFile.getThisClassIndex()) {
                     return -1;
                  }

                  return i;
               }
            }
         }
      }

      return -1;
   }
}
