package jd.core.process.analyzer.classfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.attribute.Annotation;
import jd.core.model.classfile.attribute.Attribute;
import jd.core.model.classfile.attribute.AttributeRuntimeAnnotations;
import jd.core.model.classfile.attribute.AttributeRuntimeParameterAnnotations;
import jd.core.model.classfile.attribute.AttributeSignature;
import jd.core.model.classfile.attribute.CodeException;
import jd.core.model.classfile.attribute.ElementValue;
import jd.core.model.classfile.attribute.ElementValueAnnotationValue;
import jd.core.model.classfile.attribute.ElementValueArrayValue;
import jd.core.model.classfile.attribute.ElementValueClassInfo;
import jd.core.model.classfile.attribute.ElementValueEnumConstValue;
import jd.core.model.classfile.attribute.ElementValuePair;
import jd.core.model.classfile.attribute.ParameterAnnotations;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.reference.Reference;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.SignatureAnalyzer;
import jd.core.process.analyzer.classfile.visitor.ReferenceVisitor;

public class ReferenceAnalyzer {
   public static void Analyze(ReferenceMap referenceMap, ClassFile classFile) {
      CountReferences(referenceMap, classFile);
      ReduceReferences(referenceMap, classFile);
   }

   private static void CountReferences(ReferenceMap referenceMap, ClassFile classFile) {
      referenceMap.add(classFile.getThisClassName());
      AttributeSignature as = classFile.getAttributeSignature();
      int visitor;
      if(as == null) {
         if(classFile.getSuperClassIndex() != 0) {
            referenceMap.add(classFile.getSuperClassName());
         }

         int[] innerClassFiles = classFile.getInterfaces();
         if(innerClassFiles != null) {
            for(visitor = innerClassFiles.length - 1; visitor >= 0; --visitor) {
               String internalInterfaceName = classFile.getConstantPool().getConstantClassName(innerClassFiles[visitor]);
               referenceMap.add(internalInterfaceName);
            }
         }
      } else {
         String var6 = classFile.getConstantPool().getConstantUtf8(as.signature_index);
         SignatureAnalyzer.AnalyzeClassSignature(referenceMap, var6);
      }

      CountReferencesInAttributes(referenceMap, classFile.getConstantPool(), classFile.getAttributes());
      ArrayList var7 = classFile.getInnerClassFiles();
      if(var7 != null) {
         for(visitor = var7.size() - 1; visitor >= 0; --visitor) {
            CountReferences(referenceMap, (ClassFile)var7.get(visitor));
         }
      }

      ReferenceVisitor var8 = new ReferenceVisitor(classFile.getConstantPool(), referenceMap);
      CountReferencesInFields(referenceMap, var8, classFile);
      CountReferencesInMethods(referenceMap, var8, classFile);
   }

   private static void CountReferencesInAttributes(ReferenceMap referenceMap, ConstantPool constants, Attribute[] attributes) {
      if(attributes != null) {
         label27:
         for(int i = attributes.length - 1; i >= 0; --i) {
            switch(attributes[i].tag) {
            case 16:
            case 17:
               Annotation[] var6 = ((AttributeRuntimeAnnotations)attributes[i]).annotations;
               int j = var6.length - 1;

               while(true) {
                  if(j < 0) {
                     continue label27;
                  }

                  CountAnnotationReference(referenceMap, constants, var6[j]);
                  --j;
               }
            case 18:
            case 19:
               ParameterAnnotations[] parameterAnnotations = ((AttributeRuntimeParameterAnnotations)attributes[i]).parameter_annotations;
               CountParameterAnnotationsReference(referenceMap, constants, parameterAnnotations);
            }
         }
      }

   }

   private static void CountAnnotationReference(ReferenceMap referenceMap, ConstantPool constants, Annotation annotation) {
      String typeName = constants.getConstantUtf8(annotation.type_index);
      SignatureAnalyzer.AnalyzeSimpleSignature(referenceMap, typeName);
      ElementValuePair[] elementValuePairs = annotation.elementValuePairs;
      if(elementValuePairs != null) {
         for(int j = elementValuePairs.length - 1; j >= 0; --j) {
            CountElementValue(referenceMap, constants, elementValuePairs[j].element_value);
         }
      }

   }

   private static void CountParameterAnnotationsReference(ReferenceMap referenceMap, ConstantPool constants, ParameterAnnotations[] parameterAnnotations) {
      if(parameterAnnotations != null) {
         for(int i = parameterAnnotations.length - 1; i >= 0; --i) {
            Annotation[] annotations = parameterAnnotations[i].annotations;
            if(annotations != null) {
               for(int j = annotations.length - 1; j >= 0; --j) {
                  CountAnnotationReference(referenceMap, constants, annotations[j]);
               }
            }
         }
      }

   }

   private static void CountElementValue(ReferenceMap referenceMap, ConstantPool constants, ElementValue ev) {
      String signature;
      ElementValueClassInfo evci;
      switch(ev.tag) {
      case 2:
         ElementValueEnumConstValue var9 = (ElementValueEnumConstValue)ev;
         signature = constants.getConstantUtf8(var9.type_name_index);
         SignatureAnalyzer.AnalyzeSimpleSignature(referenceMap, signature);
         break;
      case 3:
         evci = (ElementValueClassInfo)ev;
         signature = constants.getConstantUtf8(evci.class_info_index);
         SignatureAnalyzer.AnalyzeSimpleSignature(referenceMap, signature);
         break;
      case 4:
         ElementValueAnnotationValue var8 = (ElementValueAnnotationValue)ev;
         CountAnnotationReference(referenceMap, constants, var8.annotation_value);
         break;
      case 5:
         ElementValueArrayValue evecv = (ElementValueArrayValue)ev;
         ElementValue[] values = evecv.values;
         if(values != null) {
            for(int i = values.length - 1; i >= 0; --i) {
               if(values[i].tag == 3) {
                  evci = (ElementValueClassInfo)values[i];
                  signature = constants.getConstantUtf8(evci.class_info_index);
                  SignatureAnalyzer.AnalyzeSimpleSignature(referenceMap, signature);
               }
            }
         }
      }

   }

   private static void CountReferencesInFields(ReferenceMap referenceMap, ReferenceVisitor visitor, ClassFile classFile) {
      Field[] fields = classFile.getFields();
      if(fields != null) {
         for(int i = fields.length - 1; i >= 0; --i) {
            Field field = fields[i];
            if((field.access_flags & 4096) == 0) {
               CountReferencesInAttributes(referenceMap, classFile.getConstantPool(), field.getAttributes());
               AttributeSignature as = field.getAttributeSignature();
               String signature = classFile.getConstantPool().getConstantUtf8(as == null?field.descriptor_index:as.signature_index);
               SignatureAnalyzer.AnalyzeSimpleSignature(referenceMap, signature);
               if(field.getValueAndMethod() != null) {
                  visitor.visit(field.getValueAndMethod().getValue());
               }
            }
         }

      }
   }

   private static void CountReferencesInMethods(ReferenceMap referenceMap, ReferenceVisitor visitor, ClassFile classFile) {
      Method[] methods = classFile.getMethods();
      if(methods != null) {
         ConstantPool constants = classFile.getConstantPool();

         for(int i = methods.length - 1; i >= 0; --i) {
            Method method = methods[i];
            if((method.access_flags & 4160) == 0 && !method.containsError()) {
               CountReferencesInAttributes(referenceMap, classFile.getConstantPool(), method.getAttributes());
               AttributeSignature as = method.getAttributeSignature();
               String signature = constants.getConstantUtf8(as == null?method.descriptor_index:as.signature_index);
               SignatureAnalyzer.AnalyzeMethodSignature(referenceMap, signature);
               int[] exceptionIndexes = method.getExceptionIndexes();
               if(exceptionIndexes != null) {
                  for(int defaultAnnotationValue = exceptionIndexes.length - 1; defaultAnnotationValue >= 0; --defaultAnnotationValue) {
                     referenceMap.add(constants.getConstantClassName(exceptionIndexes[defaultAnnotationValue]));
                  }
               }

               ElementValue var13 = method.getDefaultAnnotationValue();
               if(var13 != null) {
                  CountElementValue(referenceMap, constants, var13);
               }

               LocalVariables localVariables = method.getLocalVariables();
               if(localVariables != null) {
                  CountReferencesInLocalVariables(referenceMap, constants, localVariables);
               }

               CodeException[] codeExceptions = method.getCodeExceptions();
               if(codeExceptions != null) {
                  CountReferencesInCodeExceptions(referenceMap, constants, codeExceptions);
               }

               CountReferencesInCode(visitor, method);
            }
         }

      }
   }

   private static void CountReferencesInLocalVariables(ReferenceMap referenceMap, ConstantPool constants, LocalVariables localVariables) {
      for(int i = localVariables.size() - 1; i >= 0; --i) {
         LocalVariable lv = localVariables.getLocalVariableAt(i);
         if(lv != null && lv.signature_index > 0) {
            String signature = constants.getConstantUtf8(lv.signature_index);
            SignatureAnalyzer.AnalyzeSimpleSignature(referenceMap, signature);
         }
      }

   }

   private static void CountReferencesInCodeExceptions(ReferenceMap referenceMap, ConstantPool constants, CodeException[] codeExceptions) {
      for(int i = codeExceptions.length - 1; i >= 0; --i) {
         CodeException ce = codeExceptions[i];
         if(ce.catch_type != 0) {
            String internalClassName = constants.getConstantClassName(ce.catch_type);
            referenceMap.add(internalClassName);
         }
      }

   }

   private static void CountReferencesInCode(ReferenceVisitor visitor, Method method) {
      List instructions = method.getFastNodes();
      if(instructions != null) {
         for(int i = instructions.size() - 1; i >= 0; --i) {
            visitor.visit((Instruction)instructions.get(i));
         }
      }

   }

   private static void ReduceReferences(ReferenceMap referenceMap, ClassFile classFile) {
      HashMap multipleInternalClassName = new HashMap();
      Iterator iterator = referenceMap.values().iterator();

      Reference reference;
      String internalName;
      int index;
      String internalPackageName;
      while(iterator.hasNext()) {
         reference = (Reference)iterator.next();
         internalName = reference.getInternalName();
         index = internalName.lastIndexOf(47);
         internalPackageName = index != -1?internalName.substring(index + 1):internalName;
         if(multipleInternalClassName.containsKey(internalPackageName)) {
            multipleInternalClassName.put(internalPackageName, Boolean.TRUE);
         } else {
            multipleInternalClassName.put(internalPackageName, Boolean.FALSE);
         }
      }

      iterator = referenceMap.values().iterator();

      while(true) {
         while(iterator.hasNext()) {
            reference = (Reference)iterator.next();
            internalName = reference.getInternalName();
            index = internalName.lastIndexOf(47);
            String internalClassName;
            if(index != -1) {
               internalPackageName = internalName.substring(0, index);
               internalClassName = internalName.substring(index + 1);
            } else {
               internalPackageName = "";
               internalClassName = internalName;
            }

            String internalPackageName_className = classFile.getInternalPackageName() + '/' + internalClassName;
            if(!classFile.getInternalPackageName().equals(internalPackageName) && ((Boolean)multipleInternalClassName.get(internalClassName)).booleanValue()) {
               iterator.remove();
            } else if(referenceMap.contains(internalPackageName_className)) {
               iterator.remove();
            }
         }

         return;
      }
   }
}
