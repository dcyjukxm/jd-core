package jd.core.model.classfile.constant;

import jd.core.model.classfile.constant.Constant;

public abstract class ConstantValue extends Constant {
   public ConstantValue(byte tag) {
      super(tag);
   }
}
