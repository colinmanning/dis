package com.setantamedia.fulcrum.common;

import java.nio.file.Path;

public class Location {

    protected String name = null;
    protected String accessCode = null;
    protected Path folder = null;

    public Location() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public Path getFolder() {
        return folder;
    }

    public void setFolder(Path folder) {
        this.folder = folder;
    }
}
