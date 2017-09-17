package jd.core.process.analyzer.classfile.reconstructor;

import java.util.HashMap;
import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.classfile.visitor.OuterGetFieldVisitor;
import jd.core.process.analyzer.classfile.visitor.OuterGetStaticVisitor;
import jd.core.process.analyzer.classfile.visitor.OuterInvokeMethodVisitor;
import jd.core.process.analyzer.classfile.visitor.OuterPutFieldVisitor;
import jd.core.process.analyzer.classfile.visitor.OuterPutStaticVisitor;
import jd.core.process.analyzer.classfile.visitor.ReplaceMultipleOuterReferenceVisitor;
import jd.core.process.analyzer.classfile.visitor.ReplaceOuterAccessorVisitor;
import jd.core.process.analyzer.classfile.visitor.ReplaceOuterReferenceVisitor;
import jd.core.util.SignatureUtil;

public class OuterReferenceReconstructor {
   private ClassFile classFile;
   private ReplaceOuterReferenceVisitor outerReferenceVisitor;
   private ReplaceMultipleOuterReferenceVisitor multipleOuterReference;
   private ReplaceOuterAccessorVisitor outerAccessorVisitor;
   private OuterGetStaticVisitor outerGetStaticVisitor;
   private OuterPutStaticVisitor outerPutStaticVisitor;
   private OuterGetFieldVisitor outerGetFieldVisitor;
   private OuterPutFieldVisitor outerPutFieldVisitor;
   private OuterInvokeMethodVisitor outerMethodVisitor;

   public OuterReferenceReconstructor(HashMap<String, ClassFile> innerClassesMap, ClassFile classFile) {
      this.classFile = classFile;
      ConstantPool constants = classFile.getConstantPool();
      this.outerReferenceVisitor = new ReplaceOuterReferenceVisitor(25, 1, CreateOuterThisInstructionIndex(classFile));
      this.multipleOuterReference = new ReplaceMultipleOuterReferenceVisitor(classFile);
      this.outerAccessorVisitor = new ReplaceOuterAccessorVisitor(classFile);
      this.outerGetFieldVisitor = new OuterGetFieldVisitor(innerClassesMap, constants);
      this.outerPutFieldVisitor = new OuterPutFieldVisitor(innerClassesMap, constants);
      this.outerGetStaticVisitor = new OuterGetStaticVisitor(innerClassesMap, constants);
      this.outerPutStaticVisitor = new OuterPutStaticVisitor(innerClassesMap, constants);
      this.outerMethodVisitor = new OuterInvokeMethodVisitor(innerClassesMap, constants);
   }

   public void reconstruct(Method method, List<Instruction> list) {
      if(this.classFile.getOuterThisField() != null) {
         ConstantPool constants = this.classFile.getConstantPool();
         if(method.name_index == constants.instanceConstructorIndex) {
            this.outerReferenceVisitor.visit(list);
         }

         this.multipleOuterReference.visit(list);
         this.outerAccessorVisitor.visit(list);
      }

      this.outerGetFieldVisitor.visit(list);
      this.outerPutFieldVisitor.visit(list);
      this.outerGetStaticVisitor.visit(list);
      this.outerPutStaticVisitor.visit(list);
      this.outerMethodVisitor.visit(list);
   }

   private static int CreateOuterThisInstructionIndex(ClassFile classFile) {
      if(classFile.getOuterClass() == null) {
         return 0;
      } else {
         String internalOuterClassName = classFile.getOuterClass().getInternalClassName();
         String outerClassName = SignatureUtil.GetInnerName(internalOuterClassName);
         ConstantPool constants = classFile.getConstantPool();
         int signatureIndex = constants.addConstantUtf8(outerClassName);
         int classIndex = constants.addConstantClass(signatureIndex);
         int thisIndex = constants.thisLocalVariableNameIndex;
         int descriptorIndex = constants.addConstantUtf8(internalOuterClassName);
         int nameAndTypeIndex = constants.addConstantNameAndType(thisIndex, descriptorIndex);
         return constants.addConstantFieldref(classIndex, nameAndTypeIndex);
      }
   }
}
