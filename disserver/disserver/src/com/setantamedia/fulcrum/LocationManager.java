package com.setantamedia.fulcrum;

import com.setantamedia.fulcrum.common.Location;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * Manage server locations
 *
 * @author colinmanning
 *
 */
public class LocationManager {

   private static Logger logger = Logger.getLogger(LocationManager.class);
   private HashMap<String, Location> locations = null;
   private FileSystem fs = FileSystems.getDefault();

   public LocationManager() {
   }

   /**
    * Make a folder in the location
    *
    * @param location
    * @param folderPath
    * @param accessCode
    * @return
    */
   public boolean makeLocationFolder(String location, String folderPath, String accessCode) {
      boolean result = false;
      try {
         Path path = null;
         Location loc = locations.get(location);
         if (loc != null && loc.getAccessCode() != null && accessCode != null && accessCode.equals(loc.getAccessCode())) {
            if (loc.getFolder() != null) {
               path = loc.getFolder();
               path = loc.getFolder().resolve(fs.getPath(folderPath));
               if (!Files.exists(path)) {
                  logger.info("Creating server folder " + path.toString());
                  Files.createDirectories(path);
               }
               // locations should have a tmp folder
               Path tmpFolder = path.resolve(fs.getPath("tmp"));
               if (!Files.exists(tmpFolder)) {
                  logger.info("Creating server folder " + tmpFolder.toString());
                  Files.createDirectories(tmpFolder);
               }
            }
            result = true;
         } else {
            logger.error("invalid access code specified for location: " + location);
         }
      } catch (Exception e) {
         result = false;
         e.printStackTrace();
      }
      return result;
   }

   public HashMap<String, Location> getLocations() {
      return locations;
   }

   public void setLocations(HashMap<String, Location> locations) {
      this.locations = locations;
   }

   public Location getLocation(String name) {
      return locations.get(name);
   }

   public Location addLocation(String name, Location location) {
      return locations.put(name, location);
   }
}
