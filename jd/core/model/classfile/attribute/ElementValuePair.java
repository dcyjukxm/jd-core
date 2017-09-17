package jd.core.model.classfile.attribute;

import jd.core.model.classfile.attribute.ElementValue;

public class ElementValuePair {
   public final int element_name_index;
   public final ElementValue element_value;

   public ElementValuePair(int element_name_index, ElementValue element_value) {
      this.element_name_index = element_name_index;
      this.element_value = element_value;
   }
}
