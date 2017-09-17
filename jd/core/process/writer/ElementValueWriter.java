package jd.core.process.writer;

import jd.core.loader.Loader;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.attribute.ElementValue;
import jd.core.model.classfile.attribute.ElementValueAnnotationValue;
import jd.core.model.classfile.attribute.ElementValueArrayValue;
import jd.core.model.classfile.attribute.ElementValueClassInfo;
import jd.core.model.classfile.attribute.ElementValueEnumConstValue;
import jd.core.model.classfile.attribute.ElementValuePrimitiveType;
import jd.core.model.reference.ReferenceMap;
import jd.core.printer.Printer;
import jd.core.process.writer.AnnotationWriter;
import jd.core.process.writer.ElementValuePrimitiveTypeWriter;
import jd.core.process.writer.SignatureWriter;
import jd.core.util.SignatureUtil;

public class ElementValueWriter {
   public static void WriteElementValue(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, ElementValue ev) {
      ConstantPool constants = classFile.getConstantPool();
      String signature;
      switch(ev.tag) {
      case 1:
         ElementValuePrimitiveType evpt = (ElementValuePrimitiveType)ev;
         ElementValuePrimitiveTypeWriter.Write(loader, printer, referenceMap, classFile, evpt);
         break;
      case 2:
         ElementValueEnumConstValue var15 = (ElementValueEnumConstValue)ev;
         signature = constants.getConstantUtf8(var15.type_name_index);
         String constName = constants.getConstantUtf8(var15.const_name_index);
         String internalClassName = SignatureUtil.GetInternalName(signature);
         SignatureWriter.WriteSignature(loader, printer, referenceMap, classFile, signature);
         printer.print('.');
         printer.printStaticField(internalClassName, constName, signature, classFile.getThisClassName());
         break;
      case 3:
         ElementValueClassInfo evci = (ElementValueClassInfo)ev;
         signature = constants.getConstantUtf8(evci.class_info_index);
         SignatureWriter.WriteSignature(loader, printer, referenceMap, classFile, signature);
         printer.print('.');
         printer.printKeyword("class");
         break;
      case 4:
         ElementValueAnnotationValue evav = (ElementValueAnnotationValue)ev;
         AnnotationWriter.WriteAnnotation(loader, printer, referenceMap, classFile, evav.annotation_value);
         break;
      case 5:
         ElementValueArrayValue evarv = (ElementValueArrayValue)ev;
         ElementValue[] values = evarv.values;
         printer.print('{');
         if(values != null && values.length > 0) {
            WriteElementValue(loader, printer, referenceMap, classFile, values[0]);

            for(int evecv = 1; evecv < values.length; ++evecv) {
               printer.print(", ");
               WriteElementValue(loader, printer, referenceMap, classFile, values[evecv]);
            }
         }

         printer.print('}');
      }

   }
}
