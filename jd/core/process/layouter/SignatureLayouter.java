package jd.core.process.layouter;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.layout.block.GenericExtendsSuperInterfacesLayoutBlock;
import jd.core.model.layout.block.GenericExtendsSuperTypeLayoutBlock;
import jd.core.model.layout.block.GenericImplementsInterfacesLayoutBlock;
import jd.core.model.layout.block.GenericTypeNameLayoutBlock;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.util.CharArrayUtil;
import jd.core.util.SignatureUtil;

public class SignatureLayouter {
   public static boolean CreateLayoutBlocksForClassSignature(ClassFile classFile, String signature, List<LayoutBlock> layoutBlockList) {
      boolean displayExtendsOrImplementsFlag = false;
      char[] caSignature = signature.toCharArray();
      int length = caSignature.length;
      byte index = 0;
      layoutBlockList.add(new GenericTypeNameLayoutBlock(classFile, signature));
      int index1 = SkipGenerics(caSignature, length, index);
      int newIndex = SignatureUtil.SkipSignature(caSignature, length, index1);
      if((classFile.access_flags & 16896) == 0 && !IsObjectClass(caSignature, index1, newIndex)) {
         displayExtendsOrImplementsFlag = true;
         layoutBlockList.add(new GenericExtendsSuperTypeLayoutBlock(classFile, caSignature, index1));
      }

      if(newIndex < length) {
         displayExtendsOrImplementsFlag = true;
         if((classFile.access_flags & 512) != 0) {
            layoutBlockList.add(new GenericExtendsSuperInterfacesLayoutBlock(classFile, caSignature, newIndex));
         } else {
            layoutBlockList.add(new GenericImplementsInterfacesLayoutBlock(classFile, caSignature, newIndex));
         }
      }

      return displayExtendsOrImplementsFlag;
   }

   private static int SkipGenerics(char[] caSignature, int length, int index) {
      if(caSignature[index] == 60) {
         int depth = 1;

         while(index < length) {
            ++index;
            char c = caSignature[index];
            if(c == 60) {
               ++depth;
            } else if(c == 62) {
               if(depth <= 1) {
                  ++index;
                  break;
               }

               --depth;
            }
         }
      }

      return index;
   }

   private static boolean IsObjectClass(char[] caSignature, int beginIndex, int endIndex) {
      return CharArrayUtil.Substring(caSignature, beginIndex, endIndex).equals("Ljava/lang/Object;");
   }
}
