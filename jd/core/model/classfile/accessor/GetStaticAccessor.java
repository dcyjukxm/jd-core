package jd.core.model.classfile.accessor;

import jd.core.model.classfile.accessor.Accessor;

public class GetStaticAccessor extends Accessor {
   public final String className;
   public final String fieldName;
   public final String fieldDescriptor;

   public GetStaticAccessor(byte tag, String className, String fieldName, String fieldDescriptor) {
      super(tag);
      this.className = className;
      this.fieldName = fieldName;
      this.fieldDescriptor = fieldDescriptor;
   }
}
