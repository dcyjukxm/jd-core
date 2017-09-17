package jd.core.util;

public class StringToIndexMap {
   private static final int INITIAL_CAPACITY = 256;
   private StringToIndexMap.HashEntry[] entries = new StringToIndexMap.HashEntry[256];

   public void put(String key, int value) {
      int hashCode = key.hashCode();
      int index = this.hashCodeToIndex(hashCode, this.entries.length);

      for(StringToIndexMap.HashEntry entry = this.entries[index]; entry != null; entry = entry.next) {
         if(entry.hashCode == hashCode && key.equals(entry.key)) {
            entry.value = value;
            return;
         }
      }

      this.entries[index] = new StringToIndexMap.HashEntry(key, hashCode, value, this.entries[index]);
   }

   public int get(String key) {
      int hashCode = key.hashCode();
      int index = this.hashCodeToIndex(hashCode, this.entries.length);

      for(StringToIndexMap.HashEntry entry = this.entries[index]; entry != null; entry = entry.next) {
         if(entry.hashCode == hashCode && key.equals(entry.key)) {
            return entry.value;
         }
      }

      return -1;
   }

   private int hashCodeToIndex(int hashCode, int size) {
      return hashCode & size - 1;
   }

   private static class HashEntry {
      public String key;
      public int hashCode;
      public int value;
      public StringToIndexMap.HashEntry next;

      public HashEntry(String key, int hashCode, int value, StringToIndexMap.HashEntry next) {
         this.key = key;
         this.hashCode = hashCode;
         this.value = value;
         this.next = next;
      }
   }
}
