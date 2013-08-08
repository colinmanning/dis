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
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class MoveFileProcessor extends FileProcessor {

   private final static Logger logger = Logger.getLogger(FtpFileProcessor.class);
   public final static String PARAM_TRANSFER_DONE_FIELD = "transferdonefield";
   public final static String PARAM_EMBED_IPTC = "embed-iptc";
   private Path destinationFolder = null;
   private String transferDoneField = null;
   private FileSystem fs = FileSystems.getDefault();

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
         if (params.get(FileServlet.PARAMETER_DESTINATION_FOLDER) != null) {
            destinationFolder = fs.getPath(params.get(FileServlet.PARAMETER_DESTINATION_FOLDER));
         }
         if (params.get(PARAM_TRANSFER_DONE_FIELD) != null) {
            transferDoneField = params.get(PARAM_TRANSFER_DONE_FIELD);
         }
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
            Path parentPath = file.getParent();
            List<String> lines = Files.readAllLines(file, Charset.forName("UTF-8"));
            // Ignore the header
            boolean ready = false;
            Path destinationFolderPath = destinationFolder;
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
                        destinationFolderPath = destinationFolder.resolve(folderPath);
                        if (!Files.exists(destinationFolderPath)) {
                           Files.createDirectories(destinationFolderPath);
                        }
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
                  Path destinationFile = destinationFolderPath.resolve(fileName);
                  if (Files.exists(destinationFile)) {
                     Files.delete(destinationFile);
                  }
                  try (InputStream is = Files.newInputStream(transferFile)) {
                     Files.move(transferFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
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
            //Files.delete(file);
            // if the source path is a sub folder, then delete it
            if (sourcePath != null && folderPath != null) {
               Files.walkFileTree(FileSystems.getDefault().getPath(sourcePath), new MoveFileProcessor.DirectoryDeleter());
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
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
