package jd.core.process;

import java.util.ArrayList;
import jd.core.Decompiler;
import jd.core.loader.Loader;
import jd.core.loader.LoaderException;
import jd.core.model.classfile.ClassFile;
import jd.core.model.reference.ReferenceMap;
import jd.core.preferences.Preferences;
import jd.core.printer.Printer;
import jd.core.process.analyzer.classfile.ClassFileAnalyzer;
import jd.core.process.analyzer.classfile.ReferenceAnalyzer;
import jd.core.process.deserializer.ClassFileDeserializer;
import jd.core.process.layouter.ClassFileLayouter;
import jd.core.process.writer.ClassFileWriter;

public class DecompilerImpl implements Decompiler {
   public void decompile(Preferences preferences, Loader loader, Printer printer, String internalClassPath) throws LoaderException {
      ClassFile classFile = ClassFileDeserializer.Deserialize(loader, internalClassPath);
      if(classFile == null) {
         throw new LoaderException("Can not deserialize \'" + internalClassPath + "\'.");
      } else {
         ReferenceMap referenceMap = new ReferenceMap();
         ClassFileAnalyzer.Analyze(referenceMap, classFile);
         ReferenceAnalyzer.Analyze(referenceMap, classFile);
         ArrayList layoutBlockList = new ArrayList(1024);
         int maxLineNumber = ClassFileLayouter.Layout(preferences, referenceMap, classFile, layoutBlockList);
         ClassFileWriter.Write(loader, printer, referenceMap, maxLineNumber, classFile.getMajorVersion(), classFile.getMinorVersion(), layoutBlockList);
      }
   }
}
