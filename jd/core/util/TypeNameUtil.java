package jd.core.util;

public class TypeNameUtil {
   public static String InternalTypeNameToInternalPackageName(String path) {
      int index = path.lastIndexOf(47);
      return index == -1?"":path.substring(0, index);
   }

   public static String InternalTypeNameToQualifiedTypeName(String path) {
      return path.replace('/', '.').replace('$', '.');
   }
}
