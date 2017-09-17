package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class MonitorEnter extends Instruction {
   public Instruction objectref;

   public MonitorEnter(int opcode, int offset, int lineNumber, Instruction objectref) {
      super(opcode, offset, lineNumber);
      this.objectref = objectref;
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      return null;
   }
}
