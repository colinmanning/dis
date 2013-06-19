package com.setantamedia.fulcrum.locationmonitor;

import java.io.IOException;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class FolderWatcher implements Runnable {

   private static Logger logger = Logger.getLogger(FolderWatcher.class);
   public final static int DEFAULT_MAX_WORKER_THREADS = 4;
   private WatchService watcher;
   private Map<WatchKey, Path> keys;
   private boolean monitorSubFolders;
   private Path folder = null;
   private LocationListener locationListener = null;
   private boolean started = false;
   private boolean ready = false;

   @SuppressWarnings("unchecked")
   static <T> WatchEvent<T> cast(WatchEvent<?> event) {
      return (WatchEvent<T>) event;
   }

   public FolderWatcher() {
      super();
      ready = false;
      started = false;
   }

   @Override
   public void run() {
      if (ready) {
         processEvents();
      } else {
         logger.error("Attempt to run folder watcher for folder: " + folder.toString() + " before initialisation is complete - exiting");
      }
   }

   private void register(Path dir) throws IOException {
      WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
      Path prev = keys.get(key);
      if (prev == null) {
         logger.info("Watching folder " + dir);
      } else {
         if (!dir.equals(prev)) {
            logger.info("Updating folder watcher " + prev + " -> " + dir);
         }
      }
      keys.put(key, dir);
   }

   private void registerSubFolders(final Path start) throws IOException {
      Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

         @Override
         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            register(dir);
            return FileVisitResult.CONTINUE;
         }
      });
   }

   public void init() {
      try {
         this.watcher = FileSystems.getDefault().newWatchService();
         this.keys = new HashMap<>();

         if (this.monitorSubFolders) {
            logger.info("Monitoring sub folders in " + folder);
            registerSubFolders(folder);
         } else {
            logger.info("Monitoring folder " + folder);
            register(folder);
         }
         ready = true;
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Process all events for keys queued to the watcher
    */
   public void processEvents() {
      started = true;
      logger.info("Started monitoring folder: " + folder.toString());
      while (started) {
         // wait for key to be signalled
         WatchKey key = null;
         try {
            key = watcher.take();
         } catch (ClosedWatchServiceException | InterruptedException ce) {
            started = false;
         }

         if (!started) {
            // termination has been requested, or some other interrupt has occured
            continue;
         }
         Path dir = keys.get(key);
         if (dir == null) {
            System.err.println("WatchKey not recognized!!");
            continue;
         }

         for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind kind = event.kind();

            // TODO - handle possible missed events
            if (kind == OVERFLOW) {
               logger.info("Some file activity may have een missed for folder: " + folder);
               continue;
            }

            // Context for directory entry event is the file name of entry
            WatchEvent<Path> ev = cast(event);
            Path name = ev.context();
            Path child = dir.resolve(name);

            if (kind == ENTRY_CREATE) {
               if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                  locationListener.directoryCreated(child);
                  if (monitorSubFolders) {
                     try {
                        registerSubFolders(child);
                     } catch (IOException ioe) {
                        ioe.printStackTrace();
                     }
                  }
               } else {
                   locationListener.fileCreated(child);
               }
            } else if (kind == ENTRY_MODIFY) {
               if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                   locationListener.directoryModified(child);

               } else {
                  locationListener.fileModified(child);

               }
            } else if (kind == ENTRY_DELETE) {
               if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                  locationListener.directoryDeleted(child);

               } else {
                  locationListener.fileDeleted(child);
               }
            }
         }

         // reset key and remove from set if directory no longer accessible
         boolean valid = key.reset();
         if (!valid) {
            keys.remove(key);

            // all directories are inaccessible
            if (keys.isEmpty()) {
               break;
            }
         }
      }
   }

   public void shutdown() {
      try {
         logger.info("Shutting down file watch for folder; " + folder.toString());
         watcher.close();
         locationListener.terminate();
      } catch (Exception e) {
         e.printStackTrace();
         logger.error("Problem shutting down file watch for folder; " + folder.toString());
      } finally {
         started = false;
      }
   }

   public Path getFolder() {
      return folder;
   }

   public void setFolder(Path folder) {
      this.folder = folder;
   }

   public boolean isMonitorSubFolders() {
      return monitorSubFolders;
   }

   public void setMonitorSubFolders(boolean monitorSubFolders) {
      this.monitorSubFolders = monitorSubFolders;
   }

   public static void main(String[] args) throws IOException {
      boolean subFolders = false;
      int dirArg = 0;
      if (args[0].equals("-r")) {
         subFolders = true;
         dirArg++;
      }

      FolderWatcher w = new FolderWatcher();
      w.setFolder(Paths.get(args[dirArg]));
      w.setMonitorSubFolders(subFolders);
      w.init();
      w.processEvents();
   }

   public LocationListener getLocationListener() {
      return locationListener;
   }

   public void setLocationListener(LocationListener locationListener) {
      this.locationListener = locationListener;
   }
}
