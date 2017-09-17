package jd.core.process.analyzer.classfile.visitor;

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

public class ReplaceDupLoadVisitor {
   private DupStore dupStore;
   private Instruction newInstruction;
   private Instruction parentFound;

   public ReplaceDupLoadVisitor() {
      this.dupStore = null;
      this.newInstruction = null;
      this.parentFound = null;
   }

   public ReplaceDupLoadVisitor(DupStore dupStore, Instruction newInstruction) {
      this.init(dupStore, newInstruction);
   }

   public void init(DupStore dupStore, Instruction newInstruction) {
      this.dupStore = dupStore;
      this.newInstruction = newInstruction;
      this.parentFound = null;
   }

   public void visit(Instruction instruction) {
      int i;
      int var10;
      List var16;
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
         StoreInstruction var47 = (StoreInstruction)instruction;
         if(this.match(var47, var47.valueref)) {
            var47.valueref = this.newInstruction;
         } else {
            this.visit(var47.valueref);
         }
         break;
      case 83:
      case 272:
         ArrayStoreInstruction var46 = (ArrayStoreInstruction)instruction;
         if(this.match(var46, var46.arrayref)) {
            var46.arrayref = this.newInstruction;
         } else {
            this.visit(var46.arrayref);
            if(this.parentFound == null) {
               if(this.match(var46, var46.indexref)) {
                  var46.indexref = this.newInstruction;
               } else {
                  this.visit(var46.indexref);
                  if(this.parentFound == null) {
                     if(this.match(var46, var46.valueref)) {
                        var46.valueref = this.newInstruction;
                     } else {
                        this.visit(var46.valueref);
                     }
                  }
               }
            }
         }
         break;
      case 87:
         Pop var45 = (Pop)instruction;
         if(this.match(var45, var45.objectref)) {
            var45.objectref = this.newInstruction;
         } else {
            this.visit(var45.objectref);
         }
         break;
      case 170:
         TableSwitch var44 = (TableSwitch)instruction;
         if(this.match(var44, var44.key)) {
            var44.key = this.newInstruction;
         } else {
            this.visit(var44.key);
         }
         break;
      case 171:
         LookupSwitch var43 = (LookupSwitch)instruction;
         if(this.match(var43, var43.key)) {
            var43.key = this.newInstruction;
         } else {
            this.visit(var43.key);
         }
         break;
      case 179:
         PutStatic var42 = (PutStatic)instruction;
         if(this.match(var42, var42.valueref)) {
            var42.valueref = this.newInstruction;
         } else {
            this.visit(var42.valueref);
         }
         break;
      case 180:
         GetField var41 = (GetField)instruction;
         if(this.match(var41, var41.objectref)) {
            var41.objectref = this.newInstruction;
         } else {
            this.visit(var41.objectref);
         }
         break;
      case 181:
         PutField var40 = (PutField)instruction;
         if(this.match(var40, var40.objectref)) {
            var40.objectref = this.newInstruction;
         } else {
            this.visit(var40.objectref);
            if(this.parentFound == null) {
               if(this.match(var40, var40.valueref)) {
                  var40.valueref = this.newInstruction;
               } else {
                  this.visit(var40.valueref);
               }
            }
         }
         break;
      case 182:
      case 183:
      case 185:
         InvokeNoStaticInstruction var39 = (InvokeNoStaticInstruction)instruction;
         if(this.match(var39, var39.objectref)) {
            var39.objectref = this.newInstruction;
         } else {
            this.visit(var39.objectref);
         }
      case 184:
      case 274:
         var16 = ((InvokeInstruction)instruction).args;

         for(var10 = var16.size() - 1; var10 >= 0 && this.parentFound == null; --var10) {
            if(this.match(instruction, (Instruction)var16.get(var10))) {
               var16.set(var10, this.newInstruction);
            } else {
               this.visit((Instruction)var16.get(var10));
            }
         }

         return;
      case 188:
         NewArray var38 = (NewArray)instruction;
         if(this.match(var38, var38.dimension)) {
            var38.dimension = this.newInstruction;
         } else {
            this.visit(var38.dimension);
         }
         break;
      case 189:
         ANewArray var37 = (ANewArray)instruction;
         if(this.match(var37, var37.dimension)) {
            var37.dimension = this.newInstruction;
         } else {
            this.visit(var37.dimension);
         }
         break;
      case 190:
         ArrayLength var36 = (ArrayLength)instruction;
         if(this.match(var36, var36.arrayref)) {
            var36.arrayref = this.newInstruction;
         } else {
            this.visit(var36.arrayref);
         }
         break;
      case 191:
         AThrow var35 = (AThrow)instruction;
         if(this.match(var35, var35.value)) {
            var35.value = this.newInstruction;
         } else {
            this.visit(var35.value);
         }
         break;
      case 192:
         CheckCast var34 = (CheckCast)instruction;
         if(this.match(var34, var34.objectref)) {
            var34.objectref = this.newInstruction;
         } else {
            this.visit(var34.objectref);
         }
         break;
      case 193:
         InstanceOf var33 = (InstanceOf)instruction;
         if(this.match(var33, var33.objectref)) {
            var33.objectref = this.newInstruction;
         } else {
            this.visit(var33.objectref);
         }
         break;
      case 194:
         MonitorEnter var32 = (MonitorEnter)instruction;
         if(this.match(var32, var32.objectref)) {
            var32.objectref = this.newInstruction;
         } else {
            this.visit(var32.objectref);
         }
         break;
      case 195:
         MonitorExit var31 = (MonitorExit)instruction;
         if(this.match(var31, var31.objectref)) {
            var31.objectref = this.newInstruction;
         } else {
            this.visit(var31.objectref);
         }
         break;
      case 197:
         Instruction[] var30 = ((MultiANewArray)instruction).dimensions;

         for(var10 = var30.length - 1; var10 >= 0 && this.parentFound == null; --var10) {
            if(this.match(instruction, var30[var10])) {
               var30[var10] = this.newInstruction;
            } else {
               this.visit(var30[var10]);
            }
         }

         return;
      case 260:
      case 262:
         IfInstruction var29 = (IfInstruction)instruction;
         if(this.match(var29, var29.value)) {
            var29.value = this.newInstruction;
         } else {
            this.visit(var29.value);
         }
         break;
      case 261:
         IfCmp var28 = (IfCmp)instruction;
         if(this.match(var28, var28.value1)) {
            var28.value1 = this.newInstruction;
         } else {
            this.visit(var28.value1);
            if(this.parentFound == null) {
               if(this.match(var28, var28.value2)) {
                  var28.value2 = this.newInstruction;
               } else {
                  this.visit(var28.value2);
               }
            }
         }
         break;
      case 264:
         DupStore var27 = (DupStore)instruction;
         if(this.match(var27, var27.objectref)) {
            var27.objectref = this.newInstruction;
         } else {
            this.visit(var27.objectref);
         }
         break;
      case 265:
         AssignmentInstruction var26 = (AssignmentInstruction)instruction;
         if(this.match(var26, var26.value1)) {
            var26.value1 = this.newInstruction;
         } else {
            this.visit(var26.value1);
            if(this.parentFound == null) {
               if(this.match(var26, var26.value2)) {
                  var26.value2 = this.newInstruction;
               } else {
                  this.visit(var26.value2);
               }
            }
         }
         break;
      case 266:
         UnaryOperatorInstruction var25 = (UnaryOperatorInstruction)instruction;
         if(this.match(var25, var25.value)) {
            var25.value = this.newInstruction;
         } else {
            this.visit(var25.value);
         }
         break;
      case 267:
         BinaryOperatorInstruction var24 = (BinaryOperatorInstruction)instruction;
         if(this.match(var24, var24.value1)) {
            var24.value1 = this.newInstruction;
         } else {
            this.visit(var24.value1);
            if(this.parentFound == null) {
               if(this.match(var24, var24.value2)) {
                  var24.value2 = this.newInstruction;
               } else {
                  this.visit(var24.value2);
               }
            }
         }
         break;
      case 271:
         ArrayLoadInstruction var23 = (ArrayLoadInstruction)instruction;
         if(this.match(var23, var23.arrayref)) {
            var23.arrayref = this.newInstruction;
         } else {
            this.visit(var23.arrayref);
            if(this.parentFound == null) {
               if(this.match(var23, var23.indexref)) {
                  var23.indexref = this.newInstruction;
               } else {
                  this.visit(var23.indexref);
               }
            }
         }
         break;
      case 273:
         ReturnInstruction var22 = (ReturnInstruction)instruction;
         if(this.match(var22, var22.valueref)) {
            var22.valueref = this.newInstruction;
         } else {
            this.visit(var22.valueref);
         }
         break;
      case 275:
      case 276:
         ConvertInstruction var21 = (ConvertInstruction)instruction;
         if(this.match(var21, var21.value)) {
            var21.value = this.newInstruction;
         } else {
            this.visit(var21.value);
         }
         break;
      case 277:
      case 278:
         IncInstruction var20 = (IncInstruction)instruction;
         if(this.match(var20, var20.value)) {
            var20.value = this.newInstruction;
         } else {
            this.visit(var20.value);
         }
         break;
      case 280:
         TernaryOpStore var19 = (TernaryOpStore)instruction;
         if(this.match(var19, var19.objectref)) {
            var19.objectref = this.newInstruction;
         } else {
            this.visit(var19.objectref);
         }
         break;
      case 281:
         TernaryOperator var18 = (TernaryOperator)instruction;
         if(this.match(var18, var18.value1)) {
            var18.value1 = this.newInstruction;
         } else {
            this.visit(var18.value1);
            if(this.parentFound == null) {
               if(this.match(var18, var18.value2)) {
                  var18.value2 = this.newInstruction;
               } else {
                  this.visit(var18.value2);
               }
            }
         }
         break;
      case 282:
      case 283:
         InitArrayInstruction var17 = (InitArrayInstruction)instruction;
         if(this.match(var17, var17.newArray)) {
            var17.newArray = this.newInstruction;
         } else {
            this.visit(var17.newArray);
            if(this.parentFound == null && var17.values != null) {
               this.visit(var17.values);
            }
         }
         break;
      case 284:
         var16 = ((ComplexConditionalBranchInstruction)instruction).instructions;

         for(var10 = var16.size() - 1; var10 >= 0 && this.parentFound == null; --var10) {
            this.visit((Instruction)var16.get(var10));
         }

         return;
      case 304:
         FastFor var14 = (FastFor)instruction;
         if(var14.init != null) {
            if(this.match(var14, var14.init)) {
               var14.init = this.newInstruction;
            } else {
               this.visit(var14.init);
            }
         }

         if(this.parentFound == null && var14.inc != null) {
            if(this.match(var14, var14.inc)) {
               var14.inc = this.newInstruction;
            } else {
               this.visit(var14.inc);
            }
         }
      case 301:
      case 302:
      case 306:
         FastTestList var15 = (FastTestList)instruction;
         if(var15.test != null) {
            if(this.match(var15, var15.test)) {
               var15.test = this.newInstruction;
            } else {
               this.visit(var15.test);
            }
         }
      case 303:
         var16 = ((FastList)instruction).instructions;
         if(var16 != null) {
            this.visit(var16);
         }
         break;
      case 305:
         FastForEach var13 = (FastForEach)instruction;
         if(this.match(var13, var13.variable)) {
            var13.variable = this.newInstruction;
         } else {
            this.visit(var13.variable);
            if(this.parentFound == null) {
               if(this.match(var13, var13.values)) {
                  var13.values = this.newInstruction;
               } else {
                  this.visit(var13.values);
                  if(this.parentFound == null) {
                     this.visit(var13.instructions);
                  }
               }
            }
         }
         break;
      case 307:
         FastTest2Lists var12 = (FastTest2Lists)instruction;
         if(this.match(var12, var12.test)) {
            var12.test = this.newInstruction;
         } else {
            this.visit(var12.test);
            if(this.parentFound == null) {
               this.visit(var12.instructions);
               if(this.parentFound == null) {
                  this.visit(var12.instructions2);
               }
            }
         }
         break;
      case 308:
      case 309:
      case 310:
      case 311:
      case 312:
      case 313:
         FastInstruction var11 = (FastInstruction)instruction;
         if(var11.instruction != null) {
            if(this.match(var11, var11.instruction)) {
               var11.instruction = this.newInstruction;
            } else {
               this.visit(var11.instruction);
            }
         }
         break;
      case 314:
      case 315:
      case 316:
         FastSwitch var8 = (FastSwitch)instruction;
         if(this.match(var8, var8.test)) {
            var8.test = this.newInstruction;
            break;
         } else {
            this.visit(var8.test);
            FastSwitch.Pair[] var9 = var8.pairs;

            for(i = var9.length - 1; i >= 0 && this.parentFound == null; --i) {
               this.visit(var9[i].getInstructions());
            }

            return;
         }
      case 317:
         FastDeclaration var7 = (FastDeclaration)instruction;
         if(var7.instruction != null) {
            if(this.match(var7, var7.instruction)) {
               var7.instruction = this.newInstruction;
            } else {
               this.visit(var7.instruction);
            }
         }
         break;
      case 318:
         FastTry var6 = (FastTry)instruction;
         this.visit(var6.instructions);
         if(this.parentFound == null) {
            if(var6.finallyInstructions != null) {
               this.visit(var6.finallyInstructions);
            }

            List catchs = var6.catches;

            for(i = catchs.size() - 1; i >= 0 && this.parentFound == null; --i) {
               this.visit(((FastTry.FastCatch)catchs.get(i)).instructions);
            }
         }
         break;
      case 319:
         FastSynchronized var5 = (FastSynchronized)instruction;
         if(this.match(var5, var5.monitor)) {
            var5.monitor = this.newInstruction;
         } else {
            this.visit(var5.monitor);
            if(this.parentFound == null) {
               this.visit(var5.instructions);
            }
         }
         break;
      case 320:
         FastLabel fd = (FastLabel)instruction;
         if(fd.instruction != null) {
            if(this.match(fd, fd.instruction)) {
               fd.instruction = this.newInstruction;
            } else {
               this.visit(fd.instruction);
            }
         }
         break;
      default:
         System.err.println("Can not replace DupLoad in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
      }

   }

   private void visit(List<Instruction> instructions) {
      for(int i = instructions.size() - 1; i >= 0; --i) {
         this.visit((Instruction)instructions.get(i));
      }

   }

   public Instruction getParentFound() {
      return this.parentFound;
   }

   private boolean match(Instruction parent, Instruction i) {
      if(i.opcode != 263) {
         return false;
      } else {
         DupLoad dupload = (DupLoad)i;
         if(dupload.dupStore == this.dupStore) {
            this.parentFound = parent;
            return true;
         } else {
            return false;
         }
      }
   }
}
