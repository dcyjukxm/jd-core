package jd.core.model.classfile;

import java.util.List;
import jd.core.model.classfile.FieldOrMethod;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.attribute.Attribute;
import jd.core.model.classfile.attribute.AttributeAnnotationDefault;
import jd.core.model.classfile.attribute.AttributeCode;
import jd.core.model.classfile.attribute.AttributeExceptions;
import jd.core.model.classfile.attribute.AttributeLocalVariableTable;
import jd.core.model.classfile.attribute.AttributeNumberTable;
import jd.core.model.classfile.attribute.AttributeRuntimeParameterAnnotations;
import jd.core.model.classfile.attribute.CodeException;
import jd.core.model.classfile.attribute.ElementValue;
import jd.core.model.classfile.attribute.LineNumber;
import jd.core.model.classfile.attribute.ParameterAnnotations;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class Method extends FieldOrMethod {
   private boolean containsError = false;
   private int[] exceptionIndexes = null;
   private byte[] code = null;
   private LineNumber[] lineNumbers = null;
   private CodeException[] codeExceptions = null;
   private ParameterAnnotations[] visibleParameterAnnotations = null;
   private ParameterAnnotations[] invisibleParameterAnnotations = null;
   private ElementValue defaultAnnotationValue = null;
   private List<Instruction> instructions;
   private List<Instruction> fastNodes;
   private LocalVariables localVariables = null;
   private int superConstructorParameterCount = 0;

   public Method(int access_flags, int name_index, int descriptor_index, Attribute[] attributes) {
      super(access_flags, name_index, descriptor_index, attributes);
      if(attributes != null) {
         AttributeCode ac = null;

         for(int alvt = this.attributes.length - 1; alvt >= 0; --alvt) {
            Attribute ant = this.attributes[alvt];
            switch(ant.tag) {
            case 3:
               ac = (AttributeCode)attributes[alvt];
               break;
            case 4:
               this.exceptionIndexes = ((AttributeExceptions)ant).exception_index_table;
               break;
            case 18:
               this.visibleParameterAnnotations = ((AttributeRuntimeParameterAnnotations)ant).parameter_annotations;
               break;
            case 19:
               this.invisibleParameterAnnotations = ((AttributeRuntimeParameterAnnotations)ant).parameter_annotations;
               break;
            case 20:
               this.defaultAnnotationValue = ((AttributeAnnotationDefault)ant).default_value;
            }
         }

         if(ac != null) {
            this.code = ac.code;
            AttributeLocalVariableTable var9 = ac.getAttributeLocalVariableTable();
            if(var9 != null && var9.local_variable_table != null) {
               AttributeLocalVariableTable var10 = ac.getAttributeLocalVariableTypeTable();
               LocalVariable[] localVariableTypeTable = var10 == null?null:var10.local_variable_table;
               this.localVariables = new LocalVariables(var9.local_variable_table, localVariableTypeTable);
            }

            AttributeNumberTable var11 = ac.getAttributeLineNumberTable();
            this.lineNumbers = var11 == null?null:var11.line_number_table;
            this.codeExceptions = ac.exception_table;
         }
      }

   }

   public boolean containsError() {
      return this.containsError;
   }

   public void setContainsError(boolean containsError) {
      this.containsError = containsError;
   }

   public int[] getExceptionIndexes() {
      return this.exceptionIndexes;
   }

   public LocalVariables getLocalVariables() {
      return this.localVariables;
   }

   public void setLocalVariables(LocalVariables llv) {
      this.localVariables = llv;
   }

   public List<Instruction> getInstructions() {
      return this.instructions;
   }

   public void setInstructions(List<Instruction> instructions) {
      this.instructions = instructions;
   }

   public List<Instruction> getFastNodes() {
      return this.fastNodes;
   }

   public void setFastNodes(List<Instruction> fastNodes) {
      this.fastNodes = fastNodes;
   }

   public byte[] getCode() {
      return this.code;
   }

   public LineNumber[] getLineNumbers() {
      return this.lineNumbers;
   }

   public CodeException[] getCodeExceptions() {
      return this.codeExceptions;
   }

   public ParameterAnnotations[] getVisibleParameterAnnotations() {
      return this.visibleParameterAnnotations;
   }

   public ParameterAnnotations[] getInvisibleParameterAnnotations() {
      return this.invisibleParameterAnnotations;
   }

   public ElementValue getDefaultAnnotationValue() {
      return this.defaultAnnotationValue;
   }

   public int getSuperConstructorParameterCount() {
      return this.superConstructorParameterCount;
   }

   public void setSuperConstructorParameterCount(int count) {
      this.superConstructorParameterCount = count;
   }
}
