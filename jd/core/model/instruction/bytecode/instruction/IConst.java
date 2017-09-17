package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.ConstInstruction;

public class IConst extends ConstInstruction {
   public String signature;

   public IConst(int opcode, int offset, int lineNumber, int value) {
      super(opcode, offset, lineNumber, value);
      if(value < 0) {
         if(value >= -128) {
            this.signature = "B";
         } else if(value >= -32768) {
            this.signature = "S";
         } else {
            this.signature = "I";
         }
      } else if(value <= 1) {
         this.signature = "X";
      } else if(value <= 127) {
         this.signature = "Y";
      } else if(value <= '\uffff') {
         this.signature = "C";
      } else if(value <= 32767) {
         this.signature = "S";
      } else {
         this.signature = "I";
      }

   }

   public String getSignature() {
      return this.signature;
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      return this.signature;
   }

   public void setReturnedSignature(String signature) {
      this.signature = signature;
   }
}
