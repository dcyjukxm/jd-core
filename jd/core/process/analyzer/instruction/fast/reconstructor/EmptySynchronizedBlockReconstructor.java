package jd.core.process.analyzer.instruction.fast.reconstructor;

import java.util.ArrayList;
import java.util.List;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.MonitorEnter;
import jd.core.model.instruction.fast.instruction.FastSynchronized;

public class EmptySynchronizedBlockReconstructor {
   public static void Reconstruct(LocalVariables localVariables, List<Instruction> list) {
      int index = list.size();

      while(true) {
         Instruction monitorExit;
         Instruction instruction;
         DupStore dupStore;
         while(true) {
            MonitorEnter me;
            do {
               do {
                  do {
                     if(index-- <= 2) {
                        return;
                     }

                     monitorExit = (Instruction)list.get(index);
                  } while(monitorExit.opcode != 195);

                  instruction = (Instruction)list.get(index - 1);
               } while(instruction.opcode != 194);

               me = (MonitorEnter)instruction;
            } while(me.objectref.opcode != 263);

            instruction = (Instruction)list.get(index - 2);
            if(instruction.opcode == 264) {
               dupStore = (DupStore)instruction;
               break;
            }

            if(instruction.opcode == 58 && index > 2) {
               AStore fastSynchronized = (AStore)instruction;
               instruction = (Instruction)list.get(index - 3);
               if(instruction.opcode == 264) {
                  dupStore = (DupStore)instruction;
                  localVariables.removeLocalVariableWithIndexAndOffset(fastSynchronized.index, fastSynchronized.offset);
                  list.remove(index--);
                  break;
               }
            }
         }

         FastSynchronized var8 = new FastSynchronized(319, monitorExit.offset, instruction.lineNumber, 1, new ArrayList());
         var8.monitor = dupStore.objectref;
         list.remove(index--);
         list.remove(index--);
         list.set(index, var8);
      }
   }
}
