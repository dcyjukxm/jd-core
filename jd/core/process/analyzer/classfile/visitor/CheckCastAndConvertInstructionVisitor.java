package jd.core.process.analyzer.classfile.visitor;

import java.util.List;
import jd.core.model.classfile.ConstantPool;
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
import jd.core.util.SignatureUtil;

public class CheckCastAndConvertInstructionVisitor {
   private static void visit(ConstantPool constants, Instruction instruction) {
      int i;
      List var11;
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
         visit(constants, ((StoreInstruction)instruction).valueref);
         break;
      case 83:
      case 272:
         visit(constants, ((ArrayStoreInstruction)instruction).arrayref);
         break;
      case 87:
         visit(constants, ((Pop)instruction).objectref);
         break;
      case 170:
         visit(constants, ((TableSwitch)instruction).key);
         break;
      case 171:
         visit(constants, ((LookupSwitch)instruction).key);
         break;
      case 179:
         visit(constants, ((PutStatic)instruction).valueref);
         break;
      case 180:
         visit(constants, ((GetField)instruction).objectref);
         break;
      case 181:
         PutField var18 = (PutField)instruction;
         visit(constants, var18.objectref);
         visit(constants, var18.valueref);
         break;
      case 182:
      case 183:
      case 185:
         visit(constants, ((InvokeNoStaticInstruction)instruction).objectref);
      case 184:
      case 274:
         var11 = ((InvokeInstruction)instruction).getListOfParameterSignatures(constants);
         if(var11 != null) {
            List var17 = ((InvokeInstruction)instruction).args;
            int i1 = var11.size();
            int j = var17.size();

            while(i1-- > 0 && j-- > 0) {
               Instruction arg = (Instruction)var17.get(j);
               switch(arg.opcode) {
               case 16:
               case 17:
               case 256:
                  String argSignature = ((IConst)arg).getSignature();
                  String parameterSignature = (String)var11.get(i1);
                  if(!parameterSignature.equals(argSignature)) {
                     int argBitFields = SignatureUtil.CreateArgOrReturnBitFields(argSignature);
                     int paramBitFields = SignatureUtil.CreateTypesBitField(parameterSignature);
                     if((argBitFields | paramBitFields) == 0) {
                        var17.set(j, new ConvertInstruction(275, arg.offset - 1, arg.lineNumber, arg, parameterSignature));
                     }
                  } else {
                     switch(parameterSignature.charAt(0)) {
                     case 'B':
                     case 'S':
                        var17.set(j, new ConvertInstruction(275, arg.offset - 1, arg.lineNumber, arg, parameterSignature));
                        break;
                     default:
                        visit(constants, arg);
                     }
                  }
                  break;
               default:
                  visit(constants, arg);
               }
            }
         }
         break;
      case 188:
         visit(constants, ((NewArray)instruction).dimension);
         break;
      case 189:
         visit(constants, ((ANewArray)instruction).dimension);
         break;
      case 190:
         visit(constants, ((ArrayLength)instruction).arrayref);
         break;
      case 191:
         visit(constants, ((AThrow)instruction).value);
         break;
      case 192:
         CheckCast var16 = (CheckCast)instruction;
         if(var16.objectref.opcode == 192) {
            var16.objectref = ((CheckCast)var16.objectref).objectref;
         }

         visit(constants, var16.objectref);
         break;
      case 193:
         visit(constants, ((InstanceOf)instruction).objectref);
         break;
      case 194:
         visit(constants, ((MonitorEnter)instruction).objectref);
         break;
      case 195:
         visit(constants, ((MonitorExit)instruction).objectref);
         break;
      case 197:
         Instruction[] var15 = ((MultiANewArray)instruction).dimensions;

         for(i = var15.length - 1; i >= 0; --i) {
            visit(constants, var15[i]);
         }

         return;
      case 260:
      case 262:
         visit(constants, ((IfInstruction)instruction).value);
         break;
      case 261:
         IfCmp var14 = (IfCmp)instruction;
         visit(constants, var14.value1);
         visit(constants, var14.value2);
         break;
      case 264:
         visit(constants, ((DupStore)instruction).objectref);
         break;
      case 265:
      case 267:
         BinaryOperatorInstruction var13 = (BinaryOperatorInstruction)instruction;
         visit(constants, var13.value1);
         visit(constants, var13.value2);
         break;
      case 266:
         visit(constants, ((UnaryOperatorInstruction)instruction).value);
         break;
      case 273:
         visit(constants, ((ReturnInstruction)instruction).valueref);
         break;
      case 275:
      case 276:
         visit(constants, ((ConvertInstruction)instruction).value);
         break;
      case 277:
      case 278:
         visit(constants, ((IncInstruction)instruction).value);
         break;
      case 280:
         visit(constants, ((TernaryOpStore)instruction).objectref);
         break;
      case 282:
      case 283:
         InitArrayInstruction var12 = (InitArrayInstruction)instruction;
         visit(constants, var12.newArray);
         if(var12.values != null) {
            visit(constants, var12.values);
         }
         break;
      case 284:
         var11 = ((ComplexConditionalBranchInstruction)instruction).instructions;

         for(i = var11.size() - 1; i >= 0; --i) {
            visit(constants, (Instruction)var11.get(i));
         }

         return;
      case 286:
         AssertInstruction iai = (AssertInstruction)instruction;
         visit(constants, iai.test);
         if(iai.msg != null) {
            visit(constants, iai.msg);
         }
         break;
      default:
         System.err.println("Can not check cast and convert instruction in " + instruction.getClass().getName() + ", opcode=" + instruction.opcode);
      }

   }

   public static void visit(ConstantPool constants, List<Instruction> instructions) {
      for(int i = instructions.size() - 1; i >= 0; --i) {
         visit(constants, (Instruction)instructions.get(i));
      }

   }
}
