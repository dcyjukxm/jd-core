package jd.core.process.analyzer.variable;

public interface VariableNameGenerator {
   void clearLocalNames();

   String generateParameterNameFromSignature(String var1, boolean var2, boolean var3, int var4);

   String generateLocalVariableNameFromSignature(String var1, boolean var2);
}
