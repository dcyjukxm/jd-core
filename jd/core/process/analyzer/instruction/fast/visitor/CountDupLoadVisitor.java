package jd.core.process.analyzer.instruction.fast.visitor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
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
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
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

public class CountDupLoadVisitor {
   private DupStore dupStore;
   private int counter;

   public CountDupLoadVisitor() {
      this.init((DupStore)null);
   }

   public void init(DupStore dupStore) {
      this.dupStore = dupStore;
      this.counter = 0;
   }

   public void visit(Instruction instruction) {
      int i;
      int var11;
      List var17;
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
         this.visit(((StoreInstruction)instruction).valueref);
         break;
      case 83:
      case 272:
         ArrayStoreInstruction var26 = (ArrayStoreInstruction)instruction;
         this.visit(var26.arrayref);
         this.visit(var26.indexref);
         this.visit(var26.valueref);
         break;
      case 87:
         this.visit(((Pop)instruction).objectref);
         break;
      case 170:
         this.visit(((TableSwitch)instruction).key);
         break;
      case 171:
         this.visit(((LookupSwitch)instruction).key);
         break;
      case 179:
         this.visit(((PutStatic)instruction).valueref);
         break;
      case 180:
         this.visit(((GetField)instruction).objectref);
         break;
      case 181:
         PutField var25 = (PutField)instruction;
         this.visit(var25.objectref);
         this.visit(var25.valueref);
         break;
      case 182:
      case 183:
      case 185:
         this.visit(((InvokeNoStaticInstruction)instruction).objectref);
      case 184:
         var17 = ((InvokeInstruction)instruction).args;

         for(var11 = var17.size() - 1; var11 >= 0; --var11) {
            this.visit((Instruction)var17.get(var11));
         }

         return;
      case 188:
         this.visit(((NewArray)instruction).dimension);
         break;
      case 189:
         this.visit(((ANewArray)instruction).dimension);
         break;
      case 190:
         this.visit(((ArrayLength)instruction).arrayref);
         break;
      case 191:
         this.visit(((AThrow)instruction).value);
         break;
      case 192:
         this.visit(((CheckCast)instruction).objectref);
         break;
      case 193:
         this.visit(((InstanceOf)instruction).objectref);
         break;
      case 194:
         this.visit(((MonitorEnter)instruction).objectref);
         break;
      case 195:
         this.visit(((MonitorExit)instruction).objectref);
         break;
      case 197:
         Instruction[] var24 = ((MultiANewArray)instruction).dimensions;

         for(var11 = var24.length - 1; var11 >= 0; --var11) {
            this.visit(var24[var11]);
         }

         return;
      case 260:
      case 262:
         this.visit(((IfInstruction)instruction).value);
         break;
      case 261:
         IfCmp var23 = (IfCmp)instruction;
         this.visit(var23.value1);
         this.visit(var23.value2);
         break;
      case 263:
         if(((DupLoad)instruction).dupStore == this.dupStore) {
            ++this.counter;
         }
         break;
      case 264:
         this.visit(((DupStore)instruction).objectref);
         break;
      case 265:
         AssignmentInstruction var22 = (AssignmentInstruction)instruction;
         this.visit(var22.value1);
         this.visit(var22.value2);
         break;
      case 266:
         this.visit(((UnaryOperatorInstruction)instruction).value);
         break;
      case 267:
         BinaryOperatorInstruction var21 = (BinaryOperatorInstruction)instruction;
         this.visit(var21.value1);
         this.visit(var21.value2);
         break;
      case 271:
         ArrayLoadInstruction var20 = (ArrayLoadInstruction)instruction;
         this.visit(var20.arrayref);
         this.visit(var20.indexref);
         break;
      case 273:
         this.visit(((ReturnInstruction)instruction).valueref);
         break;
      case 274:
         var17 = ((InvokeNew)instruction).args;

         for(var11 = var17.size() - 1; var11 >= 0; --var11) {
            this.visit((Instruction)var17.get(var11));
         }

         return;
      case 275:
      case 276:
         this.visit(((ConvertInstruction)instruction).value);
         break;
      case 277:
      case 278:
         this.visit(((IncInstruction)instruction).value);
         break;
      case 280:
         this.visit(((TernaryOpStore)instruction).objectref);
         break;
      case 281:
         TernaryOperator var19 = (TernaryOperator)instruction;
         this.visit(var19.value1);
         this.visit(var19.value2);
         break;
      case 282:
      case 283:
         InitArrayInstruction var18 = (InitArrayInstruction)instruction;
         this.visit(var18.newArray);
         if(var18.values != null) {
            this.visit(var18.values);
         }
         break;
      case 284:
         var17 = ((ComplexConditionalBranchInstruction)instruction).instructions;

         for(var11 = var17.size() - 1; var11 >= 0; --var11) {
            this.visit((Instruction)var17.get(var11));
         }

         return;
      case 304:
         FastFor var15 = (FastFor)instruction;
         if(var15.init != null) {
            this.visit(var15.init);
         }

         if(var15.inc != null) {
            this.visit(var15.inc);
         }
      case 301:
      case 302:
      case 306:
         Instruction var16 = ((FastTestList)instruction).test;
         if(var16 != null) {
            this.visit(var16);
         }
      case 303:
         var17 = ((FastList)instruction).instructions;
         if(var17 != null) {
            this.visit(var17);
         }
         break;
      case 305:
         FastForEach var14 = (FastForEach)instruction;
         this.visit(var14.variable);
         this.visit(var14.values);
         this.visit(var14.instructions);
         break;
      case 307:
         FastTest2Lists var13 = (FastTest2Lists)instruction;
         this.visit(var13.test);
         this.visit(var13.instructions);
         this.visit(var13.instructions2);
         break;
      case 308:
      case 309:
      case 310:
      case 311:
      case 312:
      case 313:
         FastInstruction var12 = (FastInstruction)instruction;
         if(var12.instruction != null) {
            this.visit(var12.instruction);
         }
         break;
      case 314:
      case 315:
      case 316:
         FastSwitch var9 = (FastSwitch)instruction;
         this.visit(var9.test);
         FastSwitch.Pair[] var10 = var9.pairs;

         for(i = var10.length - 1; i >= 0; --i) {
            List instructions = var10[i].getInstructions();
            if(instructions != null) {
               this.visit(instructions);
            }
         }

         return;
      case 317:
         FastDeclaration var8 = (FastDeclaration)instruction;
         if(var8.instruction != null) {
            this.visit(var8.instruction);
         }
         break;
      case 318:
         FastTry var7 = (FastTry)instruction;
         this.visit(var7.instructions);
         if(var7.finallyInstructions != null) {
            this.visit(var7.finallyInstructions);
         }

         List catchs = var7.catches;

         for(i = catchs.size() - 1; i >= 0; --i) {
            this.visit(((FastTry.FastCatch)catchs.get(i)).instructions);
         }

         return;
      case 319:
         FastSynchronized var6 = (FastSynchronized)instruction;
         this.visit(var6.monitor);
         this.visit(var6.instructions);
         break;
      case 320:
         FastLabel fd = (FastLabel)instruction;
         if(fd.instruction != null) {
            this.visit(fd.instruction);
         }
         break;
      default:
         System.err.println("Can not count DupLoad in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
      }

   }

   private void visit(List<Instruction> instructions) {
      for(int i = instructions.size() - 1; i >= 0; --i) {
         this.visit((Instruction)instructions.get(i));
      }

   }

   public int getCounter() {
      return this.counter;
   }
}
