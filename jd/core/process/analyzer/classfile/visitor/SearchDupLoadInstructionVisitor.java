package jd.core.process.analyzer.classfile.visitor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
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

public class SearchDupLoadInstructionVisitor {
   public static DupLoad visit(Instruction instruction, DupStore dupStore) {
      DupLoad dupLoad;
      DupLoad dupLoad1;
      List var5;
      int var9;
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
      case 268:
      case 270:
      case 279:
      case 285:
         break;
      case 54:
      case 58:
      case 269:
         return visit(((StoreInstruction)instruction).valueref, dupStore);
      case 83:
      case 272:
         ArrayStoreInstruction var14 = (ArrayStoreInstruction)instruction;
         dupLoad = visit(var14.arrayref, dupStore);
         if(dupLoad != null) {
            return dupLoad;
         }

         dupLoad = visit(var14.indexref, dupStore);
         if(dupLoad != null) {
            return dupLoad;
         }

         return visit(var14.valueref, dupStore);
      case 87:
         return visit(((Pop)instruction).objectref, dupStore);
      case 170:
         return visit(((TableSwitch)instruction).key, dupStore);
      case 171:
         return visit(((LookupSwitch)instruction).key, dupStore);
      case 179:
         return visit(((PutStatic)instruction).valueref, dupStore);
      case 180:
         return visit(((GetField)instruction).objectref, dupStore);
      case 181:
         PutField var13 = (PutField)instruction;
         dupLoad = visit(var13.objectref, dupStore);
         if(dupLoad != null) {
            return dupLoad;
         }

         return visit(var13.valueref, dupStore);
      case 182:
      case 183:
      case 185:
         DupLoad var12 = visit(((InvokeNoStaticInstruction)instruction).objectref, dupStore);
         if(var12 != null) {
            return var12;
         }
      case 184:
      case 274:
         var5 = ((InvokeInstruction)instruction).args;

         for(var9 = var5.size() - 1; var9 >= 0; --var9) {
            dupLoad1 = visit((Instruction)var5.get(var9), dupStore);
            if(dupLoad1 != null) {
               return dupLoad1;
            }
         }

         return null;
      case 188:
         return visit(((NewArray)instruction).dimension, dupStore);
      case 189:
         return visit(((ANewArray)instruction).dimension, dupStore);
      case 190:
         return visit(((ArrayLength)instruction).arrayref, dupStore);
      case 191:
         return visit(((AThrow)instruction).value, dupStore);
      case 192:
         return visit(((CheckCast)instruction).objectref, dupStore);
      case 193:
         return visit(((InstanceOf)instruction).objectref, dupStore);
      case 194:
         return visit(((MonitorEnter)instruction).objectref, dupStore);
      case 195:
         return visit(((MonitorExit)instruction).objectref, dupStore);
      case 197:
         Instruction[] var11 = ((MultiANewArray)instruction).dimensions;

         for(var9 = var11.length - 1; var9 >= 0; --var9) {
            dupLoad1 = visit(var11[var9], dupStore);
            if(dupLoad1 != null) {
               return dupLoad1;
            }
         }

         return null;
      case 260:
      case 262:
         return visit(((IfInstruction)instruction).value, dupStore);
      case 261:
         IfCmp var10 = (IfCmp)instruction;
         dupLoad = visit(var10.value1, dupStore);
         if(dupLoad != null) {
            return dupLoad;
         }

         return visit(var10.value2, dupStore);
      case 263:
         if(((DupLoad)instruction).dupStore == dupStore) {
            return (DupLoad)instruction;
         }
         break;
      case 264:
         return visit(((DupStore)instruction).objectref, dupStore);
      case 265:
      case 267:
         BinaryOperatorInstruction var8 = (BinaryOperatorInstruction)instruction;
         dupLoad = visit(var8.value1, dupStore);
         if(dupLoad != null) {
            return dupLoad;
         }

         return visit(var8.value2, dupStore);
      case 266:
         return visit(((UnaryOperatorInstruction)instruction).value, dupStore);
      case 271:
         ArrayLoadInstruction var7 = (ArrayLoadInstruction)instruction;
         dupLoad = visit(var7.arrayref, dupStore);
         if(dupLoad != null) {
            return dupLoad;
         }

         return visit(var7.indexref, dupStore);
      case 273:
         return visit(((ReturnInstruction)instruction).valueref, dupStore);
      case 275:
      case 276:
         return visit(((ConvertInstruction)instruction).value, dupStore);
      case 277:
      case 278:
         return visit(((IncInstruction)instruction).value, dupStore);
      case 280:
         return visit(((TernaryOpStore)instruction).objectref, dupStore);
      case 282:
      case 283:
         InitArrayInstruction var6 = (InitArrayInstruction)instruction;
         dupLoad = visit(var6.newArray, dupStore);
         if(dupLoad != null) {
            return dupLoad;
         }

         if(var6.values != null) {
            return visit(var6.values, dupStore);
         }
         break;
      case 284:
         var5 = ((ComplexConditionalBranchInstruction)instruction).instructions;

         for(var9 = var5.size() - 1; var9 >= 0; --var9) {
            dupLoad1 = visit((Instruction)var5.get(var9), dupStore);
            if(dupLoad1 != null) {
               return dupLoad1;
            }
         }

         return null;
      case 286:
         AssertInstruction iai = (AssertInstruction)instruction;
         dupLoad = visit(iai.test, dupStore);
         if(dupLoad != null) {
            return dupLoad;
         }

         if(iai.msg == null) {
            return null;
         }

         return visit(iai.msg, dupStore);
      default:
         System.err.println("Can not search DupLoad instruction in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
      }

      return null;
   }

   private static DupLoad visit(List<Instruction> instructions, DupStore dupStore) {
      for(int i = instructions.size() - 1; i >= 0; --i) {
         DupLoad dupLoad = visit((Instruction)instructions.get(i), dupStore);
         if(dupLoad != null) {
            return dupLoad;
         }
      }

      return null;
   }
}
