package jd.core.model.classfile.constant;

import jd.core.model.classfile.constant.ConstantValue;

public class ConstantLong extends ConstantValue {
   public final long bytes;

   public ConstantLong(byte tag, long bytes) {
      super(tag);
      this.bytes = bytes;
   }
}
