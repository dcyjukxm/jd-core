package jd.core.process.analyzer.classfile.reconstructor;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.bytecode.instruction.Invokespecial;
import jd.core.model.instruction.bytecode.instruction.New;
import jd.core.process.analyzer.classfile.reconstructor.NewInstructionReconstructorBase;

public class SimpleNewInstructionReconstructor extends NewInstructionReconstructorBase {
   public static void Reconstruct(ClassFile classFile, Method method, List<Instruction> list) {
      for(int invokespecialIndex = 0; invokespecialIndex < list.size(); ++invokespecialIndex) {
         if(((Instruction)list.get(invokespecialIndex)).opcode == 183) {
            Invokespecial is = (Invokespecial)list.get(invokespecialIndex);
            if(is.objectref.opcode == 187) {
               New nw = (New)is.objectref;
               InvokeNew invokeNew = new InvokeNew(274, is.offset, nw.lineNumber, is.index, is.args);
               list.set(invokespecialIndex, invokeNew);
               InitAnonymousClassConstructorParameterName(classFile, method, invokeNew);
            }
         }
      }

   }
}
