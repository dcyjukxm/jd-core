package jd.core.model.classfile;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.FieldOrMethod;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.attribute.Attribute;
import jd.core.model.classfile.attribute.AttributeConstantValue;
import jd.core.model.classfile.constant.ConstantValue;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class Field extends FieldOrMethod {
   private Field.ValueAndMethod valueAndMethod = null;
   public int anonymousClassConstructorParameterIndex = -1;
   public int outerMethodLocalVariableNameIndex = -1;

   public Field(int access_flags, int name_index, int descriptor_index, Attribute[] attributes) {
      super(access_flags, name_index, descriptor_index, attributes);
   }

   public ConstantValue getConstantValue(ConstantPool constants) {
      if(this.attributes != null) {
         for(int i = 0; i < this.attributes.length; ++i) {
            if(this.attributes[i].tag == 2) {
               AttributeConstantValue acv = (AttributeConstantValue)this.attributes[i];
               return constants.getConstantValue(acv.constantvalue_index);
            }
         }
      }

      return null;
   }

   public Field.ValueAndMethod getValueAndMethod() {
      return this.valueAndMethod;
   }

   public void setValueAndMethod(Instruction value, Method method) {
      this.valueAndMethod = new Field.ValueAndMethod(value, method);
   }

   public static class ValueAndMethod {
      private Instruction value;
      private Method method;

      ValueAndMethod(Instruction value, Method method) {
         this.value = value;
         this.method = method;
      }

      public Method getMethod() {
         return this.method;
      }

      public Instruction getValue() {
         return this.value;
      }
   }
}
