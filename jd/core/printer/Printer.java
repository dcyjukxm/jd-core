package jd.core.printer;

public interface Printer {
   int UNKNOWN_LINE_NUMBER = 0;

   void print(byte var1);

   void print(char var1);

   void print(int var1);

   void print(String var1);

   void printNumeric(String var1);

   void printString(String var1, String var2);

   void printKeyword(String var1);

   void printJavaWord(String var1);

   void printType(String var1, String var2, String var3);

   void printTypeDeclaration(String var1, String var2);

   void printTypeImport(String var1, String var2);

   void printField(String var1, String var2, String var3, String var4);

   void printFieldDeclaration(String var1, String var2, String var3);

   void printStaticField(String var1, String var2, String var3, String var4);

   void printStaticFieldDeclaration(String var1, String var2, String var3);

   void printConstructor(String var1, String var2, String var3, String var4);

   void printConstructorDeclaration(String var1, String var2, String var3);

   void printStaticConstructorDeclaration(String var1, String var2);

   void printMethod(String var1, String var2, String var3, String var4);

   void printMethodDeclaration(String var1, String var2, String var3);

   void printStaticMethod(String var1, String var2, String var3, String var4);

   void printStaticMethodDeclaration(String var1, String var2, String var3);

   void start(int var1, int var2, int var3);

   void end();

   void indent();

   void desindent();

   void startOfLine(int var1);

   void endOfLine();

   void extraLine(int var1);

   void startOfComment();

   void endOfComment();

   void startOfJavadoc();

   void endOfJavadoc();

   void startOfXdoclet();

   void endOfXdoclet();

   void startOfError();

   void endOfError();

   void startOfImportStatements();

   void endOfImportStatements();

   void startOfTypeDeclaration(String var1);

   void endOfTypeDeclaration();

   void startOfAnnotationName();

   void endOfAnnotationName();

   void startOfOptionalPrefix();

   void endOfOptionalPrefix();

   void debugStartOfLayoutBlock();

   void debugEndOfLayoutBlock();

   void debugStartOfSeparatorLayoutBlock();

   void debugEndOfSeparatorLayoutBlock(int var1, int var2, int var3);

   void debugStartOfStatementsBlockLayoutBlock();

   void debugEndOfStatementsBlockLayoutBlock(int var1, int var2, int var3);

   void debugStartOfInstructionBlockLayoutBlock();

   void debugEndOfInstructionBlockLayoutBlock();

   void debugStartOfCommentDeprecatedLayoutBlock();

   void debugEndOfCommentDeprecatedLayoutBlock();

   void debugMarker(String var1);

   void debugStartOfCaseBlockLayoutBlock();

   void debugEndOfCaseBlockLayoutBlock();
}
