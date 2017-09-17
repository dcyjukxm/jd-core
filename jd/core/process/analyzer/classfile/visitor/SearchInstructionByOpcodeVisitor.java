package jd.core.process.analyzer.classfile.visitor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.GetField;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.LookupSwitch;
import jd.core.model.instruction.bytecode.instruction.MonitorEnter;
import jd.core.model.instruction.bytecode.instruction.MonitorExit;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.TableSwitch;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.instruction.fast.instruction.FastFor;
import jd.core.model.instruction.fast.instruction.FastForEach;
import jd.core.model.instruction.fast.instruction.FastInstruction;
import jd.core.model.instruction.fast.instruction.FastLabel;
import jd.core.model.instruction.fast.instruction.FastList;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.model.instruction.fast.instruction.FastSynchronized;
import jd.core.model.instruction.fast.instruction.FastTest2Lists;
import jd.core.model.instruction.fast.instruction.FastTestList;
import jd.core.model.instruction.fast.instruction.FastTry;

public class SearchInstructionByOpcodeVisitor {
   public static Instruction visit(Instruction instruction, int opcode) throws RuntimeException {
      if(instruction == null) {
         throw new RuntimeException("Null instruction");
      } else if(instruction.opcode == opcode) {
         return instruction;
      } else {
         Instruction tmp;
         int i;
         Instruction var16;
         List var18;
         int var23;
         switch(instruction.opcode) {
         case 0:
         case 1:
         case 16:
         case 17:
         case 18:
         case 20:
         case 21:
         case 25:
         case 132:
         case 167:
         case 168:
         case 169:
         case 177:
         case 178:
         case 187:
         case 256:
         case 257:
         case 258:
         case 259:
         case 263:
         case 268:
         case 270:
         case 271:
         case 279:
         case 285:
            break;
         case 54:
         case 58:
         case 269:
            return visit(((StoreInstruction)instruction).valueref, opcode);
         case 83:
         case 272:
            return visit(((ArrayStoreInstruction)instruction).arrayref, opcode);
         case 87:
            return visit(((Pop)instruction).objectref, opcode);
         case 170:
            return visit(((TableSwitch)instruction).key, opcode);
         case 171:
            return visit(((LookupSwitch)instruction).key, opcode);
         case 179:
            return visit(((PutStatic)instruction).valueref, opcode);
         case 180:
            return visit(((GetField)instruction).objectref, opcode);
         case 181:
            PutField var26 = (PutField)instruction;
            tmp = visit(var26.objectref, opcode);
            if(tmp != null) {
               return tmp;
            }

            return visit(var26.valueref, opcode);
         case 182:
         case 183:
         case 185:
            Instruction var25 = visit(((InvokeNoStaticInstruction)instruction).objectref, opcode);
            if(var25 != null) {
               return var25;
            }
         case 184:
         case 274:
            var18 = ((InvokeInstruction)instruction).args;

            for(var23 = var18.size() - 1; var23 >= 0; --var23) {
               var16 = visit((Instruction)var18.get(var23), opcode);
               if(var16 != null) {
                  return var16;
               }
            }

            return null;
         case 188:
            return visit(((NewArray)instruction).dimension, opcode);
         case 189:
            return visit(((ANewArray)instruction).dimension, opcode);
         case 190:
            return visit(((ArrayLength)instruction).arrayref, opcode);
         case 191:
            return visit(((AThrow)instruction).value, opcode);
         case 192:
            return visit(((CheckCast)instruction).objectref, opcode);
         case 193:
            return visit(((InstanceOf)instruction).objectref, opcode);
         case 194:
            return visit(((MonitorEnter)instruction).objectref, opcode);
         case 195:
            return visit(((MonitorExit)instruction).objectref, opcode);
         case 197:
            Instruction[] var24 = ((MultiANewArray)instruction).dimensions;

            for(var23 = var24.length - 1; var23 >= 0; --var23) {
               var16 = visit(var24[var23], opcode);
               if(var16 != null) {
                  return var16;
               }
            }

            return null;
         case 260:
         case 262:
            return visit(((IfInstruction)instruction).value, opcode);
         case 261:
            IfCmp var22 = (IfCmp)instruction;
            tmp = visit(var22.value1, opcode);
            if(tmp != null) {
               return tmp;
            }

            return visit(var22.value2, opcode);
         case 264:
            return visit(((DupStore)instruction).objectref, opcode);
         case 265:
         case 267:
            BinaryOperatorInstruction var21 = (BinaryOperatorInstruction)instruction;
            tmp = visit(var21.value1, opcode);
            if(tmp != null) {
               return tmp;
            }

            return visit(var21.value2, opcode);
         case 266:
            return visit(((UnaryOperatorInstruction)instruction).value, opcode);
         case 273:
            return visit(((ReturnInstruction)instruction).valueref, opcode);
         case 275:
         case 276:
            return visit(((ConvertInstruction)instruction).value, opcode);
         case 277:
         case 278:
            return visit(((IncInstruction)instruction).value, opcode);
         case 280:
            return visit(((TernaryOpStore)instruction).objectref, opcode);
         case 281:
            TernaryOperator var20 = (TernaryOperator)instruction;
            tmp = visit(var20.value1, opcode);
            if(tmp != null) {
               return tmp;
            }

            return visit(var20.value2, opcode);
         case 282:
         case 283:
            InitArrayInstruction var19 = (InitArrayInstruction)instruction;
            tmp = visit(var19.newArray, opcode);
            if(tmp != null) {
               return tmp;
            }

            if(var19.values != null) {
               return visit(var19.values, opcode);
            }
            break;
         case 284:
            var18 = ((ComplexConditionalBranchInstruction)instruction).instructions;

            for(var23 = var18.size() - 1; var23 >= 0; --var23) {
               var16 = visit((Instruction)var18.get(var23), opcode);
               if(var16 != null) {
                  return var16;
               }
            }

            return null;
         case 304:
            FastFor var15 = (FastFor)instruction;
            if(var15.init != null) {
               tmp = visit(var15.init, opcode);
               if(tmp != null) {
                  return tmp;
               }
            }

            if(var15.inc != null) {
               tmp = visit(var15.inc, opcode);
               if(tmp != null) {
                  return tmp;
               }
            }
         case 301:
         case 302:
         case 306:
            FastTestList var17 = (FastTestList)instruction;
            if(var17.test != null) {
               tmp = visit(var17.test, opcode);
               if(tmp != null) {
                  return tmp;
               }
            }
         case 303:
            var18 = ((FastList)instruction).instructions;
            if(var18 != null) {
               return visit(var18, opcode);
            }
            break;
         case 305:
            FastForEach var13 = (FastForEach)instruction;
            tmp = visit(var13.variable, opcode);
            if(tmp != null) {
               return tmp;
            }

            tmp = visit(var13.values, opcode);
            if(tmp != null) {
               return tmp;
            }

            return visit(var13.instructions, opcode);
         case 307:
            FastTest2Lists var12 = (FastTest2Lists)instruction;
            tmp = visit(var12.test, opcode);
            if(tmp != null) {
               return tmp;
            }

            tmp = visit(var12.instructions, opcode);
            if(tmp != null) {
               return tmp;
            }

            return visit(var12.instructions2, opcode);
         case 308:
         case 309:
         case 310:
         case 311:
         case 312:
         case 313:
            FastInstruction var11 = (FastInstruction)instruction;
            if(var11.instruction != null) {
               return visit(var11.instruction, opcode);
            }
            break;
         case 314:
         case 315:
         case 316:
            FastSwitch var10 = (FastSwitch)instruction;
            tmp = visit(var10.test, opcode);
            if(tmp != null) {
               return tmp;
            }

            FastSwitch.Pair[] var14 = var10.pairs;

            for(i = var14.length - 1; i >= 0; --i) {
               List instructions = var14[i].getInstructions();
               if(instructions != null) {
                  tmp = visit(instructions, opcode);
                  if(tmp != null) {
                     return tmp;
                  }
               }
            }

            return null;
         case 317:
            FastDeclaration var9 = (FastDeclaration)instruction;
            if(var9.instruction != null) {
               return visit(var9.instruction, opcode);
            }
            break;
         case 318:
            FastTry var8 = (FastTry)instruction;
            tmp = visit(var8.instructions, opcode);
            if(tmp != null) {
               return tmp;
            }

            List pairs = var8.catches;

            for(i = pairs.size() - 1; i >= 0; --i) {
               tmp = visit(((FastTry.FastCatch)pairs.get(i)).instructions, opcode);
               if(tmp != null) {
                  return tmp;
               }
            }

            if(var8.finallyInstructions != null) {
               return visit(var8.finallyInstructions, opcode);
            }
            break;
         case 319:
            FastSynchronized var7 = (FastSynchronized)instruction;
            tmp = visit(var7.monitor, opcode);
            if(tmp != null) {
               return tmp;
            }

            return visit(var7.instructions, opcode);
         case 320:
            FastLabel fla = (FastLabel)instruction;
            if(fla.instruction != null) {
               return visit(fla.instruction, opcode);
            }
            break;
         default:
            System.err.println("Can not search instruction in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
         }

         return null;
      }
   }

   private static Instruction visit(List<Instruction> instructions, int opcode) throws RuntimeException {
      for(int i = instructions.size() - 1; i >= 0; --i) {
         Instruction instruction = visit((Instruction)instructions.get(i), opcode);
         if(instruction != null) {
            return instruction;
         }
      }

      return null;
   }
}
