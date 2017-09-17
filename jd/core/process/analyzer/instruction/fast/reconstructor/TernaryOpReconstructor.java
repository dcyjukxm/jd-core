package jd.core.process.analyzer.instruction.fast.reconstructor;

import java.util.List;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.process.analyzer.instruction.bytecode.ComparisonInstructionAnalyzer;
import jd.core.process.analyzer.instruction.fast.visitor.ReplaceInstructionVisitor;

public class TernaryOpReconstructor {
   public static void Reconstruct(List<Instruction> list) {
      int length = list.size();

      for(int index = 1; index < length; ++index) {
         Instruction i = (Instruction)list.get(index);
         if(i.opcode == 280 && index + 2 < length) {
            Instruction gi = (Instruction)list.get(index + 1);
            Instruction afterGi = (Instruction)list.get(index + 2);
            Instruction test = null;
            int indexTest = index;

            while(indexTest-- > 0) {
               Instruction value1 = (Instruction)list.get(indexTest);
               int fto = value1.opcode;
               if(fto == 260 || fto == 261 || fto == 262 || fto == 284) {
                  int visitor = ((BranchInstruction)value1).GetJumpOffset();
                  if(gi.offset < visitor && visitor <= afterGi.offset) {
                     test = value1;
                     break;
                  }
               }
            }

            if(test != null) {
               TernaryOpStore var12 = (TernaryOpStore)i;
               ComparisonInstructionAnalyzer.InverseComparison(test);
               TernaryOperator var13 = new TernaryOperator(281, var12.ternaryOp2ndValueOffset, test.lineNumber, test, var12.objectref, (Instruction)null);
               ReplaceInstructionVisitor var14 = new ReplaceInstructionVisitor(var12.ternaryOp2ndValueOffset, var13);
               int indexVisitor = index + 2;

               while(indexVisitor < length && var14.getOldInstruction() == null) {
                  var14.visit((Instruction)list.get(indexVisitor++));
               }

               var13.value2 = var14.getOldInstruction();
               if(isBooleanConstant(var13.value1) && isBooleanConstant(var13.value2)) {
                  if(((IConst)var13.value1).value == 0) {
                     ComparisonInstructionAnalyzer.InverseComparison(var13.test);
                  }

                  var14.init(var13.offset, var13.test);
                  indexVisitor = index + 2;

                  while(indexVisitor < length && var14.getOldInstruction() == null) {
                     var14.visit((Instruction)list.get(indexVisitor++));
                  }
               }

               list.remove(index + 1);
               list.remove(index);
               list.remove(indexTest);
               index -= 2;
               length -= 3;
            }
         }
      }

   }

   private static boolean isBooleanConstant(Instruction instruction) {
      if(instruction == null) {
         return false;
      } else {
         switch(instruction.opcode) {
         case 16:
         case 17:
         case 256:
            return "Z".equals(instruction.getReturnedSignature((ConstantPool)null, (LocalVariables)null));
         default:
            return false;
         }
      }
   }
}
