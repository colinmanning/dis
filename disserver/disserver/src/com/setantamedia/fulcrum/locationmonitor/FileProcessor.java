package com.setantamedia.fulcrum.locationmonitor;

import com.setantamedia.fulcrum.AdvancedServer;
import com.setantamedia.fulcrum.common.Dam;
import com.setantamedia.fulcrum.common.Database;
import com.setantamedia.fulcrum.config.FulcrumConfig;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;

public abstract class FileProcessor implements LocationListener {

   private static Logger logger = Logger.getLogger(FileProcessor.class);
   public final static String PARAM_MESSAGEURL = "messageUrl";
   public final static String PARAM_DATABASE = "database";
   public final static String PARAM_CONNECTION = "connection";
   public final static String PARAM_DAM_CONNECTION = "damConnection";
   public final static String PARAM_VIEW = "view";
   public final static String PARAM_DESTINATION = "destination";
   protected HashMap<String, String> params = null;
   protected HashMap<String, Database> databases = null;
   protected FulcrumConfig fulcrumConfig = null;
   protected String dbName = null;
   protected String connectionName = null;
   protected String view = null;
   protected Path destination = null;
   protected Path rootFolder = null;
   protected Path tmpFolder = null;
   protected Dam dam = null;
   protected String damConnectionName = null;
   protected String messageUrl = null;
   protected ArrayList<String> ignoreFiles = null;
   protected String previewCacheFolder = null;
   protected AdvancedServer mainServer;

   public FileProcessor() {
   }

   public AdvancedServer getMainServer() {
      return mainServer;
   }

   public void setMainServer(AdvancedServer mainServer) {
      this.mainServer = mainServer;
   }

   protected boolean ignoreFile(Path path) {
      boolean result = false;
      String fileName = path.getFileName().toString();
      for (String ignoreFileName : ignoreFiles) {
         if (fileName.endsWith(ignoreFileName)) {
            result = true;
            break;
         }
      }
      return result;
   }

   @Override
   public void init() {
      FileSystem fs = FileSystems.getDefault();
      try {
         // default ignore files, can be changed in sub classes
         ignoreFiles = new ArrayList<>(3);
         ignoreFiles.add(".DS_Store");
         ignoreFiles.add("untitled folder");
         ignoreFiles.add("New folder");
         if (params.get(PARAM_MESSAGEURL) != null) {
            messageUrl = params.get(PARAM_MESSAGEURL);
         }
         if (params.get(PARAM_DATABASE) != null) {
            dbName = params.get(PARAM_DATABASE);
         }
         if (params.get(PARAM_CONNECTION) != null) {
            connectionName = params.get(PARAM_CONNECTION);
         }
         if (params.get(PARAM_DAM_CONNECTION) != null) {
            damConnectionName = params.get(PARAM_DAM_CONNECTION);
         }
         if (params.get(PARAM_VIEW) != null) {
            view = params.get(PARAM_VIEW);
         }
         if (params.get(PARAM_DESTINATION) != null) {
            destination = fs.getPath(params.get(PARAM_DESTINATION));
         }
         if (destination != null && !Files.exists(destination)) {
            Files.createDirectories(destination);
         }
         if (!Files.exists(rootFolder)) {
            Files.createDirectories(rootFolder);
         }
         tmpFolder = rootFolder.resolve("tmp");
         if (!Files.exists(tmpFolder)) {
            Files.createDirectories(tmpFolder);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void fileModified(Path path) {
      if (!ignoreFile(path.getFileName())) {
         logger.debug("file modify event for file: " + path.toString());
      }
   }

   @Override
   public void fileDeleted(Path path) {
      if (!ignoreFile(path.getFileName())) {
         logger.debug("file delete event for file: " + path.toString());
      }
   }

   @Override
   public void fileCreated(Path path) {
      if (!ignoreFile(path.getFileName())) {
         logger.debug("file create event for file: " + path.toString());
      }
   }

   @Override
   public void directoryCreated(Path path) {
      if (!ignoreFile(path.getFileName())) {
         logger.debug("directory create event for file: " + path.toString());
      }
   }

   @Override
   public void directoryDeleted(Path path) {
      if (!ignoreFile(path)) {
         logger.debug("directory delete event for file: " + path.toString());
      }
   }

   public HashMap<String, Database> getDatabases() {
      return databases;
   }

   public void setDatabases(HashMap<String, Database> databases) {
      this.databases = databases;
   }

   public Dam getDamManager() {
      return dam;
   }

   public void setDam(Dam damManager) {
      this.dam = damManager;
   }

   public String getDbName() {
      return dbName;
   }

   public void setDbName(String dbName) {
      this.dbName = dbName;
   }

   public FulcrumConfig getFulcrumConfig() {
      return fulcrumConfig;
   }

   @Override
   public void setFulcrumConfig(FulcrumConfig fulcrumConfig) {
      this.fulcrumConfig = fulcrumConfig;
   }

   public HashMap<String, String> getParams() {
      return params;
   }

   public void setParams(HashMap<String, String> params) {
      this.params = params;
   }

   protected String printCmd(String[] cmd) {
      String result = "";
      boolean first = true;
      for (String bit : cmd) {
         if (first) {
            result = bit;
            first = false;
         } else {
            result += " " + bit;
         }

      }
      return result;
   }

   public ArrayList<String> getIgnoreFiles() {
      return ignoreFiles;
   }

   public void setIgnoreFiles(ArrayList<String> ignoreFiles) {
      this.ignoreFiles = ignoreFiles;
   }

   public void addIgnoreFile(String name) {
      ignoreFiles.add(name);
   }

   public void removeIgnoreFile(String name) {
      ignoreFiles.remove(name);
   }

   public String getPreviewCacheFolder() {
      return previewCacheFolder;
   }

   public void setPreviewCacheFolder(String previewCacheFolder) {
      this.previewCacheFolder = previewCacheFolder;
   }

   public Path getRootFolder() {
      return rootFolder;
   }

   public void setRootFolder(Path rootFolder) {
      this.rootFolder = rootFolder;
   }

   /**
    * Should be implemented by sub classes, as it will be called on shutdown of the running application.
    * Failure to implement this ´correctly can result in processes running in the operating system hanging, and locking resources.
    */
   @Override
   public abstract void terminate();
}
