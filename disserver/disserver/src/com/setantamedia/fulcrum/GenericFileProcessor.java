package com.setantamedia.fulcrum;

import com.setantamedia.fulcrum.common.ExternalProcessRunner;
import com.setantamedia.fulcrum.locationmonitor.FileProcessor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class GenericFileProcessor extends FileProcessor {

    private static Logger logger = Logger.getLogger(GenericFileProcessor.class);
    public final static String PARAM_CREATED_SCRIPT = "createdScript";
    public final static String PARAM_MODIFIED_SCRIPT = "modifiedScript";
    public final static String PARAM_DELETED_SCRIPT = "deletedScript";
    private String createdScript = null;
    private String modifiedScript = null;
    private String deletedScript = null;

    public GenericFileProcessor() {
        super();
    }

    @Override
    public void init() {
        super.init();
        if (params.get(PARAM_CREATED_SCRIPT) != null) {
            createdScript = params.get(PARAM_CREATED_SCRIPT);
        }
        if (params.get(PARAM_MODIFIED_SCRIPT) != null) {
            modifiedScript = params.get(PARAM_MODIFIED_SCRIPT);
        }
        if (params.get(PARAM_DELETED_SCRIPT) != null) {
            deletedScript = params.get(PARAM_DELETED_SCRIPT);
        }
        logger.info("Generic File Processor initialised");
    }

    @Override
    public void fileCreated(Path file) {
        if (createdScript == null) {
            return;
        }
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(createdScript);
            cmd.add(file.getParent().toString());
            cmd.add(file.getFileName().toString());
            ExternalProcessRunner runner = new ExternalProcessRunner();
            // run without waiting and no processing of input
            //TODO - make these settings parameterisable
            runner.runProcess(cmd, true, true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fileModified(Path file) {
        if (modifiedScript == null) {
            return;
        }
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(modifiedScript);
            cmd.add(file.getParent().toString());
            cmd.add(file.getFileName().toString());
            ExternalProcessRunner runner = new ExternalProcessRunner();
            // run without waiting and no processing of input
            //TODO - make these settings parameterisable
            runner.runProcess(cmd, true, true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fileDeleted(Path file) {
        if (deletedScript == null) {
            return;
        }
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(deletedScript);
            cmd.add(file.getParent().toString());
            cmd.add(file.getFileName().toString());
            ExternalProcessRunner runner = new ExternalProcessRunner();
            // run without waiting and no processing of input
            //TODO - make these settings parameterisable
            runner.runProcess(cmd, true, true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void directoryModified(Path directory) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void terminate() {
    }
}
