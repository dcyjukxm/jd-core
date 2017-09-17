package jd.core.process.analyzer.classfile.visitor;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.constant.ConstantFieldref;
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

public class ReplaceOuterAccessorVisitor {
   protected ClassFile classFile;

   public ReplaceOuterAccessorVisitor(ClassFile classFile) {
      this.classFile = classFile;
   }

   public void visit(Instruction instruction) {
      ClassFile matchedClassFile;
      ClassFile matchedClassFile1;
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
         matchedClassFile = this.match(var37.valueref);
         if(matchedClassFile != null) {
            var37.valueref = this.newInstruction(matchedClassFile, var37.valueref);
         } else {
            this.visit(var37.valueref);
         }
         break;
      case 83:
      case 272:
         ArrayStoreInstruction var36 = (ArrayStoreInstruction)instruction;
         matchedClassFile = this.match(var36.arrayref);
         if(matchedClassFile != null) {
            var36.arrayref = this.newInstruction(matchedClassFile, var36.arrayref);
         } else {
            this.visit(var36.arrayref);
         }

         matchedClassFile = this.match(var36.indexref);
         if(matchedClassFile != null) {
            var36.indexref = this.newInstruction(matchedClassFile, var36.indexref);
         } else {
            this.visit(var36.indexref);
         }

         matchedClassFile = this.match(var36.valueref);
         if(matchedClassFile != null) {
            var36.valueref = this.newInstruction(matchedClassFile, var36.valueref);
         } else {
            this.visit(var36.valueref);
         }
         break;
      case 87:
         Pop var35 = (Pop)instruction;
         matchedClassFile = this.match(var35.objectref);
         if(matchedClassFile != null) {
            var35.objectref = this.newInstruction(matchedClassFile, var35.objectref);
         } else {
            this.visit(var35.objectref);
         }
         break;
      case 170:
         TableSwitch var34 = (TableSwitch)instruction;
         matchedClassFile = this.match(var34.key);
         if(matchedClassFile != null) {
            var34.key = this.newInstruction(matchedClassFile, var34.key);
         } else {
            this.visit(var34.key);
         }
         break;
      case 171:
         LookupSwitch var33 = (LookupSwitch)instruction;
         matchedClassFile = this.match(var33.key);
         if(matchedClassFile != null) {
            var33.key = this.newInstruction(matchedClassFile, var33.key);
         } else {
            this.visit(var33.key);
         }
         break;
      case 179:
         PutStatic var32 = (PutStatic)instruction;
         matchedClassFile = this.match(var32.valueref);
         if(matchedClassFile != null) {
            var32.valueref = this.newInstruction(matchedClassFile, var32.valueref);
         } else {
            this.visit(var32.valueref);
         }
         break;
      case 180:
         GetField var31 = (GetField)instruction;
         matchedClassFile = this.match(var31.objectref);
         if(matchedClassFile != null) {
            var31.objectref = this.newInstruction(matchedClassFile, var31.objectref);
         } else {
            this.visit(var31.objectref);
         }
         break;
      case 181:
         PutField var30 = (PutField)instruction;
         matchedClassFile = this.match(var30.objectref);
         if(matchedClassFile != null) {
            var30.objectref = this.newInstruction(matchedClassFile, var30.objectref);
         } else {
            this.visit(var30.objectref);
         }

         matchedClassFile = this.match(var30.valueref);
         if(matchedClassFile != null) {
            var30.valueref = this.newInstruction(matchedClassFile, var30.valueref);
         } else {
            this.visit(var30.valueref);
         }
         break;
      case 182:
      case 183:
      case 185:
         InvokeNoStaticInstruction var29 = (InvokeNoStaticInstruction)instruction;
         matchedClassFile = this.match(var29.objectref);
         if(matchedClassFile != null) {
            var29.objectref = this.newInstruction(matchedClassFile, var29.objectref);
         } else {
            this.visit(var29.objectref);
         }
      case 184:
      case 274:
         var5 = ((InvokeInstruction)instruction).args;

         for(var10 = var5.size() - 1; var10 >= 0; --var10) {
            matchedClassFile1 = this.match((Instruction)var5.get(var10));
            if(matchedClassFile1 != null) {
               var5.set(var10, this.newInstruction(matchedClassFile1, (Instruction)var5.get(var10)));
            } else {
               this.visit((Instruction)var5.get(var10));
            }
         }

         return;
      case 188:
         NewArray var28 = (NewArray)instruction;
         matchedClassFile = this.match(var28.dimension);
         if(matchedClassFile != null) {
            var28.dimension = this.newInstruction(matchedClassFile, var28.dimension);
         } else {
            this.visit(var28.dimension);
         }
         break;
      case 189:
         ANewArray var27 = (ANewArray)instruction;
         matchedClassFile = this.match(var27.dimension);
         if(matchedClassFile != null) {
            var27.dimension = this.newInstruction(matchedClassFile, var27.dimension);
         } else {
            this.visit(var27.dimension);
         }
         break;
      case 190:
         ArrayLength var26 = (ArrayLength)instruction;
         matchedClassFile = this.match(var26.arrayref);
         if(matchedClassFile != null) {
            var26.arrayref = this.newInstruction(matchedClassFile, var26.arrayref);
         } else {
            this.visit(var26.arrayref);
         }
         break;
      case 191:
         AThrow var25 = (AThrow)instruction;
         matchedClassFile = this.match(var25.value);
         if(matchedClassFile != null) {
            var25.value = this.newInstruction(matchedClassFile, var25.value);
         } else {
            this.visit(var25.value);
         }
         break;
      case 192:
         CheckCast var24 = (CheckCast)instruction;
         matchedClassFile = this.match(var24.objectref);
         if(matchedClassFile != null) {
            var24.objectref = this.newInstruction(matchedClassFile, var24.objectref);
         } else {
            this.visit(var24.objectref);
         }
         break;
      case 193:
         InstanceOf var23 = (InstanceOf)instruction;
         matchedClassFile = this.match(var23.objectref);
         if(matchedClassFile != null) {
            var23.objectref = this.newInstruction(matchedClassFile, var23.objectref);
         } else {
            this.visit(var23.objectref);
         }
         break;
      case 194:
         MonitorEnter var22 = (MonitorEnter)instruction;
         matchedClassFile = this.match(var22.objectref);
         if(matchedClassFile != null) {
            var22.objectref = this.newInstruction(matchedClassFile, var22.objectref);
         } else {
            this.visit(var22.objectref);
         }
         break;
      case 195:
         MonitorExit var21 = (MonitorExit)instruction;
         matchedClassFile = this.match(var21.objectref);
         if(matchedClassFile != null) {
            var21.objectref = this.newInstruction(matchedClassFile, var21.objectref);
         } else {
            this.visit(var21.objectref);
         }
         break;
      case 197:
         Instruction[] var20 = ((MultiANewArray)instruction).dimensions;

         for(var10 = var20.length - 1; var10 >= 0; --var10) {
            matchedClassFile1 = this.match(var20[var10]);
            if(matchedClassFile1 != null) {
               var20[var10] = this.newInstruction(matchedClassFile1, var20[var10]);
            } else {
               this.visit(var20[var10]);
            }
         }

         return;
      case 260:
      case 262:
         IfInstruction var19 = (IfInstruction)instruction;
         matchedClassFile = this.match(var19.value);
         if(matchedClassFile != null) {
            var19.value = this.newInstruction(matchedClassFile, var19.value);
         } else {
            this.visit(var19.value);
         }
         break;
      case 261:
         IfCmp var18 = (IfCmp)instruction;
         matchedClassFile = this.match(var18.value1);
         if(matchedClassFile != null) {
            var18.value1 = this.newInstruction(matchedClassFile, var18.value1);
         } else {
            this.visit(var18.value1);
         }

         matchedClassFile = this.match(var18.value2);
         if(matchedClassFile != null) {
            var18.value2 = this.newInstruction(matchedClassFile, var18.value2);
         } else {
            this.visit(var18.value2);
         }
         break;
      case 264:
         DupStore var17 = (DupStore)instruction;
         matchedClassFile = this.match(var17.objectref);
         if(matchedClassFile != null) {
            var17.objectref = this.newInstruction(matchedClassFile, var17.objectref);
         } else {
            this.visit(var17.objectref);
         }
         break;
      case 265:
         AssignmentInstruction var16 = (AssignmentInstruction)instruction;
         matchedClassFile = this.match(var16.value1);
         if(matchedClassFile != null) {
            var16.value1 = this.newInstruction(matchedClassFile, var16.value1);
         } else {
            this.visit(var16.value1);
         }

         matchedClassFile = this.match(var16.value2);
         if(matchedClassFile != null) {
            var16.value2 = this.newInstruction(matchedClassFile, var16.value2);
         } else {
            this.visit(var16.value2);
         }
         break;
      case 266:
         UnaryOperatorInstruction var15 = (UnaryOperatorInstruction)instruction;
         matchedClassFile = this.match(var15.value);
         if(matchedClassFile != null) {
            var15.value = this.newInstruction(matchedClassFile, var15.value);
         } else {
            this.visit(var15.value);
         }
         break;
      case 267:
         BinaryOperatorInstruction var14 = (BinaryOperatorInstruction)instruction;
         matchedClassFile = this.match(var14.value1);
         if(matchedClassFile != null) {
            var14.value1 = this.newInstruction(matchedClassFile, var14.value1);
         } else {
            this.visit(var14.value1);
         }

         matchedClassFile = this.match(var14.value2);
         if(matchedClassFile != null) {
            var14.value2 = this.newInstruction(matchedClassFile, var14.value2);
         } else {
            this.visit(var14.value2);
         }
         break;
      case 271:
         ArrayLoadInstruction var13 = (ArrayLoadInstruction)instruction;
         matchedClassFile = this.match(var13.arrayref);
         if(matchedClassFile != null) {
            var13.arrayref = this.newInstruction(matchedClassFile, var13.arrayref);
         } else {
            this.visit(var13.arrayref);
         }

         matchedClassFile = this.match(var13.indexref);
         if(matchedClassFile != null) {
            var13.indexref = this.newInstruction(matchedClassFile, var13.indexref);
         } else {
            this.visit(var13.indexref);
         }
         break;
      case 273:
         ReturnInstruction var12 = (ReturnInstruction)instruction;
         matchedClassFile = this.match(var12.valueref);
         if(matchedClassFile != null) {
            var12.valueref = this.newInstruction(matchedClassFile, var12.valueref);
         } else {
            this.visit(var12.valueref);
         }
         break;
      case 275:
      case 276:
         ConvertInstruction var11 = (ConvertInstruction)instruction;
         matchedClassFile = this.match(var11.value);
         if(matchedClassFile != null) {
            var11.value = this.newInstruction(matchedClassFile, var11.value);
         } else {
            this.visit(var11.value);
         }
         break;
      case 277:
      case 278:
         IncInstruction var9 = (IncInstruction)instruction;
         matchedClassFile = this.match(var9.value);
         if(matchedClassFile != null) {
            var9.value = this.newInstruction(matchedClassFile, var9.value);
         } else {
            this.visit(var9.value);
         }
         break;
      case 280:
         TernaryOpStore var8 = (TernaryOpStore)instruction;
         matchedClassFile = this.match(var8.objectref);
         if(matchedClassFile != null) {
            var8.objectref = this.newInstruction(matchedClassFile, var8.objectref);
         } else {
            this.visit(var8.objectref);
         }
         break;
      case 281:
         TernaryOperator var7 = (TernaryOperator)instruction;
         matchedClassFile = this.match(var7.test);
         if(matchedClassFile != null) {
            var7.test = this.newInstruction(matchedClassFile, var7.test);
         } else {
            this.visit(var7.test);
         }

         matchedClassFile = this.match(var7.value1);
         if(matchedClassFile != null) {
            var7.value1 = this.newInstruction(matchedClassFile, var7.value1);
         } else {
            this.visit(var7.value1);
         }

         matchedClassFile = this.match(var7.value2);
         if(matchedClassFile != null) {
            var7.value2 = this.newInstruction(matchedClassFile, var7.value2);
         } else {
            this.visit(var7.value2);
         }
         break;
      case 282:
      case 283:
         InitArrayInstruction var6 = (InitArrayInstruction)instruction;
         matchedClassFile = this.match(var6.newArray);
         if(matchedClassFile != null) {
            var6.newArray = this.newInstruction(matchedClassFile, var6.newArray);
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
         matchedClassFile = this.match(iai.test);
         if(matchedClassFile != null) {
            iai.test = this.newInstruction(matchedClassFile, iai.test);
         } else {
            this.visit(iai.test);
         }

         if(iai.msg != null) {
            matchedClassFile = this.match(iai.msg);
            if(matchedClassFile != null) {
               iai.msg = this.newInstruction(matchedClassFile, iai.msg);
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
         ClassFile matchedClassFile = this.match(i);
         if(matchedClassFile != null) {
            instructions.set(index, this.newInstruction(matchedClassFile, i));
         } else {
            this.visit(i);
         }
      }

   }

   protected ClassFile match(Instruction instruction) {
      if(instruction.opcode != 184) {
         return null;
      } else {
         Invokestatic is = (Invokestatic)instruction;
         if(is.args.size() != 1) {
            return null;
         } else {
            ClassFile matchedClassFile = this.innerMatch((Instruction)is.args.get(0));
            if(matchedClassFile != null && matchedClassFile.isAInnerClass()) {
               ConstantPool constants = this.classFile.getConstantPool();
               ConstantMethodref cmr = constants.getConstantMethodref(is.index);
               String className = constants.getConstantClassName(cmr.class_index);
               if(!className.equals(matchedClassFile.getThisClassName())) {
                  return null;
               } else {
                  ConstantNameAndType cnat = constants.getConstantNameAndType(cmr.name_and_type_index);
                  String methodName = constants.getConstantUtf8(cnat.name_index);
                  String methodDescriptor = constants.getConstantUtf8(cnat.descriptor_index);
                  Method method = matchedClassFile.getMethod(methodName, methodDescriptor);
                  if(method != null && (method.access_flags & 4104) == 4104) {
                     ClassFile outerClassFile = matchedClassFile.getOuterClass();
                     String returnedSignature = cmr.getReturnedSignature();
                     return !returnedSignature.equals(outerClassFile.getInternalClassName())?null:outerClassFile;
                  } else {
                     return null;
                  }
               }
            } else {
               return null;
            }
         }
      }
   }

   private ClassFile innerMatch(Instruction instruction) {
      switch(instruction.opcode) {
      case 184:
         return this.match(instruction);
      case 285:
         GetStatic gs = (GetStatic)instruction;
         ConstantPool constants = this.classFile.getConstantPool();
         ConstantFieldref cfr = constants.getConstantFieldref(gs.index);
         String className = constants.getConstantClassName(cfr.class_index);
         ClassFile outerClassFile = this.classFile.getOuterClass();
         if(outerClassFile != null && className.equals(outerClassFile.getThisClassName())) {
            ConstantNameAndType cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
            String descriptor = constants.getConstantUtf8(cnat.descriptor_index);
            if(!descriptor.equals(outerClassFile.getInternalClassName())) {
               return null;
            }

            return outerClassFile;
         }

         return null;
      default:
         return null;
      }
   }

   private Instruction newInstruction(ClassFile matchedClassFile, Instruction i) {
      String internalMatchedClassName = matchedClassFile.getInternalClassName();
      String matchedClassName = matchedClassFile.getThisClassName();
      ConstantPool constants = this.classFile.getConstantPool();
      int signatureIndex = constants.addConstantUtf8(matchedClassName);
      int classIndex = constants.addConstantClass(signatureIndex);
      int thisIndex = constants.thisLocalVariableNameIndex;
      int descriptorIndex = constants.addConstantUtf8(internalMatchedClassName);
      int nameAndTypeIndex = constants.addConstantNameAndType(thisIndex, descriptorIndex);
      int matchedThisFieldrefIndex = constants.addConstantFieldref(classIndex, nameAndTypeIndex);
      return new GetStatic(285, i.offset, i.lineNumber, matchedThisFieldrefIndex);
   }
}
