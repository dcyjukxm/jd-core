package jd.core.model.classfile;

public class LocalVariable implements Comparable<LocalVariable> {
   public int start_pc;
   public int length;
   public int name_index;
   public int signature_index;
   public final int index;
   public boolean exceptionOrReturnAddress;
   public int typesBitField;
   public boolean declarationFlag;
   public boolean finalFlag;

   public LocalVariable(int start_pc, int length, int name_index, int signature_index, int index) {
      this(start_pc, length, name_index, signature_index, index, false, 0);
   }

   public LocalVariable(int start_pc, int length, int name_index, int signature_index, int index, int typesBitSet) {
      this(start_pc, length, name_index, signature_index, index, false, typesBitSet);
   }

   public LocalVariable(int start_pc, int length, int name_index, int signature_index, int index, boolean exception) {
      this(start_pc, length, name_index, signature_index, index, exception, 0);
   }

   protected LocalVariable(int start_pc, int length, int name_index, int signature_index, int index, boolean exceptionOrReturnAddress, int typesBitField) {
      this.declarationFlag = false;
      this.finalFlag = false;
      this.start_pc = start_pc;
      this.length = length;
      this.name_index = name_index;
      this.signature_index = signature_index;
      this.index = index;
      this.exceptionOrReturnAddress = exceptionOrReturnAddress;
      this.declarationFlag = exceptionOrReturnAddress;
      this.typesBitField = typesBitField;
   }

   public void updateRange(int offset) {
      if(offset < this.start_pc) {
         this.length += this.start_pc - offset;
         this.start_pc = offset;
      }

      if(offset >= this.start_pc + this.length) {
         this.length = offset - this.start_pc + 1;
      }

   }

   public void updateSignatureIndex(int signatureIndex) {
      this.signature_index = signatureIndex;
   }

   public String toString() {
      return "LocalVariable{start_pc=" + this.start_pc + ", length=" + this.length + ", name_index=" + this.name_index + ", signature_index=" + this.signature_index + ", index=" + this.index + "}";
   }

   public int compareTo(LocalVariable other) {
      return other == null?-1:(this.name_index != other.name_index?this.name_index - other.name_index:(this.length != other.length?this.length - other.length:(this.start_pc != other.start_pc?this.start_pc - other.start_pc:this.index - other.index)));
   }
}
