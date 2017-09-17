package jd.core.process.analyzer.classfile.reconstructor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.ConstInstruction;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.process.analyzer.util.ReconstructorUtil;

public class PreIncReconstructor {
   public static void Reconstruct(List<Instruction> list) {
      int length = list.size();

      label94:
      for(int dupStoreIndex = 0; dupStoreIndex < length; ++dupStoreIndex) {
         if(((Instruction)list.get(dupStoreIndex)).opcode == 264) {
            DupStore dupstore = (DupStore)list.get(dupStoreIndex);
            if(dupstore.objectref.opcode == 267) {
               BinaryOperatorInstruction boi = (BinaryOperatorInstruction)dupstore.objectref;
               if(boi.value2.opcode == 256 || boi.value2.opcode == 257 || boi.value2.opcode == 259 || boi.value2.opcode == 258) {
                  ConstInstruction ci = (ConstInstruction)boi.value2;
                  if(ci.value == 1) {
                     byte value;
                     if(boi.operator.equals("+")) {
                        value = 1;
                     } else {
                        if(!boi.operator.equals("-")) {
                           continue;
                        }

                        value = -1;
                     }

                     int xstorePutfieldPutstaticIndex = dupStoreIndex;

                     DupLoad dupload;
                     do {
                        ++xstorePutfieldPutstaticIndex;
                        if(xstorePutfieldPutstaticIndex >= length) {
                           continue label94;
                        }

                        Instruction i = (Instruction)list.get(xstorePutfieldPutstaticIndex);
                        dupload = null;
                        switch(i.opcode) {
                        case 54:
                           if(boi.value1.opcode == 21 && ((StoreInstruction)i).valueref.opcode == 263 && ((IndexInstruction)i).index == ((IndexInstruction)boi.value1).index) {
                              dupload = (DupLoad)((StoreInstruction)i).valueref;
                           }
                           break;
                        case 58:
                           if(boi.value1.opcode == 25 && ((StoreInstruction)i).valueref.opcode == 263 && ((IndexInstruction)i).index == ((IndexInstruction)boi.value1).index) {
                              dupload = (DupLoad)((StoreInstruction)i).valueref;
                           }
                           break;
                        case 179:
                           if(boi.value1.opcode == 178 && ((PutStatic)i).valueref.opcode == 263 && ((IndexInstruction)i).index == ((IndexInstruction)boi.value1).index) {
                              dupload = (DupLoad)((PutStatic)i).valueref;
                           }
                           break;
                        case 181:
                           if(boi.value1.opcode == 180 && ((PutField)i).valueref.opcode == 263 && ((IndexInstruction)i).index == ((IndexInstruction)boi.value1).index) {
                              dupload = (DupLoad)((PutField)i).valueref;
                           }
                           break;
                        case 269:
                           if(boi.value1.opcode == 268 && ((StoreInstruction)i).valueref.opcode == 263 && ((IndexInstruction)i).index == ((IndexInstruction)boi.value1).index) {
                              dupload = (DupLoad)((StoreInstruction)i).valueref;
                           }
                        }
                     } while(dupload == null || dupload.offset != dupstore.offset);

                     IncInstruction preinc = new IncInstruction(277, boi.offset, boi.lineNumber, boi.value1, value);
                     ReconstructorUtil.ReplaceDupLoad(list, xstorePutfieldPutstaticIndex + 1, dupstore, preinc);
                     list.remove(xstorePutfieldPutstaticIndex);
                     list.remove(dupStoreIndex);
                     --dupStoreIndex;
                     length = list.size();
                  }
               }
            }
         }
      }

   }
}
