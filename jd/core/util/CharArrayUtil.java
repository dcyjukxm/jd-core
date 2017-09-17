package jd.core.util;

public class CharArrayUtil {
   public static String Substring(char[] ca, int beginIndex, int endIndex) {
      return new String(ca, beginIndex, endIndex - beginIndex);
   }

   public static int IndexOf(char[] ca, char ch, int fromIndex) {
      for(int length = ca.length; fromIndex < length; ++fromIndex) {
         if(ca[fromIndex] == ch) {
            return fromIndex;
         }
      }

      return -1;
   }
}
