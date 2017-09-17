package jd.core.process.analyzer.classfile.visitor;

import java.util.List;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.constant.Constant;
import jd.core.model.classfile.constant.ConstantClass;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantMethodref;
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
import jd.core.model.instruction.bytecode.instruction.New;
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
import jd.core.model.reference.ReferenceMap;
import jd.core.util.SignatureUtil;

public class ReferenceVisitor {
   private ConstantPool constants;
   private ReferenceMap referenceMap;

   public ReferenceVisitor(ConstantPool constants, ReferenceMap referenceMap) {
      this.constants = constants;
      this.referenceMap = referenceMap;
   }

   public void visit(Instruction instruction) {
      if(instruction != null) {
         String internalName;
         int cc;
         int var31;
         StoreInstruction var39;
         switch(instruction.opcode) {
         case 0:
         case 1:
         case 16:
         case 17:
         case 20:
         case 21:
         case 25:
         case 132:
         case 167:
         case 168:
         case 169:
         case 177:
         case 256:
         case 257:
         case 258:
         case 259:
         case 263:
         case 268:
         case 270:
         case 271:
         case 277:
         case 278:
         case 279:
            break;
         case 18:
            IndexInstruction var57 = (IndexInstruction)instruction;
            Constant var48 = this.constants.get(var57.index);
            if(var48.tag == 7) {
               ConstantClass var58 = (ConstantClass)var48;
               internalName = this.constants.getConstantUtf8(var58.name_index);
               this.addReference(internalName);
            }
            break;
         case 54:
         case 269:
            var39 = (StoreInstruction)instruction;
            this.visit(var39.valueref);
            break;
         case 58:
            var39 = (StoreInstruction)instruction;
            this.visit(var39.valueref);
            break;
         case 83:
         case 272:
            ArrayStoreInstruction var36 = (ArrayStoreInstruction)instruction;
            this.visit(var36.valueref);
            break;
         case 87:
            Pop var55 = (Pop)instruction;
            this.visit(var55.objectref);
            break;
         case 170:
            TableSwitch var53 = (TableSwitch)instruction;
            this.visit(var53.key);
            break;
         case 171:
            LookupSwitch var51 = (LookupSwitch)instruction;
            this.visit(var51.key);
            break;
         case 179:
            PutStatic var49 = (PutStatic)instruction;
            this.visit(var49.valueref);
            break;
         case 180:
            GetField var45 = (GetField)instruction;
            this.visit(var45.objectref);
         case 178:
         case 285:
            IndexInstruction var28 = (IndexInstruction)instruction;
            ConstantFieldref var40 = this.constants.getConstantFieldref(var28.index);
            internalName = this.constants.getConstantClassName(var40.class_index);
            this.addReference(internalName);
            break;
         case 181:
            PutField var43 = (PutField)instruction;
            this.visit(var43.objectref);
            this.visit(var43.valueref);
            break;
         case 182:
         case 183:
         case 185:
            InvokeNoStaticInstruction var34 = (InvokeNoStaticInstruction)instruction;
            this.visit(var34.objectref);
         case 184:
         case 274:
            InvokeInstruction var41 = (InvokeInstruction)instruction;
            ConstantMethodref var25 = this.constants.getConstantMethodref(var41.index);
            internalName = this.constants.getConstantClassName(var25.class_index);
            this.addReference(internalName);
            this.visit(var41.args);
            break;
         case 187:
            New var37 = (New)instruction;
            this.addReference(this.constants.getConstantClassName(var37.index));
            break;
         case 188:
            NewArray var35 = (NewArray)instruction;
            this.visit(var35.dimension);
            break;
         case 189:
            ANewArray var32 = (ANewArray)instruction;
            this.addReference(this.constants.getConstantClassName(var32.index));
            this.visit(var32.dimension);
            break;
         case 190:
            ArrayLength var33 = (ArrayLength)instruction;
            this.visit(var33.arrayref);
            break;
         case 191:
            AThrow var29 = (AThrow)instruction;
            this.visit(var29.value);
            break;
         case 192:
            CheckCast var26 = (CheckCast)instruction;
            this.visitCheckCastAndMultiANewArray(var26.index);
            this.visit(var26.objectref);
            break;
         case 193:
            InstanceOf var22 = (InstanceOf)instruction;
            this.visitCheckCastAndMultiANewArray(var22.index);
            this.visit(var22.objectref);
            break;
         case 194:
            MonitorEnter var30 = (MonitorEnter)instruction;
            this.visit(var30.objectref);
            break;
         case 195:
            MonitorExit var27 = (MonitorExit)instruction;
            this.visit(var27.objectref);
            break;
         case 197:
            MultiANewArray var23 = (MultiANewArray)instruction;
            this.visitCheckCastAndMultiANewArray(var23.index);
            Instruction[] var24 = var23.dimensions;

            for(var31 = var24.length - 1; var31 >= 0; --var31) {
               this.visit(var24[var31]);
            }

            return;
         case 260:
         case 262:
            IfInstruction var19 = (IfInstruction)instruction;
            this.visit(var19.value);
            break;
         case 261:
            IfCmp var17 = (IfCmp)instruction;
            this.visit(var17.value1);
            this.visit(var17.value2);
            break;
         case 264:
            DupStore var15 = (DupStore)instruction;
            this.visit(var15.objectref);
            break;
         case 265:
         case 267:
            BinaryOperatorInstruction var14 = (BinaryOperatorInstruction)instruction;
            this.visit(var14.value1);
            this.visit(var14.value2);
            break;
         case 266:
            UnaryOperatorInstruction var13 = (UnaryOperatorInstruction)instruction;
            this.visit(var13.value);
            break;
         case 273:
            ReturnInstruction var20 = (ReturnInstruction)instruction;
            this.visit(var20.valueref);
            break;
         case 275:
         case 276:
            ConvertInstruction var12 = (ConvertInstruction)instruction;
            this.visit(var12.value);
            break;
         case 280:
            TernaryOpStore var18 = (TernaryOpStore)instruction;
            this.visit(var18.objectref);
            break;
         case 281:
            TernaryOperator var16 = (TernaryOperator)instruction;
            this.visit(var16.test);
            this.visit(var16.value1);
            this.visit(var16.value2);
            break;
         case 282:
         case 283:
            InitArrayInstruction var21 = (InitArrayInstruction)instruction;
            this.visit(var21.newArray);

            for(var31 = var21.values.size() - 1; var31 >= 0; --var31) {
               this.visit((Instruction)var21.values.get(var31));
            }

            return;
         case 284:
            List var11 = ((ComplexConditionalBranchInstruction)instruction).instructions;

            for(int getField = var11.size() - 1; getField >= 0; --getField) {
               this.visit((Instruction)var11.get(getField));
            }

            return;
         case 286:
            AssertInstruction insi = (AssertInstruction)instruction;
            this.visit(insi.test);
            this.visit(insi.msg);
            break;
         case 304:
            FastFor ff = (FastFor)instruction;
            this.visit(ff.init);
            this.visit(ff.inc);
         case 301:
         case 302:
         case 306:
            FastTestList ftl = (FastTestList)instruction;
            this.visit(ftl.test);
         case 303:
            List var56 = ((FastList)instruction).instructions;
            this.visit(var56);
            break;
         case 305:
            FastForEach var54 = (FastForEach)instruction;
            this.visit(var54.variable);
            this.visit(var54.values);
            this.visit(var54.instructions);
            break;
         case 307:
            FastTest2Lists var52 = (FastTest2Lists)instruction;
            this.visit(var52.test);
            this.visit(var52.instructions);
            this.visit(var52.instructions2);
            break;
         case 308:
         case 309:
         case 310:
         case 311:
         case 312:
         case 313:
            FastInstruction var50 = (FastInstruction)instruction;
            this.visit(var50.instruction);
            break;
         case 314:
         case 315:
         case 316:
            FastSwitch var46 = (FastSwitch)instruction;
            this.visit(var46.test);
            FastSwitch.Pair[] var47 = var46.pairs;

            for(cc = var47.length - 1; cc >= 0; --cc) {
               List instructions = var47[cc].getInstructions();
               this.visit(instructions);
            }

            return;
         case 317:
            FastDeclaration var44 = (FastDeclaration)instruction;
            this.visit(var44.instruction);
            break;
         case 318:
            FastTry var42 = (FastTry)instruction;
            this.visit(var42.instructions);
            List cst = var42.catches;

            for(cc = cst.size() - 1; cc >= 0; --cc) {
               this.visit(((FastTry.FastCatch)cst.get(cc)).instructions);
            }

            this.visit(var42.finallyInstructions);
            break;
         case 319:
            FastSynchronized var38 = (FastSynchronized)instruction;
            this.visit(var38.monitor);
            this.visit(var38.instructions);
            break;
         case 320:
            FastLabel indexInstruction = (FastLabel)instruction;
            this.visit(indexInstruction.instruction);
            break;
         default:
            System.err.println("Can not count reference in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
         }

      }
   }

   private void visit(List<Instruction> instructions) {
      if(instructions != null) {
         for(int i = instructions.size() - 1; i >= 0; --i) {
            this.visit((Instruction)instructions.get(i));
         }
      }

   }

   private void visitCheckCastAndMultiANewArray(int index) {
      Constant c = this.constants.get(index);
      if(c.tag == 7) {
         this.addReference(this.constants.getConstantUtf8(((ConstantClass)c).name_index));
      }

   }

   private void addReference(String signature) {
      if(signature.charAt(0) == 91) {
         signature = SignatureUtil.CutArrayDimensionPrefix(signature);
         if(signature.charAt(0) == 76) {
            this.referenceMap.add(SignatureUtil.GetInnerName(signature));
         }
      } else {
         this.referenceMap.add(signature);
      }

   }
}
