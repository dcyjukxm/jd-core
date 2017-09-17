package jd.core.process.deserializer;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import jd.core.loader.Loader;
import jd.core.loader.LoaderException;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.attribute.Attribute;
import jd.core.model.classfile.attribute.AttributeInnerClasses;
import jd.core.model.classfile.attribute.InnerClass;
import jd.core.model.classfile.constant.Constant;
import jd.core.model.classfile.constant.ConstantClass;
import jd.core.model.classfile.constant.ConstantDouble;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantFloat;
import jd.core.model.classfile.constant.ConstantInteger;
import jd.core.model.classfile.constant.ConstantInterfaceMethodref;
import jd.core.model.classfile.constant.ConstantLong;
import jd.core.model.classfile.constant.ConstantMethodref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.classfile.constant.ConstantString;
import jd.core.model.classfile.constant.ConstantUtf8;
import jd.core.process.deserializer.AttributeDeserializer;
import jd.core.process.deserializer.ClassFormatException;

public class ClassFileDeserializer {
   public static ClassFile Deserialize(Loader loader, String internalClassPath) throws LoaderException {
      ClassFile classFile = LoadSingleClass(loader, internalClassPath);
      if(classFile == null) {
         return null;
      } else {
         AttributeInnerClasses aics = classFile.getAttributeInnerClasses();
         if(aics == null) {
            return classFile;
         } else {
            String internalClassPathPrefix = internalClassPath.substring(0, internalClassPath.length() - ".class".length());
            String innerInternalClassNamePrefix = internalClassPathPrefix + '$';
            ConstantPool constants = classFile.getConstantPool();
            InnerClass[] cs = aics.classes;
            int length = cs.length;
            ArrayList innerClassFiles = new ArrayList(length);

            for(int i = 0; i < length; ++i) {
               String innerInternalClassPath = constants.getConstantClassName(cs[i].inner_class_index);
               if(innerInternalClassPath.startsWith(innerInternalClassNamePrefix)) {
                  int offsetInternalInnerSeparator = innerInternalClassPath.indexOf(36, innerInternalClassNamePrefix.length());
                  if(offsetInternalInnerSeparator != -1) {
                     String innerClassFile = innerInternalClassPath.substring(0, offsetInternalInnerSeparator) + ".class";
                     if(loader.canLoad(innerClassFile)) {
                        continue;
                     }
                  }

                  try {
                     ClassFile var15 = Deserialize(loader, innerInternalClassPath + ".class");
                     if(var15 != null) {
                        var15.setAccessFlags(cs[i].inner_access_flags);
                        var15.setOuterClass(classFile);
                        innerClassFiles.add(var15);
                     }
                  } catch (LoaderException var14) {
                     ;
                  }
               }
            }

            if(innerClassFiles != null) {
               classFile.setInnerClassFiles(innerClassFiles);
            }

            return classFile;
         }
      }
   }

   private static ClassFile LoadSingleClass(Loader loader, String internalClassPath) throws LoaderException {
      DataInputStream dis = null;
      ClassFile classFile = null;

      try {
         dis = loader.load(internalClassPath);
         if(dis != null) {
            classFile = Deserialize(dis);
         }
      } catch (IOException var13) {
         classFile = null;
      } finally {
         if(dis != null) {
            try {
               dis.close();
            } catch (IOException var12) {
               ;
            }
         }

      }

      return classFile;
   }

   private static ClassFile Deserialize(DataInput di) throws IOException {
      CheckMagic(di);
      int minor_version = di.readUnsignedShort();
      int major_version = di.readUnsignedShort();
      Constant[] constants = DeserializeConstants(di);
      ConstantPool constantPool = new ConstantPool(constants);
      int access_flags = di.readUnsignedShort();
      int this_class = di.readUnsignedShort();
      int super_class = di.readUnsignedShort();
      int[] interfaces = DeserializeInterfaces(di);
      Field[] fieldInfos = DeserializeFields(di, constantPool);
      Method[] methodInfos = DeserializeMethods(di, constantPool);
      Attribute[] attributeInfos = AttributeDeserializer.Deserialize(di, constantPool);
      return new ClassFile(minor_version, major_version, constantPool, access_flags, this_class, super_class, interfaces, fieldInfos, methodInfos, attributeInfos);
   }

   private static Constant[] DeserializeConstants(DataInput di) throws IOException {
      int count = di.readUnsignedShort();
      if(count == 0) {
         return null;
      } else {
         Constant[] constants = new Constant[count];

         for(int i = 1; i < count; ++i) {
            byte tag = di.readByte();
            switch(tag) {
            case 1:
               constants[i] = new ConstantUtf8(tag, di.readUTF());
               break;
            case 2:
            default:
               throw new ClassFormatException("Invalid constant pool entry");
            case 3:
               constants[i] = new ConstantInteger(tag, di.readInt());
               break;
            case 4:
               constants[i] = new ConstantFloat(tag, di.readFloat());
               break;
            case 5:
               constants[i++] = new ConstantLong(tag, di.readLong());
               break;
            case 6:
               constants[i++] = new ConstantDouble(tag, di.readDouble());
               break;
            case 7:
               constants[i] = new ConstantClass(tag, di.readUnsignedShort());
               break;
            case 8:
               constants[i] = new ConstantString(tag, di.readUnsignedShort());
               break;
            case 9:
               constants[i] = new ConstantFieldref(tag, di.readUnsignedShort(), di.readUnsignedShort());
               break;
            case 10:
               constants[i] = new ConstantMethodref(tag, di.readUnsignedShort(), di.readUnsignedShort());
               break;
            case 11:
               constants[i] = new ConstantInterfaceMethodref(tag, di.readUnsignedShort(), di.readUnsignedShort());
               break;
            case 12:
               constants[i] = new ConstantNameAndType(tag, di.readUnsignedShort(), di.readUnsignedShort());
            }
         }

         return constants;
      }
   }

   private static int[] DeserializeInterfaces(DataInput di) throws IOException {
      int count = di.readUnsignedShort();
      if(count == 0) {
         return null;
      } else {
         int[] interfaces = new int[count];

         for(int i = 0; i < count; ++i) {
            interfaces[i] = di.readUnsignedShort();
         }

         return interfaces;
      }
   }

   private static Field[] DeserializeFields(DataInput di, ConstantPool constantPool) throws IOException {
      int count = di.readUnsignedShort();
      if(count == 0) {
         return null;
      } else {
         Field[] fieldInfos = new Field[count];

         for(int i = 0; i < count; ++i) {
            fieldInfos[i] = new Field(di.readUnsignedShort(), di.readUnsignedShort(), di.readUnsignedShort(), AttributeDeserializer.Deserialize(di, constantPool));
         }

         return fieldInfos;
      }
   }

   private static Method[] DeserializeMethods(DataInput di, ConstantPool constants) throws IOException {
      int count = di.readUnsignedShort();
      if(count == 0) {
         return null;
      } else {
         Method[] methodInfos = new Method[count];

         for(int i = 0; i < count; ++i) {
            methodInfos[i] = new Method(di.readUnsignedShort(), di.readUnsignedShort(), di.readUnsignedShort(), AttributeDeserializer.Deserialize(di, constants));
         }

         return methodInfos;
      }
   }

   private static void CheckMagic(DataInput di) throws IOException {
      int magic = di.readInt();
      if(magic != -889275714) {
         throw new ClassFormatException("Invalid Java .class file");
      }
   }
}
