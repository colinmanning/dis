package com.setantamedia.dis.workflow.locations;

import com.setantamedia.fulcrum.locationmonitor.FileProcessor;
import com.setantamedia.fulcrum.ws.FileServlet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class FtpFileProcessor extends FileProcessor {

   private final static Logger logger = Logger.getLogger(FtpFileProcessor.class);
   public final static String FTP_PASSIVE_MODE = "passive";
   public final static String FTP_ACTIVE_MODE = "active";
   public final static String PARAM_FTP_SERVER = "ftp-server";
   public final static String PARAM_FTP_PORT = "ftp-port";
   public final static String PARAM_FTP_USERNAME = "ftp-username";
   public final static String PARAM_FTP_PASSWORD = "ftp-password";
   public final static String PARAM_FTP_FOLDER = "ftp-folder";
   public final static String PARAM_CONTROL_FILE = FileServlet.PARAMETER_CONTROL_FILE;
   public final static String PARAM_TRANSFER_DONE_FIELD = "transferdonefield";
   public final static String PARAM_FTP_MODE = "ftp-mode";
   public final static String PARAM_EMBED_IPTC = "embed-iptc";
   private String ftpServer = null;
   private Integer ftpPort = null;
   private String ftpUsername = null;
   private String ftpPassword = null;
   private String ftpFolder = null;
   private String controlFileName = null;
   private FTPClient ftpClient = null;
   private String transferDoneField = null;
   private Boolean passiveMode = true;

   @Override
   public void terminate() {
   }

   @Override
   public void directoryModified(Path directory) {
   }

   @Override
   public void init() {
      super.init();
      try {
         if (params.get(PARAM_FTP_MODE) != null) {
            passiveMode = FTP_PASSIVE_MODE.equals(params.get(PARAM_FTP_MODE));
         }
         if (params.get(PARAM_FTP_SERVER) != null) {
            ftpServer = params.get(PARAM_FTP_SERVER);
         }
         if (params.get(PARAM_FTP_PORT) != null) {
            ftpPort = new Integer(params.get(PARAM_FTP_PORT));
         }
         if (params.get(PARAM_FTP_USERNAME) != null) {
            ftpUsername = params.get(PARAM_FTP_USERNAME);
         }
         if (params.get(PARAM_FTP_PASSWORD) != null) {
            ftpPassword = params.get(PARAM_FTP_PASSWORD);
         }
         if (params.get(PARAM_FTP_FOLDER) != null) {
            ftpFolder = params.get(PARAM_FTP_FOLDER);
         }
         if (params.get(PARAM_TRANSFER_DONE_FIELD) != null) {
            transferDoneField = params.get(PARAM_TRANSFER_DONE_FIELD);
         }
         controlFileName = (params.get(FileServlet.PARAMETER_CONTROL_FILE) != null) ? params.get(FileServlet.PARAMETER_CONTROL_FILE) : FileServlet.DEFAULT_CONTROL_FILE_NAME;
         ftpClient = new FTPClient();
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
            // ignore directories
            return;
         }

         //Testing with Tim - do nothing for now
         //if (true) return;

         // look for the control file
         String fname = file.getFileName().toString();
         if (fname.endsWith(".csv")) {
            logger.info("got a file list to process");
            String folderPath = null;
            String sourcePath = null;
            ftpClient.connect(ftpServer, ftpPort);
            if (passiveMode) {
               ftpClient.enterLocalPassiveMode();
            } else {
               ftpClient.enterLocalActiveMode();
            }
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
               logger.error("Cannto get reply from Ftp server: " + ftpServer + " on port: " + ftpPort);
            } else if (!ftpClient.isAvailable()) {
               logger.error("Ftp server: " + ftpServer + " on port: " + ftpPort + " does not seem to be available");
            } else if (ftpClient.isConnected()) {
               Path parentPath = file.getParent();
               if (ftpClient.login(ftpUsername, ftpPassword)) {
                  logger.info("Logged into ftp server: " + ftpServer + " with username: " + ftpUsername + " to transfer files from: " + parentPath.toString());
                  ftpClient.changeToParentDirectory();
                  if (ftpFolder != null) {
                     boolean dirFound = false;
                     FTPFile[] dirs = ftpClient.listDirectories();
                     for (FTPFile dir : dirs) {
                        if (ftpFolder.equals(dir.getName())) {
                           dirFound = true;
                           break;
                        }
                     }
                     if (!dirFound) {
                        ftpClient.makeDirectory(ftpFolder);
                     }
                     ftpClient.changeWorkingDirectory(ftpFolder);
                  }
                  List<String> lines = Files.readAllLines(file, Charset.forName("UTF-8"));
                  // Ignore the header
                  boolean ready = false;
                  FTPFile[] existingFiles = ftpClient.listFiles();
                  for (String line : lines) {
                     if (line.startsWith("Connection")) {
                        String[] bits = line.split(",");
                        damConnectionName = bits[1];
                     } else if (line.startsWith("Source Path")) {
                        String[] bits = line.split(",");
                        sourcePath = bits[1];
                     } else if (line.startsWith("Folder Path")) {
                        String[] bits = line.split(",");
                        if (bits.length > 1) {
                           folderPath = bits[1];
                           if (!"".equals(folderPath)) {
                              boolean folderPathFound = false;
                              FTPFile[] dirs = ftpClient.listDirectories();
                              for (FTPFile dir : dirs) {
                                 if (folderPath.equals(dir.getName())) {
                                    folderPathFound = true;
                                    break;
                                 }
                              }
                              if (!folderPathFound) {
                                 ftpClient.makeDirectory(folderPath);
                              }
                              ftpClient.changeWorkingDirectory(folderPath);
                           }
                        }
                     } else if (line.startsWith("Id,")) {
                        ready = true;
                        continue;
                     }
                     if (ready) {
                        if (folderPath != null && !"".equals(folderPath)) {
                        }
                        String[] bits = line.split(",");
                        String fileName = bits[1];
                        Path transferFile = parentPath.resolve(fileName);
                        // if file already exists on the Ftp Server, delete it first
                        for (FTPFile existingFile : existingFiles) {
                           if (fileName.equals(existingFile.getName())) {
                              ftpClient.deleteFile(fileName);
                              break;
                           }
                        }
                        try (InputStream is = Files.newInputStream(transferFile)) {
                           // we assume always binary transfer
                           ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                           ftpClient.storeFile(transferFile.getFileName().toString(), is);
                           Files.delete(transferFile);
                           if (transferDoneField != null) {
                              HashMap<String, String> updateData = new HashMap<>();
                              String assetId = bits[0];
                              updateData.put(transferDoneField, "true");
                              dam.manager.updateAssetData(dam.getConnection(damConnectionName), assetId, null, updateData);
                           }

                           // ok, if there is a transfer update field, set iz to true
                           if (transferDoneField != null) {
                              try {
                              } catch (Exception e) {
                                 e.printStackTrace();
                                 logger.error("Problem setting transferdone field value to true - field probably does not exist");
                              }
                           }

                        } catch (Exception fe) {
                           if (transferFile == null) {
                              logger.error("Problem getting file name for filelist record: '" + line + "'");
                           } else {
                              logger.error("Failed to transfer file: " + transferFile.toString());
                           }
                           fe.printStackTrace();
                        }
                     }
                  }
               } else {
                  logger.error("Cound not login to ftp server: " + ftpServer + " with username: " + ftpUsername);
               }
            } else {
               logger.error("Problem connecting to ftp server: " + ftpServer + " on port: " + ftpPort);
            }
            /*
             * better not to delete stuff, let external script do it, so they can see if all went ok
            Files.delete(file);
            // if the source path is a sub folder, then delete it
            if (sourcePath != null && folderPath != null) {
               Files.walkFileTree(FileSystems.getDefault().getPath(sourcePath), new DirectoryDeleter());
            }
             */
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (ftpClient.isConnected()) {
            try {
               ftpClient.logout();
               ftpClient.disconnect();
            } catch (Exception ftpe) {
               ftpe.printStackTrace();
            }
         }
      }
   }

   @Override
   public void fileModified(Path path) {
      if (!ignoreFile(path.getFileName())) {
         //logger.debug("file modify event for file: " + path.toString());
      }
   }

   @Override
   public void fileDeleted(Path path) {
      if (!ignoreFile(path.getFileName())) {
         //logger.debug("file delete event for file: " + path.toString());
      }
   }

   @Override
   public void directoryCreated(Path path) {
      if (!ignoreFile(path.getFileName())) {
         //logger.debug("directory create event for file: " + path.toString());
      }
   }

   @Override
   public void directoryDeleted(Path path) {
      if (!ignoreFile(path)) {
         //logger.debug("directory delete event for file: " + path.toString());
      }
   }

   private class DirectoryDeleter extends SimpleFileVisitor<Path> {

      @Override
      public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
         //we only want to delete directories, not files, as there should be no files in the directories at
         logger.info("Unexpected file found on clean up: " + file.toString());
         return FileVisitResult.SKIP_SUBTREE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
         if (e == null) {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
         } else {
            // directory iteration failed
            throw e;
         }
      }
   }
}
