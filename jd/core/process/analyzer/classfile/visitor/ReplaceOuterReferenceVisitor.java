package jd.core.process.analyzer.classfile.visitor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.GetField;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
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

public class ReplaceOuterReferenceVisitor {
   private int opcode;
   private int index;
   private int outerThisInstructionIndex;

   public ReplaceOuterReferenceVisitor(int opcode, int index, int outerThisInstructionIndex) {
      this.opcode = opcode;
      this.index = index;
      this.outerThisInstructionIndex = outerThisInstructionIndex;
   }

   public void init(int opcode, int index) {
      this.opcode = opcode;
      this.index = index;
   }

   public void visit(Instruction instruction) {
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
      case 279:
      case 285:
         break;
      case 54:
      case 58:
      case 269:
         StoreInstruction var35 = (StoreInstruction)instruction;
         if(this.match(var35.valueref)) {
            var35.valueref = this.newInstruction(var35.valueref);
         } else {
            this.visit(var35.valueref);
         }
         break;
      case 83:
      case 272:
         ArrayStoreInstruction var34 = (ArrayStoreInstruction)instruction;
         if(this.match(var34.arrayref)) {
            var34.arrayref = this.newInstruction(var34.arrayref);
         } else {
            this.visit(var34.arrayref);
         }

         if(this.match(var34.indexref)) {
            var34.indexref = this.newInstruction(var34.indexref);
         } else {
            this.visit(var34.indexref);
         }

         if(this.match(var34.valueref)) {
            var34.valueref = this.newInstruction(var34.valueref);
         } else {
            this.visit(var34.valueref);
         }
         break;
      case 87:
         Pop var33 = (Pop)instruction;
         if(this.match(var33.objectref)) {
            var33.objectref = this.newInstruction(var33.objectref);
         } else {
            this.visit(var33.objectref);
         }
         break;
      case 170:
         TableSwitch var32 = (TableSwitch)instruction;
         if(this.match(var32.key)) {
            var32.key = this.newInstruction(var32.key);
         } else {
            this.visit(var32.key);
         }
         break;
      case 171:
         LookupSwitch var31 = (LookupSwitch)instruction;
         if(this.match(var31.key)) {
            var31.key = this.newInstruction(var31.key);
         } else {
            this.visit(var31.key);
         }
         break;
      case 179:
         PutStatic var30 = (PutStatic)instruction;
         if(this.match(var30.valueref)) {
            var30.valueref = this.newInstruction(var30.valueref);
         } else {
            this.visit(var30.valueref);
         }
         break;
      case 180:
         GetField var29 = (GetField)instruction;
         if(this.match(var29.objectref)) {
            var29.objectref = this.newInstruction(var29.objectref);
         } else {
            this.visit(var29.objectref);
         }
         break;
      case 181:
         PutField var28 = (PutField)instruction;
         if(this.match(var28.objectref)) {
            var28.objectref = this.newInstruction(var28.objectref);
         } else {
            this.visit(var28.objectref);
         }

         if(this.match(var28.valueref)) {
            var28.valueref = this.newInstruction(var28.valueref);
         } else {
            this.visit(var28.valueref);
         }
         break;
      case 182:
      case 183:
      case 185:
         InvokeNoStaticInstruction var27 = (InvokeNoStaticInstruction)instruction;
         if(this.match(var27.objectref)) {
            var27.objectref = this.newInstruction(var27.objectref);
         } else {
            this.visit(var27.objectref);
         }
      case 184:
      case 274:
         var4 = ((InvokeInstruction)instruction).args;

         for(i = var4.size() - 1; i >= 0; --i) {
            if(this.match((Instruction)var4.get(i))) {
               var4.set(i, this.newInstruction((Instruction)var4.get(i)));
            } else {
               this.visit((Instruction)var4.get(i));
            }
         }

         return;
      case 188:
         NewArray var26 = (NewArray)instruction;
         if(this.match(var26.dimension)) {
            var26.dimension = this.newInstruction(var26.dimension);
         } else {
            this.visit(var26.dimension);
         }
         break;
      case 189:
         ANewArray var25 = (ANewArray)instruction;
         if(this.match(var25.dimension)) {
            var25.dimension = this.newInstruction(var25.dimension);
         } else {
            this.visit(var25.dimension);
         }
         break;
      case 190:
         ArrayLength var24 = (ArrayLength)instruction;
         if(this.match(var24.arrayref)) {
            var24.arrayref = this.newInstruction(var24.arrayref);
         } else {
            this.visit(var24.arrayref);
         }
         break;
      case 191:
         AThrow var23 = (AThrow)instruction;
         if(this.match(var23.value)) {
            var23.value = this.newInstruction(var23.value);
         } else {
            this.visit(var23.value);
         }
         break;
      case 192:
         CheckCast var22 = (CheckCast)instruction;
         if(this.match(var22.objectref)) {
            var22.objectref = this.newInstruction(var22.objectref);
         } else {
            this.visit(var22.objectref);
         }
         break;
      case 193:
         InstanceOf var21 = (InstanceOf)instruction;
         if(this.match(var21.objectref)) {
            var21.objectref = this.newInstruction(var21.objectref);
         } else {
            this.visit(var21.objectref);
         }
         break;
      case 194:
         MonitorEnter var20 = (MonitorEnter)instruction;
         if(this.match(var20.objectref)) {
            var20.objectref = this.newInstruction(var20.objectref);
         } else {
            this.visit(var20.objectref);
         }
         break;
      case 195:
         MonitorExit var19 = (MonitorExit)instruction;
         if(this.match(var19.objectref)) {
            var19.objectref = this.newInstruction(var19.objectref);
         } else {
            this.visit(var19.objectref);
         }
         break;
      case 197:
         Instruction[] var18 = ((MultiANewArray)instruction).dimensions;

         for(i = var18.length - 1; i >= 0; --i) {
            if(this.match(var18[i])) {
               var18[i] = this.newInstruction(var18[i]);
            } else {
               this.visit(var18[i]);
            }
         }

         return;
      case 260:
      case 262:
         IfInstruction var17 = (IfInstruction)instruction;
         if(this.match(var17.value)) {
            var17.value = this.newInstruction(var17.value);
         } else {
            this.visit(var17.value);
         }
         break;
      case 261:
         IfCmp var16 = (IfCmp)instruction;
         if(this.match(var16.value1)) {
            var16.value1 = this.newInstruction(var16.value1);
         } else {
            this.visit(var16.value1);
         }

         if(this.match(var16.value2)) {
            var16.value2 = this.newInstruction(var16.value2);
         } else {
            this.visit(var16.value2);
         }
         break;
      case 264:
         DupStore var15 = (DupStore)instruction;
         if(this.match(var15.objectref)) {
            var15.objectref = this.newInstruction(var15.objectref);
         } else {
            this.visit(var15.objectref);
         }
         break;
      case 265:
         AssignmentInstruction var14 = (AssignmentInstruction)instruction;
         if(this.match(var14.value1)) {
            var14.value1 = this.newInstruction(var14.value1);
         } else {
            this.visit(var14.value1);
         }

         if(this.match(var14.value2)) {
            var14.value2 = this.newInstruction(var14.value2);
         } else {
            this.visit(var14.value2);
         }
         break;
      case 266:
         UnaryOperatorInstruction var13 = (UnaryOperatorInstruction)instruction;
         if(this.match(var13.value)) {
            var13.value = this.newInstruction(var13.value);
         } else {
            this.visit(var13.value);
         }
         break;
      case 267:
         BinaryOperatorInstruction var12 = (BinaryOperatorInstruction)instruction;
         if(this.match(var12.value1)) {
            var12.value1 = this.newInstruction(var12.value1);
         } else {
            this.visit(var12.value1);
         }

         if(this.match(var12.value2)) {
            var12.value2 = this.newInstruction(var12.value2);
         } else {
            this.visit(var12.value2);
         }
         break;
      case 271:
         ArrayLoadInstruction var11 = (ArrayLoadInstruction)instruction;
         if(this.match(var11.arrayref)) {
            var11.arrayref = this.newInstruction(var11.arrayref);
         } else {
            this.visit(var11.arrayref);
         }

         if(this.match(var11.indexref)) {
            var11.indexref = this.newInstruction(var11.indexref);
         } else {
            this.visit(var11.indexref);
         }
         break;
      case 273:
         ReturnInstruction var10 = (ReturnInstruction)instruction;
         if(this.match(var10.valueref)) {
            var10.valueref = this.newInstruction(var10.valueref);
         } else {
            this.visit(var10.valueref);
         }
         break;
      case 275:
      case 276:
         ConvertInstruction var9 = (ConvertInstruction)instruction;
         if(this.match(var9.value)) {
            var9.value = this.newInstruction(var9.value);
         } else {
            this.visit(var9.value);
         }
         break;
      case 277:
      case 278:
         IncInstruction var8 = (IncInstruction)instruction;
         if(this.match(var8.value)) {
            var8.value = this.newInstruction(var8.value);
         } else {
            this.visit(var8.value);
         }
         break;
      case 280:
         TernaryOpStore var7 = (TernaryOpStore)instruction;
         if(this.match(var7.objectref)) {
            var7.objectref = this.newInstruction(var7.objectref);
         } else {
            this.visit(var7.objectref);
         }
         break;
      case 281:
         TernaryOperator var6 = (TernaryOperator)instruction;
         if(this.match(var6.test)) {
            var6.test = this.newInstruction(var6.test);
         } else {
            this.visit(var6.test);
         }

         if(this.match(var6.value1)) {
            var6.value1 = this.newInstruction(var6.value1);
         } else {
            this.visit(var6.value1);
         }

         if(this.match(var6.value2)) {
            var6.value2 = this.newInstruction(var6.value2);
         } else {
            this.visit(var6.value2);
         }
         break;
      case 282:
      case 283:
         InitArrayInstruction var5 = (InitArrayInstruction)instruction;
         if(this.match(var5.newArray)) {
            var5.newArray = this.newInstruction(var5.newArray);
         } else {
            this.visit(var5.newArray);
         }

         if(var5.values != null) {
            this.visit(var5.values);
         }
         break;
      case 284:
         var4 = ((ComplexConditionalBranchInstruction)instruction).instructions;

         for(i = var4.size() - 1; i >= 0; --i) {
            this.visit((Instruction)var4.get(i));
         }

         return;
      case 286:
         AssertInstruction iai = (AssertInstruction)instruction;
         if(this.match(iai.test)) {
            iai.test = this.newInstruction(iai.test);
         } else {
            this.visit(iai.test);
         }

         if(iai.msg != null) {
            if(this.match(iai.msg)) {
               iai.msg = this.newInstruction(iai.msg);
            } else {
               this.visit(iai.msg);
            }
         }
         break;
      default:
         System.err.println("Can not replace DupLoad in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
      }

   }

   public void visit(List<Instruction> instructions) {
      for(int index = instructions.size() - 1; index >= 0; --index) {
         Instruction i = (Instruction)instructions.get(index);
         if(this.match(i)) {
            instructions.set(index, this.newInstruction(i));
         } else {
            this.visit(i);
         }
      }

   }

   private boolean match(Instruction i) {
      return i.opcode == this.opcode && ((IndexInstruction)i).index == this.index;
   }

   private Instruction newInstruction(Instruction i) {
      return new GetStatic(285, i.offset, i.lineNumber, this.outerThisInstructionIndex);
   }
}
