package jd.core.process.analyzer.classfile.visitor;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.GetField;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.classfile.visitor.ReplaceOuterAccessorVisitor;
import jd.core.util.SignatureUtil;

public class ReplaceMultipleOuterReferenceVisitor extends ReplaceOuterAccessorVisitor {
   public ReplaceMultipleOuterReferenceVisitor(ClassFile classFile) {
      super(classFile);
   }

   protected ClassFile match(Instruction instruction) {
      if(instruction.opcode != 180) {
         return null;
      } else {
         GetField gf = (GetField)instruction;
         ConstantPool constants;
         String descriptorName;
         ClassFile matchedClassFile;
         Field matchedField;
         String fieldName;
         ConstantPool matchedConstants;
         String matchedFieldName;
         String matchedDescriptorName;
         switch(gf.objectref.opcode) {
         case 25:
            ALoad constants1 = (ALoad)gf.objectref;
            if(constants1.index != 0) {
               return null;
            } else {
               Field cfr2 = this.classFile.getOuterThisField();
               if(cfr2 == null) {
                  return null;
               } else {
                  ConstantPool cnat2 = this.classFile.getConstantPool();
                  ConstantFieldref descriptorName1 = cnat2.getConstantFieldref(gf.index);
                  if(descriptorName1.class_index != this.classFile.getThisClassIndex()) {
                     return null;
                  } else {
                     ConstantNameAndType matchedClassFile1 = cnat2.getConstantNameAndType(descriptorName1.name_and_type_index);
                     if(cfr2.name_index == matchedClassFile1.name_index && cfr2.descriptor_index == matchedClassFile1.descriptor_index) {
                        return this.classFile.getOuterClass();
                     }

                     return null;
                  }
               }
            }
         case 180:
            constants = this.classFile.getConstantPool();
            ConstantFieldref cfr1 = constants.getConstantFieldref(gf.index);
            ConstantNameAndType cnat1 = constants.getConstantNameAndType(cfr1.name_and_type_index);
            descriptorName = constants.getConstantUtf8(cnat1.descriptor_index);
            if(!SignatureUtil.IsObjectSignature(descriptorName)) {
               return null;
            } else {
               matchedClassFile = this.match(gf.objectref);
               if(matchedClassFile != null && matchedClassFile.isAInnerClass()) {
                  matchedField = matchedClassFile.getOuterThisField();
                  if(matchedField == null) {
                     return null;
                  }

                  String className1 = constants.getConstantClassName(cfr1.class_index);
                  if(!className1.equals(matchedClassFile.getThisClassName())) {
                     return null;
                  }

                  fieldName = constants.getConstantUtf8(cnat1.name_index);
                  matchedConstants = matchedClassFile.getConstantPool();
                  matchedFieldName = matchedConstants.getConstantUtf8(matchedField.name_index);
                  if(!fieldName.equals(matchedFieldName)) {
                     return null;
                  }

                  matchedDescriptorName = matchedConstants.getConstantUtf8(matchedField.descriptor_index);
                  if(!descriptorName.equals(matchedDescriptorName)) {
                     return null;
                  }

                  return matchedClassFile.getOuterClass();
               }

               return null;
            }
         case 285:
            constants = this.classFile.getConstantPool();
            GetStatic cfr = (GetStatic)gf.objectref;
            ConstantFieldref cnat = constants.getConstantFieldref(cfr.index);
            descriptorName = constants.getConstantClassName(cnat.class_index);

            for(matchedClassFile = this.classFile.getOuterClass(); matchedClassFile != null; matchedClassFile = matchedClassFile.getOuterClass()) {
               if(matchedClassFile.getThisClassName().equals(descriptorName)) {
                  matchedField = matchedClassFile.getOuterThisField();
                  if(matchedField == null) {
                     return null;
                  }

                  cnat = constants.getConstantFieldref(gf.index);
                  ConstantNameAndType className = constants.getConstantNameAndType(cnat.name_and_type_index);
                  fieldName = constants.getConstantUtf8(className.name_index);
                  matchedConstants = matchedClassFile.getConstantPool();
                  matchedFieldName = matchedConstants.getConstantUtf8(matchedField.name_index);
                  if(!fieldName.equals(matchedFieldName)) {
                     return null;
                  }

                  matchedDescriptorName = constants.getConstantUtf8(className.descriptor_index);
                  String outerFieldDescriptor = matchedConstants.getConstantUtf8(matchedField.descriptor_index);
                  if(!matchedDescriptorName.equals(outerFieldDescriptor)) {
                     return null;
                  }

                  return matchedClassFile.getOuterClass();
               }
            }

            return null;
         default:
            return null;
         }
      }
   }
}
