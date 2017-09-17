package jd.core.model.classfile.attribute;

import jd.core.model.classfile.attribute.Attribute;
import jd.core.model.classfile.attribute.ParameterAnnotations;

public class AttributeRuntimeParameterAnnotations extends Attribute {
   public ParameterAnnotations[] parameter_annotations;

   public AttributeRuntimeParameterAnnotations(byte tag, int attribute_name_index, ParameterAnnotations[] parameter_annotations) {
      super(tag, attribute_name_index);
      this.parameter_annotations = parameter_annotations;
   }
}
