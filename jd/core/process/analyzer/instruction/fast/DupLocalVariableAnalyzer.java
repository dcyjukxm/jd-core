package jd.core.process.analyzer.instruction.fast;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.instruction.fast.instruction.FastList;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.model.instruction.fast.instruction.FastTest2Lists;
import jd.core.model.instruction.fast.instruction.FastTry;

public class DupLocalVariableAnalyzer {
   public static void Declare(ClassFile classFile, Method method, List<Instruction> list) {
      RecursiveDeclare(classFile.getConstantPool(), method.getLocalVariables(), method.getCode().length, list);
   }

   private static void RecursiveDeclare(ConstantPool constants, LocalVariables localVariables, int codeLength, List<Instruction> list) {
      int length = list.size();

      int i;
      Instruction instruction;
      label64:
      for(i = 0; i < length; ++i) {
         instruction = (Instruction)list.get(i);
         int signature;
         switch(instruction.opcode) {
         case 301:
         case 302:
         case 303:
         case 304:
         case 305:
         case 306:
         case 319:
            List var15 = ((FastList)instruction).instructions;
            if(var15 != null) {
               RecursiveDeclare(constants, localVariables, codeLength, var15);
            }
            continue;
         case 307:
            FastTest2Lists var14 = (FastTest2Lists)instruction;
            RecursiveDeclare(constants, localVariables, codeLength, var14.instructions);
            RecursiveDeclare(constants, localVariables, codeLength, var14.instructions2);
         case 308:
         case 309:
         case 310:
         case 311:
         case 312:
         case 313:
         case 317:
         default:
            continue;
         case 314:
         case 315:
         case 316:
            FastSwitch.Pair[] var13 = ((FastSwitch)instruction).pairs;
            if(var13 == null) {
               continue;
            }

            signature = var13.length - 1;

            while(true) {
               if(signature < 0) {
                  continue label64;
               }

               List signatureIndex = var13[signature].getInstructions();
               if(signatureIndex != null) {
                  RecursiveDeclare(constants, localVariables, codeLength, signatureIndex);
               }

               --signature;
            }
         case 318:
         }

         FastTry dupStore = (FastTry)instruction;
         RecursiveDeclare(constants, localVariables, codeLength, dupStore.instructions);
         if(dupStore.catches != null) {
            for(signature = dupStore.catches.size() - 1; signature >= 0; --signature) {
               RecursiveDeclare(constants, localVariables, codeLength, ((FastTry.FastCatch)dupStore.catches.get(signature)).instructions);
            }
         }

         if(dupStore.finallyInstructions != null) {
            RecursiveDeclare(constants, localVariables, codeLength, dupStore.finallyInstructions);
         }
      }

      for(i = 0; i < length; ++i) {
         instruction = (Instruction)list.get(i);
         if(instruction.opcode == 264) {
            DupStore var16 = (DupStore)instruction;
            String var17 = var16.objectref.getReturnedSignature(constants, localVariables);
            int var18 = constants.addConstantUtf8(var17);
            int nameIndex = constants.addConstantUtf8("tmp" + var16.offset + "_" + ((DupStore)instruction).objectref.offset);
            int varIndex = localVariables.size();
            LocalVariable lv = new LocalVariable(var16.offset, codeLength, nameIndex, var18, varIndex);
            lv.declarationFlag = true;
            localVariables.add(lv);
            list.set(i, new FastDeclaration(317, var16.offset, Instruction.UNKNOWN_LINE_NUMBER, lv.index, var16));
         }
      }

   }
}
