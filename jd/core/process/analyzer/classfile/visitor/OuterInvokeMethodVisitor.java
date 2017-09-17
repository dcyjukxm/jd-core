package jd.core.process.analyzer.classfile.visitor;

import java.util.HashMap;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.accessor.Accessor;
import jd.core.model.classfile.accessor.InvokeMethodAccessor;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokeinterface;
import jd.core.model.instruction.bytecode.instruction.Invokespecial;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.process.analyzer.classfile.visitor.OuterGetStaticVisitor;

public class OuterInvokeMethodVisitor extends OuterGetStaticVisitor {
   public OuterInvokeMethodVisitor(HashMap<String, ClassFile> innerClassesMap, ConstantPool constants) {
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
         if(descriptor.charAt(1) == 41) {
            return null;
         } else {
            String className = this.constants.getConstantClassName(cmr.class_index);
            ClassFile classFile = (ClassFile)this.innerClassesMap.get(className);
            if(classFile == null) {
               return null;
            } else {
               String name = this.constants.getConstantUtf8(cnat.name_index);
               Accessor accessor = classFile.getAccessor(name, descriptor);
               return accessor != null && accessor.tag == 5?accessor:null;
            }
         }
      }
   }

   protected Instruction newInstruction(Instruction i, Accessor a) {
      InvokeMethodAccessor ima = (InvokeMethodAccessor)a;
      Invokestatic is = (Invokestatic)i;
      int nameIndex = this.constants.addConstantUtf8(ima.methodName);
      int descriptorIndex = this.constants.addConstantUtf8(ima.methodDescriptor);
      int cnatIndex = this.constants.addConstantNameAndType(nameIndex, descriptorIndex);
      int classNameIndex = this.constants.addConstantUtf8(ima.className);
      int classIndex = this.constants.addConstantClass(classNameIndex);
      int cmrIndex = this.constants.addConstantMethodref(classIndex, cnatIndex, ima.listOfParameterSignatures, ima.returnedSignature);
      Instruction objectref;
      switch(ima.methodOpcode) {
      case 182:
         objectref = (Instruction)is.args.remove(0);
         return new Invokevirtual(182, i.offset, i.lineNumber, cmrIndex, objectref, is.args);
      case 183:
         objectref = (Instruction)is.args.remove(0);
         return new Invokespecial(183, i.offset, i.lineNumber, cmrIndex, objectref, is.args);
      case 184:
         return new Invokestatic(184, i.offset, i.lineNumber, cmrIndex, is.args);
      case 185:
         objectref = (Instruction)is.args.remove(0);
         return new Invokeinterface(185, i.offset, i.lineNumber, cmrIndex, objectref, is.args);
      default:
         return i;
      }
   }
}
