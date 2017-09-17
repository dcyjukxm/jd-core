package jd.core.process.deserializer;

import java.io.DataInput;
import java.io.IOException;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.attribute.Attribute;
import jd.core.model.classfile.attribute.AttributeAnnotationDefault;
import jd.core.model.classfile.attribute.AttributeCode;
import jd.core.model.classfile.attribute.AttributeConstantValue;
import jd.core.model.classfile.attribute.AttributeDeprecated;
import jd.core.model.classfile.attribute.AttributeEnclosingMethod;
import jd.core.model.classfile.attribute.AttributeExceptions;
import jd.core.model.classfile.attribute.AttributeInnerClasses;
import jd.core.model.classfile.attribute.AttributeLocalVariableTable;
import jd.core.model.classfile.attribute.AttributeNumberTable;
import jd.core.model.classfile.attribute.AttributeRuntimeAnnotations;
import jd.core.model.classfile.attribute.AttributeRuntimeParameterAnnotations;
import jd.core.model.classfile.attribute.AttributeSignature;
import jd.core.model.classfile.attribute.AttributeSourceFile;
import jd.core.model.classfile.attribute.AttributeSynthetic;
import jd.core.model.classfile.attribute.CodeException;
import jd.core.model.classfile.attribute.InnerClass;
import jd.core.model.classfile.attribute.LineNumber;
import jd.core.model.classfile.attribute.ParameterAnnotations;
import jd.core.model.classfile.attribute.UnknowAttribute;
import jd.core.process.deserializer.AnnotationDeserializer;
import jd.core.process.deserializer.ClassFormatException;

public class AttributeDeserializer {
   public static Attribute[] Deserialize(DataInput di, ConstantPool constants) throws IOException {
      int count = di.readUnsignedShort();
      if(count == 0) {
         return null;
      } else {
         Attribute[] attributes = new Attribute[count];

         for(int i = 0; i < count; ++i) {
            int attribute_name_index = di.readUnsignedShort();
            int attribute_length = di.readInt();
            if(attribute_name_index == constants.annotationDefaultAttributeNameIndex) {
               attributes[i] = new AttributeAnnotationDefault(20, attribute_name_index, AnnotationDeserializer.DeserializeElementValue(di));
            } else if(attribute_name_index == constants.codeAttributeNameIndex) {
               attributes[i] = new AttributeCode(3, attribute_name_index, di.readUnsignedShort(), di.readUnsignedShort(), DeserializeCode(di), DeserializeCodeExceptions(di), Deserialize(di, constants));
            } else if(attribute_name_index == constants.constantValueAttributeNameIndex) {
               if(attribute_length != 2) {
                  throw new ClassFormatException("Invalid attribute length");
               }

               attributes[i] = new AttributeConstantValue(2, attribute_name_index, di.readUnsignedShort());
            } else if(attribute_name_index == constants.deprecatedAttributeNameIndex) {
               if(attribute_length != 0) {
                  throw new ClassFormatException("Invalid attribute length");
               }

               attributes[i] = new AttributeDeprecated(10, attribute_name_index);
            } else if(attribute_name_index == constants.enclosingMethodAttributeNameIndex) {
               if(attribute_length != 4) {
                  throw new ClassFormatException("Invalid attribute length");
               }

               attributes[i] = new AttributeEnclosingMethod(14, attribute_name_index, di.readUnsignedShort(), di.readUnsignedShort());
            } else if(attribute_name_index == constants.exceptionsAttributeNameIndex) {
               attributes[i] = new AttributeExceptions(4, attribute_name_index, DeserializeExceptionIndexTable(di));
            } else if(attribute_name_index == constants.innerClassesAttributeNameIndex) {
               attributes[i] = new AttributeInnerClasses(8, attribute_name_index, DeserializeInnerClasses(di));
            } else if(attribute_name_index == constants.lineNumberTableAttributeNameIndex) {
               attributes[i] = new AttributeNumberTable(15, attribute_name_index, DeserializeLineNumbers(di));
            } else if(attribute_name_index == constants.localVariableTableAttributeNameIndex) {
               attributes[i] = new AttributeLocalVariableTable(6, attribute_name_index, DeserializeLocalVariable(di));
            } else if(attribute_name_index == constants.localVariableTypeTableAttributeNameIndex) {
               attributes[i] = new AttributeLocalVariableTable(7, attribute_name_index, DeserializeLocalVariable(di));
            } else if(attribute_name_index == constants.runtimeInvisibleAnnotationsAttributeNameIndex) {
               attributes[i] = new AttributeRuntimeAnnotations(17, attribute_name_index, AnnotationDeserializer.Deserialize(di));
            } else if(attribute_name_index == constants.runtimeVisibleAnnotationsAttributeNameIndex) {
               attributes[i] = new AttributeRuntimeAnnotations(16, attribute_name_index, AnnotationDeserializer.Deserialize(di));
            } else if(attribute_name_index == constants.runtimeInvisibleParameterAnnotationsAttributeNameIndex) {
               attributes[i] = new AttributeRuntimeParameterAnnotations(19, attribute_name_index, DeserializeParameterAnnotations(di));
            } else if(attribute_name_index == constants.runtimeVisibleParameterAnnotationsAttributeNameIndex) {
               attributes[i] = new AttributeRuntimeParameterAnnotations(18, attribute_name_index, DeserializeParameterAnnotations(di));
            } else if(attribute_name_index == constants.signatureAttributeNameIndex) {
               if(attribute_length != 2) {
                  throw new ClassFormatException("Invalid attribute length");
               }

               attributes[i] = new AttributeSignature(12, attribute_name_index, di.readUnsignedShort());
            } else if(attribute_name_index == constants.sourceFileAttributeNameIndex) {
               if(attribute_length != 2) {
                  throw new ClassFormatException("Invalid attribute length");
               }

               attributes[i] = new AttributeSourceFile(1, attribute_name_index, di.readUnsignedShort());
            } else if(attribute_name_index == constants.syntheticAttributeNameIndex) {
               if(attribute_length != 0) {
                  throw new ClassFormatException("Invalid attribute length");
               }

               attributes[i] = new AttributeSynthetic(9, attribute_name_index);
            } else {
               attributes[i] = new UnknowAttribute(0, attribute_name_index);

               for(int j = 0; j < attribute_length; ++j) {
                  di.readByte();
               }
            }
         }

         return attributes;
      }
   }

   private static byte[] DeserializeCode(DataInput di) throws IOException {
      int code_length = di.readInt();
      if(code_length == 0) {
         return null;
      } else {
         byte[] code = new byte[code_length];
         di.readFully(code);
         return code;
      }
   }

   private static CodeException[] DeserializeCodeExceptions(DataInput di) throws IOException {
      int count = di.readUnsignedShort();
      if(count == 0) {
         return null;
      } else {
         CodeException[] codeExceptions = new CodeException[count];

         for(int i = 0; i < count; ++i) {
            codeExceptions[i] = new CodeException(i, di.readUnsignedShort(), di.readUnsignedShort(), di.readUnsignedShort(), di.readUnsignedShort());
         }

         return codeExceptions;
      }
   }

   private static LineNumber[] DeserializeLineNumbers(DataInput di) throws IOException {
      int count = di.readUnsignedShort();
      if(count == 0) {
         return null;
      } else {
         LineNumber[] lineNumbers = new LineNumber[count];

         for(int i = 0; i < count; ++i) {
            lineNumbers[i] = new LineNumber(di.readUnsignedShort(), di.readUnsignedShort());
         }

         return lineNumbers;
      }
   }

   private static LocalVariable[] DeserializeLocalVariable(DataInput di) throws IOException {
      int count = di.readUnsignedShort();
      if(count == 0) {
         return null;
      } else {
         LocalVariable[] localVariables = new LocalVariable[count];

         for(int i = 0; i < count; ++i) {
            localVariables[i] = new LocalVariable(di.readUnsignedShort(), di.readUnsignedShort(), di.readUnsignedShort(), di.readUnsignedShort(), di.readUnsignedShort());
         }

         return localVariables;
      }
   }

   private static int[] DeserializeExceptionIndexTable(DataInput di) throws IOException {
      int number_of_exceptions = di.readUnsignedShort();
      if(number_of_exceptions == 0) {
         return null;
      } else {
         int[] exception_index_table = new int[number_of_exceptions];

         for(int i = 0; i < number_of_exceptions; ++i) {
            exception_index_table[i] = di.readUnsignedShort();
         }

         return exception_index_table;
      }
   }

   private static InnerClass[] DeserializeInnerClasses(DataInput di) throws IOException {
      int number_of_classes = di.readUnsignedShort();
      if(number_of_classes == 0) {
         return null;
      } else {
         InnerClass[] classes = new InnerClass[number_of_classes];

         for(int i = 0; i < number_of_classes; ++i) {
            classes[i] = new InnerClass(di.readUnsignedShort(), di.readUnsignedShort(), di.readUnsignedShort(), di.readUnsignedShort());
         }

         return classes;
      }
   }

   private static ParameterAnnotations[] DeserializeParameterAnnotations(DataInput di) throws IOException {
      int num_parameters = di.readUnsignedByte();
      if(num_parameters == 0) {
         return null;
      } else {
         ParameterAnnotations[] parameterAnnotations = new ParameterAnnotations[num_parameters];

         for(int i = 0; i < num_parameters; ++i) {
            parameterAnnotations[i] = new ParameterAnnotations(AnnotationDeserializer.Deserialize(di));
         }

         return parameterAnnotations;
      }
   }
}
