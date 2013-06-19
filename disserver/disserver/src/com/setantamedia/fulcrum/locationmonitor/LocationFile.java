/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.setantamedia.fulcrum.locationmonitor;

import java.io.File;
import java.nio.file.Path;

/**
 *
 * @author Colin Manning
 */
public class LocationFile {

   private LocationDirectory parent = null;
   private Path me = null;

   public LocationFile() {
   }

   public Path getMe() {
      return me;
   }

   public void setMe(Path me) {
      this.me = me;
   }

   public LocationDirectory getParent() {
      return parent;
   }

   public void setParent(LocationDirectory parent) {
      this.parent = parent;
   }


}
