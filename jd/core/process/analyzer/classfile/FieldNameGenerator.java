package jd.core.process.analyzer.classfile;

import jd.core.util.CharArrayUtil;

public class FieldNameGenerator {
   public static String GenerateName(String signature, String name) {
      StringBuffer sbName = new StringBuffer("jdField_");
      sbName.append(name);
      sbName.append("_of_type");
      char[] caSignature = signature.toCharArray();
      int length = caSignature.length;
      GenerateName(sbName, caSignature, length, 0);
      return sbName.toString();
   }

   private static int GenerateName(StringBuffer sbName, char[] caSignature, int length, int index) {
      sbName.append('_');

      while(true) {
         int nbrOfDimensions;
         for(nbrOfDimensions = 0; caSignature[index] == 91; ++nbrOfDimensions) {
            ++index;
         }

         if(nbrOfDimensions > 0) {
            sbName.append("Array");
            if(nbrOfDimensions > 1) {
               sbName.append(nbrOfDimensions);
               sbName.append('d');
            }

            sbName.append("Of");
         }

         int beginIndex;
         switch(caSignature[index]) {
         case '*':
            sbName.append('X');
            ++index;
            break;
         case '+':
            sbName.append("_extends_");
            index = GenerateName(sbName, caSignature, length, index + 1);
            break;
         case '-':
            sbName.append("_super_");
            index = GenerateName(sbName, caSignature, length, index + 1);
            break;
         case '.':
         case 'L':
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
            InternalClassNameToCapitalizedClassName(sbName, internalClassName);
            if(c == 60) {
               sbName.append("_of");

               for(index = GenerateName(sbName, caSignature, length, index + 1); caSignature[index] != 62; index = GenerateName(sbName, caSignature, length, index)) {
                  sbName.append("_and");
               }

               ++index;
            }

            if(caSignature[index] == 59) {
               ++index;
            }
            break;
         case 'B':
            sbName.append("Byte");
            ++index;
            break;
         case 'C':
            sbName.append("Char");
            ++index;
            break;
         case 'D':
            sbName.append("Double");
            ++index;
            break;
         case 'F':
            sbName.append("Float");
            ++index;
            break;
         case 'I':
            sbName.append("Int");
            ++index;
            break;
         case 'J':
            sbName.append("Long");
            ++index;
            break;
         case 'S':
            sbName.append("Short");
            ++index;
            break;
         case 'T':
            ++index;
            beginIndex = index;
            index = CharArrayUtil.IndexOf(caSignature, ';', index);
            sbName.append(caSignature, beginIndex, index - beginIndex);
            ++index;
            break;
         case 'V':
            sbName.append("Void");
            ++index;
            break;
         case 'X':
         case 'Y':
            sbName.append('X');
            System.err.println("<UNDEFINED>");
            ++index;
            break;
         case 'Z':
            sbName.append("Boolean");
            ++index;
            break;
         default:
            (new Throwable("SignatureWriter.WriteSignature: invalid signature \'" + String.valueOf(caSignature) + "\'")).printStackTrace();
         }

         if(index >= length || caSignature[index] != 46) {
            return index;
         }

         sbName.append("_");
      }
   }

   private static void InternalClassNameToCapitalizedClassName(StringBuffer sbName, String internalClassName) {
      int index1 = 0;

      for(int index2 = internalClassName.indexOf(47); index2 != -1; index2 = internalClassName.indexOf(47, index1)) {
         sbName.append(Character.toUpperCase(internalClassName.charAt(index1)));
         sbName.append(internalClassName.substring(index1 + 1, index2));
         index1 = index2 + 1;
      }

      sbName.append(Character.toUpperCase(internalClassName.charAt(index1)));
      sbName.append(internalClassName.substring(index1 + 1));
   }
}
