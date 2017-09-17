package jd.core.process.analyzer.classfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.attribute.AttributeSignature;
import jd.core.model.classfile.constant.Constant;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.GetField;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokespecial;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.fast.instruction.FastLabel;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.AccessorAnalyzer;
import jd.core.process.analyzer.classfile.FieldNameGenerator;
import jd.core.process.analyzer.classfile.LocalVariableAnalyzer;
import jd.core.process.analyzer.classfile.reconstructor.AssignmentInstructionReconstructor;
import jd.core.process.analyzer.classfile.reconstructor.DotClass118AReconstructor;
import jd.core.process.analyzer.classfile.reconstructor.DotClass14Reconstructor;
import jd.core.process.analyzer.classfile.reconstructor.DupStoreThisReconstructor;
import jd.core.process.analyzer.classfile.reconstructor.InitDexEnumFieldsReconstructor;
import jd.core.process.analyzer.classfile.reconstructor.InitInstanceFieldsReconstructor;
import jd.core.process.analyzer.classfile.reconstructor.InitStaticFieldsReconstructor;
import jd.core.process.analyzer.classfile.reconstructor.NewInstructionReconstructor;
import jd.core.process.analyzer.classfile.reconstructor.OuterReferenceReconstructor;
import jd.core.process.analyzer.classfile.reconstructor.PostIncReconstructor;
import jd.core.process.analyzer.classfile.reconstructor.PreIncReconstructor;
import jd.core.process.analyzer.classfile.reconstructor.SimpleNewInstructionReconstructor;
import jd.core.process.analyzer.classfile.visitor.CheckCastAndConvertInstructionVisitor;
import jd.core.process.analyzer.classfile.visitor.ReplaceStringBuxxxerVisitor;
import jd.core.process.analyzer.classfile.visitor.SetConstantTypeInStringIndexOfMethodsVisitor;
import jd.core.process.analyzer.instruction.bytecode.InstructionListBuilder;
import jd.core.process.analyzer.instruction.fast.DupLocalVariableAnalyzer;
import jd.core.process.analyzer.instruction.fast.FastInstructionListBuilder;
import jd.core.process.analyzer.instruction.fast.ReturnLineNumberAnalyzer;
import jd.core.process.analyzer.variable.DefaultVariableNameGenerator;
import jd.core.util.SignatureUtil;

public class ClassFileAnalyzer {
   public static void Analyze(ReferenceMap referenceMap, ClassFile classFile) {
      HashMap innerClassesMap;
      if(classFile.getInnerClassFiles() != null) {
         innerClassesMap = new HashMap(10);
         innerClassesMap.put(classFile.getThisClassName(), classFile);
         PopulateInnerClassMap(innerClassesMap, classFile);
      } else {
         innerClassesMap = null;
      }

      AnalyzeClass(referenceMap, innerClassesMap, classFile);
   }

   private static void PopulateInnerClassMap(HashMap<String, ClassFile> innerClassesMap, ClassFile classFile) {
      ArrayList innerClassFiles = classFile.getInnerClassFiles();
      if(innerClassFiles != null) {
         int length = innerClassFiles.size();

         for(int i = 0; i < length; ++i) {
            ClassFile innerClassFile = (ClassFile)innerClassFiles.get(i);
            innerClassesMap.put(innerClassFile.getThisClassName(), innerClassFile);
            PopulateInnerClassMap(innerClassesMap, innerClassFile);
         }
      }

   }

   private static void AnalyzeClass(ReferenceMap referenceMap, HashMap<String, ClassFile> innerClassesMap, ClassFile classFile) {
      if((classFile.access_flags & 4096) != 0) {
         AnalyzeSyntheticClass(classFile);
      } else {
         HashMap eclipseSwitchMaps = new HashMap();
         PreAnalyzeMethods(eclipseSwitchMaps, classFile);
         ArrayList innerClassFiles = classFile.getInnerClassFiles();
         if(innerClassFiles != null) {
            int length = innerClassFiles.size();

            for(int i = 0; i < length; ++i) {
               AnalyzeClass(referenceMap, innerClassesMap, (ClassFile)innerClassFiles.get(i));
            }
         }

         CheckUnicityOfFieldNames(classFile);
         CheckUnicityOfFieldrefNames(classFile);
         AnalyzeMethods(referenceMap, innerClassesMap, classFile);
         CheckAssertionsDisabledField(classFile);
         if((classFile.access_flags & 16384) != 0) {
            AnalyzeEnum(classFile);
         }
      }

   }

   private static void AnalyzeSyntheticClass(ClassFile classFile) {
      if((classFile.access_flags & 8) != 0 && classFile.getOuterClass() != null && classFile.getInternalAnonymousClassName() != null && classFile.getFields() != null && classFile.getMethods() != null && classFile.getFields().length > 0 && classFile.getMethods().length == 1 && (classFile.getMethods()[0].access_flags & 4127) == 8) {
         ClassFile outerClassFile = classFile.getOuterClass();
         ConstantPool outerConstants = outerClassFile.getConstantPool();
         ConstantPool constants = classFile.getConstantPool();
         Method method = classFile.getMethods()[0];

         try {
            AnalyzeMethodref(classFile);
            ArrayList e = new ArrayList();
            ArrayList listForAnalyze = new ArrayList();
            InstructionListBuilder.Build(classFile, method, e, listForAnalyze);
            int length = e.size();

            for(int index = 0; index < length && ((Instruction)e.get(index)).opcode == 179; ++index) {
               PutStatic ps = (PutStatic)e.get(index);
               ConstantFieldref cfr = constants.getConstantFieldref(ps.index);
               if(cfr.class_index != classFile.getThisClassIndex()) {
                  break;
               }

               ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
               Field field = SearchField(classFile, cnat);
               if(field == null || (field.access_flags & 4127) != 4120) {
                  break;
               }

               String fieldName = constants.getConstantUtf8(cnat.name_index);
               if(!fieldName.startsWith("$SwitchMap$")) {
                  break;
               }

               ArrayList enumNameIndexes = new ArrayList();

               for(index += 3; index < length; index += 3) {
                  Instruction outerFieldNameIndex = (Instruction)e.get(index - 2);
                  if(outerFieldNameIndex.opcode != 272 || ((Instruction)e.get(index - 1)).opcode != 167 || ((Instruction)e.get(index)).opcode != 58) {
                     break;
                  }

                  outerFieldNameIndex = ((ArrayStoreInstruction)outerFieldNameIndex).indexref;
                  if(outerFieldNameIndex.opcode != 182) {
                     break;
                  }

                  outerFieldNameIndex = ((Invokevirtual)outerFieldNameIndex).objectref;
                  if(outerFieldNameIndex.opcode != 178) {
                     break;
                  }

                  cfr = constants.getConstantFieldref(((GetStatic)outerFieldNameIndex).index);
                  cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
                  String enumName = constants.getConstantUtf8(cnat.name_index);
                  int outerEnumNameIndex = outerConstants.addConstantUtf8(enumName);
                  enumNameIndexes.add(Integer.valueOf(outerEnumNameIndex));
               }

               int var19 = outerConstants.addConstantUtf8(fieldName);
               outerClassFile.getSwitchMaps().put(Integer.valueOf(var19), enumNameIndexes);
               index -= 3;
            }
         } catch (Exception var18) {
            method.setContainsError(true);
         }
      }

   }

   private static Field SearchField(ClassFile classFile, ConstantNameAndType cnat) {
      Field[] fields = classFile.getFields();
      int i = fields.length;

      Field field;
      do {
         if(i-- <= 0) {
            return null;
         }

         field = fields[i];
      } while(field.name_index != cnat.name_index || field.descriptor_index != cnat.descriptor_index);

      return field;
   }

   private static void AnalyzeMethodref(ClassFile classFile) {
      ConstantPool constants = classFile.getConstantPool();

      for(int i = constants.size() - 1; i >= 0; --i) {
         Constant constant = constants.get(i);
         if(constant != null && (constant.tag == 10 || constant.tag == 11)) {
            ConstantMethodref cmr = (ConstantMethodref)constant;
            ConstantNameAndType cnat = constants.getConstantNameAndType(cmr.name_and_type_index);
            if(cnat != null) {
               String signature = constants.getConstantUtf8(cnat.descriptor_index);
               cmr.setParameterSignatures(SignatureUtil.GetParameterSignatures(signature));
               cmr.setReturnedSignature(SignatureUtil.GetMethodReturnedSignature(signature));
            }
         }
      }

   }

   private static void CheckUnicityOfFieldNames(ClassFile classFile) {
      Field[] fields = classFile.getFields();
      if(fields != null) {
         ConstantPool constants = classFile.getConstantPool();
         HashMap map = new HashMap();
         int i = fields.length;

         String name;
         ArrayList list;
         while(i-- > 0) {
            Field iteratorName = fields[i];
            if((iteratorName.access_flags & 5) == 0) {
               name = constants.getConstantUtf8(iteratorName.name_index);
               list = (ArrayList)map.get(name);
               if(list == null) {
                  list = new ArrayList(5);
                  map.put(name, list);
               }

               list.add(iteratorName);
            }
         }

         Iterator var12 = map.keySet().iterator();

         while(true) {
            int j;
            do {
               if(!var12.hasNext()) {
                  return;
               }

               name = (String)var12.next();
               list = (ArrayList)map.get(name);
               j = list.size();
            } while(j < 2);

            while(j-- > 0) {
               Field field = (Field)list.get(j);
               String newName = FieldNameGenerator.GenerateName(constants.getConstantUtf8(field.descriptor_index), constants.getConstantUtf8(field.name_index));
               int newNameIndex = constants.addConstantUtf8(newName);
               field.name_index = newNameIndex;
            }
         }
      }
   }

   private static void CheckUnicityOfFieldrefNames(ClassFile classFile) {
      ConstantPool constants = classFile.getConstantPool();
      int i = constants.size();
      Object[] array = new Object[i];

      while(i-- > 0) {
         Constant map = constants.get(i);
         if(map != null && map.tag == 9) {
            ConstantFieldref iterator = (ConstantFieldref)map;
            HashMap name = (HashMap)array[iterator.class_index];
            if(name == null) {
               name = new HashMap();
               array[iterator.class_index] = name;
            }

            ConstantNameAndType list = constants.getConstantNameAndType(iterator.name_and_type_index);
            String k = constants.getConstantUtf8(list.name_index);
            ArrayList cnat = (ArrayList)name.get(k);
            if(cnat != null) {
               if(((ConstantNameAndType)cnat.get(0)).descriptor_index != list.descriptor_index) {
                  cnat.add(list);
               }
            } else {
               cnat = new ArrayList(5);
               name.put(k, cnat);
               cnat.add(list);
            }
         }
      }

      i = array.length;

      label51:
      while(true) {
         do {
            if(i-- <= 0) {
               return;
            }
         } while(array[i] == null);

         HashMap var12 = (HashMap)array[i];
         Iterator var13 = var12.keySet().iterator();

         while(true) {
            String var14;
            ArrayList var15;
            int var16;
            do {
               if(!var13.hasNext()) {
                  continue label51;
               }

               var14 = (String)var13.next();
               var15 = (ArrayList)var12.get(var14);
               var16 = var15.size();
            } while(var16 < 2);

            while(var16-- > 0) {
               ConstantNameAndType var17 = (ConstantNameAndType)var15.get(var16);
               String signature = constants.getConstantUtf8(var17.descriptor_index);
               String newName = FieldNameGenerator.GenerateName(signature, var14);
               var17.name_index = constants.addConstantUtf8(newName);
            }
         }
      }
   }

   private static void CheckAssertionsDisabledField(ClassFile classFile) {
      ConstantPool constants = classFile.getConstantPool();
      Field[] fields = classFile.getFields();
      if(fields != null) {
         int i = fields.length;

         while(i-- > 0) {
            Field field = fields[i];
            if((field.access_flags & 4127) == 24 && field.getValueAndMethod() != null) {
               String name = constants.getConstantUtf8(field.name_index);
               if(name.equals("$assertionsDisabled")) {
                  field.access_flags |= 4096;
               }
            }
         }

      }
   }

   private static boolean HasAAccessorMethodName(ClassFile classFile, Method method) {
      String methodName = classFile.getConstantPool().getConstantUtf8(method.name_index);
      if(!methodName.startsWith("access$")) {
         return false;
      } else {
         int i = methodName.length();

         while(i-- > "access$".length()) {
            if(!Character.isDigit(methodName.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean HasAEclipseSwitchTableMethodName(ClassFile classFile, Method method) {
      String methodName = classFile.getConstantPool().getConstantUtf8(method.name_index);
      if(!methodName.startsWith("$SWITCH_TABLE$")) {
         return false;
      } else {
         String methodDescriptor = classFile.getConstantPool().getConstantUtf8(method.descriptor_index);
         return methodDescriptor.equals("()[I");
      }
   }

   private static void ParseEclipseOrDexSwitchTableMethod(ClassFile classFile, Method method) {
      List list = method.getInstructions();
      int length = list.size();
      ConstantPool constants;
      ArrayList enumNameIndexes;
      int index;
      Instruction instruction;
      ConstantFieldref cfr;
      ConstantNameAndType cnat;
      if(length >= 6 && ((Instruction)list.get(0)).opcode == 264 && ((Instruction)list.get(1)).opcode == 262 && ((Instruction)list.get(2)).opcode == 273 && ((Instruction)list.get(3)).opcode == 87 && ((Instruction)list.get(4)).opcode == 58) {
         constants = classFile.getConstantPool();
         enumNameIndexes = new ArrayList();

         for(index = 7; index < length; index += 3) {
            instruction = (Instruction)list.get(index - 2);
            if(instruction.opcode != 272 || ((Instruction)list.get(index - 1)).opcode != 167 || ((Instruction)list.get(index)).opcode != 87) {
               break;
            }

            instruction = ((ArrayStoreInstruction)instruction).indexref;
            if(instruction.opcode != 182) {
               break;
            }

            instruction = ((Invokevirtual)instruction).objectref;
            if(instruction.opcode != 178) {
               break;
            }

            cfr = constants.getConstantFieldref(((GetStatic)instruction).index);
            cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
            enumNameIndexes.add(Integer.valueOf(cnat.name_index));
         }

         classFile.getSwitchMaps().put(Integer.valueOf(method.name_index), enumNameIndexes);
      } else if(length >= 7 && ((Instruction)list.get(0)).opcode == 58 && ((Instruction)list.get(1)).opcode == 262 && ((Instruction)list.get(2)).opcode == 273 && ((Instruction)list.get(3)).opcode == 58 && ((Instruction)list.get(4)).opcode == 272) {
         constants = classFile.getConstantPool();
         enumNameIndexes = new ArrayList();

         for(index = 4; index < length; ++index) {
            instruction = (Instruction)list.get(index);
            if(instruction.opcode != 272) {
               break;
            }

            instruction = ((ArrayStoreInstruction)instruction).indexref;
            if(instruction.opcode != 182) {
               break;
            }

            instruction = ((Invokevirtual)instruction).objectref;
            if(instruction.opcode != 178) {
               break;
            }

            cfr = constants.getConstantFieldref(((GetStatic)instruction).index);
            cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
            enumNameIndexes.add(Integer.valueOf(cnat.name_index));
         }

         classFile.getSwitchMaps().put(Integer.valueOf(method.name_index), enumNameIndexes);
      }

   }

   private static void PreAnalyzeMethods(HashMap<Integer, List<Instruction>> eclipseSwitchMaps, ClassFile classFile) {
      AnalyzeMethodref(classFile);
      Method[] methods = classFile.getMethods();
      if(methods != null) {
         int length = methods.length;
         DefaultVariableNameGenerator variableNameGenerator = new DefaultVariableNameGenerator(classFile);
         int outerThisFieldrefIndex = 0;

         for(int i = 0; i < length; ++i) {
            Method method = methods[i];

            try {
               if(method.getCode() == null) {
                  if((method.access_flags & 4160) == 0) {
                     LocalVariableAnalyzer.Analyze(classFile, method, variableNameGenerator, (List)null, (List)null);
                  }
               } else {
                  ArrayList e = new ArrayList();
                  ArrayList listForAnalyze = new ArrayList();
                  InstructionListBuilder.Build(classFile, method, e, listForAnalyze);
                  method.setInstructions(e);
                  if((method.access_flags & 15) == 8 && HasAAccessorMethodName(classFile, method)) {
                     AccessorAnalyzer.Analyze(classFile, method);
                     method.access_flags |= 4096;
                  } else if((method.access_flags & 4160) == 0) {
                     LocalVariableAnalyzer.Analyze(classFile, method, variableNameGenerator, e, listForAnalyze);
                     outerThisFieldrefIndex = SearchOuterThisFieldrefIndex(classFile, method, e, outerThisFieldrefIndex);
                  } else if((method.access_flags & 4111) == 4104 && HasAEclipseSwitchTableMethodName(classFile, method)) {
                     ParseEclipseOrDexSwitchTableMethod(classFile, method);
                  }
               }
            } catch (Exception var10) {
               method.setContainsError(true);
            }
         }

         if(outerThisFieldrefIndex != 0) {
            AnalyzeOuterReferences(classFile, outerThisFieldrefIndex);
         }

      }
   }

   private static void AnalyzeMethods(ReferenceMap referenceMap, HashMap<String, ClassFile> innerClassesMap, ClassFile classFile) {
      Method[] methods = classFile.getMethods();
      if(methods != null) {
         int length = methods.length;
         OuterReferenceReconstructor outerReferenceReconstructor = innerClassesMap != null?new OuterReferenceReconstructor(innerClassesMap, classFile):null;

         int i;
         Method method;
         for(i = 0; i < length; ++i) {
            method = methods[i];
            if((method.access_flags & 4160) == 0 && method.getCode() != null && !method.containsError()) {
               try {
                  List e = method.getInstructions();
                  if(outerReferenceReconstructor != null) {
                     outerReferenceReconstructor.reconstruct(method, e);
                  }

                  NewInstructionReconstructor.Reconstruct(classFile, method, e);
                  SimpleNewInstructionReconstructor.Reconstruct(classFile, method, e);
                  PreIncReconstructor.Reconstruct(e);
                  PostIncReconstructor.Reconstruct(e);
                  DotClass118AReconstructor.Reconstruct(referenceMap, classFile, e);
                  DotClass14Reconstructor.Reconstruct(referenceMap, classFile, e);
                  ReplaceStringBufferAndStringBuilder(classFile, e);
                  RemoveUnusedPopInstruction(e);
                  TransformTestOnLongOrDouble(e);
                  SetConstantTypeInStringIndexOfMethods(classFile, e);
                  DupStoreThisReconstructor.Reconstruct(e);
                  AssignmentInstructionReconstructor.Reconstruct(e);
                  CheckCastAndConvertInstructionVisitor.visit(classFile.getConstantPool(), e);
                  ArrayList fastList = new ArrayList(e);
                  method.setFastNodes(fastList);
                  FastInstructionListBuilder.Build(referenceMap, classFile, method, fastList);
                  DupLocalVariableAnalyzer.Declare(classFile, method, fastList);
               } catch (Exception var11) {
                  method.setContainsError(true);
               }
            }
         }

         InitDexEnumFieldsReconstructor.Reconstruct(classFile);
         InitStaticFieldsReconstructor.Reconstruct(classFile);
         InitInstanceFieldsReconstructor.Reconstruct(classFile);

         for(i = 0; i < length; ++i) {
            method = methods[i];
            if((method.access_flags & 4160) == 0 && method.getCode() != null && method.getFastNodes() != null && !method.containsError()) {
               try {
                  AnalyseAndModifyConstructors(classFile, method);
                  ReturnLineNumberAnalyzer.Check(method);
                  RemoveLastReturnInstruction(method);
               } catch (Exception var10) {
                  method.setContainsError(true);
               }
            }
         }

      }
   }

   private static int SearchOuterThisFieldrefIndex(ClassFile classFile, Method method, List<Instruction> list, int outerThisFieldrefIndex) {
      if(classFile.isAInnerClass() && (classFile.access_flags & 8) == 0) {
         ConstantPool constants = classFile.getConstantPool();
         if(method.name_index != constants.instanceConstructorIndex) {
            return outerThisFieldrefIndex;
         } else {
            AttributeSignature as = method.getAttributeSignature();
            String methodSignature = constants.getConstantUtf8(as == null?method.descriptor_index:as.signature_index);
            if(methodSignature.charAt(1) == 41) {
               return 0;
            } else {
               int length = list.size();

               for(int i = 0; i < length; ++i) {
                  Instruction instruction = (Instruction)list.get(i);
                  if(instruction.opcode == 181) {
                     PutField is = (PutField)instruction;
                     if(is.objectref.opcode == 25 && is.valueref.opcode == 25 && ((ALoad)is.objectref).index == 0 && ((ALoad)is.valueref).index == 1 && (outerThisFieldrefIndex == 0 || is.index == outerThisFieldrefIndex)) {
                        return is.index;
                     }
                  } else if(instruction.opcode == 183) {
                     Invokespecial var13 = (Invokespecial)instruction;
                     ConstantMethodref cmr = constants.getConstantMethodref(var13.index);
                     if(cmr.class_index == classFile.getThisClassIndex()) {
                        ConstantNameAndType cnat = constants.getConstantNameAndType(cmr.name_and_type_index);
                        if(cnat.name_index == constants.instanceConstructorIndex) {
                           return outerThisFieldrefIndex;
                        }
                     }
                  }
               }

               return 0;
            }
         }
      } else {
         return 0;
      }
   }

   private static void AnalyzeOuterReferences(ClassFile classFile, int outerThisFieldrefIndex) {
      Method[] methods = classFile.getMethods();
      if(methods != null) {
         int length = methods.length;
         ConstantPool constants = classFile.getConstantPool();
         ConstantFieldref cfr = constants.getConstantFieldref(outerThisFieldrefIndex);
         if(cfr.class_index == classFile.getThisClassIndex()) {
            ConstantNameAndType i = constants.getConstantNameAndType(cfr.name_and_type_index);
            Field[] method = classFile.getFields();
            if(method != null) {
               for(int list = method.length - 1; list >= 0; --list) {
                  Field listLength = method[list];
                  if(listLength.name_index == i.name_index && listLength.descriptor_index == i.descriptor_index) {
                     classFile.setOuterThisField(listLength);
                     listLength.access_flags |= 4096;
                     break;
                  }
               }
            }
         }

         for(int var14 = 0; var14 < length; ++var14) {
            Method var15 = methods[var14];
            if(var15.getCode() != null && !var15.containsError()) {
               List var16 = var15.getInstructions();
               if(var16 != null) {
                  int var17 = var16.size();
                  if(var15.name_index == constants.instanceConstructorIndex) {
                     for(int var18 = 0; var18 < var17; ++var18) {
                        Instruction var19 = (Instruction)var16.get(var18);
                        if(var19.opcode == 181 && ((PutField)var19).index == outerThisFieldrefIndex) {
                           var16.remove(var18);
                           break;
                        }
                     }
                  } else if((var15.access_flags & 4104) == 8 && var15.name_index != constants.classConstructorIndex && var17 == 1 && classFile.isAInnerClass()) {
                     Instruction instruction = (Instruction)var16.get(0);
                     if(instruction.opcode == 273) {
                        instruction = ((ReturnInstruction)instruction).valueref;
                        if(instruction.opcode == 180) {
                           GetField gf = (GetField)instruction;
                           if(gf.objectref.opcode == 25 && ((ALoad)gf.objectref).index == 0) {
                              cfr = constants.getConstantFieldref(gf.index);
                              if(cfr.class_index == classFile.getThisClassIndex()) {
                                 ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
                                 Field outerField = classFile.getOuterThisField();
                                 if(cnat.descriptor_index == outerField.descriptor_index && cnat.name_index == outerField.name_index) {
                                    var15.access_flags |= 4096;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private static void AnalyseAndModifyConstructors(ClassFile classFile, Method method) {
      ConstantPool constants = classFile.getConstantPool();
      if(method.name_index == constants.instanceConstructorIndex) {
         for(List list = method.getFastNodes(); list.size() > 0; list.remove(0)) {
            Instruction instruction = (Instruction)list.get(0);
            if(instruction.opcode == 183) {
               Invokespecial pf = (Invokespecial)instruction;
               if(pf.objectref.opcode == 25 && ((ALoad)pf.objectref).index == 0) {
                  ConstantMethodref ii = constants.getConstantMethodref(pf.index);
                  ConstantNameAndType cfr = constants.getConstantNameAndType(ii.name_and_type_index);
                  if(cfr.name_index == constants.instanceConstructorIndex) {
                     if(ii.class_index == classFile.getSuperClassIndex()) {
                        int cnat = pf.args.size();
                        method.setSuperConstructorParameterCount(cnat);
                        if((classFile.access_flags & 16384) != 0) {
                           if(cnat == 2) {
                              list.remove(0);
                           }
                        } else if(cnat == 0) {
                           list.remove(0);
                        }
                     }
                     break;
                  }
               }
            } else if(instruction.opcode == 181) {
               PutField pf1 = (PutField)instruction;
               switch(pf1.valueref.opcode) {
               case 21:
               case 25:
               case 268:
                  IndexInstruction ii1 = (IndexInstruction)pf1.valueref;
                  if(ii1.index > 1) {
                     ConstantFieldref cfr1 = constants.getConstantFieldref(pf1.index);
                     ConstantNameAndType cnat1 = constants.getConstantNameAndType(cfr1.name_and_type_index);
                     Field field = classFile.getField(cnat1.name_index, cnat1.descriptor_index);
                     field.anonymousClassConstructorParameterIndex = ii1.index - 1;
                     field.access_flags |= 4096;
                  }
               }
            }
         }
      }

   }

   private static void RemoveLastReturnInstruction(Method method) {
      List list = method.getFastNodes();
      if(list != null) {
         int length = list.size();
         if(length > 0) {
            switch(((Instruction)list.get(length - 1)).opcode) {
            case 177:
               list.remove(length - 1);
               break;
            case 320:
               FastLabel fl = (FastLabel)list.get(length - 1);
               if(fl.instruction.opcode == 177) {
                  fl.instruction = null;
               }
            }
         }
      }

   }

   private static void ReplaceStringBufferAndStringBuilder(ClassFile classFile, List<Instruction> list) {
      ReplaceStringBuxxxerVisitor visitor = new ReplaceStringBuxxxerVisitor(classFile.getConstantPool());
      int length = list.size();

      for(int i = 0; i < length; ++i) {
         visitor.visit((Instruction)list.get(i));
      }

   }

   private static void RemoveUnusedPopInstruction(List<Instruction> list) {
      int index = list.size();

      while(index-- > 0) {
         Instruction instruction = (Instruction)list.get(index);
         if(instruction.opcode == 87) {
            switch(((Pop)instruction).objectref.opcode) {
            case 21:
            case 25:
            case 178:
            case 180:
            case 268:
            case 285:
               list.remove(index);
            }
         }
      }

   }

   private static void TransformTestOnLongOrDouble(List<Instruction> list) {
      int index = list.size();

      while(index-- > 0) {
         Instruction instruction = (Instruction)list.get(index);
         if(instruction.opcode == 260) {
            IfInstruction ii = (IfInstruction)instruction;
            switch(ii.cmp) {
            case 0:
            case 1:
            case 2:
            case 5:
            case 6:
            case 7:
               if(ii.value.opcode == 267) {
                  BinaryOperatorInstruction boi = (BinaryOperatorInstruction)ii.value;
                  if("<".equals(boi.operator)) {
                     list.set(index, new IfCmp(261, ii.offset, ii.lineNumber, ii.cmp, boi.value1, boi.value2, ii.branch));
                  }
               }
            case 3:
            case 4:
            }
         }
      }

   }

   private static void SetConstantTypeInStringIndexOfMethods(ClassFile classFile, List<Instruction> list) {
      SetConstantTypeInStringIndexOfMethodsVisitor visitor = new SetConstantTypeInStringIndexOfMethodsVisitor(classFile.getConstantPool());
      visitor.visit(list);
   }

   private static void AnalyzeEnum(ClassFile classFile) {
      if(classFile.getFields() != null) {
         ConstantPool constants = classFile.getConstantPool();
         String enumArraySignature = "[" + classFile.getInternalClassName();
         Field[] fields = classFile.getFields();

         for(int i = fields.length - 1; i >= 0; --i) {
            Field field = fields[i];
            if((field.access_flags & 20480) != 0 && field.getValueAndMethod() != null) {
               Instruction instruction = field.getValueAndMethod().getValue();
               if((instruction.opcode == 282 || instruction.opcode == 283) && constants.getConstantUtf8(field.descriptor_index).equals(enumArraySignature)) {
                  String fieldName = constants.getConstantUtf8(field.name_index);
                  if(fieldName.equals("$VALUES") || fieldName.equals("ENUM$VALUES")) {
                     classFile.setEnumValues(((InitArrayInstruction)instruction).values);
                     break;
                  }
               }
            }
         }

      }
   }
}
