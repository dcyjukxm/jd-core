package jd.core.process.analyzer.instruction.bytecode.util;

import jd.core.model.instruction.bytecode.ByteCodeConstants;

public class ByteCodeUtil {
   public static int NextTableSwitchOffset(byte[] code, int index) {
      int i = index + 4 & '￼';
      i += 4;
      int low = (code[i] & 255) << 24 | (code[i + 1] & 255) << 16 | (code[i + 2] & 255) << 8 | code[i + 3] & 255;
      i += 4;
      int high = (code[i] & 255) << 24 | (code[i + 1] & 255) << 16 | (code[i + 2] & 255) << 8 | code[i + 3] & 255;
      i += 4;
      i += 4 * (high - low + 1);
      return i - 1;
   }

   public static int NextLookupSwitchOffset(byte[] code, int index) {
      int i = index + 4 & '￼';
      i += 4;
      int npairs = (code[i] & 255) << 24 | (code[i + 1] & 255) << 16 | (code[i + 2] & 255) << 8 | code[i + 3] & 255;
      i += 4;
      i += 8 * npairs;
      return i - 1;
   }

   public static int NextWideOffset(byte[] code, int index) {
      int opcode = code[index + 1] & 255;
      return index + (opcode == 132?5:3);
   }

   public static int NextInstructionOffset(byte[] code, int index) {
      int opcode = code[index] & 255;
      switch(opcode) {
      case 170:
         return NextTableSwitchOffset(code, index);
      case 171:
         return NextLookupSwitchOffset(code, index);
      case 196:
         return NextWideOffset(code, index);
      default:
         return index + 1 + ByteCodeConstants.NO_OF_OPERANDS[opcode];
      }
   }

   public static boolean JumpTo(byte[] code, int offset, int targetOffset) {
      if(offset != -1) {
         int codeLength = code.length;

         for(int i = 0; i < 10; ++i) {
            if(offset == targetOffset) {
               return true;
            }

            if(offset >= codeLength) {
               break;
            }

            int opcode = code[offset] & 255;
            if(opcode == 167) {
               offset += (short)((code[offset + 1] & 255) << 8 | code[offset + 2] & 255);
            } else {
               if(opcode != 200) {
                  break;
               }

               offset += (code[offset + 1] & 255) << 24 | (code[offset + 2] & 255) << 16 | (code[offset + 3] & 255) << 8 | code[offset + 4] & 255;
            }
         }
      }

      return false;
   }
}
