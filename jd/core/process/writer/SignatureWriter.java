package jd.core.process.writer;

import java.util.HashSet;
import jd.core.loader.Loader;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.attribute.ParameterAnnotations;
import jd.core.model.reference.ReferenceMap;
import jd.core.printer.Printer;
import jd.core.process.writer.AnnotationWriter;
import jd.core.util.CharArrayUtil;
import jd.core.util.SignatureFormatException;
import jd.core.util.SignatureUtil;

public class SignatureWriter {
   public static void WriteTypeDeclaration(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, String signature) {
      char[] caSignature = signature.toCharArray();
      int length = caSignature.length;
      printer.printTypeDeclaration(classFile.getThisClassName(), classFile.getClassName());
      WriteGenerics(loader, printer, referenceMap, classFile, caSignature, length, 0);
   }

   public static int WriteConstructor(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, String signature, String descriptor) {
      char[] caSignature = signature.toCharArray();
      return WriteSignature(loader, printer, referenceMap, classFile, caSignature, caSignature.length, 0, true, descriptor, false);
   }

   public static void WriteMethodDeclaration(HashSet<String> keywordSet, Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, Method method, String signature, boolean descriptorFlag) {
      char[] caSignature = signature.toCharArray();
      int length = caSignature.length;
      int index = 0;
      int newIndex = WriteGenerics(loader, printer, referenceMap, classFile, caSignature, length, index);
      if(newIndex != index) {
         printer.print(' ');
         index = newIndex;
      }

      if(caSignature[index] != 40) {
         throw new SignatureFormatException(signature);
      } else {
         ++index;
         ConstantPool constants = classFile.getConstantPool();
         String internalClassName = classFile.getThisClassName();
         String descriptor = constants.getConstantUtf8(method.descriptor_index);
         boolean staticMethodFlag = (method.access_flags & 8) != 0;
         if(method.name_index == constants.instanceConstructorIndex) {
            printer.printConstructorDeclaration(internalClassName, classFile.getClassName(), descriptor);
         } else {
            newIndex = index;

            while(newIndex < length && caSignature[newIndex++] != 41) {
               ;
            }

            WriteSignature(loader, printer, referenceMap, classFile, caSignature, length, newIndex, false, (String)null, false);
            printer.print(' ');
            String variableIndex = constants.getConstantUtf8(method.name_index);
            if(keywordSet.contains(variableIndex)) {
               variableIndex = "jdMethod_" + variableIndex;
            }

            if(staticMethodFlag) {
               printer.printStaticMethodDeclaration(internalClassName, variableIndex, descriptor);
            } else {
               printer.printMethodDeclaration(internalClassName, variableIndex, descriptor);
            }
         }

         printer.print('(');
         int var24 = staticMethodFlag?0:1;
         byte firstVisibleParameterIndex = 0;
         if(method.name_index == constants.instanceConstructorIndex) {
            if((classFile.access_flags & 16384) != 0) {
               if(descriptorFlag) {
                  firstVisibleParameterIndex = 2;
               } else {
                  var24 = 3;
               }
            } else if(classFile.isAInnerClass() && (classFile.access_flags & 8) == 0) {
               firstVisibleParameterIndex = 1;
            }
         }

         ParameterAnnotations[] invisibleParameterAnnotations = method.getInvisibleParameterAnnotations();
         ParameterAnnotations[] visibleParameterAnnotations = method.getVisibleParameterAnnotations();
         int parameterIndex = 0;
         int varargsParameterIndex;
         if((method.access_flags & 128) == 0) {
            varargsParameterIndex = Integer.MAX_VALUE;
         } else {
            varargsParameterIndex = SignatureUtil.GetParameterSignatureCount(signature) - 1;
         }

         while(caSignature[index] != 41) {
            char firstChar = caSignature[index];
            if(parameterIndex >= firstVisibleParameterIndex) {
               if(parameterIndex > firstVisibleParameterIndex) {
                  printer.print(", ");
               }

               if(invisibleParameterAnnotations != null) {
                  AnnotationWriter.WriteParameterAnnotation(loader, printer, referenceMap, classFile, invisibleParameterAnnotations[parameterIndex]);
               }

               if(visibleParameterAnnotations != null) {
                  AnnotationWriter.WriteParameterAnnotation(loader, printer, referenceMap, classFile, visibleParameterAnnotations[parameterIndex]);
               }

               LocalVariable lv = null;
               if(method.getLocalVariables() != null) {
                  lv = method.getLocalVariables().searchLocalVariableWithIndexAndOffset(var24, 0);
                  if(lv != null && lv.finalFlag) {
                     printer.printKeyword("final");
                     printer.print(' ');
                  }
               }

               index = WriteSignature(loader, printer, referenceMap, classFile, caSignature, length, index, false, (String)null, parameterIndex == varargsParameterIndex);
               if(lv != null) {
                  printer.print(' ');
                  if(lv.name_index == -1) {
                     printer.startOfError();
                     printer.print("???");
                     printer.endOfError();
                  } else {
                     printer.print(constants.getConstantUtf8(lv.name_index));
                  }
               } else {
                  printer.print(" arg");
                  printer.print(var24);
               }
            } else {
               index = SignatureUtil.SkipSignature(caSignature, length, index);
            }

            var24 += firstChar != 68 && firstChar != 74?1:2;
            ++parameterIndex;
         }

         printer.print(')');
      }
   }

   private static int WriteGenerics(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, char[] caSignature, int length, int index) {
      if(caSignature[index] == 60) {
         printer.print('<');
         ++index;

         while(index < length) {
            int endIndex = CharArrayUtil.IndexOf(caSignature, ':', index);
            String templateName = CharArrayUtil.Substring(caSignature, index, endIndex);
            printer.print(templateName);
            index = endIndex + 1;
            if(caSignature[index] == 58) {
               ++index;
            }

            int newIndex = SignatureUtil.SkipSignature(caSignature, length, index);
            if(!IsObjectClass(caSignature, index, newIndex)) {
               printer.print(' ');
               printer.printKeyword("extends");
               printer.print(' ');
               WriteSignature(loader, printer, referenceMap, classFile, caSignature, length, index, false, (String)null, false);
            }

            index = newIndex;
            if(caSignature[newIndex] == 62) {
               break;
            }

            printer.print(", ");
         }

         printer.print('>');
         ++index;
      }

      return index;
   }

   public static int WriteSignature(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, char[] caSignature, int length, int index) {
      return WriteSignature(loader, printer, referenceMap, classFile, caSignature, length, index, false, (String)null, false);
   }

   public static int WriteSignature(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, String signature) {
      char[] caSignature = signature.toCharArray();
      return WriteSignature(loader, printer, referenceMap, classFile, caSignature, caSignature.length, 0, false, (String)null, false);
   }

   private static int WriteSignature(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, char[] caSignature, int length, int index, boolean constructorFlag, String constructorDescriptor, boolean varargsFlag) {
      while(true) {
         int dimensionLength = 0;
         if(caSignature[index] == 91) {
            ++dimensionLength;

            label121:
            while(true) {
               while(true) {
                  ++index;
                  if(index >= length) {
                     break label121;
                  }

                  if(caSignature[index] == 76 && index + 1 < length && caSignature[index + 1] == 91) {
                     ++index;
                     --length;
                     ++dimensionLength;
                  } else {
                     if(caSignature[index] != 91) {
                        break label121;
                     }

                     ++dimensionLength;
                  }
               }
            }
         }

         int beginIndex;
         switch(caSignature[index]) {
         case '*':
            printer.print('?');
            ++index;
            break;
         case '+':
            printer.print("? ");
            printer.printKeyword("extends");
            printer.print(' ');
            index = WriteSignature(loader, printer, referenceMap, classFile, caSignature, length, index + 1, false, (String)null, false);
            break;
         case '-':
            printer.print("? ");
            printer.printKeyword("super");
            printer.print(' ');
            index = WriteSignature(loader, printer, referenceMap, classFile, caSignature, length, index + 1, false, (String)null, false);
            break;
         case '.':
         case 'L':
            boolean typeFlag = caSignature[index] == 76;
            ++index;
            beginIndex = index;

            char c;
            for(c = 46; index < length; ++index) {
               c = caSignature[index];
               if(c == 59 || c == 60) {
                  break;
               }
            }

            String internalClassName = CharArrayUtil.Substring(caSignature, beginIndex, index);
            if(typeFlag) {
               String thisClassName = classFile.getThisClassName();
               if(constructorFlag) {
                  printer.printConstructor(internalClassName, InternalClassNameToClassName(loader, referenceMap, classFile, internalClassName), constructorDescriptor, thisClassName);
               } else {
                  printer.printType(internalClassName, InternalClassNameToClassName(loader, referenceMap, classFile, internalClassName), thisClassName);
               }
            } else {
               printer.print(InternalClassNameToClassName(loader, referenceMap, classFile, internalClassName));
            }

            if(c == 60) {
               printer.print('<');

               for(index = WriteSignature(loader, printer, referenceMap, classFile, caSignature, length, index + 1, false, (String)null, false); caSignature[index] != 62; index = WriteSignature(loader, printer, referenceMap, classFile, caSignature, length, index, false, (String)null, false)) {
                  printer.print(", ");
               }

               printer.print('>');
               ++index;
            }

            if(caSignature[index] == 59) {
               ++index;
            }
            break;
         case 'B':
            printer.printKeyword("byte");
            ++index;
            break;
         case 'C':
            printer.printKeyword("char");
            ++index;
            break;
         case 'D':
            printer.printKeyword("double");
            ++index;
            break;
         case 'F':
            printer.printKeyword("float");
            ++index;
            break;
         case 'I':
            printer.printKeyword("int");
            ++index;
            break;
         case 'J':
            printer.printKeyword("long");
            ++index;
            break;
         case 'S':
            printer.printKeyword("short");
            ++index;
            break;
         case 'T':
            ++index;
            beginIndex = index;
            index = CharArrayUtil.IndexOf(caSignature, ';', index);
            printer.print(new String(caSignature, beginIndex, index - beginIndex));
            ++index;
            break;
         case 'V':
            printer.printKeyword("void");
            ++index;
            break;
         case 'X':
         case 'Y':
            printer.printKeyword("int");
            System.err.println("<UNDEFINED>");
            ++index;
            break;
         case 'Z':
            printer.printKeyword("boolean");
            ++index;
            break;
         default:
            (new Throwable("SignatureWriter.WriteSignature: invalid signature \'" + String.valueOf(caSignature) + "\'")).printStackTrace();
         }

         if(varargsFlag) {
            if(dimensionLength > 0) {
               while(true) {
                  --dimensionLength;
                  if(dimensionLength <= 0) {
                     printer.print("...");
                     break;
                  }

                  printer.print("[]");
               }
            }
         } else {
            while(dimensionLength-- > 0) {
               printer.print("[]");
            }
         }

         if(index >= length || caSignature[index] != 46) {
            return index;
         }

         printer.print('.');
      }
   }

   public static String InternalClassNameToClassName(Loader loader, ReferenceMap referenceMap, ClassFile classFile, String internalName) {
      int index;
      if(classFile.getThisClassName().equals(internalName)) {
         index = internalName.lastIndexOf(36);
         if(index >= 0) {
            internalName = internalName.substring(index + 1);
         } else {
            index = internalName.lastIndexOf(47);
            if(index >= 0) {
               internalName = internalName.substring(index + 1);
            }
         }
      } else {
         index = internalName.lastIndexOf(47);
         if(index != -1) {
            String internalPackageName = internalName.substring(0, index);
            if(classFile.getInternalPackageName().equals(internalPackageName)) {
               if(classFile.getInnerClassFile(internalName) != null) {
                  internalName = internalName.substring(classFile.getThisClassName().length() + 1);
               } else {
                  internalName = internalName.substring(index + 1);
               }
            } else if(referenceMap.contains(internalName)) {
               internalName = internalName.substring(index + 1);
            } else if("java/lang".equals(internalPackageName)) {
               String internalClassName = internalName.substring(index + 1);
               String currentPackageNamePlusInternalClassName = classFile.getInternalPackageName() + '/' + internalClassName + ".class";
               if(loader.canLoad(currentPackageNamePlusInternalClassName)) {
                  internalName = internalName.replace('/', '.');
               } else {
                  internalName = internalClassName;
               }
            } else {
               internalName = internalName.replace('/', '.');
            }
         }
      }

      return internalName.replace('$', '.');
   }

   public static String InternalClassNameToShortClassName(ReferenceMap referenceMap, ClassFile classFile, String internalClassName) {
      int index = internalClassName.lastIndexOf(47);
      if(index != -1) {
         String aPackageName = internalClassName.substring(0, index);
         if(classFile.getInternalPackageName().equals(aPackageName)) {
            internalClassName = internalClassName.substring(index + 1);
         } else if(referenceMap.contains(internalClassName)) {
            internalClassName = internalClassName.substring(index + 1);
         } else {
            internalClassName = internalClassName.replace('/', '.');
         }
      }

      return internalClassName.replace('$', '.');
   }

   private static boolean IsObjectClass(char[] caSignature, int beginIndex, int endIndex) {
      int length = "Ljava/lang/Object;".length();
      return endIndex - beginIndex == length?CharArrayUtil.Substring(caSignature, beginIndex, endIndex).equals("Ljava/lang/Object;"):false;
   }
}
