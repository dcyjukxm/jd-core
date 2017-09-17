package jd.core.process.layouter;

import java.util.ArrayList;
import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.attribute.Annotation;
import jd.core.model.classfile.attribute.Attribute;
import jd.core.model.classfile.attribute.AttributeRuntimeAnnotations;
import jd.core.model.layout.block.AnnotationsLayoutBlock;
import jd.core.model.layout.block.LayoutBlock;

public class AnnotationLayouter {
   public static void CreateBlocksForAnnotations(ClassFile classFile, Attribute[] attributes, List<LayoutBlock> layoutBlockList) {
      if(attributes != null) {
         int attributesLength = attributes.length;
         ArrayList annotations = new ArrayList(attributesLength);

         for(int i = 0; i < attributesLength; ++i) {
            Attribute attribute = attributes[i];
            switch(attribute.tag) {
            case 16:
            case 17:
               Annotation[] array = ((AttributeRuntimeAnnotations)attribute).annotations;
               if(array != null) {
                  Annotation[] var11 = array;
                  int var10 = array.length;

                  for(int var9 = 0; var9 < var10; ++var9) {
                     Annotation annotation = var11[var9];
                     annotations.add(annotation);
                  }
               }
            }
         }

         if(annotations.size() > 0) {
            layoutBlockList.add(new AnnotationsLayoutBlock(classFile, annotations));
         }

      }
   }
}
