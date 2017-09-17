package jd.core.process.writer;

import jd.core.loader.Loader;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.attribute.ElementValuePrimitiveType;
import jd.core.model.classfile.constant.ConstantValue;
import jd.core.model.reference.ReferenceMap;
import jd.core.printer.Printer;
import jd.core.process.writer.ConstantValueWriter;
import jd.core.util.StringUtil;

public class ElementValuePrimitiveTypeWriter {
   public static void Write(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, ElementValuePrimitiveType evpt) {
      ConstantPool constants = classFile.getConstantPool();
      if(evpt.type == 115) {
         String cv = constants.getConstantUtf8(evpt.const_value_index);
         String escapedString = StringUtil.EscapeStringAndAppendQuotationMark(cv);
         printer.printString(escapedString, classFile.getThisClassName());
      } else {
         ConstantValue cv1 = constants.getConstantValue(evpt.const_value_index);
         ConstantValueWriter.Write(loader, printer, referenceMap, classFile, cv1, evpt.type);
      }

   }
}
