package com.setantamedia.dis.workflow.locations;

import com.setantamedia.fulcrum.common.ExternalProcessRunner;
import com.setantamedia.fulcrum.locationmonitor.FileProcessor;
import com.setantamedia.fulcrum.ws.types.Category;
import com.setantamedia.fulcrum.ws.types.User;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * A file processor that will process video files, and convert them to FLV format, and then upload the FLV file to the
 * DAM system. The processor uses the ffmpeg utility, however the path and parameters used for conversion are all passed
 * in as parameters, defined in the DIS config file. For convenience, we link the uploaded files to a category in the
 * DAM system, the category being specified in the DIS config file entry also.
 *
 * @author Colin Manning
 *
 */
public class VideoFileInputProcessor extends FileProcessor {

   private final static Logger logger = Logger.getLogger(VideoFileInputProcessor.class);
   public final static String PARAM_FFMPEG_PATH = "ffmpegPath";
   public final static String PARAM_FFMPEG_PARAMS = "ffmpegParams";
   public final static String PARAM_CATEGORY_ROOT = "categoryRoot";
   public final static String PARAM_UPLOAD_PROFILE = "uploadProfile";
   public final static String DEFAULT_CATEGORY = "$Categories";
   public final static String DEFAULT_UPLOAD_PROFILE = "Standard";
   public final static String ffmpegParamsRegex = "\\$\\{file\\}";
   private String categoryRoot = DEFAULT_CATEGORY;
   private String ffmpegPath = null;
   private String uploadProfile = DEFAULT_UPLOAD_PROFILE;
   private Category fileCategory = null;

   /**
    * Initialize things, in particular process the parameters from the config file. If the categoryRoot does not exist
    * in the DAM, it will be created. Note the "Upload Profile" is the term we use for rules that define how a file is
    * uploaded - f.r example, the name of a Cumulus "Asset Handling Set"
    */
   @Override
   public void init() {
      super.init();
      try {
      if (params.get(PARAM_CATEGORY_ROOT) != null) {
         categoryRoot = params.get(PARAM_CATEGORY_ROOT);
      }
      if (params.get(PARAM_UPLOAD_PROFILE) != null) {
         uploadProfile = params.get(PARAM_UPLOAD_PROFILE);
      }
      if (params.get(PARAM_FFMPEG_PATH) != null) {
         ffmpegPath = params.get(PARAM_FFMPEG_PATH);
      }
      fileCategory = dam.manager.createCategory(dam.getConnection(damConnectionName), null, categoryRoot);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /*
    * Convert the video file, and copy to the tmp folder for uploading TODO - consider using exiftool to get metadata
    * form original file to send with converted file when uploading - save as JSON metadata file
    */
   private void doConvert(Path file) {
      try {
         String name = file.getFileName().toString();
         String tmpFileName = name + ".flv";
         int ep = name.lastIndexOf(".");
         if (ep > 0) {
            tmpFileName = name.substring(0, ep) + ".flv";
         }
         Path tmpFile = tmpFolder.resolve(tmpFileName);

         //String runParams = ffmpegParams.replaceAll(ffmpegParamsRegex, "\""+file.getAbsolutePath()+"\"");
         List<String> cmd = new ArrayList<>();
         cmd.add(ffmpegPath);
         cmd.add("-i");
         cmd.add(file.toString());
         cmd.add("-f");
         cmd.add("flv");
         cmd.add("-b");
         cmd.add("200000");
         cmd.add(tmpFile.toString());

         ExternalProcessRunner runner = new ExternalProcessRunner();

         Integer status = runner.runProcess(cmd, true, true, true);
         if (status == 0) {
            String damId = doUpload(tmpFile);
            if (!"-1".equals(damId)) {
               logger.info("Converted and uploaded file:" + file.toString() + " to " + tmpFile.toString() + " with new DAM id: " + damId);
               Files.delete(file);
            } else {
               logger.error("failed to upload file: " + tmpFile + " to DAM");
            }
            Files.delete(tmpFile);
         } else {
            logger.error("Attempt to convert file: " + file.toString() + " failed with error code: " + status);
         }
      } catch (Exception e) {
         e.printStackTrace();
         logger.error("Problem converting file: " + file.toString());
      }
   }

   /*
    * Upload a converted file
    */
   private String doUpload(Path file) {
      String result = "-1";
      try {
         User user = null;
         HashMap<String, String> fields = new HashMap<>();
         String damId = dam.manager.uploadFile(dam.getConnection(damConnectionName), user, file, file.getFileName().toString(), uploadProfile, fields);

         /*
          * If we get a valid id from the DAM, then link the file to the category created form the file name pattern
          */
         if (damId != null && !"-1".equals(damId)) {
            dam.manager.addRecordToCategory(dam.getConnection(damConnectionName), String.valueOf(fileCategory.getId()), damId);
            result = damId;
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
            // ignore directories
            return;
         }
         if (tmpFolder.compareTo(file.getParent()) == 0) {
            // Files to be uploaded
            doUpload(file);
         } else {
            doConvert(file);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void fileModified(Path file) {
      // NOOP
   }

   @Override
   public void fileDeleted(Path file) {
      // NOOP
   }

   @Override
   public void directoryModified(Path directory) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void terminate() {

   }
}
