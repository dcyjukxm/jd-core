package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.constant.Constant;
import jd.core.model.instruction.bytecode.instruction.LdcInstruction;

public class Ldc extends LdcInstruction {
   public Ldc(int opcode, int offset, int lineNumber, int index) {
      super(opcode, offset, lineNumber, index);
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      if(constants == null) {
         return null;
      } else {
         Constant c = constants.get(this.index);
         if(c == null) {
            return null;
         } else {
            switch(c.tag) {
            case 3:
               return "I";
            case 4:
               return "F";
            case 5:
            case 6:
            default:
               return null;
            case 7:
               return "Ljava/lang/Class;";
            case 8:
               return "Ljava/lang/String;";
            }
         }
      }
   }
}
