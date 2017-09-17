package jd.core.process.analyzer.instruction.bytecode.factory;

import java.util.List;
import java.util.Stack;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.TableSwitch;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;

public class TableSwitchFactory extends InstructionFactory {
   public int create(ClassFile classFile, Method method, List<Instruction> list, List<Instruction> listForAnalyze, Stack<Instruction> stack, byte[] code, int offset, int lineNumber, boolean[] jumps) {
      int opcode = code[offset] & 255;
      int i = offset + 4 & '￼';
      int defaultOffset = (code[i] & 255) << 24 | (code[i + 1] & 255) << 16 | (code[i + 2] & 255) << 8 | code[i + 3] & 255;
      i += 4;
      int low = (code[i] & 255) << 24 | (code[i + 1] & 255) << 16 | (code[i + 2] & 255) << 8 | code[i + 3] & 255;
      i += 4;
      int high = (code[i] & 255) << 24 | (code[i + 1] & 255) << 16 | (code[i + 2] & 255) << 8 | code[i + 3] & 255;
      i += 4;
      int length = high - low + 1;
      int[] offsets = new int[length];

      for(int key = 0; key < length; ++key) {
         offsets[key] = (code[i] & 255) << 24 | (code[i + 1] & 255) << 16 | (code[i + 2] & 255) << 8 | code[i + 3] & 255;
         i += 4;
      }

      Instruction var18 = (Instruction)stack.pop();
      list.add(new TableSwitch(opcode, offset, lineNumber, var18, defaultOffset, offsets, low, high));
      return i - offset - 1;
   }
}
