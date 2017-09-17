package jd.core.process.analyzer.classfile.reconstructor;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;

public class NewInstructionReconstructorBase {
   public static void InitAnonymousClassConstructorParameterName(ClassFile classFile, Method method, InvokeNew invokeNew) {
      ConstantPool constants = classFile.getConstantPool();
      ConstantMethodref cmr = constants.getConstantMethodref(invokeNew.index);
      String internalClassName = constants.getConstantClassName(cmr.class_index);
      ClassFile innerClassFile = classFile.getInnerClassFile(internalClassName);
      if(innerClassFile != null) {
         Field[] innerFields = innerClassFile.getFields();
         if(innerFields != null) {
            int i = innerFields.length;
            int argsLength = invokeNew.args.size();
            ConstantPool innerConstants = innerClassFile.getConstantPool();
            LocalVariables localVariables = method.getLocalVariables();

            while(i-- > 0) {
               Field innerField = innerFields[i];
               int index = innerField.anonymousClassConstructorParameterIndex;
               if(index != -1) {
                  innerField.anonymousClassConstructorParameterIndex = -1;
                  if(index < argsLength) {
                     Instruction arg = (Instruction)invokeNew.args.get(index);
                     if(arg.opcode == 192) {
                        arg = ((CheckCast)arg).objectref;
                     }

                     switch(arg.opcode) {
                     case 21:
                     case 25:
                     case 268:
                        LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(((IndexInstruction)arg).index, arg.offset);
                        if(lv != null) {
                           String name = constants.getConstantUtf8(lv.name_index);
                           innerField.outerMethodLocalVariableNameIndex = innerConstants.addConstantUtf8(name);
                           lv.finalFlag = true;
                        }
                     }
                  }
               }
            }
         }
      }

   }
}
