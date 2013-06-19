/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.setantamedia.fulcrum.locationmonitor;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

/**
 *
 * @author Colin Manning
 */
public class LocationDirectory {

   private LocationDirectory parent = null;
   private Path folder = null;
   private HashMap<String, LocationFile> files = new HashMap<String, LocationFile>();
   private long lastScan = -1L;

   public LocationDirectory() {

   }

   public HashMap<String, LocationFile> getFiles() {
      return files;
   }

   public void setFiles(HashMap<String, LocationFile> files) {
      this.files = files;
   }

   public void addFile(File file) {
      LocationFile lf = new LocationFile();
      lf.setMe(folder);
      lf.setParent(this);
      files.put(file.getName(), lf);
   }

   public void removeFile(String name) {
      files.remove(name);
   }

   public Path getFolder() {
      return folder;
   }

   public void setFolder(Path me) {
      this.folder = me;
   }

   public LocationDirectory getParent() {
      return parent;
   }

   public void setParent(LocationDirectory parent) {
      this.parent = parent;
   }

   public long getLastScan() {
      return lastScan;
   }

   public void setLastScan(long lastScan) {
      this.lastScan = lastScan;
   }


}
