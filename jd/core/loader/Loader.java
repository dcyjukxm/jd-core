package jd.core.loader;

import java.io.DataInputStream;
import jd.core.loader.LoaderException;

public interface Loader {
   DataInputStream load(String var1) throws LoaderException;

   boolean canLoad(String var1);
}
