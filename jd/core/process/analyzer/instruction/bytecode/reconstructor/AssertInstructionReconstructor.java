package jd.core.process.analyzer.instruction.bytecode.reconstructor;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;

public class AssertInstructionReconstructor {
   public static void Reconstruct(ClassFile classFile, List<Instruction> list) {
      int index = list.size();
      if(index-- != 0) {
         while(index-- > 1) {
            Instruction instruction = (Instruction)list.get(index);
            if(instruction.opcode == 191) {
               AThrow athrow = (AThrow)instruction;
               if(athrow.value.opcode == 274) {
                  instruction = (Instruction)list.get(index - 1);
                  if(instruction.opcode == 284) {
                     ComplexConditionalBranchInstruction cbl = (ComplexConditionalBranchInstruction)instruction;
                     int jumpOffset = cbl.GetJumpOffset();
                     int lastOffset = ((Instruction)list.get(index + 1)).offset;
                     if(athrow.offset < jumpOffset && jumpOffset <= lastOffset && cbl.cmp == 2 && cbl.instructions.size() >= 1) {
                        instruction = (Instruction)cbl.instructions.get(0);
                        if(instruction.opcode == 260) {
                           IfInstruction if1 = (IfInstruction)instruction;
                           if(if1.cmp == 7 && if1.value.opcode == 178) {
                              GetStatic gs = (GetStatic)if1.value;
                              ConstantPool constants = classFile.getConstantPool();
                              ConstantFieldref cfr = constants.getConstantFieldref(gs.index);
                              if(cfr.class_index == classFile.getThisClassIndex()) {
                                 ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
                                 String fieldName = constants.getConstantUtf8(cnat.name_index);
                                 if(fieldName.equals("$assertionsDisabled")) {
                                    InvokeNew in = (InvokeNew)athrow.value;
                                    ConstantMethodref cmr = constants.getConstantMethodref(in.index);
                                    String className = constants.getConstantClassName(cmr.class_index);
                                    if(className.equals("java/lang/AssertionError")) {
                                       cbl.instructions.remove(0);
                                       Instruction msg = in.args.size() == 0?null:(Instruction)in.args.get(0);
                                       list.remove(index--);
                                       list.set(index, new AssertInstruction(286, athrow.offset, cbl.lineNumber, cbl, msg));
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }
}
