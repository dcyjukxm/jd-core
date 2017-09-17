package jd.core.util;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Method;

public class ClassFileUtil {
   public static boolean ContainsMultipleConstructor(ClassFile classFile) {
      ConstantPool constants = classFile.getConstantPool();
      Method[] methods = classFile.getMethods();
      boolean flag = false;

      for(int i = 0; i < methods.length; ++i) {
         Method method = methods[i];
         if((method.access_flags & 4160) == 0 && method.name_index == constants.instanceConstructorIndex) {
            if(flag) {
               return true;
            }

            flag = true;
         }
      }

      return false;
   }

   public static boolean IsAMethodOfEnum(ClassFile classFile, Method method, String signature) {
      ConstantPool constants = classFile.getConstantPool();
      if((method.access_flags & 9) == 9) {
         String methodName = constants.getConstantUtf8(method.name_index);
         String s;
         if(methodName.equals("valueOf")) {
            s = "(Ljava/lang/String;)" + classFile.getInternalClassName();
            if(s.equals(signature)) {
               return true;
            }
         }

         if(methodName.equals("values")) {
            s = "()[" + classFile.getInternalClassName();
            if(s.equals(signature)) {
               return true;
            }
         }
      }

      return false;
   }
}
