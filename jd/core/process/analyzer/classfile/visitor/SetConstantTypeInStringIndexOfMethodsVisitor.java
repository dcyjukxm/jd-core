package jd.core.process.analyzer.classfile.visitor;

import java.util.List;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.constant.ConstantClass;
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
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
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

public class SetConstantTypeInStringIndexOfMethodsVisitor {
   protected ConstantPool constants;

   public SetConstantTypeInStringIndexOfMethodsVisitor(ConstantPool constants) {
      this.constants = constants;
   }

   public void visit(Instruction instruction) {
      label255: {
         int i;
         List var10;
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
            return;
         case 54:
         case 58:
         case 269:
            this.visit(((StoreInstruction)instruction).valueref);
            return;
         case 83:
         case 272:
            this.visit(((ArrayStoreInstruction)instruction).arrayref);
            return;
         case 87:
            this.visit(((Pop)instruction).objectref);
            return;
         case 170:
            this.visit(((TableSwitch)instruction).key);
            return;
         case 171:
            this.visit(((LookupSwitch)instruction).key);
            return;
         case 179:
            this.visit(((PutStatic)instruction).valueref);
            return;
         case 180:
            this.visit(((GetField)instruction).objectref);
            return;
         case 181:
            PutField var17 = (PutField)instruction;
            this.visit(var17.objectref);
            this.visit(var17.valueref);
            return;
         case 182:
            Invokevirtual var15 = (Invokevirtual)instruction;
            ConstantMethodref var16 = this.constants.getConstantMethodref(var15.index);
            ConstantClass cc = this.constants.getConstantClass(var16.class_index);
            if(cc.name_index == this.constants.stringClassNameIndex) {
               int nbrOfParameters = var15.args.size();
               if(1 <= nbrOfParameters && nbrOfParameters <= 2) {
                  int opcode = ((Instruction)var15.args.get(0)).opcode;
                  if((opcode == 16 || opcode == 17) && var16.getReturnedSignature().equals("I") && ((String)var16.getListOfParameterSignatures().get(0)).equals("I")) {
                     ConstantNameAndType cnat = this.constants.getConstantNameAndType(var16.name_and_type_index);
                     String name = this.constants.getConstantUtf8(cnat.name_index);
                     if("indexOf".equals(name) || "lastIndexOf".equals(name)) {
                        IConst ic = (IConst)var15.args.get(0);
                        ic.setReturnedSignature("C");
                        return;
                     }
                  }
               }
            }
         case 183:
         case 185:
            this.visit(((InvokeNoStaticInstruction)instruction).objectref);
         case 184:
         case 274:
            break;
         case 188:
            this.visit(((NewArray)instruction).dimension);
            return;
         case 189:
            this.visit(((ANewArray)instruction).dimension);
            return;
         case 190:
            this.visit(((ArrayLength)instruction).arrayref);
            return;
         case 191:
            this.visit(((AThrow)instruction).value);
            return;
         case 192:
            this.visit(((CheckCast)instruction).objectref);
            return;
         case 193:
            this.visit(((InstanceOf)instruction).objectref);
            return;
         case 194:
            this.visit(((MonitorEnter)instruction).objectref);
            return;
         case 195:
            this.visit(((MonitorExit)instruction).objectref);
            return;
         case 197:
            Instruction[] var14 = ((MultiANewArray)instruction).dimensions;

            for(i = var14.length - 1; i >= 0; --i) {
               this.visit(var14[i]);
            }
            break label255;
         case 260:
         case 262:
            this.visit(((IfInstruction)instruction).value);
            return;
         case 261:
            IfCmp var13 = (IfCmp)instruction;
            this.visit(var13.value1);
            this.visit(var13.value2);
            return;
         case 264:
            this.visit(((DupStore)instruction).objectref);
            return;
         case 265:
         case 267:
            BinaryOperatorInstruction var12 = (BinaryOperatorInstruction)instruction;
            this.visit(var12.value1);
            this.visit(var12.value2);
            return;
         case 266:
            this.visit(((UnaryOperatorInstruction)instruction).value);
            return;
         case 273:
            this.visit(((ReturnInstruction)instruction).valueref);
            return;
         case 275:
         case 276:
            this.visit(((ConvertInstruction)instruction).value);
            return;
         case 277:
         case 278:
            this.visit(((IncInstruction)instruction).value);
            return;
         case 280:
            this.visit(((TernaryOpStore)instruction).objectref);
            return;
         case 282:
         case 283:
            InitArrayInstruction var11 = (InitArrayInstruction)instruction;
            this.visit(var11.newArray);
            if(var11.values != null) {
               this.visit(var11.values);
            }

            return;
         case 284:
            var10 = ((ComplexConditionalBranchInstruction)instruction).instructions;

            for(i = var10.size() - 1; i >= 0; --i) {
               this.visit((Instruction)var10.get(i));
            }

            return;
         case 286:
            AssertInstruction iai = (AssertInstruction)instruction;
            this.visit(iai.test);
            if(iai.msg != null) {
               this.visit(iai.msg);
            }

            return;
         default:
            System.err.println("Can not search String.indexOf in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
            return;
         }

         var10 = ((InvokeInstruction)instruction).args;

         for(i = var10.size() - 1; i >= 0; --i) {
            this.visit((Instruction)var10.get(i));
         }
      }

   }

   public void visit(List<Instruction> instructions) {
      for(int i = instructions.size() - 1; i >= 0; --i) {
         this.visit((Instruction)instructions.get(i));
      }

   }
}
