package jd.core.process.writer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import jd.core.loader.Loader;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.attribute.Annotation;
import jd.core.model.classfile.attribute.AttributeSignature;
import jd.core.model.classfile.attribute.ElementValue;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantValue;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.model.layout.block.AnnotationsLayoutBlock;
import jd.core.model.layout.block.BlockLayoutBlock;
import jd.core.model.layout.block.ByteCodeLayoutBlock;
import jd.core.model.layout.block.CaseEnumLayoutBlock;
import jd.core.model.layout.block.CaseLayoutBlock;
import jd.core.model.layout.block.DeclareLayoutBlock;
import jd.core.model.layout.block.ExtendsSuperInterfacesLayoutBlock;
import jd.core.model.layout.block.ExtendsSuperTypeLayoutBlock;
import jd.core.model.layout.block.FastCatchLayoutBlock;
import jd.core.model.layout.block.FieldNameLayoutBlock;
import jd.core.model.layout.block.GenericExtendsSuperInterfacesLayoutBlock;
import jd.core.model.layout.block.GenericExtendsSuperTypeLayoutBlock;
import jd.core.model.layout.block.GenericImplementsInterfacesLayoutBlock;
import jd.core.model.layout.block.GenericTypeNameLayoutBlock;
import jd.core.model.layout.block.ImplementsInterfacesLayoutBlock;
import jd.core.model.layout.block.ImportsLayoutBlock;
import jd.core.model.layout.block.InstructionLayoutBlock;
import jd.core.model.layout.block.InstructionsLayoutBlock;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.model.layout.block.MarkerLayoutBlock;
import jd.core.model.layout.block.MethodNameLayoutBlock;
import jd.core.model.layout.block.MethodStaticLayoutBlock;
import jd.core.model.layout.block.OffsetLayoutBlock;
import jd.core.model.layout.block.PackageLayoutBlock;
import jd.core.model.layout.block.ThrowsLayoutBlock;
import jd.core.model.layout.block.TypeNameLayoutBlock;
import jd.core.model.reference.Reference;
import jd.core.model.reference.ReferenceMap;
import jd.core.printer.InstructionPrinter;
import jd.core.printer.Printer;
import jd.core.process.writer.AnnotationWriter;
import jd.core.process.writer.ByteCodeWriter;
import jd.core.process.writer.ConstantValueWriter;
import jd.core.process.writer.ElementValueWriter;
import jd.core.process.writer.ReferenceByCountComparator;
import jd.core.process.writer.ReferenceByInternalNameComparator;
import jd.core.process.writer.SignatureWriter;
import jd.core.process.writer.visitor.SourceWriterVisitor;
import jd.core.util.SignatureUtil;
import jd.core.util.StringUtil;
import jd.core.util.TypeNameUtil;

public class ClassFileWriter {
   private static HashSet<String> keywords = new HashSet();
   private static final String[] ACCESS_FIELD_NAMES = new String[]{"public", "private", "protected", "static", "final", null, "volatile", "transient"};
   private static final String[] ACCESS_METHOD_NAMES = new String[]{"public", "private", "protected", "static", "final", "synchronized", null, null, "native", null, "abstract", "strictfp"};
   private static final String[] ACCESS_NESTED_CLASS_NAMES = new String[]{"public", "private", "protected", "static", "final"};
   private static final String[] ACCESS_NESTED_ENUM_NAMES = new String[]{"public", "private", "protected", "static"};
   private Loader loader;
   private Printer printer;
   private InstructionPrinter instructionPrinter;
   private SourceWriterVisitor visitor;
   private ReferenceMap referenceMap;
   private List<LayoutBlock> layoutBlockList;
   private int index;
   private boolean addSpace = false;

   static {
      keywords.add("@interface");
      keywords.add("abstract");
      keywords.add("assert");
      keywords.add("boolean");
      keywords.add("break");
      keywords.add("byte");
      keywords.add("case");
      keywords.add("catch");
      keywords.add("char");
      keywords.add("class");
      keywords.add("const");
      keywords.add("continue");
      keywords.add("default");
      keywords.add("do");
      keywords.add("double");
      keywords.add("else");
      keywords.add("enum");
      keywords.add("extends");
      keywords.add("false");
      keywords.add("final");
      keywords.add("finally");
      keywords.add("float");
      keywords.add("for");
      keywords.add("goto");
      keywords.add("if");
      keywords.add("implements");
      keywords.add("import");
      keywords.add("instanceof");
      keywords.add("int");
      keywords.add("interface");
      keywords.add("long");
      keywords.add("native");
      keywords.add("new");
      keywords.add("null");
      keywords.add("package");
      keywords.add("private");
      keywords.add("protected");
      keywords.add("public");
      keywords.add("return");
      keywords.add("short");
      keywords.add("static");
      keywords.add("strictfp");
      keywords.add("super");
      keywords.add("switch");
      keywords.add("synchronized");
      keywords.add("this");
      keywords.add("throw");
      keywords.add("throws");
      keywords.add("transient");
      keywords.add("true");
      keywords.add("try");
      keywords.add("void");
      keywords.add("volatile");
      keywords.add("while");
   }

   public static void Write(Loader loader, Printer printer, ReferenceMap referenceMap, int maxLineNumber, int majorVersion, int minorVersion, List<LayoutBlock> layoutBlockList) {
      ClassFileWriter cfw = new ClassFileWriter(loader, printer, referenceMap, layoutBlockList);
      cfw.write(maxLineNumber, majorVersion, minorVersion);
   }

   private ClassFileWriter(Loader loader, Printer printer, ReferenceMap referenceMap, List<LayoutBlock> layoutBlockList) {
      this.loader = loader;
      this.printer = printer;
      this.instructionPrinter = new InstructionPrinter(printer);
      this.visitor = new SourceWriterVisitor(loader, this.instructionPrinter, referenceMap, keywords);
      this.referenceMap = referenceMap;
      this.layoutBlockList = layoutBlockList;
      this.index = 0;
   }

   public void write(int maxLineNumber, int majorVersion, int minorVersion) {
      int length = this.layoutBlockList.size();
      this.printer.start(maxLineNumber, majorVersion, minorVersion);
      this.printer.startOfLine(this.searchFirstLineNumber());

      while(this.index < length) {
         LayoutBlock lb = (LayoutBlock)this.layoutBlockList.get(this.index++);
         switch(lb.tag) {
         case 1:
            this.writePackage((PackageLayoutBlock)lb);
            break;
         case 2:
         case 4:
         case 5:
            this.writeSeparator(lb);
            break;
         case 3:
            this.writeSeparatorAtBegining(lb);
            break;
         case 6:
            this.writeImports((ImportsLayoutBlock)lb);
            break;
         case 7:
            this.writeTypeMarkerStart((MarkerLayoutBlock)lb);
            break;
         case 8:
            this.writeTypeMarkerEnd((MarkerLayoutBlock)lb);
            break;
         case 9:
            this.writeFieldMarkerStart((MarkerLayoutBlock)lb);
            break;
         case 10:
            this.writeFieldMarkerEnd((MarkerLayoutBlock)lb);
            break;
         case 11:
            this.writeMethodMarkerStart((MarkerLayoutBlock)lb);
            break;
         case 12:
            this.writeMethodMarkerEnd((MarkerLayoutBlock)lb);
            break;
         case 13:
         case 16:
         case 19:
         case 22:
         case 25:
            this.writeStatementBlockStart(lb);
            break;
         case 14:
         case 20:
         case 23:
         case 26:
            this.writeStatementsBlockEnd(lb);
            break;
         case 15:
         case 18:
         case 21:
         case 27:
            this.writeStatementsBlockStartEnd(lb);
            break;
         case 17:
            this.writeStatementsInnerBodyBlockEnd(lb);
         case 24:
         case 35:
         case 36:
         case 58:
         case 59:
         case 60:
         default:
            break;
         case 28:
            this.writeSingleStatementsBlockStart(lb);
            break;
         case 29:
            this.writeSingleStatementsBlockEnd(lb);
            break;
         case 30:
            this.writeSingleStatementsBlockStartEnd(lb);
            break;
         case 31:
            this.writeSwitchBlockStart(lb);
            break;
         case 32:
            this.writeSwitchBlockEnd(lb);
            break;
         case 33:
            this.writeCaseBlockStart(lb);
            break;
         case 34:
            this.writeCaseBlockEnd(lb);
            break;
         case 37:
            this.writeForBlockStart(lb);
            break;
         case 38:
            this.writeForBlockEnd(lb);
            break;
         case 39:
            this.writeCommentDeprecated(lb);
            break;
         case 40:
            this.writeCommentError(lb);
            break;
         case 41:
            this.writeAnnotations((AnnotationsLayoutBlock)lb);
            break;
         case 42:
            this.writeType((TypeNameLayoutBlock)lb);
            break;
         case 43:
            this.writeExtendsSuperType((ExtendsSuperTypeLayoutBlock)lb);
            break;
         case 44:
            this.writeExtendsSuperInterfaces((ExtendsSuperInterfacesLayoutBlock)lb);
            break;
         case 45:
            this.writeImplementsInterfaces((ImplementsInterfacesLayoutBlock)lb);
            break;
         case 46:
            this.writeGenericType((GenericTypeNameLayoutBlock)lb);
            break;
         case 47:
            this.writeGenericExtendsSuperType((GenericExtendsSuperTypeLayoutBlock)lb);
            break;
         case 48:
            this.writeGenericExtendsSuperInterfaces((GenericExtendsSuperInterfacesLayoutBlock)lb);
            break;
         case 49:
            this.writeGenericImplementsInterfaces((GenericImplementsInterfacesLayoutBlock)lb);
            break;
         case 50:
            this.writeField((FieldNameLayoutBlock)lb);
            break;
         case 51:
            this.writeMethodStatic((MethodStaticLayoutBlock)lb);
            break;
         case 52:
            this.writeMethod((MethodNameLayoutBlock)lb);
            break;
         case 53:
            this.writeThrows((ThrowsLayoutBlock)lb);
            break;
         case 54:
            this.writeInstruction((InstructionLayoutBlock)lb);
            break;
         case 55:
            this.writeInstructions((InstructionsLayoutBlock)lb);
            break;
         case 56:
            this.writeByteCode((ByteCodeLayoutBlock)lb);
            break;
         case 57:
            this.writeDeclaration((DeclareLayoutBlock)lb);
            break;
         case 61:
            this.writeWhile();
            break;
         case 62:
            this.writeFor();
            break;
         case 63:
            this.writeIf();
            break;
         case 64:
            this.writeSwitch();
            break;
         case 65:
            this.writeCase((CaseLayoutBlock)lb);
            break;
         case 66:
            this.writeCaseEnum((CaseEnumLayoutBlock)lb);
            break;
         case 67:
            this.writeCaseString((CaseLayoutBlock)lb);
            break;
         case 68:
            this.writeCatch((FastCatchLayoutBlock)lb);
            break;
         case 69:
            this.writeSynchronized();
            break;
         case 70:
            this.writeLabel((OffsetLayoutBlock)lb);
            break;
         case 71:
            this.writeElse();
            break;
         case 72:
            this.writeElseSpace();
            break;
         case 73:
            this.writeDo();
            break;
         case 74:
            this.writeInfiniteLoop();
            break;
         case 75:
            this.writeTry();
            break;
         case 76:
            this.writeFinally();
            break;
         case 77:
            this.writeContinue();
            break;
         case 78:
            this.writeBreak();
            break;
         case 79:
            this.writeLabeledBreak((OffsetLayoutBlock)lb);
            break;
         case 80:
            this.writeRightRoundBracket();
            break;
         case 81:
            this.writeRightRoundBracketSemicolon();
            break;
         case 82:
            this.writeSemicolon();
            break;
         case 83:
            this.writeSemicolonSpace();
            break;
         case 84:
            this.writeSpaceColonSpace();
            break;
         case 85:
            this.writeComaSpace();
         }
      }

      this.printer.endOfLine();
      this.printer.end();
   }

   private int searchFirstLineNumber() {
      int i = this.index;
      int length = this.layoutBlockList.size();

      while(i < length) {
         LayoutBlock lb = (LayoutBlock)this.layoutBlockList.get(i++);
         switch(lb.tag) {
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 39:
         case 41:
         case 43:
         case 44:
         case 45:
         case 47:
         case 48:
         case 49:
         case 65:
         case 66:
         case 67:
            if(lb.lineCount > 0) {
               return 0;
            }
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 24:
         case 35:
         case 36:
         case 37:
         case 38:
         case 40:
         case 42:
         case 46:
         case 50:
         case 51:
         case 52:
         case 53:
         case 56:
         case 57:
         case 58:
         case 59:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         default:
            break;
         case 54:
         case 55:
            return lb.firstLineNumber;
         }
      }

      return 0;
   }

   private void writePackage(PackageLayoutBlock plb) {
      this.printer.printKeyword("package");
      this.printer.print(' ');
      String internalPackageName = plb.classFile.getInternalPackageName();
      this.printer.print(internalPackageName.replace('/', '.'));
      this.printer.print(';');
   }

   private void writeSeparatorAtBegining(LayoutBlock slb) {
      int lineCount = slb.lineCount;
      this.printer.debugStartOfSeparatorLayoutBlock();
      if(lineCount > 0) {
         this.endOfLine();
         if(lineCount > 1) {
            this.printer.extraLine(lineCount - 1);
         }

         this.printer.startOfLine(this.searchFirstLineNumber());
      }

      this.printer.debugEndOfSeparatorLayoutBlock(slb.minimalLineCount, slb.lineCount, slb.maximalLineCount);
   }

   private void writeSeparator(LayoutBlock slb) {
      int lineCount = slb.lineCount;
      this.printer.debugStartOfSeparatorLayoutBlock();
      if(lineCount > 0) {
         this.endOfLine();
         if(lineCount > 1) {
            this.printer.startOfLine(0);
            this.endOfLine();
            if(lineCount > 2) {
               this.printer.extraLine(lineCount - 2);
            }
         }

         this.printer.startOfLine(this.searchFirstLineNumber());
      } else {
         this.printer.print(' ');
         this.addSpace = false;
      }

      this.printer.debugEndOfSeparatorLayoutBlock(slb.minimalLineCount, slb.lineCount, slb.maximalLineCount);
   }

   private void writeImports(ImportsLayoutBlock ilb) {
      Collection collection = this.referenceMap.values();
      int length = collection.size();
      if(length > 0) {
         ClassFile classFile = ilb.classFile;
         String internalPackageName = classFile.getInternalPackageName();
         Iterator iterator = collection.iterator();
         ArrayList references = new ArrayList(length);

         while(iterator.hasNext()) {
            Reference delta = (Reference)iterator.next();
            String index = TypeNameUtil.InternalTypeNameToInternalPackageName(delta.getInternalName());
            if(!index.equals(internalPackageName) && !index.equals("java/lang")) {
               references.add(delta);
            }
         }

         if(references.size() > 0) {
            int var11 = ilb.preferedLineCount - ilb.lineCount;
            if(var11 > 0) {
               Collections.sort(references, new ReferenceByCountComparator());
               int var12 = references.size();

               while(var11-- > 0) {
                  --var12;
                  Reference reference = (Reference)references.remove(var12);
                  this.referenceMap.remove(reference.getInternalName());
               }
            }

            if(references.size() > 0) {
               Collections.sort(references, new ReferenceByInternalNameComparator());
               this.printer.debugStartOfLayoutBlock();
               this.printer.startOfImportStatements();
               iterator = references.iterator();
               if(iterator.hasNext()) {
                  this.writeImport((Reference)iterator.next());

                  while(iterator.hasNext()) {
                     this.endOfLine();
                     this.printer.startOfLine(0);
                     this.writeImport((Reference)iterator.next());
                  }
               }

               this.printer.endOfImportStatements();
               this.printer.debugEndOfLayoutBlock();
            }
         }
      }

   }

   private void writeImport(Reference reference) {
      this.printer.printKeyword("import");
      this.printer.print(' ');
      this.printer.printTypeImport(reference.getInternalName(), TypeNameUtil.InternalTypeNameToQualifiedTypeName(reference.getInternalName()));
      this.printer.print(';');
   }

   private void writeTypeMarkerStart(MarkerLayoutBlock mlb) {
      String internalPath = mlb.classFile.getThisClassName() + ".class";
      this.printer.startOfTypeDeclaration(internalPath);
      this.printer.debugMarker("&lt;T&lt;");
   }

   private void writeTypeMarkerEnd(MarkerLayoutBlock mlb) {
      this.printer.debugMarker("&gt;T&gt;");
      this.printer.endOfTypeDeclaration();
   }

   private void writeFieldMarkerStart(MarkerLayoutBlock mlb) {
      String internalPath = mlb.classFile.getThisClassName() + ".class";
      this.printer.startOfTypeDeclaration(internalPath);
      this.printer.debugMarker("&lt;F&lt;");
   }

   private void writeFieldMarkerEnd(MarkerLayoutBlock mlb) {
      this.printer.debugMarker("&gt;F&gt;");
      this.printer.endOfTypeDeclaration();
   }

   private void writeMethodMarkerStart(MarkerLayoutBlock mlb) {
      String internalPath = mlb.classFile.getThisClassName() + ".class";
      this.printer.startOfTypeDeclaration(internalPath);
      this.printer.debugMarker("&lt;M&lt;");
   }

   private void writeMethodMarkerEnd(MarkerLayoutBlock mlb) {
      this.printer.debugMarker("&gt;M&gt;");
      this.printer.endOfTypeDeclaration();
   }

   private void writeCommentDeprecated(LayoutBlock lb) {
      this.printer.debugStartOfCommentDeprecatedLayoutBlock();
      switch(lb.lineCount) {
      case 0:
         this.printer.startOfJavadoc();
         this.printer.print("/** ");
         this.printer.startOfXdoclet();
         this.printer.print("@deprecated");
         this.printer.endOfXdoclet();
         this.printer.print(" */");
         this.printer.endOfJavadoc();
         break;
      case 1:
         this.printer.startOfJavadoc();
         this.printer.print("/** ");
         this.printer.startOfXdoclet();
         this.printer.print("@deprecated");
         this.printer.endOfXdoclet();
         this.printer.print(" */");
         this.printer.endOfJavadoc();
         this.endOfLine();
         this.printer.startOfLine(this.searchFirstLineNumber());
         break;
      case 2:
         this.printer.startOfJavadoc();
         this.printer.print("/**");
         this.endOfLine();
         this.printer.startOfLine(0);
         this.printer.print(" * ");
         this.printer.startOfXdoclet();
         this.printer.print("@deprecated");
         this.printer.endOfXdoclet();
         this.printer.print(" */");
         this.printer.endOfJavadoc();
         this.endOfLine();
         this.printer.startOfLine(this.searchFirstLineNumber());
         break;
      case 3:
         this.printer.startOfJavadoc();
         this.printer.print("/**");
         this.endOfLine();
         this.printer.startOfLine(0);
         this.printer.print(" * ");
         this.printer.startOfXdoclet();
         this.printer.print("@deprecated");
         this.printer.endOfXdoclet();
         this.endOfLine();
         this.printer.startOfLine(0);
         this.printer.print(" */");
         this.printer.endOfJavadoc();
         this.endOfLine();
         this.printer.startOfLine(this.searchFirstLineNumber());
      }

      this.printer.debugEndOfCommentDeprecatedLayoutBlock();
   }

   private void writeCommentError(LayoutBlock lb) {
      this.printer.debugStartOfCommentDeprecatedLayoutBlock();
      switch(lb.lineCount) {
      case 0:
         this.printer.startOfError();
         this.printer.print("/* Error */ ");
         this.printer.endOfError();
         break;
      case 1:
         this.printer.startOfError();
         this.printer.print("/* Error */");
         this.printer.endOfError();
         this.endOfLine();
         this.printer.startOfLine(this.searchFirstLineNumber());
      }

      this.printer.debugEndOfCommentDeprecatedLayoutBlock();
   }

   private void writeAnnotations(AnnotationsLayoutBlock alb) {
      ArrayList annotations = alb.annotations;
      int length = annotations.size();
      if(length > 0) {
         this.printer.debugStartOfLayoutBlock();
         ReferenceMap referenceMap = this.referenceMap;
         ClassFile classFile = alb.classFile;
         int annotationsByLine;
         if(alb.lineCount == 0) {
            for(annotationsByLine = 0; annotationsByLine < length; ++annotationsByLine) {
               AnnotationWriter.WriteAnnotation(this.loader, this.printer, referenceMap, classFile, (Annotation)annotations.get(annotationsByLine));
            }
         } else {
            annotationsByLine = length / alb.lineCount;
            if(annotationsByLine * alb.lineCount < length) {
               ++annotationsByLine;
            }

            int j = annotationsByLine;
            int k = alb.lineCount;

            for(int i = 0; i < length; ++i) {
               AnnotationWriter.WriteAnnotation(this.loader, this.printer, referenceMap, classFile, (Annotation)annotations.get(i));
               --j;
               if(j > 0) {
                  this.printer.print(' ');
               } else {
                  --k;
                  if(k > 0) {
                     this.endOfLine();
                     this.printer.startOfLine(0);
                  }

                  j = annotationsByLine;
               }
            }

            this.endOfLine();
            this.printer.startOfLine(this.searchFirstLineNumber());
         }

         this.printer.debugEndOfLayoutBlock();
      }

   }

   private void writeType(TypeNameLayoutBlock tdlb) {
      this.printer.debugStartOfLayoutBlock();
      ClassFile classFile = tdlb.classFile;
      this.writeAccessAndType(classFile);
      this.printer.printTypeDeclaration(classFile.getThisClassName(), classFile.getClassName());
      if(tdlb.lineCount > 0) {
         this.endOfLine();
         this.printer.startOfLine(this.searchFirstLineNumber());
      }

      this.printer.debugEndOfLayoutBlock();
   }

   private void writeAccessAndType(ClassFile classFile) {
      if((classFile.access_flags & 8192) != 0) {
         classFile.access_flags &= -1025;
      }

      if((classFile.access_flags & 16384) == 0) {
         if(classFile.isAInnerClass()) {
            this.writeAccessNestedClass(classFile.access_flags);
         } else {
            this.writeAccessClass(classFile.access_flags);
         }
      } else if(classFile.isAInnerClass()) {
         this.writeAccessNestedEnum(classFile.access_flags);
      } else {
         this.writeAccessEnum(classFile.access_flags);
      }

      this.writeType(classFile.access_flags);
      this.printer.print(' ');
   }

   private void writeAccessNestedClass(int access_flags) {
      for(int i = 0; i < ACCESS_NESTED_CLASS_NAMES.length; ++i) {
         int acc = 1 << i;
         if((access_flags & acc) != 0 && acc != 32 && acc != 512) {
            this.printer.printKeyword(ACCESS_NESTED_CLASS_NAMES[i]);
            this.printer.print(' ');
         }
      }

      if((access_flags & 1024) != 0) {
         this.printer.printKeyword("abstract");
         this.printer.print(' ');
      }

   }

   private void writeAccessClass(int access_flags) {
      if((access_flags & 1) != 0) {
         this.printer.printKeyword("public");
         this.printer.print(' ');
      }

      if((access_flags & 16) != 0) {
         this.printer.printKeyword("final");
         this.printer.print(' ');
      }

      if((access_flags & 1024) != 0) {
         this.printer.printKeyword("abstract");
         this.printer.print(' ');
      }

   }

   private void writeAccessNestedEnum(int access_flags) {
      for(int i = 0; i < ACCESS_NESTED_ENUM_NAMES.length; ++i) {
         int acc = 1 << i;
         if((access_flags & acc) != 0 && acc != 32 && acc != 512) {
            this.printer.printKeyword(ACCESS_NESTED_ENUM_NAMES[i]);
            this.printer.print(' ');
         }
      }

      if((access_flags & 1024) != 0) {
         this.printer.printKeyword("abstract");
         this.printer.print(' ');
      }

   }

   private void writeAccessEnum(int access_flags) {
      if((access_flags & 1) != 0) {
         this.printer.printKeyword("public");
      }

      this.printer.print(' ');
   }

   private void writeType(int access_flags) {
      if((access_flags & 8192) != 0) {
         this.printer.printKeyword("@interface");
      } else if((access_flags & 16384) != 0) {
         this.printer.printKeyword("enum");
      } else if((access_flags & 512) != 0) {
         this.printer.printKeyword("interface");
      } else {
         this.printer.printKeyword("class");
      }

   }

   private void writeExtendsSuperType(ExtendsSuperTypeLayoutBlock stelb) {
      this.printer.debugStartOfLayoutBlock();
      if(stelb.lineCount > 0) {
         this.endOfLine();
         this.printer.indent();
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.printer.desindent();
      } else {
         this.printer.print(' ');
      }

      ClassFile classFile = stelb.classFile;
      this.printer.printKeyword("extends");
      this.printer.print(' ');
      String signature = SignatureUtil.CreateTypeName(classFile.getSuperClassName());
      SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, classFile, signature);
      this.printer.debugEndOfLayoutBlock();
   }

   private void writeExtendsSuperInterfaces(ExtendsSuperInterfacesLayoutBlock sielb) {
      this.writeInterfaces(sielb, sielb.classFile, true);
   }

   private void writeImplementsInterfaces(ImplementsInterfacesLayoutBlock iilb) {
      this.writeInterfaces(iilb, iilb.classFile, false);
   }

   private void writeInterfaces(LayoutBlock lb, ClassFile classFile, boolean extendsKeyword) {
      this.printer.debugStartOfLayoutBlock();
      if(lb.lineCount > 0) {
         this.endOfLine();
         this.printer.indent();
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.printer.desindent();
      } else {
         this.printer.print(' ');
      }

      int[] interfaceIndexes = classFile.getInterfaces();
      ConstantPool constants = classFile.getConstantPool();
      if(extendsKeyword) {
         this.printer.printKeyword("extends");
      } else {
         this.printer.printKeyword("implements");
      }

      this.printer.print(' ');
      String signature = SignatureUtil.CreateTypeName(constants.getConstantClassName(interfaceIndexes[0]));
      SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, classFile, signature);

      for(int i = 1; i < interfaceIndexes.length; ++i) {
         this.printer.print(", ");
         signature = SignatureUtil.CreateTypeName(constants.getConstantClassName(interfaceIndexes[i]));
         SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, classFile, signature);
      }

      this.printer.debugEndOfLayoutBlock();
   }

   private void writeGenericType(GenericTypeNameLayoutBlock gtdlb) {
      this.writeAccessAndType(gtdlb.classFile);
      SignatureWriter.WriteTypeDeclaration(this.loader, this.printer, this.referenceMap, gtdlb.classFile, gtdlb.signature);
   }

   private void writeGenericExtendsSuperType(GenericExtendsSuperTypeLayoutBlock gstelb) {
      this.printer.debugStartOfLayoutBlock();
      if(gstelb.lineCount > 0) {
         this.endOfLine();
         this.printer.indent();
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.printer.desindent();
      } else {
         this.printer.print(' ');
      }

      this.printer.printKeyword("extends");
      this.printer.print(' ');
      char[] caSignature = gstelb.caSignature;
      SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, gstelb.classFile, caSignature, caSignature.length, gstelb.signatureIndex);
      this.printer.debugEndOfLayoutBlock();
   }

   private void writeGenericExtendsSuperInterfaces(GenericExtendsSuperInterfacesLayoutBlock gsielb) {
      this.writeGenericInterfaces(gsielb, gsielb.classFile, gsielb.caSignature, gsielb.signatureIndex, true);
   }

   private void writeGenericImplementsInterfaces(GenericImplementsInterfacesLayoutBlock giilb) {
      this.writeGenericInterfaces(giilb, giilb.classFile, giilb.caSignature, giilb.signatureIndex, false);
   }

   private void writeGenericInterfaces(LayoutBlock lb, ClassFile classFile, char[] caSignature, int signatureIndex, boolean extendsKeyword) {
      this.printer.debugStartOfLayoutBlock();
      if(lb.lineCount > 0) {
         this.endOfLine();
         this.printer.indent();
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.printer.desindent();
      } else {
         this.printer.print(' ');
      }

      if(extendsKeyword) {
         this.printer.printKeyword("extends");
      } else {
         this.printer.printKeyword("implements");
      }

      this.printer.print(' ');
      int signatureLength = caSignature.length;

      for(signatureIndex = SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, classFile, caSignature, signatureLength, signatureIndex); signatureIndex < signatureLength; signatureIndex = SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, classFile, caSignature, signatureLength, signatureIndex)) {
         this.printer.print(", ");
      }

      this.printer.debugEndOfLayoutBlock();
   }

   private void writeStatementBlockStart(LayoutBlock lb) {
      this.printer.debugStartOfStatementsBlockLayoutBlock();
      switch(lb.lineCount) {
      case 0:
         this.printer.print(" { ");
         this.printer.indent();
         break;
      case 1:
         this.printer.print(" {");
         this.endOfLine();
         this.printer.indent();
         this.printer.startOfLine(this.searchFirstLineNumber());
         break;
      default:
         this.endOfLine();
         this.printer.startOfLine(0);
         this.printer.print('{');
         this.endOfLine();
         this.printer.extraLine(lb.lineCount - 2);
         this.printer.indent();
         this.printer.startOfLine(this.searchFirstLineNumber());
      }

      this.printer.debugEndOfStatementsBlockLayoutBlock(lb.minimalLineCount, lb.lineCount, lb.maximalLineCount);
   }

   private void writeSwitchBlockStart(LayoutBlock lb) {
      this.printer.debugStartOfStatementsBlockLayoutBlock();
      switch(lb.lineCount) {
      case 0:
         this.printer.print(" {");
         break;
      case 1:
         this.printer.print(" {");
         this.endOfLine();
         this.printer.startOfLine(this.searchFirstLineNumber());
         break;
      default:
         this.endOfLine();
         this.printer.startOfLine(0);
         this.printer.print('{');
         this.endOfLine();
         this.printer.extraLine(lb.lineCount - 2);
         this.printer.startOfLine(this.searchFirstLineNumber());
      }

      this.printer.debugEndOfStatementsBlockLayoutBlock(lb.minimalLineCount, lb.lineCount, lb.maximalLineCount);
   }

   private void writeStatementsBlockEnd(LayoutBlock lb) {
      this.printer.debugStartOfStatementsBlockLayoutBlock();
      switch(lb.lineCount) {
      case 0:
         this.printer.print(" }");
         this.addSpace = true;
         this.printer.desindent();
         break;
      case 1:
         this.endOfLine();
         this.printer.desindent();
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.printer.print('}');
         this.addSpace = true;
         break;
      default:
         this.endOfLine();
         this.printer.desindent();
         this.printer.extraLine(lb.lineCount - 2);
         this.printer.startOfLine(0);
         this.printer.print('}');
         this.endOfLine();
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.addSpace = false;
      }

      this.printer.debugEndOfStatementsBlockLayoutBlock(lb.minimalLineCount, lb.lineCount, lb.maximalLineCount);
   }

   private void writeStatementsInnerBodyBlockEnd(LayoutBlock lb) {
      this.printer.debugStartOfStatementsBlockLayoutBlock();
      switch(lb.lineCount) {
      case 0:
         this.printer.print(" }");
         this.printer.desindent();
         break;
      case 1:
         this.endOfLine();
         this.printer.desindent();
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.printer.print('}');
         break;
      default:
         this.endOfLine();
         this.printer.desindent();
         this.printer.extraLine(lb.lineCount - 1);
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.printer.print('}');
      }

      this.printer.debugEndOfStatementsBlockLayoutBlock(lb.minimalLineCount, lb.lineCount, lb.maximalLineCount);
      this.addSpace = false;
   }

   private void writeSwitchBlockEnd(LayoutBlock lb) {
      this.printer.debugStartOfStatementsBlockLayoutBlock();
      switch(lb.lineCount) {
      case 0:
         this.printer.print('}');
         this.addSpace = true;
         break;
      case 1:
         this.printer.print('}');
         this.endOfLine();
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.addSpace = false;
         break;
      default:
         this.endOfLine();
         this.printer.desindent();
         this.printer.extraLine(lb.lineCount - 1);
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.printer.print('}');
         this.addSpace = false;
      }

      this.printer.debugEndOfStatementsBlockLayoutBlock(lb.minimalLineCount, lb.lineCount, lb.maximalLineCount);
   }

   private void writeCaseBlockStart(LayoutBlock lb) {
      this.printer.indent();
      this.printer.debugStartOfCaseBlockLayoutBlock();
      int lineCount = lb.lineCount;
      if(lineCount > 0) {
         this.endOfLine();
         if(lineCount > 1) {
            this.printer.startOfLine(0);
            this.endOfLine();
            if(lineCount > 2) {
               this.printer.extraLine(lineCount - 2);
            }
         }

         this.printer.startOfLine(this.searchFirstLineNumber());
      } else {
         this.printer.print(' ');
      }

      this.printer.debugEndOfCaseBlockLayoutBlock();
   }

   private void writeCaseBlockEnd(LayoutBlock lb) {
      this.printer.desindent();
      this.printer.debugStartOfCaseBlockLayoutBlock();
      int lineCount = lb.lineCount;
      if(lineCount > 0) {
         this.endOfLine();
         if(lineCount > 1) {
            this.printer.startOfLine(0);
            this.endOfLine();
            if(lineCount > 2) {
               this.printer.extraLine(lineCount - 2);
            }
         }

         this.printer.startOfLine(this.searchFirstLineNumber());
      } else {
         this.printer.print(' ');
      }

      this.printer.debugEndOfCaseBlockLayoutBlock();
   }

   private void writeForBlockStart(LayoutBlock lb) {
      this.printer.indent();
      this.printer.indent();
      this.printer.debugStartOfSeparatorLayoutBlock();
      int lineCount = lb.lineCount;
      if(lineCount > 0) {
         this.endOfLine();
         if(lineCount > 1) {
            this.printer.startOfLine(0);
            this.endOfLine();
            if(lineCount > 2) {
               this.printer.extraLine(lineCount - 2);
            }
         }

         this.printer.startOfLine(this.searchFirstLineNumber());
      }

      this.printer.debugEndOfSeparatorLayoutBlock(lb.minimalLineCount, lb.lineCount, lb.maximalLineCount);
   }

   private void writeForBlockEnd(LayoutBlock lb) {
      this.printer.desindent();
      this.printer.desindent();
   }

   private void writeStatementsBlockStartEnd(LayoutBlock lb) {
      this.printer.debugStartOfStatementsBlockLayoutBlock();
      switch(lb.lineCount) {
      case 0:
         this.printer.print(" {}");
         break;
      case 1:
         this.endOfLine();
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.printer.print("{}");
         break;
      default:
         this.endOfLine();
         this.printer.startOfLine(0);
         this.printer.print("{}");
         this.endOfLine();
         this.printer.extraLine(lb.lineCount - 1);
         this.printer.startOfLine(this.searchFirstLineNumber());
      }

      this.printer.debugEndOfStatementsBlockLayoutBlock(lb.minimalLineCount, lb.lineCount, lb.maximalLineCount);
   }

   private void writeSingleStatementsBlockStart(LayoutBlock lb) {
      this.printer.debugStartOfStatementsBlockLayoutBlock();
      switch(lb.lineCount) {
      case 0:
         if(((BlockLayoutBlock)lb).other.lineCount > 0) {
            this.printer.print(" {");
         }

         this.printer.print(' ');
         this.printer.indent();
         break;
      case 1:
         if(((BlockLayoutBlock)lb).other.lineCount > 0) {
            this.printer.print(" {");
         }

         this.endOfLine();
         this.printer.indent();
         this.printer.startOfLine(this.searchFirstLineNumber());
         break;
      default:
         this.endOfLine();
         this.printer.startOfLine(0);
         this.printer.print('{');
         this.endOfLine();
         this.printer.extraLine(lb.lineCount - 2);
         this.printer.indent();
         this.printer.startOfLine(this.searchFirstLineNumber());
      }

      this.printer.debugEndOfStatementsBlockLayoutBlock(lb.minimalLineCount, lb.lineCount, lb.maximalLineCount);
   }

   private void writeSingleStatementsBlockEnd(LayoutBlock lb) {
      this.printer.debugStartOfStatementsBlockLayoutBlock();
      switch(lb.lineCount) {
      case 0:
         if(((BlockLayoutBlock)lb).other.lineCount > 1) {
            this.printer.print(" }");
         }

         this.addSpace = true;
         this.printer.desindent();
         break;
      case 1:
         this.endOfLine();
         this.printer.desindent();
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.printer.print('}');
         this.addSpace = true;
         break;
      default:
         this.endOfLine();
         this.printer.desindent();
         this.printer.extraLine(lb.lineCount - 2);
         this.printer.startOfLine(0);
         this.printer.print('}');
         this.endOfLine();
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.addSpace = false;
      }

      this.printer.debugEndOfStatementsBlockLayoutBlock(lb.minimalLineCount, lb.lineCount, lb.maximalLineCount);
   }

   private void writeSingleStatementsBlockStartEnd(LayoutBlock lb) {
      this.printer.debugStartOfStatementsBlockLayoutBlock();
      switch(lb.lineCount) {
      case 0:
         this.printer.print(" ;");
         break;
      default:
         this.printer.print(" ;");
         this.endOfLine();
         this.printer.extraLine(lb.lineCount - 1);
         this.printer.indent();
         this.printer.startOfLine(this.searchFirstLineNumber());
      }

      this.printer.debugEndOfStatementsBlockLayoutBlock(lb.minimalLineCount, lb.lineCount, lb.maximalLineCount);
   }

   private void writeField(FieldNameLayoutBlock flb) {
      ClassFile classFile = flb.classFile;
      Field field = flb.field;
      this.writeAccessField(field.access_flags);
      ConstantPool constants = classFile.getConstantPool();
      AttributeSignature as = field.getAttributeSignature();
      int signatureIndex = as == null?field.descriptor_index:as.signature_index;
      String signature = constants.getConstantUtf8(signatureIndex);
      SignatureWriter.WriteSignature(this.loader, this.printer, this.referenceMap, classFile, signature);
      this.printer.print(' ');
      String fieldName = constants.getConstantUtf8(field.name_index);
      if(keywords.contains(fieldName)) {
         fieldName = "jdField_" + fieldName;
      }

      String internalClassName = classFile.getThisClassName();
      String descriptor = constants.getConstantUtf8(field.descriptor_index);
      if((field.access_flags & 8) != 0) {
         this.printer.printStaticFieldDeclaration(internalClassName, fieldName, descriptor);
      } else {
         this.printer.printFieldDeclaration(internalClassName, fieldName, descriptor);
      }

      if(field.getValueAndMethod() != null) {
         this.printer.print(" = ");
      } else {
         ConstantValue cv = field.getConstantValue(constants);
         if(cv != null) {
            this.printer.print(" = ");
            ConstantValueWriter.Write(this.loader, this.printer, this.referenceMap, classFile, cv, (byte)signature.charAt(0));
            this.printer.print(';');
         } else {
            this.printer.print(';');
         }
      }

   }

   private void writeAccessField(int access_flags) {
      for(int i = 0; i < ACCESS_FIELD_NAMES.length; ++i) {
         int acc = 1 << i;
         if((access_flags & acc) != 0 && acc != 32 && acc != 512 && ACCESS_FIELD_NAMES[i] != null) {
            this.printer.printKeyword(ACCESS_FIELD_NAMES[i]);
            this.printer.print(' ');
         }
      }

   }

   private void writeMethodStatic(MethodStaticLayoutBlock mslb) {
      this.printer.printStaticConstructorDeclaration(mslb.classFile.getThisClassName(), "static");
   }

   private void writeMethod(MethodNameLayoutBlock mlb) {
      Method method = mlb.method;
      if((mlb.classFile.access_flags & 8192) == 0) {
         this.writeAccessMethod(method.access_flags);
         SignatureWriter.WriteMethodDeclaration(keywords, this.loader, this.printer, this.referenceMap, mlb.classFile, method, mlb.signature, mlb.descriptorFlag);
         if(mlb.nullCodeFlag) {
            this.printer.print(';');
         }
      } else {
         this.writeAccessMethod(method.access_flags & -1026);
         SignatureWriter.WriteMethodDeclaration(keywords, this.loader, this.printer, this.referenceMap, mlb.classFile, method, mlb.signature, mlb.descriptorFlag);
         ElementValue defaultAnnotationValue = method.getDefaultAnnotationValue();
         if(defaultAnnotationValue != null) {
            this.printer.print(' ');
            this.printer.printKeyword("default");
            this.printer.print(' ');
            ElementValueWriter.WriteElementValue(this.loader, this.printer, this.referenceMap, mlb.classFile, defaultAnnotationValue);
         }

         this.printer.print(';');
      }

   }

   private void writeAccessMethod(int access_flags) {
      for(int i = 0; i < ACCESS_METHOD_NAMES.length; ++i) {
         int acc = 1 << i;
         if((access_flags & acc) != 0 && ACCESS_METHOD_NAMES[i] != null) {
            this.printer.printKeyword(ACCESS_METHOD_NAMES[i]);
            this.printer.print(' ');
         }
      }

   }

   private void writeThrows(ThrowsLayoutBlock tlb) {
      this.printer.debugStartOfLayoutBlock();
      if(tlb.lineCount > 0) {
         this.endOfLine();
         this.printer.indent();
         this.printer.startOfLine(this.searchFirstLineNumber());
         this.printer.desindent();
      } else {
         this.printer.print(' ');
      }

      this.printer.printKeyword("throws");
      this.printer.print(' ');
      ClassFile classFile = tlb.classFile;
      ConstantPool constants = classFile.getConstantPool();
      int[] exceptionIndexes = tlb.method.getExceptionIndexes();
      int exceptionIndexesLength = exceptionIndexes.length;
      if(exceptionIndexesLength > 0) {
         String firstInternalClassName = constants.getConstantClassName(exceptionIndexes[0]);
         this.printer.print(SignatureWriter.InternalClassNameToShortClassName(this.referenceMap, classFile, firstInternalClassName));

         for(int j = 1; j < exceptionIndexesLength; ++j) {
            this.printer.print(", ");
            String nextInternalClassName = constants.getConstantClassName(exceptionIndexes[j]);
            this.printer.print(SignatureWriter.InternalClassNameToShortClassName(this.referenceMap, classFile, nextInternalClassName));
         }
      }

      if(tlb.nullCodeFlag) {
         this.printer.print(';');
      }

      this.printer.debugEndOfLayoutBlock();
   }

   private void writeInstruction(InstructionLayoutBlock ilb) {
      this.printer.debugStartOfInstructionBlockLayoutBlock();
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.instructionPrinter.init(ilb.firstLineNumber);
      this.visitor.init(ilb.classFile, ilb.method, ilb.firstOffset, ilb.lastOffset);
      this.instructionPrinter.startOfInstruction();
      this.visitor.visit(ilb.instruction);
      this.instructionPrinter.endOfInstruction();
      this.instructionPrinter.release();
      this.printer.debugEndOfInstructionBlockLayoutBlock();
   }

   private void writeInstructions(InstructionsLayoutBlock ilb) {
      this.printer.debugStartOfInstructionBlockLayoutBlock();
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.instructionPrinter.init(ilb.firstLineNumber);
      this.visitor.init(ilb.classFile, ilb.method, ilb.firstOffset, ilb.lastOffset);
      int index = ilb.firstIndex;
      int lastIndex = ilb.lastIndex;

      for(List instructions = ilb.instructions; index <= lastIndex; ++index) {
         Instruction instruction = (Instruction)instructions.get(index);
         if(index > ilb.firstIndex || ilb.firstOffset == 0) {
            this.instructionPrinter.startOfInstruction();
         }

         this.visitor.visit(instruction);
         if(index < lastIndex || ilb.lastOffset == instruction.offset) {
            this.instructionPrinter.endOfInstruction();
            this.printer.print(';');
         }
      }

      this.instructionPrinter.release();
      this.printer.debugEndOfInstructionBlockLayoutBlock();
   }

   private void writeByteCode(ByteCodeLayoutBlock bclb) {
      ByteCodeWriter.Write(this.loader, this.printer, this.referenceMap, bclb.classFile, bclb.method);
   }

   private void writeDeclaration(DeclareLayoutBlock dlb) {
      this.printer.debugStartOfInstructionBlockLayoutBlock();
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.instructionPrinter.init(dlb.firstLineNumber);
      this.visitor.init(dlb.classFile, dlb.method, 0, dlb.instruction.offset);
      this.instructionPrinter.startOfInstruction();
      this.visitor.visit(dlb.instruction);
      this.instructionPrinter.endOfInstruction();
      this.printer.print(';');
      this.instructionPrinter.release();
      this.printer.debugEndOfInstructionBlockLayoutBlock();
   }

   private void writeIf() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("if");
      this.printer.print(" (");
   }

   private void writeWhile() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("while");
      this.printer.print(" (");
   }

   private void writeFor() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("for");
      this.printer.print(" (");
   }

   private void writeLabeledBreak(OffsetLayoutBlock olb) {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("break");
      this.printer.print(' ');
      this.printer.print("label");
      this.printer.print(olb.offset);
      this.printer.print(';');
   }

   private void writeRightRoundBracket() {
      this.printer.print(')');
   }

   private void writeRightRoundBracketSemicolon() {
      this.printer.print(");");
   }

   private void writeSemicolon() {
      this.printer.print(';');
   }

   private void writeSemicolonSpace() {
      this.printer.print("; ");
   }

   private void writeSpaceColonSpace() {
      this.printer.print(" : ");
   }

   private void writeComaSpace() {
      this.printer.print(", ");
   }

   private void writeSwitch() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("switch");
      this.printer.print(" (");
   }

   private void writeCase(CaseLayoutBlock clb) {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      String signature = clb.fs.test.getReturnedSignature(clb.classFile.getConstantPool(), clb.method.getLocalVariables());
      char type = signature == null?88:signature.charAt(0);
      FastSwitch.Pair[] pairs = clb.fs.pairs;
      int lineCount = clb.lineCount + 1;
      int lastIndex = clb.lastIndex;
      int caseCount = lastIndex - clb.firstIndex + 1;
      int caseByLine = caseCount / lineCount;
      int middleLineCount = caseCount - caseByLine * lineCount;
      int middleIndex = clb.firstIndex + middleLineCount * (caseByLine + 1);
      int j = caseByLine + 1;

      int i;
      FastSwitch.Pair pair;
      String escapedString;
      for(i = clb.firstIndex; i < middleIndex; ++i) {
         pair = pairs[i];
         if(pair.isDefault()) {
            this.printer.printKeyword("default");
            this.printer.print(": ");
         } else {
            this.printer.printKeyword("case");
            this.printer.print(' ');
            this.printer.debugStartOfInstructionBlockLayoutBlock();
            if(type == 67) {
               escapedString = StringUtil.EscapeCharAndAppendApostrophe((char)pair.getKey());
               this.printer.printString(escapedString, clb.classFile.getThisClassName());
            } else {
               this.printer.printNumeric(String.valueOf(pair.getKey()));
            }

            this.printer.debugEndOfInstructionBlockLayoutBlock();
            this.printer.print(": ");
         }

         if(lineCount > 0) {
            if(j == 1 && i < lastIndex) {
               this.endOfLine();
               this.printer.startOfLine(0);
               j = caseByLine + 1;
            } else {
               --j;
            }
         }
      }

      j = caseByLine;

      for(i = middleIndex; i <= lastIndex; ++i) {
         pair = pairs[i];
         if(pair.isDefault()) {
            this.printer.printKeyword("default");
            this.printer.print(": ");
         } else {
            this.printer.printKeyword("case");
            this.printer.print(' ');
            this.printer.debugStartOfInstructionBlockLayoutBlock();
            if(type == 67) {
               escapedString = StringUtil.EscapeCharAndAppendApostrophe((char)pair.getKey());
               this.printer.printString(escapedString, clb.classFile.getThisClassName());
            } else {
               this.printer.printNumeric(String.valueOf(pair.getKey()));
            }

            this.printer.debugEndOfInstructionBlockLayoutBlock();
            this.printer.print(": ");
         }

         if(lineCount > 0) {
            if(j == 1 && i < lastIndex) {
               this.endOfLine();
               this.printer.startOfLine(0);
               j = caseByLine;
            } else {
               --j;
            }
         }
      }

   }

   private void writeCaseEnum(CaseEnumLayoutBlock celb) {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      ClassFile classFile = celb.classFile;
      ConstantPool constants = classFile.getConstantPool();
      List switchMap = (List)classFile.getSwitchMaps().get(Integer.valueOf(celb.switchMapKeyIndex));
      ArrayLoadInstruction ali = (ArrayLoadInstruction)celb.fs.test;
      Invokevirtual iv = (Invokevirtual)ali.indexref;
      ConstantMethodref cmr = constants.getConstantMethodref(iv.index);
      String internalEnumName = constants.getConstantClassName(cmr.class_index);
      String enumDescriptor = SignatureUtil.CreateTypeName(internalEnumName);
      FastSwitch.Pair[] pairs = celb.fs.pairs;
      int lineCount = celb.lineCount + 1;
      int lastIndex = celb.lastIndex;
      int caseCount = lastIndex - celb.firstIndex + 1;
      int caseByLine = caseCount / lineCount;
      int middleLineCount = caseCount - caseByLine * lineCount;
      int middleIndex = celb.firstIndex + middleLineCount * (caseByLine + 1);
      int j = caseByLine + 1;

      int i;
      FastSwitch.Pair pair;
      int key;
      String value;
      for(i = celb.firstIndex; i < middleIndex; ++i) {
         pair = pairs[i];
         if(pair.isDefault()) {
            this.printer.printKeyword("default");
            this.printer.print(": ");
         } else {
            this.printer.printKeyword("case");
            this.printer.print(' ');
            this.printer.debugStartOfInstructionBlockLayoutBlock();
            key = pair.getKey();
            if(key > 0 && key <= switchMap.size()) {
               value = constants.getConstantUtf8(((Integer)switchMap.get(key - 1)).intValue());
               this.printer.printStaticField(internalEnumName, value, enumDescriptor, classFile.getThisClassName());
            } else {
               this.printer.startOfError();
               this.printer.print("???");
               this.printer.endOfError();
            }

            this.printer.debugEndOfInstructionBlockLayoutBlock();
            this.printer.print(": ");
         }

         if(lineCount > 0) {
            if(j == 1 && i < lastIndex) {
               this.endOfLine();
               this.printer.startOfLine(0);
               j = caseByLine + 1;
            } else {
               --j;
            }
         }
      }

      j = caseByLine;

      for(i = middleIndex; i <= lastIndex; ++i) {
         pair = pairs[i];
         if(pair.isDefault()) {
            this.printer.printKeyword("default");
            this.printer.print(": ");
         } else {
            this.printer.printKeyword("case");
            this.printer.print(' ');
            this.printer.debugStartOfInstructionBlockLayoutBlock();
            key = pair.getKey();
            if(key > 0 && key <= switchMap.size()) {
               value = constants.getConstantUtf8(((Integer)switchMap.get(key - 1)).intValue());
               this.printer.printStaticField(internalEnumName, value, enumDescriptor, classFile.getThisClassName());
            } else {
               this.printer.startOfError();
               this.printer.print("???");
               this.printer.endOfError();
            }

            this.printer.debugEndOfInstructionBlockLayoutBlock();
            this.printer.print(": ");
         }

         if(lineCount > 0) {
            if(j == 1 && i < lastIndex) {
               this.endOfLine();
               this.printer.startOfLine(0);
               j = caseByLine;
            } else {
               --j;
            }
         }
      }

   }

   private void writeCaseString(CaseLayoutBlock clb) {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      ClassFile classFile = clb.classFile;
      ConstantPool constants = classFile.getConstantPool();
      FastSwitch.Pair[] pairs = clb.fs.pairs;
      int lineCount = clb.lineCount + 1;
      int lastIndex = clb.lastIndex;
      int caseCount = lastIndex - clb.firstIndex + 1;
      int caseByLine = caseCount / lineCount;
      int middleLineCount = caseCount - caseByLine * lineCount;
      int middleIndex = clb.firstIndex + middleLineCount * (caseByLine + 1);
      int j = caseByLine + 1;

      int i;
      FastSwitch.Pair pair;
      ConstantValue cv;
      for(i = clb.firstIndex; i < middleIndex; ++i) {
         pair = pairs[i];
         if(pair.isDefault()) {
            this.printer.printKeyword("default");
            this.printer.print(": ");
         } else {
            this.printer.printKeyword("case");
            this.printer.print(' ');
            this.printer.debugStartOfInstructionBlockLayoutBlock();
            cv = constants.getConstantValue(pair.getKey());
            ConstantValueWriter.Write(this.loader, this.printer, this.referenceMap, classFile, cv);
            this.printer.debugEndOfInstructionBlockLayoutBlock();
            this.printer.print(": ");
         }

         if(lineCount > 0) {
            if(j == 1 && i < lastIndex) {
               this.endOfLine();
               this.printer.startOfLine(0);
               j = caseByLine + 1;
            } else {
               --j;
            }
         }
      }

      j = caseByLine;

      for(i = middleIndex; i <= lastIndex; ++i) {
         pair = pairs[i];
         if(pair.isDefault()) {
            this.printer.printKeyword("default");
            this.printer.print(": ");
         } else {
            this.printer.printKeyword("case");
            this.printer.print(' ');
            this.printer.debugStartOfInstructionBlockLayoutBlock();
            cv = constants.getConstantValue(pair.getKey());
            ConstantValueWriter.Write(this.loader, this.printer, this.referenceMap, classFile, cv);
            this.printer.debugEndOfInstructionBlockLayoutBlock();
            this.printer.print(": ");
         }

         if(lineCount > 0) {
            if(j == 1 && i < lastIndex) {
               this.endOfLine();
               this.printer.startOfLine(0);
               j = caseByLine;
            } else {
               --j;
            }
         }
      }

   }

   private void writeCatch(FastCatchLayoutBlock fslb) {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("catch");
      this.printer.print(" (");
      ClassFile classFile = fslb.classFile;
      ConstantPool constants = classFile.getConstantPool();
      Method method = fslb.method;
      FastTry.FastCatch fc = fslb.fc;
      this.writeCatchType(classFile, constants, fc.exceptionTypeIndex);
      if(fc.otherExceptionTypeIndexes != null) {
         int[] lv = fc.otherExceptionTypeIndexes;
         int otherExceptionTypeIndexesLength = lv.length;

         for(int i = 0; i < otherExceptionTypeIndexesLength; ++i) {
            if(lv[i] != 0) {
               this.printer.print('|');
               this.writeCatchType(classFile, constants, lv[i]);
            }
         }
      }

      this.printer.print(' ');
      LocalVariable var9 = method.getLocalVariables().searchLocalVariableWithIndexAndOffset(fc.localVarIndex, fc.exceptionOffset);
      if(var9 == null) {
         this.printer.startOfError();
         this.printer.print("???");
         this.printer.endOfError();
      } else {
         this.printer.print(constants.getConstantUtf8(var9.name_index));
      }

      this.printer.print(')');
   }

   private void writeCatchType(ClassFile classFile, ConstantPool constants, int exceptionTypeIndex) {
      String internalClassName = constants.getConstantClassName(exceptionTypeIndex);
      String className = SignatureWriter.InternalClassNameToClassName(this.loader, this.referenceMap, classFile, internalClassName);
      this.printer.printType(internalClassName, className, classFile.getThisClassName());
   }

   private void writeSynchronized() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("synchronized");
      this.printer.print(" (");
   }

   private void writeLabel(OffsetLayoutBlock olb) {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.print("label");
      this.printer.print(olb.offset);
      this.printer.print(':');
   }

   private void writeElse() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("else");
   }

   private void writeElseSpace() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("else");
      this.printer.print(' ');
   }

   private void writeDo() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("do");
   }

   private void writeInfiniteLoop() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("for");
      this.printer.print(" (;;)");
   }

   private void writeTry() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("try");
   }

   private void writeFinally() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("finally");
   }

   private void writeContinue() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("continue");
      this.printer.print(';');
   }

   private void writeBreak() {
      if(this.addSpace) {
         this.printer.print(" ");
         this.addSpace = false;
      }

      this.printer.printKeyword("break");
      this.printer.print(';');
   }

   private void endOfLine() {
      this.printer.endOfLine();
      this.addSpace = false;
   }
}
