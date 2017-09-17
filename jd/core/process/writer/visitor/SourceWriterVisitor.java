package jd.core.process.writer.visitor;

import java.util.HashSet;
import java.util.List;
import jd.core.loader.Loader;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.constant.Constant;
import jd.core.model.classfile.constant.ConstantClass;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.classfile.constant.ConstantUtf8;
import jd.core.model.classfile.constant.ConstantValue;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConstInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.ExceptionLoad;
import jd.core.model.instruction.bytecode.instruction.GetField;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.IInc;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Jsr;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.LookupSwitch;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.TableSwitch;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.reference.ReferenceMap;
import jd.core.printer.InstructionPrinter;
import jd.core.process.writer.ConstantValueWriter;
import jd.core.process.writer.SignatureWriter;
import jd.core.util.SignatureUtil;
import jd.core.util.StringUtil;

public class SourceWriterVisitor {
   protected Loader loader;
   protected InstructionPrinter printer;
   protected ReferenceMap referenceMap;
   protected HashSet<String> keywordSet;
   protected ConstantPool constants;
   protected LocalVariables localVariables;
   protected ClassFile classFile;
   protected int methodAccessFlags;
   protected int firstOffset;
   protected int lastOffset;
   protected int previousOffset;

   public SourceWriterVisitor(Loader loader, InstructionPrinter printer, ReferenceMap referenceMap, HashSet<String> keywordSet) {
      this.loader = loader;
      this.printer = printer;
      this.referenceMap = referenceMap;
      this.keywordSet = keywordSet;
   }

   public void init(ClassFile classFile, Method method, int firstOffset, int lastOffset) {
      this.classFile = classFile;
      this.firstOffset = firstOffset;
      this.lastOffset = lastOffset;
      this.previousOffset = 0;
      if(classFile != null && method != null) {
         this.constants = classFile.getConstantPool();
         this.methodAccessFlags = method.access_flags;
         this.localVariables = method.getLocalVariables();
      } else {
         this.constants = null;
         this.methodAccessFlags = 0;
         this.localVariables = null;
      }

   }

   public int visit(Instruction instruction) {
      int lineNumber = instruction.lineNumber;
      if(instruction.offset >= this.firstOffset && this.previousOffset <= this.lastOffset) {
         int nextOffset;
         String cnat;
         int nextOffset1;
         int var12;
         String var17;
         switch(instruction.opcode) {
         case 0:
            break;
         case 1:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.printKeyword(lineNumber, "null");
            }
            break;
         case 16:
         case 17:
         case 256:
            lineNumber = this.writeBIPush_SIPush_IConst((IConst)instruction);
            break;
         case 18:
         case 20:
            lineNumber = this.writeLcdInstruction((IndexInstruction)instruction);
            break;
         case 21:
         case 25:
         case 268:
            lineNumber = this.writeLoadInstruction((LoadInstruction)instruction);
            break;
         case 54:
         case 58:
         case 269:
            lineNumber = this.writeStoreInstruction((StoreInstruction)instruction);
            break;
         case 83:
         case 272:
            ArrayStoreInstruction var37 = (ArrayStoreInstruction)instruction;
            lineNumber = this.writeArray(var37, var37.arrayref, var37.indexref);
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= nextOffset && nextOffset <= this.lastOffset) {
               this.printer.print(lineNumber, " = ");
            }

            lineNumber = this.visit(var37, var37.valueref);
            break;
         case 87:
            lineNumber = this.visit(instruction, ((Pop)instruction).objectref);
            break;
         case 132:
            lineNumber = this.writeIInc((IInc)instruction);
            break;
         case 167:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               Goto var36 = (Goto)instruction;
               this.printer.printKeyword(lineNumber, "goto");
               this.printer.print(' ');
               this.printer.print(lineNumber, var36.GetJumpOffset());
            }
            break;
         case 168:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.printKeyword(lineNumber, "jsr");
               this.printer.print(' ');
               this.printer.print((int)((short)((Jsr)instruction).branch));
            }
            break;
         case 169:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.startOfError();
               this.printer.printKeyword(lineNumber, "ret");
               this.printer.endOfError();
            }
            break;
         case 170:
            TableSwitch var35 = (TableSwitch)instruction;
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.printKeyword(lineNumber, "switch");
               this.printer.print(" (");
            }

            lineNumber = this.visit(var35.key);
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.print(lineNumber, ')');
            }
            break;
         case 171:
            LookupSwitch var33 = (LookupSwitch)instruction;
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.printKeyword(lineNumber, "switch");
               this.printer.print(" (");
            }

            lineNumber = this.visit(var33.key);
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.print(lineNumber, ')');
            }
            break;
         case 177:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.printKeyword(lineNumber, "return");
            }
            break;
         case 178:
            lineNumber = this.writeGetStatic((GetStatic)instruction);
            break;
         case 179:
            lineNumber = this.writePutStatic((PutStatic)instruction);
            break;
         case 180:
            this.writeGetField((GetField)instruction);
            break;
         case 181:
            PutField var32 = (PutField)instruction;
            ConstantFieldref var28 = this.constants.getConstantFieldref(var32.index);
            ConstantNameAndType var21 = this.constants.getConstantNameAndType(var28.name_and_type_index);
            boolean var20 = false;
            if(this.localVariables.containsLocalVariableWithNameIndex(var21.name_index)) {
               switch(var32.objectref.opcode) {
               case 25:
                  if(((ALoad)var32.objectref).index == 0) {
                     var20 = true;
                  }
                  break;
               case 285:
                  if(!this.needAPrefixForThisField(var21.name_index, var21.descriptor_index, (GetStatic)var32.objectref)) {
                     var20 = true;
                  }
               }
            }

            if(this.firstOffset <= this.previousOffset && var32.objectref.offset <= this.lastOffset) {
               if(!var20) {
                  this.printer.addNewLinesAndPrefix(lineNumber);
                  this.printer.startOfOptionalPrefix();
               }

               lineNumber = this.visit(var32, var32.objectref);
               this.printer.print(lineNumber, '.');
               if(!var20) {
                  this.printer.endOfOptionalPrefix();
               }
            }

            String var27 = this.constants.getConstantUtf8(var21.name_index);
            if(this.keywordSet.contains(var27)) {
               var27 = "jdField_" + var27;
            }

            nextOffset1 = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset1 <= this.lastOffset) {
               String var34 = this.constants.getConstantClassName(var28.class_index);
               String descriptor = this.constants.getConstantUtf8(var21.descriptor_index);
               this.printer.printField(lineNumber, var34, var27, descriptor, this.classFile.getThisClassName());
               this.printer.print(" = ");
            }

            lineNumber = this.visit(var32, var32.valueref);
            break;
         case 182:
         case 185:
            lineNumber = this.writeInvokeNoStaticInstruction((InvokeNoStaticInstruction)instruction);
            break;
         case 183:
            lineNumber = this.writeInvokespecial((InvokeNoStaticInstruction)instruction);
            break;
         case 184:
            lineNumber = this.writeInvokestatic((Invokestatic)instruction);
            break;
         case 187:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.printKeyword(lineNumber, "new");
               this.printer.print(' ');
               this.printer.print(lineNumber, this.constants.getConstantClassName(((IndexInstruction)instruction).index));
            }
            break;
         case 188:
            NewArray var31 = (NewArray)instruction;
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.printKeyword(lineNumber, "new");
               this.printer.print(' ');
               SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, SignatureUtil.GetSignatureFromType(var31.type));
               this.printer.print(lineNumber, '[');
            }

            lineNumber = this.visit(var31.dimension);
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.print(lineNumber, ']');
            }
            break;
         case 189:
            ANewArray var30 = (ANewArray)instruction;
            Instruction var23 = var30.dimension;
            cnat = this.constants.getConstantClassName(var30.index);
            if(cnat.charAt(0) != 91) {
               cnat = SignatureUtil.CreateTypeName(cnat);
            }

            String var19 = SignatureUtil.CutArrayDimensionPrefix(cnat);
            int var25 = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && var25 <= this.lastOffset) {
               this.printer.printKeyword(lineNumber, "new");
               this.printer.print(' ');
               SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, var19);
               this.printer.print(lineNumber, '[');
            }

            lineNumber = this.visit(var23);
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.print(lineNumber, ']');
               nextOffset1 = cnat.length() - var19.length();

               for(int internalClassName = nextOffset1; internalClassName > 0; --internalClassName) {
                  this.printer.print(lineNumber, "[]");
               }
            }
            break;
         case 190:
            lineNumber = this.visit(instruction, ((ArrayLength)instruction).arrayref);
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.print(lineNumber, '.');
               this.printer.printJavaWord("length");
            }
            break;
         case 191:
            AThrow var29 = (AThrow)instruction;
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.printKeyword(lineNumber, "throw");
               this.printer.print(' ');
            }

            lineNumber = this.visit(var29, var29.value);
            break;
         case 192:
            CheckCast var26 = (CheckCast)instruction;
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.print(lineNumber, '(');
               Constant displayPrefix = this.constants.get(var26.index);
               if(displayPrefix.tag == 1) {
                  ConstantUtf8 fieldName = (ConstantUtf8)displayPrefix;
                  cnat = fieldName.bytes;
               } else {
                  ConstantClass var22 = (ConstantClass)displayPrefix;
                  cnat = this.constants.getConstantUtf8(var22.name_index);
                  if(cnat.charAt(0) != 91) {
                     cnat = SignatureUtil.CreateTypeName(cnat);
                  }
               }

               SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, cnat);
               this.printer.print(')');
            }

            lineNumber = this.visit(var26, var26.objectref);
            break;
         case 193:
            InstanceOf var24 = (InstanceOf)instruction;
            lineNumber = this.visit(var24, var24.objectref);
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.print(lineNumber, ' ');
               this.printer.printKeyword("instanceof");
               this.printer.print(' ');
               String var18 = this.constants.getConstantClassName(var24.index);
               if(var18.charAt(0) != 91) {
                  var18 = SignatureUtil.CreateTypeName(var18);
               }

               SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, var18);
            }
            break;
         case 194:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.startOfError();
               this.printer.printKeyword(lineNumber, "monitorenter");
               this.printer.endOfError();
            }
            break;
         case 195:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.startOfError();
               this.printer.printKeyword(lineNumber, "monitorexit");
               this.printer.endOfError();
            }
            break;
         case 197:
            lineNumber = this.writeMultiANewArray((MultiANewArray)instruction);
            break;
         case 257:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.printNumeric(lineNumber, String.valueOf(((ConstInstruction)instruction).value) + 'L');
            }
            break;
         case 258:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               var17 = String.valueOf(((ConstInstruction)instruction).value);
               if(var17.indexOf(46) == -1) {
                  var17 = var17 + ".0";
               }

               this.printer.printNumeric(lineNumber, var17 + 'F');
            }
            break;
         case 259:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               var17 = String.valueOf(((ConstInstruction)instruction).value);
               if(var17.indexOf(46) == -1) {
                  var17 = var17 + ".0";
               }

               this.printer.printNumeric(lineNumber, var17 + 'D');
            }
            break;
         case 260:
            lineNumber = this.writeIfTest((IfInstruction)instruction);
            break;
         case 261:
            lineNumber = this.writeIfCmpTest((IfCmp)instruction);
            break;
         case 262:
            lineNumber = this.writeIfXNullTest((IfInstruction)instruction);
            break;
         case 263:
            var12 = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && var12 <= this.lastOffset) {
               this.printer.print(lineNumber, "tmp");
               this.printer.print(instruction.offset);
               this.printer.print('_');
               this.printer.print(((DupLoad)instruction).dupStore.objectref.offset);
            }
            break;
         case 264:
            DupStore var16 = (DupStore)instruction;
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.print(lineNumber, "tmp");
               this.printer.print(instruction.offset);
               this.printer.print('_');
               this.printer.print(((DupStore)instruction).objectref.offset);
               this.printer.print(" = ");
            }

            lineNumber = this.visit(instruction, var16.objectref);
            break;
         case 265:
            lineNumber = this.writeAssignmentInstruction((AssignmentInstruction)instruction);
            break;
         case 266:
            UnaryOperatorInstruction var15 = (UnaryOperatorInstruction)instruction;
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.print(lineNumber, var15.operator);
            }

            lineNumber = this.visit(var15, var15.value);
            break;
         case 267:
            lineNumber = this.writeBinaryOperatorInstruction((BinaryOperatorInstruction)instruction);
            break;
         case 270:
            lineNumber = this.writeExceptionLoad((ExceptionLoad)instruction);
            break;
         case 271:
            ArrayLoadInstruction var14 = (ArrayLoadInstruction)instruction;
            lineNumber = this.writeArray(var14, var14.arrayref, var14.indexref);
            break;
         case 273:
            ReturnInstruction var13 = (ReturnInstruction)instruction;
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.printKeyword(var13.lineNumber, "return");
               this.printer.print(' ');
            }

            lineNumber = this.visit(var13.valueref);
            break;
         case 274:
            lineNumber = this.writeInvokeNewInstruction((InvokeNew)instruction);
            break;
         case 275:
            lineNumber = this.writeConvertInstruction((ConvertInstruction)instruction);
            break;
         case 276:
            lineNumber = this.visit(((ConvertInstruction)instruction).value);
            break;
         case 277:
            lineNumber = this.writePreInc((IncInstruction)instruction);
            break;
         case 278:
            lineNumber = this.writePostInc((IncInstruction)instruction);
            break;
         case 279:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.startOfError();
               this.printer.printKeyword(lineNumber, "returnAddress");
               this.printer.endOfError();
            }
            break;
         case 280:
            var12 = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && var12 <= this.lastOffset) {
               this.printer.startOfError();
               this.printer.print(lineNumber, "tmpTernaryOp");
               this.printer.print(lineNumber, " = ");
               this.printer.endOfError();
            }

            lineNumber = this.visit(instruction, ((TernaryOpStore)instruction).objectref);
            break;
         case 281:
            TernaryOperator var11 = (TernaryOperator)instruction;
            lineNumber = this.visit(var11.test);
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.print(lineNumber, " ? ");
            }

            lineNumber = this.visit(var11, var11.value1);
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.print(lineNumber, " : ");
            }

            lineNumber = this.visit(var11, var11.value2);
            break;
         case 282:
            lineNumber = this.WriteInitArrayInstruction((InitArrayInstruction)instruction);
            break;
         case 283:
            lineNumber = this.WriteNewAndInitArrayInstruction((InitArrayInstruction)instruction);
            break;
         case 284:
            lineNumber = this.writeComplexConditionalBranchInstructionTest((ComplexConditionalBranchInstruction)instruction);
            break;
         case 285:
            lineNumber = this.writeOuterThis((GetStatic)instruction);
            break;
         case 286:
            AssertInstruction tp = (AssertInstruction)instruction;
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.printKeyword(lineNumber, "assert");
               this.printer.print(' ');
            }

            lineNumber = this.visit(tp, tp.test);
            if(tp.msg != null) {
               nextOffset = this.previousOffset + 1;
               if(this.firstOffset <= this.previousOffset && tp.msg.offset <= this.lastOffset) {
                  this.printer.print(lineNumber, " : ");
               }

               lineNumber = this.visit(tp, tp.msg);
            }
            break;
         case 311:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.printKeyword(lineNumber, "continue");
            }
            break;
         case 312:
            if(this.firstOffset <= this.previousOffset && instruction.offset <= this.lastOffset) {
               this.printer.printKeyword(lineNumber, "break");
            }
            break;
         case 317:
            lineNumber = this.writeDeclaration((FastDeclaration)instruction);
            break;
         case 321:
            lineNumber = this.writeEnumValueInstruction((InvokeNew)instruction);
            break;
         default:
            System.err.println("Can not write code for " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
         }

         this.previousOffset = instruction.offset;
         return lineNumber;
      } else {
         return lineNumber;
      }
   }

   protected int visit(Instruction parent, Instruction child) {
      return this.visit(parent.getPriority(), child);
   }

   protected int visit(int parentPriority, Instruction child) {
      if(parentPriority < child.getPriority()) {
         int nextOffset = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.print(child.lineNumber, '(');
         }

         int lineNumber = this.visit(child);
         nextOffset = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.print(lineNumber, ')');
         }

         return lineNumber;
      } else {
         return this.visit(child);
      }
   }

   private boolean needAPrefixForThisField(int fieldNameIndex, int fieldDescriptorIndex, GetStatic getStatic) {
      if(this.classFile.getField(fieldNameIndex, fieldDescriptorIndex) != null) {
         return true;
      } else {
         ConstantFieldref cfr = this.constants.getConstantFieldref(getStatic.index);
         String getStaticOuterClassName = this.constants.getConstantClassName(cfr.class_index);
         String fieldName = this.constants.getConstantUtf8(fieldNameIndex);
         String fieldDescriptor = this.constants.getConstantUtf8(fieldDescriptorIndex);

         for(ClassFile outerClassFile = this.classFile.getOuterClass(); outerClassFile != null; outerClassFile = outerClassFile.getOuterClass()) {
            String outerClassName = outerClassFile.getThisClassName();
            if(outerClassName.equals(getStaticOuterClassName)) {
               break;
            }

            if(outerClassFile.getField(fieldName, fieldDescriptor) != null) {
               return true;
            }
         }

         return false;
      }
   }

   private int writeBIPush_SIPush_IConst(IConst iconst) {
      int lineNumber = iconst.lineNumber;
      if(this.firstOffset <= this.previousOffset && iconst.offset <= this.lastOffset) {
         int value = iconst.value;
         String signature = iconst.getSignature();
         if("S".equals(signature)) {
            if((short)value == -32768) {
               this.writeBIPush_SIPush_IConst(lineNumber, "java/lang/Short", "MIN_VALUE", "S");
            } else if((short)value == 32767) {
               this.writeBIPush_SIPush_IConst(lineNumber, "java/lang/Short", "MAX_VALUE", "S");
            } else {
               this.printer.printNumeric(lineNumber, String.valueOf(value));
            }
         } else if("B".equals(signature)) {
            if(value == -128) {
               this.writeBIPush_SIPush_IConst(lineNumber, "java/lang/Byte", "MIN_VALUE", "B");
            } else if(value == 127) {
               this.writeBIPush_SIPush_IConst(lineNumber, "java/lang/Byte", "MAX_VALUE", "B");
            } else {
               this.printer.printNumeric(lineNumber, String.valueOf(value));
            }
         } else if("C".equals(signature)) {
            String escapedString = StringUtil.EscapeCharAndAppendApostrophe((char)value);
            String scopeInternalName = this.classFile.getThisClassName();
            this.printer.printString(lineNumber, escapedString, scopeInternalName);
         } else if("Z".equals(signature)) {
            this.printer.printKeyword(lineNumber, value == 0?"false":"true");
         } else {
            this.printer.printNumeric(lineNumber, String.valueOf(value));
         }
      }

      return lineNumber;
   }

   private void writeBIPush_SIPush_IConst(int lineNumber, String internalTypeName, String name, String descriptor) {
      String className = SignatureWriter.InternalClassNameToClassName(this.loader, this.referenceMap, this.classFile, internalTypeName);
      String scopeInternalName = this.classFile.getThisClassName();
      this.printer.printType(lineNumber, internalTypeName, className, scopeInternalName);
      this.printer.print(lineNumber, '.');
      this.printer.printStaticField(lineNumber, internalTypeName, name, descriptor, scopeInternalName);
   }

   private int writeArray(Instruction parent, Instruction arrayref, Instruction indexref) {
      int lineNumber = this.visit(parent, arrayref);
      int nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         this.printer.print(lineNumber, '[');
      }

      lineNumber = this.visit(parent, indexref);
      if(this.firstOffset <= this.previousOffset && parent.offset <= this.lastOffset) {
         this.printer.print(lineNumber, ']');
      }

      return lineNumber;
   }

   private int writeBinaryOperatorInstruction(BinaryOperatorInstruction boi) {
      int lineNumber = boi.value1.lineNumber;
      int nextOffset;
      if(boi.operator.length() == 1) {
         switch(boi.operator.charAt(0)) {
         case '&':
         case '^':
         case '|':
            lineNumber = this.writeBinaryOperatorParameterInHexaOrBoolean(boi, boi.value1);
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.print(lineNumber, ' ');
               this.printer.print(lineNumber, boi.operator);
               this.printer.print(lineNumber, ' ');
            }

            return this.writeBinaryOperatorParameterInHexaOrBoolean(boi, boi.value2);
         }
      }

      lineNumber = this.visit(boi, boi.value1);
      nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         this.printer.print(lineNumber, ' ');
         this.printer.print(lineNumber, boi.operator);
         this.printer.print(lineNumber, ' ');
      }

      if(boi.getPriority() <= boi.value2.getPriority()) {
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.print(lineNumber, '(');
         }

         lineNumber = this.visit(boi.value2);
         if(this.firstOffset <= this.previousOffset && boi.offset <= this.lastOffset) {
            this.printer.print(lineNumber, ')');
         }

         return lineNumber;
      } else {
         return this.visit(boi.value2);
      }
   }

   protected int writeBinaryOperatorParameterInHexaOrBoolean(Instruction parent, Instruction child) {
      if(parent.getPriority() < child.getPriority()) {
         int nextOffset = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.print(child.lineNumber, '(');
         }

         int lineNumber = this.writeBinaryOperatorParameterInHexaOrBoolean(child);
         nextOffset = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.print(lineNumber, ')');
         }

         return lineNumber;
      } else {
         return this.writeBinaryOperatorParameterInHexaOrBoolean(child);
      }
   }

   private int writeBinaryOperatorParameterInHexaOrBoolean(Instruction value) {
      int lineNumber = value.lineNumber;
      if(this.firstOffset <= this.previousOffset && value.offset <= this.lastOffset) {
         switch(value.opcode) {
         case 16:
         case 17:
         case 256:
            IConst cst1 = (IConst)value;
            if(cst1.signature.equals("Z")) {
               if(cst1.value == 0) {
                  this.printer.printKeyword(lineNumber, "false");
               } else {
                  this.printer.printKeyword(lineNumber, "true");
               }
            } else {
               this.printer.printNumeric(lineNumber, "0x" + Integer.toHexString(cst1.value).toUpperCase());
            }
            break;
         case 18:
         case 20:
            this.printer.addNewLinesAndPrefix(lineNumber);
            Constant cst = this.constants.get(((IndexInstruction)value).index);
            ConstantValueWriter.WriteHexa(this.loader, this.printer, this.referenceMap, this.classFile, (ConstantValue)cst);
            break;
         default:
            lineNumber = this.visit(value);
         }
      }

      return lineNumber;
   }

   protected int writeIfTest(IfInstruction ifInstruction) {
      String signature = ifInstruction.value.getReturnedSignature(this.constants, this.localVariables);
      int lineNumber;
      if(signature != null && signature.charAt(0) == 90) {
         switch(ifInstruction.cmp) {
         case 0:
         case 5:
         case 6:
            lineNumber = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && lineNumber <= this.lastOffset) {
               this.printer.print(ifInstruction.lineNumber, "!");
            }
         case 1:
         case 2:
         case 3:
         case 4:
         default:
            return this.visit(2, ifInstruction.value);
         }
      } else {
         lineNumber = this.visit(6, ifInstruction.value);
         if(this.firstOffset <= this.previousOffset && ifInstruction.offset <= this.lastOffset) {
            this.printer.print(' ');
            this.printer.print(ByteCodeConstants.CMP_NAMES[ifInstruction.cmp]);
            this.printer.print(' ');
            this.printer.printNumeric("0");
         }

         return lineNumber;
      }
   }

   protected int writeIfCmpTest(IfCmp ifCmpInstruction) {
      int lineNumber = this.visit(6, ifCmpInstruction.value1);
      int nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         this.printer.print(lineNumber, ' ');
         this.printer.print(ByteCodeConstants.CMP_NAMES[ifCmpInstruction.cmp]);
         this.printer.print(' ');
      }

      return this.visit(6, ifCmpInstruction.value2);
   }

   protected int writeIfXNullTest(IfInstruction ifXNull) {
      int lineNumber = this.visit(6, ifXNull.value);
      if(this.firstOffset <= this.previousOffset && ifXNull.offset <= this.lastOffset) {
         this.printer.print(lineNumber, ' ');
         this.printer.print(ByteCodeConstants.CMP_NAMES[ifXNull.cmp]);
         this.printer.print(' ');
         this.printer.printKeyword("null");
      }

      return lineNumber;
   }

   protected int writeComplexConditionalBranchInstructionTest(ComplexConditionalBranchInstruction ccbi) {
      List branchList = ccbi.instructions;
      int lenght = branchList.size();
      if(lenght > 1) {
         String operator = ccbi.cmp == 0?" && ":" || ";
         Instruction instruction = (Instruction)branchList.get(0);
         int lineNumber = instruction.lineNumber;
         int nextOffset = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.print(lineNumber, '(');
         }

         lineNumber = this.visit(instruction);
         nextOffset = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.print(lineNumber, ')');
         }

         for(int i = 1; i < lenght; ++i) {
            instruction = (Instruction)branchList.get(i);
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.print(lineNumber, operator);
               this.printer.print(instruction.lineNumber, '(');
            }

            lineNumber = this.visit(instruction);
            if(this.firstOffset <= this.previousOffset && ccbi.offset <= this.lastOffset) {
               this.printer.print(lineNumber, ')');
            }
         }

         return lineNumber;
      } else {
         return lenght > 0?this.visit((Instruction)branchList.get(0)):Instruction.UNKNOWN_LINE_NUMBER;
      }
   }

   private int writeIInc(IInc iinc) {
      int lineNumber = iinc.lineNumber;
      if(this.firstOffset <= this.previousOffset && iinc.offset <= this.lastOffset) {
         String lvName = null;
         LocalVariable lv = this.localVariables.getLocalVariableWithIndexAndOffset(iinc.index, iinc.offset);
         if(lv != null) {
            int lvNameIndex = lv.name_index;
            if(lvNameIndex > 0) {
               lvName = this.constants.getConstantUtf8(lvNameIndex);
            }
         }

         if(lvName == null) {
            this.printer.startOfError();
            this.printer.print(lineNumber, "???");
            this.printer.endOfError();
         } else {
            this.printer.print(lineNumber, lvName);
         }

         switch(iinc.count) {
         case -1:
            this.printer.print(lineNumber, "--");
            break;
         case 0:
         default:
            if(iinc.count >= 0) {
               this.printer.print(lineNumber, " += ");
               this.printer.printNumeric(lineNumber, String.valueOf(iinc.count));
            } else {
               this.printer.print(lineNumber, " -= ");
               this.printer.printNumeric(lineNumber, String.valueOf(-iinc.count));
            }
            break;
         case 1:
            this.printer.print(lineNumber, "++");
         }
      }

      return lineNumber;
   }

   private int writePreInc(IncInstruction ii) {
      int lineNumber = ii.lineNumber;
      if(this.firstOffset <= this.previousOffset && ii.offset <= this.lastOffset) {
         switch(ii.count) {
         case -1:
            this.printer.print(lineNumber, "--");
            lineNumber = this.visit(ii.value);
            break;
         case 0:
         default:
            lineNumber = this.visit(ii.value);
            if(ii.count >= 0) {
               this.printer.print(lineNumber, " += ");
               this.printer.printNumeric(lineNumber, String.valueOf(ii.count));
            } else {
               this.printer.print(lineNumber, " -= ");
               this.printer.printNumeric(lineNumber, String.valueOf(-ii.count));
            }
            break;
         case 1:
            this.printer.print(lineNumber, "++");
            lineNumber = this.visit(ii.value);
         }
      }

      return lineNumber;
   }

   private int writePostInc(IncInstruction ii) {
      int lineNumber = ii.lineNumber;
      if(this.firstOffset <= this.previousOffset && ii.offset <= this.lastOffset) {
         switch(ii.count) {
         case -1:
            lineNumber = this.visit(ii.value);
            this.printer.print(lineNumber, "--");
            break;
         case 0:
         default:
            (new RuntimeException("PostInc with value=" + ii.count)).printStackTrace();
            break;
         case 1:
            lineNumber = this.visit(ii.value);
            this.printer.print(lineNumber, "++");
         }
      }

      return lineNumber;
   }

   private int writeInvokeNewInstruction(InvokeNew in) {
      ConstantMethodref cmr = this.constants.getConstantMethodref(in.index);
      String internalClassName = this.constants.getConstantClassName(cmr.class_index);
      String prefix = this.classFile.getThisClassName() + '$';
      ClassFile innerClassFile;
      if(internalClassName.startsWith(prefix)) {
         innerClassFile = this.classFile.getInnerClassFile(internalClassName);
      } else {
         innerClassFile = null;
      }

      int lineNumber = in.lineNumber;
      int length = in.args.size();
      ConstantNameAndType cnat = this.constants.getConstantNameAndType(cmr.name_and_type_index);
      String constructorDescriptor = this.constants.getConstantUtf8(cnat.descriptor_index);
      int firstIndex;
      if(innerClassFile == null) {
         firstIndex = 0;
      } else if(innerClassFile.getInternalAnonymousClassName() == null) {
         firstIndex = this.computeFirstIndex(innerClassFile.access_flags, in);
      } else {
         firstIndex = this.computeFirstIndex(this.methodAccessFlags, in);
         String constructorName = this.constants.getConstantUtf8(cnat.name_index);
         Method constructor = innerClassFile.getMethod(constructorName, constructorDescriptor);
         if(constructor != null) {
            length = firstIndex + constructor.getSuperConstructorParameterCount();

            assert length <= in.args.size();
         }
      }

      if(this.firstOffset <= this.previousOffset) {
         this.printer.printKeyword(lineNumber, "new");
         this.printer.print(' ');
         if(innerClassFile == null) {
            SignatureWriter.WriteConstructor(this.loader, this.printer, this.referenceMap, this.classFile, SignatureUtil.CreateTypeName(internalClassName), constructorDescriptor);
         } else if(innerClassFile.getInternalAnonymousClassName() == null) {
            SignatureWriter.WriteConstructor(this.loader, this.printer, this.referenceMap, this.classFile, SignatureUtil.CreateTypeName(internalClassName), constructorDescriptor);
         } else {
            SignatureWriter.WriteConstructor(this.loader, this.printer, this.referenceMap, this.classFile, SignatureUtil.CreateTypeName(innerClassFile.getInternalAnonymousClassName()), constructorDescriptor);
         }
      }

      return this.writeArgs(in.lineNumber, firstIndex, length, in.args);
   }

   private int computeFirstIndex(int accessFlags, InvokeNew in) {
      if((accessFlags & 8) == 0 && in.args.size() > 0) {
         Instruction arg0 = (Instruction)in.args.get(0);
         return arg0.opcode == 25 && ((ALoad)arg0).index == 0?1:0;
      } else {
         return 0;
      }
   }

   private int writeEnumValueInstruction(InvokeNew in) {
      int lineNumber = in.lineNumber;
      ConstantFieldref cfr = this.constants.getConstantFieldref(in.enumValueFieldRefIndex);
      ConstantNameAndType cnat = this.constants.getConstantNameAndType(cfr.name_and_type_index);
      String internalClassName = this.classFile.getThisClassName();
      String name = this.constants.getConstantUtf8(cnat.name_index);
      String descriptor = this.constants.getConstantUtf8(cnat.descriptor_index);
      this.printer.addNewLinesAndPrefix(lineNumber);
      this.printer.printStaticFieldDeclaration(internalClassName, name, descriptor);
      if(in.args.size() > 2) {
         lineNumber = this.writeArgs(lineNumber, 2, in.args.size(), in.args);
      }

      return lineNumber;
   }

   private int writeGetField(GetField getField) {
      int lineNumber = getField.lineNumber;
      ConstantFieldref cfr = this.constants.getConstantFieldref(getField.index);
      ConstantNameAndType cnat = this.constants.getConstantNameAndType(cfr.name_and_type_index);
      Field field = this.classFile.getField(cnat.name_index, cnat.descriptor_index);
      String internalClassName;
      String fieldName;
      if(field != null && field.outerMethodLocalVariableNameIndex != -1) {
         if(this.firstOffset <= this.previousOffset && getField.offset <= this.lastOffset) {
            String displayPrefix1 = this.constants.getConstantClassName(cfr.class_index);
            internalClassName = this.constants.getConstantUtf8(field.outerMethodLocalVariableNameIndex);
            if(this.keywordSet.contains(internalClassName)) {
               internalClassName = "jdField_" + internalClassName;
            }

            fieldName = this.constants.getConstantUtf8(cnat.descriptor_index);
            this.printer.printField(lineNumber, displayPrefix1, internalClassName, fieldName, this.classFile.getThisClassName());
         }
      } else {
         boolean displayPrefix = false;
         if(this.localVariables.containsLocalVariableWithNameIndex(cnat.name_index)) {
            switch(getField.objectref.opcode) {
            case 25:
               if(((ALoad)getField.objectref).index == 0) {
                  displayPrefix = true;
               }
               break;
            case 285:
               if(!this.needAPrefixForThisField(cnat.name_index, cnat.descriptor_index, (GetStatic)getField.objectref)) {
                  displayPrefix = true;
               }
            }
         }

         if(this.firstOffset <= this.previousOffset && getField.objectref.offset <= this.lastOffset) {
            if(!displayPrefix) {
               this.printer.addNewLinesAndPrefix(lineNumber);
               this.printer.startOfOptionalPrefix();
            }

            lineNumber = this.visit(getField, getField.objectref);
            this.printer.print(lineNumber, '.');
            if(!displayPrefix) {
               this.printer.endOfOptionalPrefix();
            }
         }

         if(this.firstOffset <= this.previousOffset && getField.offset <= this.lastOffset) {
            internalClassName = this.constants.getConstantClassName(cfr.class_index);
            fieldName = this.constants.getConstantUtf8(cnat.name_index);
            if(this.keywordSet.contains(fieldName)) {
               fieldName = "jdField_" + fieldName;
            }

            String descriptor = this.constants.getConstantUtf8(cnat.descriptor_index);
            this.printer.printField(lineNumber, internalClassName, fieldName, descriptor, this.classFile.getThisClassName());
         }
      }

      return lineNumber;
   }

   private int writeInvokeNoStaticInstruction(InvokeNoStaticInstruction insi) {
      ConstantMethodref cmr = this.constants.getConstantMethodref(insi.index);
      ConstantNameAndType cnat = this.constants.getConstantNameAndType(cmr.name_and_type_index);
      boolean thisInvoke = false;
      String nextOffset;
      if(insi.objectref.opcode == 25 && ((ALoad)insi.objectref).index == 0) {
         ALoad displayPrefix = (ALoad)insi.objectref;
         LocalVariable lineNumber = this.localVariables.getLocalVariableWithIndexAndOffset(displayPrefix.index, displayPrefix.offset);
         if(lineNumber != null) {
            nextOffset = this.constants.getConstantUtf8(lineNumber.name_index);
            if("this".equals(nextOffset)) {
               thisInvoke = true;
            }
         }
      }

      String internalClassName;
      if(thisInvoke) {
         int displayPrefix1 = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && displayPrefix1 <= this.lastOffset) {
            String lineNumber1 = this.constants.getConstantClassName(cmr.class_index);
            nextOffset = this.constants.getConstantUtf8(cnat.name_index);
            if(this.keywordSet.contains(nextOffset)) {
               nextOffset = "jdMethod_" + nextOffset;
            }

            internalClassName = this.constants.getConstantUtf8(cnat.descriptor_index);
            this.printer.printMethod(insi.lineNumber, lineNumber1, nextOffset, internalClassName, this.classFile.getThisClassName());
         }
      } else {
         boolean displayPrefix2 = insi.objectref.opcode != 285 || this.needAPrefixForThisMethod(cnat.name_index, cnat.descriptor_index, (GetStatic)insi.objectref);
         int lineNumber2 = insi.objectref.lineNumber;
         if(!displayPrefix2) {
            this.printer.addNewLinesAndPrefix(lineNumber2);
            this.printer.startOfOptionalPrefix();
         }

         this.visit(insi, insi.objectref);
         int nextOffset1 = this.previousOffset + 1;
         lineNumber2 = insi.lineNumber;
         if(this.firstOffset <= this.previousOffset && nextOffset1 <= this.lastOffset) {
            this.printer.print(lineNumber2, '.');
         }

         if(!displayPrefix2) {
            this.printer.endOfOptionalPrefix();
         }

         nextOffset1 = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset1 <= this.lastOffset) {
            internalClassName = this.constants.getConstantClassName(cmr.class_index);
            String methodName = this.constants.getConstantUtf8(cnat.name_index);
            if(this.keywordSet.contains(methodName)) {
               methodName = "jdMethod_" + methodName;
            }

            String descriptor = this.constants.getConstantUtf8(cnat.descriptor_index);
            this.printer.printMethod(lineNumber2, internalClassName, methodName, descriptor, this.classFile.getThisClassName());
         }
      }

      return this.writeArgs(insi.lineNumber, 0, insi.args.size(), insi.args);
   }

   private boolean needAPrefixForThisMethod(int methodNameIndex, int methodDescriptorIndex, GetStatic getStatic) {
      if(this.classFile.getMethod(methodNameIndex, methodDescriptorIndex) != null) {
         return true;
      } else {
         ConstantFieldref cfr = this.constants.getConstantFieldref(getStatic.index);
         String getStaticOuterClassName = this.constants.getConstantClassName(cfr.class_index);
         String methodName = this.constants.getConstantUtf8(methodNameIndex);
         String methodDescriptor = this.constants.getConstantUtf8(methodDescriptorIndex);

         for(ClassFile outerClassFile = this.classFile.getOuterClass(); outerClassFile != null; outerClassFile = outerClassFile.getOuterClass()) {
            String outerClassName = outerClassFile.getThisClassName();
            if(outerClassName.equals(getStaticOuterClassName)) {
               break;
            }

            if(outerClassFile.getMethod(methodName, methodDescriptor) != null) {
               return true;
            }
         }

         return false;
      }
   }

   private int writeInvokespecial(InvokeNoStaticInstruction insi) {
      ConstantMethodref cmr = this.constants.getConstantMethodref(insi.index);
      ConstantNameAndType cnat = this.constants.getConstantNameAndType(cmr.name_and_type_index);
      boolean thisInvoke = false;
      if(insi.objectref.opcode == 25 && ((ALoad)insi.objectref).index == 0) {
         ALoad lineNumber = (ALoad)insi.objectref;
         LocalVariable nextOffset = this.localVariables.getLocalVariableWithIndexAndOffset(lineNumber.index, lineNumber.offset);
         if(nextOffset != null && nextOffset.name_index == this.constants.thisLocalVariableNameIndex) {
            thisInvoke = true;
         }
      }

      byte firstIndex;
      if(thisInvoke) {
         if(cnat.name_index == this.constants.instanceConstructorIndex) {
            if(cmr.class_index == this.classFile.getThisClassIndex()) {
               if((this.classFile.access_flags & 16384) == 0) {
                  if(this.classFile.isAInnerClass() && (this.classFile.access_flags & 8) == 0) {
                     firstIndex = 1;
                  } else {
                     firstIndex = 0;
                  }
               } else {
                  firstIndex = 2;
               }
            } else if(this.classFile.isAInnerClass()) {
               firstIndex = 1;
            } else {
               firstIndex = 0;
            }
         } else {
            firstIndex = 0;
         }
      } else {
         firstIndex = 0;
      }

      String methodName;
      String descriptor;
      int lineNumber1;
      int nextOffset1;
      if(thisInvoke) {
         lineNumber1 = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && lineNumber1 <= this.lastOffset) {
            nextOffset1 = insi.lineNumber;
            if(cnat.name_index == this.constants.instanceConstructorIndex) {
               if(cmr.class_index == this.classFile.getThisClassIndex()) {
                  this.printer.printKeyword(nextOffset1, "this");
               } else {
                  this.printer.printKeyword(nextOffset1, "super");
               }
            } else {
               Method internalClassName = this.classFile.getMethod(cnat.name_index, cnat.descriptor_index);
               if(internalClassName == null || (internalClassName.access_flags & 2) == 0) {
                  this.printer.printKeyword(nextOffset1, "super");
                  this.printer.print(nextOffset1, '.');
               }

               methodName = this.constants.getConstantClassName(cmr.class_index);
               descriptor = this.constants.getConstantUtf8(cnat.name_index);
               if(this.keywordSet.contains(descriptor)) {
                  descriptor = "jdMethod_" + descriptor;
               }

               String descriptor1 = this.constants.getConstantUtf8(cnat.descriptor_index);
               this.printer.printMethod(nextOffset1, methodName, descriptor, descriptor1, this.classFile.getThisClassName());
            }
         }
      } else {
         lineNumber1 = insi.lineNumber;
         this.visit(insi, insi.objectref);
         nextOffset1 = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset1 <= this.lastOffset) {
            this.printer.print(lineNumber1, '.');
            String internalClassName1 = this.constants.getConstantClassName(cmr.class_index);
            methodName = this.constants.getConstantUtf8(cnat.name_index);
            if(this.keywordSet.contains(methodName)) {
               methodName = "jdMethod_" + methodName;
            }

            descriptor = this.constants.getConstantUtf8(cnat.descriptor_index);
            this.printer.printMethod(internalClassName1, methodName, descriptor, this.classFile.getThisClassName());
         }
      }

      return this.writeArgs(insi.lineNumber, firstIndex, insi.args.size(), insi.args);
   }

   private int writeInvokestatic(Invokestatic invokestatic) {
      int nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         int lineNumber = invokestatic.lineNumber;
         ConstantMethodref cmr = this.constants.getConstantMethodref(invokestatic.index);
         String internalClassName = this.constants.getConstantClassName(cmr.class_index);
         if(this.classFile.getThisClassIndex() != cmr.class_index) {
            this.printer.addNewLinesAndPrefix(lineNumber);
            int cnat = SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, SignatureUtil.CreateTypeName(this.constants.getConstantClassName(cmr.class_index)));
            if(cnat > 0) {
               this.printer.print('.');
            }
         }

         ConstantNameAndType cnat1 = this.constants.getConstantNameAndType(cmr.name_and_type_index);
         String methodName = this.constants.getConstantUtf8(cnat1.name_index);
         if(this.keywordSet.contains(methodName)) {
            methodName = "jdMethod_" + methodName;
         }

         String descriptor = this.constants.getConstantUtf8(cnat1.descriptor_index);
         this.printer.printStaticMethod(lineNumber, internalClassName, methodName, descriptor, this.classFile.getThisClassName());
      }

      return this.writeArgs(invokestatic.lineNumber, 0, invokestatic.args.size(), invokestatic.args);
   }

   private int writeArgs(int lineNumber, int firstIndex, int length, List<Instruction> args) {
      int nextOffset;
      if(length > firstIndex) {
         nextOffset = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.print(lineNumber, '(');
         }

         lineNumber = this.visit((Instruction)args.get(firstIndex));

         for(int i = firstIndex + 1; i < length; ++i) {
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.print(lineNumber, ", ");
            }

            lineNumber = this.visit((Instruction)args.get(i));
         }

         nextOffset = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.print(lineNumber, ')');
         }
      } else {
         nextOffset = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.print(lineNumber, "()");
         }
      }

      return lineNumber;
   }

   private int writeGetStatic(GetStatic getStatic) {
      int lineNumber = getStatic.lineNumber;
      if(this.firstOffset <= this.previousOffset && getStatic.offset <= this.lastOffset) {
         ConstantFieldref cfr = this.constants.getConstantFieldref(getStatic.index);
         String internalClassName = this.constants.getConstantClassName(cfr.class_index);
         if(cfr.class_index != this.classFile.getThisClassIndex()) {
            this.printer.addNewLinesAndPrefix(lineNumber);
            String cnat = SignatureUtil.CreateTypeName(internalClassName);
            int descriptor = SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, cnat);
            if(descriptor > 0) {
               this.printer.print(lineNumber, '.');
            }
         }

         ConstantNameAndType cnat1 = this.constants.getConstantNameAndType(cfr.name_and_type_index);
         String descriptor1 = this.constants.getConstantUtf8(cnat1.descriptor_index);
         String constName = this.constants.getConstantUtf8(cnat1.name_index);
         this.printer.printStaticField(lineNumber, internalClassName, constName, descriptor1, this.classFile.getThisClassName());
      }

      return lineNumber;
   }

   private int writeOuterThis(GetStatic getStatic) {
      int lineNumber = getStatic.lineNumber;
      if(this.firstOffset <= this.previousOffset && getStatic.offset <= this.lastOffset) {
         ConstantFieldref cfr = this.constants.getConstantFieldref(getStatic.index);
         if(cfr.class_index != this.classFile.getThisClassIndex()) {
            this.printer.addNewLinesAndPrefix(lineNumber);
            int cnat = SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, SignatureUtil.CreateTypeName(this.constants.getConstantClassName(cfr.class_index)));
            if(cnat > 0) {
               this.printer.print(lineNumber, '.');
            }
         }

         ConstantNameAndType cnat1 = this.constants.getConstantNameAndType(cfr.name_and_type_index);
         this.printer.printKeyword(lineNumber, this.constants.getConstantUtf8(cnat1.name_index));
      }

      return lineNumber;
   }

   private int writeLcdInstruction(IndexInstruction ii) {
      int lineNumber = ii.lineNumber;
      if(this.firstOffset <= this.previousOffset && ii.offset <= this.lastOffset) {
         Constant cst = this.constants.get(ii.index);
         if(cst.tag == 7) {
            ConstantClass cc = (ConstantClass)cst;
            String signature = SignatureUtil.CreateTypeName(this.constants.getConstantUtf8(cc.name_index));
            this.printer.addNewLinesAndPrefix(lineNumber);
            SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, signature);
            this.printer.print('.');
            this.printer.printKeyword("class");
         } else {
            this.printer.addNewLinesAndPrefix(lineNumber);
            ConstantValueWriter.Write(this.loader, this.printer, this.referenceMap, this.classFile, (ConstantValue)cst);
         }
      }

      return lineNumber;
   }

   private int writeLoadInstruction(LoadInstruction loadInstruction) {
      int lineNumber = loadInstruction.lineNumber;
      if(this.firstOffset <= this.previousOffset && loadInstruction.offset <= this.lastOffset) {
         LocalVariable lv = this.localVariables.getLocalVariableWithIndexAndOffset(loadInstruction.index, loadInstruction.offset);
         if(lv != null && lv.name_index > 0) {
            int nameIndex = lv.name_index;
            if(nameIndex == -1) {
               this.printer.startOfError();
               this.printer.print(lineNumber, "???");
               this.printer.endOfError();
            } else if(nameIndex == this.constants.thisLocalVariableNameIndex) {
               this.printer.printKeyword(lineNumber, this.constants.getConstantUtf8(lv.name_index));
            } else {
               this.printer.print(lineNumber, this.constants.getConstantUtf8(lv.name_index));
            }
         } else {
            this.printer.startOfError();
            this.printer.print(lineNumber, "???");
            this.printer.endOfError();
         }
      }

      return lineNumber;
   }

   private int writeMultiANewArray(MultiANewArray multiANewArray) {
      int lineNumber = multiANewArray.lineNumber;
      String signature = this.constants.getConstantClassName(multiANewArray.index);
      int nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         this.printer.printKeyword(lineNumber, "new");
         this.printer.print(' ');
         SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, SignatureUtil.CutArrayDimensionPrefix(signature));
      }

      Instruction[] dimensions = multiANewArray.dimensions;

      int dimensionCount;
      for(dimensionCount = dimensions.length - 1; dimensionCount >= 0; --dimensionCount) {
         nextOffset = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.print(lineNumber, '[');
         }

         lineNumber = this.visit(dimensions[dimensionCount]);
         nextOffset = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.print(lineNumber, ']');
         }
      }

      nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         dimensionCount = SignatureUtil.GetArrayDimensionCount(signature);

         for(int i = dimensions.length; i < dimensionCount; ++i) {
            this.printer.print(lineNumber, "[]");
         }
      }

      return lineNumber;
   }

   private int writePutStatic(PutStatic putStatic) {
      int nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         int lineNumber = putStatic.lineNumber;
         ConstantFieldref cfr = this.constants.getConstantFieldref(putStatic.index);
         if(cfr.class_index != this.classFile.getThisClassIndex()) {
            this.printer.addNewLinesAndPrefix(lineNumber);
            String cnat = SignatureUtil.CreateTypeName(this.constants.getConstantClassName(cfr.class_index));
            int descriptor = SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, cnat);
            if(descriptor > 0) {
               this.printer.print(lineNumber, '.');
            }
         }

         ConstantNameAndType cnat1 = this.constants.getConstantNameAndType(cfr.name_and_type_index);
         String descriptor1 = this.constants.getConstantUtf8(cnat1.descriptor_index);
         String internalClassName = SignatureUtil.GetInternalName(descriptor1);
         String constName = this.constants.getConstantUtf8(cnat1.name_index);
         this.printer.printStaticField(lineNumber, internalClassName, constName, descriptor1, this.classFile.getThisClassName());
         this.printer.print(lineNumber, " = ");
      }

      return this.visit(putStatic.valueref);
   }

   private int writeStoreInstruction(StoreInstruction storeInstruction) {
      int nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         int lineNumber = storeInstruction.lineNumber;
         LocalVariable lv = this.localVariables.getLocalVariableWithIndexAndOffset(storeInstruction.index, storeInstruction.offset);
         if(lv != null && lv.name_index > 0) {
            this.printer.print(lineNumber, this.constants.getConstantUtf8(lv.name_index));
         } else {
            this.printer.startOfError();
            this.printer.print(lineNumber, "???");
            this.printer.endOfError();
         }

         this.printer.print(lineNumber, " = ");
      }

      return this.visit(storeInstruction.valueref);
   }

   private int writeExceptionLoad(ExceptionLoad exceptionLoad) {
      int lineNumber = exceptionLoad.lineNumber;
      int nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         if(exceptionLoad.exceptionNameIndex == 0) {
            this.printer.printKeyword(lineNumber, "finally");
         } else {
            LocalVariable lv = this.localVariables.getLocalVariableWithIndexAndOffset(exceptionLoad.index, exceptionLoad.offset);
            if(lv != null && lv.name_index != 0) {
               this.printer.print(lineNumber, this.constants.getConstantUtf8(lv.name_index));
            } else {
               this.printer.startOfError();
               this.printer.print(lineNumber, "???");
               this.printer.endOfError();
            }
         }
      }

      return lineNumber;
   }

   private int writeAssignmentInstruction(AssignmentInstruction ai) {
      int lineNumber = ai.lineNumber;
      int previousOffsetBackup = this.previousOffset;
      this.visit(ai.value1);
      this.previousOffset = previousOffsetBackup;
      int nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         this.printer.print(lineNumber, ' ');
         this.printer.print(lineNumber, ai.operator);
         this.printer.print(lineNumber, ' ');
      }

      if(ai.operator.length() > 0) {
         switch(ai.operator.charAt(0)) {
         case '&':
         case '^':
         case '|':
            return this.writeBinaryOperatorParameterInHexaOrBoolean(ai, ai.value2);
         }
      }

      return this.visit(ai, ai.value2);
   }

   private int writeConvertInstruction(ConvertInstruction instruction) {
      int nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         int lineNumber = instruction.lineNumber;
         switch(instruction.signature.charAt(0)) {
         case 'B':
            this.printer.print(lineNumber, '(');
            this.printer.printKeyword("byte");
            this.printer.print(')');
            break;
         case 'C':
            this.printer.print(lineNumber, '(');
            this.printer.printKeyword("char");
            this.printer.print(')');
            break;
         case 'D':
            this.printer.print(lineNumber, '(');
            this.printer.printKeyword("double");
            this.printer.print(')');
            break;
         case 'F':
            this.printer.print(lineNumber, '(');
            this.printer.printKeyword("float");
            this.printer.print(')');
            break;
         case 'I':
            this.printer.print(lineNumber, '(');
            this.printer.printKeyword("int");
            this.printer.print(')');
            break;
         case 'L':
            this.printer.print(lineNumber, '(');
            this.printer.printKeyword("long");
            this.printer.print(')');
            break;
         case 'S':
            this.printer.print(lineNumber, '(');
            this.printer.printKeyword("short");
            this.printer.print(')');
         }
      }

      return this.visit(instruction, instruction.value);
   }

   private int writeDeclaration(FastDeclaration fd) {
      int lineNumber = fd.lineNumber;
      LocalVariable lv = this.localVariables.getLocalVariableWithIndexAndOffset(fd.index, fd.offset);
      int nextOffset;
      if(lv == null) {
         if(fd.instruction == null) {
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.startOfError();
               this.printer.print(lineNumber, "???");
               this.printer.endOfError();
            }
         } else {
            lineNumber = this.visit(fd.instruction);
         }
      } else {
         nextOffset = this.previousOffset + 1;
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
            this.printer.addNewLinesAndPrefix(lineNumber);
            String signature = this.constants.getConstantUtf8(lv.signature_index);
            String internalName = SignatureUtil.GetInternalName(signature);
            ClassFile innerClassFile = this.classFile.getInnerClassFile(internalName);
            if(lv.finalFlag) {
               this.printer.printKeyword("final");
               this.printer.print(' ');
            }

            if(innerClassFile != null && innerClassFile.getInternalAnonymousClassName() != null) {
               String internalAnonymousClassSignature = SignatureUtil.CreateTypeName(innerClassFile.getInternalAnonymousClassName());
               SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, internalAnonymousClassSignature);
            } else {
               SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, signature);
            }

            this.printer.print(' ');
         }

         if(fd.instruction == null) {
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.print(lineNumber, this.constants.getConstantUtf8(lv.name_index));
            }
         } else {
            lineNumber = this.visit(fd.instruction);
         }
      }

      return lineNumber;
   }

   private int WriteInitArrayInstruction(InitArrayInstruction iai) {
      int lineNumber = iai.lineNumber;
      int nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         this.printer.print(lineNumber, "{");
      }

      List values = iai.values;
      int length = values.size();
      if(length > 0) {
         Instruction instruction = (Instruction)values.get(0);
         if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset && lineNumber == instruction.lineNumber) {
            this.printer.print(" ");
         }

         lineNumber = this.visit(instruction);

         for(int i = 1; i < length; ++i) {
            nextOffset = this.previousOffset + 1;
            if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
               this.printer.print(lineNumber, ", ");
            }

            lineNumber = this.visit((Instruction)values.get(i));
         }
      }

      nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         this.printer.print(lineNumber, " }");
      }

      return lineNumber;
   }

   private int WriteNewAndInitArrayInstruction(InitArrayInstruction iai) {
      int nextOffset = this.previousOffset + 1;
      if(this.firstOffset <= this.previousOffset && nextOffset <= this.lastOffset) {
         int lineNumber = iai.lineNumber;
         this.printer.printKeyword(lineNumber, "new");
         this.printer.print(' ');
         switch(iai.newArray.opcode) {
         case 188:
            NewArray na = (NewArray)iai.newArray;
            SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, SignatureUtil.GetSignatureFromType(na.type));
            break;
         case 189:
            ANewArray ana = (ANewArray)iai.newArray;
            String signature = this.constants.getConstantClassName(ana.index);
            if(signature.charAt(0) != 91) {
               signature = SignatureUtil.CreateTypeName(signature);
            }

            SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, this.classFile, signature);
         }

         this.printer.print(lineNumber, "[] ");
      }

      return this.WriteInitArrayInstruction(iai);
   }
}
