package com.setantamedia.fulcrum.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStreamer {

    private String name = null;
    private String assetAction = null;
    private InputStream stream = null;
    private Path file = null;
    private Boolean accessForbidden = false;
    private String guid = null;
    private Path workDir = null;
    private String mimeType = null;

    public FileStreamer() {
    }

    public void closeStream() throws IOException {
        if (stream != null) {
            stream.close();
        }
    }

    public Path getWorkDir() {
        return workDir;
    }

    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    public String getAssetAction() {
        return assetAction;
    }

    public void setAssetAction(String assetAction) {
        this.assetAction = assetAction;
    }

    public Path getFile() {
        return file;
    }

    public void setFile(Path file) {
        this.file = file;
    }

    public Boolean getAccessForbidden() {
        return accessForbidden;
    }

    public void setAccessForbidden(Boolean accessForbidden) {
        this.accessForbidden = accessForbidden;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Boolean removeWorkDir() {
        Boolean result = false;
        if (workDir == null) {
            return result;
        }
        try {
            // first remove any files - assumes work directories have no sub directories - in fact it should be just one file
            // we could use the DirectoryContentsDeleter and walkTreePath if we want to be more sophisticated
            // but this is nice and simple
            for (File fileToDelete : workDir.toFile().listFiles()) {
                fileToDelete.delete();
            }
            Files.delete(workDir);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
   
}
