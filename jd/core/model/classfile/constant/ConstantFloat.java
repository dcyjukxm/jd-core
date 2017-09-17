package jd.core.model.classfile.constant;

import jd.core.model.classfile.constant.ConstantValue;

public class ConstantFloat extends ConstantValue {
   public final float bytes;

   public ConstantFloat(byte tag, float bytes) {
      super(tag);
      this.bytes = bytes;
   }
}
