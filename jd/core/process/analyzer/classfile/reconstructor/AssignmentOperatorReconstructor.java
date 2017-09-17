package jd.core.process.analyzer.classfile.reconstructor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.GetField;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.process.analyzer.classfile.visitor.CompareInstructionVisitor;

public class AssignmentOperatorReconstructor {
   public static void Reconstruct(List<Instruction> list) {
      int index = list.size();

      while(index-- > 0) {
         Instruction i = (Instruction)list.get(index);
         BinaryOperatorInstruction boi;
         switch(i.opcode) {
         case 54:
            if(((StoreInstruction)i).valueref.opcode == 267) {
               boi = (BinaryOperatorInstruction)((StoreInstruction)i).valueref;
               if(boi.value1.opcode == 21) {
                  index = ReconstructStoreOperator(list, index, i, boi);
               }
            }
            break;
         case 179:
            if(((PutStatic)i).valueref.opcode == 267) {
               index = ReconstructPutStaticOperator(list, index, i);
            }
            break;
         case 181:
            if(((PutField)i).valueref.opcode == 267) {
               index = ReconstructPutFieldOperator(list, index, i);
            }
            break;
         case 269:
            if(((StoreInstruction)i).valueref.opcode == 267) {
               boi = (BinaryOperatorInstruction)((StoreInstruction)i).valueref;
               if(boi.value1.opcode == 268) {
                  index = ReconstructStoreOperator(list, index, i, boi);
               }
            }
            break;
         case 272:
            if(((ArrayStoreInstruction)i).valueref.opcode == 267) {
               index = ReconstructArrayOperator(list, index, i);
            }
         }
      }

   }

   private static int ReconstructPutStaticOperator(List<Instruction> list, int index, Instruction i) {
      PutStatic putStatic = (PutStatic)i;
      BinaryOperatorInstruction boi = (BinaryOperatorInstruction)putStatic.valueref;
      if(boi.value1.opcode != 178) {
         return index;
      } else {
         GetStatic getStatic = (GetStatic)boi.value1;
         if(putStatic.lineNumber == getStatic.lineNumber && putStatic.index == getStatic.index) {
            String newOperator = boi.operator + "=";
            list.set(index, new AssignmentInstruction(265, putStatic.offset, getStatic.lineNumber, boi.getPriority(), newOperator, getStatic, boi.value2));
            return index;
         } else {
            return index;
         }
      }
   }

   private static int ReconstructPutFieldOperator(List<Instruction> list, int index, Instruction i) {
      PutField putField = (PutField)i;
      BinaryOperatorInstruction boi = (BinaryOperatorInstruction)putField.valueref;
      if(boi.value1.opcode != 180) {
         return index;
      } else {
         GetField getField = (GetField)boi.value1;
         CompareInstructionVisitor visitor = new CompareInstructionVisitor();
         if(putField.lineNumber == getField.lineNumber && putField.index == getField.index && visitor.visit(putField.objectref, getField.objectref)) {
            if(putField.objectref.opcode == 263) {
               DupLoad newOperator = (DupLoad)getField.objectref;
               index = DeleteDupStoreInstruction(list, index, newOperator);
               getField.objectref = newOperator.dupStore.objectref;
            }

            String newOperator1 = boi.operator + "=";
            list.set(index, new AssignmentInstruction(265, putField.offset, getField.lineNumber, boi.getPriority(), newOperator1, getField, boi.value2));
            return index;
         } else {
            return index;
         }
      }
   }

   private static int ReconstructStoreOperator(List<Instruction> list, int index, Instruction i, BinaryOperatorInstruction boi) {
      StoreInstruction si = (StoreInstruction)i;
      LoadInstruction li = (LoadInstruction)boi.value1;
      if(si.lineNumber == li.lineNumber && si.index == li.index) {
         String newOperator = boi.operator + "=";
         list.set(index, new AssignmentInstruction(265, si.offset, li.lineNumber, boi.getPriority(), newOperator, li, boi.value2));
         return index;
      } else {
         return index;
      }
   }

   private static int ReconstructArrayOperator(List<Instruction> list, int index, Instruction i) {
      ArrayStoreInstruction asi = (ArrayStoreInstruction)i;
      BinaryOperatorInstruction boi = (BinaryOperatorInstruction)asi.valueref;
      if(boi.value1.opcode != 271) {
         return index;
      } else {
         ArrayLoadInstruction ali = (ArrayLoadInstruction)boi.value1;
         CompareInstructionVisitor visitor = new CompareInstructionVisitor();
         if(asi.lineNumber == ali.lineNumber && visitor.visit(asi.arrayref, ali.arrayref) && visitor.visit(asi.indexref, ali.indexref)) {
            DupLoad newOperator;
            if(asi.arrayref.opcode == 263) {
               newOperator = (DupLoad)ali.arrayref;
               index = DeleteDupStoreInstruction(list, index, newOperator);
               ali.arrayref = newOperator.dupStore.objectref;
            }

            if(asi.indexref.opcode == 263) {
               newOperator = (DupLoad)ali.indexref;
               index = DeleteDupStoreInstruction(list, index, newOperator);
               ali.indexref = newOperator.dupStore.objectref;
            }

            String newOperator1 = boi.operator + "=";
            list.set(index, new AssignmentInstruction(265, asi.offset, ali.lineNumber, boi.getPriority(), newOperator1, ali, boi.value2));
            return index;
         } else {
            return index;
         }
      }
   }

   private static int DeleteDupStoreInstruction(List<Instruction> list, int index, DupLoad dupLoad) {
      int indexTmp = index;

      while(indexTmp-- > 0) {
         Instruction i = (Instruction)list.get(indexTmp);
         if(dupLoad.dupStore == i) {
            list.remove(indexTmp);
            --index;
            return index;
         }
      }

      return index;
   }
}
