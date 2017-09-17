package jd.core.process.analyzer.instruction.fast;

import java.util.List;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Return;
import jd.core.model.instruction.fast.instruction.FastList;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.model.instruction.fast.instruction.FastTest2Lists;
import jd.core.model.instruction.fast.instruction.FastTry;

public class ReturnLineNumberAnalyzer {
   public static void Check(Method method) {
      List list = method.getFastNodes();
      int length = list.size();
      if(length > 1) {
         int afterListLineNumber = ((Instruction)list.get(length - 1)).lineNumber;
         if(afterListLineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
            RecursiveCheck(list, afterListLineNumber);
         }
      }

   }

   private static void RecursiveCheck(List<Instruction> list, int afterListLineNumber) {
      Instruction instruction;
      label62:
      for(int index = list.size(); index-- > 0; afterListLineNumber = instruction.lineNumber) {
         instruction = (Instruction)list.get(index);
         int i;
         List catchInstructions;
         switch(instruction.opcode) {
         case 177:
            Return var10 = (Return)instruction;
            if(var10.lineNumber > afterListLineNumber) {
               var10.lineNumber = Instruction.UNKNOWN_LINE_NUMBER;
            }
            break;
         case 301:
         case 302:
         case 303:
         case 304:
         case 305:
         case 306:
         case 319:
            List var9 = ((FastList)instruction).instructions;
            if(var9 != null) {
               RecursiveCheck(var9, afterListLineNumber);
            }
            break;
         case 307:
            FastTest2Lists var8 = (FastTest2Lists)instruction;
            RecursiveCheck(var8.instructions, afterListLineNumber);
            RecursiveCheck(var8.instructions2, afterListLineNumber);
            break;
         case 314:
         case 315:
         case 316:
            FastSwitch.Pair[] var7 = ((FastSwitch)instruction).pairs;
            if(var7 == null) {
               break;
            }

            i = var7.length - 1;

            while(true) {
               if(i < 0) {
                  continue label62;
               }

               catchInstructions = var7[i].getInstructions();
               if(catchInstructions != null) {
                  RecursiveCheck(catchInstructions, afterListLineNumber);
                  if(catchInstructions.size() > 0) {
                     afterListLineNumber = ((Instruction)catchInstructions.get(0)).lineNumber;
                  }
               }

               --i;
            }
         case 318:
            FastTry r = (FastTry)instruction;
            if(r.finallyInstructions != null) {
               RecursiveCheck(r.finallyInstructions, afterListLineNumber);
               if(r.finallyInstructions.size() > 0) {
                  afterListLineNumber = ((Instruction)r.finallyInstructions.get(0)).lineNumber;
               }
            }

            if(r.catches != null) {
               for(i = r.catches.size() - 1; i >= 0; --i) {
                  catchInstructions = ((FastTry.FastCatch)r.catches.get(i)).instructions;
                  RecursiveCheck(catchInstructions, afterListLineNumber);
                  if(catchInstructions.size() > 0) {
                     afterListLineNumber = ((Instruction)catchInstructions.get(0)).lineNumber;
                  }
               }
            }

            RecursiveCheck(r.instructions, afterListLineNumber);
         }
      }

   }
}
