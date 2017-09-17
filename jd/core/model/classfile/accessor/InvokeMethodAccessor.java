package jd.core.model.classfile.accessor;

import java.util.List;
import jd.core.model.classfile.accessor.Accessor;

public class InvokeMethodAccessor extends Accessor {
   public final String className;
   public final int methodOpcode;
   public final String methodName;
   public final String methodDescriptor;
   public final List<String> listOfParameterSignatures;
   public final String returnedSignature;

   public InvokeMethodAccessor(byte tag, String className, int methodOpcode, String methodName, String methodDescriptor, List<String> listOfParameterSignatures, String returnedSignature) {
      super(tag);
      this.className = className;
      this.methodOpcode = methodOpcode;
      this.methodName = methodName;
      this.methodDescriptor = methodDescriptor;
      this.listOfParameterSignatures = listOfParameterSignatures;
      this.returnedSignature = returnedSignature;
   }
}
