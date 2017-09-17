package jd.core.model.classfile.constant;

public abstract class Constant {
   public final byte tag;

   protected Constant(byte tag) {
      this.tag = tag;
   }
}
