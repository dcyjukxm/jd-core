package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;

public class IStore extends StoreInstruction {
   public IStore(int opcode, int offset, int lineNumber, int index, Instruction objectref) {
      super(opcode, offset, lineNumber, index, (String)null, objectref);
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      return this.valueref.getReturnedSignature(constants, localVariables);
   }
}
