package jd.core.process.analyzer.classfile.visitor;

import java.util.List;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
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
import jd.core.model.instruction.bytecode.instruction.ConstInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
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
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;

public class CompareInstructionVisitor {
   public boolean visit(Instruction i1, Instruction i2) {
      if(i1.opcode != i2.opcode) {
         return false;
      } else {
         String var6;
         String var8;
         switch(i1.opcode) {
         case 0:
         case 1:
         case 18:
         case 20:
         case 87:
         case 132:
         case 167:
         case 168:
         case 169:
         case 170:
         case 171:
         case 177:
         case 178:
         case 179:
         case 187:
         case 194:
         case 195:
         case 268:
         case 270:
         case 273:
         case 279:
         case 285:
            return true;
         case 16:
         case 17:
         case 256:
            if(((IConst)i1).value != ((IConst)i2).value) {
               return false;
            } else {
               if(((IConst)i1).signature.compareTo(((IConst)i2).signature) == 0) {
                  return true;
               }

               return false;
            }
         case 21:
         case 25:
            var6 = ((LoadInstruction)i1).getReturnedSignature((ConstantPool)null, (LocalVariables)null);
            var8 = ((LoadInstruction)i2).getReturnedSignature((ConstantPool)null, (LocalVariables)null);
            return var6 == null?var8 == null:var6.compareTo(var8) == 0;
         case 54:
         case 58:
         case 269:
            var6 = ((StoreInstruction)i1).getReturnedSignature((ConstantPool)null, (LocalVariables)null);
            var8 = ((StoreInstruction)i2).getReturnedSignature((ConstantPool)null, (LocalVariables)null);
            if(var6 == null) {
               if(var8 != null) {
                  return false;
               }
            } else if(var6.compareTo(var8) != 0) {
               return false;
            }

            return this.visit(((StoreInstruction)i1).valueref, ((StoreInstruction)i2).valueref);
         case 83:
         case 272:
            if(((ArrayStoreInstruction)i1).signature.compareTo(((ArrayStoreInstruction)i2).signature) != 0) {
               return false;
            } else if(!this.visit(((ArrayStoreInstruction)i1).arrayref, ((ArrayStoreInstruction)i2).arrayref)) {
               return false;
            } else {
               if(!this.visit(((ArrayStoreInstruction)i1).indexref, ((ArrayStoreInstruction)i2).indexref)) {
                  return false;
               }

               return this.visit(((ArrayStoreInstruction)i1).valueref, ((ArrayStoreInstruction)i2).valueref);
            }
         case 180:
            if(((GetField)i1).index != ((GetField)i2).index) {
               return false;
            }

            return this.visit(((GetField)i1).objectref, ((GetField)i2).objectref);
         case 181:
            if(!this.visit(((PutField)i1).objectref, ((PutField)i2).objectref)) {
               return false;
            }

            return this.visit(((PutField)i1).valueref, ((PutField)i2).valueref);
         case 182:
         case 183:
         case 185:
            if(!this.visit(((InvokeNoStaticInstruction)i1).objectref, ((InvokeNoStaticInstruction)i2).objectref)) {
               return false;
            }
         case 184:
            return this.visit(((InvokeInstruction)i1).args, ((InvokeInstruction)i2).args);
         case 188:
            if(((NewArray)i1).type != ((NewArray)i2).type) {
               return false;
            }

            return this.visit(((NewArray)i1).dimension, ((NewArray)i2).dimension);
         case 189:
            if(((ANewArray)i1).index != ((ANewArray)i2).index) {
               return false;
            }

            return this.visit(((ANewArray)i1).dimension, ((ANewArray)i2).dimension);
         case 190:
            return this.visit(((ArrayLength)i1).arrayref, ((ArrayLength)i2).arrayref);
         case 191:
            return this.visit(((AThrow)i1).value, ((AThrow)i2).value);
         case 192:
            if(((CheckCast)i1).index != ((CheckCast)i2).index) {
               return false;
            }

            return this.visit(((CheckCast)i1).objectref, ((CheckCast)i2).objectref);
         case 193:
            if(((InstanceOf)i1).index != ((InstanceOf)i2).index) {
               return false;
            }

            return this.visit(((InstanceOf)i1).objectref, ((InstanceOf)i2).objectref);
         case 197:
            if(((MultiANewArray)i1).index != ((MultiANewArray)i2).index) {
               return false;
            } else {
               Instruction[] var7 = ((MultiANewArray)i1).dimensions;
               Instruction[] var9 = ((MultiANewArray)i2).dimensions;
               if(var7.length != var9.length) {
                  return false;
               } else {
                  for(int i = var7.length - 1; i >= 0; --i) {
                     if(!this.visit(var7[i], var9[i])) {
                        return false;
                     }
                  }

                  return true;
               }
            }
         case 257:
         case 258:
         case 259:
            if(((ConstInstruction)i1).value == ((ConstInstruction)i2).value) {
               return true;
            }

            return false;
         case 260:
         case 262:
            if(((IfInstruction)i1).cmp != ((IfInstruction)i2).cmp) {
               return false;
            }

            return this.visit(((IfInstruction)i1).value, ((IfInstruction)i2).value);
         case 261:
            if(((IfCmp)i1).cmp != ((IfCmp)i2).cmp) {
               return false;
            } else {
               if(!this.visit(((IfCmp)i1).value1, ((IfCmp)i2).value1)) {
                  return false;
               }

               return this.visit(((IfCmp)i1).value2, ((IfCmp)i2).value2);
            }
         case 263:
            if(((DupLoad)i1).dupStore == ((DupLoad)i2).dupStore) {
               return true;
            }

            return false;
         case 264:
            return this.visit(((DupStore)i1).objectref, ((DupStore)i2).objectref);
         case 265:
            if(((AssignmentInstruction)i1).getPriority() != ((AssignmentInstruction)i2).getPriority()) {
               return false;
            } else if(((AssignmentInstruction)i1).operator.compareTo(((AssignmentInstruction)i2).operator) != 0) {
               return false;
            } else {
               if(!this.visit(((AssignmentInstruction)i1).value1, ((AssignmentInstruction)i2).value1)) {
                  return false;
               }

               return this.visit(((AssignmentInstruction)i1).value2, ((AssignmentInstruction)i2).value2);
            }
         case 266:
            if(((UnaryOperatorInstruction)i1).getPriority() != ((UnaryOperatorInstruction)i2).getPriority()) {
               return false;
            } else if(((UnaryOperatorInstruction)i1).signature.compareTo(((UnaryOperatorInstruction)i2).signature) != 0) {
               return false;
            } else {
               if(((UnaryOperatorInstruction)i1).operator.compareTo(((UnaryOperatorInstruction)i2).operator) != 0) {
                  return false;
               }

               return this.visit(((UnaryOperatorInstruction)i1).value, ((UnaryOperatorInstruction)i2).value);
            }
         case 267:
            if(((BinaryOperatorInstruction)i1).getPriority() != ((BinaryOperatorInstruction)i2).getPriority()) {
               return false;
            } else if(((BinaryOperatorInstruction)i1).signature.compareTo(((BinaryOperatorInstruction)i2).signature) != 0) {
               return false;
            } else if(((BinaryOperatorInstruction)i1).operator.compareTo(((BinaryOperatorInstruction)i2).operator) != 0) {
               return false;
            } else {
               if(!this.visit(((BinaryOperatorInstruction)i1).value1, ((BinaryOperatorInstruction)i2).value1)) {
                  return false;
               }

               return this.visit(((BinaryOperatorInstruction)i1).value2, ((BinaryOperatorInstruction)i2).value2);
            }
         case 271:
            var6 = ((ArrayLoadInstruction)i1).getReturnedSignature((ConstantPool)null, (LocalVariables)null);
            var8 = ((ArrayLoadInstruction)i2).getReturnedSignature((ConstantPool)null, (LocalVariables)null);
            if(var6 == null) {
               if(var8 != null) {
                  return false;
               }
            } else if(var6.compareTo(var8) != 0) {
               return false;
            }

            if(!this.visit(((ArrayLoadInstruction)i1).arrayref, ((ArrayLoadInstruction)i2).arrayref)) {
               return false;
            }

            return this.visit(((ArrayLoadInstruction)i1).indexref, ((ArrayLoadInstruction)i2).indexref);
         case 274:
            if(((InvokeNew)i1).index != ((InvokeNew)i2).index) {
               return false;
            }

            return this.visit(((InvokeNew)i1).args, ((InvokeNew)i2).args);
         case 275:
         case 276:
            if(((ConvertInstruction)i1).signature.compareTo(((ConvertInstruction)i2).signature) != 0) {
               return false;
            }

            return this.visit(((ConvertInstruction)i1).value, ((ConvertInstruction)i2).value);
         case 277:
         case 278:
            if(((IncInstruction)i1).count != ((IncInstruction)i2).count) {
               return false;
            }

            return this.visit(((IncInstruction)i1).value, ((IncInstruction)i2).value);
         case 280:
            if(((TernaryOpStore)i1).ternaryOp2ndValueOffset - i1.offset != ((TernaryOpStore)i2).ternaryOp2ndValueOffset - i2.offset) {
               return false;
            }

            return this.visit(((TernaryOpStore)i1).objectref, ((TernaryOpStore)i2).objectref);
         case 281:
            if(!this.visit(((TernaryOperator)i1).test, ((TernaryOperator)i2).test)) {
               return false;
            } else {
               if(!this.visit(((TernaryOperator)i1).value1, ((TernaryOperator)i2).value1)) {
                  return false;
               }

               return this.visit(((TernaryOperator)i1).value2, ((TernaryOperator)i2).value2);
            }
         case 282:
         case 283:
            if(!this.visit(((InitArrayInstruction)i1).newArray, ((InitArrayInstruction)i2).newArray)) {
               return false;
            }

            return this.visit(((InitArrayInstruction)i1).values, ((InitArrayInstruction)i2).values);
         case 284:
            if(((ComplexConditionalBranchInstruction)i1).cmp != ((ComplexConditionalBranchInstruction)i2).cmp) {
               return false;
            } else {
               if(((ComplexConditionalBranchInstruction)i1).branch != ((ComplexConditionalBranchInstruction)i2).branch) {
                  return false;
               }

               return this.visit(((ComplexConditionalBranchInstruction)i1).instructions, ((ComplexConditionalBranchInstruction)i2).instructions);
            }
         case 286:
            if(!this.visit(((AssertInstruction)i1).test, ((AssertInstruction)i2).test)) {
               return false;
            } else {
               Instruction rs1 = ((AssertInstruction)i1).msg;
               Instruction rs2 = ((AssertInstruction)i2).msg;
               if(rs1 == rs2) {
                  return true;
               } else {
                  if(rs1 != null && rs2 != null) {
                     return this.visit(rs1, rs2);
                  }

                  return false;
               }
            }
         default:
            System.err.println("Can not compare instruction " + i1.getClass().getName() + " and " + i2.getClass().getName());
            return false;
         }
      }
   }

   protected boolean visit(List<Instruction> l1, List<Instruction> l2) {
      int i = l1.size();
      if(i != l2.size()) {
         return false;
      } else {
         while(i-- > 0) {
            if(!this.visit((Instruction)l1.get(i), (Instruction)l2.get(i))) {
               return false;
            }
         }

         return true;
      }
   }
}
