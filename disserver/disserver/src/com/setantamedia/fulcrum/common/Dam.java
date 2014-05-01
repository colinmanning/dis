/**
 *
 */
package com.setantamedia.fulcrum.common;

import com.setantamedia.fulcrum.DamManager;
import java.util.HashMap;

/**
 * @author Colin Manning
 *
 */
public class Dam {

   private String name = null;
   public HashMap<String, Connection> connections = new HashMap<>();
   public DamManager manager = null;

   public Dam() {

   }

   /**
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * @param name
    *           the name to set
    */
   public void setName(String name) {
      this.name = name;
   }

   public Connection getConnection(String name) {
	   return connections.get(name);
   }
}
