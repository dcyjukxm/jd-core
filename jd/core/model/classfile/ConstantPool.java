package jd.core.model.classfile;

import java.util.ArrayList;
import java.util.List;
import jd.core.model.classfile.constant.Constant;
import jd.core.model.classfile.constant.ConstantClass;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantInteger;
import jd.core.model.classfile.constant.ConstantInterfaceMethodref;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.classfile.constant.ConstantUtf8;
import jd.core.model.classfile.constant.ConstantValue;
import jd.core.util.IndexToIndexMap;
import jd.core.util.InvalidParameterException;
import jd.core.util.StringToIndexMap;

public class ConstantPool {
   private ArrayList<Constant> listOfConstants = new ArrayList();
   private StringToIndexMap constantUtf8ToIndex = new StringToIndexMap();
   private IndexToIndexMap constantClassToIndex = new IndexToIndexMap();
   public final int instanceConstructorIndex;
   public final int classConstructorIndex;
   public final int internalDeprecatedSignatureIndex;
   public final int toStringIndex;
   public final int valueOfIndex;
   public final int appendIndex;
   public final int objectClassIndex;
   public final int objectClassNameIndex;
   public final int stringClassNameIndex;
   public final int stringBufferClassNameIndex;
   public final int stringBuilderClassNameIndex;
   public final int objectSignatureIndex;
   public final int thisLocalVariableNameIndex;
   public final int annotationDefaultAttributeNameIndex;
   public final int codeAttributeNameIndex;
   public final int constantValueAttributeNameIndex;
   public final int deprecatedAttributeNameIndex;
   public final int enclosingMethodAttributeNameIndex;
   public final int exceptionsAttributeNameIndex;
   public final int innerClassesAttributeNameIndex;
   public final int lineNumberTableAttributeNameIndex;
   public final int localVariableTableAttributeNameIndex;
   public final int localVariableTypeTableAttributeNameIndex;
   public final int runtimeInvisibleAnnotationsAttributeNameIndex;
   public final int runtimeVisibleAnnotationsAttributeNameIndex;
   public final int runtimeInvisibleParameterAnnotationsAttributeNameIndex;
   public final int runtimeVisibleParameterAnnotationsAttributeNameIndex;
   public final int signatureAttributeNameIndex;
   public final int sourceFileAttributeNameIndex;
   public final int syntheticAttributeNameIndex;

   public ConstantPool(Constant[] constants) {
      for(int i = 0; i < constants.length; ++i) {
         Constant constant = constants[i];
         int index = this.listOfConstants.size();
         this.listOfConstants.add(constant);
         if(constant != null) {
            switch(constant.tag) {
            case 1:
               this.constantUtf8ToIndex.put(((ConstantUtf8)constant).bytes, index);
               break;
            case 7:
               this.constantClassToIndex.put(((ConstantClass)constant).name_index, index);
            }
         }
      }

      this.instanceConstructorIndex = this.addConstantUtf8("<init>");
      this.classConstructorIndex = this.addConstantUtf8("<clinit>");
      this.internalDeprecatedSignatureIndex = this.addConstantUtf8("Ljava/lang/Deprecated;");
      this.toStringIndex = this.addConstantUtf8("toString");
      this.valueOfIndex = this.addConstantUtf8("valueOf");
      this.appendIndex = this.addConstantUtf8("append");
      this.objectClassNameIndex = this.addConstantUtf8("java/lang/Object");
      this.objectClassIndex = this.addConstantClass(this.objectClassNameIndex);
      this.objectSignatureIndex = this.addConstantUtf8("Ljava/lang/Object;");
      this.stringClassNameIndex = this.addConstantUtf8("java/lang/String");
      this.stringBufferClassNameIndex = this.addConstantUtf8("java/lang/StringBuffer");
      this.stringBuilderClassNameIndex = this.addConstantUtf8("java/lang/StringBuilder");
      this.thisLocalVariableNameIndex = this.addConstantUtf8("this");
      this.annotationDefaultAttributeNameIndex = this.addConstantUtf8("AnnotationDefault");
      this.codeAttributeNameIndex = this.addConstantUtf8("Code");
      this.constantValueAttributeNameIndex = this.addConstantUtf8("ConstantValue");
      this.deprecatedAttributeNameIndex = this.addConstantUtf8("Deprecated");
      this.enclosingMethodAttributeNameIndex = this.addConstantUtf8("EnclosingMethod");
      this.exceptionsAttributeNameIndex = this.addConstantUtf8("Exceptions");
      this.innerClassesAttributeNameIndex = this.addConstantUtf8("InnerClasses");
      this.lineNumberTableAttributeNameIndex = this.addConstantUtf8("LineNumberTable");
      this.localVariableTableAttributeNameIndex = this.addConstantUtf8("LocalVariableTable");
      this.localVariableTypeTableAttributeNameIndex = this.addConstantUtf8("LocalVariableTypeTable");
      this.runtimeInvisibleAnnotationsAttributeNameIndex = this.addConstantUtf8("RuntimeInvisibleAnnotations");
      this.runtimeVisibleAnnotationsAttributeNameIndex = this.addConstantUtf8("RuntimeVisibleAnnotations");
      this.runtimeInvisibleParameterAnnotationsAttributeNameIndex = this.addConstantUtf8("RuntimeInvisibleParameterAnnotations");
      this.runtimeVisibleParameterAnnotationsAttributeNameIndex = this.addConstantUtf8("RuntimeVisibleParameterAnnotations");
      this.signatureAttributeNameIndex = this.addConstantUtf8("Signature");
      this.sourceFileAttributeNameIndex = this.addConstantUtf8("SourceFile");
      this.syntheticAttributeNameIndex = this.addConstantUtf8("Synthetic");
   }

   public Constant get(int i) {
      return (Constant)this.listOfConstants.get(i);
   }

   public int size() {
      return this.listOfConstants.size();
   }

   public int addConstantUtf8(String s) {
      if(s == null) {
         throw new InvalidParameterException("Constant string is null");
      } else {
         assert !s.startsWith("L[");

         int index = this.constantUtf8ToIndex.get(s);
         if(index == -1) {
            ConstantUtf8 cutf8 = new ConstantUtf8(1, s);
            index = this.listOfConstants.size();
            this.listOfConstants.add(cutf8);
            this.constantUtf8ToIndex.put(s, index);
         }

         return index;
      }
   }

   public int addConstantClass(int name_index) {
      String internalName = this.getConstantUtf8(name_index);
      if(internalName == null || internalName.length() == 0 || internalName.charAt(internalName.length() - 1) == 59) {
         System.err.println("ConstantPool.addConstantClass: invalid name index");
      }

      int index = this.constantClassToIndex.get(name_index);
      if(index == -1) {
         ConstantClass cc = new ConstantClass(7, name_index);
         index = this.listOfConstants.size();
         this.listOfConstants.add(cc);
         this.constantClassToIndex.put(name_index, index);
      }

      return index;
   }

   public int addConstantNameAndType(int name_index, int descriptor_index) {
      int index = this.listOfConstants.size();

      while(true) {
         --index;
         if(index <= 0) {
            ConstantNameAndType var6 = new ConstantNameAndType(12, name_index, descriptor_index);
            index = this.listOfConstants.size();
            this.listOfConstants.add(var6);
            return index;
         }

         Constant cnat = (Constant)this.listOfConstants.get(index);
         if(cnat != null && cnat.tag == 12) {
            ConstantNameAndType cnat1 = (ConstantNameAndType)cnat;
            if(cnat1.name_index == name_index && cnat1.descriptor_index == descriptor_index) {
               return index;
            }
         }
      }
   }

   public int addConstantFieldref(int class_index, int name_and_type_index) {
      int index = this.listOfConstants.size();

      while(true) {
         --index;
         if(index <= 0) {
            ConstantFieldref var6 = new ConstantFieldref(9, class_index, name_and_type_index);
            index = this.listOfConstants.size();
            this.listOfConstants.add(var6);
            return index;
         }

         Constant cfr = (Constant)this.listOfConstants.get(index);
         if(cfr != null && cfr.tag == 9) {
            ConstantFieldref cfr1 = (ConstantFieldref)cfr;
            if(cfr1.class_index == class_index && cfr1.name_and_type_index == name_and_type_index) {
               return index;
            }
         }
      }
   }

   public int addConstantMethodref(int class_index, int name_and_type_index) {
      return this.addConstantMethodref(class_index, name_and_type_index, (List)null, (String)null);
   }

   public int addConstantMethodref(int class_index, int name_and_type_index, List<String> listOfParameterSignatures, String returnedSignature) {
      int index = this.listOfConstants.size();

      while(true) {
         --index;
         if(index <= 0) {
            ConstantMethodref var8 = new ConstantMethodref(10, class_index, name_and_type_index, listOfParameterSignatures, returnedSignature);
            index = this.listOfConstants.size();
            this.listOfConstants.add(var8);
            return index;
         }

         Constant cfr = (Constant)this.listOfConstants.get(index);
         if(cfr != null && cfr.tag == 10) {
            ConstantMethodref cmr = (ConstantMethodref)cfr;
            if(cmr.class_index == class_index && cmr.name_and_type_index == name_and_type_index) {
               return index;
            }
         }
      }
   }

   public String getConstantUtf8(int index) {
      ConstantUtf8 cutf8 = (ConstantUtf8)this.listOfConstants.get(index);
      return cutf8.bytes;
   }

   public String getConstantClassName(int index) {
      ConstantClass cc = (ConstantClass)this.listOfConstants.get(index);
      ConstantUtf8 cutf8 = (ConstantUtf8)this.listOfConstants.get(cc.name_index);
      return cutf8.bytes;
   }

   public ConstantClass getConstantClass(int index) {
      return (ConstantClass)this.listOfConstants.get(index);
   }

   public ConstantFieldref getConstantFieldref(int index) {
      return (ConstantFieldref)this.listOfConstants.get(index);
   }

   public ConstantNameAndType getConstantNameAndType(int index) {
      return (ConstantNameAndType)this.listOfConstants.get(index);
   }

   public ConstantMethodref getConstantMethodref(int index) {
      return (ConstantMethodref)this.listOfConstants.get(index);
   }

   public ConstantInterfaceMethodref getConstantInterfaceMethodref(int index) {
      return (ConstantInterfaceMethodref)this.listOfConstants.get(index);
   }

   public ConstantValue getConstantValue(int index) {
      return (ConstantValue)this.listOfConstants.get(index);
   }

   public ConstantInteger getConstantInteger(int index) {
      return (ConstantInteger)this.listOfConstants.get(index);
   }
}
