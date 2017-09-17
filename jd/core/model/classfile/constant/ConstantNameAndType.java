package jd.core.model.classfile.constant;

import jd.core.model.classfile.constant.Constant;

public class ConstantNameAndType extends Constant {
   public int name_index;
   public final int descriptor_index;

   public ConstantNameAndType(byte tag, int name_index, int descriptor_index) {
      super(tag);
      this.name_index = name_index;
      this.descriptor_index = descriptor_index;
   }
}
