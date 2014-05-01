package com.setantamedia.fulcrum.common;

import java.util.HashMap;

/**
 *
 * @author Colin Manning
 */
public class ExternalProcessResult {

    public final static int RETURN_STATUS_DEFAULT = -1;
    public final static int RETURN_STATUS_OK = 0;
    private int returnCode = RETURN_STATUS_DEFAULT;
    private String errorText = "";
    private String outputText = "";
    private HashMap<String, Object> interestingObjects = new HashMap<>();

    public ExternalProcessResult() {
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public String getOutputText() {
        return outputText;
    }

    public void setOutputText(String outputText) {
        this.outputText = outputText;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public void setInterestingObjects(HashMap<String, Object> interestingObjects) {
        this.interestingObjects = interestingObjects;
    }

    public HashMap<String, Object> getInterestingObjects() {
        return interestingObjects;
    }

    public void addInterestingObject(String key, Object object) {
        interestingObjects.put(key, object);
    }

    public Object getInterestingObject(String key) {
        return interestingObjects.get(key);
    }
}
