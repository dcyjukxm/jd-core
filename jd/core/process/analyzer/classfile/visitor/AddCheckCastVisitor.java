package jd.core.process.analyzer.classfile.visitor;

import java.util.List;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.constant.ConstantClass;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
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
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
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
import jd.core.util.SignatureUtil;

public class AddCheckCastVisitor {
   private ConstantPool constants;
   private LocalVariables localVariables;
   private LocalVariable localVariable;

   public AddCheckCastVisitor(ConstantPool constants, LocalVariables localVariables, LocalVariable localVariable) {
      this.constants = constants;
      this.localVariables = localVariables;
      this.localVariable = localVariable;
   }

   public void visit(Instruction instruction) {
      int cfr;
      List var8;
      ConstantClass var15;
      ConstantFieldref var22;
      Instruction var23;
      ConstantNameAndType var25;
      String var29;
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
      case 274:
      case 277:
      case 278:
      case 279:
      case 285:
         break;
      case 54:
      case 269:
         this.visit(((StoreInstruction)instruction).valueref);
         break;
      case 58:
         StoreInstruction var30 = (StoreInstruction)instruction;
         if(this.match(var30.valueref)) {
            LocalVariable var31 = this.localVariables.getLocalVariableWithIndexAndOffset(var30.index, var30.offset);
            if(var31.signature_index > 0 && var31.signature_index != this.constants.objectSignatureIndex) {
               String var32 = this.constants.getConstantUtf8(var31.signature_index);
               var30.valueref = this.newInstruction(var32, var30.valueref);
            }
         } else {
            this.visit(var30.valueref);
         }
         break;
      case 83:
      case 272:
         ArrayStoreInstruction var27 = (ArrayStoreInstruction)instruction;
         this.visit(var27.arrayref);
         this.visit(var27.valueref);
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
         PutStatic var26 = (PutStatic)instruction;
         if(this.match(var26.valueref)) {
            var22 = this.constants.getConstantFieldref(var26.index);
            var25 = this.constants.getConstantNameAndType(var22.name_and_type_index);
            if(var25.descriptor_index != this.constants.objectSignatureIndex) {
               var29 = this.constants.getConstantUtf8(var25.descriptor_index);
               var26.valueref = this.newInstruction(var29, var26.valueref);
            }
         } else {
            this.visit(var26.valueref);
         }
         break;
      case 180:
         GetField var24 = (GetField)instruction;
         if(this.match(var24.objectref)) {
            var22 = this.constants.getConstantFieldref(var24.index);
            var15 = this.constants.getConstantClass(var22.class_index);
            if(this.constants.objectClassNameIndex != var15.name_index) {
               var23 = var24.objectref;
               var24.objectref = new CheckCast(192, var23.offset, var23.lineNumber, var22.class_index, var23);
            }
         } else {
            this.visit(var24.objectref);
         }
         break;
      case 181:
         PutField var21 = (PutField)instruction;
         if(this.match(var21.objectref)) {
            var22 = this.constants.getConstantFieldref(var21.index);
            var15 = this.constants.getConstantClass(var22.class_index);
            if(this.constants.objectClassNameIndex != var15.name_index) {
               var23 = var21.objectref;
               var21.objectref = new CheckCast(192, var23.offset, var23.lineNumber, var22.class_index, var23);
            }
         } else {
            this.visit(var21.objectref);
         }

         if(this.match(var21.valueref)) {
            var22 = this.constants.getConstantFieldref(var21.index);
            var25 = this.constants.getConstantNameAndType(var22.name_and_type_index);
            if(var25.descriptor_index != this.constants.objectSignatureIndex) {
               var29 = this.constants.getConstantUtf8(var25.descriptor_index);
               var21.valueref = this.newInstruction(var29, var21.valueref);
            }
         } else {
            this.visit(var21.valueref);
         }
         break;
      case 182:
      case 183:
      case 185:
         InvokeNoStaticInstruction var17 = (InvokeNoStaticInstruction)instruction;
         if(this.match(var17.objectref)) {
            ConstantMethodref var19 = this.constants.getConstantMethodref(var17.index);
            var15 = this.constants.getConstantClass(var19.class_index);
            if(this.constants.objectClassNameIndex != var15.name_index) {
               var23 = var17.objectref;
               var17.objectref = new CheckCast(192, var23.offset, var23.lineNumber, var19.class_index, var23);
            }
         } else {
            this.visit(var17.objectref);
         }
      case 184:
         var8 = ((InvokeInstruction)instruction).args;
         List var20 = ((InvokeInstruction)instruction).getListOfParameterSignatures(this.constants);

         for(int var18 = var8.size() - 1; var18 >= 0; --var18) {
            var23 = (Instruction)var8.get(var18);
            if(this.match(var23)) {
               String var28 = (String)var20.get(var18);
               if(!var28.equals("Ljava/lang/Object;")) {
                  var8.set(var18, this.newInstruction(var28, var23));
               }
            } else {
               this.visit(var23);
            }
         }

         return;
      case 188:
         this.visit(((NewArray)instruction).dimension);
         break;
      case 189:
         this.visit(((ANewArray)instruction).dimension);
         break;
      case 190:
         ArrayLength var14 = (ArrayLength)instruction;
         this.visit(var14.arrayref);
         break;
      case 191:
         AThrow var13 = (AThrow)instruction;
         if(this.match(var13.value)) {
            LoadInstruction var16 = (LoadInstruction)var13.value;
            LocalVariable cnat = this.localVariables.getLocalVariableWithIndexAndOffset(var16.index, var16.offset);
            if(cnat.signature_index == this.constants.objectSignatureIndex) {
               int signature = this.constants.addConstantUtf8("java/lang/Throwable");
               int signature1 = this.constants.addConstantClass(signature);
               Instruction i = var13.value;
               var13.value = new CheckCast(192, i.offset, i.lineNumber, signature1, i);
            }
         } else {
            this.visit(var13.value);
         }
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
         Instruction[] var12 = ((MultiANewArray)instruction).dimensions;

         for(cfr = var12.length - 1; cfr >= 0; --cfr) {
            this.visit(var12[cfr]);
         }

         return;
      case 260:
      case 262:
         this.visit(((IfInstruction)instruction).value);
         break;
      case 261:
         IfCmp var11 = (IfCmp)instruction;
         this.visit(var11.value1);
         this.visit(var11.value2);
         break;
      case 264:
         this.visit(((DupStore)instruction).objectref);
         break;
      case 266:
         this.visit(((UnaryOperatorInstruction)instruction).value);
         break;
      case 267:
         BinaryOperatorInstruction var10 = (BinaryOperatorInstruction)instruction;
         this.visit(var10.value1);
         this.visit(var10.value2);
         break;
      case 273:
         this.visit(((ReturnInstruction)instruction).valueref);
         break;
      case 275:
      case 276:
         this.visit(((ConvertInstruction)instruction).value);
         break;
      case 280:
         this.visit(((TernaryOpStore)instruction).objectref);
         break;
      case 281:
         TernaryOperator var9 = (TernaryOperator)instruction;
         this.visit(var9.value1);
         this.visit(var9.value2);
         break;
      case 284:
         var8 = ((ComplexConditionalBranchInstruction)instruction).instructions;

         for(cfr = var8.size() - 1; cfr >= 0; --cfr) {
            this.visit((Instruction)var8.get(cfr));
         }

         return;
      case 286:
         AssertInstruction to = (AssertInstruction)instruction;
         this.visit(to.test);
         if(to.msg != null) {
            this.visit(to.msg);
         }
         break;
      default:
         System.err.println("Can not add cast in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
      }

   }

   private boolean match(Instruction i) {
      if(i.opcode == 25) {
         LoadInstruction li = (LoadInstruction)i;
         if(li.index == this.localVariable.index) {
            LocalVariable lv = this.localVariables.getLocalVariableWithIndexAndOffset(li.index, li.offset);
            if(lv == this.localVariable) {
               return true;
            }

            return false;
         }
      }

      return false;
   }

   private Instruction newInstruction(String signature, Instruction i) {
      if(SignatureUtil.IsPrimitiveSignature(signature)) {
         return new ConvertInstruction(275, i.offset, i.lineNumber, i, signature);
      } else {
         int nameIndex;
         if(signature.charAt(0) == 76) {
            String classIndex = SignatureUtil.GetInnerName(signature);
            nameIndex = this.constants.addConstantUtf8(classIndex);
         } else {
            nameIndex = this.constants.addConstantUtf8(signature);
         }

         int classIndex1 = this.constants.addConstantClass(nameIndex);
         return new CheckCast(192, i.offset, i.lineNumber, classIndex1, i);
      }
   }
}
