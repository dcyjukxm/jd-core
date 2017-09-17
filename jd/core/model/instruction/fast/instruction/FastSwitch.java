package jd.core.model.instruction.fast.instruction;

import java.util.List;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class FastSwitch extends BranchInstruction {
   public Instruction test;
   public FastSwitch.Pair[] pairs;

   public FastSwitch(int opcode, int offset, int lineNumber, int branch, Instruction test, FastSwitch.Pair[] pairs) {
      super(opcode, offset, lineNumber, branch);
      this.test = test;
      this.pairs = pairs;
   }

   public String getReturnedSignature(ConstantPool constants, LocalVariables localVariables) {
      return null;
   }

   public static class Pair implements Comparable<FastSwitch.Pair> {
      private boolean defaultFlag;
      private int key;
      private int offset;
      private List<Instruction> instructions;

      public Pair(boolean defaultFlag, int key, int offset) {
         this.defaultFlag = defaultFlag;
         this.key = key;
         this.offset = offset;
         this.instructions = null;
      }

      public boolean isDefault() {
         return this.defaultFlag;
      }

      public int getKey() {
         return this.key;
      }

      public void setKey(int key) {
         this.key = key;
      }

      public int getOffset() {
         return this.offset;
      }

      public List<Instruction> getInstructions() {
         return this.instructions;
      }

      public void setInstructions(List<Instruction> instructions) {
         this.instructions = instructions;
      }

      public int compareTo(FastSwitch.Pair p) {
         int diffOffset = this.offset - p.offset;
         return diffOffset != 0?diffOffset:(this.isDefault()?1:(p.isDefault()?-1:this.key - p.key));
      }
   }
}
