package jd.core.model.classfile;

import java.util.ArrayList;
import jd.core.model.classfile.LocalVariable;

public class LocalVariables {
   private ArrayList<LocalVariable> listOfLocalVariables;
   private int indexOfFirstLocalVariable = 0;

   public LocalVariables() {
      this.listOfLocalVariables = new ArrayList(1);
   }

   public LocalVariables(LocalVariable[] localVariableTable, LocalVariable[] localVariableTypeTable) {
      int length = localVariableTable.length;
      this.listOfLocalVariables = new ArrayList(length);

      for(int i = 0; i < length; ++i) {
         LocalVariable localVariable = localVariableTable[i];
         if(localVariableTypeTable != null) {
            int typeLength = localVariableTypeTable.length;

            for(int j = 0; j < typeLength; ++j) {
               LocalVariable typeLocalVariable = localVariableTypeTable[j];
               if(typeLocalVariable != null && localVariable.compareTo(typeLocalVariable) == 0) {
                  localVariableTypeTable[j] = null;
                  localVariable = typeLocalVariable;
                  break;
               }
            }
         }

         this.add(localVariable);
      }

   }

   public void add(LocalVariable localVariable) {
      int length = this.listOfLocalVariables.size();
      int index = localVariable.index;

      for(int i = 0; i < length; ++i) {
         LocalVariable lv = (LocalVariable)this.listOfLocalVariables.get(i);
         if(lv.index == index && lv.start_pc > localVariable.start_pc || lv.index > index) {
            this.listOfLocalVariables.add(i, localVariable);
            return;
         }
      }

      this.listOfLocalVariables.add(localVariable);
   }

   public LocalVariable get(int i) {
      return (LocalVariable)this.listOfLocalVariables.get(i);
   }

   public void remove(int i) {
      this.listOfLocalVariables.remove(i);
   }

   public String toString() {
      return this.listOfLocalVariables.toString();
   }

   public LocalVariable getLocalVariableAt(int i) {
      return i >= this.listOfLocalVariables.size()?null:(LocalVariable)this.listOfLocalVariables.get(i);
   }

   public LocalVariable getLocalVariableWithIndexAndOffset(int index, int offset) {
      int length = this.listOfLocalVariables.size();

      for(int i = length - 1; i >= 0; --i) {
         LocalVariable lv = (LocalVariable)this.listOfLocalVariables.get(i);
         if(lv.index == index && lv.start_pc <= offset && offset < lv.start_pc + lv.length) {
            return lv;
         }
      }

      return null;
   }

   public boolean containsLocalVariableWithNameIndex(int nameIndex) {
      int length = this.listOfLocalVariables.size();

      for(int i = length - 1; i >= 0; --i) {
         LocalVariable lv = (LocalVariable)this.listOfLocalVariables.get(i);
         if(lv.name_index == nameIndex) {
            return true;
         }
      }

      return false;
   }

   public void removeLocalVariableWithIndexAndOffset(int index, int offset) {
      int length = this.listOfLocalVariables.size();

      for(int i = length - 1; i >= 0; --i) {
         LocalVariable lv = (LocalVariable)this.listOfLocalVariables.get(i);
         if(lv.index == index && lv.start_pc <= offset && offset < lv.start_pc + lv.length) {
            this.listOfLocalVariables.remove(i);
            break;
         }
      }

   }

   public LocalVariable searchLocalVariableWithIndexAndOffset(int index, int offset) {
      int length = this.listOfLocalVariables.size();

      for(int i = length - 1; i >= 0; --i) {
         LocalVariable lv = (LocalVariable)this.listOfLocalVariables.get(i);
         if(lv.index == index && lv.start_pc <= offset) {
            return lv;
         }
      }

      return null;
   }

   public int size() {
      return this.listOfLocalVariables.size();
   }

   public int getIndexOfFirstLocalVariable() {
      return this.indexOfFirstLocalVariable;
   }

   public void setIndexOfFirstLocalVariable(int indexOfFirstLocalVariable) {
      this.indexOfFirstLocalVariable = indexOfFirstLocalVariable;
   }

   public int getMaxLocalVariableIndex() {
      int length = this.listOfLocalVariables.size();
      return length == 0?-1:((LocalVariable)this.listOfLocalVariables.get(length - 1)).index;
   }
}
