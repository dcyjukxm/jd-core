package jd.core.process.analyzer.instruction.fast.visitor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.fast.instruction.FastSynchronized;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.process.analyzer.classfile.visitor.CompareInstructionVisitor;

public class FastCompareInstructionVisitor extends CompareInstructionVisitor {
   public boolean visit(List<Instruction> list1, List<Instruction> list2, int index1, int index2, int length) {
      if(index1 + length <= list1.size() && index2 + length <= list2.size()) {
         while(length-- > 0) {
            if(!this.visit((Instruction)list1.get(index1++), (Instruction)list2.get(index2++))) {
               return false;
            }
         }
      }

      return true;
   }

   public boolean visit(Instruction i1, Instruction i2) {
      if(i1.opcode != i2.opcode) {
         return false;
      } else {
         int i;
         FastTry var8;
         FastTry var9;
         switch(i1.opcode) {
         case 318:
            var8 = (FastTry)i1;
            var9 = (FastTry)i2;
            i = var8.catches.size();
            if(i != var9.catches.size()) {
               return false;
            }

            if(var8.finallyInstructions == null) {
               if(var9.finallyInstructions != null) {
                  return false;
               }
            } else if(var9.finallyInstructions == null) {
               if(var8.finallyInstructions != null) {
                  return false;
               }
            } else if(!this.visit(var8.finallyInstructions, var9.finallyInstructions)) {
               return false;
            }
            break;
         case 319:
            FastSynchronized fs1 = (FastSynchronized)i1;
            FastSynchronized fs2 = (FastSynchronized)i2;
            if(!this.visit(fs1.monitor, fs2.monitor)) {
               return false;
            }

            return this.visit(fs1.instructions, fs2.instructions);
         default:
            return super.visit(i1, i2);
         }

         FastTry.FastCatch fc1;
         FastTry.FastCatch fc2;
         do {
            if(i-- <= 0) {
               return this.visit(var8.instructions, var9.instructions);
            }

            fc1 = (FastTry.FastCatch)var8.catches.get(i);
            fc2 = (FastTry.FastCatch)var9.catches.get(i);
         } while(fc1.exceptionTypeIndex == fc2.exceptionTypeIndex && this.visit(fc1.instructions, fc2.instructions) && CompareExceptionTypeIndexes(fc1.otherExceptionTypeIndexes, fc2.otherExceptionTypeIndexes));

         return false;
      }
   }

   private static boolean CompareExceptionTypeIndexes(int[] otherExceptionTypeIndexes1, int[] otherExceptionTypeIndexes2) {
      if(otherExceptionTypeIndexes1 == null) {
         return otherExceptionTypeIndexes2 == null;
      } else if(otherExceptionTypeIndexes2 == null) {
         return false;
      } else {
         int i = otherExceptionTypeIndexes1.length;
         if(i != otherExceptionTypeIndexes2.length) {
            return false;
         } else {
            while(i-- > 0) {
               if(otherExceptionTypeIndexes1[i] != otherExceptionTypeIndexes2[i]) {
                  return false;
               }
            }

            return true;
         }
      }
   }
}
