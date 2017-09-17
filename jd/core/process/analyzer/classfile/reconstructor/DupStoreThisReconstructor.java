package jd.core.process.analyzer.classfile.reconstructor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.MonitorEnter;
import jd.core.process.analyzer.classfile.visitor.ReplaceDupLoadVisitor;

public class DupStoreThisReconstructor {
   public static void Reconstruct(List<Instruction> list) {
      for(int dupStoreIndex = 0; dupStoreIndex < list.size(); ++dupStoreIndex) {
         if(((Instruction)list.get(dupStoreIndex)).opcode == 264) {
            DupStore dupStore = (DupStore)list.get(dupStoreIndex);
            if(dupStore.objectref.opcode == 25 && ((ALoad)dupStore.objectref).index == 0) {
               if(dupStoreIndex + 2 < list.size()) {
                  Instruction visitor = (Instruction)list.get(dupStoreIndex + 2);
                  if(visitor.opcode == 194) {
                     MonitorEnter length = (MonitorEnter)visitor;
                     if(length.objectref.opcode == 263 && ((DupLoad)length.objectref).dupStore == dupStore) {
                        continue;
                     }
                  }
               }

               ReplaceDupLoadVisitor var6 = new ReplaceDupLoadVisitor(dupStore, dupStore.objectref);
               int var7 = list.size();

               int index;
               for(index = dupStoreIndex + 1; index < var7; ++index) {
                  var6.visit((Instruction)list.get(index));
                  if(var6.getParentFound() != null) {
                     break;
                  }
               }

               var6.init(dupStore, dupStore.objectref);

               while(index < var7) {
                  var6.visit((Instruction)list.get(index));
                  if(var6.getParentFound() != null) {
                     break;
                  }

                  ++index;
               }

               list.remove(dupStoreIndex--);
            }
         }
      }

   }
}
