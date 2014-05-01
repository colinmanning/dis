package com.setantamedia.dis.workflow.locations;

import com.setantamedia.fulcrum.locationmonitor.FileProcessor;
import java.nio.file.Path;

/**
 *
 * @author Colin Manning
 */
public class PrintFileProcessor extends FileProcessor {

    @Override
    public void terminate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void directoryModified(Path directory) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
