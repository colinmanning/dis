package com.setantamedia.dis.workflow.locations;

import com.setantamedia.fulcrum.locationmonitor.FileProcessor;
import com.setantamedia.fulcrum.ws.types.Category;
import com.setantamedia.fulcrum.ws.types.User;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import org.apache.log4j.Logger;

public class OfficeDocumentFileProcessor extends FileProcessor {

    private final static Logger logger = Logger.getLogger(OfficeDocumentFileProcessor.class);
    public final static String PARAM_CATEGORY_ROOT = "categoryRoot";
    public final static String PARAM_SUBFOLDER_CATEGORIES = "subFolderCategories";
    public final static String PARAM_UPLOAD_PROFILE = "uploadProfile";
    public final static String DEFAULT_CATEGORY = "$Categories";
    public final static String DEFAULT_CATEGORY_DELIMITER = "+";
    public final static String DEFAULT_UPLOAD_PROFILE = "Standard";
    private String categoryRoot = DEFAULT_CATEGORY;
    private String categoryDelimiter = DEFAULT_CATEGORY_DELIMITER;
    private String uploadProfile = DEFAULT_UPLOAD_PROFILE;
    private Boolean subFolderCategories = true;

    /**
     * Initialize things, in particular process the parameters from the config
     * file. If the categoryRoot does not exist in the DAM, it will be created.
     * Note the "Upload Profile" is the term we use for rules that define how a
     * file is uploaded - f.r example, the name of a Cumulus "Asset Handling
     * Set"
     */
    @Override
    public void init() {
        super.init();
        try {
            if (params.get(PARAM_CATEGORY_ROOT) != null) {
                categoryRoot = params.get(PARAM_CATEGORY_ROOT);
            }
            if (params.get(PARAM_SUBFOLDER_CATEGORIES) != null) {
                subFolderCategories = Boolean.valueOf(params.get(PARAM_SUBFOLDER_CATEGORIES));
            }
            if (params.get(PARAM_UPLOAD_PROFILE) != null) {
                uploadProfile = params.get(PARAM_UPLOAD_PROFILE);
            }
            dam.manager.createCategory(dam.getConnection(damConnectionName), null, categoryRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fileCreated(Path file) {
        try {
            /*
             * Ignore registered ignore files, and files in the temp folder
             */
            if (ignoreFile(file)) {
                return;
            }
            if (tmpFolder.compareTo(file.getParent()) == 0) {
                return;
            }

            String name = file.getFileName().toString();

            /*
             * Split up the file name, and map to a category in the DAM. The category will be created if necessary
             */
            String fileName = name;
            Category fileCategory = null;
            String[] bits = name.split(categoryDelimiter);
            String categoryPath = categoryRoot;
            if (bits.length > 1) {
                fileName = bits[bits.length - 1];
                for (int i = 0; i < (bits.length - 1); i++) {
                    categoryPath += ":" + bits[i];
                }
            }
            fileCategory = dam.manager.createCategory(dam.getConnection(damConnectionName), null, categoryPath);

            /*
             * Copy the file to the location tmp folder, and upload to the DAM
             */
            Path tmpFile = tmpFolder.resolve(fileName);
            Files.move(file, tmpFile);

            // we could also pass in some metadata fields if we like here
            User user = null;
            HashMap<String, String> fields = new HashMap<>();
            String damId = dam.manager.uploadFile(dam.getConnection(damConnectionName), user, tmpFile, fileName, uploadProfile, fields);

            /*
             * If we get a valid id from the DAM, then link the file to the category created form the file name pattern
             */
            if (damId != null && !"-1".equals(damId)) {
                dam.manager.addRecordToCategory(dam.getConnection(damConnectionName), String.valueOf(fileCategory.getId()), damId);
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
        // do nothing
    }

    @Override
    public void directoryCreated(Path directory) {
        if (ignoreFile(directory)) {
            return;
        }
        if (tmpFolder.compareTo(directory.getParent()) == 0) {
            return;
        }

        try {
            String name = directory.getFileName().toString();
            /*
             * If sub folders are created in the location, they are treated as category levels if "subFolderCategories" is set to true.
             */
            if (subFolderCategories) {
                String[] bits = name.split("/");
                String categoryPath = categoryRoot;
                if (bits.length > 1) {
                    for (int i = 0; i < (bits.length - 1); i++) {
                        if ("tmp".equals(bits[i])) {
                            continue;
                        }
                        categoryPath += ":" + bits[i];
                    }
                    dam.manager.createCategory(dam.getConnection(damConnectionName), null, categoryPath);
                }
            }
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
