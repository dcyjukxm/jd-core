package jd.core.process.analyzer.classfile.visitor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
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
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;

public class SearchInstructionByOffsetVisitor {
   public static Instruction visit(Instruction instruction, int offset) {
      if(instruction.offset == offset) {
         return instruction;
      } else {
         int i;
         List var4;
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
            return visit(((StoreInstruction)instruction).valueref, offset);
         case 83:
         case 272:
            return visit(((ArrayStoreInstruction)instruction).arrayref, offset);
         case 87:
            return visit(((Pop)instruction).objectref, offset);
         case 170:
            return visit(((TableSwitch)instruction).key, offset);
         case 171:
            return visit(((LookupSwitch)instruction).key, offset);
         case 179:
            return visit(((PutStatic)instruction).valueref, offset);
         case 180:
            return visit(((GetField)instruction).objectref, offset);
         case 181:
            PutField var10 = (PutField)instruction;
            instruction = visit(var10.objectref, offset);
            if(instruction != null) {
               return instruction;
            }

            return visit(var10.valueref, offset);
         case 182:
         case 183:
         case 185:
            Instruction var9 = visit(((InvokeNoStaticInstruction)instruction).objectref, offset);
            if(var9 != null) {
               return var9;
            }
         case 184:
         case 274:
            var4 = ((InvokeInstruction)instruction).args;

            for(i = var4.size() - 1; i >= 0; --i) {
               instruction = visit((Instruction)var4.get(i), offset);
               if(instruction != null) {
                  return instruction;
               }
            }

            return null;
         case 188:
            return visit(((NewArray)instruction).dimension, offset);
         case 189:
            return visit(((ANewArray)instruction).dimension, offset);
         case 190:
            return visit(((ArrayLength)instruction).arrayref, offset);
         case 191:
            return visit(((AThrow)instruction).value, offset);
         case 192:
            return visit(((CheckCast)instruction).objectref, offset);
         case 193:
            return visit(((InstanceOf)instruction).objectref, offset);
         case 194:
            return visit(((MonitorEnter)instruction).objectref, offset);
         case 195:
            return visit(((MonitorExit)instruction).objectref, offset);
         case 197:
            Instruction[] var8 = ((MultiANewArray)instruction).dimensions;

            for(i = var8.length - 1; i >= 0; --i) {
               instruction = visit(var8[i], offset);
               if(instruction != null) {
                  return instruction;
               }
            }

            return null;
         case 260:
         case 262:
            return visit(((IfInstruction)instruction).value, offset);
         case 261:
            IfCmp var7 = (IfCmp)instruction;
            instruction = visit(var7.value1, offset);
            if(instruction != null) {
               return instruction;
            }

            return visit(var7.value2, offset);
         case 264:
            return visit(((DupStore)instruction).objectref, offset);
         case 265:
         case 267:
            BinaryOperatorInstruction var6 = (BinaryOperatorInstruction)instruction;
            instruction = visit(var6.value1, offset);
            if(instruction != null) {
               return instruction;
            }

            return visit(var6.value2, offset);
         case 266:
            return visit(((UnaryOperatorInstruction)instruction).value, offset);
         case 273:
            return visit(((ReturnInstruction)instruction).valueref, offset);
         case 275:
         case 276:
            return visit(((ConvertInstruction)instruction).value, offset);
         case 277:
         case 278:
            return visit(((IncInstruction)instruction).value, offset);
         case 280:
            return visit(((TernaryOpStore)instruction).objectref, offset);
         case 282:
         case 283:
            InitArrayInstruction var5 = (InitArrayInstruction)instruction;
            instruction = visit(var5.newArray, offset);
            if(instruction != null) {
               return instruction;
            }

            if(var5.values != null) {
               return visit(var5.values, offset);
            }
            break;
         case 284:
            var4 = ((ComplexConditionalBranchInstruction)instruction).instructions;

            for(i = var4.size() - 1; i >= 0; --i) {
               instruction = visit((Instruction)var4.get(i), offset);
               if(instruction != null) {
                  return instruction;
               }
            }

            return null;
         case 286:
            AssertInstruction iai = (AssertInstruction)instruction;
            instruction = visit(iai.test, offset);
            if(instruction != null) {
               return instruction;
            }

            if(iai.msg == null) {
               return null;
            }

            return visit(iai.msg, offset);
         default:
            System.err.println("Can not search instruction in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
         }

         return null;
      }
   }

   private static Instruction visit(List<Instruction> instructions, int offset) {
      for(int i = instructions.size() - 1; i >= 0; --i) {
         Instruction instruction = visit((Instruction)instructions.get(i), offset);
         if(instruction != null) {
            return instruction;
         }
      }

      return null;
   }
}
