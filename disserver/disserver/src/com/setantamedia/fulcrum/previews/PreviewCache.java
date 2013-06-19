package com.setantamedia.fulcrum.previews;

import com.setantamedia.fulcrum.common.DirectoryContentsDeleter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

public class PreviewCache {

   protected Path cacheDir = null;
   protected DirectoryContentsDeleter directoryContentsDeleter = null;
   protected IdPathMatcher idPathMatcher = null;

   public PreviewCache(Path cacheDir) {
      this.cacheDir = cacheDir;
      idPathMatcher = new IdPathMatcher();
      directoryContentsDeleter = new DirectoryContentsDeleter();
      directoryContentsDeleter.setFilter(idPathMatcher);
      directoryContentsDeleter.setRecursive(false);
   }

   public Path getCacheDir() {
      return cacheDir;
   }

   /**
    * Returns a file to be used for a preview, and if it exists, and force is true, then delete the existing file if
    * possible
    *
    * @param fieldId
    * @param recordId
    * @param previewName
    * @param force
    * @return
    */
   public Path makeCacheFile(String database, String id, String previewName, boolean force) {
      return makeCacheFile(makeCachePath(database, id), id, previewName, force);
   }

   /**
    * Returns a file to be used for a preview, and if it exists, and force is true, then delete the existing file if
    * possible
    *
    * @param path
    * @param id
    * @param previewName
    * @param force
    * @return
    */
   public Path makeCacheFile(Path path, String id, String previewName, boolean force) {
      Path result = path.resolve(id + "_" + previewName.split(":")[0]);
      try {
         if (force && Files.exists(result)) {
            Files.delete(result);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   /**
    * Return the actual bytes form a preview file
    *
    * @param file
    * @return
    * @throws Exception
    */
   public byte[] getPreviewData(Path file) throws Exception {
      return Files.readAllBytes(file);
   }

   /**
    * Remove file in a cache path for a given record id preview, or just one preview if a preview name is provided
    *
    * @param path
    * @param id
    * @param cachePreviewName
    * @throws Exception
    */
   public void clearPathForRecord(Path path, String id, String previewName) throws Exception {
      if (previewName == null) {
         //idPathMatcher.setId(id);
         //Files.walkFileTree(path, directoryContentsDeleter);
         for (File file : path.toFile().listFiles()) {
            if (!file.isDirectory() && file.getName().startsWith(id)) {
               file.delete();
            }
         }
      } else {
         Path previewFile = path.resolve(id + "_" + previewName);
         try {
            if (Files.exists(previewFile)) {
               Files.delete(previewFile);
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   /**
    * Remove all files in a cache path for a given record id, or just one preview if a preview name is provided
    *
    * @param path
    * @param id
    * @throws Exception
    */
   public void clearPathForRecord(Path path, String id) throws Exception {
      clearPathForRecord(path, id, null);
   }

   /**
    * Looks at the record id, and calculates the directory structure based on the max id and folder level values set for
    * the cache
    *
    * @param id
    * @return
    */
   protected String makePartitionName(String id) {
      return mapIdToPath(id);
   }

   private String mapIdToPath(String id) {
      String result = id;
      try {
         // drop the last 2 digits, not part of folder structure
         int fid = new Integer(id);
         int base = fid / 100;
         int l[] = new int[4];
         for (int i = 3; i >= 0; i--) {
            l[i] = base % 100;
            base = base / 100;
         }
         result = String.format("%02d/%02d/%02d/%02d", l[0], l[1], l[2], l[3]);
      } catch (Exception e) {
         // probably not a number, so just return as is ad ignore exception
      }
      return result;
   }

   /**
    * Return a file that will be used to store a preview for an asset do not put the field id in the path - as we use
    * the id all the time
    *
    * @param database the database name
    * @param id the record id
    * @return
    */
   public Path makeCachePath(String database, String id) {
      String cacheLocation = database + "/" + makePartitionName(id);
      Path cachePath = cacheDir.resolve(cacheLocation);
      try {
         if (!Files.exists(cachePath)) {
            Files.createDirectories(cachePath);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return cachePath;
   }

   protected class IdPathMatcher implements PathMatcher {

      private String id;

      public IdPathMatcher() {
      }

      public IdPathMatcher(Integer id) {
         this.id = id.toString();
      }

      public IdPathMatcher(String id) {
         this.id = id;
      }

      @Override
      public boolean matches(Path path) {
         return (path.getFileName().toString().startsWith(id) ? true : false);
      }

      public String getId() {
         return id;
      }

      public void setId(String id) {
         this.id = id;
      }
   }
}
