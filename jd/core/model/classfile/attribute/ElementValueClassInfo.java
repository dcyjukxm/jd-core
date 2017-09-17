package jd.core.model.classfile.attribute;

import jd.core.model.classfile.attribute.ElementValue;

public class ElementValueClassInfo extends ElementValue {
   public final int class_info_index;

   public ElementValueClassInfo(byte tag, int class_info_index) {
      super(tag);
      this.class_info_index = class_info_index;
   }
}
