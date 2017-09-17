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
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Ldc;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.visitor.ReplaceDupLoadVisitor;

public class DotClassEclipseReconstructor {
   public static void Reconstruct(ReferenceMap referenceMap, ClassFile classFile, List<Instruction> list) {
      int i = list.size();
      if(i >= 3) {
         i -= 2;
         ConstantPool constants = classFile.getConstantPool();

         while(true) {
            while(true) {
               IfInstruction ii;
               FastTry.FastCatch fc;
               ConstantNameAndType cnatField;
               DupStore ds;
               Ldc ldc;
               ConstantValue cv;
               String exceptionName;
               do {
                  do {
                     PutStatic ps;
                     do {
                        GetStatic gs;
                        do {
                           do {
                              Instruction instruction;
                              do {
                                 FastTry ft;
                                 do {
                                    String name;
                                    do {
                                       ConstantMethodref cmr;
                                       do {
                                          Invokestatic is;
                                          do {
                                             do {
                                                do {
                                                   do {
                                                      do {
                                                         String signature;
                                                         do {
                                                            ConstantFieldref cfr;
                                                            do {
                                                               int jumpOffset;
                                                               do {
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
                                                                                          } while(instruction.opcode != 262);

                                                                                          ii = (IfInstruction)instruction;
                                                                                       } while(ii.value.opcode != 178);

                                                                                       jumpOffset = ii.GetJumpOffset();
                                                                                       instruction = (Instruction)list.get(i + 1);
                                                                                    } while(instruction.opcode != 318);

                                                                                    ft = (FastTry)instruction;
                                                                                 } while(ft.catches.size() != 1);
                                                                              } while(ft.finallyInstructions != null);
                                                                           } while(ft.instructions.size() != 2);

                                                                           fc = (FastTry.FastCatch)ft.catches.get(0);
                                                                        } while(fc.instructions.size() != 1);
                                                                     } while(fc.otherExceptionTypeIndexes != null);

                                                                     instruction = (Instruction)list.get(i + 2);
                                                                  } while(ft.offset >= jumpOffset);
                                                               } while(jumpOffset > instruction.offset);

                                                               gs = (GetStatic)ii.value;
                                                               cfr = constants.getConstantFieldref(gs.index);
                                                            } while(cfr.class_index != classFile.getThisClassIndex());

                                                            cnatField = constants.getConstantNameAndType(cfr.name_and_type_index);
                                                            signature = constants.getConstantUtf8(cnatField.descriptor_index);
                                                         } while(!"Ljava/lang/Class;".equals(signature));

                                                         name = constants.getConstantUtf8(cnatField.name_index);
                                                      } while(!name.startsWith("class$"));

                                                      instruction = (Instruction)ft.instructions.get(0);
                                                   } while(instruction.opcode != 264);

                                                   ds = (DupStore)instruction;
                                                } while(ds.objectref.opcode != 184);

                                                is = (Invokestatic)ds.objectref;
                                             } while(is.args.size() != 1);

                                             instruction = (Instruction)is.args.get(0);
                                          } while(instruction.opcode != 18);

                                          cmr = constants.getConstantMethodref(is.index);
                                          name = constants.getConstantClassName(cmr.class_index);
                                       } while(!name.equals("java/lang/Class"));

                                       ConstantNameAndType cnatMethod = constants.getConstantNameAndType(cmr.name_and_type_index);
                                       name = constants.getConstantUtf8(cnatMethod.name_index);
                                    } while(!name.equals("forName"));

                                    ldc = (Ldc)instruction;
                                    cv = constants.getConstantValue(ldc.index);
                                 } while(cv.tag != 8);

                                 instruction = (Instruction)ft.instructions.get(1);
                              } while(instruction.opcode != 179);

                              ps = (PutStatic)instruction;
                           } while(ps.index != gs.index);
                        } while(ps.valueref.opcode != 263);
                     } while(ps.valueref.offset != ds.offset);

                     exceptionName = constants.getConstantClassName(fc.exceptionTypeIndex);
                  } while(!exceptionName.equals("Ljava/lang/ClassNotFoundException;"));
               } while(((Instruction)fc.instructions.get(0)).opcode != 191);

               ConstantString cs = (ConstantString)cv;
               String className = constants.getConstantUtf8(cs.string_index);
               String internalName = className.replace('.', '/');
               referenceMap.add(internalName);
               int index = constants.addConstantUtf8(internalName);
               index = constants.addConstantClass(index);
               ldc = new Ldc(18, ii.offset, ii.lineNumber, index);
               ReplaceDupLoadVisitor visitor = new ReplaceDupLoadVisitor(ds, ldc);
               visitor.visit((Instruction)list.get(i + 2));
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
