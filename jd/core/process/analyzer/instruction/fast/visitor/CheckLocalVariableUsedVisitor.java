package jd.core.process.analyzer.instruction.fast.visitor;

import java.util.List;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
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
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
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

public class CheckLocalVariableUsedVisitor {
   public static boolean Visit(LocalVariables localVariables, int maxOffset, Instruction instruction) {
      int i;
      int var12;
      List var17;
      LocalVariable var20;
      switch(instruction.opcode) {
      case 0:
      case 1:
      case 16:
      case 17:
      case 18:
      case 20:
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
      case 270:
      case 279:
      case 285:
         return false;
      case 21:
      case 25:
      case 268:
         LoadInstruction var30 = (LoadInstruction)instruction;
         var20 = localVariables.getLocalVariableWithIndexAndOffset(var30.index, var30.offset);
         if(var20 != null && maxOffset <= var20.start_pc) {
            return true;
         }

         return false;
      case 54:
      case 58:
      case 269:
         StoreInstruction var29 = (StoreInstruction)instruction;
         var20 = localVariables.getLocalVariableWithIndexAndOffset(var29.index, var29.offset);
         if(var20 != null && maxOffset <= var20.start_pc) {
            return true;
         }

         return Visit(localVariables, maxOffset, var29.valueref);
      case 83:
      case 272:
         ArrayStoreInstruction var28 = (ArrayStoreInstruction)instruction;
         if(Visit(localVariables, maxOffset, var28.indexref)) {
            return true;
         }

         return Visit(localVariables, maxOffset, var28.valueref);
      case 87:
         return Visit(localVariables, maxOffset, ((Pop)instruction).objectref);
      case 170:
         return Visit(localVariables, maxOffset, ((TableSwitch)instruction).key);
      case 171:
         return Visit(localVariables, maxOffset, ((LookupSwitch)instruction).key);
      case 179:
         return Visit(localVariables, maxOffset, ((PutStatic)instruction).valueref);
      case 180:
         return Visit(localVariables, maxOffset, ((GetField)instruction).objectref);
      case 181:
         PutField var27 = (PutField)instruction;
         if(Visit(localVariables, maxOffset, var27.objectref)) {
            return true;
         }

         return Visit(localVariables, maxOffset, var27.valueref);
      case 182:
      case 183:
      case 185:
         if(Visit(localVariables, maxOffset, ((InvokeNoStaticInstruction)instruction).objectref)) {
            return true;
         }
      case 184:
         var17 = ((InvokeInstruction)instruction).args;

         for(var12 = var17.size() - 1; var12 >= 0; --var12) {
            if(Visit(localVariables, maxOffset, (Instruction)var17.get(var12))) {
               return true;
            }
         }

         return false;
      case 188:
         return Visit(localVariables, maxOffset, ((NewArray)instruction).dimension);
      case 189:
         return Visit(localVariables, maxOffset, ((ANewArray)instruction).dimension);
      case 190:
         return Visit(localVariables, maxOffset, ((ArrayLength)instruction).arrayref);
      case 191:
         return Visit(localVariables, maxOffset, ((AThrow)instruction).value);
      case 192:
         return Visit(localVariables, maxOffset, ((CheckCast)instruction).objectref);
      case 193:
         return Visit(localVariables, maxOffset, ((InstanceOf)instruction).objectref);
      case 194:
         return Visit(localVariables, maxOffset, ((MonitorEnter)instruction).objectref);
      case 195:
         return Visit(localVariables, maxOffset, ((MonitorExit)instruction).objectref);
      case 197:
         Instruction[] var26 = ((MultiANewArray)instruction).dimensions;

         for(var12 = var26.length - 1; var12 >= 0; --var12) {
            if(Visit(localVariables, maxOffset, var26[var12])) {
               return true;
            }
         }

         return false;
      case 260:
      case 262:
         return Visit(localVariables, maxOffset, ((IfInstruction)instruction).value);
      case 261:
         IfCmp var25 = (IfCmp)instruction;
         if(Visit(localVariables, maxOffset, var25.value1)) {
            return true;
         }

         return Visit(localVariables, maxOffset, var25.value2);
      case 264:
         return Visit(localVariables, maxOffset, ((DupStore)instruction).objectref);
      case 265:
         AssignmentInstruction var24 = (AssignmentInstruction)instruction;
         if(Visit(localVariables, maxOffset, var24.value1)) {
            return true;
         }

         return Visit(localVariables, maxOffset, var24.value2);
      case 266:
         return Visit(localVariables, maxOffset, ((UnaryOperatorInstruction)instruction).value);
      case 267:
         BinaryOperatorInstruction var23 = (BinaryOperatorInstruction)instruction;
         if(Visit(localVariables, maxOffset, var23.value1)) {
            return true;
         }

         return Visit(localVariables, maxOffset, var23.value2);
      case 271:
         ArrayLoadInstruction var22 = (ArrayLoadInstruction)instruction;
         if(Visit(localVariables, maxOffset, var22.arrayref)) {
            return true;
         }

         return Visit(localVariables, maxOffset, var22.indexref);
      case 273:
         return Visit(localVariables, maxOffset, ((ReturnInstruction)instruction).valueref);
      case 274:
         var17 = ((InvokeNew)instruction).args;

         for(var12 = var17.size() - 1; var12 >= 0; --var12) {
            if(Visit(localVariables, maxOffset, (Instruction)var17.get(var12))) {
               return true;
            }
         }

         return false;
      case 275:
      case 276:
         return Visit(localVariables, maxOffset, ((ConvertInstruction)instruction).value);
      case 277:
      case 278:
         return Visit(localVariables, maxOffset, ((IncInstruction)instruction).value);
      case 280:
         return Visit(localVariables, maxOffset, ((TernaryOpStore)instruction).objectref);
      case 281:
         TernaryOperator var21 = (TernaryOperator)instruction;
         if(Visit(localVariables, maxOffset, var21.value1)) {
            return true;
         }

         return Visit(localVariables, maxOffset, var21.value2);
      case 282:
      case 283:
         InitArrayInstruction var19 = (InitArrayInstruction)instruction;
         if(Visit(localVariables, maxOffset, var19.newArray)) {
            return true;
         } else {
            if(var19.values != null && visit(localVariables, maxOffset, var19.values)) {
               return true;
            }

            return false;
         }
      case 284:
         var17 = ((ComplexConditionalBranchInstruction)instruction).instructions;

         for(var12 = var17.size() - 1; var12 >= 0; --var12) {
            if(Visit(localVariables, maxOffset, (Instruction)var17.get(var12))) {
               return true;
            }
         }

         return false;
      case 301:
      case 302:
      case 306:
         Instruction var18 = ((FastTestList)instruction).test;
         if(var18 != null && Visit(localVariables, maxOffset, var18)) {
            return true;
         }

         return false;
      case 303:
         var17 = ((FastList)instruction).instructions;
         if(var17 != null) {
            return visit(localVariables, maxOffset, var17);
         }

         return false;
      case 304:
         FastFor var16 = (FastFor)instruction;
         if(var16.init != null && Visit(localVariables, maxOffset, var16.init)) {
            return true;
         } else {
            if(var16.inc != null && Visit(localVariables, maxOffset, var16.inc)) {
               return true;
            }

            return false;
         }
      case 305:
         FastForEach var15 = (FastForEach)instruction;
         if(Visit(localVariables, maxOffset, var15.variable)) {
            return true;
         } else {
            if(Visit(localVariables, maxOffset, var15.values)) {
               return true;
            }

            return visit(localVariables, maxOffset, var15.instructions);
         }
      case 307:
         FastTest2Lists var14 = (FastTest2Lists)instruction;
         if(Visit(localVariables, maxOffset, var14.test)) {
            return true;
         } else {
            if(visit(localVariables, maxOffset, var14.instructions)) {
               return true;
            }

            return visit(localVariables, maxOffset, var14.instructions2);
         }
      case 308:
      case 309:
      case 310:
      case 311:
      case 312:
      case 313:
         FastInstruction var13 = (FastInstruction)instruction;
         if(var13.instruction != null && Visit(localVariables, maxOffset, var13.instruction)) {
            return true;
         }

         return false;
      case 314:
      case 315:
      case 316:
         FastSwitch var10 = (FastSwitch)instruction;
         if(Visit(localVariables, maxOffset, var10.test)) {
            return true;
         } else {
            FastSwitch.Pair[] var11 = var10.pairs;

            for(i = var11.length - 1; i >= 0; --i) {
               List instructions = var11[i].getInstructions();
               if(instructions != null && visit(localVariables, maxOffset, instructions)) {
                  return true;
               }
            }

            return false;
         }
      case 317:
         FastDeclaration var9 = (FastDeclaration)instruction;
         if(var9.instruction != null && Visit(localVariables, maxOffset, var9.instruction)) {
            return true;
         }

         return false;
      case 318:
         FastTry var8 = (FastTry)instruction;
         if(visit(localVariables, maxOffset, var8.instructions)) {
            return true;
         } else if(var8.finallyInstructions != null && visit(localVariables, maxOffset, var8.finallyInstructions)) {
            return true;
         } else {
            List catchs = var8.catches;

            for(i = catchs.size() - 1; i >= 0; --i) {
               if(visit(localVariables, maxOffset, ((FastTry.FastCatch)catchs.get(i)).instructions)) {
                  return true;
               }
            }

            return false;
         }
      case 319:
         FastSynchronized var7 = (FastSynchronized)instruction;
         if(Visit(localVariables, maxOffset, var7.monitor)) {
            return true;
         }

         return visit(localVariables, maxOffset, var7.instructions);
      case 320:
         FastLabel fd = (FastLabel)instruction;
         if(fd.instruction != null && Visit(localVariables, maxOffset, fd.instruction)) {
            return true;
         }

         return false;
      default:
         System.err.println("Can not find local variable used in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
         return false;
      }
   }

   private static boolean visit(LocalVariables localVariables, int maxOffset, List<Instruction> instructions) {
      for(int i = instructions.size() - 1; i >= 0; --i) {
         if(Visit(localVariables, maxOffset, (Instruction)instructions.get(i))) {
            return true;
         }
      }

      return false;
   }
}
