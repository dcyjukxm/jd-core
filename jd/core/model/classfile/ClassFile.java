package jd.core.model.classfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jd.core.model.classfile.Base;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.accessor.Accessor;
import jd.core.model.classfile.attribute.Attribute;
import jd.core.model.classfile.attribute.AttributeInnerClasses;
import jd.core.model.classfile.constant.ConstantClass;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.util.SignatureUtil;

public class ClassFile extends Base {
   private int minor_version;
   private int major_version;
   private int this_class;
   private int super_class;
   private int[] interfaces;
   private Field[] fields;
   private Method[] methods;
   private ConstantPool constants;
   private String thisClassName;
   private String superClassName;
   private String internalClassName;
   private String internalPackageName;
   private ClassFile outerClass = null;
   private Field outerThisField = null;
   private ArrayList<ClassFile> innerClassFiles = null;
   private Method staticMethod = null;
   private List<Instruction> enumValues = null;
   private String internalAnonymousClassName;
   private Map<String, Map<String, Accessor>> accessors;
   private Map<Integer, List<Integer>> switchMaps;

   public ClassFile(int minor_version, int major_version, ConstantPool constants, int access_flags, int this_class, int super_class, int[] interfaces, Field[] fields, Method[] methods, Attribute[] attributes) {
      super(access_flags, attributes);
      this.minor_version = minor_version;
      this.major_version = major_version;
      this.this_class = this_class;
      this.super_class = super_class;
      this.interfaces = interfaces;
      this.fields = fields;
      this.methods = methods;
      this.constants = constants;
      this.thisClassName = this.constants.getConstantClassName(this.this_class);
      this.superClassName = this.super_class == 0?null:this.constants.getConstantClassName(this.super_class);
      this.internalClassName = SignatureUtil.CreateTypeName(this.thisClassName);
      int index = this.thisClassName.lastIndexOf(47);
      this.internalPackageName = index == -1?"":this.thisClassName.substring(0, index);
      if(this.methods != null) {
         for(int i = this.methods.length - 1; i >= 0; --i) {
            Method method = this.methods[i];
            if((method.access_flags & 8) != 0 && method.name_index == this.constants.classConstructorIndex) {
               this.staticMethod = method;
               break;
            }
         }
      }

      this.internalAnonymousClassName = null;
      this.accessors = new HashMap(10);
      this.switchMaps = new HashMap();
   }

   public ConstantPool getConstantPool() {
      return this.constants;
   }

   public int[] getInterfaces() {
      return this.interfaces;
   }

   public int getMajorVersion() {
      return this.major_version;
   }

   public int getMinorVersion() {
      return this.minor_version;
   }

   public int getSuperClassIndex() {
      return this.super_class;
   }

   public int getThisClassIndex() {
      return this.this_class;
   }

   public String getClassName() {
      if(this.outerClass == null) {
         int outerClassName1 = this.thisClassName.lastIndexOf(47);
         return outerClassName1 == -1?this.thisClassName:this.thisClassName.substring(outerClassName1 + 1);
      } else {
         String outerClassName = this.outerClass.getThisClassName();
         return this.thisClassName.substring(outerClassName.length() + 1);
      }
   }

   public String getThisClassName() {
      return this.thisClassName;
   }

   public String getSuperClassName() {
      return this.superClassName;
   }

   public String getInternalClassName() {
      return this.internalClassName;
   }

   public String getInternalPackageName() {
      return this.internalPackageName;
   }

   public void setAccessFlags(int access_flags) {
      this.access_flags = access_flags;
   }

   public Field[] getFields() {
      return this.fields;
   }

   public Method[] getMethods() {
      return this.methods;
   }

   public Attribute[] getAttributes() {
      return this.attributes;
   }

   public AttributeInnerClasses getAttributeInnerClasses() {
      if(this.attributes != null) {
         for(int i = 0; i < this.attributes.length; ++i) {
            if(this.attributes[i].tag == 8) {
               return (AttributeInnerClasses)this.attributes[i];
            }
         }
      }

      return null;
   }

   private boolean isAnonymousClass() {
      int index = this.thisClassName.lastIndexOf(36);
      return index != -1 && index + 1 < this.thisClassName.length()?Character.isDigit(this.thisClassName.charAt(index + 1)):false;
   }

   public boolean isAInnerClass() {
      return this.outerClass != null;
   }

   public ClassFile getOuterClass() {
      return this.outerClass;
   }

   public void setOuterClass(ClassFile outerClass) {
      this.outerClass = outerClass;
      if(this.isAnonymousClass()) {
         ConstantClass cc = this.constants.getConstantClass(this.super_class);
         if(cc.name_index != this.constants.objectClassNameIndex) {
            this.internalAnonymousClassName = this.superClassName;
         } else if(this.interfaces != null && this.interfaces.length > 0) {
            int interfaceIndex = this.interfaces[0];
            this.internalAnonymousClassName = this.constants.getConstantClassName(interfaceIndex);
         } else {
            this.internalAnonymousClassName = "java/lang/Object";
         }
      } else {
         this.internalAnonymousClassName = null;
      }

   }

   public Field getOuterThisField() {
      return this.outerThisField;
   }

   public void setOuterThisField(Field outerThisField) {
      this.outerThisField = outerThisField;
   }

   public ArrayList<ClassFile> getInnerClassFiles() {
      return this.innerClassFiles;
   }

   public void setInnerClassFiles(ArrayList<ClassFile> innerClassFiles) {
      this.innerClassFiles = innerClassFiles;
   }

   public ClassFile getInnerClassFile(String internalClassName) {
      if(this.innerClassFiles != null && internalClassName.length() > this.thisClassName.length() + 1 && internalClassName.charAt(this.thisClassName.length()) == 36) {
         for(int i = this.innerClassFiles.size() - 1; i >= 0; --i) {
            if(((ClassFile)this.innerClassFiles.get(i)).thisClassName.equals(internalClassName)) {
               return (ClassFile)this.innerClassFiles.get(i);
            }
         }
      }

      return null;
   }

   public Field getField(int fieldNameIndex, int fieldDescriptorIndex) {
      if(this.fields != null) {
         for(int i = this.fields.length - 1; i >= 0; --i) {
            Field field = this.fields[i];
            if(fieldNameIndex == field.name_index && fieldDescriptorIndex == field.descriptor_index) {
               return field;
            }
         }
      }

      return null;
   }

   public Field getField(String fieldName, String fieldDescriptor) {
      if(this.fields != null) {
         for(int i = this.fields.length - 1; i >= 0; --i) {
            Field field = this.fields[i];
            String name = this.constants.getConstantUtf8(field.name_index);
            if(fieldName.equals(name)) {
               String descriptor = this.constants.getConstantUtf8(field.descriptor_index);
               if(fieldDescriptor.equals(descriptor)) {
                  return field;
               }
            }
         }
      }

      return null;
   }

   public Method getStaticMethod() {
      return this.staticMethod;
   }

   public Method getMethod(int methodNameIndex, int methodDescriptorIndex) {
      if(this.methods != null) {
         for(int i = this.methods.length - 1; i >= 0; --i) {
            Method method = this.methods[i];
            if(methodNameIndex == method.name_index && methodDescriptorIndex == method.descriptor_index) {
               return method;
            }
         }
      }

      return null;
   }

   public Method getMethod(String methodName, String methodDescriptor) {
      if(this.methods != null) {
         for(int i = this.methods.length - 1; i >= 0; --i) {
            Method method = this.methods[i];
            String name = this.constants.getConstantUtf8(method.name_index);
            if(methodName.equals(name)) {
               String descriptor = this.constants.getConstantUtf8(method.descriptor_index);
               if(methodDescriptor.equals(descriptor)) {
                  return method;
               }
            }
         }
      }

      return null;
   }

   public List<Instruction> getEnumValues() {
      return this.enumValues;
   }

   public void setEnumValues(List<Instruction> enumValues) {
      this.enumValues = enumValues;
   }

   public String getInternalAnonymousClassName() {
      return this.internalAnonymousClassName;
   }

   public void addAccessor(String name, String descriptor, Accessor accessor) {
      Object map = (Map)this.accessors.get(name);
      if(map == null) {
         map = new HashMap(1);
         this.accessors.put(name, map);
      }

      ((Map)map).put(descriptor, accessor);
   }

   public Accessor getAccessor(String name, String descriptor) {
      Map map = (Map)this.accessors.get(name);
      return map == null?null:(Accessor)map.get(descriptor);
   }

   public Map<Integer, List<Integer>> getSwitchMaps() {
      return this.switchMaps;
   }
}
