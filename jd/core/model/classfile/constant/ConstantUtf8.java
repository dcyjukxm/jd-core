package jd.core.model.classfile.constant;

import jd.core.model.classfile.constant.Constant;

public class ConstantUtf8 extends Constant {
   public String bytes;

   public ConstantUtf8(byte tag, String bytes) {
      super(tag);
      this.bytes = bytes;
   }
}
