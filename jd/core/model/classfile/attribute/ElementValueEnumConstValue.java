package jd.core.model.classfile.attribute;

import jd.core.model.classfile.attribute.ElementValue;

public class ElementValueEnumConstValue extends ElementValue {
   public final int type_name_index;
   public final int const_name_index;

   public ElementValueEnumConstValue(byte tag, int type_name_index, int const_name_index) {
      super(tag);
      this.type_name_index = type_name_index;
      this.const_name_index = const_name_index;
   }
}
