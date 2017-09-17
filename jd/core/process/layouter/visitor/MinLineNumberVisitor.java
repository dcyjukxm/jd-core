package jd.core.process.layouter.visitor;

import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;

public class MinLineNumberVisitor {
   public static int visit(Instruction instruction) {
      IncInstruction ii;
      switch(instruction.opcode) {
      case 83:
      case 272:
         return visit(((ArrayStoreInstruction)instruction).arrayref);
      case 87:
         return visit(((Pop)instruction).objectref);
      case 181:
         return visit(((PutField)instruction).objectref);
      case 182:
      case 183:
      case 185:
         return visit(((InvokeNoStaticInstruction)instruction).objectref);
      case 193:
         return visit(((InstanceOf)instruction).objectref);
      case 265:
         return visit(((AssignmentInstruction)instruction).value1);
      case 267:
         return visit(((BinaryOperatorInstruction)instruction).value1);
      case 271:
         return visit(((ArrayLoadInstruction)instruction).arrayref);
      case 277:
         ii = (IncInstruction)instruction;
         switch(ii.count) {
         case -1:
         case 1:
            return instruction.lineNumber;
         case 0:
         default:
            return visit(ii.value);
         }
      case 278:
         ii = (IncInstruction)instruction;
         switch(ii.count) {
         case -1:
         case 1:
            return visit(ii.value);
         case 0:
         default:
            return instruction.lineNumber;
         }
      case 281:
         return visit(((TernaryOperator)instruction).test);
      default:
         return instruction.lineNumber;
      }
   }
}
