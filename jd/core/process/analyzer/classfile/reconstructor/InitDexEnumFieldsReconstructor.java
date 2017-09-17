package jd.core.process.analyzer.classfile.reconstructor;

import java.util.ArrayList;
import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.AAStore;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.fast.instruction.FastDeclaration;

public class InitDexEnumFieldsReconstructor {
   public static void Reconstruct(ClassFile classFile) {
      Method method = classFile.getStaticMethod();
      if(method != null) {
         Field[] fields = classFile.getFields();
         if(fields != null) {
            List list = method.getFastNodes();
            if(list != null) {
               ConstantPool constants = classFile.getConstantPool();
               int indexInstruction = list.size();
               if(indexInstruction > 0) {
                  --indexInstruction;

                  while(true) {
                     PutStatic putStatic;
                     Field field;
                     int index;
                     ArrayList values;
                     int valuesLength;
                     FastDeclaration var21;
                     do {
                        int localEnumArrayIndex;
                        AStore astore;
                        do {
                           do {
                              Instruction instruction;
                              label86:
                              do {
                                 while(indexInstruction-- > 0) {
                                    instruction = (Instruction)list.get(indexInstruction);
                                    if(instruction.opcode != 179) {
                                       return;
                                    }

                                    putStatic = (PutStatic)instruction;
                                    if(putStatic.valueref.opcode != 25) {
                                       return;
                                    }

                                    ConstantFieldref cfr = constants.getConstantFieldref(putStatic.index);
                                    if(cfr.class_index != classFile.getThisClassIndex()) {
                                       return;
                                    }

                                    ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
                                    String name = constants.getConstantUtf8(cnat.name_index);
                                    if(!name.equals("ENUM$VALUES")) {
                                       return;
                                    }

                                    int indexField = fields.length;

                                    while(indexField-- > 0) {
                                       field = fields[indexField];
                                       if((field.access_flags & 4122) == 4122 && cnat.descriptor_index == field.descriptor_index && cnat.name_index == field.name_index) {
                                          ALoad aload = (ALoad)putStatic.valueref;
                                          localEnumArrayIndex = aload.index;
                                          index = indexInstruction;
                                          values = new ArrayList();

                                          while(index-- > 0) {
                                             instruction = (Instruction)list.get(index);
                                             if(instruction.opcode != 83) {
                                                continue label86;
                                             }

                                             AAStore declaration = (AAStore)instruction;
                                             if(declaration.arrayref.opcode != 25 || declaration.valueref.opcode != 178 || ((ALoad)declaration.arrayref).index != localEnumArrayIndex) {
                                                continue label86;
                                             }

                                             values.add(declaration.valueref);
                                          }
                                          continue label86;
                                       }
                                    }
                                 }

                                 return;
                              } while(instruction.opcode != 317);

                              var21 = (FastDeclaration)instruction;
                           } while(var21.instruction.opcode != 58);

                           astore = (AStore)var21.instruction;
                        } while(astore.index != localEnumArrayIndex);

                        valuesLength = values.size();
                     } while(valuesLength <= 0);

                     InitArrayInstruction iai = new InitArrayInstruction(282, putStatic.offset, var21.lineNumber, new ANewArray(189, putStatic.offset, var21.lineNumber, classFile.getThisClassIndex(), new IConst(256, putStatic.offset, var21.lineNumber, valuesLength)), values);
                     field.setValueAndMethod(iai, method);
                     list.remove(indexInstruction);

                     while(true) {
                        --indexInstruction;
                        if(indexInstruction <= index) {
                           list.remove(indexInstruction);
                           break;
                        }

                        list.remove(indexInstruction);
                     }
                  }
               }
            }
         }
      }
   }
}
