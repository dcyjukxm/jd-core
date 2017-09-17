package jd.core.model.classfile.attribute;

import jd.core.model.classfile.attribute.ElementValuePair;

public class Annotation {
   public final int type_index;
   public final ElementValuePair[] elementValuePairs;

   public Annotation(int type_index, ElementValuePair[] elementValuePairs) {
      this.type_index = type_index;
      this.elementValuePairs = elementValuePairs;
   }
}
