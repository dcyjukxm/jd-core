package jd.core.model.instruction.bytecode.instruction;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.constant.Constant;
import jd.core.model.classfile.constant.ConstantClass;
import jd.core.model.classfile.constant.ConstantUtf8;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.util.SignatureUtil;

public class CheckCast extends IndexInstruction {
   public Instruction objectref;

   public CheckCast(int opcode, int offset, int lineNumber, int index, Instruction objectref) {
      super(opcode, offset, lineNumber, index);
      this.objectref = objectref;
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      if(constants == null) {
         return null;
      } else {
         Constant c = constants.get(this.index);
         if(c.tag == 1) {
            ConstantUtf8 cc1 = (ConstantUtf8)c;
            return cc1.bytes;
         } else {
            ConstantClass cc = (ConstantClass)c;
            String signature = constants.getConstantUtf8(cc.name_index);
            if(signature.charAt(0) != 91) {
               signature = SignatureUtil.CreateTypeName(signature);
            }

            return signature;
         }
      }
   }

   public int getPriority() {
      return 2;
   }
}
