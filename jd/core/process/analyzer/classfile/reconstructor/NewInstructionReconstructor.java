package jd.core.process.analyzer.classfile.reconstructor;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.bytecode.instruction.Invokespecial;
import jd.core.model.instruction.bytecode.instruction.New;
import jd.core.process.analyzer.classfile.reconstructor.NewInstructionReconstructorBase;
import jd.core.process.analyzer.util.ReconstructorUtil;

public class NewInstructionReconstructor extends NewInstructionReconstructorBase {
   public static void Reconstruct(ClassFile classFile, Method method, List<Instruction> list) {
      for(int dupStoreIndex = 0; dupStoreIndex < list.size(); ++dupStoreIndex) {
         if(((Instruction)list.get(dupStoreIndex)).opcode == 264) {
            DupStore ds = (DupStore)list.get(dupStoreIndex);
            if(ds.objectref.opcode == 187) {
               int invokespecialIndex = dupStoreIndex;
               int length = list.size();

               while(true) {
                  ++invokespecialIndex;
                  if(invokespecialIndex >= length) {
                     break;
                  }

                  Instruction instruction = (Instruction)list.get(invokespecialIndex);
                  if(instruction.opcode == 183) {
                     Invokespecial is = (Invokespecial)instruction;
                     if(is.objectref.opcode == 263) {
                        DupLoad dl = (DupLoad)is.objectref;
                        if(dl.offset == ds.offset) {
                           ConstantPool constants = classFile.getConstantPool();
                           ConstantMethodref cmr = constants.getConstantMethodref(is.index);
                           ConstantNameAndType cnat = constants.getConstantNameAndType(cmr.name_and_type_index);
                           if(cnat.name_index == constants.instanceConstructorIndex) {
                              New nw = (New)ds.objectref;
                              InvokeNew invokeNew = new InvokeNew(274, is.offset, nw.lineNumber, is.index, is.args);
                              Instruction parentFound = ReconstructorUtil.ReplaceDupLoad(list, invokespecialIndex + 1, ds, invokeNew);
                              list.remove(invokespecialIndex);
                              if(parentFound == null) {
                                 list.set(dupStoreIndex, invokeNew);
                              } else {
                                 list.remove(dupStoreIndex--);
                              }

                              InitAnonymousClassConstructorParameterName(classFile, method, invokeNew);
                              break;
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
