package jd.core.model.classfile.constant;

import jd.core.model.classfile.constant.Constant;

public class ConstantFieldref extends Constant {
   public final int class_index;
   public final int name_and_type_index;

   public ConstantFieldref(byte tag, int class_index, int name_and_type_index) {
      super(tag);
      this.class_index = class_index;
      this.name_and_type_index = name_and_type_index;
   }
}
