package jd.core.process.analyzer.classfile.reconstructor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.ConstInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.process.analyzer.util.ReconstructorUtil;

public class PostIncReconstructor {
   public static void Reconstruct(List<Instruction> list) {
      int length = list.size();

      label160:
      for(int dupStoreIndex = 0; dupStoreIndex < length; ++dupStoreIndex) {
         if(((Instruction)list.get(dupStoreIndex)).opcode == 264) {
            DupStore dupstore = (DupStore)list.get(dupStoreIndex);
            int xstorePutfieldPutstaticIndex = dupStoreIndex;

            BinaryOperatorInstruction boi;
            byte value;
            while(true) {
               ConstInstruction ci;
               do {
                  do {
                     do {
                        do {
                           do {
                              ++xstorePutfieldPutstaticIndex;
                              if(xstorePutfieldPutstaticIndex >= length) {
                                 continue label160;
                              }

                              Instruction i = (Instruction)list.get(xstorePutfieldPutstaticIndex);
                              boi = null;
                              switch(i.opcode) {
                              case 54:
                                 if(dupstore.objectref.opcode != 21 || ((IndexInstruction)i).index != ((IndexInstruction)dupstore.objectref).index) {
                                    break;
                                 }

                                 i = ((StoreInstruction)i).valueref;
                                 if(i.opcode == 275 || i.opcode == 276) {
                                    i = ((ConvertInstruction)i).value;
                                 }

                                 if(i.opcode == 267) {
                                    boi = (BinaryOperatorInstruction)i;
                                 }
                                 break;
                              case 58:
                                 if(dupstore.objectref.opcode != 25 || ((IndexInstruction)i).index != ((IndexInstruction)dupstore.objectref).index) {
                                    break;
                                 }

                                 i = ((StoreInstruction)i).valueref;
                                 if(i.opcode == 275 || i.opcode == 276) {
                                    i = ((ConvertInstruction)i).value;
                                 }

                                 if(i.opcode == 267) {
                                    boi = (BinaryOperatorInstruction)i;
                                 }
                                 break;
                              case 179:
                                 if(dupstore.objectref.opcode != 178 || ((IndexInstruction)i).index != ((IndexInstruction)dupstore.objectref).index) {
                                    break;
                                 }

                                 i = ((PutStatic)i).valueref;
                                 if(i.opcode == 275 || i.opcode == 276) {
                                    i = ((ConvertInstruction)i).value;
                                 }

                                 if(i.opcode == 267) {
                                    boi = (BinaryOperatorInstruction)i;
                                 }
                                 break;
                              case 181:
                                 if(dupstore.objectref.opcode != 180 || ((IndexInstruction)i).index != ((IndexInstruction)dupstore.objectref).index) {
                                    break;
                                 }

                                 i = ((PutField)i).valueref;
                                 if(i.opcode == 275 || i.opcode == 276) {
                                    i = ((ConvertInstruction)i).value;
                                 }

                                 if(i.opcode == 267) {
                                    boi = (BinaryOperatorInstruction)i;
                                 }
                                 break;
                              case 269:
                                 if(dupstore.objectref.opcode == 268 && ((IndexInstruction)i).index == ((IndexInstruction)dupstore.objectref).index) {
                                    i = ((StoreInstruction)i).valueref;
                                    if(i.opcode == 275 || i.opcode == 276) {
                                       i = ((ConvertInstruction)i).value;
                                    }

                                    if(i.opcode == 267) {
                                       boi = (BinaryOperatorInstruction)i;
                                    }
                                 }
                              }
                           } while(boi == null);
                        } while(boi.value1.opcode != 263);
                     } while(boi.value1.offset != dupstore.offset);
                  } while(boi.value2.opcode != 256 && boi.value2.opcode != 257 && boi.value2.opcode != 259 && boi.value2.opcode != 258);

                  ci = (ConstInstruction)boi.value2;
               } while(ci.value != 1);

               if(boi.operator.equals("+")) {
                  value = 1;
                  break;
               }

               if(boi.operator.equals("-")) {
                  value = -1;
                  break;
               }
            }

            IncInstruction inc = new IncInstruction(278, boi.offset, boi.lineNumber, dupstore.objectref, value);
            ReconstructorUtil.ReplaceDupLoad(list, xstorePutfieldPutstaticIndex + 1, dupstore, inc);
            list.remove(xstorePutfieldPutstaticIndex);
            list.remove(dupStoreIndex);
            --dupStoreIndex;
            length = list.size();
         }
      }

   }
}
