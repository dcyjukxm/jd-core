package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class BinaryOperatorInstruction extends Instruction {
   private int priority;
   public String signature;
   public String operator;
   public Instruction value1;
   public Instruction value2;

   public BinaryOperatorInstruction(int opcode, int offset, int lineNumber, int priority, String signature, String operator, Instruction value1, Instruction value2) {
      super(opcode, offset, lineNumber);
      this.priority = priority;
      this.signature = signature;
      this.operator = operator;
      this.value1 = value1;
      this.value2 = value2;
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      return this.signature;
   }

   public int getPriority() {
      return this.priority;
   }
}
