package com.setantamedia.dis.workflow.locations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.setantamedia.fulcrum.locationmonitor.FileProcessor;
import com.setantamedia.fulcrum.ws.types.Category;
import com.setantamedia.fulcrum.ws.types.User;

/**
 * A file processor to watch a folder for directories and files, create
 * categories for folders, and upload filesas they arrive. Example usage would
 * be a simple Dropbox folder monitor.
 */
public class SimpleFolderWatcher extends FileProcessor {

   private final static Logger logger = Logger.getLogger(SimpleFolderWatcher.class);
   public final static String PARAM_CATEGORY_ROOT = "categoryRoot";
   public final static String PARAM_ASSET_HANDLING_SET = "assetHandlingSet";
   public final static String PARAM_DELETE_FILE = "deleteFile";
   public final static String DEFAULT_CATEGORY = "$Categories";
   public final static String DEFAULT_ASSET_HANDLING_SET = "Standard";
   private String categoryRoot = DEFAULT_CATEGORY;
   private String assetHandlingSet = DEFAULT_ASSET_HANDLING_SET;
   private Category fileCategory = null;
   private Boolean deleteFile = false;

   /**
    * Initialize things, in particular process the parameters from the config
    * file. If the categoryRoot does not exist in the DAM, it will be created.
    * Note the "Upload Profile" is the term we use for rules that define how a
    * file is uploaded - f.r example, the name of a Cumulus "Asset Handling Set"
    */
   @Override
   public void init() {
      super.init();
      try {
         if (params.get(PARAM_CATEGORY_ROOT) != null) {
            categoryRoot = params.get(PARAM_CATEGORY_ROOT);
         }
         if (params.get(PARAM_ASSET_HANDLING_SET) != null) {
            assetHandlingSet = params.get(PARAM_ASSET_HANDLING_SET);
         }
         if (params.get(PARAM_DELETE_FILE) != null) {
            deleteFile = Boolean.valueOf(params.get(PARAM_ASSET_HANDLING_SET));
         }
         fileCategory = dam.manager.createCategory(dam.getConnection(damConnectionName), null, categoryRoot);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /*
    * Create a category for the folder
    */
   private Category doFolder(Path folder) {
      Category result = null;
      try {
         String folderCategoryPath = buildFolderPath(folder);
         //System.out.println("Creating category: "+folderCategoryPath);
         result = dam.manager.createCategory(dam.getConnection(damConnectionName), null, folderCategoryPath);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }
   
   private String buildFolderPath(Path file) {
      String result = categoryRoot;
      try {
         Path parent = file;
         if (!Files.isDirectory(file)) {
            parent = file.getParent();
         }
         ArrayList<String> folderBits = new ArrayList<>();
         if (parent != null && !Files.isSameFile(rootFolder, parent)) {
            folderBits.add(parent.getFileName().toString());
            parent = parent.getParent();
            while (parent != null && !Files.isSameFile(rootFolder, parent)) {
               folderBits.add(parent.getFileName().toString());
               parent = parent.getParent();
            }
         }
         if (folderBits.size() > 0) {
            for (int i = folderBits.size() - 1; i >= 0; i--) {
               result = result + ":" + folderBits.get(i);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }
   
   /*
    * Upload a converted file
    */
   private String doUpload(Path file) {
      String result = "-1";
      try {
         User user = null;
         HashMap<String, String> fields = new HashMap<>();
         String damId = dam.manager.uploadFile(dam.getConnection(damConnectionName), user, file, file.getFileName().toString(), assetHandlingSet, fields);

         /*
          * If we get a valid id from the DAM, then link the file to the
          * category created form the file name pattern
          */
         if (damId != null && !"-1".equals(damId)) {
            Category category = doFolder(file);
            dam.manager.addRecordToCategory(dam.getConnection(damConnectionName), String.valueOf(category.getId()), damId);
            result = damId;
            logger.info("Uploaded file: '"+file.toString()+"'"+" with DAM id: "+damId);
            if (deleteFile) {
               logger.info("Deleting original file: '"+file.toString()+"'");
               Files.deleteIfExists(file);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   @Override
   public void fileCreated(Path file) {
      try {
         if (ignoreFile(file)) {
            return;
         }

         if (Files.isDirectory(file)) {
            doFolder(file);
         } else {
            doUpload(file);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void fileModified(Path file) {
      if (ignoreFile(file)) {
         return;
      }
   }

   @Override
   public void fileDeleted(Path file) {
      if (ignoreFile(file)) {
         return;
      }
   }

   @Override
   public void directoryModified(Path directory) {
      if (ignoreFile(directory)) {
         return;
      }
      // for now, assume this is a rename of a new Folder created
      doFolder(directory);
   }

   @Override
   public void terminate() {

   }
}
