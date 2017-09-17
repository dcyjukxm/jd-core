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
import jd.core.process.analyzer.classfile.visitor.ReplaceDupLoadVisitor;
import jd.core.util.SignatureUtil;

public class DotClass118AReconstructor {
   public static void Reconstruct(ReferenceMap referenceMap, ClassFile classFile, List<Instruction> list) {
      int i = list.size();
      if(i >= 6) {
         i -= 5;
         ConstantPool constants = classFile.getConstantPool();

         while(true) {
            while(true) {
               ConstantNameAndType cnatMethod;
               ConstantNameAndType cnatField;
               label161:
               while(true) {
                  IfInstruction ii;
                  DupStore ds;
                  Ldc ldc;
                  ConstantValue cv;
                  String signatureField;
                  do {
                     GetStatic gs;
                     PutStatic ps;
                     do {
                        do {
                           Instruction instruction;
                           do {
                              do {
                                 String nameMethod;
                                 do {
                                    Invokestatic is;
                                    do {
                                       do {
                                          do {
                                             int jumpOffset;
                                             Goto g;
                                             do {
                                                do {
                                                   do {
                                                      do {
                                                         TernaryOpStore tos;
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

                                                                  gs = (GetStatic)ii.value;
                                                                  jumpOffset = ii.GetJumpOffset();
                                                                  instruction = (Instruction)list.get(i + 1);
                                                               } while(instruction.opcode != 280);

                                                               tos = (TernaryOpStore)instruction;
                                                            } while(tos.objectref.opcode != 178);
                                                         } while(gs.index != ((GetStatic)tos.objectref).index);

                                                         instruction = (Instruction)list.get(i + 2);
                                                      } while(instruction.opcode != 167);

                                                      g = (Goto)instruction;
                                                      instruction = (Instruction)list.get(i + 3);
                                                   } while(instruction.opcode != 264);
                                                } while(g.offset >= jumpOffset);
                                             } while(jumpOffset > instruction.offset);

                                             ds = (DupStore)instruction;
                                          } while(ds.objectref.opcode != 184);

                                          is = (Invokestatic)ds.objectref;
                                       } while(is.args.size() != 1);

                                       instruction = (Instruction)is.args.get(0);
                                    } while(instruction.opcode != 18);

                                    ConstantMethodref cmr = constants.getConstantMethodref(is.index);
                                    cnatMethod = constants.getConstantNameAndType(cmr.name_and_type_index);
                                    nameMethod = constants.getConstantUtf8(cnatMethod.name_index);
                                 } while(!nameMethod.equals("class$"));

                                 ldc = (Ldc)instruction;
                                 cv = constants.getConstantValue(ldc.index);
                              } while(cv.tag != 8);

                              instruction = (Instruction)list.get(i + 4);
                           } while(instruction.opcode != 179);

                           ps = (PutStatic)instruction;
                        } while(ps.valueref.opcode != 263);
                     } while(ds.offset != ps.valueref.offset);

                     ConstantFieldref cfr = constants.getConstantFieldref(gs.index);
                     cnatField = constants.getConstantNameAndType(cfr.name_and_type_index);
                     signatureField = constants.getConstantUtf8(cnatField.descriptor_index);
                  } while(!signatureField.equals("Ljava/lang/Class;"));

                  String nameField = constants.getConstantUtf8(cnatField.name_index);
                  ConstantString fields;
                  String j;
                  String methods;
                  int var42;
                  if(nameField.startsWith("class$")) {
                     fields = (ConstantString)cv;
                     j = constants.getConstantUtf8(fields.string_index);
                     methods = j.replace('.', '/');
                     referenceMap.add(methods);
                     int var39 = constants.addConstantUtf8(methods);
                     var39 = constants.addConstantClass(var39);
                     ldc = new Ldc(18, ii.offset, ii.lineNumber, var39);
                     ReplaceDupLoadVisitor var41 = new ReplaceDupLoadVisitor(ds, ldc);
                     var42 = i + 5;

                     while(true) {
                        if(var42 >= list.size()) {
                           break label161;
                        }

                        var41.visit((Instruction)list.get(var42));
                        if(var41.getParentFound() != null) {
                           break label161;
                        }

                        ++var42;
                     }
                  }

                  if(nameField.startsWith("array$")) {
                     fields = (ConstantString)cv;
                     j = constants.getConstantUtf8(fields.string_index);
                     methods = SignatureUtil.CutArrayDimensionPrefix(j);
                     IConst method = new IConst(256, ii.offset, ii.lineNumber, 0);
                     Object newArray;
                     int nameAndTypeIndex;
                     if(SignatureUtil.IsObjectSignature(methods)) {
                        String methodNameIndex = methods.replace('.', '/');
                        String methodDescriptorIndex = methodNameIndex.substring(1, methodNameIndex.length() - 1);
                        referenceMap.add(methodDescriptorIndex);
                        nameAndTypeIndex = constants.addConstantUtf8(methodDescriptorIndex);
                        nameAndTypeIndex = constants.addConstantClass(nameAndTypeIndex);
                        newArray = new ANewArray(189, ii.offset, ii.lineNumber, nameAndTypeIndex, method);
                     } else {
                        newArray = new NewArray(188, ii.offset, ii.lineNumber, SignatureUtil.GetTypeFromSignature(methods), method);
                     }

                     var42 = constants.addConstantUtf8("getClass");
                     int var43 = constants.addConstantUtf8("()Ljava/lang/Class;");
                     nameAndTypeIndex = constants.addConstantNameAndType(var42, var43);
                     int cmrIndex = constants.addConstantMethodref(constants.objectClassIndex, nameAndTypeIndex);
                     Invokevirtual iv = new Invokevirtual(182, ii.offset, ii.lineNumber, cmrIndex, (Instruction)newArray, new ArrayList(0));
                     ReplaceDupLoadVisitor visitor = new ReplaceDupLoadVisitor(ds, iv);
                     int j1 = i + 5;

                     while(true) {
                        if(j1 >= list.size()) {
                           break label161;
                        }

                        visitor.visit((Instruction)list.get(j1));
                        if(visitor.getParentFound() != null) {
                           break label161;
                        }

                        ++j1;
                     }
                  }
               }

               list.remove(i + 4);
               list.remove(i + 3);
               list.remove(i + 2);
               list.remove(i + 1);
               list.remove(i);
               Field[] var35 = classFile.getFields();
               int var36 = var35.length;

               while(var36-- > 0) {
                  Field var37 = var35[var36];
                  if(var37.name_index == cnatField.name_index) {
                     var37.access_flags |= 4096;
                     break;
                  }
               }

               Method[] var38 = classFile.getMethods();
               var36 = var38.length;

               while(var36-- > 0) {
                  Method var40 = var38[var36];
                  if(var40.name_index == cnatMethod.name_index) {
                     var40.access_flags |= 4096;
                     break;
                  }
               }
            }
         }
      }
   }
}
