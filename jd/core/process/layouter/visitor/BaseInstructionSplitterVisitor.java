package jd.core.process.layouter.visitor;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.constant.ConstantMethodref;
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
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.fast.instruction.FastDeclaration;

public abstract class BaseInstructionSplitterVisitor {
   protected ClassFile classFile;
   protected ConstantPool constants;

   public void start(ClassFile classFile) {
      this.classFile = classFile;
      this.constants = classFile == null?null:classFile.getConstantPool();
   }

   public void visit(Instruction instruction) {
      this.visit((Instruction)null, instruction);
   }

   protected void visit(Instruction parent, Instruction instruction) {
      int values;
      int lenght;
      int i;
      List var11;
      List var14;
      switch(instruction.opcode) {
      case 54:
      case 58:
      case 269:
         this.visit(instruction, ((StoreInstruction)instruction).valueref);
         break;
      case 83:
      case 272:
         ArrayStoreInstruction var21 = (ArrayStoreInstruction)instruction;
         this.visit(instruction, var21.arrayref);
         this.visit(instruction, var21.indexref);
         this.visit(instruction, var21.valueref);
         break;
      case 87:
         this.visit(instruction, ((Pop)instruction).objectref);
         break;
      case 170:
      case 171:
         this.visit(instruction, ((Switch)instruction).key);
         break;
      case 179:
         this.visit(instruction, ((PutStatic)instruction).valueref);
         break;
      case 180:
         this.visit(instruction, ((GetField)instruction).objectref);
         break;
      case 181:
         PutField var20 = (PutField)instruction;
         this.visit(instruction, var20.objectref);
         this.visit(instruction, var20.valueref);
         break;
      case 182:
      case 183:
      case 185:
         this.visit(instruction, ((InvokeNoStaticInstruction)instruction).objectref);
      case 184:
         var11 = ((InvokeInstruction)instruction).args;
         values = var11.size();

         for(lenght = 0; lenght < values; ++lenght) {
            this.visit(instruction, (Instruction)var11.get(lenght));
         }

         return;
      case 188:
         this.visit(instruction, ((NewArray)instruction).dimension);
         break;
      case 189:
         this.visit(instruction, ((ANewArray)instruction).dimension);
         break;
      case 190:
         this.visit(instruction, ((ArrayLength)instruction).arrayref);
         break;
      case 191:
         this.visit(instruction, ((AThrow)instruction).value);
         break;
      case 192:
         this.visit(instruction, ((CheckCast)instruction).objectref);
         break;
      case 193:
         this.visit(instruction, ((InstanceOf)instruction).objectref);
         break;
      case 197:
         Instruction[] var19 = ((MultiANewArray)instruction).dimensions;
         values = var19.length;

         for(lenght = 0; lenght < values; ++lenght) {
            this.visit(instruction, var19[lenght]);
         }

         return;
      case 260:
      case 262:
         this.visit(instruction, ((IfInstruction)instruction).value);
         break;
      case 261:
         IfCmp var18 = (IfCmp)instruction;
         this.visit(instruction, var18.value1);
         this.visit(instruction, var18.value2);
         break;
      case 265:
      case 267:
         BinaryOperatorInstruction var17 = (BinaryOperatorInstruction)instruction;
         this.visit(instruction, var17.value1);
         this.visit(instruction, var17.value2);
         break;
      case 266:
         this.visit(instruction, ((UnaryOperatorInstruction)instruction).value);
         break;
      case 271:
         ArrayLoadInstruction var16 = (ArrayLoadInstruction)instruction;
         this.visit(instruction, var16.arrayref);
         this.visit(instruction, var16.indexref);
         break;
      case 273:
         this.visit(instruction, ((ReturnInstruction)instruction).valueref);
         break;
      case 274:
         InvokeNew var15 = (InvokeNew)instruction;
         var14 = var15.args;
         lenght = var14.size();

         for(i = 0; i < lenght; ++i) {
            this.visit(instruction, (Instruction)var14.get(i));
         }

         ConstantMethodref var22 = this.constants.getConstantMethodref(var15.index);
         String internalClassName = this.constants.getConstantClassName(var22.class_index);
         String prefix = this.classFile.getThisClassName() + '$';
         if(internalClassName.startsWith(prefix)) {
            ClassFile innerClassFile = this.classFile.getInnerClassFile(internalClassName);
            if(innerClassFile != null && innerClassFile.getInternalAnonymousClassName() != null) {
               this.visitAnonymousNewInvoke((Instruction)(parent == null?var15:parent), var15, innerClassFile);
            }
         }
         break;
      case 275:
      case 276:
         this.visit(instruction, ((ConvertInstruction)instruction).value);
         break;
      case 277:
      case 278:
         this.visit(instruction, ((IncInstruction)instruction).value);
         break;
      case 280:
         this.visit(instruction, ((TernaryOpStore)instruction).objectref);
         break;
      case 281:
         TernaryOperator var13 = (TernaryOperator)instruction;
         this.visit(instruction, var13.test);
         this.visit(instruction, var13.value1);
         this.visit(instruction, var13.value2);
         break;
      case 282:
      case 283:
         InitArrayInstruction var12 = (InitArrayInstruction)instruction;
         this.visit(instruction, var12.newArray);
         var14 = var12.values;
         lenght = var14.size();

         for(i = 0; i < lenght; ++i) {
            this.visit(instruction, (Instruction)var14.get(i));
         }

         return;
      case 284:
         var11 = ((ComplexConditionalBranchInstruction)instruction).instructions;
         values = var11.size();

         for(lenght = 0; lenght < values; ++lenght) {
            this.visit(instruction, (Instruction)var11.get(lenght));
         }

         return;
      case 286:
         AssertInstruction var10 = (AssertInstruction)instruction;
         this.visit(instruction, var10.test);
         if(var10.msg != null) {
            this.visit(instruction, var10.msg);
         }
         break;
      case 317:
         FastDeclaration iai = (FastDeclaration)instruction;
         if(iai.instruction != null) {
            this.visit(instruction, iai.instruction);
         }
      }

   }

   public abstract void visitAnonymousNewInvoke(Instruction var1, InvokeNew var2, ClassFile var3);
}
