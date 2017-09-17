package jd.core.process.analyzer.instruction.fast;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.classfile.visitor.ReplaceDupLoadVisitor;
import jd.core.process.analyzer.instruction.fast.visitor.CountDupLoadVisitor;

public class SingleDupLoadAnalyzer {
   public static void Cleanup(List<Instruction> list) {
      CountDupLoadVisitor countDupLoadVisitor = new CountDupLoadVisitor();
      ReplaceDupLoadVisitor replaceDupLoadVisitor = new ReplaceDupLoadVisitor();
      int length = list.size();

      for(int dupStoreIndex = 0; dupStoreIndex < length; ++dupStoreIndex) {
         if(((Instruction)list.get(dupStoreIndex)).opcode == 264) {
            DupStore dupStore = (DupStore)list.get(dupStoreIndex);
            countDupLoadVisitor.init(dupStore);

            int counter;
            for(counter = dupStoreIndex + 1; counter < length; ++counter) {
               countDupLoadVisitor.visit((Instruction)list.get(counter));
               if(countDupLoadVisitor.getCounter() >= 2) {
                  break;
               }
            }

            counter = countDupLoadVisitor.getCounter();
            if(counter < 2) {
               if(counter > 0) {
                  replaceDupLoadVisitor.init(dupStore, dupStore.objectref);

                  for(int index = dupStoreIndex + 1; index < length; ++index) {
                     replaceDupLoadVisitor.visit((Instruction)list.get(index));
                  }
               }

               list.remove(dupStoreIndex--);
               --length;
            }
         }
      }

   }
}
