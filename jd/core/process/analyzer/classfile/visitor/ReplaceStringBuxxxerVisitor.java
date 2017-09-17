package jd.core.process.analyzer.classfile.visitor;

import java.util.List;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.constant.ConstantClass;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
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
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
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
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;

public class ReplaceStringBuxxxerVisitor {
   private ConstantPool constants;

   public ReplaceStringBuxxxerVisitor(ConstantPool constants) {
      this.constants = constants;
   }

   public void visit(Instruction instruction) {
      Instruction i;
      int index;
      switch(instruction.opcode) {
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
      case 277:
      case 278:
      case 279:
      case 285:
         break;
      case 54:
      case 58:
      case 269:
         StoreInstruction var38 = (StoreInstruction)instruction;
         i = this.match(var38.valueref);
         if(i == null) {
            this.visit(var38.valueref);
         } else {
            var38.valueref = i;
         }
         break;
      case 83:
      case 272:
         ArrayStoreInstruction var37 = (ArrayStoreInstruction)instruction;
         i = this.match(var37.arrayref);
         if(i == null) {
            this.visit(var37.arrayref);
         } else {
            var37.arrayref = i;
         }

         i = this.match(var37.indexref);
         if(i == null) {
            this.visit(var37.indexref);
         } else {
            var37.indexref = i;
         }

         i = this.match(var37.valueref);
         if(i == null) {
            this.visit(var37.valueref);
         } else {
            var37.valueref = i;
         }
         break;
      case 87:
         this.visit(((Pop)instruction).objectref);
         break;
      case 170:
         TableSwitch var36 = (TableSwitch)instruction;
         i = this.match(var36.key);
         if(i == null) {
            this.visit(var36.key);
         } else {
            var36.key = i;
         }
         break;
      case 171:
         LookupSwitch var35 = (LookupSwitch)instruction;
         i = this.match(var35.key);
         if(i == null) {
            this.visit(var35.key);
         } else {
            var35.key = i;
         }
         break;
      case 179:
         PutStatic var34 = (PutStatic)instruction;
         i = this.match(var34.valueref);
         if(i == null) {
            this.visit(var34.valueref);
         } else {
            var34.valueref = i;
         }
         break;
      case 180:
         GetField var33 = (GetField)instruction;
         i = this.match(var33.objectref);
         if(i == null) {
            this.visit(var33.objectref);
         } else {
            var33.objectref = i;
         }
         break;
      case 181:
         PutField var31 = (PutField)instruction;
         i = this.match(var31.objectref);
         if(i == null) {
            this.visit(var31.objectref);
         } else {
            var31.objectref = i;
         }

         i = this.match(var31.valueref);
         if(i == null) {
            this.visit(var31.valueref);
         } else {
            var31.valueref = i;
         }
         break;
      case 182:
      case 183:
      case 185:
         InvokeNoStaticInstruction var30 = (InvokeNoStaticInstruction)instruction;
         i = this.match(var30.objectref);
         if(i == null) {
            this.visit(var30.objectref);
         } else {
            var30.objectref = i;
         }

         this.replaceInArgs(var30.args);
         break;
      case 184:
      case 274:
         this.replaceInArgs(((InvokeInstruction)instruction).args);
         break;
      case 188:
         NewArray var29 = (NewArray)instruction;
         i = this.match(var29.dimension);
         if(i == null) {
            this.visit(var29.dimension);
         } else {
            var29.dimension = i;
         }
         break;
      case 189:
         ANewArray var28 = (ANewArray)instruction;
         i = this.match(var28.dimension);
         if(i == null) {
            this.visit(var28.dimension);
         } else {
            var28.dimension = i;
         }
         break;
      case 190:
         ArrayLength var27 = (ArrayLength)instruction;
         i = this.match(var27.arrayref);
         if(i == null) {
            this.visit(var27.arrayref);
         } else {
            var27.arrayref = i;
         }
         break;
      case 191:
         AThrow var26 = (AThrow)instruction;
         this.visit(var26.value);
         break;
      case 192:
         CheckCast var25 = (CheckCast)instruction;
         i = this.match(var25.objectref);
         if(i == null) {
            this.visit(var25.objectref);
         } else {
            var25.objectref = i;
         }
         break;
      case 193:
         InstanceOf var24 = (InstanceOf)instruction;
         i = this.match(var24.objectref);
         if(i == null) {
            this.visit(var24.objectref);
         } else {
            var24.objectref = i;
         }
         break;
      case 194:
         MonitorEnter var23 = (MonitorEnter)instruction;
         i = this.match(var23.objectref);
         if(i == null) {
            this.visit(var23.objectref);
         } else {
            var23.objectref = i;
         }
         break;
      case 195:
         MonitorExit var22 = (MonitorExit)instruction;
         i = this.match(var22.objectref);
         if(i == null) {
            this.visit(var22.objectref);
         } else {
            var22.objectref = i;
         }
         break;
      case 197:
         MultiANewArray var21 = (MultiANewArray)instruction;
         Instruction[] var32 = var21.dimensions;

         for(int i1 = var32.length - 1; i1 >= 0; --i1) {
            Instruction var17 = this.match(var32[i1]);
            if(var17 == null) {
               this.visit(var32[i1]);
            } else {
               var32[i1] = var17;
            }
         }

         return;
      case 260:
      case 262:
         IfInstruction var20 = (IfInstruction)instruction;
         i = this.match(var20.value);
         if(i == null) {
            this.visit(var20.value);
         } else {
            var20.value = i;
         }
         break;
      case 261:
         IfCmp var19 = (IfCmp)instruction;
         i = this.match(var19.value1);
         if(i == null) {
            this.visit(var19.value1);
         } else {
            var19.value1 = i;
         }

         i = this.match(var19.value2);
         if(i == null) {
            this.visit(var19.value2);
         } else {
            var19.value2 = i;
         }
         break;
      case 264:
         DupStore var18 = (DupStore)instruction;
         i = this.match(var18.objectref);
         if(i == null) {
            this.visit(var18.objectref);
         } else {
            var18.objectref = i;
         }
         break;
      case 265:
         AssignmentInstruction var16 = (AssignmentInstruction)instruction;
         i = this.match(var16.value1);
         if(i == null) {
            this.visit(var16.value1);
         } else {
            var16.value1 = i;
         }

         i = this.match(var16.value2);
         if(i == null) {
            this.visit(var16.value2);
         } else {
            var16.value2 = i;
         }
         break;
      case 266:
         UnaryOperatorInstruction var15 = (UnaryOperatorInstruction)instruction;
         i = this.match(var15.value);
         if(i == null) {
            this.visit(var15.value);
         } else {
            var15.value = i;
         }
         break;
      case 267:
         BinaryOperatorInstruction var14 = (BinaryOperatorInstruction)instruction;
         i = this.match(var14.value1);
         if(i == null) {
            this.visit(var14.value1);
         } else {
            var14.value1 = i;
         }

         i = this.match(var14.value2);
         if(i == null) {
            this.visit(var14.value2);
         } else {
            var14.value2 = i;
         }
         break;
      case 271:
         ArrayLoadInstruction var13 = (ArrayLoadInstruction)instruction;
         i = this.match(var13.arrayref);
         if(i == null) {
            this.visit(var13.arrayref);
         } else {
            var13.arrayref = i;
         }

         i = this.match(var13.indexref);
         if(i == null) {
            this.visit(var13.indexref);
         } else {
            var13.indexref = i;
         }
         break;
      case 273:
         ReturnInstruction var12 = (ReturnInstruction)instruction;
         i = this.match(var12.valueref);
         if(i == null) {
            this.visit(var12.valueref);
         } else {
            var12.valueref = i;
         }
         break;
      case 275:
      case 276:
         ConvertInstruction var10 = (ConvertInstruction)instruction;
         i = this.match(var10.value);
         if(i == null) {
            this.visit(var10.value);
         } else {
            var10.value = i;
         }
         break;
      case 280:
         TernaryOpStore var9 = (TernaryOpStore)instruction;
         i = this.match(var9.objectref);
         if(i == null) {
            this.visit(var9.objectref);
         } else {
            var9.objectref = i;
         }
         break;
      case 281:
         TernaryOperator var8 = (TernaryOperator)instruction;
         i = this.match(var8.value1);
         if(i == null) {
            this.visit(var8.value1);
         } else {
            var8.value1 = i;
         }

         i = this.match(var8.value2);
         if(i == null) {
            this.visit(var8.value2);
         } else {
            var8.value2 = i;
         }
         break;
      case 282:
      case 283:
         InitArrayInstruction var7 = (InitArrayInstruction)instruction;
         i = this.match(var7.newArray);
         if(i == null) {
            this.visit(var7.newArray);
         } else {
            var7.newArray = i;
         }

         for(index = var7.values.size() - 1; index >= 0; --index) {
            i = this.match((Instruction)var7.values.get(index));
            if(i == null) {
               this.visit((Instruction)var7.values.get(index));
            } else {
               var7.values.set(index, i);
            }
         }

         return;
      case 284:
         ComplexConditionalBranchInstruction var6 = (ComplexConditionalBranchInstruction)instruction;
         List var11 = var6.instructions;

         for(index = var11.size() - 1; index >= 0; --index) {
            this.visit((Instruction)var11.get(index));
         }

         return;
      case 286:
         AssertInstruction iaInstruction = (AssertInstruction)instruction;
         i = this.match(iaInstruction.test);
         if(i == null) {
            this.visit(iaInstruction.test);
         } else {
            iaInstruction.test = i;
         }

         if(iaInstruction.msg != null) {
            i = this.match(iaInstruction.msg);
            if(i == null) {
               this.visit(iaInstruction.msg);
            } else {
               iaInstruction.msg = i;
            }
         }
         break;
      default:
         System.err.println("Can not replace StringBuxxxer in " + instruction.getClass().getName() + " " + instruction.opcode);
      }

   }

   private void replaceInArgs(List<Instruction> args) {
      if(args.size() > 0) {
         for(int i = args.size() - 1; i >= 0; --i) {
            Instruction ins = this.match((Instruction)args.get(i));
            if(ins == null) {
               this.visit((Instruction)args.get(i));
            } else {
               args.set(i, ins);
            }
         }
      }

   }

   private Instruction match(Instruction i) {
      if(i.opcode == 182) {
         Invokevirtual iv = (Invokevirtual)i;
         ConstantMethodref cmr = this.constants.getConstantMethodref(iv.index);
         ConstantClass cc = this.constants.getConstantClass(cmr.class_index);
         if(cc.name_index == this.constants.stringBufferClassNameIndex || cc.name_index == this.constants.stringBuilderClassNameIndex) {
            ConstantNameAndType cnat = this.constants.getConstantNameAndType(cmr.name_and_type_index);
            if(cnat.name_index == this.constants.toStringIndex) {
               return this.match(iv.objectref, cmr.class_index);
            }
         }
      }

      return null;
   }

   private Instruction match(Instruction i, int classIndex) {
      ConstantMethodref cmr;
      if(i.opcode == 182) {
         InvokeNoStaticInstruction in = (InvokeNoStaticInstruction)i;
         cmr = this.constants.getConstantMethodref(in.index);
         if(cmr.class_index == classIndex) {
            ConstantNameAndType arg0 = this.constants.getConstantNameAndType(cmr.name_and_type_index);
            if(arg0.name_index == this.constants.appendIndex && in.args.size() == 1) {
               Instruction is = this.match(in.objectref, cmr.class_index);
               if(is == null) {
                  return (Instruction)in.args.get(0);
               }

               return new BinaryOperatorInstruction(267, i.offset, i.lineNumber, 4, "Ljava/lang/String;", "+", is, (Instruction)in.args.get(0));
            }
         }
      } else if(i.opcode == 274) {
         InvokeNew in1 = (InvokeNew)i;
         cmr = this.constants.getConstantMethodref(in1.index);
         if(cmr.class_index == classIndex && in1.args.size() == 1) {
            Instruction arg01 = (Instruction)in1.args.get(0);
            if(arg01.opcode == 184) {
               Invokestatic is1 = (Invokestatic)arg01;
               cmr = this.constants.getConstantMethodref(is1.index);
               ConstantClass cc = this.constants.getConstantClass(cmr.class_index);
               if(cc.name_index == this.constants.stringClassNameIndex) {
                  ConstantNameAndType cnat = this.constants.getConstantNameAndType(cmr.name_and_type_index);
                  if(cnat.name_index == this.constants.valueOfIndex && is1.args.size() == 1) {
                     return (Instruction)is1.args.get(0);
                  }
               }
            }

            return arg01;
         }
      }

      return null;
   }
}
