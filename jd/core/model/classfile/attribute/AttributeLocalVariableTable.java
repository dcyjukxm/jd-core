package jd.core.model.classfile.attribute;

import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.attribute.Attribute;

public class AttributeLocalVariableTable extends Attribute {
   public final LocalVariable[] local_variable_table;

   public AttributeLocalVariableTable(byte tag, int attribute_name_index, LocalVariable[] local_variable_table) {
      super(tag, attribute_name_index);
      this.local_variable_table = local_variable_table;
   }
}
