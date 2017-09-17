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
import jd.core.model.classfile.constant.ConstantString;
import jd.core.model.classfile.constant.ConstantValue;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.model.instruction.bytecode.instruction.Ldc;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.visitor.ReplaceGetStaticVisitor;
import jd.core.util.SignatureUtil;

public class DotClass14Reconstructor {
   public static void Reconstruct(ReferenceMap referenceMap, ClassFile classFile, List<Instruction> list) {
      int i = list.size();
      if(i >= 6) {
         i -= 5;
         ConstantPool constants = classFile.getConstantPool();

         while(true) {
            while(true) {
               while(true) {
                  Instruction instruction;
                  IfInstruction ii;
                  GetStatic gs;
                  ConstantNameAndType cnatField;
                  String nameField;
                  ClassFile matchingClassFile;
                  ConstantNameAndType cnatMethod;
                  String nameMethod;
                  Ldc ldc;
                  ConstantValue cv;
                  do {
                     Invokestatic is;
                     do {
                        ConstantMethodref cmr;
                        do {
                           do {
                              String descriptorField;
                              do {
                                 ConstantFieldref cfr;
                                 do {
                                    PutStatic ps;
                                    do {
                                       int jumpOffset;
                                       Goto g;
                                       do {
                                          do {
                                             do {
                                                DupStore ds;
                                                TernaryOpStore tos;
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
                                                                           } while(instruction.opcode != 264);

                                                                           ds = (DupStore)instruction;
                                                                        } while(ds.objectref.opcode != 184);

                                                                        is = (Invokestatic)ds.objectref;
                                                                     } while(is.args.size() != 1);

                                                                     instruction = (Instruction)is.args.get(0);
                                                                  } while(instruction.opcode != 18);

                                                                  instruction = (Instruction)list.get(i + 2);
                                                               } while(instruction.opcode != 179);

                                                               ps = (PutStatic)instruction;
                                                            } while(ps.valueref.opcode != 263);
                                                         } while(ds.offset != ps.valueref.offset);

                                                         instruction = (Instruction)list.get(i + 3);
                                                      } while(instruction.opcode != 280);

                                                      tos = (TernaryOpStore)instruction;
                                                   } while(tos.objectref.opcode != 263);
                                                } while(ds.offset != tos.objectref.offset);

                                                instruction = (Instruction)list.get(i + 4);
                                             } while(instruction.opcode != 167);

                                             g = (Goto)instruction;
                                             instruction = (Instruction)list.get(i + 5);
                                          } while(g.offset >= jumpOffset);
                                       } while(jumpOffset > instruction.offset);

                                       gs = (GetStatic)ii.value;
                                    } while(ps.index != gs.index);

                                    cfr = constants.getConstantFieldref(gs.index);
                                 } while(searchMatchingClassFile(cfr.class_index, classFile) == null);

                                 cnatField = constants.getConstantNameAndType(cfr.name_and_type_index);
                                 descriptorField = constants.getConstantUtf8(cnatField.descriptor_index);
                              } while(!descriptorField.equals("Ljava/lang/Class;"));

                              nameField = constants.getConstantUtf8(cnatField.name_index);
                           } while(!nameField.startsWith("class$") && !nameField.startsWith("array$"));

                           cmr = constants.getConstantMethodref(is.index);
                           matchingClassFile = searchMatchingClassFile(cmr.class_index, classFile);
                        } while(matchingClassFile == null);

                        cnatMethod = constants.getConstantNameAndType(cmr.name_and_type_index);
                        nameMethod = constants.getConstantUtf8(cnatMethod.name_index);
                     } while(!nameMethod.equals("class$"));

                     ldc = (Ldc)is.args.get(0);
                     cv = constants.getConstantValue(ldc.index);
                  } while(cv.tag != 8);

                  ConstantString cs = (ConstantString)cv;
                  String signature = constants.getConstantUtf8(cs.string_index);
                  int fields;
                  if(SignatureUtil.GetArrayDimensionCount(signature) == 0) {
                     String matchingConstants = signature.replace('.', '/');
                     referenceMap.add(matchingConstants);
                     fields = constants.addConstantUtf8(matchingConstants);
                     fields = constants.addConstantClass(fields);
                     ldc = new Ldc(18, ii.offset, ii.lineNumber, fields);
                     ReplaceGetStaticVisitor j = new ReplaceGetStaticVisitor(gs.index, ldc);
                     j.visit(instruction);
                  } else {
                     IConst var35 = new IConst(256, ii.offset, ii.lineNumber, 0);
                     String var39 = SignatureUtil.CutArrayDimensionPrefix(signature);
                     int nameAndTypeIndex;
                     Object var38;
                     if(SignatureUtil.IsObjectSignature(var39)) {
                        String methods = var39.replace('.', '/');
                        String method = methods.substring(1, methods.length() - 1);
                        referenceMap.add(method);
                        nameAndTypeIndex = constants.addConstantUtf8(method);
                        nameAndTypeIndex = constants.addConstantClass(nameAndTypeIndex);
                        var38 = new ANewArray(189, ii.offset, ii.lineNumber, nameAndTypeIndex, var35);
                     } else {
                        var38 = new NewArray(188, ii.offset, ii.lineNumber, SignatureUtil.GetTypeFromSignature(var39), var35);
                     }

                     int var42 = constants.addConstantUtf8("getClass");
                     int var46 = constants.addConstantUtf8("()Ljava/lang/Class;");
                     nameAndTypeIndex = constants.addConstantNameAndType(var42, var46);
                     int cmrIndex = constants.addConstantMethodref(constants.objectClassIndex, nameAndTypeIndex);
                     Invokevirtual iv = new Invokevirtual(182, ii.offset, ii.lineNumber, cmrIndex, (Instruction)var38, new ArrayList(0));
                     ReplaceGetStaticVisitor visitor = new ReplaceGetStaticVisitor(gs.index, iv);
                     visitor.visit(instruction);
                  }

                  list.remove(i + 4);
                  list.remove(i + 3);
                  list.remove(i + 2);
                  list.remove(i + 1);
                  list.remove(i);
                  if(matchingClassFile == classFile) {
                     Field[] var37 = classFile.getFields();
                     fields = var37.length;

                     while(fields-- > 0) {
                        Field var44 = var37[fields];
                        if(var44.name_index == cnatField.name_index) {
                           var44.access_flags |= 4096;
                           break;
                        }
                     }

                     Method[] var47 = classFile.getMethods();
                     fields = var47.length;

                     while(fields-- > 0) {
                        Method var48 = var47[fields];
                        if(var48.name_index == cnatMethod.name_index) {
                           var48.access_flags |= 4096;
                           break;
                        }
                     }
                  } else {
                     ConstantPool var36 = matchingClassFile.getConstantPool();
                     Field[] var40 = matchingClassFile.getFields();
                     int var41 = var40.length;

                     while(var41-- > 0) {
                        Field var43 = var40[var41];
                        if(nameField.equals(var36.getConstantUtf8(var43.name_index))) {
                           var43.access_flags |= 4096;
                           break;
                        }
                     }

                     Method[] var45 = matchingClassFile.getMethods();
                     var41 = var45.length;

                     while(var41-- > 0) {
                        Method var49 = var45[var41];
                        if(nameMethod.equals(var36.getConstantUtf8(var49.name_index))) {
                           var49.access_flags |= 4096;
                           break;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static ClassFile searchMatchingClassFile(int classIndex, ClassFile classFile) {
      if(classIndex == classFile.getThisClassIndex()) {
         return classFile;
      } else {
         String className = classFile.getConstantPool().getConstantClassName(classIndex);

         do {
            classFile = classFile.getOuterClass();
            if(classFile == null) {
               return null;
            }
         } while(!classFile.getThisClassName().equals(className));

         return classFile;
      }
   }
}
