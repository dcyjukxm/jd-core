package jd.core.model.classfile;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.attribute.Annotation;
import jd.core.model.classfile.attribute.Attribute;
import jd.core.model.classfile.attribute.AttributeRuntimeAnnotations;
import jd.core.model.classfile.attribute.AttributeSignature;

public class Base {
   public int access_flags;
   public final Attribute[] attributes;

   public Base(int access_flags, Attribute[] attributes) {
      this.access_flags = access_flags;
      this.attributes = attributes;
   }

   public AttributeSignature getAttributeSignature() {
      if(this.attributes != null) {
         for(int i = this.attributes.length - 1; i >= 0; --i) {
            if(this.attributes[i].tag == 12) {
               return (AttributeSignature)this.attributes[i];
            }
         }
      }

      return null;
   }

   public boolean containsAttributeDeprecated() {
      if(this.attributes != null) {
         for(int i = this.attributes.length - 1; i >= 0; --i) {
            if(this.attributes[i].tag == 10) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean containsAnnotationDeprecated(ClassFile classFile) {
      if(this.attributes != null) {
         int i = this.attributes.length - 1;

         while(i >= 0) {
            switch(this.attributes[i].tag) {
            case 16:
            case 17:
               Annotation[] annotations = ((AttributeRuntimeAnnotations)this.attributes[i]).annotations;
               if(this.containsAnnotationDeprecated(classFile, annotations)) {
                  return true;
               }
            default:
               --i;
            }
         }
      }

      return false;
   }

   private boolean containsAnnotationDeprecated(ClassFile classFile, Annotation[] annotations) {
      if(annotations != null) {
         int idsIndex = classFile.getConstantPool().internalDeprecatedSignatureIndex;

         for(int i = annotations.length - 1; i >= 0; --i) {
            if(idsIndex == annotations[i].type_index) {
               return true;
            }
         }
      }

      return false;
   }
}
