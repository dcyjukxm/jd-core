package jd.core.model.classfile.attribute;

import jd.core.model.classfile.attribute.ElementValue;

public class ElementValueArrayValue extends ElementValue {
   public final ElementValue[] values;

   public ElementValueArrayValue(byte tag, ElementValue[] values) {
      super(tag);
      this.values = values;
   }
}
