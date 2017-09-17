package jd.core.process.analyzer.classfile;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.accessor.GetFieldAccessor;
import jd.core.model.classfile.accessor.GetStaticAccessor;
import jd.core.model.classfile.accessor.InvokeMethodAccessor;
import jd.core.model.classfile.accessor.PutFieldAccessor;
import jd.core.model.classfile.accessor.PutStaticAccessor;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.GetField;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.util.SignatureUtil;

public class AccessorAnalyzer {
   public static void Analyze(ClassFile classFile, Method method) {
      if(!SearchGetStaticAccessor(classFile, method)) {
         if(!SearchPutStaticAccessor(classFile, method)) {
            if(!SearchGetFieldAccessor(classFile, method)) {
               if(!SearchPutFieldAccessor(classFile, method)) {
                  SearchInvokeMethodAccessor(classFile, method);
               }
            }
         }
      }
   }

   private static boolean SearchGetStaticAccessor(ClassFile classFile, Method method) {
      List list = method.getInstructions();
      if(list.size() != 1) {
         return false;
      } else {
         Instruction instruction = (Instruction)list.get(0);
         if(instruction.opcode != 273) {
            return false;
         } else {
            instruction = ((ReturnInstruction)instruction).valueref;
            if(instruction.opcode != 178) {
               return false;
            } else {
               ConstantPool constants = classFile.getConstantPool();
               ConstantFieldref cfr = constants.getConstantFieldref(((GetStatic)instruction).index);
               if(cfr.class_index != classFile.getThisClassIndex()) {
                  return false;
               } else {
                  String methodDescriptor = constants.getConstantUtf8(method.descriptor_index);
                  if(methodDescriptor.charAt(1) != 41) {
                     return false;
                  } else {
                     String methodName = constants.getConstantUtf8(method.name_index);
                     ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
                     String fieldDescriptor = constants.getConstantUtf8(cnat.descriptor_index);
                     String fieldName = constants.getConstantUtf8(cnat.name_index);
                     classFile.addAccessor(methodName, methodDescriptor, new GetStaticAccessor(1, classFile.getThisClassName(), fieldName, fieldDescriptor));
                     return true;
                  }
               }
            }
         }
      }
   }

   private static boolean SearchPutStaticAccessor(ClassFile classFile, Method method) {
      List list = method.getInstructions();
      if(list.size() != 2) {
         return false;
      } else if(((Instruction)list.get(1)).opcode != 177) {
         return false;
      } else {
         Instruction instruction = (Instruction)list.get(0);
         if(instruction.opcode != 179) {
            return false;
         } else {
            ConstantPool constants = classFile.getConstantPool();
            ConstantFieldref cfr = constants.getConstantFieldref(((PutStatic)instruction).index);
            if(cfr.class_index != classFile.getThisClassIndex()) {
               return false;
            } else {
               String methodDescriptor = constants.getConstantUtf8(method.descriptor_index);
               if(methodDescriptor.charAt(1) == 41) {
                  return false;
               } else if(SignatureUtil.GetParameterSignatureCount(methodDescriptor) != 1) {
                  return false;
               } else {
                  String methodName = constants.getConstantUtf8(method.name_index);
                  ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
                  String fieldDescriptor = constants.getConstantUtf8(cnat.descriptor_index);
                  String fieldName = constants.getConstantUtf8(cnat.name_index);
                  classFile.addAccessor(methodName, methodDescriptor, new PutStaticAccessor(2, classFile.getThisClassName(), fieldName, fieldDescriptor));
                  return true;
               }
            }
         }
      }
   }

   private static boolean SearchGetFieldAccessor(ClassFile classFile, Method method) {
      List list = method.getInstructions();
      if(list.size() != 1) {
         return false;
      } else {
         Instruction instruction = (Instruction)list.get(0);
         if(instruction.opcode != 273) {
            return false;
         } else {
            instruction = ((ReturnInstruction)instruction).valueref;
            if(instruction.opcode != 180) {
               return false;
            } else {
               ConstantPool constants = classFile.getConstantPool();
               ConstantFieldref cfr = constants.getConstantFieldref(((GetField)instruction).index);
               if(cfr.class_index != classFile.getThisClassIndex()) {
                  return false;
               } else {
                  String methodDescriptor = constants.getConstantUtf8(method.descriptor_index);
                  if(methodDescriptor.charAt(1) == 41) {
                     return false;
                  } else if(SignatureUtil.GetParameterSignatureCount(methodDescriptor) != 1) {
                     return false;
                  } else {
                     String methodName = constants.getConstantUtf8(method.name_index);
                     ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
                     String fieldDescriptor = constants.getConstantUtf8(cnat.descriptor_index);
                     String fieldName = constants.getConstantUtf8(cnat.name_index);
                     classFile.addAccessor(methodName, methodDescriptor, new GetFieldAccessor(3, classFile.getThisClassName(), fieldName, fieldDescriptor));
                     return true;
                  }
               }
            }
         }
      }
   }

   private static boolean SearchPutFieldAccessor(ClassFile classFile, Method method) {
      List list = method.getInstructions();
      PutField pf;
      Instruction constants;
      switch(list.size()) {
      case 2:
         if(((Instruction)list.get(1)).opcode != 177) {
            return false;
         }

         constants = (Instruction)list.get(0);
         if(constants.opcode != 181) {
            return false;
         }

         pf = (PutField)constants;
         break;
      case 3:
         if(((Instruction)list.get(0)).opcode != 264) {
            return false;
         }

         if(((Instruction)list.get(2)).opcode != 273) {
            return false;
         }

         constants = (Instruction)list.get(1);
         if(constants.opcode != 181) {
            return false;
         }

         pf = (PutField)constants;
         break;
      default:
         return false;
      }

      ConstantPool constants1 = classFile.getConstantPool();
      ConstantFieldref cfr = constants1.getConstantFieldref(pf.index);
      if(cfr.class_index != classFile.getThisClassIndex()) {
         return false;
      } else {
         String methodDescriptor = constants1.getConstantUtf8(method.descriptor_index);
         if(methodDescriptor.charAt(1) == 41) {
            return false;
         } else if(SignatureUtil.GetParameterSignatureCount(methodDescriptor) != 2) {
            return false;
         } else {
            ConstantNameAndType cnat = constants1.getConstantNameAndType(cfr.name_and_type_index);
            String methodName = constants1.getConstantUtf8(method.name_index);
            String fieldDescriptor = constants1.getConstantUtf8(cnat.descriptor_index);
            String fieldName = constants1.getConstantUtf8(cnat.name_index);
            classFile.addAccessor(methodName, methodDescriptor, new PutFieldAccessor(4, classFile.getThisClassName(), fieldName, fieldDescriptor));
            return true;
         }
      }
   }

   private static boolean SearchInvokeMethodAccessor(ClassFile classFile, Method method) {
      List list = method.getInstructions();
      Instruction instruction;
      switch(list.size()) {
      case 1:
         instruction = (Instruction)list.get(0);
         if(instruction.opcode != 273) {
            return false;
         }

         instruction = ((ReturnInstruction)instruction).valueref;
         break;
      case 2:
         instruction = (Instruction)list.get(1);
         if(instruction.opcode != 177) {
            return false;
         }

         instruction = (Instruction)list.get(0);
         break;
      default:
         return false;
      }

      Object ii;
      switch(instruction.opcode) {
      case 182:
      case 183:
      case 185:
         InvokeNoStaticInstruction constants = (InvokeNoStaticInstruction)instruction;
         if(constants.objectref.opcode == 25 && ((ALoad)constants.objectref).index == 0) {
            ii = constants;
            break;
         }

         return false;
      case 184:
         ii = (InvokeInstruction)instruction;
         break;
      default:
         return false;
      }

      ConstantPool constants1 = classFile.getConstantPool();
      String methodName = constants1.getConstantUtf8(method.name_index);
      String methodDescriptor = constants1.getConstantUtf8(method.descriptor_index);
      ConstantMethodref cmr = constants1.getConstantMethodref(((InvokeInstruction)ii).index);
      ConstantNameAndType cnat = constants1.getConstantNameAndType(cmr.name_and_type_index);
      String targetMethodName = constants1.getConstantUtf8(cnat.name_index);
      String targetMethodDescriptor = constants1.getConstantUtf8(cnat.descriptor_index);
      classFile.addAccessor(methodName, methodDescriptor, new InvokeMethodAccessor(5, classFile.getThisClassName(), ((InvokeInstruction)ii).opcode, targetMethodName, targetMethodDescriptor, cmr.getListOfParameterSignatures(), cmr.getReturnedSignature()));
      return true;
   }
}
