package jd.core.process.analyzer.instruction.fast.reconstructor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.classfile.visitor.ReplaceDupLoadVisitor;

public class RemoveDupConstantsAttributes {
   public static void Reconstruct(List<Instruction> list) {
      for(int dupStoreIndex = 0; dupStoreIndex < list.size(); ++dupStoreIndex) {
         if(((Instruction)list.get(dupStoreIndex)).opcode == 264) {
            DupStore dupstore = (DupStore)list.get(dupStoreIndex);
            int opcode = dupstore.objectref.opcode;
            if(opcode == 16 || opcode == 17) {
               Instruction i = dupstore.objectref;
               int dupLoadIndex = dupStoreIndex + 1;
               ReplaceDupLoadVisitor visitor = new ReplaceDupLoadVisitor(dupstore, i);

               int length;
               for(length = list.size(); dupLoadIndex < length; ++dupLoadIndex) {
                  visitor.visit((Instruction)list.get(dupLoadIndex));
                  if(visitor.getParentFound() != null) {
                     break;
                  }
               }

               visitor.init(dupstore, i);

               while(dupLoadIndex < length) {
                  visitor.visit((Instruction)list.get(dupLoadIndex));
                  if(visitor.getParentFound() != null) {
                     break;
                  }

                  ++dupLoadIndex;
               }

               list.remove(dupStoreIndex--);
            }
         }
      }

   }
}
