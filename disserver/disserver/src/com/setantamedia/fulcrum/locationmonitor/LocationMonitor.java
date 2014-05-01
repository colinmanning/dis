package com.setantamedia.fulcrum.locationmonitor;

import com.setantamedia.fulcrum.common.Location;
import java.io.IOException;
import java.nio.file.*;
import java.util.Calendar;
import java.util.HashMap;
import org.apache.log4j.Logger;

public class LocationMonitor extends Location {

   private static Logger logger = Logger.getLogger(LocationMonitor.class);
   public final static int DEFAULT_STABLE_TEST_TIMEOUT = 100; // 100 milliseconds
   public final static int DEFAULT_MAX_RETRIES = 3;
   public final static int DEFAULT_WAIT = 5000; // every 5 seconds
   public final static String TMP_FOLDER = "tmp";
   public Path tmpFolder = null;
   private LocationListener locationListener = null;
   private boolean started = false;
   private boolean monitorSubFolders = false;
   private boolean initialScan = false;
   protected HashMap<String, LocationFile> files = new HashMap<>();
   protected HashMap<String, LocationDirectory> directories = new HashMap<>();
   protected LocationDirectory me = null;
   private FolderWatcher folderWatcher = null;
   private long lastScan = -1L;
   private int stableTestTimout = DEFAULT_STABLE_TEST_TIMEOUT;
   private boolean ready = false;
   private FileSystem fs = FileSystems.getDefault();

   public LocationMonitor() {
      try {
         ready = false;
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void init() {
      try {
         me = new LocationDirectory();
         me.setFolder(folder);
         tmpFolder = folder.resolve(fs.getPath(TMP_FOLDER));
         ready = true;
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public void start() {
      if (!ready) {
         init();
      }
      try {
         if (initialScan) {
            FolderScanner scanner = new FolderScanner(me);
            new Thread(scanner).start();
         }
         folderWatcher = new FolderWatcher();
         folderWatcher.setFolder(folder);
         folderWatcher.setMonitorSubFolders(monitorSubFolders);
         folderWatcher.setLocationListener(locationListener);
         folderWatcher.init();
         new Thread(folderWatcher).start();
         started = true;
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void stop() {
      if (!started) {
         return;
      }
      try {
         folderWatcher.shutdown();
         started = false;
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public long getLastScan() {
      return lastScan;
   }

   public void setLastScan(long lastScan) {
      this.lastScan = (lastScan / 1000) * 1000;// flatten milliseconds, as Linux uses seconds for file modified time
   }

   public LocationListener getLocationListener() {
      return locationListener;
   }

   public void setLocationListener(LocationListener locationListener) throws Exception {
      this.locationListener = locationListener;
   }

   public boolean isMonitorSubFolders() {
      return monitorSubFolders;
   }

   public void setMonitorSubFolders(boolean monitorSubFolders) throws Exception {
      this.monitorSubFolders = monitorSubFolders;
   }

   public HashMap<String, LocationDirectory> getDirectories() {
      return directories;
   }

   public void setDirectories(HashMap<String, LocationDirectory> directories) {
      this.directories = directories;
   }

   public HashMap<String, LocationFile> getFiles() {
      return files;
   }

   public void setFiles(HashMap<String, LocationFile> files) {
      this.files = files;
   }

   public boolean isInitialScan() {
      return initialScan;
   }

   public void setInitialScan(boolean initialScan) {
      this.initialScan = initialScan;
   }

   public boolean isStable(Path file) {
      boolean result = false;
      if (file == null) {
         return result;
      }
      try {
         long l = Files.size(file);
         if (l == 0L) {
            return result;
         }
         // wait a bit and see if still growing
         Thread.sleep(stableTestTimout);
         long l1 = Files.size(file);
         if (l1 == l) {
            // ok - not growing, so let us assume it is stable
            result = true;
         }
      } catch (IOException | InterruptedException e) {
         e.printStackTrace();
      }
      return result;
   }

   private class FolderScanner implements Runnable {

      private LocationDirectory dir = null;

      public FolderScanner(LocationDirectory dir) {
         this.dir = dir;
      }

      @Override
      public void run() {
         doScan(dir);
      }

      /*
       * Scan the location without processing any files
       *
       */

      private void doScan(LocationDirectory locDir) {
         long scanTime = Calendar.getInstance().getTimeInMillis();
         try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(locDir.getFolder())) {
               for (Path file : stream) {
                  if (Files.isDirectory(file)) {
                     if (tmpFolder.compareTo(file) == 0) {
                        continue;
                     }
                     locationListener.directoryCreated(file);
                     LocationDirectory ld = new LocationDirectory();
                     ld.setFolder(file);
                     ld.setParent(locDir);
                     ld.setLastScan(scanTime);
                     directories.put(file.getFileName().toString(), ld);
                     if (monitorSubFolders) {
                        doScan(ld);
                     }
                  } else {
                     if (isStable(file)) {
                        locationListener.fileCreated(file);
                        LocationFile lf = new LocationFile();
                        lf.setMe(file);
                        lf.setParent(locDir);
                        files.put(file.getFileName().toString(), lf);
                     }
                  }
                  System.out.println(file.getFileName());
               }
            } catch (IOException | DirectoryIteratorException ioe) {
               // IOException can never be thrown by the iteration.
               // In this snippet, it can only be thrown by newDirectoryStream.
               ioe.printStackTrace();
               logger.error("Problem listing directory contents: " + ioe.getMessage());
            }
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            setLastScan(scanTime);
         }
      }
   }
}
