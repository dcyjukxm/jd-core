package jd.core.util;

import java.util.ArrayList;
import jd.core.util.CharArrayUtil;

public class SignatureUtil {
   public static int SkipSignature(char[] caSignature, int length, int index) {
      do {
         if(caSignature[index] == 91) {
            label73:
            while(true) {
               while(true) {
                  ++index;
                  if(index >= length) {
                     break label73;
                  }

                  if(caSignature[index] == 76 && index + 1 < length && caSignature[index + 1] == 91) {
                     ++index;
                     --length;
                  } else if(caSignature[index] != 91) {
                     break label73;
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
         case 'X':
         case 'Y':
         case 'Z':
            ++index;
            break;
         case '+':
         case '-':
            index = SkipSignature(caSignature, length, index + 1);
            break;
         case '.':
         case 'L':
            ++index;

            char c;
            for(c = 46; index < length; ++index) {
               c = caSignature[index];
               if(c == 59 || c == 60) {
                  break;
               }
            }

            if(c == 60) {
               for(index = SkipSignature(caSignature, length, index + 1); caSignature[index] != 62; index = SkipSignature(caSignature, length, index)) {
                  ;
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
      } while(index < length && caSignature[index] == 46);

      return index;
   }

   public static String GetSignatureFromType(int type) {
      switch(type) {
      case 4:
         return "Z";
      case 5:
         return "C";
      case 6:
         return "F";
      case 7:
         return "D";
      case 8:
         return "B";
      case 9:
         return "S";
      case 10:
         return "I";
      case 11:
         return "J";
      default:
         return null;
      }
   }

   public static int GetTypeFromSignature(String signature) {
      if(signature.length() != 1) {
         return 0;
      } else {
         switch(signature.charAt(0)) {
         case 'B':
            return 8;
         case 'C':
            return 5;
         case 'D':
            return 7;
         case 'F':
            return 6;
         case 'I':
            return 10;
         case 'J':
            return 11;
         case 'S':
            return 9;
         case 'Z':
            return 4;
         default:
            return 0;
         }
      }
   }

   public static boolean IsPrimitiveSignature(String signature) {
      if(signature != null && signature.length() == 1) {
         switch(signature.charAt(0)) {
         case 'B':
         case 'C':
         case 'D':
         case 'F':
         case 'I':
         case 'J':
         case 'S':
         case 'Z':
            return true;
         default:
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean IsIntegerSignature(String signature) {
      if(signature != null && signature.length() == 1) {
         switch(signature.charAt(0)) {
         case 'B':
         case 'C':
         case 'I':
         case 'S':
            return true;
         default:
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean IsObjectSignature(String signature) {
      return signature != null && signature.length() > 2?signature.charAt(0) == 76:false;
   }

   public static String GetInternalName(String signature) {
      char[] caSignature = signature.toCharArray();
      int length = signature.length();

      int beginIndex;
      for(beginIndex = 0; beginIndex < length && caSignature[beginIndex] == 91; ++beginIndex) {
         ;
      }

      if(beginIndex < length && caSignature[beginIndex] == 76) {
         ++beginIndex;
         --length;
         return CharArrayUtil.Substring(caSignature, beginIndex, length);
      } else {
         return beginIndex == 0?signature:CharArrayUtil.Substring(caSignature, beginIndex, length);
      }
   }

   public static String CutArrayDimensionPrefix(String signature) {
      int beginIndex;
      for(beginIndex = 0; signature.charAt(beginIndex) == 91; ++beginIndex) {
         ;
      }

      return signature.substring(beginIndex);
   }

   public static int GetArrayDimensionCount(String signature) {
      int beginIndex;
      for(beginIndex = 0; signature.charAt(beginIndex) == 91; ++beginIndex) {
         ;
      }

      return beginIndex;
   }

   public static String GetInnerName(String signature) {
      signature = CutArrayDimensionPrefix(signature);
      switch(signature.charAt(0)) {
      case 'L':
      case 'T':
         return signature.substring(1, signature.length() - 1);
      default:
         return signature;
      }
   }

   public static ArrayList<String> GetParameterSignatures(String methodSignature) {
      char[] caSignature = methodSignature.toCharArray();
      int length = caSignature.length;
      ArrayList parameterTypes = new ArrayList(1);
      int index = CharArrayUtil.IndexOf(caSignature, '(', 0);
      if(index != -1) {
         ++index;

         while(caSignature[index] != 41) {
            int newIndex = SkipSignature(caSignature, length, index);
            parameterTypes.add(methodSignature.substring(index, newIndex));
            index = newIndex;
         }
      }

      return parameterTypes;
   }

   public static String GetMethodReturnedSignature(String signature) {
      int index = signature.indexOf(41);
      return index == -1?null:signature.substring(index + 1);
   }

   public static int GetParameterSignatureCount(String methodSignature) {
      char[] caSignature = methodSignature.toCharArray();
      int length = caSignature.length;
      int index = CharArrayUtil.IndexOf(caSignature, '(', 0);
      int count = 0;
      if(index != -1) {
         ++index;

         while(caSignature[index] != 41) {
            int newIndex = SkipSignature(caSignature, length, index);
            index = newIndex;
            ++count;
         }
      }

      return count;
   }

   public static int CreateTypesBitField(String signature) {
      switch(signature.charAt(0)) {
      case 'B':
         return 14;
      case 'C':
         return 13;
      case 'I':
         return 8;
      case 'S':
         return 12;
      case 'X':
         return 31;
      case 'Y':
         return 15;
      case 'Z':
         return 16;
      default:
         return 0;
      }
   }

   public static int CreateArgOrReturnBitFields(String signature) {
      switch(signature.charAt(0)) {
      case 'B':
         return 2;
      case 'C':
         return 1;
      case 'I':
         return 15;
      case 'S':
         return 6;
      case 'X':
         return 31;
      case 'Y':
         return 15;
      case 'Z':
         return 16;
      default:
         return 0;
      }
   }

   public static String GetSignatureFromTypesBitField(int typesBitField) {
      return (typesBitField & 8) != 0?"I":((typesBitField & 4) != 0?"S":((typesBitField & 1) != 0?"C":((typesBitField & 2) != 0?"B":((typesBitField & 16) != 0?"Z":"I"))));
   }

   public static String CreateTypeName(String signature) {
      if(signature.length() == 0) {
         return signature;
      } else {
         switch(signature.charAt(0)) {
         case 'L':
         case 'T':
            if(signature.charAt(signature.length() - 1) == 59) {
               return signature;
            }
         default:
            return "L" + signature + ';';
         case '[':
            return signature;
         }
      }
   }
}
