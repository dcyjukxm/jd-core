package jd.core.process.analyzer.classfile.visitor;

import java.util.HashMap;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.accessor.Accessor;
import jd.core.model.classfile.accessor.PutFieldAccessor;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.process.analyzer.classfile.visitor.OuterGetStaticVisitor;

public class OuterPutFieldVisitor extends OuterGetStaticVisitor {
   public OuterPutFieldVisitor(HashMap<String, ClassFile> innerClassesMap, ConstantPool constants) {
      super(innerClassesMap, constants);
   }

   protected Accessor match(Instruction i) {
      if(i.opcode != 184) {
         return null;
      } else {
         Invokestatic is = (Invokestatic)i;
         ConstantMethodref cmr = this.constants.getConstantMethodref(is.index);
         ConstantNameAndType cnat = this.constants.getConstantNameAndType(cmr.name_and_type_index);
         String descriptor = this.constants.getConstantUtf8(cnat.descriptor_index);
         if(cmr.getNbrOfParameters() != 2) {
            return null;
         } else {
            String className = this.constants.getConstantClassName(cmr.class_index);
            ClassFile classFile = (ClassFile)this.innerClassesMap.get(className);
            if(classFile == null) {
               return null;
            } else {
               String name = this.constants.getConstantUtf8(cnat.name_index);
               Accessor accessor = classFile.getAccessor(name, descriptor);
               return accessor != null && accessor.tag == 4?(PutFieldAccessor)accessor:null;
            }
         }
      }
   }

   protected Instruction newInstruction(Instruction i, Accessor a) {
      PutFieldAccessor pfa = (PutFieldAccessor)a;
      Invokestatic is = (Invokestatic)i;
      int nameIndex = this.constants.addConstantUtf8(pfa.fieldName);
      int descriptorIndex = this.constants.addConstantUtf8(pfa.fieldDescriptor);
      int cnatIndex = this.constants.addConstantNameAndType(nameIndex, descriptorIndex);
      int classNameIndex = this.constants.addConstantUtf8(pfa.className);
      int classIndex = this.constants.addConstantClass(classNameIndex);
      int cfrIndex = this.constants.addConstantFieldref(classIndex, cnatIndex);
      Instruction valueref = (Instruction)is.args.remove(1);
      Instruction objectref = (Instruction)is.args.remove(0);
      return new PutField(181, i.offset, i.lineNumber, cfrIndex, objectref, valueref);
   }
}
