package jd.core.model.classfile.attribute;

import jd.core.model.classfile.attribute.Annotation;
import jd.core.model.classfile.attribute.ElementValue;

public class ElementValueAnnotationValue extends ElementValue {
   public final Annotation annotation_value;

   public ElementValueAnnotationValue(byte tag, Annotation annotation_value) {
      super(tag);
      this.annotation_value = annotation_value;
   }
}
