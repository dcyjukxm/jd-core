package jd.core.process.analyzer.classfile.visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.accessor.Accessor;
import jd.core.model.classfile.accessor.GetStaticAccessor;
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
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
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

public class OuterGetStaticVisitor {
   protected Map<String, ClassFile> innerClassesMap;
   protected ConstantPool constants;

   public OuterGetStaticVisitor(HashMap<String, ClassFile> innerClassesMap, ConstantPool constants) {
      this.innerClassesMap = innerClassesMap;
      this.constants = constants;
   }

   public void visit(Instruction instruction) {
      Accessor a;
      Accessor a1;
      List var5;
      int var10;
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
         StoreInstruction var37 = (StoreInstruction)instruction;
         a = this.match(var37.valueref);
         if(a != null) {
            var37.valueref = this.newInstruction(var37.valueref, a);
         } else {
            this.visit(var37.valueref);
         }
         break;
      case 83:
      case 272:
         ArrayStoreInstruction var36 = (ArrayStoreInstruction)instruction;
         a = this.match(var36.arrayref);
         if(a != null) {
            var36.arrayref = this.newInstruction(var36.arrayref, a);
         } else {
            this.visit(var36.arrayref);
         }

         a = this.match(var36.indexref);
         if(a != null) {
            var36.indexref = this.newInstruction(var36.indexref, a);
         } else {
            this.visit(var36.indexref);
         }

         a = this.match(var36.valueref);
         if(a != null) {
            var36.valueref = this.newInstruction(var36.valueref, a);
         } else {
            this.visit(var36.valueref);
         }
         break;
      case 87:
         Pop var35 = (Pop)instruction;
         a = this.match(var35.objectref);
         if(a != null) {
            var35.objectref = this.newInstruction(var35.objectref, a);
         } else {
            this.visit(var35.objectref);
         }
         break;
      case 170:
         TableSwitch var34 = (TableSwitch)instruction;
         a = this.match(var34.key);
         if(a != null) {
            var34.key = this.newInstruction(var34.key, a);
         } else {
            this.visit(var34.key);
         }
         break;
      case 171:
         LookupSwitch var33 = (LookupSwitch)instruction;
         a = this.match(var33.key);
         if(a != null) {
            var33.key = this.newInstruction(var33.key, a);
         } else {
            this.visit(var33.key);
         }
         break;
      case 179:
         PutStatic var32 = (PutStatic)instruction;
         a = this.match(var32.valueref);
         if(a != null) {
            var32.valueref = this.newInstruction(var32.valueref, a);
         } else {
            this.visit(var32.valueref);
         }
         break;
      case 180:
         GetField var31 = (GetField)instruction;
         a = this.match(var31.objectref);
         if(a != null) {
            var31.objectref = this.newInstruction(var31.objectref, a);
         } else {
            this.visit(var31.objectref);
         }
         break;
      case 181:
         PutField var30 = (PutField)instruction;
         a = this.match(var30.objectref);
         if(a != null) {
            var30.objectref = this.newInstruction(var30.objectref, a);
         } else {
            this.visit(var30.objectref);
         }

         a = this.match(var30.valueref);
         if(a != null) {
            var30.valueref = this.newInstruction(var30.valueref, a);
         } else {
            this.visit(var30.valueref);
         }
         break;
      case 182:
      case 183:
      case 185:
         InvokeNoStaticInstruction var29 = (InvokeNoStaticInstruction)instruction;
         a = this.match(var29.objectref);
         if(a != null) {
            var29.objectref = this.newInstruction(var29.objectref, a);
         } else {
            this.visit(var29.objectref);
         }
      case 184:
      case 274:
         var5 = ((InvokeInstruction)instruction).args;

         for(var10 = var5.size() - 1; var10 >= 0; --var10) {
            a1 = this.match((Instruction)var5.get(var10));
            if(a1 != null) {
               var5.set(var10, this.newInstruction((Instruction)var5.get(var10), a1));
            } else {
               this.visit((Instruction)var5.get(var10));
            }
         }

         return;
      case 188:
         NewArray var28 = (NewArray)instruction;
         a = this.match(var28.dimension);
         if(a != null) {
            var28.dimension = this.newInstruction(var28.dimension, a);
         } else {
            this.visit(var28.dimension);
         }
         break;
      case 189:
         ANewArray var27 = (ANewArray)instruction;
         a = this.match(var27.dimension);
         if(a != null) {
            var27.dimension = this.newInstruction(var27.dimension, a);
         } else {
            this.visit(var27.dimension);
         }
         break;
      case 190:
         ArrayLength var26 = (ArrayLength)instruction;
         a = this.match(var26.arrayref);
         if(a != null) {
            var26.arrayref = this.newInstruction(var26.arrayref, a);
         } else {
            this.visit(var26.arrayref);
         }
         break;
      case 191:
         AThrow var25 = (AThrow)instruction;
         a = this.match(var25.value);
         if(a != null) {
            var25.value = this.newInstruction(var25.value, a);
         } else {
            this.visit(var25.value);
         }
         break;
      case 192:
         CheckCast var24 = (CheckCast)instruction;
         a = this.match(var24.objectref);
         if(a != null) {
            var24.objectref = this.newInstruction(var24.objectref, a);
         } else {
            this.visit(var24.objectref);
         }
         break;
      case 193:
         InstanceOf var23 = (InstanceOf)instruction;
         a = this.match(var23.objectref);
         if(a != null) {
            var23.objectref = this.newInstruction(var23.objectref, a);
         } else {
            this.visit(var23.objectref);
         }
         break;
      case 194:
         MonitorEnter var22 = (MonitorEnter)instruction;
         a = this.match(var22.objectref);
         if(a != null) {
            var22.objectref = this.newInstruction(var22.objectref, a);
         } else {
            this.visit(var22.objectref);
         }
         break;
      case 195:
         MonitorExit var21 = (MonitorExit)instruction;
         a = this.match(var21.objectref);
         if(a != null) {
            var21.objectref = this.newInstruction(var21.objectref, a);
         } else {
            this.visit(var21.objectref);
         }
         break;
      case 197:
         Instruction[] var20 = ((MultiANewArray)instruction).dimensions;

         for(var10 = var20.length - 1; var10 >= 0; --var10) {
            a1 = this.match(var20[var10]);
            if(a1 != null) {
               var20[var10] = this.newInstruction(var20[var10], a1);
            } else {
               this.visit(var20[var10]);
            }
         }

         return;
      case 260:
      case 262:
         IfInstruction var19 = (IfInstruction)instruction;
         a = this.match(var19.value);
         if(a != null) {
            var19.value = this.newInstruction(var19.value, a);
         } else {
            this.visit(var19.value);
         }
         break;
      case 261:
         IfCmp var18 = (IfCmp)instruction;
         a = this.match(var18.value1);
         if(a != null) {
            var18.value1 = this.newInstruction(var18.value1, a);
         } else {
            this.visit(var18.value1);
         }

         a = this.match(var18.value2);
         if(a != null) {
            var18.value2 = this.newInstruction(var18.value2, a);
         } else {
            this.visit(var18.value2);
         }
         break;
      case 264:
         DupStore var17 = (DupStore)instruction;
         a = this.match(var17.objectref);
         if(a != null) {
            var17.objectref = this.newInstruction(var17.objectref, a);
         } else {
            this.visit(var17.objectref);
         }
         break;
      case 265:
         AssignmentInstruction var16 = (AssignmentInstruction)instruction;
         a = this.match(var16.value1);
         if(a != null) {
            var16.value1 = this.newInstruction(var16.value1, a);
         } else {
            this.visit(var16.value1);
         }

         a = this.match(var16.value2);
         if(a != null) {
            var16.value2 = this.newInstruction(var16.value2, a);
         } else {
            this.visit(var16.value2);
         }
         break;
      case 266:
         UnaryOperatorInstruction var15 = (UnaryOperatorInstruction)instruction;
         a = this.match(var15.value);
         if(a != null) {
            var15.value = this.newInstruction(var15.value, a);
         } else {
            this.visit(var15.value);
         }
         break;
      case 267:
         BinaryOperatorInstruction var14 = (BinaryOperatorInstruction)instruction;
         a = this.match(var14.value1);
         if(a != null) {
            var14.value1 = this.newInstruction(var14.value1, a);
         } else {
            this.visit(var14.value1);
         }

         a = this.match(var14.value2);
         if(a != null) {
            var14.value2 = this.newInstruction(var14.value2, a);
         } else {
            this.visit(var14.value2);
         }
         break;
      case 271:
         ArrayLoadInstruction var13 = (ArrayLoadInstruction)instruction;
         a = this.match(var13.arrayref);
         if(a != null) {
            var13.arrayref = this.newInstruction(var13.arrayref, a);
         } else {
            this.visit(var13.arrayref);
         }

         a = this.match(var13.indexref);
         if(a != null) {
            var13.indexref = this.newInstruction(var13.indexref, a);
         } else {
            this.visit(var13.indexref);
         }
         break;
      case 273:
         ReturnInstruction var12 = (ReturnInstruction)instruction;
         a = this.match(var12.valueref);
         if(a != null) {
            var12.valueref = this.newInstruction(var12.valueref, a);
         } else {
            this.visit(var12.valueref);
         }
         break;
      case 275:
      case 276:
         ConvertInstruction var11 = (ConvertInstruction)instruction;
         a = this.match(var11.value);
         if(a != null) {
            var11.value = this.newInstruction(var11.value, a);
         } else {
            this.visit(var11.value);
         }
         break;
      case 277:
      case 278:
         IncInstruction var9 = (IncInstruction)instruction;
         a = this.match(var9.value);
         if(a != null) {
            var9.value = this.newInstruction(var9.value, a);
         } else {
            this.visit(var9.value);
         }
         break;
      case 280:
         TernaryOpStore var8 = (TernaryOpStore)instruction;
         a = this.match(var8.objectref);
         if(a != null) {
            var8.objectref = this.newInstruction(var8.objectref, a);
         } else {
            this.visit(var8.objectref);
         }
         break;
      case 281:
         TernaryOperator var7 = (TernaryOperator)instruction;
         a = this.match(var7.test);
         if(a != null) {
            var7.test = this.newInstruction(var7.test, a);
         } else {
            this.visit(var7.test);
         }

         a = this.match(var7.value1);
         if(a != null) {
            var7.value1 = this.newInstruction(var7.value1, a);
         } else {
            this.visit(var7.value1);
         }

         a = this.match(var7.value2);
         if(a != null) {
            var7.value2 = this.newInstruction(var7.value2, a);
         } else {
            this.visit(var7.value2);
         }
         break;
      case 282:
      case 283:
         InitArrayInstruction var6 = (InitArrayInstruction)instruction;
         a = this.match(var6.newArray);
         if(a != null) {
            var6.newArray = this.newInstruction(var6.newArray, a);
         } else {
            this.visit(var6.newArray);
         }

         if(var6.values != null) {
            this.visit(var6.values);
         }
         break;
      case 284:
         var5 = ((ComplexConditionalBranchInstruction)instruction).instructions;

         for(var10 = var5.size() - 1; var10 >= 0; --var10) {
            this.visit((Instruction)var5.get(var10));
         }

         return;
      case 286:
         AssertInstruction iai = (AssertInstruction)instruction;
         a = this.match(iai.test);
         if(a != null) {
            iai.test = this.newInstruction(iai.test, a);
         } else {
            this.visit(iai.test);
         }

         if(iai.msg != null) {
            a = this.match(iai.msg);
            if(a != null) {
               iai.msg = this.newInstruction(iai.msg, a);
            } else {
               this.visit(iai.msg);
            }
         }
         break;
      default:
         System.err.println("Can not replace accessor in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
      }

   }

   public void visit(List<Instruction> instructions) {
      for(int index = instructions.size() - 1; index >= 0; --index) {
         Instruction i = (Instruction)instructions.get(index);
         Accessor a = this.match(i);
         if(a != null) {
            instructions.set(index, this.newInstruction(i, a));
         } else {
            this.visit(i);
         }
      }

   }

   protected Accessor match(Instruction i) {
      if(i.opcode != 184) {
         return null;
      } else {
         Invokestatic is = (Invokestatic)i;
         ConstantMethodref cmr = this.constants.getConstantMethodref(is.index);
         ConstantNameAndType cnat = this.constants.getConstantNameAndType(cmr.name_and_type_index);
         String descriptor = this.constants.getConstantUtf8(cnat.descriptor_index);
         if(descriptor.charAt(1) != 41) {
            return null;
         } else {
            String className = this.constants.getConstantClassName(cmr.class_index);
            ClassFile classFile = (ClassFile)this.innerClassesMap.get(className);
            if(classFile == null) {
               return null;
            } else {
               String name = this.constants.getConstantUtf8(cnat.name_index);
               Accessor accessor = classFile.getAccessor(name, descriptor);
               return accessor != null && accessor.tag == 1?(GetStaticAccessor)accessor:null;
            }
         }
      }
   }

   protected Instruction newInstruction(Instruction i, Accessor a) {
      GetStaticAccessor gsa = (GetStaticAccessor)a;
      int nameIndex = this.constants.addConstantUtf8(gsa.fieldName);
      int descriptorIndex = this.constants.addConstantUtf8(gsa.fieldDescriptor);
      int cnatIndex = this.constants.addConstantNameAndType(nameIndex, descriptorIndex);
      int classNameIndex = this.constants.addConstantUtf8(gsa.className);
      int classIndex = this.constants.addConstantClass(classNameIndex);
      int cfrIndex = this.constants.addConstantFieldref(classIndex, cnatIndex);
      return new GetStatic(178, i.offset, i.lineNumber, cfrIndex);
   }
}
