package jd.core.process.analyzer.classfile.reconstructor;

import java.util.List;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.AALoad;
import jd.core.model.instruction.bytecode.instruction.AAStore;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.GetField;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.ILoad;
import jd.core.model.instruction.bytecode.instruction.IStore;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;
import jd.core.process.analyzer.classfile.visitor.CompareInstructionVisitor;
import jd.core.process.analyzer.classfile.visitor.ReplaceDupLoadVisitor;
import jd.core.process.analyzer.classfile.visitor.SearchDupLoadInstructionVisitor;

public class AssignmentInstructionReconstructor {
   public static void Reconstruct(List<Instruction> list) {
      label93:
      for(int dupStoreIndex = 0; dupStoreIndex < list.size(); ++dupStoreIndex) {
         if(((Instruction)list.get(dupStoreIndex)).opcode == 264) {
            DupStore dupStore = (DupStore)list.get(dupStoreIndex);
            int length = list.size();
            if(dupStoreIndex + 1 < length) {
               Instruction xstorePutfieldPutstaticIndex = (Instruction)list.get(dupStoreIndex + 1);
               if(xstorePutfieldPutstaticIndex.opcode == 83 || xstorePutfieldPutstaticIndex.opcode == 272) {
                  xstorePutfieldPutstaticIndex = ((ArrayStoreInstruction)xstorePutfieldPutstaticIndex).arrayref;
                  if(xstorePutfieldPutstaticIndex.opcode == 263 && ((DupLoad)xstorePutfieldPutstaticIndex).dupStore == dupStore) {
                     continue;
                  }
               }
            }

            int var13 = dupStoreIndex;

            while(true) {
               while(true) {
                  Instruction xstorePutfieldPutstatic;
                  DupLoad dupload1;
                  int dupload2Index;
                  DupLoad var14;
                  do {
                     do {
                        ++var13;
                        if(var13 >= length) {
                           continue label93;
                        }

                        xstorePutfieldPutstatic = (Instruction)list.get(var13);
                        dupload1 = null;
                        switch(xstorePutfieldPutstatic.opcode) {
                        case 54:
                        case 58:
                        case 83:
                        case 179:
                        case 181:
                        case 269:
                        case 272:
                           Instruction dupload2 = ((ValuerefAttribute)xstorePutfieldPutstatic).getValueref();
                           if(dupload2.opcode == 263 && ((DupLoad)dupload2).dupStore == dupStore) {
                              dupload1 = (DupLoad)dupload2;
                           }
                           break;
                        case 55:
                        case 56:
                        case 57:
                           (new RuntimeException("Instruction inattendue")).printStackTrace();
                        }
                     } while(dupload1 == null);

                     var14 = null;
                     dupload2Index = var13;

                     do {
                        ++dupload2Index;
                        if(dupload2Index >= length) {
                           break;
                        }

                        var14 = SearchDupLoadInstructionVisitor.visit((Instruction)list.get(dupload2Index), dupStore);
                     } while(var14 == null);
                  } while(var14 == null);

                  Instruction newInstruction;
                  ReplaceDupLoadVisitor visitor;
                  if(dupload1.lineNumber == var14.lineNumber) {
                     newInstruction = CreateAssignmentInstruction(xstorePutfieldPutstatic, dupStore);
                     visitor = new ReplaceDupLoadVisitor(dupStore, newInstruction);
                     visitor.visit((Instruction)list.get(dupload2Index));
                     int j = dupStoreIndex;

                     while(j-- > 0) {
                        if(((Instruction)list.get(j)).opcode == 280) {
                           TernaryOpStore tos = (TernaryOpStore)list.get(j);
                           if(tos.ternaryOp2ndValueOffset == dupStore.offset) {
                              tos.ternaryOp2ndValueOffset = newInstruction.offset;
                              break;
                           }
                        }
                     }

                     list.remove(var13);
                     list.remove(dupStoreIndex);
                     --dupStoreIndex;
                     length -= 2;
                  } else {
                     newInstruction = CreateInstruction(xstorePutfieldPutstatic);
                     if(newInstruction != null) {
                        visitor = new ReplaceDupLoadVisitor(dupStore, dupStore.objectref);
                        visitor.visit(xstorePutfieldPutstatic);
                        visitor.init(dupStore, newInstruction);
                        visitor.visit((Instruction)list.get(dupload2Index));
                        list.remove(dupStoreIndex);
                        --dupStoreIndex;
                        --length;
                     }
                  }
               }
            }
         }
      }

   }

   private static Instruction CreateInstruction(Instruction xstorePutfieldPutstatic) {
      switch(xstorePutfieldPutstatic.opcode) {
      case 54:
         return new ILoad(21, xstorePutfieldPutstatic.offset, xstorePutfieldPutstatic.lineNumber, ((IStore)xstorePutfieldPutstatic).index);
      case 58:
         return new ALoad(25, xstorePutfieldPutstatic.offset, xstorePutfieldPutstatic.lineNumber, ((AStore)xstorePutfieldPutstatic).index);
      case 83:
         return new AALoad(271, xstorePutfieldPutstatic.offset, xstorePutfieldPutstatic.lineNumber, ((AAStore)xstorePutfieldPutstatic).arrayref, ((AAStore)xstorePutfieldPutstatic).indexref);
      case 179:
         return new GetStatic(178, xstorePutfieldPutstatic.offset, xstorePutfieldPutstatic.lineNumber, ((PutStatic)xstorePutfieldPutstatic).index);
      case 181:
         return new GetField(180, xstorePutfieldPutstatic.offset, xstorePutfieldPutstatic.lineNumber, ((PutField)xstorePutfieldPutstatic).index, ((PutField)xstorePutfieldPutstatic).objectref);
      case 269:
         return new LoadInstruction(268, xstorePutfieldPutstatic.offset, xstorePutfieldPutstatic.lineNumber, ((StoreInstruction)xstorePutfieldPutstatic).index, xstorePutfieldPutstatic.getReturnedSignature((ConstantPool)null, (LocalVariables)null));
      case 272:
         return new ArrayLoadInstruction(271, xstorePutfieldPutstatic.offset, xstorePutfieldPutstatic.lineNumber, ((ArrayStoreInstruction)xstorePutfieldPutstatic).arrayref, ((ArrayStoreInstruction)xstorePutfieldPutstatic).indexref, ((ArrayStoreInstruction)xstorePutfieldPutstatic).signature);
      default:
         return null;
      }
   }

   private static Instruction CreateAssignmentInstruction(Instruction xstorePutfieldPutstatic, DupStore dupStore) {
      Instruction newInstruction;
      if(dupStore.objectref.opcode == 267) {
         newInstruction = ((BinaryOperatorInstruction)dupStore.objectref).value1;
         if(xstorePutfieldPutstatic.lineNumber == newInstruction.lineNumber) {
            ArrayStoreInstruction aas;
            ArrayLoadInstruction aal;
            CompareInstructionVisitor visitor;
            switch(xstorePutfieldPutstatic.opcode) {
            case 54:
               if(newInstruction.opcode == 21 && ((StoreInstruction)xstorePutfieldPutstatic).index == ((LoadInstruction)newInstruction).index) {
                  return CreateBinaryOperatorAssignmentInstruction(xstorePutfieldPutstatic, dupStore);
               }
               break;
            case 55:
            case 56:
            case 57:
               (new RuntimeException("Unexpected instruction")).printStackTrace();
               break;
            case 58:
               if(newInstruction.opcode == 25 && ((StoreInstruction)xstorePutfieldPutstatic).index == ((LoadInstruction)newInstruction).index) {
                  return CreateBinaryOperatorAssignmentInstruction(xstorePutfieldPutstatic, dupStore);
               }
               break;
            case 83:
               if(newInstruction.opcode == 50) {
                  aas = (ArrayStoreInstruction)xstorePutfieldPutstatic;
                  aal = (ArrayLoadInstruction)newInstruction;
                  visitor = new CompareInstructionVisitor();
                  if(visitor.visit(aas.arrayref, aal.arrayref) && visitor.visit(aas.indexref, aal.indexref)) {
                     return CreateBinaryOperatorAssignmentInstruction(xstorePutfieldPutstatic, dupStore);
                  }
               }
               break;
            case 179:
               if(newInstruction.opcode == 180 && ((PutStatic)xstorePutfieldPutstatic).index == ((GetStatic)newInstruction).index) {
                  return CreateBinaryOperatorAssignmentInstruction(xstorePutfieldPutstatic, dupStore);
               }
               break;
            case 181:
               if(newInstruction.opcode == 180 && ((PutField)xstorePutfieldPutstatic).index == ((GetField)newInstruction).index) {
                  CompareInstructionVisitor aas1 = new CompareInstructionVisitor();
                  if(aas1.visit(((PutField)xstorePutfieldPutstatic).objectref, ((GetField)newInstruction).objectref)) {
                     return CreateBinaryOperatorAssignmentInstruction(xstorePutfieldPutstatic, dupStore);
                  }
               }
               break;
            case 269:
               if(newInstruction.opcode == 268 && ((StoreInstruction)xstorePutfieldPutstatic).index == ((LoadInstruction)newInstruction).index) {
                  return CreateBinaryOperatorAssignmentInstruction(xstorePutfieldPutstatic, dupStore);
               }
               break;
            case 272:
               if(newInstruction.opcode == 271) {
                  aas = (ArrayStoreInstruction)xstorePutfieldPutstatic;
                  aal = (ArrayLoadInstruction)newInstruction;
                  visitor = new CompareInstructionVisitor();
                  if(visitor.visit(aas.arrayref, aal.arrayref) && visitor.visit(aas.indexref, aal.indexref)) {
                     return CreateBinaryOperatorAssignmentInstruction(xstorePutfieldPutstatic, dupStore);
                  }
               }
            }
         }
      }

      newInstruction = CreateInstruction(xstorePutfieldPutstatic);
      return new AssignmentInstruction(265, xstorePutfieldPutstatic.offset, dupStore.lineNumber, 14, "=", newInstruction, dupStore.objectref);
   }

   private static AssignmentInstruction CreateBinaryOperatorAssignmentInstruction(Instruction xstorePutfieldPutstatic, DupStore dupstore) {
      BinaryOperatorInstruction boi = (BinaryOperatorInstruction)dupstore.objectref;
      String newOperator = boi.operator + "=";
      return new AssignmentInstruction(265, xstorePutfieldPutstatic.offset, dupstore.lineNumber, boi.getPriority(), newOperator, CreateInstruction(xstorePutfieldPutstatic), boi.value2);
   }
}
