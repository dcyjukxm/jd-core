package jd.core.process.analyzer.instruction.fast.visitor;

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
import jd.core.model.instruction.fast.instruction.FastSynchronized;
import jd.core.model.instruction.fast.instruction.FastTestList;
import jd.core.model.instruction.fast.instruction.FastTry;

public class ReplaceInstructionVisitor {
   private int offset;
   private Instruction newInstruction;
   private Instruction oldInstruction;

   public ReplaceInstructionVisitor(int offset, Instruction newInstruction) {
      this.init(offset, newInstruction);
   }

   public void init(int offset, Instruction newInstruction) {
      this.offset = offset;
      this.newInstruction = newInstruction;
      this.oldInstruction = null;
   }

   public void visit(Instruction instruction) {
      int i;
      Instruction instuction;
      List var9;
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
         StoreInstruction var38 = (StoreInstruction)instruction;
         if(var38.valueref.offset == this.offset) {
            this.oldInstruction = var38.valueref;
            var38.valueref = this.newInstruction;
         } else {
            this.visit(var38.valueref);
         }
         break;
      case 83:
      case 272:
         ArrayStoreInstruction var37 = (ArrayStoreInstruction)instruction;
         if(var37.arrayref.offset == this.offset) {
            this.oldInstruction = var37.arrayref;
            var37.arrayref = this.newInstruction;
         } else {
            this.visit(var37.arrayref);
            if(this.oldInstruction == null) {
               if(var37.indexref.offset == this.offset) {
                  this.oldInstruction = var37.indexref;
                  var37.indexref = this.newInstruction;
               } else {
                  this.visit(var37.indexref);
                  if(this.oldInstruction == null) {
                     if(var37.valueref.offset == this.offset) {
                        this.oldInstruction = var37.valueref;
                        var37.valueref = this.newInstruction;
                     } else {
                        this.visit(var37.valueref);
                     }
                  }
               }
            }
         }
         break;
      case 87:
         Pop var36 = (Pop)instruction;
         if(var36.objectref.offset == this.offset) {
            this.oldInstruction = var36.objectref;
            var36.objectref = this.newInstruction;
         } else {
            this.visit(var36.objectref);
         }
         break;
      case 170:
         TableSwitch var35 = (TableSwitch)instruction;
         if(var35.key.offset == this.offset) {
            this.oldInstruction = var35.key;
            var35.key = this.newInstruction;
         } else {
            this.visit(var35.key);
         }
         break;
      case 171:
         LookupSwitch var34 = (LookupSwitch)instruction;
         if(var34.key.offset == this.offset) {
            this.oldInstruction = var34.key;
            var34.key = this.newInstruction;
         } else {
            this.visit(var34.key);
         }
         break;
      case 179:
         PutStatic var33 = (PutStatic)instruction;
         if(var33.valueref.offset == this.offset) {
            this.oldInstruction = var33.valueref;
            var33.valueref = this.newInstruction;
         } else {
            this.visit(var33.valueref);
         }
         break;
      case 180:
         GetField var32 = (GetField)instruction;
         if(var32.objectref.offset == this.offset) {
            this.oldInstruction = var32.objectref;
            var32.objectref = this.newInstruction;
         } else {
            this.visit(var32.objectref);
         }
         break;
      case 181:
         PutField var31 = (PutField)instruction;
         if(var31.objectref.offset == this.offset) {
            this.oldInstruction = var31.objectref;
            var31.objectref = this.newInstruction;
         } else {
            this.visit(var31.objectref);
            if(this.oldInstruction == null) {
               if(var31.valueref.offset == this.offset) {
                  this.oldInstruction = var31.valueref;
                  var31.valueref = this.newInstruction;
               } else {
                  this.visit(var31.valueref);
               }
            }
         }
         break;
      case 182:
      case 183:
      case 185:
         InvokeNoStaticInstruction var30 = (InvokeNoStaticInstruction)instruction;
         if(var30.objectref.offset == this.offset) {
            this.oldInstruction = var30.objectref;
            var30.objectref = this.newInstruction;
         } else {
            this.visit(var30.objectref);
         }
      case 184:
         var9 = ((InvokeInstruction)instruction).args;

         for(i = var9.size() - 1; i >= 0 && this.oldInstruction == null; --i) {
            instuction = (Instruction)var9.get(i);
            if(instuction.offset == this.offset) {
               this.oldInstruction = instuction;
               var9.set(i, this.newInstruction);
            } else {
               this.visit(instuction);
            }
         }

         return;
      case 188:
         NewArray var29 = (NewArray)instruction;
         if(var29.dimension.offset == this.offset) {
            this.oldInstruction = var29.dimension;
            var29.dimension = this.newInstruction;
         } else {
            this.visit(var29.dimension);
         }
         break;
      case 189:
         ANewArray var28 = (ANewArray)instruction;
         if(var28.dimension.offset == this.offset) {
            this.oldInstruction = var28.dimension;
            var28.dimension = this.newInstruction;
         } else {
            this.visit(var28.dimension);
         }
         break;
      case 190:
         ArrayLength var27 = (ArrayLength)instruction;
         if(var27.arrayref.offset == this.offset) {
            this.oldInstruction = var27.arrayref;
            var27.arrayref = this.newInstruction;
         } else {
            this.visit(var27.arrayref);
         }
         break;
      case 191:
         AThrow var26 = (AThrow)instruction;
         if(var26.value.offset == this.offset) {
            this.oldInstruction = var26.value;
            var26.value = this.newInstruction;
         } else {
            this.visit(var26.value);
         }
         break;
      case 192:
         CheckCast var25 = (CheckCast)instruction;
         if(var25.objectref.offset == this.offset) {
            this.oldInstruction = var25.objectref;
            var25.objectref = this.newInstruction;
         } else {
            this.visit(var25.objectref);
         }
         break;
      case 193:
         InstanceOf var24 = (InstanceOf)instruction;
         if(var24.objectref.offset == this.offset) {
            this.oldInstruction = var24.objectref;
            var24.objectref = this.newInstruction;
         } else {
            this.visit(var24.objectref);
         }
         break;
      case 194:
         MonitorEnter var23 = (MonitorEnter)instruction;
         if(var23.objectref.offset == this.offset) {
            this.oldInstruction = var23.objectref;
            var23.objectref = this.newInstruction;
         } else {
            this.visit(var23.objectref);
         }
         break;
      case 195:
         MonitorExit var22 = (MonitorExit)instruction;
         if(var22.objectref.offset == this.offset) {
            this.oldInstruction = var22.objectref;
            var22.objectref = this.newInstruction;
         } else {
            this.visit(var22.objectref);
         }
         break;
      case 197:
         Instruction[] var21 = ((MultiANewArray)instruction).dimensions;

         for(i = var21.length - 1; i >= 0 && this.oldInstruction == null; --i) {
            if(var21[i].offset == this.offset) {
               this.oldInstruction = var21[i];
               var21[i] = this.newInstruction;
            } else {
               this.visit(var21[i]);
            }
         }

         return;
      case 260:
      case 262:
         IfInstruction var20 = (IfInstruction)instruction;
         if(var20.value.offset == this.offset) {
            this.oldInstruction = var20.value;
            var20.value = this.newInstruction;
         } else {
            this.visit(var20.value);
         }
         break;
      case 261:
         IfCmp var19 = (IfCmp)instruction;
         if(var19.value1.offset == this.offset) {
            this.oldInstruction = var19.value1;
            var19.value1 = this.newInstruction;
         } else {
            this.visit(var19.value1);
            if(this.oldInstruction == null) {
               if(var19.value2.offset == this.offset) {
                  this.oldInstruction = var19.value2;
                  var19.value2 = this.newInstruction;
               } else {
                  this.visit(var19.value2);
               }
            }
         }
         break;
      case 264:
         DupStore var18 = (DupStore)instruction;
         if(var18.objectref.offset == this.offset) {
            this.oldInstruction = var18.objectref;
            var18.objectref = this.newInstruction;
         } else {
            this.visit(var18.objectref);
         }
         break;
      case 265:
      case 267:
         BinaryOperatorInstruction var17 = (BinaryOperatorInstruction)instruction;
         if(var17.value1.offset == this.offset) {
            this.oldInstruction = var17.value1;
            var17.value1 = this.newInstruction;
         } else {
            this.visit(var17.value1);
            if(this.oldInstruction == null) {
               if(var17.value2.offset == this.offset) {
                  this.oldInstruction = var17.value2;
                  var17.value2 = this.newInstruction;
               } else {
                  this.visit(var17.value2);
               }
            }
         }
         break;
      case 266:
         UnaryOperatorInstruction var16 = (UnaryOperatorInstruction)instruction;
         if(var16.value.offset == this.offset) {
            this.oldInstruction = var16.value;
            var16.value = this.newInstruction;
         } else {
            this.visit(var16.value);
         }
         break;
      case 273:
         ReturnInstruction var15 = (ReturnInstruction)instruction;
         if(var15.valueref.offset == this.offset) {
            this.oldInstruction = var15.valueref;
            var15.valueref = this.newInstruction;
         } else {
            this.visit(var15.valueref);
         }
         break;
      case 274:
         var9 = ((InvokeNew)instruction).args;

         for(i = var9.size() - 1; i >= 0 && this.oldInstruction == null; --i) {
            instuction = (Instruction)var9.get(i);
            if(instuction.offset == this.offset) {
               this.oldInstruction = instuction;
               var9.set(i, this.newInstruction);
            } else {
               this.visit(instuction);
            }
         }

         return;
      case 275:
      case 276:
         ConvertInstruction var14 = (ConvertInstruction)instruction;
         if(var14.value.offset == this.offset) {
            this.oldInstruction = var14.value;
            var14.value = this.newInstruction;
         } else {
            this.visit(var14.value);
         }
         break;
      case 277:
      case 278:
         IncInstruction var13 = (IncInstruction)instruction;
         if(var13.value.offset == this.offset) {
            this.oldInstruction = var13.value;
            var13.value = this.newInstruction;
         } else {
            this.visit(var13.value);
         }
         break;
      case 280:
         TernaryOpStore var12 = (TernaryOpStore)instruction;
         if(var12.objectref.offset == this.offset) {
            this.oldInstruction = var12.objectref;
            var12.objectref = this.newInstruction;
         } else {
            this.visit(var12.objectref);
         }
         break;
      case 281:
         TernaryOperator var11 = (TernaryOperator)instruction;
         if(var11.test.offset == this.offset) {
            this.oldInstruction = var11.test;
            var11.test = this.newInstruction;
         } else {
            this.visit(var11.test);
            if(this.oldInstruction == null) {
               if(var11.value1.offset == this.offset) {
                  this.oldInstruction = var11.value1;
                  var11.value1 = this.newInstruction;
               } else {
                  this.visit(var11.value1);
                  if(this.oldInstruction == null) {
                     if(var11.value2.offset == this.offset) {
                        this.oldInstruction = var11.value2;
                        var11.value2 = this.newInstruction;
                     } else {
                        this.visit(var11.value2);
                     }
                  }
               }
            }
         }
         break;
      case 282:
      case 283:
         InitArrayInstruction var10 = (InitArrayInstruction)instruction;
         if(var10.newArray.offset == this.offset) {
            this.oldInstruction = var10.newArray;
            var10.newArray = this.newInstruction;
         } else {
            this.visit(var10.newArray);
            if(var10.values != null) {
               this.visit(var10.values);
            }
         }
         break;
      case 284:
         var9 = ((ComplexConditionalBranchInstruction)instruction).instructions;

         for(i = var9.size() - 1; i >= 0; --i) {
            this.visit((Instruction)var9.get(i));
         }

         return;
      case 286:
         AssertInstruction var8 = (AssertInstruction)instruction;
         if(var8.test.offset == this.offset) {
            this.oldInstruction = var8.test;
            var8.test = this.newInstruction;
         } else {
            this.visit(var8.test);
            if(this.oldInstruction == null && var8.msg != null) {
               if(var8.msg.offset == this.offset) {
                  this.oldInstruction = var8.msg;
                  var8.msg = this.newInstruction;
               } else {
                  this.visit(var8.msg);
               }
            }
         }
         break;
      case 306:
         FastTestList var7 = (FastTestList)instruction;
         if(var7.test.offset == this.offset) {
            this.oldInstruction = var7.test;
            var7.test = this.newInstruction;
         } else {
            this.visit(var7.test);
            if(this.oldInstruction == null && var7.instructions != null) {
               this.visit(var7.instructions);
            }
         }
         break;
      case 317:
         FastDeclaration var6 = (FastDeclaration)instruction;
         if(var6.instruction != null) {
            if(var6.instruction.offset == this.offset) {
               this.oldInstruction = var6.instruction;
               var6.instruction = this.newInstruction;
            } else {
               this.visit(var6.instruction);
            }
         }
         break;
      case 318:
         FastTry var5 = (FastTry)instruction;
         this.visit(var5.instructions);
         if(this.oldInstruction == null) {
            if(var5.finallyInstructions != null) {
               this.visit(var5.finallyInstructions);
            }

            for(i = var5.catches.size() - 1; i >= 0 && this.oldInstruction == null; --i) {
               this.visit(((FastTry.FastCatch)var5.catches.get(i)).instructions);
            }
         }
         break;
      case 319:
         FastSynchronized ftl = (FastSynchronized)instruction;
         if(ftl.monitor.offset == this.offset) {
            this.oldInstruction = ftl.monitor;
            ftl.monitor = this.newInstruction;
         } else {
            this.visit(ftl.monitor);
            if(this.oldInstruction == null) {
               this.visit(ftl.instructions);
            }
         }
         break;
      default:
         System.err.println("Can not replace code in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
      }

   }

   private void visit(List<Instruction> instructions) {
      for(int i = instructions.size() - 1; i >= 0; --i) {
         this.visit((Instruction)instructions.get(i));
      }

   }

   public Instruction getOldInstruction() {
      return this.oldInstruction;
   }
}
