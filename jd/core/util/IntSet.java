package jd.core.util;

public class IntSet {
   private int[] values = null;
   private int capacity = 0;
   private int size = 0;
   private int min;
   private int max;

   public int size() {
      return this.size;
   }

   public void add(int newValue) {
      if(this.capacity == 0) {
         this.capacity = 5;
         this.values = new int[this.capacity];
         this.size = 1;
         this.min = this.max = this.values[0] = newValue;
      } else {
         if(this.capacity == this.size) {
            this.capacity *= 2;
            int[] firstIndex = new int[this.capacity];
            System.arraycopy(this.values, 0, firstIndex, 0, this.size);
            this.values = firstIndex;
         }

         if(this.max < newValue) {
            this.values[this.size++] = newValue;
            this.max = newValue;
         } else if(newValue < this.min) {
            System.arraycopy(this.values, 0, this.values, 1, this.size);
            this.min = this.values[0] = newValue;
            ++this.size;
         } else {
            int var6 = 0;
            int lastIndex = this.size - 1;

            int medIndex;
            int value;
            while(var6 < lastIndex) {
               medIndex = (lastIndex + var6) / 2;
               value = this.values[medIndex];
               if(value < newValue) {
                  var6 = medIndex + 1;
               } else {
                  if(value <= newValue) {
                     break;
                  }

                  lastIndex = medIndex - 1;
               }
            }

            medIndex = (lastIndex + var6) / 2;
            value = this.values[medIndex];
            if(value < newValue) {
               ++medIndex;
               System.arraycopy(this.values, medIndex, this.values, medIndex + 1, this.size - medIndex);
               this.values[medIndex] = newValue;
               ++this.size;
            } else if(value > newValue) {
               System.arraycopy(this.values, medIndex, this.values, medIndex + 1, this.size - medIndex);
               this.values[medIndex] = newValue;
               ++this.size;
            }
         }
      }

   }

   public int[] toArray() {
      if(this.values == null) {
         return null;
      } else {
         int[] tmp = new int[this.size];
         System.arraycopy(this.values, 0, tmp, 0, this.size);
         return tmp;
      }
   }

   public int get(int index) {
      if(this.values != null && index < this.size) {
         return this.values[index];
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
      }
   }
}
