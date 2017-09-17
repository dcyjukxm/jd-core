package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;
import jd.core.process.analyzer.instruction.bytecode.util.ByteCodeUtil;

public class GotoFactory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      short value = (short)((code[offset + 1] & 255) << 8 | code[offset + 2] & 255);
      if(!stack.isEmpty() && !list.isEmpty()) {
         generateTernaryOpStore(list, listForAnalyze, stack, code, offset, value);
      }

      list.add(new Goto(opcode, offset, lineNumber, value));
      return ByteCodeConstants.NO_OF_OPERANDS[opcode];
   }

   private static void generateTernaryOpStore(List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int value) {
      int i = list.size();

      while(i-- > 0) {
         Instruction previousInstruction = (Instruction)list.get(i);
         switch(previousInstruction.opcode) {
         case 260:
         case 261:
         case 262:
            int ternaryOp2ndValueOffset = search2ndValueOffset(code, offset, offset + value);
            Instruction value0 = (Instruction)stack.pop();
            TernaryOpStore tos = new TernaryOpStore(280, offset - 1, value0.lineNumber, value0, ternaryOp2ndValueOffset);
            list.add(tos);
            listForAnalyze.add(tos);
            return;
         }
      }

   }

   private static int search2ndValueOffset(byte[] code, int offset, int jumpOffset) {
      int result;
      for(result = offset; offset < jumpOffset; ++offset) {
         int opcode = code[offset] & 255;
         switch(opcode) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
         case 53:
         case 89:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 95:
         case 96:
         case 97:
         case 98:
         case 99:
         case 100:
         case 101:
         case 102:
         case 103:
         case 104:
         case 105:
         case 106:
         case 107:
         case 108:
         case 109:
         case 110:
         case 111:
         case 112:
         case 113:
         case 114:
         case 115:
         case 116:
         case 117:
         case 118:
         case 119:
         case 120:
         case 121:
         case 122:
         case 123:
         case 124:
         case 125:
         case 126:
         case 127:
         case 128:
         case 129:
         case 130:
         case 131:
         case 132:
         case 133:
         case 134:
         case 135:
         case 136:
         case 137:
         case 138:
         case 139:
         case 140:
         case 141:
         case 142:
         case 143:
         case 144:
         case 145:
         case 146:
         case 147:
         case 148:
         case 149:
         case 150:
         case 151:
         case 152:
         case 178:
         case 180:
         case 182:
         case 183:
         case 184:
         case 185:
         case 187:
         case 188:
         case 189:
         case 190:
         case 192:
         case 193:
         case 196:
         case 197:
         case 256:
         case 257:
         case 258:
         case 259:
         case 263:
         case 265:
         case 266:
         case 267:
         case 268:
         case 270:
         case 271:
         case 274:
         case 275:
         case 276:
         case 277:
         case 278:
         case 285:
            result = offset;
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         case 65:
         case 66:
         case 67:
         case 68:
         case 69:
         case 70:
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 153:
         case 154:
         case 155:
         case 156:
         case 157:
         case 158:
         case 159:
         case 160:
         case 161:
         case 162:
         case 163:
         case 164:
         case 165:
         case 166:
         case 167:
         case 168:
         case 169:
         case 170:
         case 171:
         case 172:
         case 173:
         case 174:
         case 175:
         case 176:
         case 177:
         case 179:
         case 181:
         case 186:
         case 191:
         case 194:
         case 195:
         case 198:
         case 199:
         case 200:
         case 201:
         case 202:
         case 203:
         case 204:
         case 205:
         case 206:
         case 207:
         case 208:
         case 209:
         case 210:
         case 211:
         case 212:
         case 213:
         case 214:
         case 215:
         case 216:
         case 217:
         case 218:
         case 219:
         case 220:
         case 221:
         case 222:
         case 223:
         case 224:
         case 225:
         case 226:
         case 227:
         case 228:
         case 229:
         case 230:
         case 231:
         case 232:
         case 233:
         case 234:
         case 235:
         case 236:
         case 237:
         case 238:
         case 239:
         case 240:
         case 241:
         case 242:
         case 243:
         case 244:
         case 245:
         case 246:
         case 247:
         case 248:
         case 249:
         case 250:
         case 251:
         case 252:
         case 253:
         case 254:
         case 255:
         case 260:
         case 261:
         case 262:
         case 264:
         case 269:
         case 272:
         case 273:
         case 279:
         case 280:
         case 281:
         case 282:
         case 283:
         case 284:
         }

         short nbOfOperands = ByteCodeConstants.NO_OF_OPERANDS[opcode];
         switch(nbOfOperands) {
         case -2:
            switch(opcode) {
            case 170:
               offset = ByteCodeUtil.NextTableSwitchOffset(code, offset);
               break;
            case 171:
               offset = ByteCodeUtil.NextLookupSwitchOffset(code, offset);
               break;
            case 196:
               offset = ByteCodeUtil.NextWideOffset(code, offset);
            }
         case -1:
            break;
         default:
            offset += nbOfOperands;
         }
      }

      return result;
   }
}
