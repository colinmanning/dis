package com.setantamedia.fulcrum.workflow;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Colin Manning
 */
public abstract class Workflow implements Runnable {

   public final static int RETURN_STATUS_UNKNOWN = -1;
   public final static int RETURN_STATUS_OK = 0;
   public final static int RETURN_STATUS_FAILED = 1;
   protected ArrayList<Path> inputFiles = null;
   protected HashMap<String, Object> inputParams = null;
   protected HashMap<String, Object> outputParams = null;
   protected int runStatus = RETURN_STATUS_UNKNOWN;
   private String name = null;

   public abstract void init();

   public abstract int execute();

      public ArrayList<Path> getInputFiles() {
      return inputFiles;
   }

   public void setInputFiles(ArrayList<Path> inputFiles) {
      this.inputFiles = inputFiles;
   }

   public HashMap<String, Object> getInputParams() {
      return inputParams;
   }

   public void setInputParams(HashMap<String, Object> inputParams) {
      this.inputParams = inputParams;
   }

   public int getRunStatus() {
      return runStatus;
   }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
