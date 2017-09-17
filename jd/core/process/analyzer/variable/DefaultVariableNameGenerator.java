package jd.core.process.analyzer.variable;

import java.util.HashSet;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Field;
import jd.core.process.analyzer.variable.VariableNameGenerator;

public class DefaultVariableNameGenerator implements VariableNameGenerator {
   private HashSet<String> fieldNames = new HashSet();
   private HashSet<String> localNames = new HashSet();

   public DefaultVariableNameGenerator(ClassFile classFile) {
      Field[] fields = classFile.getFields();
      if(fields != null) {
         for(int i = 0; i < fields.length; ++i) {
            this.fieldNames.add(classFile.getConstantPool().getConstantUtf8(fields[i].name_index));
         }
      }

   }

   public void clearLocalNames() {
      this.localNames.clear();
   }

   public String generateParameterNameFromSignature(String signature, boolean appearsOnceFlag, boolean varargsFlag, int anonymousClassDepth) {
      String prefix;
      switch(anonymousClassDepth) {
      case 0:
         prefix = "param";
         break;
      case 1:
         prefix = "paramAnonymous";
         break;
      default:
         prefix = "paramAnonymous" + anonymousClassDepth;
      }

      if(varargsFlag) {
         return prefix + "VarArgs";
      } else {
         int index = CountDimensionOfArray(signature);
         if(index > 0) {
            prefix = prefix + "ArrayOf";
         }

         return this.generateValidName(prefix + GetSuffixFromSignature(signature.substring(index)), appearsOnceFlag);
      }
   }

   public String generateLocalVariableNameFromSignature(String signature, boolean appearsOnce) {
      int index = CountDimensionOfArray(signature);
      if(index > 0) {
         return this.generateValidName("arrayOf" + GetSuffixFromSignature(signature.substring(index)), appearsOnce);
      } else {
         switch(signature.charAt(0)) {
         case 'B':
            return this.generateValidName("b", appearsOnce);
         case 'C':
            return this.generateValidName("c", appearsOnce);
         case 'D':
            return this.generateValidName("d", appearsOnce);
         case 'F':
            return this.generateValidName("f", appearsOnce);
         case 'I':
            return this.generateValidIntName(appearsOnce);
         case 'J':
            return this.generateValidName("l", appearsOnce);
         case 'L':
            String s = FormatSignature(signature);
            if(s.equals("String")) {
               return this.generateValidName("str", appearsOnce);
            }

            return this.generateValidName("local" + s, appearsOnce);
         case 'S':
            return this.generateValidName("s", appearsOnce);
         case 'Z':
            return this.generateValidName("bool", appearsOnce);
         default:
            (new Throwable("NameGenerator.generateParameterNameFromSignature: invalid signature \'" + signature + "\'")).printStackTrace();
            return "?";
         }
      }
   }

   private static int CountDimensionOfArray(String signature) {
      int index = 0;
      int length = signature.length();
      if(signature.charAt(index) == 91) {
         while(true) {
            while(true) {
               ++index;
               if(index >= length) {
                  return index;
               }

               if(signature.charAt(index) == 76 && index + 1 < length && signature.charAt(index + 1) == 91) {
                  ++index;
                  --length;
               } else if(signature.charAt(index) != 91) {
                  return index;
               }
            }
         }
      } else {
         return index;
      }
   }

   private static String GetSuffixFromSignature(String signature) {
      switch(signature.charAt(0)) {
      case 'B':
         return "Byte";
      case 'C':
         return "Char";
      case 'D':
         return "Double";
      case 'E':
      case 'G':
      case 'H':
      case 'K':
      case 'M':
      case 'N':
      case 'O':
      case 'P':
      case 'Q':
      case 'R':
      case 'U':
      case 'V':
      case 'W':
      case 'X':
      case 'Y':
      default:
         (new Throwable("NameGenerator.generateParameterNameFromSignature: invalid signature \'" + signature + "\'")).printStackTrace();
         return "?";
      case 'F':
         return "Float";
      case 'I':
         return "Int";
      case 'J':
         return "Long";
      case 'L':
         return FormatSignature(signature);
      case 'S':
         return "Short";
      case 'T':
         return FormatTemplate(signature);
      case 'Z':
         return "Boolean";
      case '[':
         return "Array";
      }
   }

   private static String FormatSignature(String signature) {
      signature = signature.substring(1, signature.length() - 1);
      int index = signature.indexOf(60);
      if(index != -1) {
         signature = signature.substring(0, index);
      }

      index = signature.lastIndexOf(36);
      if(index != -1) {
         signature = signature.substring(index + 1);
      }

      index = signature.lastIndexOf(47);
      if(index != -1) {
         signature = signature.substring(index + 1);
      }

      return signature;
   }

   private static String FormatTemplate(String signature) {
      return signature.substring(1, signature.length() - 1);
   }

   private String generateValidName(String name, boolean appearsOnceFlag) {
      if(Character.isUpperCase(name.charAt(0))) {
         name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
      }

      if(appearsOnceFlag && !this.fieldNames.contains(name) && !this.localNames.contains(name)) {
         this.localNames.add(name);
         return name;
      } else {
         int index = 1;

         while(true) {
            String newName = name + index;
            if(!this.fieldNames.contains(newName) && !this.localNames.contains(newName)) {
               this.localNames.add(newName);
               return newName;
            }

            ++index;
         }
      }
   }

   private String generateValidIntName(boolean appearsOnce) {
      if(!this.fieldNames.contains("i") && !this.localNames.contains("i")) {
         this.localNames.add("i");
         return "i";
      } else if(!this.fieldNames.contains("j") && !this.localNames.contains("j")) {
         this.localNames.add("j");
         return "j";
      } else if(!this.fieldNames.contains("k") && !this.localNames.contains("k")) {
         this.localNames.add("k");
         return "k";
      } else if(!this.fieldNames.contains("m") && !this.localNames.contains("m")) {
         this.localNames.add("m");
         return "m";
      } else if(!this.fieldNames.contains("n") && !this.localNames.contains("n")) {
         this.localNames.add("n");
         return "n";
      } else {
         return this.generateValidName("i", false);
      }
   }
}
