package jd.core;

import jd.core.loader.Loader;
import jd.core.loader.LoaderException;
import jd.core.preferences.Preferences;
import jd.core.printer.Printer;

public interface Decompiler {
   void decompile(Preferences var1, Loader var2, Printer var3, String var4) throws LoaderException;
}
