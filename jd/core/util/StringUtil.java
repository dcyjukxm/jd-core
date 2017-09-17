package jd.core.util;

public class StringUtil {
   private static void EscapeChar(StringBuffer sb, char c) {
      switch(c) {
      case '\b':
         sb.append("\\b");
         break;
      case '\t':
         sb.append("\\t");
         break;
      case '\n':
         sb.append("\\n");
         break;
      case '\f':
         sb.append("\\f");
         break;
      case '\r':
         sb.append("\\r");
         break;
      case '\\':
         sb.append("\\\\");
         break;
      default:
         if(c < 32) {
            sb.append("\\0");
            sb.append((char)(48 + (c >> 3)));
            sb.append((char)(48 + (c & 7)));
         } else {
            sb.append(c);
         }
      }

   }

   public static String EscapeStringAndAppendQuotationMark(String s) {
      int length = s.length();
      StringBuffer sb = new StringBuffer(length * 2 + 2);
      sb.append('\"');
      if(length > 0) {
         for(int i = 0; i < length; ++i) {
            if(s.charAt(i) == 34) {
               sb.append("\\\"");
            } else {
               EscapeChar(sb, s.charAt(i));
            }
         }
      }

      sb.append('\"');
      return sb.toString();
   }

   public static String EscapeCharAndAppendApostrophe(char c) {
      StringBuffer sb = new StringBuffer(10);
      sb.append('\'');
      if(c == 39) {
         sb.append("\\\'");
      } else {
         EscapeChar(sb, c);
      }

      sb.append('\'');
      return sb.toString();
   }
}
