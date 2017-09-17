package jd.core.model.classfile.attribute;

import jd.core.model.classfile.attribute.ElementValue;

public class ElementValuePrimitiveType extends ElementValue {
   public byte type;
   public final int const_value_index;

   public ElementValuePrimitiveType(byte tag, byte type, int const_value_index) {
      super(tag);
      this.type = type;
      this.const_value_index = const_value_index;
   }
}
