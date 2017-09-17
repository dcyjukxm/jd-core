package jd.core.process.layouter;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
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
import jd.core.model.layout.block.BlockLayoutBlock;
import jd.core.model.layout.block.CaseBlockEndLayoutBlock;
import jd.core.model.layout.block.CaseBlockStartLayoutBlock;
import jd.core.model.layout.block.CaseEnumLayoutBlock;
import jd.core.model.layout.block.CaseLayoutBlock;
import jd.core.model.layout.block.DeclareLayoutBlock;
import jd.core.model.layout.block.FastCatchLayoutBlock;
import jd.core.model.layout.block.FragmentLayoutBlock;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.model.layout.block.OffsetLayoutBlock;
import jd.core.model.layout.block.SeparatorLayoutBlock;
import jd.core.model.layout.block.SingleStatementBlockEndLayoutBlock;
import jd.core.model.layout.block.SingleStatementBlockStartLayoutBlock;
import jd.core.model.layout.block.StatementsBlockEndLayoutBlock;
import jd.core.model.layout.block.StatementsBlockStartLayoutBlock;
import jd.core.model.layout.block.SwitchBlockEndLayoutBlock;
import jd.core.model.layout.block.SwitchBlockStartLayoutBlock;
import jd.core.preferences.Preferences;
import jd.core.process.layouter.visitor.InstructionSplitterVisitor;
import jd.core.process.layouter.visitor.InstructionsSplitterVisitor;
import jd.core.process.layouter.visitor.MaxLineNumberVisitor;

public class JavaSourceLayouter {
   InstructionSplitterVisitor instructionSplitterVisitor = new InstructionSplitterVisitor();
   InstructionsSplitterVisitor instructionsSplitterVisitor = new InstructionsSplitterVisitor();

   public boolean createBlocks(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, List<Instruction> list) {
      int length = list.size();
      boolean singleLine = false;

      for(int index = 0; index < length; ++index) {
         Instruction instruction = (Instruction)list.get(index);
         if(index > 0) {
            layoutBlockList.add(new SeparatorLayoutBlock(5, 1));
         }

         switch(instruction.opcode) {
         case 301:
            this.createBlockForFastTestList(preferences, 61, layoutBlockList, classFile, method, (FastTestList)instruction, true);
            break;
         case 302:
            this.createBlocksForDoWhileLoop(preferences, layoutBlockList, classFile, method, (FastTestList)instruction);
            break;
         case 303:
            this.createBlocksForInfiniteLoop(preferences, layoutBlockList, classFile, method, (FastList)instruction);
            break;
         case 304:
            this.createBlocksForForLoop(preferences, layoutBlockList, classFile, method, (FastFor)instruction);
            break;
         case 305:
            this.createBlockForFastForEach(preferences, layoutBlockList, classFile, method, (FastForEach)instruction);
            break;
         case 306:
            this.createBlockForFastTestList(preferences, 63, layoutBlockList, classFile, method, (FastTestList)instruction, true);
            break;
         case 307:
            FastTest2Lists ft2l = (FastTest2Lists)instruction;
            this.createBlocksForIfElse(preferences, layoutBlockList, classFile, method, ft2l, ShowSingleInstructionBlock(ft2l));
            break;
         case 308:
         case 309:
            this.createBlocksForIfContinueOrBreak(preferences, layoutBlockList, classFile, method, (FastInstruction)instruction);
            break;
         case 310:
            this.createBlocksForIfLabeledBreak(preferences, layoutBlockList, classFile, method, (FastInstruction)instruction);
            break;
         case 313:
            CreateBlocksForGotoLabeledBreak(layoutBlockList, classFile, method, (FastInstruction)instruction);
            break;
         case 314:
            this.createBlocksForSwitch(preferences, layoutBlockList, classFile, method, (FastSwitch)instruction, 65);
            break;
         case 315:
            this.createBlocksForSwitchEnum(preferences, layoutBlockList, classFile, method, (FastSwitch)instruction);
            break;
         case 316:
            this.createBlocksForSwitch(preferences, layoutBlockList, classFile, method, (FastSwitch)instruction, 67);
            break;
         case 317:
            if(((FastDeclaration)instruction).instruction == null) {
               layoutBlockList.add(new DeclareLayoutBlock(classFile, method, instruction));
               break;
            }
         case 311:
         case 312:
         default:
            if(length == 1) {
               int min = instruction.lineNumber;
               if(min != Instruction.UNKNOWN_LINE_NUMBER) {
                  int max = MaxLineNumberVisitor.visit(instruction);
                  singleLine = min == max;
               }
            }

            index = this.createBlockForInstructions(preferences, layoutBlockList, classFile, method, list, index);
            break;
         case 318:
            this.createBlocksForTry(preferences, layoutBlockList, classFile, method, (FastTry)instruction);
            break;
         case 319:
            this.createBlocksForSynchronized(preferences, layoutBlockList, classFile, method, (FastSynchronized)instruction);
            break;
         case 320:
            this.createBlocksForLabel(preferences, layoutBlockList, classFile, method, (FastLabel)instruction);
         }
      }

      return singleLine;
   }

   private void createBlockForFastTestList(Preferences preferences, byte tag, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastTestList ftl, boolean showSingleInstructionBlock) {
      layoutBlockList.add(new FragmentLayoutBlock(tag));
      this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, ftl.test);
      layoutBlockList.add(new FragmentLayoutBlock(80));
      this.createBlockForSubList(preferences, layoutBlockList, classFile, method, ftl.instructions, showSingleInstructionBlock, 1);
   }

   private void createBlocksForIfElse(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastTest2Lists ft2l, boolean showSingleInstructionBlock) {
      layoutBlockList.add(new FragmentLayoutBlock(63));
      this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, ft2l.test);
      layoutBlockList.add(new FragmentLayoutBlock(80));
      List instructions = ft2l.instructions;
      if(instructions.size() == 1) {
         switch(((Instruction)instructions.get(0)).opcode) {
         case 306:
         case 307:
            this.createBlockForSubList(preferences, layoutBlockList, classFile, method, instructions, false, 2);
            break;
         default:
            this.createBlockForSubList(preferences, layoutBlockList, classFile, method, instructions, showSingleInstructionBlock, 2);
         }
      } else {
         this.createBlockForSubList(preferences, layoutBlockList, classFile, method, instructions, showSingleInstructionBlock, 2);
      }

      List instructions2 = ft2l.instructions2;
      if(instructions2.size() == 1) {
         Instruction instruction = (Instruction)instructions2.get(0);
         switch(instruction.opcode) {
         case 306:
            layoutBlockList.add(new FragmentLayoutBlock(72));
            this.createBlockForFastTestList(preferences, 63, layoutBlockList, classFile, method, (FastTestList)instruction, showSingleInstructionBlock);
            return;
         case 307:
            layoutBlockList.add(new FragmentLayoutBlock(72));
            this.createBlocksForIfElse(preferences, layoutBlockList, classFile, method, (FastTest2Lists)instruction, showSingleInstructionBlock);
            return;
         }
      }

      layoutBlockList.add(new FragmentLayoutBlock(71));
      this.createBlockForSubList(preferences, layoutBlockList, classFile, method, instructions2, showSingleInstructionBlock, 1);
   }

   private static boolean ShowSingleInstructionBlock(FastTest2Lists ifElse) {
      while(true) {
         List instructions = ifElse.instructions;
         if(instructions != null && instructions.size() >= 2) {
            return false;
         }

         int instructions2Size = ifElse.instructions2.size();
         if(instructions2Size == 0) {
            return true;
         }

         if(instructions2Size >= 2) {
            return false;
         }

         if(instructions2Size == 1) {
            Instruction instruction = (Instruction)ifElse.instructions2.get(0);
            if(instruction.opcode == 306) {
               instructions = ((FastTestList)instruction).instructions;
               if(instructions != null && instructions.size() >= 2) {
                  return false;
               }

               return true;
            }

            if(instruction.opcode != 307) {
               return true;
            }

            ifElse = (FastTest2Lists)instruction;
         }
      }
   }

   private void createBlocksForDoWhileLoop(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastTestList ftl) {
      layoutBlockList.add(new FragmentLayoutBlock(73));
      this.createBlockForSubList(preferences, layoutBlockList, classFile, method, ftl.instructions, false, 1);
      layoutBlockList.add(new FragmentLayoutBlock(61));
      this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, ftl.test);
      layoutBlockList.add(new FragmentLayoutBlock(81));
   }

   private void createBlocksForInfiniteLoop(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastList fl) {
      layoutBlockList.add(new FragmentLayoutBlock(74));
      this.createBlockForSubList(preferences, layoutBlockList, classFile, method, fl.instructions, false, 1);
   }

   private void createBlocksForForLoop(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastFor ff) {
      layoutBlockList.add(new FragmentLayoutBlock(62));
      BlockLayoutBlock sblb;
      BlockLayoutBlock eblb;
      if(ff.init != null) {
         sblb = new BlockLayoutBlock(37, 0);
         layoutBlockList.add(sblb);
         this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, ff.init);
         eblb = new BlockLayoutBlock(38, 0);
         sblb.other = eblb;
         eblb.other = sblb;
         layoutBlockList.add(eblb);
      }

      if(ff.test == null) {
         layoutBlockList.add(new FragmentLayoutBlock(82));
      } else {
         layoutBlockList.add(new FragmentLayoutBlock(83));
         sblb = new BlockLayoutBlock(37, 0, Integer.MAX_VALUE, 0);
         layoutBlockList.add(sblb);
         this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, ff.test);
         eblb = new BlockLayoutBlock(38, 0);
         sblb.other = eblb;
         eblb.other = sblb;
         layoutBlockList.add(eblb);
      }

      if(ff.inc == null) {
         layoutBlockList.add(new FragmentLayoutBlock(82));
      } else {
         layoutBlockList.add(new FragmentLayoutBlock(83));
         sblb = new BlockLayoutBlock(37, 0, Integer.MAX_VALUE, 0);
         layoutBlockList.add(sblb);
         this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, ff.inc);
         eblb = new BlockLayoutBlock(38, 0);
         sblb.other = eblb;
         eblb.other = sblb;
         layoutBlockList.add(eblb);
      }

      layoutBlockList.add(new FragmentLayoutBlock(80));
      this.createBlockForSubList(preferences, layoutBlockList, classFile, method, ff.instructions, true, 1);
   }

   private void createBlockForFastForEach(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastForEach ffe) {
      layoutBlockList.add(new FragmentLayoutBlock(62));
      this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, ffe.variable);
      layoutBlockList.add(new FragmentLayoutBlock(84));
      this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, ffe.values);
      layoutBlockList.add(new FragmentLayoutBlock(80));
      this.createBlockForSubList(preferences, layoutBlockList, classFile, method, ffe.instructions, true, 1);
   }

   private void createBlocksForIfContinueOrBreak(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastInstruction fi) {
      layoutBlockList.add(new FragmentLayoutBlock(63));
      this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, fi.instruction);
      layoutBlockList.add(new FragmentLayoutBlock(80));
      SingleStatementBlockStartLayoutBlock ssbslb = new SingleStatementBlockStartLayoutBlock();
      layoutBlockList.add(ssbslb);
      if(fi.opcode == 308) {
         layoutBlockList.add(new FragmentLayoutBlock(77));
      } else {
         layoutBlockList.add(new FragmentLayoutBlock(78));
      }

      SingleStatementBlockEndLayoutBlock ssbelb = new SingleStatementBlockEndLayoutBlock(1);
      ssbslb.other = ssbelb;
      ssbelb.other = ssbslb;
      layoutBlockList.add(ssbelb);
   }

   private void createBlocksForIfLabeledBreak(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastInstruction fi) {
      layoutBlockList.add(new FragmentLayoutBlock(63));
      this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, fi.instruction);
      layoutBlockList.add(new FragmentLayoutBlock(80));
      SingleStatementBlockStartLayoutBlock ssbslb = new SingleStatementBlockStartLayoutBlock();
      layoutBlockList.add(ssbslb);
      BranchInstruction bi = (BranchInstruction)fi.instruction;
      layoutBlockList.add(new OffsetLayoutBlock(79, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 0, 0, bi.GetJumpOffset()));
      SingleStatementBlockEndLayoutBlock ssbelb = new SingleStatementBlockEndLayoutBlock(1);
      ssbslb.other = ssbelb;
      ssbelb.other = ssbslb;
      layoutBlockList.add(ssbelb);
   }

   private static void CreateBlocksForGotoLabeledBreak(List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastInstruction fi) {
      BranchInstruction bi = (BranchInstruction)fi.instruction;
      layoutBlockList.add(new OffsetLayoutBlock(79, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 0, 0, bi.GetJumpOffset()));
   }

   private void createBlocksForSwitch(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastSwitch fs, byte tagCase) {
      layoutBlockList.add(new FragmentLayoutBlock(64));
      this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, fs.test);
      layoutBlockList.add(new FragmentLayoutBlock(80));
      SwitchBlockStartLayoutBlock sbslb = new SwitchBlockStartLayoutBlock();
      layoutBlockList.add(sbslb);
      FastSwitch.Pair[] pairs = fs.pairs;
      int length = pairs.length;
      int firstIndex = 0;

      for(int sbelb = 0; sbelb < length; ++sbelb) {
         boolean last = sbelb == length - 1;
         FastSwitch.Pair pair = pairs[sbelb];
         List instructions = pair.getInstructions();
         if(pair.isDefault() && last && (instructions == null || instructions.size() == 0 || instructions.size() == 1 && ((Instruction)instructions.get(0)).opcode == 312)) {
            break;
         }

         if(instructions != null) {
            layoutBlockList.add(new CaseLayoutBlock(tagCase, classFile, method, fs, firstIndex, sbelb));
            firstIndex = sbelb + 1;
            layoutBlockList.add(new CaseBlockStartLayoutBlock());
            this.createBlocks(preferences, layoutBlockList, classFile, method, instructions);
            layoutBlockList.add(new CaseBlockEndLayoutBlock());
         }
      }

      SwitchBlockEndLayoutBlock var15 = new SwitchBlockEndLayoutBlock();
      sbslb.other = var15;
      var15.other = sbslb;
      layoutBlockList.add(var15);
   }

   private void createBlocksForSwitchEnum(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastSwitch fs) {
      layoutBlockList.add(new FragmentLayoutBlock(64));
      Instruction test = fs.test;
      ConstantPool constants = classFile.getConstantPool();
      int switchMapKeyIndex = -1;
      if(test.opcode == 271) {
         ArrayLoadInstruction sbslb = (ArrayLoadInstruction)test;
         ConstantNameAndType pairs;
         if(sbslb.arrayref.opcode == 184) {
            Invokestatic length = (Invokestatic)sbslb.arrayref;
            ConstantMethodref firstIndex = constants.getConstantMethodref(length.index);
            pairs = constants.getConstantNameAndType(firstIndex.name_and_type_index);
         } else {
            if(sbslb.arrayref.opcode != 178) {
               throw new RuntimeException();
            }

            GetStatic var19 = (GetStatic)sbslb.arrayref;
            ConstantFieldref var22 = constants.getConstantFieldref(var19.index);
            pairs = constants.getConstantNameAndType(var22.name_and_type_index);
         }

         switchMapKeyIndex = pairs.name_index;
         Invokevirtual var20 = (Invokevirtual)sbslb.indexref;
         this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, var20.objectref);
      }

      if(switchMapKeyIndex == -1) {
         throw new RuntimeException();
      } else {
         layoutBlockList.add(new FragmentLayoutBlock(80));
         SwitchBlockStartLayoutBlock var17 = new SwitchBlockStartLayoutBlock();
         layoutBlockList.add(var17);
         FastSwitch.Pair[] var18 = fs.pairs;
         int var21 = var18.length;
         int var23 = 0;

         for(int sbelb = 0; sbelb < var21; ++sbelb) {
            boolean last = sbelb == var21 - 1;
            FastSwitch.Pair pair = var18[sbelb];
            List instructions = pair.getInstructions();
            if(pair.isDefault() && last && (instructions == null || instructions.size() == 0 || instructions.size() == 1 && ((Instruction)instructions.get(0)).opcode == 312)) {
               break;
            }

            if(instructions != null) {
               layoutBlockList.add(new CaseEnumLayoutBlock(classFile, method, fs, var23, sbelb, switchMapKeyIndex));
               var23 = sbelb + 1;
               layoutBlockList.add(new CaseBlockStartLayoutBlock());
               this.createBlocks(preferences, layoutBlockList, classFile, method, instructions);
               layoutBlockList.add(new CaseBlockEndLayoutBlock());
            }
         }

         SwitchBlockEndLayoutBlock var24 = new SwitchBlockEndLayoutBlock();
         var17.other = var24;
         var24.other = var17;
         layoutBlockList.add(var24);
      }
   }

   private void createBlocksForTry(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastTry ft) {
      layoutBlockList.add(new FragmentLayoutBlock(75));
      this.createBlockForSubList(preferences, layoutBlockList, classFile, method, ft.instructions, false, 2);
      if(ft.catches != null) {
         int length = ft.catches.size();
         if(length > 0) {
            --length;

            for(int fc = 0; fc < length; ++fc) {
               FastTry.FastCatch blockEndPreferedLineCount = (FastTry.FastCatch)ft.catches.get(fc);
               layoutBlockList.add(new FastCatchLayoutBlock(classFile, method, blockEndPreferedLineCount));
               this.createBlockForSubList(preferences, layoutBlockList, classFile, method, blockEndPreferedLineCount.instructions, false, 2);
            }

            FastTry.FastCatch var9 = (FastTry.FastCatch)ft.catches.get(length);
            layoutBlockList.add(new FastCatchLayoutBlock(classFile, method, var9));
            int var10 = ft.finallyInstructions == null?1:2;
            this.createBlockForSubList(preferences, layoutBlockList, classFile, method, var9.instructions, false, var10);
         }
      }

      if(ft.finallyInstructions != null) {
         layoutBlockList.add(new FragmentLayoutBlock(76));
         this.createBlockForSubList(preferences, layoutBlockList, classFile, method, ft.finallyInstructions, false, 1);
      }

   }

   private void createBlocksForSynchronized(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastSynchronized fs) {
      layoutBlockList.add(new FragmentLayoutBlock(69));
      this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, fs.monitor);
      layoutBlockList.add(new FragmentLayoutBlock(80));
      this.createBlockForSubList(preferences, layoutBlockList, classFile, method, fs.instructions, false, 1);
   }

   private void createBlocksForLabel(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, FastLabel fl) {
      layoutBlockList.add(new OffsetLayoutBlock(70, Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER, 0, 0, 0, fl.offset));
      Instruction instruction = fl.instruction;
      if(instruction != null) {
         layoutBlockList.add(new SeparatorLayoutBlock(5, 1));
         switch(instruction.opcode) {
         case 301:
            this.createBlockForFastTestList(preferences, 61, layoutBlockList, classFile, method, (FastTestList)instruction, true);
            break;
         case 302:
            this.createBlocksForDoWhileLoop(preferences, layoutBlockList, classFile, method, (FastTestList)instruction);
            break;
         case 303:
            this.createBlocksForInfiniteLoop(preferences, layoutBlockList, classFile, method, (FastList)instruction);
            break;
         case 304:
            this.createBlocksForForLoop(preferences, layoutBlockList, classFile, method, (FastFor)instruction);
            break;
         case 305:
            this.createBlockForFastForEach(preferences, layoutBlockList, classFile, method, (FastForEach)instruction);
            break;
         case 306:
            this.createBlockForFastTestList(preferences, 63, layoutBlockList, classFile, method, (FastTestList)instruction, true);
            break;
         case 307:
            FastTest2Lists ft2l = (FastTest2Lists)instruction;
            this.createBlocksForIfElse(preferences, layoutBlockList, classFile, method, ft2l, ShowSingleInstructionBlock(ft2l));
            break;
         case 308:
         case 309:
            this.createBlocksForIfContinueOrBreak(preferences, layoutBlockList, classFile, method, (FastInstruction)instruction);
            break;
         case 310:
            this.createBlocksForIfLabeledBreak(preferences, layoutBlockList, classFile, method, (FastInstruction)instruction);
            break;
         case 313:
            CreateBlocksForGotoLabeledBreak(layoutBlockList, classFile, method, (FastInstruction)instruction);
            break;
         case 314:
            this.createBlocksForSwitch(preferences, layoutBlockList, classFile, method, (FastSwitch)instruction, 65);
            break;
         case 315:
            this.createBlocksForSwitchEnum(preferences, layoutBlockList, classFile, method, (FastSwitch)instruction);
            break;
         case 316:
            this.createBlocksForSwitch(preferences, layoutBlockList, classFile, method, (FastSwitch)instruction, 67);
            break;
         case 317:
            if(((FastDeclaration)instruction).instruction == null) {
               layoutBlockList.add(new DeclareLayoutBlock(classFile, method, instruction));
               break;
            }
         case 311:
         case 312:
         default:
            this.createBlockForInstruction(preferences, layoutBlockList, classFile, method, instruction);
            layoutBlockList.add(new FragmentLayoutBlock(82));
            break;
         case 318:
            this.createBlocksForTry(preferences, layoutBlockList, classFile, method, (FastTry)instruction);
            break;
         case 319:
            this.createBlocksForSynchronized(preferences, layoutBlockList, classFile, method, (FastSynchronized)instruction);
            break;
         case 320:
            this.createBlocksForLabel(preferences, layoutBlockList, classFile, method, (FastLabel)instruction);
         }
      }

   }

   private void createBlockForSubList(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, List<Instruction> instructions, boolean showSingleInstructionBlock, int blockEndPreferedLineCount) {
      if(instructions != null && instructions.size() != 0) {
         if(instructions.size() > 1) {
            showSingleInstructionBlock = false;
         }

         Object sbslb1 = showSingleInstructionBlock?new SingleStatementBlockStartLayoutBlock():new StatementsBlockStartLayoutBlock();
         layoutBlockList.add(sbslb1);
         this.createBlocks(preferences, layoutBlockList, classFile, method, instructions);
         Object sbelb = showSingleInstructionBlock?new SingleStatementBlockEndLayoutBlock(1):new StatementsBlockEndLayoutBlock(blockEndPreferedLineCount);
         ((BlockLayoutBlock)sbslb1).other = (BlockLayoutBlock)sbelb;
         ((BlockLayoutBlock)sbelb).other = (BlockLayoutBlock)sbslb1;
         layoutBlockList.add(sbelb);
      } else {
         StatementsBlockStartLayoutBlock sbslb = new StatementsBlockStartLayoutBlock();
         sbslb.transformToStartEndBlock(0);
         layoutBlockList.add(sbslb);
      }

   }

   private void createBlockForInstruction(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, Instruction instruction) {
      this.instructionSplitterVisitor.start(preferences, layoutBlockList, classFile, method, instruction);
      this.instructionSplitterVisitor.visit(instruction);
      this.instructionSplitterVisitor.end();
   }

   private int createBlockForInstructions(Preferences preferences, List<LayoutBlock> layoutBlockList, ClassFile classFile, Method method, List<Instruction> list, int index1) {
      int index2 = SkipInstructions(list, index1);
      this.instructionsSplitterVisitor.start(preferences, layoutBlockList, classFile, method, list, index1);

      for(int index = index1; index <= index2; ++index) {
         this.instructionsSplitterVisitor.setIndex2(index);
         this.instructionsSplitterVisitor.visit((Instruction)list.get(index));
      }

      this.instructionsSplitterVisitor.end();
      return index2;
   }

   private static int SkipInstructions(List<Instruction> list, int index) {
      int length = list.size();

      while(true) {
         ++index;
         if(index >= length) {
            return length - 1;
         }

         Instruction instruction = (Instruction)list.get(index);
         switch(instruction.opcode) {
         case 301:
         case 302:
         case 303:
         case 304:
         case 305:
         case 306:
         case 307:
         case 308:
         case 309:
         case 310:
         case 313:
         case 314:
         case 315:
         case 316:
         case 318:
         case 319:
         case 320:
            return index - 1;
         case 311:
         case 312:
         default:
            break;
         case 317:
            if(((FastDeclaration)instruction).instruction == null) {
               return index - 1;
            }
         }
      }
   }
}
