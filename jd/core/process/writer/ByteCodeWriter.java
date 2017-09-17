package jd.core.process.writer;

import jd.core.loader.Loader;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.attribute.CodeException;
import jd.core.model.classfile.attribute.LineNumber;
import jd.core.model.classfile.constant.Constant;
import jd.core.model.classfile.constant.ConstantClass;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.reference.ReferenceMap;
import jd.core.printer.Printer;
import jd.core.process.analyzer.instruction.bytecode.util.ByteCodeUtil;
import jd.core.process.writer.SignatureWriter;

public class ByteCodeWriter {
   private static final String CORRUPTED_CONSTANT_POOL = "Corrupted_Constant_Pool";

   public static void Write(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, Method method) {
      byte[] code = method.getCode();
      if(code != null) {
         int length = code.length;
         boolean ioperande = false;
         boolean soperande = false;
         printer.startOfComment();
         ConstantPool constants = classFile.getConstantPool();
         printer.print("// Byte code:");

         label70:
         for(int index = 0; index < length; ++index) {
            int offset = index;
            int opcode = code[index] & 255;
            printer.endOfLine();
            printer.startOfLine(Instruction.UNKNOWN_LINE_NUMBER);
            printer.print("//   ");
            printer.print(index);
            printer.print(": ");
            printer.print(ByteCodeConstants.OPCODE_NAMES[opcode]);
            int var10000;
            switch(ByteCodeConstants.NO_OF_OPERANDS[opcode]) {
            case 1:
               printer.print(" ");
               switch(opcode) {
               case 188:
                  ++index;
                  printer.print(ByteCodeConstants.TYPE_NAMES[code[index] & 16]);
                  continue;
               default:
                  ++index;
                  printer.print(code[index]);
                  continue;
               }
            case 2:
               printer.print(" ");
               int var18;
               switch(opcode) {
               case 132:
                  ++index;
                  printer.print(code[index]);
                  printer.print(" ");
                  ++index;
                  printer.print(code[index]);
                  continue;
               case 153:
               case 154:
               case 155:
               case 156:
               case 157:
               case 158:
               case 159:
               case 160:
               case 161:
               case 162:
               case 163:
               case 164:
               case 165:
               case 166:
               case 167:
               case 168:
               case 198:
               case 199:
                  ++index;
                  var10000 = (code[index] & 255) << 8;
                  ++index;
                  short var19 = (short)(var10000 | code[index] & 255);
                  if(var19 >= 0) {
                     printer.print('+');
                  }

                  printer.print((int)var19);
                  printer.print(" -> ");
                  printer.print(index + var19 - 2);
                  continue;
               case 178:
               case 179:
               case 180:
               case 181:
               case 285:
                  ++index;
                  var10000 = (code[index] & 255) << 8;
                  ++index;
                  var18 = var10000 | code[index] & 255;
                  printer.print(var18);
                  printer.print("\t");
                  String jump = GetConstantFieldName(constants, var18);
                  if(jump == null) {
                     printer.startOfError();
                     printer.print("Corrupted_Constant_Pool");
                     printer.endOfError();
                  } else {
                     printer.print(jump);
                  }
                  continue;
               case 182:
               case 183:
               case 184:
                  ++index;
                  var10000 = (code[index] & 255) << 8;
                  ++index;
                  var18 = var10000 | code[index] & 255;
                  printer.print(var18);
                  printer.print("\t");
                  String low = GetConstantMethodName(constants, var18);
                  if(low == null) {
                     printer.startOfError();
                     printer.print("Corrupted_Constant_Pool");
                     printer.endOfError();
                  } else {
                     printer.print(low);
                  }
                  continue;
               case 187:
               case 189:
               case 192:
                  ++index;
                  var10000 = (code[index] & 255) << 8;
                  ++index;
                  var18 = var10000 | code[index] & 255;
                  printer.print(var18);
                  printer.print("\t");
                  Constant high = constants.get(var18);
                  if(high.tag == 7) {
                     ConstantClass npairs = (ConstantClass)high;
                     printer.print(constants.getConstantUtf8(npairs.name_index));
                  } else {
                     printer.print("Corrupted_Constant_Pool");
                  }
                  continue;
               default:
                  ++index;
                  var10000 = (code[index] & 255) << 8;
                  ++index;
                  var18 = var10000 | code[index] & 255;
                  printer.print(var18);
                  continue;
               }
            default:
               int var10001;
               int j;
               int var20;
               int var23;
               switch(opcode) {
               case 170:
                  index = (index + 4 & '￼') - 1;
                  printer.print("\tdefault:+");
                  ++index;
                  var10000 = (code[index] & 255) << 24;
                  ++index;
                  var10000 |= (code[index] & 255) << 16;
                  ++index;
                  var10000 |= (code[index] & 255) << 8;
                  ++index;
                  var20 = var10000 | code[index] & 255;
                  printer.print(var20);
                  printer.print("->");
                  printer.print(offset + var20);
                  ++index;
                  var10000 = (code[index] & 255) << 24;
                  ++index;
                  var10000 |= (code[index] & 255) << 16;
                  ++index;
                  var10000 |= (code[index] & 255) << 8;
                  ++index;
                  int var21 = var10000 | code[index] & 255;
                  ++index;
                  var10000 = (code[index] & 255) << 24;
                  ++index;
                  var10000 |= (code[index] & 255) << 16;
                  ++index;
                  var10000 |= (code[index] & 255) << 8;
                  ++index;
                  int var22 = var10000 | code[index] & 255;
                  var23 = var21;

                  while(true) {
                     if(var23 > var22) {
                        continue label70;
                     }

                     printer.print(", ");
                     printer.print(var23);
                     printer.print(":+");
                     ++index;
                     var10000 = (code[index] & 255) << 24;
                     ++index;
                     var10000 |= (code[index] & 255) << 16;
                     ++index;
                     var10000 |= (code[index] & 255) << 8;
                     ++index;
                     var20 = var10000 | code[index] & 255;
                     printer.print(var20);
                     printer.print("->");
                     printer.print(offset + var20);
                     ++var23;
                  }
               case 171:
                  index = (index + 4 & '￼') - 1;
                  printer.print("\tdefault:+");
                  ++index;
                  var10000 = (code[index] & 255) << 24;
                  ++index;
                  var10000 |= (code[index] & 255) << 16;
                  ++index;
                  var10000 |= (code[index] & 255) << 8;
                  ++index;
                  var20 = var10000 | code[index] & 255;
                  printer.print(var20);
                  printer.print("->");
                  printer.print(offset + var20);
                  ++index;
                  var10000 = (code[index] & 255) << 24;
                  ++index;
                  var10000 |= (code[index] & 255) << 16;
                  ++index;
                  var10000 |= (code[index] & 255) << 8;
                  ++index;
                  var23 = var10000 | code[index] & 255;
                  j = 0;

                  while(true) {
                     if(j >= var23) {
                        continue label70;
                     }

                     printer.print(", ");
                     ++index;
                     var10001 = (code[index] & 255) << 24;
                     ++index;
                     var10001 |= (code[index] & 255) << 16;
                     ++index;
                     var10001 |= (code[index] & 255) << 8;
                     ++index;
                     printer.print(var10001 | code[index] & 255);
                     printer.print(":+");
                     ++index;
                     var10000 = (code[index] & 255) << 24;
                     ++index;
                     var10000 |= (code[index] & 255) << 16;
                     ++index;
                     var10000 |= (code[index] & 255) << 8;
                     ++index;
                     var20 = var10000 | code[index] & 255;
                     printer.print(var20);
                     printer.print("->");
                     printer.print(offset + var20);
                     ++j;
                  }
               case 185:
                  printer.print(" ");
                  ++index;
                  var10001 = (code[index] & 255) << 8;
                  ++index;
                  printer.print(var10001 | code[index] & 255);
                  printer.print(" ");
                  ++index;
                  printer.print(code[index]);
                  printer.print(" ");
                  ++index;
                  printer.print(code[index]);
                  break;
               case 196:
                  index = ByteCodeUtil.NextWideOffset(code, index);
                  break;
               case 197:
                  printer.print(" ");
                  ++index;
                  var10001 = (code[index] & 255) << 8;
                  ++index;
                  printer.print(var10001 | code[index] & 255);
                  printer.print(" ");
                  ++index;
                  printer.print(code[index]);
                  break;
               default:
                  for(j = ByteCodeConstants.NO_OF_OPERANDS[opcode]; j > 0; --j) {
                     printer.print(" ");
                     ++index;
                     printer.print(code[index]);
                  }
               }
            }
         }

         WriteAttributeNumberTables(printer, method);
         WriteAttributeLocalVariableTables(loader, printer, referenceMap, classFile, method);
         WriteCodeExceptions(printer, referenceMap, classFile, method);
         printer.endOfComment();
      }

   }

   private static void WriteAttributeNumberTables(Printer printer, Method method) {
      LineNumber[] lineNumbers = method.getLineNumbers();
      if(lineNumbers != null) {
         printer.endOfLine();
         printer.startOfLine(Instruction.UNKNOWN_LINE_NUMBER);
         printer.print("// Line number table:");

         for(int i = 0; i < lineNumbers.length; ++i) {
            printer.endOfLine();
            printer.startOfLine(Instruction.UNKNOWN_LINE_NUMBER);
            printer.print("//   Java source line #");
            printer.print(lineNumbers[i].line_number);
            printer.print("\t-> byte code offset #");
            printer.print(lineNumbers[i].start_pc);
         }
      }

   }

   private static void WriteAttributeLocalVariableTables(Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile, Method method) {
      LocalVariables localVariables = method.getLocalVariables();
      if(localVariables != null) {
         int length = localVariables.size();
         printer.endOfLine();
         printer.startOfLine(Instruction.UNKNOWN_LINE_NUMBER);
         printer.print("// Local variable table:");
         printer.endOfLine();
         printer.startOfLine(Instruction.UNKNOWN_LINE_NUMBER);
         printer.print("//   start\tlength\tslot\tname\tsignature");
         ConstantPool constants = classFile.getConstantPool();

         for(int i = 0; i < length; ++i) {
            LocalVariable lv = localVariables.getLocalVariableAt(i);
            if(lv != null) {
               printer.endOfLine();
               printer.startOfLine(Instruction.UNKNOWN_LINE_NUMBER);
               printer.print("//   ");
               printer.print(lv.start_pc);
               printer.print("\t");
               printer.print(lv.length);
               printer.print("\t");
               printer.print(lv.index);
               printer.print("\t");
               if(lv.name_index > 0) {
                  printer.print(constants.getConstantUtf8(lv.name_index));
               } else {
                  printer.print("???");
               }

               printer.print("\t");
               if(lv.signature_index > 0) {
                  SignatureWriter.WriteSignature(loader, printer, referenceMap, classFile, constants.getConstantUtf8(lv.signature_index));
               } else {
                  printer.print("???");
               }
            }
         }
      }

   }

   private static void WriteCodeExceptions(Printer printer, ReferenceMap referenceMap, ClassFile classFile, Method method) {
      CodeException[] codeExceptions = method.getCodeExceptions();
      if(codeExceptions != null && codeExceptions.length > 0) {
         printer.endOfLine();
         printer.startOfLine(Instruction.UNKNOWN_LINE_NUMBER);
         printer.print("// Exception table:");
         printer.endOfLine();
         printer.startOfLine(Instruction.UNKNOWN_LINE_NUMBER);
         printer.print("//   from\tto\ttarget\ttype");

         for(int i = 0; i < codeExceptions.length; ++i) {
            printer.endOfLine();
            printer.startOfLine(Instruction.UNKNOWN_LINE_NUMBER);
            printer.print("//   ");
            printer.print(codeExceptions[i].start_pc);
            printer.print("\t");
            printer.print(codeExceptions[i].end_pc);
            printer.print("\t");
            printer.print(codeExceptions[i].handler_pc);
            printer.print("\t");
            if(codeExceptions[i].catch_type == 0) {
               printer.print("finally");
            } else {
               printer.print(classFile.getConstantPool().getConstantClassName(codeExceptions[i].catch_type));
            }
         }
      }

   }

   private static String GetConstantFieldName(ConstantPool constants, int index) {
      Constant c = constants.get(index);
      switch(c.tag) {
      case 9:
         ConstantFieldref cfr = (ConstantFieldref)c;
         c = constants.get(cfr.class_index);
         switch(c.tag) {
         case 7:
            ConstantClass cc = (ConstantClass)c;
            String classPath = constants.getConstantUtf8(cc.name_index);
            ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
            String fieldName = constants.getConstantUtf8(cnat.name_index);
            String fieldDescriptor = constants.getConstantUtf8(cnat.descriptor_index);
            return classPath + ':' + fieldName + "\t" + fieldDescriptor;
         default:
            return null;
         }
      default:
         return null;
      }
   }

   private static String GetConstantMethodName(ConstantPool constants, int index) {
      Constant c = constants.get(index);
      switch(c.tag) {
      case 10:
      case 11:
         ConstantMethodref cfr = (ConstantMethodref)c;
         c = constants.get(cfr.class_index);
         switch(c.tag) {
         case 7:
            ConstantClass cc = (ConstantClass)c;
            String classPath = constants.getConstantUtf8(cc.name_index);
            ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
            String fieldName = constants.getConstantUtf8(cnat.name_index);
            String fieldDescriptor = constants.getConstantUtf8(cnat.descriptor_index);
            return classPath + ':' + fieldName + "\t" + fieldDescriptor;
         default:
            return null;
         }
      default:
         return null;
      }
   }
}
