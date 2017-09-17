package jd.core.process.writer;

import jd.core.loader.Loader;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.constant.ConstantDouble;
import jd.core.model.classfile.constant.ConstantFloat;
import jd.core.model.classfile.constant.ConstantInteger;
import jd.core.model.classfile.constant.ConstantLong;
import jd.core.model.classfile.constant.ConstantString;
import jd.core.model.classfile.constant.ConstantValue;
import jd.core.model.reference.ReferenceMap;
import jd.core.printer.Printer;
import jd.core.process.writer.SignatureWriter;
import jd.core.util.StringUtil;

public class ConstantValueWriter {
   public static void Write(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, ConstantValue cv) {
      Write(loader, printer, referenceMap, classFile, cv, 0);
   }

   public static void Write(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, ConstantValue cv, byte constantIntegerType) {
      ConstantPool constants = classFile.getConstantPool();
      String escapedString;
      String scopeInternalName;
      switch(cv.tag) {
      case 3:
         int s4 = ((ConstantInteger)cv).bytes;
         switch(constantIntegerType) {
         case 67:
            escapedString = StringUtil.EscapeCharAndAppendApostrophe((char)s4);
            scopeInternalName = classFile.getThisClassName();
            printer.printString(escapedString, scopeInternalName);
            return;
         case 90:
            printer.printKeyword(s4 == 0?"false":"true");
            return;
         default:
            if(s4 == Integer.MIN_VALUE) {
               Write(loader, printer, referenceMap, classFile, "java/lang/Integer", "MIN_VALUE", "I");
               return;
            } else {
               if(s4 == Integer.MAX_VALUE) {
                  Write(loader, printer, referenceMap, classFile, "java/lang/Integer", "MAX_VALUE", "I");
               } else {
                  printer.printNumeric(String.valueOf(s4));
               }

               return;
            }
         }
      case 4:
         float s3 = ((ConstantFloat)cv).bytes;
         if(s3 == Float.POSITIVE_INFINITY) {
            Write(loader, printer, referenceMap, classFile, "java/lang/Float", "POSITIVE_INFINITY", "F");
         } else if(s3 == Float.NEGATIVE_INFINITY) {
            Write(loader, printer, referenceMap, classFile, "java/lang/Float", "NEGATIVE_INFINITY", "F");
         } else if(s3 == Float.NaN) {
            Write(loader, printer, referenceMap, classFile, "java/lang/Float", "NaN", "F");
         } else if(s3 == Float.MAX_VALUE) {
            Write(loader, printer, referenceMap, classFile, "java/lang/Float", "MAX_VALUE", "F");
         } else if(s3 == Float.MIN_VALUE) {
            Write(loader, printer, referenceMap, classFile, "java/lang/Float", "MIN_VALUE", "F");
         } else {
            escapedString = String.valueOf(s3);
            if(escapedString.indexOf(46) == -1) {
               escapedString = escapedString + ".0";
            }

            printer.printNumeric(escapedString + 'F');
         }
         break;
      case 5:
         long s2 = ((ConstantLong)cv).bytes;
         if(s2 == Long.MIN_VALUE) {
            Write(loader, printer, referenceMap, classFile, "java/lang/Long", "MIN_VALUE", "J");
         } else if(s2 == Long.MAX_VALUE) {
            Write(loader, printer, referenceMap, classFile, "java/lang/Long", "MAX_VALUE", "J");
         } else {
            printer.printNumeric(String.valueOf(s2) + 'L');
         }
         break;
      case 6:
         double s1 = ((ConstantDouble)cv).bytes;
         if(s1 == Double.POSITIVE_INFINITY) {
            Write(loader, printer, referenceMap, classFile, "java/lang/Double", "POSITIVE_INFINITY", "D");
         } else if(s1 == Double.NEGATIVE_INFINITY) {
            Write(loader, printer, referenceMap, classFile, "java/lang/Double", "NEGATIVE_INFINITY", "D");
         } else if(s1 == Double.NaN) {
            Write(loader, printer, referenceMap, classFile, "java/lang/Double", "NaN", "D");
         } else if(s1 == Double.MAX_VALUE) {
            Write(loader, printer, referenceMap, classFile, "java/lang/Double", "MAX_VALUE", "D");
         } else if(s1 == Double.MIN_VALUE) {
            Write(loader, printer, referenceMap, classFile, "java/lang/Double", "MIN_VALUE", "D");
         } else {
            scopeInternalName = String.valueOf(s1);
            if(scopeInternalName.indexOf(46) == -1) {
               scopeInternalName = scopeInternalName + ".0";
            }

            printer.printNumeric(scopeInternalName + 'D');
         }
      case 7:
      default:
         break;
      case 8:
         String s = constants.getConstantUtf8(((ConstantString)cv).string_index);
         escapedString = StringUtil.EscapeStringAndAppendQuotationMark(s);
         scopeInternalName = classFile.getThisClassName();
         printer.printString(escapedString, scopeInternalName);
      }

   }

   private static void Write(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, String internalTypeName, String name, String descriptor) {
      String className = SignatureWriter.InternalClassNameToClassName(loader, referenceMap, classFile, internalTypeName);
      String scopeInternalName = classFile.getThisClassName();
      printer.printType(internalTypeName, className, scopeInternalName);
      printer.print('.');
      printer.printStaticField(internalTypeName, name, descriptor, scopeInternalName);
   }

   public static void WriteHexa(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, ConstantValue cv) {
      switch(cv.tag) {
      case 3:
         printer.printNumeric("0x" + Integer.toHexString(((ConstantInteger)cv).bytes).toUpperCase());
         break;
      case 4:
      default:
         Write(loader, printer, referenceMap, classFile, cv, 0);
         break;
      case 5:
         printer.printNumeric("0x" + Long.toHexString(((ConstantLong)cv).bytes).toUpperCase());
      }

   }
}
