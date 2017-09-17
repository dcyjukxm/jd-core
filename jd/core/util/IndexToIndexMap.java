package jd.core.util;

public class IndexToIndexMap {
   private static final int INITIAL_CAPACITY = 128;
   private IndexToIndexMap.MapEntry[] entries = new IndexToIndexMap.MapEntry[128];

   public void put(int key, int value) {
      int index = this.hashCodeToIndex(key, this.entries.length);

      for(IndexToIndexMap.MapEntry entry = this.entries[index]; entry != null; entry = entry.next) {
         if(entry.key == key) {
            entry.value = value;
            return;
         }
      }

      this.entries[index] = new IndexToIndexMap.MapEntry(key, value, this.entries[index]);
   }

   public int get(int key) {
      int index = this.hashCodeToIndex(key, this.entries.length);

      for(IndexToIndexMap.MapEntry entry = this.entries[index]; entry != null; entry = entry.next) {
         if(entry.key == key) {
            return entry.value;
         }
      }

      return -1;
   }

   private int hashCodeToIndex(int hashCode, int size) {
      return hashCode & size - 1;
   }

   private static class MapEntry {
      public int key;
      public int value;
      public IndexToIndexMap.MapEntry next;

      public MapEntry(int key, int value, IndexToIndexMap.MapEntry next) {
         this.key = key;
         this.value = value;
         this.next = next;
      }
   }
}
