package jd.core.model.classfile.constant;

import jd.core.model.classfile.constant.ConstantValue;

public class ConstantString extends ConstantValue {
   public final int string_index;

   public ConstantString(byte tag, int string_index) {
      super(tag);
      this.string_index = string_index;
   }
}
