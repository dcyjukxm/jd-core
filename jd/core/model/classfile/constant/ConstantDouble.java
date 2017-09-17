package jd.core.model.classfile.constant;

import jd.core.model.classfile.constant.ConstantValue;

public class ConstantDouble extends ConstantValue {
   public final double bytes;

   public ConstantDouble(byte tag, double bytes) {
      super(tag);
      this.bytes = bytes;
   }
}
