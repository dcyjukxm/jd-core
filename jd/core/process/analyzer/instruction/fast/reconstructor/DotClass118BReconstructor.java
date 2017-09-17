package jd.core.process.analyzer.instruction.fast.reconstructor;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.classfile.constant.ConstantString;
import jd.core.model.classfile.constant.ConstantValue;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Ldc;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.visitor.ReplaceDupLoadVisitor;

public class DotClass118BReconstructor {
   public static void Reconstruct(ReferenceMap referenceMap, ClassFile classFile, List<Instruction> list) {
      int i = list.size();
      if(i >= 5) {
         i -= 4;
         ConstantPool constants = classFile.getConstantPool();

         while(true) {
            while(true) {
               DupStore ds;
               IfInstruction ii;
               ConstantNameAndType cnatField;
               Ldc ldc;
               ConstantValue cv;
               do {
                  Instruction instruction;
                  String nameMethod;
                  do {
                     ConstantMethodref cmr;
                     String className;
                     do {
                        Invokestatic is;
                        String nameField;
                        do {
                           String signature;
                           do {
                              ConstantFieldref cfr;
                              do {
                                 do {
                                    AssignmentInstruction ai;
                                    do {
                                       TernaryOpStore tos;
                                       do {
                                          do {
                                             FastTry ft;
                                             List catchInstructions;
                                             do {
                                                do {
                                                   do {
                                                      do {
                                                         do {
                                                            do {
                                                               Pop pop;
                                                               do {
                                                                  do {
                                                                     do {
                                                                        do {
                                                                           do {
                                                                              do {
                                                                                 do {
                                                                                    do {
                                                                                       do {
                                                                                          if(i-- <= 0) {
                                                                                             return;
                                                                                          }

                                                                                          instruction = (Instruction)list.get(i);
                                                                                       } while(instruction.opcode != 264);

                                                                                       ds = (DupStore)instruction;
                                                                                    } while(ds.objectref.opcode != 178);

                                                                                    GetStatic gs = (GetStatic)ds.objectref;
                                                                                    cfr = constants.getConstantFieldref(gs.index);
                                                                                 } while(cfr.class_index != classFile.getThisClassIndex());

                                                                                 instruction = (Instruction)list.get(i + 1);
                                                                              } while(instruction.opcode != 262);

                                                                              ii = (IfInstruction)instruction;
                                                                           } while(ii.value.opcode != 263);
                                                                        } while(ds.offset != ii.value.offset);

                                                                        instruction = (Instruction)list.get(i + 2);
                                                                     } while(instruction.opcode != 87);

                                                                     pop = (Pop)instruction;
                                                                  } while(pop.objectref.opcode != 263);
                                                               } while(ds.offset != pop.objectref.offset);

                                                               instruction = (Instruction)list.get(i + 3);
                                                            } while(instruction.opcode != 318);

                                                            ft = (FastTry)instruction;
                                                         } while(ft.finallyInstructions != null);
                                                      } while(ft.instructions.size() != 1);
                                                   } while(ft.catches.size() != 1);

                                                   catchInstructions = ((FastTry.FastCatch)ft.catches.get(0)).instructions;
                                                } while(catchInstructions.size() != 1);
                                             } while(((Instruction)catchInstructions.get(0)).opcode != 191);

                                             instruction = (Instruction)ft.instructions.get(0);
                                          } while(instruction.opcode != 280);

                                          tos = (TernaryOpStore)instruction;
                                       } while(tos.objectref.opcode != 265);

                                       ai = (AssignmentInstruction)tos.objectref;
                                    } while(ai.value2.opcode != 184);

                                    is = (Invokestatic)ai.value2;
                                 } while(is.args.size() != 1);

                                 instruction = (Instruction)is.args.get(0);
                              } while(instruction.opcode != 18);

                              cnatField = constants.getConstantNameAndType(cfr.name_and_type_index);
                              signature = constants.getConstantUtf8(cnatField.descriptor_index);
                           } while(!"Ljava/lang/Class;".equals(signature));

                           nameField = constants.getConstantUtf8(cnatField.name_index);
                        } while(!nameField.startsWith("class$"));

                        cmr = constants.getConstantMethodref(is.index);
                        className = constants.getConstantClassName(cmr.class_index);
                     } while(!className.equals("java/lang/Class"));

                     ConstantNameAndType cnatMethod = constants.getConstantNameAndType(cmr.name_and_type_index);
                     nameMethod = constants.getConstantUtf8(cnatMethod.name_index);
                  } while(!nameMethod.equals("forName"));

                  ldc = (Ldc)instruction;
                  cv = constants.getConstantValue(ldc.index);
               } while(cv.tag != 8);

               ConstantString cs = (ConstantString)cv;
               String dotClassName = constants.getConstantUtf8(cs.string_index);
               String internalName = dotClassName.replace('.', '/');
               referenceMap.add(internalName);
               int index = constants.addConstantUtf8(internalName);
               index = constants.addConstantClass(index);
               ldc = new Ldc(18, ii.offset, ii.lineNumber, index);
               ReplaceDupLoadVisitor visitor = new ReplaceDupLoadVisitor(ds, ldc);
               visitor.visit((Instruction)list.get(i + 4));
               list.remove(i + 3);
               list.remove(i + 2);
               list.remove(i + 1);
               list.remove(i);
               Field[] fields = classFile.getFields();
               int j = fields.length;

               while(j-- > 0) {
                  Field field = fields[j];
                  if(field.name_index == cnatField.name_index) {
                     field.access_flags |= 4096;
                     break;
                  }
               }
            }
         }
      }
   }
}
