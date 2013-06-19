package com.setantamedia.fulcrum.common;

import com.setantamedia.fulcrum.locationmonitor.FolderWatcher;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * This class provides a mechanism to run external processes (i.e. native operation system programs, scripts etc. In
 * general, this an instance of this class should only be instantiated for one external process call.
 *
 * In particular, the class handles gobbling up input and output stream data. Current implementation simple sends this
 * down a black hole, so data is lost, user gets only the process exit code back.
 *
 * @author colinmanning
 *
 */
public class ExternalProcessRunner {

   private static Logger logger = Logger.getLogger(FolderWatcher.class);
   private String outputStuff = null;
   private String errorStuff = null;
   private Process process = null;
   private String cmdString = null;

   public ExternalProcessRunner() {
   }

   public ExternalProcessRunner(boolean doLogging) {
   }

   public Integer runProcess(List<String> cmd) {
      return runProcess(cmd, false, false);
   }

   public Integer runProcess(List<String> cmd, boolean cacheErrors) {
      return runProcess(cmd, cacheErrors, true);
   }

   public Integer runProcess(List<String> cmd, boolean cacheErrors, boolean cacheOutput) {
      return runProcess(cmd, cacheErrors, cacheOutput, true);
   }

   public Integer runProcess(List<String> cmd, boolean cacheErrors, boolean cacheOutput, boolean waitForProcess) {
      Integer result = null;
      try {
         errorStuff = "";
         outputStuff = "";
         if (process != null) {
            // this should never happen but we should be sure we kill any existing process and log it
            logger.error("Attempt to run external process while existing process has not stopped - for command: '" + cmdString + "'");
            terminate();
         }

         if (Utilities.isWindows()) {
            cmdString = "\"" + cmd.get(0) + "\"";
            for (int i = 1; i < cmd.size(); i++) {
               cmdString += " " + cmd.get(i);
            }
            cmd = new ArrayList<>();
            // need to run inside a cmd shell
            // need to handle double quotes and spaces, so we need quotes around the full path, and also quote the program path in case of spacea
            cmd.add("cmd");
            cmd.add("/c");
            cmd.add("\"" + cmdString + "\"");
         } else {
            // here it is just for debugging
            cmdString = cmd.get(0);
            for (int i = 1; i < cmd.size(); i++) {
               cmdString += " " + cmd.get(i);
            }
         }

         ProcessBuilder processBuilder = new ProcessBuilder(cmd);
         process = processBuilder.start();
         if (!cacheOutput) {
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT");
            outputGobbler.start();
         }

         if (!cacheErrors) {
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");
            errorGobbler.start();
         }

         if (cacheOutput) {
            StringBuilder sb = new StringBuilder();
            try {
               InputStreamReader isr = new InputStreamReader(process.getInputStream());
               BufferedReader br = new BufferedReader(isr);
               int c = 0;
               while ((c = br.read()) != -1) {
                  sb.append((char) c);
               }
            } catch (IOException ioe) {
               ioe.printStackTrace();
            } finally {
               outputStuff = sb.toString();
            }
         }

         if (cacheErrors) {
            StringBuilder sb = new StringBuilder();
            try {
               InputStreamReader isr = new InputStreamReader(process.getErrorStream());
               BufferedReader br = new BufferedReader(isr);
               int c = 0;
               while ((c = br.read()) != -1) {
                  sb.append((char) c);
               }
            } catch (IOException ioe) {
               ioe.printStackTrace();
            } finally {
               errorStuff = sb.toString();
            }
         }
         if (waitForProcess) {
            result = process.waitFor();
            process = null;
         }
      } catch (IOException | InterruptedException e) {
         e.printStackTrace();
      }
      return result;
   }

   /**
    * Returns any text written to the output stream by the process
    *
    * @return Text sent to the output stream
    */
   public String getOutputStuff() {
      return outputStuff;
   }

   /**
    * Returns any text written to the error stream by the process
    *
    * @return Text sent to the error stream
    */
   public String getErrorStuff() {
      return errorStuff;
   }

   /**
    * Terminate the external process if it is still running
    */
   public void terminate() {
      if (process != null) {
         try {
            // check for exit code, will throw exception if not already terminated
            int status = process.exitValue();
         } catch (IllegalThreadStateException e) {
            logger.info("Destroying external process: '" + cmdString + "'");
            process.destroy();
         }
      }
   }

   public String getCommandString() {
      return cmdString;
   }
}
