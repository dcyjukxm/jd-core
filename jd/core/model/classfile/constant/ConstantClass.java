package jd.core.model.classfile.constant;

import jd.core.model.classfile.constant.Constant;

public class ConstantClass extends Constant {
   public final int name_index;

   public ConstantClass(byte tag, int name_index) {
      super(tag);
      this.name_index = name_index;
   }
}
