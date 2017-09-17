package jd.core.model.reference;

import java.util.Collection;
import java.util.HashMap;
import jd.core.model.reference.Reference;

public class ReferenceMap {
   private HashMap<String, Reference> references = new HashMap();

   public void add(String internalName) {
      if(internalName.indexOf(59) != -1) {
         System.err.println("ReferenceMap.add: InvalidParameterException(" + internalName + ")");
      } else {
         Reference ref = (Reference)this.references.get(internalName);
         if(ref == null) {
            this.references.put(internalName, new Reference(internalName));
         } else {
            ref.incCounter();
         }
      }

   }

   public Reference remove(String internalName) {
      return (Reference)this.references.remove(internalName);
   }

   public Collection<Reference> values() {
      return this.references.values();
   }

   public boolean contains(String internalName) {
      return this.references.containsKey(internalName);
   }
}
