package com.printflow2.workflow.locations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.setantamedia.fulcrum.locationmonitor.FileProcessor;
import com.setantamedia.fulcrum.ws.types.Category;
import com.setantamedia.fulcrum.ws.types.User;

public class PrintFlow2LocationMonitor  extends FileProcessor {
   
   private final static Logger logger = Logger.getLogger(PrintFlow2LocationMonitor.class);
   public final static String PARAM_CATEGORY_ROOT = "categoryRoot";
   public final static String PARAM_ASSET_HANDLING_SET = "assetHandlingSet";
   public final static String PARAM_DELETE_FILE = "deleteFile";
   public final static String DEFAULT_CATEGORY = "$Categories";
   public final static String DEFAULT_ASSET_HANDLING_SET = "Standard";
   private String categoryRoot = DEFAULT_CATEGORY;
   private String assetHandlingSet = DEFAULT_ASSET_HANDLING_SET;
   private Boolean deleteFile = false;

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
         dam.manager.createCategory(dam.getConnection(damConnectionName), null, categoryRoot);
      } catch (Exception e) {
         e.printStackTrace();
      }
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
   public void directoryModified(Path arg0) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void terminate() {      
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

}
