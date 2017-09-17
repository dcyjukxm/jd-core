package jd.core.process.analyzer.instruction.bytecode;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.attribute.CodeException;
import jd.core.model.classfile.attribute.LineNumber;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ExceptionLoad;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.ReturnAddressLoad;
import jd.core.process.analyzer.instruction.bytecode.InstructionListException;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactoryConstants;
import jd.core.process.analyzer.instruction.bytecode.util.ByteCodeUtil;
import jd.core.util.IntSet;
import jd.core.util.SignatureUtil;

public class InstructionListBuilder {
   private static InstructionListBuilder.CodeExceptionComparator COMPARATOR = new InstructionListBuilder.CodeExceptionComparator((InstructionListBuilder.CodeExceptionComparator)null);

   public static void Build(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze) throws Exception {
      byte[] code = method.getCode();
      if(code != null) {
         byte offset = 0;

         try {
            int e = code.length;
            boolean[] jumps = new boolean[e];
            IntSet offsetSet = new IntSet();
            PopulateJumpsArrayAndSubProcOffsets(code, e, jumps, offsetSet);
            int[] subProcOffsets = offsetSet.toArray();
            int subProcOffsetsIndex = 0;
            int subProcOffset = subProcOffsets == null?-1:subProcOffsets[0];
            Stack stack = new Stack();
            CodeException[] codeExceptions = method.getCodeExceptions();
            int codeExceptionsIndex = 0;
            ConstantPool constants = classFile.getConstantPool();
            int exceptionOffset;
            if(codeExceptions == null) {
               exceptionOffset = -1;
            } else {
               Arrays.sort(codeExceptions, COMPARATOR);
               exceptionOffset = codeExceptions[0].handler_pc;
            }

            LineNumber[] lineNumbers = method.getLineNumbers();
            int lineNumbersIndex = 0;
            int lineNumber;
            int nextLineOffset;
            if(lineNumbers == null) {
               lineNumber = Instruction.UNKNOWN_LINE_NUMBER;
               nextLineOffset = -1;
            } else {
               LineNumber className = lineNumbers[lineNumbersIndex];
               lineNumber = className.line_number;
               nextLineOffset = -1;
               int methodName = className.start_pc;

               while(true) {
                  ++lineNumbersIndex;
                  if(lineNumbersIndex >= lineNumbers.length) {
                     break;
                  }

                  className = lineNumbers[lineNumbersIndex];
                  if(className.start_pc != methodName) {
                     nextLineOffset = className.start_pc;
                     break;
                  }

                  lineNumber = className.line_number;
               }
            }

            for(int var28 = 0; var28 < e; ++var28) {
               int var29 = code[var28] & 255;
               InstructionFactory var31 = InstructionFactoryConstants.FACTORIES[var29];
               if(var31 == null) {
                  String var34 = "No factory for " + ByteCodeConstants.OPCODE_NAMES[var29];
                  System.err.println(var34);
                  throw new Exception(var34);
               }

               int startPc;
               if(var28 == exceptionOffset) {
                  int msg = codeExceptions[codeExceptionsIndex].catch_type;
                  if(msg == 0) {
                     startPc = 0;
                  } else {
                     String el = SignatureUtil.CreateTypeName(constants.getConstantClassName(msg));
                     startPc = constants.addConstantUtf8(el);
                  }

                  ExceptionLoad var35 = new ExceptionLoad(270, var28, lineNumber, startPc);
                  stack.push(var35);
                  listForAnalyze.add(var35);

                  int nextOffsetException;
                  do {
                     ++codeExceptionsIndex;
                     if(codeExceptionsIndex >= codeExceptions.length) {
                        nextOffsetException = -1;
                        break;
                     }

                     nextOffsetException = codeExceptions[codeExceptionsIndex].handler_pc;
                  } while(nextOffsetException == exceptionOffset);

                  exceptionOffset = nextOffsetException;
               }

               if(var28 == subProcOffset) {
                  stack.push(new ReturnAddressLoad(279, var28, lineNumber));
                  ++subProcOffsetsIndex;
                  if(subProcOffsetsIndex >= subProcOffsets.length) {
                     subProcOffset = -1;
                  } else {
                     subProcOffset = subProcOffsets[subProcOffsetsIndex];
                  }
               }

               if(var28 == nextLineOffset) {
                  LineNumber var33 = lineNumbers[lineNumbersIndex];
                  lineNumber = var33.line_number;
                  nextLineOffset = -1;
                  startPc = var33.start_pc;

                  while(true) {
                     ++lineNumbersIndex;
                     if(lineNumbersIndex >= lineNumbers.length) {
                        break;
                     }

                     var33 = lineNumbers[lineNumbersIndex];
                     if(var33.start_pc != startPc) {
                        nextLineOffset = var33.start_pc;
                        break;
                     }

                     lineNumber = var33.line_number;
                  }
               }

               var28 += var31.create(classFile, method, list, listForAnalyze, stack, code, var28, lineNumber, jumps);
            }

            if(!stack.isEmpty()) {
               String var30 = classFile.getClassName();
               String var32 = classFile.getConstantPool().getConstantUtf8(method.name_index);
               System.err.println("\'" + var30 + '.' + var32 + "\' build error: stack not empty. stack=" + stack);
            }
         } catch (Exception var27) {
            throw new InstructionListException(classFile, method, offset, var27);
         }
      }

   }

   private static void PopulateJumpsArrayAndSubProcOffsets(byte[] code, int length, boolean[] jumps, IntSet offsetSet) {
      for(int offset = 0; offset < length; ++offset) {
         int opcode = code[offset] & 255;
         int var10000;
         int jumpOffset;
         switch(ByteCodeConstants.NO_OF_OPERANDS[opcode]) {
         case 0:
            break;
         case 1:
         case 3:
         default:
            switch(opcode) {
            case 170:
               offset = ByteCodeUtil.NextTableSwitchOffset(code, offset);
               continue;
            case 171:
               offset = ByteCodeUtil.NextLookupSwitchOffset(code, offset);
               continue;
            case 196:
               offset = ByteCodeUtil.NextWideOffset(code, offset);
               continue;
            default:
               offset += ByteCodeConstants.NO_OF_OPERANDS[opcode];
               continue;
            }
         case 2:
            int var10001;
            switch(opcode) {
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
            case 198:
            case 199:
               var10000 = offset++;
               var10001 = (code[offset] & 255) << 8;
               ++offset;
               jumpOffset = var10000 + (short)(var10001 | code[offset] & 255);
               jumps[jumpOffset] = true;
               continue;
            case 168:
               var10000 = offset++;
               var10001 = (code[offset] & 255) << 8;
               ++offset;
               jumpOffset = var10000 + (short)(var10001 | code[offset] & 255);
               offsetSet.add(jumpOffset);
               continue;
            default:
               offset += 2;
               continue;
            }
         case 4:
            switch(opcode) {
            case 200:
               var10000 = offset++ + ((code[offset] & 255) << 24);
               ++offset;
               var10000 |= (code[offset] & 255) << 16;
               ++offset;
               var10000 |= (code[offset] & 255) << 8;
               ++offset;
               jumpOffset = var10000 | code[offset] & 255;
               jumps[jumpOffset] = true;
               break;
            case 201:
               var10000 = offset++ + ((code[offset] & 255) << 24);
               ++offset;
               var10000 |= (code[offset] & 255) << 16;
               ++offset;
               var10000 |= (code[offset] & 255) << 8;
               ++offset;
               jumpOffset = var10000 | code[offset] & 255;
               offsetSet.add(jumpOffset);
               break;
            default:
               offset += 4;
            }
         }
      }

   }

   private static class CodeExceptionComparator implements Comparator<CodeException> {
      private CodeExceptionComparator() {
      }

      public int compare(CodeException ce1, CodeException ce2) {
         return ce1.handler_pc != ce2.handler_pc?ce1.handler_pc - ce2.handler_pc:(ce1.end_pc != ce2.end_pc?ce1.end_pc - ce2.end_pc:(ce1.start_pc != ce2.start_pc?ce1.start_pc - ce2.start_pc:ce1.index - ce2.index));
      }

      // $FF: synthetic method
      CodeExceptionComparator(InstructionListBuilder.CodeExceptionComparator var1) {
         this();
      }
   }
}
