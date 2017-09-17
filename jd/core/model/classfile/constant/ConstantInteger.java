package jd.core.model.classfile.constant;

import jd.core.model.classfile.constant.ConstantValue;

public class ConstantInteger extends ConstantValue {
   public final int bytes;

   public ConstantInteger(byte tag, int bytes) {
      super(tag);
      this.bytes = bytes;
   }
}
