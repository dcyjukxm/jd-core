package jd.core.process.layouter.visitor;

import java.util.List;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
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

public class MaxLineNumberVisitor {
   public static int visit(Instruction instruction) {
      int maxLineNumber = instruction.lineNumber;
      int length;
      int i;
      int lineNumber;
      List var7;
      IncInstruction var9;
      switch(instruction.opcode) {
      case 54:
      case 58:
      case 269:
         maxLineNumber = visit(((StoreInstruction)instruction).valueref);
         break;
      case 83:
      case 272:
         maxLineNumber = visit(((ArrayStoreInstruction)instruction).valueref);
         break;
      case 87:
         maxLineNumber = visit(((Pop)instruction).objectref);
         break;
      case 170:
         maxLineNumber = visit(((TableSwitch)instruction).key);
         break;
      case 171:
         maxLineNumber = visit(((LookupSwitch)instruction).key);
         break;
      case 179:
         maxLineNumber = visit(((PutStatic)instruction).valueref);
         break;
      case 181:
         maxLineNumber = visit(((PutField)instruction).valueref);
         break;
      case 182:
      case 183:
      case 184:
      case 185:
         var7 = ((InvokeInstruction)instruction).args;
         length = var7.size();
         if(length == 0) {
            maxLineNumber = instruction.lineNumber;
            break;
         } else {
            maxLineNumber = visit((Instruction)var7.get(0));

            for(i = length - 1; i > 0; --i) {
               lineNumber = visit((Instruction)var7.get(i));
               if(maxLineNumber < lineNumber) {
                  maxLineNumber = lineNumber;
               }
            }

            return maxLineNumber < instruction.lineNumber?instruction.lineNumber:maxLineNumber;
         }
      case 188:
         maxLineNumber = visit(((NewArray)instruction).dimension);
         break;
      case 189:
         maxLineNumber = visit(((ANewArray)instruction).dimension);
         break;
      case 191:
         maxLineNumber = visit(((AThrow)instruction).value);
         break;
      case 192:
         maxLineNumber = visit(((CheckCast)instruction).objectref);
         break;
      case 193:
         maxLineNumber = visit(((InstanceOf)instruction).objectref);
         break;
      case 194:
         maxLineNumber = visit(((MonitorEnter)instruction).objectref);
         break;
      case 195:
         maxLineNumber = visit(((MonitorExit)instruction).objectref);
         break;
      case 197:
         Instruction[] var10 = ((MultiANewArray)instruction).dimensions;
         length = var10.length;
         if(length > 0) {
            maxLineNumber = visit(var10[length - 1]);
         }
         break;
      case 260:
      case 262:
         maxLineNumber = visit(((IfInstruction)instruction).value);
         break;
      case 261:
         maxLineNumber = visit(((IfCmp)instruction).value2);
         break;
      case 264:
         maxLineNumber = visit(((DupStore)instruction).objectref);
         break;
      case 265:
      case 267:
         maxLineNumber = visit(((BinaryOperatorInstruction)instruction).value2);
         break;
      case 266:
         maxLineNumber = visit(((UnaryOperatorInstruction)instruction).value);
         break;
      case 271:
         maxLineNumber = visit(((ArrayLoadInstruction)instruction).indexref);
         break;
      case 273:
         maxLineNumber = visit(((ReturnInstruction)instruction).valueref);
         break;
      case 274:
      case 321:
         var7 = ((InvokeNew)instruction).args;
         length = var7.size();
         if(length == 0) {
            maxLineNumber = instruction.lineNumber;
            break;
         } else {
            maxLineNumber = visit((Instruction)var7.get(0));

            for(i = length - 1; i > 0; --i) {
               lineNumber = visit((Instruction)var7.get(i));
               if(maxLineNumber < lineNumber) {
                  maxLineNumber = lineNumber;
               }
            }

            return maxLineNumber < instruction.lineNumber?instruction.lineNumber:maxLineNumber;
         }
      case 275:
      case 276:
         maxLineNumber = visit(((ConvertInstruction)instruction).value);
         break;
      case 277:
         var9 = (IncInstruction)instruction;
         switch(var9.count) {
         case -1:
         case 1:
            maxLineNumber = visit(var9.value);
            return maxLineNumber < instruction.lineNumber?instruction.lineNumber:maxLineNumber;
         case 0:
         default:
            return maxLineNumber < instruction.lineNumber?instruction.lineNumber:maxLineNumber;
         }
      case 278:
         var9 = (IncInstruction)instruction;
         switch(var9.count) {
         case -1:
         case 1:
            maxLineNumber = instruction.lineNumber;
         case 0:
         default:
            maxLineNumber = visit(var9.value);
            return maxLineNumber < instruction.lineNumber?instruction.lineNumber:maxLineNumber;
         }
      case 280:
         maxLineNumber = visit(((TernaryOpStore)instruction).objectref);
         break;
      case 281:
         maxLineNumber = visit(((TernaryOperator)instruction).value2);
         break;
      case 282:
      case 283:
         InitArrayInstruction var8 = (InitArrayInstruction)instruction;
         length = var8.values.size();
         if(length > 0) {
            maxLineNumber = visit((Instruction)var8.values.get(length - 1));
         }
         break;
      case 284:
         var7 = ((ComplexConditionalBranchInstruction)instruction).instructions;
         maxLineNumber = visit((Instruction)var7.get(var7.size() - 1));
         break;
      case 286:
         AssertInstruction var6 = (AssertInstruction)instruction;
         maxLineNumber = visit(var6.msg == null?var6.test:var6.msg);
         break;
      case 317:
         FastDeclaration iai = (FastDeclaration)instruction;
         if(iai.instruction != null) {
            maxLineNumber = visit(iai.instruction);
         }
      }

      return maxLineNumber < instruction.lineNumber?instruction.lineNumber:maxLineNumber;
   }
}
