package jd.core.model.classfile.constant;

import jd.core.model.classfile.constant.ConstantMethodref;

public class ConstantInterfaceMethodref extends ConstantMethodref {
   public ConstantInterfaceMethodref(byte tag, int class_index, int name_and_type_index) {
      super(tag, class_index, name_and_type_index);
   }
}
