package jd.core.process.layouter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.attribute.AttributeSignature;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.layout.block.BlockLayoutBlock;
import jd.core.model.layout.block.ByteCodeLayoutBlock;
import jd.core.model.layout.block.CommentDeprecatedLayoutBlock;
import jd.core.model.layout.block.CommentErrorLayoutBlock;
import jd.core.model.layout.block.ExtendsSuperInterfacesLayoutBlock;
import jd.core.model.layout.block.ExtendsSuperTypeLayoutBlock;
import jd.core.model.layout.block.FieldNameLayoutBlock;
import jd.core.model.layout.block.FragmentLayoutBlock;
import jd.core.model.layout.block.ImplementsInterfacesLayoutBlock;
import jd.core.model.layout.block.ImportsLayoutBlock;
import jd.core.model.layout.block.InnerTypeBodyBlockEndLayoutBlock;
import jd.core.model.layout.block.InnerTypeBodyBlockStartLayoutBlock;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.model.layout.block.MarkerLayoutBlock;
import jd.core.model.layout.block.MethodBodyBlockEndLayoutBlock;
import jd.core.model.layout.block.MethodBodyBlockStartLayoutBlock;
import jd.core.model.layout.block.MethodBodySingleLineBlockEndLayoutBlock;
import jd.core.model.layout.block.MethodNameLayoutBlock;
import jd.core.model.layout.block.MethodStaticLayoutBlock;
import jd.core.model.layout.block.PackageLayoutBlock;
import jd.core.model.layout.block.SeparatorLayoutBlock;
import jd.core.model.layout.block.SubListLayoutBlock;
import jd.core.model.layout.block.ThrowsLayoutBlock;
import jd.core.model.layout.block.TypeBodyBlockEndLayoutBlock;
import jd.core.model.layout.block.TypeBodyBlockStartLayoutBlock;
import jd.core.model.layout.block.TypeNameLayoutBlock;
import jd.core.model.layout.section.LayoutSection;
import jd.core.model.reference.Reference;
import jd.core.model.reference.ReferenceMap;
import jd.core.preferences.Preferences;
import jd.core.process.layouter.AnnotationLayouter;
import jd.core.process.layouter.JavaSourceLayouter;
import jd.core.process.layouter.LayoutBlockComparator;
import jd.core.process.layouter.SignatureLayouter;
import jd.core.process.layouter.visitor.InstructionSplitterVisitor;
import jd.core.process.layouter.visitor.MaxLineNumberVisitor;
import jd.core.util.ClassFileUtil;
import jd.core.util.SignatureUtil;
import jd.core.util.TypeNameUtil;

public class ClassFileLayouter {
   public static int Layout(Preferences preferences, ReferenceMap referenceMap, ClassFile classFile, ArrayList<LayoutBlock> layoutBlockList) {
      int maxLineNumber = CreateBlocks(preferences, referenceMap, classFile, layoutBlockList);
      if(maxLineNumber != Instruction.UNKNOWN_LINE_NUMBER && preferences.getRealignmentLineNumber()) {
         LayoutBlocks(layoutBlockList);
      }

      return maxLineNumber;
   }

   private static int CreateBlocks(Preferences preferences, ReferenceMap referenceMap, ClassFile classFile, ArrayList<LayoutBlock> layoutBlockList) {
      boolean separator = true;
      String internalPackageName = classFile.getInternalPackageName();
      if(internalPackageName != null && internalPackageName.length() > 0) {
         layoutBlockList.add(new PackageLayoutBlock(classFile));
         layoutBlockList.add(new SeparatorLayoutBlock(2, 2));
         separator = false;
      }

      int importCount = GetImportCount(referenceMap, classFile);
      if(importCount > 0) {
         layoutBlockList.add(new ImportsLayoutBlock(classFile, importCount - 1));
         layoutBlockList.add(new SeparatorLayoutBlock(4, 2));
         separator = false;
      }

      if(separator) {
         layoutBlockList.add(new SeparatorLayoutBlock(3, 0));
      }

      return CreateBlocksForClass(preferences, classFile, layoutBlockList);
   }

   private static int GetImportCount(ReferenceMap referenceMap, ClassFile classFile) {
      Collection collection = referenceMap.values();
      if(collection.size() > 0) {
         int importCount = 0;
         String internalPackageName = classFile.getInternalPackageName();
         Iterator iterator = collection.iterator();

         while(iterator.hasNext()) {
            String internalReferencePackageName = TypeNameUtil.InternalTypeNameToInternalPackageName(((Reference)iterator.next()).getInternalName());
            if(!internalReferencePackageName.equals(internalPackageName) && !internalReferencePackageName.equals("java/lang")) {
               ++importCount;
            }
         }

         return importCount;
      } else {
         return 0;
      }
   }

   private static int CreateBlocksForClass(Preferences preferences, ClassFile classFile, List<LayoutBlock> layoutBlockList) {
      MarkerLayoutBlock tmslb = new MarkerLayoutBlock(7, classFile);
      layoutBlockList.add(tmslb);
      boolean displayExtendsOrImplementsFlag = CreateBlocksForHeader(classFile, layoutBlockList);
      TypeBodyBlockStartLayoutBlock bbslb = new TypeBodyBlockStartLayoutBlock();
      layoutBlockList.add(bbslb);
      int layoutBlockListLength = layoutBlockList.size();
      int maxLineNumber = CreateBlocksForBody(preferences, classFile, layoutBlockList, displayExtendsOrImplementsFlag);
      if(layoutBlockListLength == layoutBlockList.size()) {
         if(displayExtendsOrImplementsFlag) {
            bbslb.transformToStartEndBlock(1);
         } else {
            bbslb.transformToStartEndBlock(0);
         }
      } else {
         TypeBodyBlockEndLayoutBlock tmelb = new TypeBodyBlockEndLayoutBlock();
         bbslb.other = tmelb;
         tmelb.other = bbslb;
         layoutBlockList.add(tmelb);
      }

      MarkerLayoutBlock tmelb1 = new MarkerLayoutBlock(8, classFile);
      tmslb.other = tmelb1;
      tmelb1.other = tmslb;
      layoutBlockList.add(tmelb1);
      return maxLineNumber;
   }

   private static boolean CreateBlocksForHeader(ClassFile classFile, List<LayoutBlock> layoutBlockList) {
      boolean displayExtendsOrImplementsFlag = false;
      if(classFile.containsAttributeDeprecated() && !classFile.containsAnnotationDeprecated(classFile)) {
         layoutBlockList.add(new CommentDeprecatedLayoutBlock());
      }

      AnnotationLayouter.CreateBlocksForAnnotations(classFile, classFile.getAttributes(), layoutBlockList);
      AttributeSignature as = classFile.getAttributeSignature();
      if(as == null) {
         layoutBlockList.add(new TypeNameLayoutBlock(classFile));
         if((classFile.access_flags & 8192) == 0) {
            if((classFile.access_flags & 16384) != 0) {
               displayExtendsOrImplementsFlag = CreateBlocksForInterfacesImplements(classFile, layoutBlockList);
            } else if((classFile.access_flags & 512) != 0) {
               int[] constants = classFile.getInterfaces();
               if(constants != null && constants.length > 0) {
                  displayExtendsOrImplementsFlag = true;
                  layoutBlockList.add(new ExtendsSuperInterfacesLayoutBlock(classFile));
               }
            } else {
               String constants1 = classFile.getSuperClassName();
               if(constants1 != null && !"java/lang/Object".equals(constants1)) {
                  displayExtendsOrImplementsFlag = true;
                  layoutBlockList.add(new ExtendsSuperTypeLayoutBlock(classFile));
               }

               displayExtendsOrImplementsFlag |= CreateBlocksForInterfacesImplements(classFile, layoutBlockList);
            }
         }
      } else {
         ConstantPool constants2 = classFile.getConstantPool();
         String signature = constants2.getConstantUtf8(as.signature_index);
         displayExtendsOrImplementsFlag = SignatureLayouter.CreateLayoutBlocksForClassSignature(classFile, signature, layoutBlockList);
      }

      return displayExtendsOrImplementsFlag;
   }

   private static boolean CreateBlocksForInterfacesImplements(ClassFile classFile, List<LayoutBlock> layoutBlockList) {
      int[] interfaceIndexes = classFile.getInterfaces();
      if(interfaceIndexes != null && interfaceIndexes.length > 0) {
         layoutBlockList.add(new ImplementsInterfacesLayoutBlock(classFile));
         return true;
      } else {
         return false;
      }
   }

   public static int CreateBlocksForBodyOfAnonymousClass(Preferences preferences, ClassFile classFile, List<LayoutBlock> layoutBlockList) {
      InnerTypeBodyBlockStartLayoutBlock ibbslb = new InnerTypeBodyBlockStartLayoutBlock();
      layoutBlockList.add(ibbslb);
      int layoutBlockListLength = layoutBlockList.size();
      int maxLineNumber = CreateBlocksForBody(preferences, classFile, layoutBlockList, false);
      if(layoutBlockListLength == layoutBlockList.size()) {
         ibbslb.transformToStartEndBlock();
      } else {
         InnerTypeBodyBlockEndLayoutBlock ibbelb = new InnerTypeBodyBlockEndLayoutBlock();
         ibbslb.other = ibbelb;
         ibbelb.other = ibbslb;
         layoutBlockList.add(ibbelb);
      }

      return maxLineNumber;
   }

   private static int CreateBlocksForBody(Preferences preferences, ClassFile classFile, List<LayoutBlock> layoutBlockList, boolean displayExtendsOrImplementsFlag) {
      CreateBlockForEnumValues(preferences, classFile, layoutBlockList);
      List sortedFieldBlockList = CreateSortedBlocksForFields(preferences, classFile);
      List sortedMethodBlockList = CreateSortedBlocksForMethods(preferences, classFile);
      List sortedInnerClassBlockList = CreateSortedBlocksForInnerClasses(preferences, classFile);
      return MergeBlocks(layoutBlockList, sortedFieldBlockList, sortedMethodBlockList, sortedInnerClassBlockList);
   }

   private static void CreateBlockForEnumValues(Preferences preferences, ClassFile classFile, List<LayoutBlock> layoutBlockList) {
      List values = classFile.getEnumValues();
      if(values != null) {
         int valuesLength = values.size();
         if(valuesLength > 0) {
            ConstantPool constants = classFile.getConstantPool();
            Field[] fields = classFile.getFields();
            int fieldsLength = fields.length;
            ArrayList enumValues = new ArrayList(fieldsLength);
            InstructionSplitterVisitor visitor = new InstructionSplitterVisitor();

            int length;
            for(length = 0; length < valuesLength; ++length) {
               GetStatic enumValue = (GetStatic)values.get(length);
               ConstantFieldref i = constants.getConstantFieldref(enumValue.index);
               ConstantNameAndType cnat = constants.getConstantNameAndType(i.name_and_type_index);
               int j = fields.length;

               while(j-- > 0) {
                  Field field = fields[j];
                  if(field.name_index == cnat.name_index && field.descriptor_index == cnat.descriptor_index) {
                     Field.ValueAndMethod vam = field.getValueAndMethod();
                     InvokeNew invokeNew = (InvokeNew)vam.getValue();
                     invokeNew.transformToEnumValue(enumValue);
                     enumValues.add(invokeNew);
                     break;
                  }
               }
            }

            length = enumValues.size();
            if(length > 0) {
               InvokeNew var18 = (InvokeNew)enumValues.get(0);
               visitor.start(preferences, layoutBlockList, classFile, classFile.getStaticMethod(), var18);
               visitor.visit(var18);
               visitor.end();

               for(int var19 = 1; var19 < length; ++var19) {
                  layoutBlockList.add(new FragmentLayoutBlock(85));
                  layoutBlockList.add(new SeparatorLayoutBlock(2, 0));
                  var18 = (InvokeNew)enumValues.get(var19);
                  visitor.start(preferences, layoutBlockList, classFile, classFile.getStaticMethod(), var18);
                  visitor.visit(var18);
                  visitor.end();
               }

               layoutBlockList.add(new FragmentLayoutBlock(82));
            }
         }
      }

   }

   private static List<SubListLayoutBlock> CreateSortedBlocksForFields(Preferences preferences, ClassFile classFile) {
      Field[] fields = classFile.getFields();
      if(fields == null) {
         return Collections.emptyList();
      } else {
         int length = fields.length;
         ArrayList sortedFieldBlockList = new ArrayList(length);
         InstructionSplitterVisitor visitor = new InstructionSplitterVisitor();

         for(int i = 0; i < length; ++i) {
            Field field = fields[i];
            if((field.access_flags & 20480) == 0) {
               ArrayList subLayoutBlockList = new ArrayList(6);
               MarkerLayoutBlock fmslb = new MarkerLayoutBlock(9, classFile);
               subLayoutBlockList.add(fmslb);
               if(field.containsAttributeDeprecated() && !field.containsAnnotationDeprecated(classFile)) {
                  subLayoutBlockList.add(new CommentDeprecatedLayoutBlock());
               }

               AnnotationLayouter.CreateBlocksForAnnotations(classFile, field.getAttributes(), subLayoutBlockList);
               subLayoutBlockList.add(new FieldNameLayoutBlock(classFile, field));
               int firstLineNumber = Instruction.UNKNOWN_LINE_NUMBER;
               int lastLineNumber = Instruction.UNKNOWN_LINE_NUMBER;
               int preferedLineNumber = Integer.MAX_VALUE;
               if(field.getValueAndMethod() != null) {
                  Field.ValueAndMethod fmelb = field.getValueAndMethod();
                  Instruction value = fmelb.getValue();
                  Method method = fmelb.getMethod();
                  firstLineNumber = value.lineNumber;
                  lastLineNumber = MaxLineNumberVisitor.visit(value);
                  preferedLineNumber = lastLineNumber - firstLineNumber;
                  visitor.start(preferences, subLayoutBlockList, classFile, method, value);
                  visitor.visit(value);
                  visitor.end();
                  subLayoutBlockList.add(new FragmentLayoutBlock(82));
               }

               MarkerLayoutBlock var16 = new MarkerLayoutBlock(10, classFile);
               fmslb.other = var16;
               var16.other = fmslb;
               subLayoutBlockList.add(var16);
               sortedFieldBlockList.add(new SubListLayoutBlock(58, subLayoutBlockList, firstLineNumber, lastLineNumber, preferedLineNumber));
            }
         }

         return SortBlocks(sortedFieldBlockList);
      }
   }

   private static List<SubListLayoutBlock> CreateSortedBlocksForMethods(Preferences preferences, ClassFile classFile) {
      Method[] methods = classFile.getMethods();
      if(methods == null) {
         return Collections.emptyList();
      } else {
         ConstantPool constants = classFile.getConstantPool();
         boolean multipleConstructorFlag = ClassFileUtil.ContainsMultipleConstructor(classFile);
         int length = methods.length;
         ArrayList sortedMethodBlockList = new ArrayList(length);
         boolean showDefaultConstructor = preferences.getShowDefaultConstructor();
         JavaSourceLayouter javaSourceLayouter = new JavaSourceLayouter();

         for(int i = 0; i < length; ++i) {
            Method method = methods[i];
            if((method.access_flags & 4160) == 0) {
               AttributeSignature as = method.getAttributeSignature();
               boolean descriptorFlag = as == null;
               int signatureIndex = descriptorFlag?method.descriptor_index:as.signature_index;
               String signature = constants.getConstantUtf8(signatureIndex);
               if((classFile.access_flags & 16384) == 0 || !ClassFileUtil.IsAMethodOfEnum(classFile, method, signature)) {
                  if(method.name_index == constants.instanceConstructorIndex) {
                     if(classFile.getInternalAnonymousClassName() != null) {
                        continue;
                     }

                     if(!multipleConstructorFlag && (method.getFastNodes() == null || method.getFastNodes().size() == 0)) {
                        int[] subLayoutBlockList = method.getExceptionIndexes();
                        if(subLayoutBlockList == null || subLayoutBlockList.length == 0) {
                           if((classFile.access_flags & 16384) != 0) {
                              if(SignatureUtil.GetParameterSignatureCount(signature) == 2) {
                                 continue;
                              }
                           } else if(!showDefaultConstructor && signature.equals("()V")) {
                              continue;
                           }
                        }
                     }
                  }

                  if(method.name_index != constants.classConstructorIndex || method.getFastNodes() != null && method.getFastNodes().size() != 0) {
                     ArrayList var29 = new ArrayList(30);
                     MarkerLayoutBlock mmslb = new MarkerLayoutBlock(11, classFile);
                     var29.add(mmslb);
                     if(method.containsError()) {
                        var29.add(new CommentErrorLayoutBlock());
                     }

                     if(method.containsAttributeDeprecated() && !method.containsAnnotationDeprecated(classFile)) {
                        var29.add(new CommentDeprecatedLayoutBlock());
                     }

                     AnnotationLayouter.CreateBlocksForAnnotations(classFile, method.getAttributes(), var29);
                     boolean nullCodeFlag = method.getCode() == null;
                     boolean displayThrowsFlag = false;
                     if(method.name_index == constants.classConstructorIndex) {
                        var29.add(new MethodStaticLayoutBlock(classFile));
                     } else if(method.getExceptionIndexes() == null) {
                        var29.add(new MethodNameLayoutBlock(classFile, method, signature, descriptorFlag, nullCodeFlag));
                     } else {
                        var29.add(new MethodNameLayoutBlock(classFile, method, signature, descriptorFlag, false));
                        var29.add(new ThrowsLayoutBlock(classFile, method, nullCodeFlag));
                        displayThrowsFlag = true;
                     }

                     int firstLineNumber = Instruction.UNKNOWN_LINE_NUMBER;
                     int lastLineNumber = Instruction.UNKNOWN_LINE_NUMBER;
                     int preferedLineNumber = Integer.MAX_VALUE;
                     if(!nullCodeFlag) {
                        if(method.containsError()) {
                           MethodBodyBlockStartLayoutBlock mmelb = new MethodBodyBlockStartLayoutBlock();
                           var29.add(mmelb);
                           var29.add(new ByteCodeLayoutBlock(classFile, method));
                           MethodBodyBlockEndLayoutBlock mbbslb = new MethodBodyBlockEndLayoutBlock();
                           var29.add(mbbslb);
                           mmelb.other = mbbslb;
                           mbbslb.other = mmelb;
                        } else {
                           List var30 = method.getFastNodes();
                           MethodBodyBlockStartLayoutBlock var32 = new MethodBodyBlockStartLayoutBlock();
                           var29.add(var32);
                           int subLayoutBlockListLength = var29.size();
                           boolean singleLine = false;
                           if(var30.size() > 0) {
                              int currentLength;
                              try {
                                 int mbbelb = var29.size();
                                 singleLine = javaSourceLayouter.createBlocks(preferences, var29, classFile, method, var30);
                                 currentLength = var29.size();
                                 firstLineNumber = SearchFirstLineNumber(var29, mbbelb, currentLength);
                                 lastLineNumber = SearchLastLineNumber(var29, mbbelb, currentLength);
                              } catch (Exception var28) {
                                 currentLength = var29.size();

                                 while(currentLength > subLayoutBlockListLength) {
                                    --currentLength;
                                    var29.remove(currentLength);
                                 }

                                 var29.add(new ByteCodeLayoutBlock(classFile, method));
                              }
                           }

                           if(subLayoutBlockListLength == var29.size()) {
                              if(displayThrowsFlag) {
                                 var32.transformToStartEndBlock(1);
                              } else {
                                 var32.transformToStartEndBlock(0);
                              }
                           } else if(singleLine) {
                              var32.transformToSingleLineBlock();
                              MethodBodySingleLineBlockEndLayoutBlock var33 = new MethodBodySingleLineBlockEndLayoutBlock();
                              var32.other = var33;
                              var33.other = var32;
                              var29.add(var33);
                           } else {
                              MethodBodyBlockEndLayoutBlock var34 = new MethodBodyBlockEndLayoutBlock();
                              var32.other = var34;
                              var34.other = var32;
                              var29.add(var34);
                           }
                        }
                     }

                     MarkerLayoutBlock var31 = new MarkerLayoutBlock(12, classFile);
                     mmslb.other = var31;
                     var31.other = mmslb;
                     var29.add(var31);
                     sortedMethodBlockList.add(new SubListLayoutBlock(59, var29, firstLineNumber, lastLineNumber, preferedLineNumber));
                  }
               }
            }
         }

         return SortBlocks(sortedMethodBlockList);
      }
   }

   private static List<SubListLayoutBlock> CreateSortedBlocksForInnerClasses(Preferences preferences, ClassFile classFile) {
      ArrayList innerClassFiles = classFile.getInnerClassFiles();
      if(innerClassFiles == null) {
         return Collections.emptyList();
      } else {
         int length = innerClassFiles.size();
         ArrayList sortedInnerClassBlockList = new ArrayList(length);

         for(int i = 0; i < length; ++i) {
            ClassFile innerClassFile = (ClassFile)innerClassFiles.get(i);
            if((innerClassFile.access_flags & 4096) == 0 && innerClassFile.getInternalAnonymousClassName() == null) {
               ArrayList innerClassLayoutBlockList = new ArrayList(100);
               CreateBlocksForClass(preferences, innerClassFile, innerClassLayoutBlockList);
               int afterIndex = innerClassLayoutBlockList.size();
               int firstLineNumber = SearchFirstLineNumber(innerClassLayoutBlockList, 0, afterIndex);
               int lastLineNumber = SearchLastLineNumber(innerClassLayoutBlockList, 0, afterIndex);
               int preferedLineCount = Integer.MAX_VALUE;
               if(firstLineNumber != Instruction.UNKNOWN_LINE_NUMBER && lastLineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
                  preferedLineCount = lastLineNumber - firstLineNumber;
               }

               sortedInnerClassBlockList.add(new SubListLayoutBlock(60, innerClassLayoutBlockList, firstLineNumber, lastLineNumber, preferedLineCount));
            }
         }

         return SortBlocks(sortedInnerClassBlockList);
      }
   }

   private static int SearchFirstLineNumber(List<LayoutBlock> layoutBlockList, int firstIndex, int afterIndex) {
      for(int index = firstIndex; index < afterIndex; ++index) {
         int firstLineNumber = ((LayoutBlock)layoutBlockList.get(index)).firstLineNumber;
         if(firstLineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
            return firstLineNumber;
         }
      }

      return Instruction.UNKNOWN_LINE_NUMBER;
   }

   private static int SearchLastLineNumber(List<LayoutBlock> layoutBlockList, int firstIndex, int afterIndex) {
      int lastLineNumber;
      do {
         if(afterIndex-- <= firstIndex) {
            return Instruction.UNKNOWN_LINE_NUMBER;
         }

         lastLineNumber = ((LayoutBlock)layoutBlockList.get(afterIndex)).lastLineNumber;
      } while(lastLineNumber == Instruction.UNKNOWN_LINE_NUMBER);

      return lastLineNumber;
   }

   private static List<SubListLayoutBlock> SortBlocks(List<SubListLayoutBlock> blockList) {
      int length = blockList.size();
      int lineNumber = Instruction.UNKNOWN_LINE_NUMBER;
      int order = 0;

      int i;
      for(i = 0; i < length; ++i) {
         SubListLayoutBlock layoutBlock = (SubListLayoutBlock)blockList.get(i);
         int newLineNumber = layoutBlock.lastLineNumber;
         if(newLineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
            if(lineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
               if(order == 0) {
                  order = lineNumber < newLineNumber?1:2;
               } else if(order == 1) {
                  if(lineNumber > newLineNumber) {
                     order = 3;
                     break;
                  }
               } else if(order == 2 && lineNumber < newLineNumber) {
                  order = 3;
                  break;
               }
            }

            lineNumber = newLineNumber;
         }
      }

      switch(order) {
      case 2:
         Collections.reverse(blockList);
         break;
      case 3:
         for(i = 0; i < length; ((SubListLayoutBlock)blockList.get(i)).index = i++) {
            ;
         }

         Collections.sort(blockList, new LayoutBlockComparator());
      }

      return blockList;
   }

   private static int MergeBlocks(List<LayoutBlock> layoutBlockList, List<SubListLayoutBlock> sortedFieldBlockList, List<SubListLayoutBlock> sortedMethodBlockList, List<SubListLayoutBlock> sortedInnerClassBlockList) {
      int maxLineNumber = Instruction.UNKNOWN_LINE_NUMBER;
      Collections.reverse(sortedFieldBlockList);
      Collections.reverse(sortedMethodBlockList);
      Collections.reverse(sortedInnerClassBlockList);
      int minLineNumberMethod = SearchMinimalLineNumber(sortedMethodBlockList);
      int minLineNumberInnerClass = SearchMinimalLineNumber(sortedInnerClassBlockList);

      while(sortedFieldBlockList.size() > 0) {
         if(minLineNumberMethod == Instruction.UNKNOWN_LINE_NUMBER) {
            if(minLineNumberInnerClass == Instruction.UNKNOWN_LINE_NUMBER) {
               maxLineNumber = MergeFieldBlockList(layoutBlockList, sortedFieldBlockList, maxLineNumber);
               break;
            }

            maxLineNumber = ExclusiveMergeFieldBlockList(layoutBlockList, sortedFieldBlockList, minLineNumberInnerClass, maxLineNumber);
            maxLineNumber = MergeBlockList(layoutBlockList, sortedMethodBlockList, maxLineNumber);
            maxLineNumber = InclusiveMergeBlockList(layoutBlockList, sortedInnerClassBlockList, minLineNumberInnerClass, maxLineNumber);
            minLineNumberInnerClass = SearchMinimalLineNumber(sortedInnerClassBlockList);
         } else if(minLineNumberInnerClass != Instruction.UNKNOWN_LINE_NUMBER && minLineNumberMethod >= minLineNumberInnerClass) {
            maxLineNumber = ExclusiveMergeFieldBlockList(layoutBlockList, sortedFieldBlockList, minLineNumberInnerClass, maxLineNumber);
            maxLineNumber = ExclusiveMergeMethodOrInnerClassBlockList(layoutBlockList, sortedMethodBlockList, minLineNumberInnerClass, maxLineNumber);
            maxLineNumber = InclusiveMergeBlockList(layoutBlockList, sortedInnerClassBlockList, minLineNumberInnerClass, maxLineNumber);
            minLineNumberInnerClass = SearchMinimalLineNumber(sortedInnerClassBlockList);
         } else {
            maxLineNumber = ExclusiveMergeFieldBlockList(layoutBlockList, sortedFieldBlockList, minLineNumberMethod, maxLineNumber);
            maxLineNumber = InclusiveMergeBlockList(layoutBlockList, sortedMethodBlockList, minLineNumberMethod, maxLineNumber);
            minLineNumberMethod = SearchMinimalLineNumber(sortedMethodBlockList);
         }
      }

      while(sortedMethodBlockList.size() > 0) {
         if(minLineNumberInnerClass == Instruction.UNKNOWN_LINE_NUMBER) {
            maxLineNumber = MergeBlockList(layoutBlockList, sortedMethodBlockList, maxLineNumber);
            break;
         }

         maxLineNumber = ExclusiveMergeMethodOrInnerClassBlockList(layoutBlockList, sortedMethodBlockList, minLineNumberInnerClass, maxLineNumber);
         maxLineNumber = InclusiveMergeBlockList(layoutBlockList, sortedInnerClassBlockList, minLineNumberInnerClass, maxLineNumber);
         minLineNumberInnerClass = SearchMinimalLineNumber(sortedInnerClassBlockList);
      }

      maxLineNumber = MergeBlockList(layoutBlockList, sortedInnerClassBlockList, maxLineNumber);
      return maxLineNumber;
   }

   private static int ExclusiveMergeMethodOrInnerClassBlockList(List<LayoutBlock> destination, List<SubListLayoutBlock> source, int minLineNumber, int maxLineNumber) {
      byte lastTag = ((LayoutBlock)destination.get(destination.size() - 1)).tag;

      for(int index = source.size(); index > 0; lastTag = 0) {
         SubListLayoutBlock sllb = (SubListLayoutBlock)source.get(index - 1);
         int lineNumber = sllb.lastLineNumber;
         if(lineNumber != Instruction.UNKNOWN_LINE_NUMBER && lineNumber >= minLineNumber) {
            break;
         }

         switch(lastTag) {
         case 2:
         case 13:
         case 16:
         case 19:
         case 22:
         case 25:
         case 31:
            break;
         case 10:
            destination.add(new SeparatorLayoutBlock(2, 1));
            break;
         default:
            destination.add(new SeparatorLayoutBlock(2, 2));
         }

         destination.addAll(sllb.subList);
         int lastLineNumber = sllb.lastLineNumber;
         if(lastLineNumber != Instruction.UNKNOWN_LINE_NUMBER && (maxLineNumber == Instruction.UNKNOWN_LINE_NUMBER || maxLineNumber < lastLineNumber)) {
            maxLineNumber = lastLineNumber;
         }

         --index;
         source.remove(index);
      }

      return maxLineNumber;
   }

   private static int ExclusiveMergeFieldBlockList(List<LayoutBlock> destination, List<SubListLayoutBlock> source, int minLineNumber, int maxLineNumber) {
      byte lastTag = ((LayoutBlock)destination.get(destination.size() - 1)).tag;

      for(int index = source.size(); index > 0; lastTag = 10) {
         SubListLayoutBlock sllb = (SubListLayoutBlock)source.get(index - 1);
         int lineNumber = sllb.lastLineNumber;
         if(lineNumber != Instruction.UNKNOWN_LINE_NUMBER && lineNumber >= minLineNumber) {
            break;
         }

         switch(lastTag) {
         case 2:
         case 13:
         case 16:
         case 19:
         case 22:
         case 25:
         case 31:
            break;
         case 10:
            destination.add(new SeparatorLayoutBlock(2, 1));
            break;
         default:
            destination.add(new SeparatorLayoutBlock(2, 2));
         }

         --index;
         source.remove(index);
         destination.addAll(sllb.subList);
         int lastLineNumber = sllb.lastLineNumber;
         if(lastLineNumber != Instruction.UNKNOWN_LINE_NUMBER && (maxLineNumber == Instruction.UNKNOWN_LINE_NUMBER || maxLineNumber < lastLineNumber)) {
            maxLineNumber = lastLineNumber;
         }
      }

      return maxLineNumber;
   }

   private static int InclusiveMergeBlockList(List<LayoutBlock> destination, List<SubListLayoutBlock> source, int minLineNumber, int maxLineNumber) {
      byte lastTag = ((LayoutBlock)destination.get(destination.size() - 1)).tag;

      for(int index = source.size(); index > 0; lastTag = 0) {
         SubListLayoutBlock sllb = (SubListLayoutBlock)source.get(index - 1);
         int lineNumber = sllb.lastLineNumber;
         if(lineNumber != Instruction.UNKNOWN_LINE_NUMBER && lineNumber > minLineNumber) {
            break;
         }

         switch(lastTag) {
         case 2:
         case 13:
         case 16:
         case 19:
         case 22:
         case 25:
         case 31:
            break;
         default:
            destination.add(new SeparatorLayoutBlock(2, 2));
         }

         destination.addAll(sllb.subList);
         int lastLineNumber = sllb.lastLineNumber;
         if(lastLineNumber != Instruction.UNKNOWN_LINE_NUMBER && (maxLineNumber == Instruction.UNKNOWN_LINE_NUMBER || maxLineNumber < lastLineNumber)) {
            maxLineNumber = lastLineNumber;
         }

         --index;
         source.remove(index);
         if(lineNumber == minLineNumber) {
            break;
         }
      }

      return maxLineNumber;
   }

   private static int MergeBlockList(List<LayoutBlock> destination, List<SubListLayoutBlock> source, int maxLineNumber) {
      byte lastTag = ((LayoutBlock)destination.get(destination.size() - 1)).tag;

      for(int index = source.size(); index-- > 0; lastTag = 0) {
         switch(lastTag) {
         case 2:
         case 13:
         case 16:
         case 19:
         case 22:
         case 25:
         case 31:
            break;
         default:
            destination.add(new SeparatorLayoutBlock(2, 2));
         }

         SubListLayoutBlock sllb = (SubListLayoutBlock)source.remove(index);
         destination.addAll(sllb.subList);
         int lastLineNumber = sllb.lastLineNumber;
         if(lastLineNumber != Instruction.UNKNOWN_LINE_NUMBER && (maxLineNumber == Instruction.UNKNOWN_LINE_NUMBER || maxLineNumber < lastLineNumber)) {
            maxLineNumber = lastLineNumber;
         }
      }

      return maxLineNumber;
   }

   private static int MergeFieldBlockList(List<LayoutBlock> destination, List<SubListLayoutBlock> source, int maxLineNumber) {
      byte lastTag = ((LayoutBlock)destination.get(destination.size() - 1)).tag;

      for(int index = source.size(); index-- > 0; lastTag = 10) {
         switch(lastTag) {
         case 2:
         case 13:
         case 16:
         case 19:
         case 22:
         case 25:
         case 31:
            break;
         case 10:
            destination.add(new SeparatorLayoutBlock(2, 1));
            break;
         default:
            destination.add(new SeparatorLayoutBlock(2, 2));
         }

         SubListLayoutBlock sllb = (SubListLayoutBlock)source.remove(index);
         destination.addAll(sllb.subList);
         int lastLineNumber = sllb.lastLineNumber;
         if(lastLineNumber != Instruction.UNKNOWN_LINE_NUMBER && (maxLineNumber == Instruction.UNKNOWN_LINE_NUMBER || maxLineNumber < lastLineNumber)) {
            maxLineNumber = lastLineNumber;
         }
      }

      return maxLineNumber;
   }

   private static int SearchMinimalLineNumber(List<? extends LayoutBlock> list) {
      int index = list.size();

      while(index-- > 0) {
         int lineNumber = ((LayoutBlock)list.get(index)).lastLineNumber;
         if(lineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
            return lineNumber;
         }
      }

      return Instruction.UNKNOWN_LINE_NUMBER;
   }

   private static void LayoutBlocks(ArrayList<LayoutBlock> layoutBlockList) {
      ArrayList layoutSectionList = new ArrayList();
      CreateSections(layoutBlockList, layoutSectionList);
      InitializeBlocks(layoutBlockList, layoutSectionList);
      int layoutCount = 20;

      do {
         LayoutSections(layoutBlockList, layoutSectionList);
         ScoreSections(layoutBlockList, layoutSectionList);
         if(!SliceDownBlocks(layoutBlockList, layoutSectionList)) {
            break;
         }

         ResetLineCounts(layoutBlockList, layoutSectionList);
      } while(layoutCount-- > 0);

      layoutCount = 20;

      do {
         LayoutSections(layoutBlockList, layoutSectionList);
         ScoreSections(layoutBlockList, layoutSectionList);
         if(!SliceUpBlocks(layoutBlockList, layoutSectionList)) {
            break;
         }

         ResetLineCounts(layoutBlockList, layoutSectionList);
      } while(layoutCount-- > 0);

   }

   private static void CreateSections(ArrayList<LayoutBlock> layoutBlockList, ArrayList<LayoutSection> layoutSectionList) {
      int blockLength = layoutBlockList.size();
      int layoutSectionListSize = 0;
      int firstBlockIndex = 0;
      int firstLineNumber = 1;
      boolean containsError = false;

      for(int blockIndex = 1; blockIndex < blockLength; ++blockIndex) {
         LayoutBlock lb = (LayoutBlock)layoutBlockList.get(blockIndex);
         if(lb.tag == 56) {
            containsError = true;
         }

         if(lb.firstLineNumber != Instruction.UNKNOWN_LINE_NUMBER) {
            if(firstLineNumber > lb.firstLineNumber) {
               containsError = true;
            }

            layoutSectionList.add(new LayoutSection(layoutSectionListSize++, firstBlockIndex, blockIndex - 1, firstLineNumber, lb.firstLineNumber, containsError));
            firstBlockIndex = blockIndex + 1;
            firstLineNumber = lb.lastLineNumber;
            containsError = false;
         }
      }

      if(firstBlockIndex < blockLength - 1) {
         layoutSectionList.add(new LayoutSection(layoutSectionListSize++, firstBlockIndex, blockLength - 1, firstLineNumber, Instruction.UNKNOWN_LINE_NUMBER, containsError));
      }

   }

   private static void InitializeBlocks(ArrayList<LayoutBlock> layoutBlockList, ArrayList<LayoutSection> layoutSectionList) {
      boolean blockIndex = false;
      int sectionLength = layoutSectionList.size();

      for(int sectionIndex = 0; sectionIndex < sectionLength; ++sectionIndex) {
         LayoutSection section = (LayoutSection)layoutSectionList.get(sectionIndex);
         int lastBlockIndex = section.lastBlockIndex;

         for(int var8 = section.firstBlockIndex; var8 <= lastBlockIndex; ++var8) {
            LayoutBlock lb = (LayoutBlock)layoutBlockList.get(var8);
            lb.index = var8;
            lb.section = section;
         }
      }

   }

   private static void ResetLineCounts(ArrayList<LayoutBlock> layoutBlockList, ArrayList<LayoutSection> layoutSectionList) {
      int sectionLength = layoutSectionList.size();

      for(int sectionIndex = 0; sectionIndex < sectionLength; ++sectionIndex) {
         LayoutSection section = (LayoutSection)layoutSectionList.get(sectionIndex);
         if(section.relayout) {
            int lastBlockIndex = section.lastBlockIndex;

            for(int blockIndex = section.firstBlockIndex; blockIndex <= lastBlockIndex; ++blockIndex) {
               LayoutBlock lb = (LayoutBlock)layoutBlockList.get(blockIndex);
               lb.lineCount = lb.preferedLineCount;
            }
         }
      }

   }

   private static void LayoutSections(ArrayList<LayoutBlock> layoutBlockList, ArrayList<LayoutSection> layoutSectionList) {
      int sectionLength = layoutSectionList.size();
      if(sectionLength > 0) {
         --sectionLength;
         int layoutCount = 5;

         boolean redo;
         int sectionIndex;
         LayoutSection section;
         int originalLineCount;
         int currentLineCount;
         do {
            redo = false;

            for(sectionIndex = 0; sectionIndex < sectionLength; ++sectionIndex) {
               section = (LayoutSection)layoutSectionList.get(sectionIndex);
               if(section.relayout && !section.containsError) {
                  section.relayout = false;
                  originalLineCount = section.originalLineCount;
                  currentLineCount = GetLineCount(layoutBlockList, section.firstBlockIndex, section.lastBlockIndex);
                  if(originalLineCount > currentLineCount) {
                     ExpandBlocksWithHeuristics(layoutBlockList, section.firstBlockIndex, section.lastBlockIndex, originalLineCount - currentLineCount);
                     redo = true;
                  } else if(currentLineCount > originalLineCount) {
                     CompactBlocksWithHeuristics(layoutBlockList, section.firstBlockIndex, section.lastBlockIndex, currentLineCount - originalLineCount);
                     redo = true;
                  }
               }
            }

            ((LayoutSection)layoutSectionList.get(sectionLength)).relayout = false;
         } while(redo && layoutCount-- > 0);

         if(redo) {
            for(sectionIndex = 0; sectionIndex < sectionLength; ++sectionIndex) {
               section = (LayoutSection)layoutSectionList.get(sectionIndex);
               if(section.relayout && !section.containsError) {
                  section.relayout = false;
                  originalLineCount = section.originalLineCount;
                  currentLineCount = GetLineCount(layoutBlockList, section.firstBlockIndex, section.lastBlockIndex);
                  if(originalLineCount > currentLineCount) {
                     ExpandBlocks(layoutBlockList, section.firstBlockIndex, section.lastBlockIndex, originalLineCount - currentLineCount);
                  } else if(currentLineCount > originalLineCount) {
                     CompactBlocks(layoutBlockList, section.firstBlockIndex, section.lastBlockIndex, currentLineCount - originalLineCount);
                  }
               }
            }

            ((LayoutSection)layoutSectionList.get(sectionLength)).relayout = false;
         }
      }

   }

   private static int GetLineCount(ArrayList<LayoutBlock> layoutBlockList, int firstIndex, int lastIndex) {
      int sum = 0;

      for(int index = firstIndex; index <= lastIndex; ++index) {
         int lineCount = ((LayoutBlock)layoutBlockList.get(index)).lineCount;
         if(lineCount != Integer.MAX_VALUE) {
            sum += lineCount;
         }
      }

      return sum;
   }

   private static void CompactBlocksWithHeuristics(ArrayList<LayoutBlock> layoutBlockList, int firstIndex, int lastIndex, int delta) {
      int oldDelta;
      int i;
      LayoutBlock lb;
      do {
         oldDelta = delta;
         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 2:
            case 5:
               if(lb.lineCount > 2) {
                  --lb.lineCount;
                  --delta;
               }
            case 3:
            case 4:
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 45:
            case 49:
            case 53:
               if(lb.lineCount > 0) {
                  --lb.lineCount;
                  --delta;
               }
            default:
               --i;
            }
         }

         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 43:
            case 44:
            case 47:
            case 48:
               if(lb.lineCount > 0) {
                  --lb.lineCount;
                  --delta;
               }
            case 45:
            case 46:
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      i = lastIndex;

      while(i >= firstIndex && delta > 0) {
         lb = (LayoutBlock)layoutBlockList.get(i);
         switch(lb.tag) {
         case 6:
            if(lb.lineCount > 0) {
               if(lb.lineCount >= delta) {
                  lb.lineCount -= delta;
                  delta = 0;
               } else {
                  delta -= lb.lineCount;
                  lb.lineCount = 0;
               }
            }
         default:
            --i;
         }
      }

      do {
         oldDelta = delta;
         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 13:
            case 16:
            case 19:
            case 22:
            case 25:
            case 28:
            case 31:
               if(lb.lineCount > 1 && lb.lineCount > lb.minimalLineCount) {
                  --lb.lineCount;
                  --delta;
               }
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 5:
               if(lb.lineCount > 1) {
                  --lb.lineCount;
                  --delta;
               }
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 14:
            case 17:
            case 20:
            case 23:
            case 26:
            case 29:
            case 32:
               if(lb.lineCount > 1 && lb.lineCount > lb.minimalLineCount) {
                  --lb.lineCount;
                  --delta;
               }
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 65:
            case 66:
            case 67:
               if(lb.lineCount > 0) {
                  --lb.lineCount;
                  --delta;
               }
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = firstIndex;

         while(i <= lastIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 29:
               if(lb.lineCount > lb.minimalLineCount) {
                  --lb.lineCount;
                  --delta;
               }
            default:
               ++i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 33:
            case 34:
               if(lb.lineCount > 0) {
                  --lb.lineCount;
                  --delta;
               }
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      BlockLayoutBlock blb;
      do {
         oldDelta = delta;
         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 22:
            case 23:
               if(lb.lineCount > lb.minimalLineCount) {
                  blb = (BlockLayoutBlock)lb;
                  --lb.lineCount;
                  --delta;
                  if(lb.lineCount <= 1) {
                     if(blb.section == blb.other.section) {
                        if(blb.other.lineCount > delta) {
                           blb.other.lineCount -= delta;
                           delta = 0;
                        } else {
                           delta -= blb.other.lineCount;
                           blb.other.lineCount = 0;
                        }
                     } else {
                        blb.other.section.relayout = true;
                        blb.other.lineCount = 0;
                     }
                  }
               }
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;

         for(i = lastIndex; i >= firstIndex && delta > 0; --i) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 13:
            case 16:
            case 19:
            case 25:
            case 31:
               if(lb.lineCount > lb.minimalLineCount) {
                  --lb.lineCount;
                  --delta;
               }
               break;
            case 28:
               if(lb.lineCount > lb.minimalLineCount) {
                  --lb.lineCount;
                  --delta;
                  if(lb.lineCount == 0) {
                     blb = (BlockLayoutBlock)lb;
                     if(blb.section == blb.other.section) {
                        if(blb.other.lineCount > delta) {
                           blb.other.lineCount -= delta;
                           delta = 0;
                        } else {
                           delta -= blb.other.lineCount;
                           blb.other.lineCount = 0;
                        }
                     } else {
                        blb.other.section.relayout = true;
                        blb.other.lineCount = 0;
                     }
                  }
               }
            }
         }

         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 14:
            case 17:
            case 20:
            case 26:
            case 32:
               if(lb.lineCount > lb.minimalLineCount) {
                  --lb.lineCount;
                  --delta;
               }
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 2:
            case 5:
               if(lb.lineCount > 0) {
                  --lb.lineCount;
                  --delta;
               }
            case 3:
            case 4:
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 40:
               if(lb.lineCount > 0) {
                  --lb.lineCount;
                  --delta;
               }
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

   }

   private static void ExpandBlocksWithHeuristics(ArrayList<LayoutBlock> layoutBlockList, int firstIndex, int lastIndex, int delta) {
      int oldDelta;
      int i;
      LayoutBlock lb;
      do {
         oldDelta = delta;
         i = firstIndex;

         while(i <= lastIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 44:
            case 45:
            case 48:
            case 49:
               if(lb.lineCount < lb.maximalLineCount) {
                  ++lb.lineCount;
                  --delta;
               }
            case 46:
            case 47:
            default:
               ++i;
            }
         }

         i = firstIndex;

         while(i <= lastIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 43:
            case 47:
               if(lb.lineCount < lb.maximalLineCount) {
                  ++lb.lineCount;
                  --delta;
               }
            case 44:
            case 45:
            case 46:
            default:
               ++i;
            }
         }

         i = firstIndex;

         while(i <= lastIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 3:
            case 4:
               lb.lineCount += delta;
               delta = 0;
            default:
               ++i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = firstIndex;

         while(i <= lastIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 37:
               if(lb.lineCount < lb.maximalLineCount) {
                  ++lb.lineCount;
                  --delta;
               }
            default:
               ++i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 34:
               if(lb.lineCount == 0) {
                  ++lb.lineCount;
                  --delta;
               }
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      BlockLayoutBlock blb;
      do {
         oldDelta = delta;
         i = firstIndex;

         while(i <= lastIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 14:
            case 17:
            case 20:
            case 26:
            case 29:
            case 32:
               if(lb.lineCount == 0) {
                  blb = (BlockLayoutBlock)lb;
                  ++lb.lineCount;
                  --delta;
                  if(blb.other.lineCount == 0) {
                     if(blb.section == blb.other.section) {
                        if(delta > 0) {
                           blb.other.lineCount = blb.other.lineCount + 1;
                           --delta;
                        }
                     } else {
                        blb.other.section.relayout = true;
                        blb.other.lineCount = 1;
                     }
                  }
               }
            default:
               ++i;
            }
         }

         i = firstIndex;

         while(i <= lastIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 13:
            case 16:
            case 19:
            case 25:
            case 31:
               if(lb.lineCount == 0) {
                  blb = (BlockLayoutBlock)lb;
                  ++lb.lineCount;
                  --delta;
                  if(blb.section == blb.other.section) {
                     int d = 2 - blb.other.lineCount;
                     if(d > delta) {
                        blb.other.lineCount += delta;
                        delta = 0;
                     } else {
                        delta -= d;
                        blb.other.lineCount = 2;
                     }
                  } else {
                     blb.other.section.relayout = true;
                     blb.other.lineCount = 2;
                  }
               }
            default:
               ++i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = firstIndex;

         while(i <= lastIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 2:
            case 5:
               ++lb.lineCount;
               --delta;
            case 3:
            case 4:
            default:
               ++i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = lastIndex;

         while(i >= firstIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 34:
               if(lb.lineCount < lb.maximalLineCount) {
                  ++lb.lineCount;
                  --delta;
               }
            default:
               --i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

      do {
         oldDelta = delta;
         i = firstIndex;

         while(i <= lastIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 14:
            case 17:
            case 20:
            case 23:
            case 26:
            case 29:
            case 32:
               if(lb.lineCount < lb.maximalLineCount) {
                  ++lb.lineCount;
                  --delta;
               }
            default:
               ++i;
            }
         }

         i = firstIndex;

         while(i <= lastIndex && delta > 0) {
            lb = (LayoutBlock)layoutBlockList.get(i);
            switch(lb.tag) {
            case 13:
            case 16:
            case 19:
            case 22:
            case 25:
            case 28:
            case 31:
               if(lb.lineCount < lb.maximalLineCount) {
                  blb = (BlockLayoutBlock)lb;
                  ++lb.lineCount;
                  --delta;
                  if(lb.lineCount > 1 && blb.other.lineCount == 0) {
                     if(blb.section == blb.other.section) {
                        if(delta > 0) {
                           blb.other.lineCount = 1;
                           --delta;
                        }
                     } else {
                        blb.other.section.relayout = true;
                        blb.other.lineCount = 1;
                     }
                  }
               }
            default:
               ++i;
            }
         }
      } while(delta > 0 && oldDelta > delta);

   }

   private static void CompactBlocks(ArrayList<LayoutBlock> layoutBlockList, int firstIndex, int lastIndex, int delta) {
      int oldDelta;
      do {
         oldDelta = delta;

         for(int i = lastIndex; i >= firstIndex && delta > 0; --i) {
            LayoutBlock lb = (LayoutBlock)layoutBlockList.get(i);
            if(lb.lineCount > lb.minimalLineCount) {
               --lb.lineCount;
               --delta;
            }
         }
      } while(delta > 0 && oldDelta > delta);

   }

   private static void ExpandBlocks(ArrayList<LayoutBlock> layoutBlockList, int firstIndex, int lastIndex, int delta) {
      int oldDelta;
      do {
         oldDelta = delta;

         for(int i = firstIndex; i <= lastIndex && delta > 0; ++i) {
            LayoutBlock lb = (LayoutBlock)layoutBlockList.get(i);
            if(lb.lineCount < lb.maximalLineCount) {
               ++lb.lineCount;
               --delta;
            }
         }
      } while(delta > 0 && oldDelta > delta);

   }

   private static void ScoreSections(ArrayList<LayoutBlock> layoutBlockList, ArrayList<LayoutSection> layoutSectionList) {
      int sectionLength = layoutSectionList.size();
      if(sectionLength > 0) {
         --sectionLength;

         for(int sectionIndex = 0; sectionIndex < sectionLength; ++sectionIndex) {
            LayoutSection section = (LayoutSection)layoutSectionList.get(sectionIndex);
            int lastBlockIndex = section.lastBlockIndex;
            int score = 0;
            int sumScore = 0;
            int blockIndex = section.firstBlockIndex;

            while(blockIndex <= lastBlockIndex) {
               LayoutBlock lb = (LayoutBlock)layoutBlockList.get(blockIndex);
               switch(lb.tag) {
               case 2:
                  if(lb.lineCount < lb.preferedLineCount) {
                     sumScore += lb.preferedLineCount - lb.lineCount;
                     if(lb.lineCount > 0) {
                        score += sumScore * sumScore;
                        sumScore = 0;
                     }
                  } else if(lb.lineCount > lb.preferedLineCount) {
                     int delta = lb.lineCount - lb.preferedLineCount;
                     score -= delta * delta;
                  }
               default:
                  ++blockIndex;
               }
            }

            score += sumScore * sumScore;
            section.score = score;
         }
      }

   }

   private static boolean SliceDownBlocks(ArrayList<LayoutBlock> layoutBlockList, ArrayList<LayoutSection> layoutSectionList) {
      int sectionLenght = layoutSectionList.size();
      ArrayList sortedLayoutSectionList = new ArrayList(sectionLenght);
      sortedLayoutSectionList.addAll(layoutSectionList);
      Collections.sort(sortedLayoutSectionList);

      for(int sectionSourceIndex = 0; sectionSourceIndex < sectionLenght; ++sectionSourceIndex) {
         LayoutSection lsSource = (LayoutSection)sortedLayoutSectionList.get(sectionSourceIndex);
         if(lsSource.score <= 0) {
            break;
         }

         if(SliceDownBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, lsSource)) {
            return true;
         }
      }

      return false;
   }

   private static boolean SliceUpBlocks(ArrayList<LayoutBlock> layoutBlockList, ArrayList<LayoutSection> layoutSectionList) {
      int sectionLenght = layoutSectionList.size();
      ArrayList sortedLayoutSectionList = new ArrayList(sectionLenght);
      sortedLayoutSectionList.addAll(layoutSectionList);
      Collections.sort(sortedLayoutSectionList);

      for(int sectionSourceIndex = 0; sectionSourceIndex < sectionLenght; ++sectionSourceIndex) {
         LayoutSection lsSource = (LayoutSection)sortedLayoutSectionList.get(sectionSourceIndex);
         if(lsSource.score <= 0) {
            break;
         }

         if(SliceUpBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, lsSource)) {
            return true;
         }
      }

      return false;
   }

   private static boolean SliceDownBlocks(ArrayList<LayoutBlock> layoutBlockList, ArrayList<LayoutSection> layoutSectionList, int sectionSourceIndex, LayoutSection lsSource) {
      int firstBlockIndex = lsSource.firstBlockIndex;

      for(int blockIndex = lsSource.lastBlockIndex; blockIndex >= firstBlockIndex; --blockIndex) {
         LayoutBlock lb = (LayoutBlock)layoutBlockList.get(blockIndex);
         switch(lb.tag) {
         case 7:
            if(SliceDownBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, blockIndex, lsSource, 11, 12)) {
               return true;
            }

            if(SliceDownBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, blockIndex, lsSource, 9, 10)) {
               return true;
            }
         case 8:
         case 10:
         default:
            break;
         case 9:
            if(SliceDownBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, blockIndex, lsSource, 7, 8)) {
               return true;
            }

            if(SliceDownBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, blockIndex, lsSource, 11, 12)) {
               return true;
            }
            break;
         case 11:
            if(SliceDownBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, blockIndex, lsSource, 7, 8)) {
               return true;
            }

            if(SliceDownBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, blockIndex, lsSource, 9, 10)) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean SliceDownBlocks(ArrayList<LayoutBlock> layoutBlockList, ArrayList<LayoutSection> layoutSectionList, int sectionSourceIndex, int blockIndex, LayoutSection lsSource, int markerStartTag, int markerEndTag) {
      int firstBlockIndex = lsSource.firstBlockIndex;

      while(firstBlockIndex < blockIndex) {
         --blockIndex;
         LayoutBlock lb = (LayoutBlock)layoutBlockList.get(blockIndex);
         if(lb.tag == markerEndTag) {
            MarkerLayoutBlock mlb = (MarkerLayoutBlock)lb;
            if(mlb.section != mlb.other.section) {
               return false;
            }

            if(mlb.other.index <= firstBlockIndex) {
               return false;
            }

            int lastEndTagBlockIndex = blockIndex;
            int counter = 1;
            blockIndex = mlb.other.index;

            while(firstBlockIndex < blockIndex) {
               --blockIndex;
               lb = (LayoutBlock)layoutBlockList.get(blockIndex);
               if(lb.tag == 13) {
                  break;
               }

               if(lb.tag == markerEndTag) {
                  mlb = (MarkerLayoutBlock)lb;
                  if(mlb.section != mlb.other.section || mlb.other.index <= firstBlockIndex) {
                     break;
                  }

                  ++counter;
                  blockIndex = mlb.other.index;
               } else if(lb.tag == 10 || lb.tag == 12 || lb.tag == 8) {
                  break;
               }
            }

            int blockLenght = layoutBlockList.size();
            blockIndex = lastEndTagBlockIndex;
            int lowerScore = lsSource.score;
            int lowerScoreBlockIndex = lastEndTagBlockIndex;

            while(true) {
               ++blockIndex;
               if(blockIndex >= blockLenght) {
                  break;
               }

               lb = (LayoutBlock)layoutBlockList.get(blockIndex);
               if(lb.tag == 14 || lb.tag == markerStartTag) {
                  if(lowerScore > lb.section.score) {
                     lowerScore = lb.section.score;
                     lowerScoreBlockIndex = blockIndex;
                  }
                  break;
               }

               if(lb.tag == 9 || lb.tag == 11 || lb.tag == 7) {
                  if(lb.section != null && lowerScore > lb.section.score) {
                     lowerScore = lb.section.score;
                     lowerScoreBlockIndex = blockIndex;
                  }

                  blockIndex = ((MarkerLayoutBlock)lb).other.index;
               }
            }

            if(lowerScore != lsSource.score) {
               counter = (counter + 1) / 2;
               blockIndex = lastEndTagBlockIndex;

               int firstStartTagBlockIndex;
               for(firstStartTagBlockIndex = lastEndTagBlockIndex; firstBlockIndex < blockIndex; --blockIndex) {
                  lb = (LayoutBlock)layoutBlockList.get(blockIndex);
                  if(lb.tag == 13) {
                     break;
                  }

                  if(lb.tag == markerEndTag) {
                     blockIndex = ((MarkerLayoutBlock)lb).other.index;
                     firstStartTagBlockIndex = ((MarkerLayoutBlock)lb).other.index;
                     --counter;
                     if(counter == 0) {
                        break;
                     }
                  }
               }

               LayoutBlock insertionLayoutBlock = (LayoutBlock)layoutBlockList.get(lowerScoreBlockIndex);
               LayoutSection lsTarget = insertionLayoutBlock.section;
               int sourceDeltaIndex = lastEndTagBlockIndex - firstStartTagBlockIndex + 1;
               ArrayList layoutBlockListToMove = new ArrayList(sourceDeltaIndex);

               for(blockIndex = lastEndTagBlockIndex; blockIndex >= firstStartTagBlockIndex; --blockIndex) {
                  lb = (LayoutBlock)layoutBlockList.remove(blockIndex);
                  lb.section = lsTarget;
                  layoutBlockListToMove.add(lb);
               }

               Collections.reverse(layoutBlockListToMove);
               if(((LayoutBlock)layoutBlockList.get(blockIndex + 1)).tag == 2) {
                  layoutBlockList.remove(blockIndex + 1);
                  ++sourceDeltaIndex;
               }

               if(((LayoutBlock)layoutBlockList.get(blockIndex)).tag == 2) {
                  ((LayoutBlock)layoutBlockList.get(blockIndex)).preferedLineCount = 2;
               }

               lowerScoreBlockIndex -= sourceDeltaIndex;
               int targetDeltaIndex = 0;
               byte delta;
               if(insertionLayoutBlock.tag == 14) {
                  byte layoutBlockListToMoveSize = 2;
                  if(markerEndTag == 10 && ((LayoutBlock)layoutBlockList.get(lowerScoreBlockIndex - 1)).tag == 10) {
                     layoutBlockListToMoveSize = 1;
                  }

                  layoutBlockList.add(lowerScoreBlockIndex, new SeparatorLayoutBlock(2, layoutBlockListToMoveSize));
                  ++targetDeltaIndex;
               } else {
                  LayoutBlock var25 = (LayoutBlock)layoutBlockList.get(lowerScoreBlockIndex - 1);
                  delta = 2;
                  if(markerEndTag == 10 && ((LayoutBlock)layoutBlockList.get(lowerScoreBlockIndex - 2)).tag == 10) {
                     delta = 1;
                  }

                  var25.preferedLineCount = delta;
               }

               int var26 = layoutBlockListToMove.size();
               layoutBlockList.addAll(lowerScoreBlockIndex + targetDeltaIndex, layoutBlockListToMove);
               targetDeltaIndex += var26;
               if(insertionLayoutBlock.tag != 14) {
                  delta = 2;
                  if(markerStartTag == 9) {
                     delta = 1;
                  }

                  layoutBlockList.add(lowerScoreBlockIndex + targetDeltaIndex, new SeparatorLayoutBlock(2, delta));
                  ++targetDeltaIndex;
               }

               lsSource.lastBlockIndex -= sourceDeltaIndex;

               int var28;
               for(var28 = lsSource.index + 1; var28 <= lsTarget.index - 1; ++var28) {
                  LayoutSection sectionIndex = (LayoutSection)layoutSectionList.get(var28);
                  sectionIndex.firstBlockIndex -= sourceDeltaIndex;
                  sectionIndex.lastBlockIndex -= sourceDeltaIndex;
               }

               lsTarget.firstBlockIndex -= sourceDeltaIndex;
               var28 = sourceDeltaIndex - targetDeltaIndex;
               if(var28 != 0) {
                  lsTarget.lastBlockIndex -= var28;

                  for(int var27 = layoutSectionList.size() - 1; var27 > lsTarget.index; --var27) {
                     LayoutSection ls = (LayoutSection)layoutSectionList.get(var27);
                     ls.firstBlockIndex -= var28;
                     ls.lastBlockIndex -= var28;
                  }
               }

               blockLenght = layoutBlockList.size();

               for(blockIndex = firstStartTagBlockIndex; blockIndex < blockLenght; ((LayoutBlock)layoutBlockList.get(blockIndex)).index = blockIndex++) {
                  ;
               }

               UpdateRelayoutFlag(layoutBlockList, lsSource);
               UpdateRelayoutFlag(layoutBlockList, lsTarget);
               return true;
            }
            break;
         }

         if(lb.tag != 10 && lb.tag != 12 && lb.tag != 8) {
            if(lb.tag == 13) {
               break;
            }
         } else {
            blockIndex = ((MarkerLayoutBlock)lb).other.index;
         }
      }

      return false;
   }

   private static boolean SliceUpBlocks(ArrayList<LayoutBlock> layoutBlockList, ArrayList<LayoutSection> layoutSectionList, int sectionSourceIndex, LayoutSection lsSource) {
      int lastBlockIndex = lsSource.lastBlockIndex;
      int blockIndex = lsSource.firstBlockIndex;

      while(blockIndex <= lastBlockIndex) {
         LayoutBlock lb = (LayoutBlock)layoutBlockList.get(blockIndex);
         switch(lb.tag) {
         case 8:
            if(SliceUpBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, blockIndex, lsSource, 9, 10)) {
               return true;
            }

            if(SliceUpBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, blockIndex, lsSource, 11, 12)) {
               return true;
            }

            return false;
         case 9:
         case 11:
         default:
            ++blockIndex;
            break;
         case 10:
            if(SliceUpBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, blockIndex, lsSource, 11, 12)) {
               return true;
            }

            if(SliceUpBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, blockIndex, lsSource, 7, 8)) {
               return true;
            }

            return false;
         case 12:
            if(SliceUpBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, blockIndex, lsSource, 9, 10)) {
               return true;
            }

            if(SliceUpBlocks(layoutBlockList, layoutSectionList, sectionSourceIndex, blockIndex, lsSource, 7, 8)) {
               return true;
            }

            return false;
         }
      }

      return false;
   }

   private static boolean SliceUpBlocks(ArrayList<LayoutBlock> layoutBlockList, ArrayList<LayoutSection> layoutSectionList, int sectionSourceIndex, int blockIndex, LayoutSection lsSource, int markerStartTag, int markerEndTag) {
      int lastBlockIndex = lsSource.lastBlockIndex;

      while(blockIndex < lastBlockIndex) {
         ++blockIndex;
         LayoutBlock lb = (LayoutBlock)layoutBlockList.get(blockIndex);
         if(lb.tag == markerStartTag) {
            MarkerLayoutBlock mlb = (MarkerLayoutBlock)lb;
            if(mlb.section != mlb.other.section) {
               return false;
            }

            if(mlb.other.index >= lastBlockIndex) {
               return false;
            }

            int firstStartTagBlockIndex = blockIndex;
            int counter = 1;
            blockIndex = mlb.other.index;

            while(blockIndex < lastBlockIndex) {
               ++blockIndex;
               lb = (LayoutBlock)layoutBlockList.get(blockIndex);
               if(lb.tag == 14) {
                  break;
               }

               if(lb.tag == markerStartTag) {
                  mlb = (MarkerLayoutBlock)lb;
                  if(mlb.section != mlb.other.section || mlb.other.index >= lastBlockIndex) {
                     break;
                  }

                  ++counter;
                  blockIndex = mlb.other.index;
               } else if(lb.tag == 9 || lb.tag == 11 || lb.tag == 7) {
                  break;
               }
            }

            blockIndex = firstStartTagBlockIndex;
            int lowerScore = lsSource.score;
            int lowerScoreBlockIndex = firstStartTagBlockIndex;

            while(blockIndex-- > 0) {
               lb = (LayoutBlock)layoutBlockList.get(blockIndex);
               if(lb.tag == 13 || lb.tag == markerEndTag) {
                  if(lowerScore > lb.section.score) {
                     lowerScore = lb.section.score;
                     lowerScoreBlockIndex = blockIndex;
                  }
                  break;
               }

               if(lb.tag == 10 || lb.tag == 12 || lb.tag == 8) {
                  if(lb.section != null && lowerScore > lb.section.score) {
                     lowerScore = lb.section.score;
                     lowerScoreBlockIndex = blockIndex;
                  }

                  blockIndex = ((MarkerLayoutBlock)lb).other.index;
               }
            }

            if(lowerScore != lsSource.score) {
               counter = (counter + 1) / 2;
               blockIndex = firstStartTagBlockIndex;

               int lastEndTagBlockIndex;
               for(lastEndTagBlockIndex = firstStartTagBlockIndex; blockIndex > 0; ++blockIndex) {
                  lb = (LayoutBlock)layoutBlockList.get(blockIndex);
                  if(lb.tag == 14) {
                     break;
                  }

                  if(lb.tag == markerStartTag) {
                     blockIndex = ((MarkerLayoutBlock)lb).other.index;
                     lastEndTagBlockIndex = ((MarkerLayoutBlock)lb).other.index;
                     --counter;
                     if(counter == 0) {
                        break;
                     }
                  }
               }

               LayoutBlock insertionLayoutBlock = (LayoutBlock)layoutBlockList.get(lowerScoreBlockIndex);
               LayoutSection lsTarget = insertionLayoutBlock.section;
               int sourceDeltaIndex = lastEndTagBlockIndex - firstStartTagBlockIndex + 1;
               ArrayList layoutBlockListToMove = new ArrayList(sourceDeltaIndex);

               for(blockIndex = lastEndTagBlockIndex; blockIndex >= firstStartTagBlockIndex; --blockIndex) {
                  lb = (LayoutBlock)layoutBlockList.remove(blockIndex);
                  lb.section = lsTarget;
                  layoutBlockListToMove.add(lb);
               }

               Collections.reverse(layoutBlockListToMove);
               if(((LayoutBlock)layoutBlockList.get(blockIndex + 1)).tag == 2) {
                  layoutBlockList.remove(blockIndex + 1);
                  ++sourceDeltaIndex;
               }

               if(((LayoutBlock)layoutBlockList.get(blockIndex)).tag == 2) {
                  ((LayoutBlock)layoutBlockList.get(blockIndex)).preferedLineCount = 2;
               }

               ++lowerScoreBlockIndex;
               int targetDeltaIndex = 0;
               if(insertionLayoutBlock.tag != 13) {
                  byte layoutBlockListToMoveSize = 2;
                  if(markerEndTag == 10) {
                     layoutBlockListToMoveSize = 1;
                  }

                  layoutBlockList.add(lowerScoreBlockIndex, new SeparatorLayoutBlock(2, layoutBlockListToMoveSize));
                  ++targetDeltaIndex;
               }

               int var24 = layoutBlockListToMove.size();
               layoutBlockList.addAll(lowerScoreBlockIndex + targetDeltaIndex, layoutBlockListToMove);
               targetDeltaIndex += var24;
               if(insertionLayoutBlock.tag == 13) {
                  byte delta = 2;
                  if(markerEndTag == 10 && ((LayoutBlock)layoutBlockList.get(lowerScoreBlockIndex + targetDeltaIndex)).tag == 10) {
                     delta = 1;
                  }

                  layoutBlockList.add(lowerScoreBlockIndex + targetDeltaIndex, new SeparatorLayoutBlock(2, delta));
                  ++targetDeltaIndex;
               } else {
                  LayoutBlock var25 = (LayoutBlock)layoutBlockList.get(lowerScoreBlockIndex + targetDeltaIndex);
                  byte blockLenght = 2;
                  if(markerStartTag == 9 && ((LayoutBlock)layoutBlockList.get(lowerScoreBlockIndex + targetDeltaIndex + 1)).tag == 9) {
                     blockLenght = 1;
                  }

                  var25.preferedLineCount = blockLenght;
               }

               lsTarget.lastBlockIndex += targetDeltaIndex;

               int var26;
               for(var26 = lsTarget.index + 1; var26 <= lsSource.index - 1; ++var26) {
                  LayoutSection var27 = (LayoutSection)layoutSectionList.get(var26);
                  var27.firstBlockIndex += targetDeltaIndex;
                  var27.lastBlockIndex += targetDeltaIndex;
               }

               lsSource.firstBlockIndex += targetDeltaIndex;
               var26 = sourceDeltaIndex - targetDeltaIndex;
               int var28;
               if(var26 != 0) {
                  lsSource.lastBlockIndex -= var26;

                  for(var28 = layoutSectionList.size() - 1; var28 > lsSource.index; --var28) {
                     LayoutSection ls = (LayoutSection)layoutSectionList.get(var28);
                     ls.firstBlockIndex -= var26;
                     ls.lastBlockIndex -= var26;
                  }
               }

               var28 = layoutBlockList.size();

               for(blockIndex = lowerScoreBlockIndex; blockIndex < var28; ((LayoutBlock)layoutBlockList.get(blockIndex)).index = blockIndex++) {
                  ;
               }

               UpdateRelayoutFlag(layoutBlockList, lsSource);
               UpdateRelayoutFlag(layoutBlockList, lsTarget);
               return true;
            }
            break;
         }

         if(lb.tag != 9 && lb.tag != 11 && lb.tag != 7) {
            if(lb.tag == 14) {
               break;
            }
         } else {
            blockIndex = ((MarkerLayoutBlock)lb).other.index;
         }
      }

      return false;
   }

   private static void UpdateRelayoutFlag(ArrayList<LayoutBlock> layoutBlockList, LayoutSection section) {
      section.relayout = true;
      int lastBlockIndex = section.lastBlockIndex;
      int blockIndex = section.firstBlockIndex;

      while(blockIndex < lastBlockIndex) {
         LayoutBlock block = (LayoutBlock)layoutBlockList.get(blockIndex);
         switch(block.tag) {
         case 13:
         case 14:
         case 16:
         case 17:
         case 19:
         case 20:
         case 22:
         case 23:
         case 25:
         case 26:
         case 28:
         case 29:
         case 31:
         case 32:
            BlockLayoutBlock blb = (BlockLayoutBlock)block;
            LayoutSection otherSection = blb.other.section;
            if(!otherSection.relayout) {
               UpdateRelayoutFlag(layoutBlockList, otherSection);
            }
         case 15:
         case 18:
         case 21:
         case 24:
         case 27:
         case 30:
         default:
            ++blockIndex;
         }
      }

   }
}
