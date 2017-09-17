package jd.core.model.classfile.attribute;

import jd.core.model.classfile.attribute.Annotation;

public class ParameterAnnotations {
   public Annotation[] annotations;

   public ParameterAnnotations(Annotation[] annotations) {
      this.annotations = annotations;
   }
}
