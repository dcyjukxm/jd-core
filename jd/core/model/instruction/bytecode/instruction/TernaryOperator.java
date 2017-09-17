package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class TernaryOperator extends Instruction {
   public Instruction test;
   public Instruction value1;
   public Instruction value2;

   public TernaryOperator(int opcode, int offset, int lineNumber, Instruction test, Instruction value1, Instruction value2) {
      super(opcode, offset, lineNumber);
      this.test = test;
      this.value1 = value1;
      this.value2 = value2;
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      return this.value1 != null?this.value1.getReturnedSignature(constants, localVariables):this.value2.getReturnedSignature(constants, localVariables);
   }

   public int getPriority() {
      return 13;
   }
}
