package jd.core.process.analyzer.classfile;

import jd.core.model.reference.ReferenceMap;
import jd.core.util.CharArrayUtil;
import jd.core.util.SignatureFormatException;

public class SignatureAnalyzer {
   public static void AnalyzeClassSignature(ReferenceMap referenceMap, String signature) {
      try {
         char[] e = signature.toCharArray();
         int length = e.length;
         byte index = 0;
         int index1 = AnalyzeGenerics(referenceMap, e, length, index);

         for(index1 = AnalyzeSignature(referenceMap, e, length, index1); index1 < signature.length(); index1 = AnalyzeSignature(referenceMap, e, length, index1)) {
            ;
         }

      } catch (RuntimeException var5) {
         System.err.println("SignatureAnalyzer.AnalyzeClassSignature: Infinite loop, signature=" + signature);
         throw var5;
      }
   }

   public static void AnalyzeMethodSignature(ReferenceMap referenceMap, String signature) {
      try {
         char[] e = signature.toCharArray();
         int length = e.length;
         byte index = 0;
         int var6 = AnalyzeGenerics(referenceMap, e, length, index);
         if(e[var6] != 40) {
            throw new SignatureFormatException(signature);
         } else {
            ++var6;

            while(e[var6] != 41) {
               var6 = AnalyzeSignature(referenceMap, e, length, var6);
            }

            ++var6;
            AnalyzeSignature(referenceMap, e, length, var6);
         }
      } catch (RuntimeException var5) {
         System.err.println("SignatureAnalyzer.AnalyzeMethodSignature: Infinite loop, signature=" + signature);
         throw var5;
      }
   }

   public static void AnalyzeSimpleSignature(ReferenceMap referenceMap, String signature) {
      try {
         char[] e = signature.toCharArray();
         AnalyzeSignature(referenceMap, e, e.length, 0);
      } catch (RuntimeException var3) {
         System.err.println("SignatureAnalyzer.AnalyzeSimpleSignature: Infinite loop, signature=" + signature);
         throw var3;
      }
   }

   private static int AnalyzeGenerics(ReferenceMap referenceMap, char[] caSignature, int length, int index) {
      if(caSignature[index] == 60) {
         ++index;

         while(index < length) {
            index = CharArrayUtil.IndexOf(caSignature, ':', index) + 1;
            if(caSignature[index] == 58) {
               ++index;
            }

            index = AnalyzeSignature(referenceMap, caSignature, length, index);
            if(caSignature[index] == 62) {
               break;
            }
         }

         ++index;
      }

      return index;
   }

   private static int AnalyzeSignature(ReferenceMap referenceMap, char[] caSignature, int length, int index) {
      int debugCounter = 0;

      do {
         if(caSignature[index] == 91) {
            label59:
            while(true) {
               while(true) {
                  ++index;
                  if(index >= length) {
                     break label59;
                  }

                  if(caSignature[index] == 76 && index + 1 < length && caSignature[index + 1] == 91) {
                     ++index;
                     --length;
                  } else if(caSignature[index] != 91) {
                     break label59;
                  }
               }
            }
         }

         switch(caSignature[index]) {
         case '*':
         case 'B':
         case 'C':
         case 'D':
         case 'F':
         case 'I':
         case 'J':
         case 'S':
         case 'V':
         case 'Z':
            ++index;
            break;
         case '+':
         case '-':
            index = AnalyzeSignature(referenceMap, caSignature, length, index + 1);
            break;
         case '.':
         case 'L':
            boolean classFlag = caSignature[index] == 76;
            ++index;
            int beginIndex = index;

            char c;
            for(c = 46; index < length; ++index) {
               c = caSignature[index];
               if(c == 59 || c == 60) {
                  break;
               }
            }

            if(classFlag) {
               referenceMap.add(CharArrayUtil.Substring(caSignature, beginIndex, index));
            }

            if(c == 60) {
               ++index;

               while(caSignature[index] != 62) {
                  index = AnalyzeSignature(referenceMap, caSignature, length, index);
               }

               ++index;
            }

            if(caSignature[index] == 59) {
               ++index;
            }
            break;
         case 'T':
            index = CharArrayUtil.IndexOf(caSignature, ';', index + 1) + 1;
         }

         if(index >= length || caSignature[index] != 46) {
            return index;
         }

         ++debugCounter;
      } while(debugCounter <= 3000);

      throw new RuntimeException("Infinite loop");
   }
}
