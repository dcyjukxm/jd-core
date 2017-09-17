package jd.core.process.analyzer.instruction.fast.reconstructor;

import java.util.ArrayList;
import java.util.List;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.BIPush;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.SIPush;
import jd.core.process.analyzer.util.ReconstructorUtil;

public class InitArrayInstructionReconstructor {
   public static void Reconstruct(List<Instruction> list) {
      for(int index = list.size() - 1; index >= 0; --index) {
         Instruction i = (Instruction)list.get(index);
         if(i.opcode == 264) {
            DupStore dupStore = (DupStore)i;
            int opcode = dupStore.objectref.opcode;
            if(opcode == 188 || opcode == 189) {
               ReconstructAInstruction(list, index, dupStore);
            }
         }
      }

   }

   private static void ReconstructAInstruction(List<Instruction> list, int index, DupStore dupStore) {
      int length = list.size();
      int firstDupStoreIndex = index;
      DupStore lastDupStore = dupStore;
      ArrayStoreInstruction lastAsi = null;
      int arrayIndex = 0;
      ArrayList values = new ArrayList();

      while(true) {
         ++index;
         if(index >= length) {
            break;
         }

         Instruction iai = (Instruction)list.get(index);
         if(iai.opcode != 83 && iai.opcode != 272) {
            break;
         }

         ArrayStoreInstruction parent = (ArrayStoreInstruction)iai;
         if(parent.arrayref.opcode != 263 || parent.arrayref.offset != lastDupStore.offset) {
            break;
         }

         lastAsi = parent;

         for(int na = getArrayIndex(parent.indexref); na > arrayIndex; ++arrayIndex) {
            values.add(new IConst(256, parent.offset, parent.lineNumber, 0));
         }

         values.add(parent.valueref);
         ++arrayIndex;
         ++index;
         if(index >= length) {
            break;
         }

         iai = (Instruction)list.get(index);
         if(iai.opcode != 264) {
            break;
         }

         DupStore nextDupStore = (DupStore)iai;
         if(nextDupStore.objectref.opcode != 263 || nextDupStore.objectref.offset != lastDupStore.offset) {
            break;
         }

         lastDupStore = nextDupStore;
      }

      if(lastAsi != null) {
         InitArrayInstruction var13 = new InitArrayInstruction(283, lastAsi.offset, dupStore.lineNumber, dupStore.objectref, values);
         Instruction var14 = ReconstructorUtil.ReplaceDupLoad(list, index, lastDupStore, var13);
         if(var14 != null) {
            switch(var14.opcode) {
            case 83:
               var13.opcode = 282;
            }
         }

         while(firstDupStoreIndex < index) {
            --index;
            list.remove(index);
         }

         if(var13.newArray.opcode == 188) {
            NewArray var15 = (NewArray)var13.newArray;
            switch(var15.type) {
            case 4:
               SetContantTypes("Z", var13.values);
               break;
            case 5:
               SetContantTypes("C", var13.values);
            case 6:
            case 7:
            default:
               break;
            case 8:
               SetContantTypes("B", var13.values);
               break;
            case 9:
               SetContantTypes("S", var13.values);
               break;
            case 10:
               SetContantTypes("I", var13.values);
            }
         }
      }

   }

   private static void SetContantTypes(String signature, List<Instruction> values) {
      int length = values.size();
      int i = 0;

      while(i < length) {
         Instruction value = (Instruction)values.get(i);
         switch(value.opcode) {
         case 16:
         case 17:
         case 256:
            ((IConst)value).setReturnedSignature(signature);
         default:
            ++i;
         }
      }

   }

   private static int getArrayIndex(Instruction i) {
      switch(i.opcode) {
      case 16:
         return ((BIPush)i).value;
      case 17:
         return ((SIPush)i).value;
      case 256:
         return ((IConst)i).value;
      default:
         return -1;
      }
   }
}
