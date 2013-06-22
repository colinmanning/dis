package damcumulusapi;

import com.canto.cumulus.*;
import com.canto.cumulus.constants.CombineMode;
import com.canto.cumulus.constants.FindFlag;
import com.canto.cumulus.exceptions.*;
import com.canto.cumulus.fieldvalue.AssetReference;
import com.canto.cumulus.fieldvalue.*;
import com.canto.cumulus.usermanagement.AuthenticationManager;
import com.canto.cumulus.usermanagement.FieldValues;
import com.canto.cumulus.usermanagement.UserFieldDefinition;
import com.canto.cumulus.utils.LanguageManager;
import com.setantamedia.fulcrum.DamManager;
import com.setantamedia.fulcrum.DamManagerNotImplementedException;
import com.setantamedia.fulcrum.common.FieldValue;
import com.setantamedia.fulcrum.common.*;
import com.setantamedia.fulcrum.config.Field;
import com.setantamedia.fulcrum.config.View;
import com.setantamedia.fulcrum.models.core.Person;
import com.setantamedia.fulcrum.ws.types.Category;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.apache.log4j.Logger;

public class CumulusHelper {

    private static Logger logger = Logger.getLogger(CumulusHelper.class);

    public static enum QueryTypes {

        New, Narrow, Broaden, Page
    }
    private static int MAX_ADD_CATEGORY_RETRIES = 3;
    // User catalog fields
    public final static GUID UID_USER_FIRST_NAME = new GUID("{7c437141-daa4-11d6-b6be-0050baeba6c7}");
    public final static GUID UID_USER_LAST_NAME = new GUID("{7c437143-daa4-11d6-b6be-0050baeba6c7}");
    public final static GUID UID_USER_EMAIL = new GUID("{7c43714f-daa4-11d6-b6be-0050baeba6c7}");
    public final static GUID UID_USER_LOGIN_ACTIVE = new GUID("{1212109f-727a-48b1-a470-ed4269f06dc9}");
    public final static GUID UID_USER_GROUP = new GUID("{1212109f-727a-48b1-a470-ed4269f06dc9}");
    private Server cumulusServer = null;
    private HashMap<String, Catalog> catalogs = null;
    private HashMap<String, CumulusCollectionManager> collectionManagers = new HashMap<>();
    private HashMap<Connection, HashMap<String, DatabaseTable>> tableFields = new HashMap<>();
    private HashMap<Connection, HashMap<String, DatabaseField[]>> viewFields = new HashMap<>();
    private HashMap<Connection, DatabaseField[]> recordFields = new HashMap<>();
    private HashMap<Connection, HashMap<String, DatabaseField>> recordFieldsByName = new HashMap<>();
    private HashMap<Connection, HashMap<String, HashMap<String, String>>> previewFields = new HashMap<>();
    private HashMap<Connection, HashMap<String, HashMap<String, String>>> linkFields = new HashMap<>();
    private HashMap<Connection, HashMap<String, HashMap<String, String>>> referenceFields = new HashMap<>();
    private HashMap<String, View> views = new HashMap<>();
    private FileSystem fs = FileSystems.getDefault();
    private Path tmpDir = null;
    private String baseUrl = null;
    private String serverPrefix = "";

    public CumulusHelper() {
    }

    public void setTmpDir(Path tmpDir) {
        this.tmpDir = tmpDir;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setServerPrefix(String serverPrefix) {
        this.serverPrefix = serverPrefix;
    }

    public String getServerPrefix() {
        return serverPrefix;
    }

    public void setViews(HashMap<String, View> views) {
        this.views = views;
    }

    public Catalog getCatalog(Connection connection) throws Exception {
        Catalog result = catalogs.get(connection.getDatabase());
        if (result == null) {
            result = cumulusServer.openCatalog(cumulusServer.findCatalogID(connection.getDatabase()));
            catalogs.put(connection.getDatabase(), result);
        }
        return result;
    }

    public CumulusCollectionManager getCollectionManager(Connection connection) {
        return collectionManagers.get(connection.getName());
    }

    public CumulusCollectionManager getOrInitCollectionManager(Connection connection) {
        CumulusCollectionManager result = null;
        try {
            result = collectionManagers.get(connection.getName());
            if (result == null) {
                result = new CumulusCollectionManager();
                if (result.init(connection)) {
                    collectionManagers.put(connection.getName(), result);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return result;
    }

    public String getFieldGuid(Connection connection, String fieldName) {
        String result = null;
        try {
            result = recordFieldsByName.get(connection).get(fieldName).getGuid().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public FileStreamer downloadAsset(Connection connection, Integer id, Integer version, String assetAction) {
        FileStreamer result = null;
        CumulusCollectionManager collectionManager = null;
        RecordItemCollection collection = null;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            collection = (RecordItemCollection) collectionManager.borrowObjectToRead(RecordItemCollection.class);
            if (collection != null) {
                RecordItem recordItem = collectionManager.getRecordItemByID(collection, id);
                if (assetAction != null && !"".equals(assetAction)) {
                    String fileGuid = Utilities.generateGuid();
                    Path workDir = tmpDir.resolve(connection.getName() + "_" + recordItem.getID() + "_" + fileGuid);
                    while (Files.exists(workDir)) {
                        // avoid clashes by generating new guids till we avoid a clash - don't really expect clashes
                        workDir = tmpDir.resolve(connection.getName() + "_" + recordItem.getID() + "_" + fileGuid);
                    }
                    Files.createDirectories(workDir); // and if someone else grapped this one, it will fail here also
                    Asset destinationAsset = new Asset(recordItem.getCumulusSession(), workDir.toFile());
                    AssetCollection assetCollection = recordItem.doAssetAction(destinationAsset, assetAction);
                    if (assetCollection != null) {
                        result = new FileStreamer();
                        result.setWorkDir(workDir);
                        Iterator<Asset> it = assetCollection.iterator();
                        Asset downloadAsset = it.next();
                        while (it.hasNext()) {
                            downloadAsset = it.next();
                        }
                        if (downloadAsset != null) {
                            result.setName(recordItem.getStringValue(GUID.UID_REC_RECORD_NAME));
                            result.setGuid(fileGuid);
                            result.setAssetAction(assetAction);
                            result.setFile(fs.getPath(downloadAsset.getAsFile().getAbsolutePath()));
                            result.setStream(Files.newInputStream(result.getFile()));
                        }
                    }
                } else {
                    result = new FileStreamer();
                    // do not set the file reference in the FileStreamer - do not want to all a caller to get a handle on this
                    recordItem = collection.getRecordItemByID(id);
                    result.setName(recordItem.getStringValue(GUID.UID_REC_RECORD_NAME));
                    AssetReference assetReference = recordItem.getAssetReferenceValue(GUID.UID_REC_ASSET_REFERENCE);
                    result.setStream(assetReference.getAsset(true).openInputDataStream());
                }
            }
        } catch (ItemNotFoundException | CumulusException | FieldNotFoundException | IOException | UnresolvableAssetReferenceException e) {
            // Do nothing, caller can handle returned value
            logger.error("Problem getting asset from cumulus to download: '" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (collectionManager != null && collection != null) {
                collectionManager.returnWriteObject(collection);
            }
        }
        return result;
    }

    public Category createCategory(Connection connection, String path) {
        Category result = null;
        CumulusCollectionManager collectionManager;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            CategoryItem rootCategory;
            CategoryItemCollection collection = collectionManager.getAllCategoriesItemCollection();
            if (collection != null) {
                synchronized (collection) {
                    try {
                        rootCategory = collection.getCategoryTreeCatalogRootCategory();
                        CategoryItem categoryItem = rootCategory.createCategoryItems(path);
                        result = new Category();
                        result.setId(categoryItem.getID());
                        result.setName(categoryItem.getStringValue(GUID.UID_CAT_NAME));
                    } catch (PermissionDeniedException | CumulusException | FieldNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Category createSubCategory(Connection connection, Integer parentId, String name) {
        Category result = null;
        CategoryItemCollection collection;
        CategoryItem category;
        CumulusCollectionManager collectionManager;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            collection = (CategoryItemCollection) collectionManager.borrowObjectToWrite(CategoryItemCollection.class);
            if (collection != null) {
                synchronized (collection) {
                    try {
                        category = collection.getCategoryItemByID(parentId);
                        CategoryItem categoryItem = category.createCategoryItems(name);
                        result = new Category();
                        result.setId(categoryItem.getID());
                        result.setName(categoryItem.getStringValue(GUID.UID_CAT_NAME));
                    } catch (ItemNotFoundException | CumulusException | PermissionDeniedException | FieldNotFoundException e) {
                        // Do nothing, caller can handle returned value
                    } finally {
                        collectionManager.returnWriteObject(collection);
                    }
                }
            }
        } catch (Exception e) {
            // Do nothing, caller can handle returned value
        }
        return result;
    }

    public boolean incrementFieldValue(Connection connection, String id, String field, int amount) {
        boolean result = false;

        RecordItemCollection recordCollection = null;
        CumulusCollectionManager collectionManager = null;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            int recordId = new Integer(id);
            checkAllFieldsReady(connection);
            // top level record
            RecordItem record = null;
            try {
                record = collectionManager.getRecordItemById(recordId, true);
                DatabaseField fieldDef = recordFieldsByName.get(connection).get(field);
                GUID guid = new GUID(fieldDef.getGuid());
                FieldValue fieldValue = getFieldValue(record, guid, collectionManager.getRecordLayout(), Locale.getDefault());
                switch (fieldDef.getDataType()) {
                    case FieldTypeConstants.TypeInteger:
                        int iv = fieldValue.getIntegerValue();
                        iv += amount;
                        fieldValue.setIntegerValue(iv);
                        setFieldValue(record, new GUID(fieldDef.getGuid()), fieldValue, collectionManager.getRecordLayout());
                        record.save();
                        result = true;
                        break;
                    case FieldTypeConstants.TypeLong:
                        long lv = fieldValue.getLongValue();
                        lv += amount;
                        fieldValue.setLongValue(lv);
                        setFieldValue(record, new GUID(fieldDef.getGuid()), fieldValue, collectionManager.getRecordLayout());
                        record.save();
                        result = true;
                        break;
                }
            } catch (Exception e1) {
                logger.error("Problem incrementing value for field: '" + field + "'");
                System.out.println("[CumulusHelper.incrementFieldValue] Problem incrementing value for field: '" + field + "' - maybe it is not a number field");
                e1.printStackTrace();
            } finally {
                if (record != null && collectionManager != null) {
                    collectionManager.releaseReadRecordItem(record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (collectionManager != null && recordCollection != null) {
                collectionManager.returnWriteObject(recordCollection);
            }
        }
        return result;
    }

    public boolean decrementFieldValue(Connection connection, String id, String field, int amount) {
        boolean result = false;

        RecordItemCollection recordCollection = null;
        CumulusCollectionManager collectionManager = null;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            int recordId = new Integer(id);
            checkAllFieldsReady(connection);
            RecordItem record = null;
            try {
                record = collectionManager.getRecordItemById(recordId, true);
                DatabaseField fieldDef = recordFieldsByName.get(connection).get(field);
                GUID guid = new GUID(fieldDef.getGuid());
                FieldValue fieldValue = getFieldValue(record, guid, collectionManager.getRecordLayout(), Locale.getDefault());
                switch (fieldDef.getDataType()) {
                    case FieldTypeConstants.TypeInteger:
                        int iv = fieldValue.getIntegerValue();
                        iv -= amount;
                        fieldValue.setIntegerValue(iv);
                        setFieldValue(record, new GUID(fieldDef.getGuid()), fieldValue, collectionManager.getRecordLayout());
                        record.save();
                        result = true;
                        break;
                    case FieldTypeConstants.TypeLong:
                        long lv = fieldValue.getLongValue();
                        lv -= amount;
                        fieldValue.setLongValue(lv);
                        setFieldValue(record, new GUID(fieldDef.getGuid()), fieldValue, collectionManager.getRecordLayout());
                        record.save();
                        result = true;
                        break;
                }
            } catch (Exception e1) {
                logger.error("Problem incrementing value for field: '" + field + "'");
                System.out.println("[CumulusHelper.incrementFieldValue] Problem incrementing value for field: '" + field + "' - maybe it is not a number field");
                e1.printStackTrace();
            } finally {
                if (record != null && collectionManager != null) {
                    collectionManager.releaseReadRecordItem(record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (collectionManager != null && recordCollection != null) {
                collectionManager.returnWriteObject(recordCollection);
            }
        }
        return result;
    }

    public boolean updateRecord(Connection connection, DatabaseField rootFieldDef, String tablePath, String recordKey, String fileName, HashMap<String, String> fieldValues) {
        boolean result = false;

        RecordItemCollection recordCollection = null;
        ItemCollection tableCollection = null;
        CumulusCollectionManager collectionManager = null;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            int recordId = -1;
            Integer[] hostIds = null;
            String keybits[] = recordKey.split("_");
            if (keybits.length == 1) {
                recordId = new Integer(keybits[0]);
            } else if (keybits.length > 1) {
                hostIds = new Integer[keybits.length - 1];
                for (int i = 0; i < keybits.length - 1; i++) {
                    hostIds[i] = new Integer(keybits[i]);
                }
                recordId = new Integer(keybits[keybits.length - 1]);
            }
            if (recordId != -1) {
                checkAllFieldsReady(connection);
                if (hostIds == null) {
                    // top level record
                    RecordItem record = null;
                    String fieldName = null;
                    try {
                        record = collectionManager.getRecordItemById(recordId, true);
                        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
                            if (FieldUtilities.NULL_VALUE.equalsIgnoreCase(entry.getValue())) {
                                // do not try to set null values
                                continue;
                            }
                            fieldName = entry.getKey();
                            DatabaseField fieldDef = recordFieldsByName.get(connection).get(fieldName);
                            setFieldValue(record, new GUID(fieldDef.getGuid()), FieldUtilities.createFieldValue(fieldDef, entry.getValue()), collectionManager.getRecordLayout());
                        }
                        if (fileName != null && !"".equals(fileName)) {
                            record.setStringValue(GUID.UID_REC_RECORD_NAME, fileName);
                        }
                        record.save();
                        result = true;
                    } catch (Exception e1) {
                        logger.error("Problem setting value for field: '" + fieldName + "'");
                        System.out.println("[CumulusHelper.updateRecord] Problem setting value for field: '" + fieldName + "'");
                        e1.printStackTrace();
                    } finally {
                        if (record != null && collectionManager != null) {
                            collectionManager.releaseReadRecordItem(record);
                        }
                    }
                } else {
                    // table field inside record
                    RecordItem parentRecord = null;
                    Layout layout = collectionManager.getRecordLayout();
                    try {
                        Item tableRecord = null;
                        // TODO - this not really implemented or thought - need to set foreign key fields etc.
                        // parent is a table field, get the record first, then the field
                        // We must traverse the parent ids, assuming a root AssetRecord
                        parentRecord = collectionManager.getRecordItemById(hostIds[0], true);
                        String[] tablePathBits = tablePath.split("/");
                        if (rootFieldDef != null) {
                            DatabaseTable tableDef = rootFieldDef.getTableDefinition();
                            tableCollection = parentRecord.getTableValue(new GUID(tableDef.getGuid()));
                            layout = tableCollection.getLayout();
                            if (tablePathBits.length > 1) {
                                for (int i = 1; i < tablePathBits.length; i++) {
                                    tableRecord = tableCollection.getItemByID(hostIds[i]);
                                    tableDef = tableDef.getColumn(tablePathBits[i]).getTableDefinition();
                                    tableCollection = tableRecord.getTableValue(new GUID(tableDef.getGuid()));
                                }
                            } else {
                                tableDef = rootFieldDef.getTableDefinition();
                            }

                            tableRecord = tableCollection.getItemByID(recordId);
                            for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
                                //logger.debug("COLIN>>>>> updating sub table field: " + entry.getKey());
                                setFieldValue(tableRecord, new GUID(tableDef.getColumn(entry.getKey()).getGuid()), FieldUtilities.createFieldValue(tableDef.getColumn(entry.getKey()), entry.getValue()), layout);
                            }
                            tableRecord.save();
                        } else {
                            parentRecord = collectionManager.getRecordItemById(recordId, true);
                            for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
                                DatabaseField fieldDef = recordFieldsByName.get(connection).get(entry.getKey());
                                setFieldValue(parentRecord, new GUID(fieldDef.getGuid()), FieldUtilities.createFieldValue(fieldDef, entry.getValue()), layout);
                            }
                            parentRecord.save();
                        }
                        result = true;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    } finally {
                        if (tableCollection != null) {
                            tableCollection.close();
                        }
                        if (parentRecord != null) {
                            collectionManager.releaseReadRecordItem(parentRecord);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (collectionManager != null && recordCollection != null) {
                collectionManager.returnWriteObject(recordCollection);
            }
        }
        return result;
    }

    public boolean addRecordToCategories(Connection connection, Integer[] categoryIds, Integer recordId) {
        boolean result = false;
        RecordItem recordItem = null;
        CumulusCollectionManager collectionManager = null;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            recordItem = collectionManager.getRecordItemById(recordId, true);
            if (recordItem != null) {
                CategoriesFieldValue cfv = recordItem.getCategoriesValue();
                for (Integer categoryId : categoryIds) {
                    cfv.addID(categoryId);
                }
                int retries = 0;
                while (retries < MAX_ADD_CATEGORY_RETRIES) {
                    try {
                        recordItem.setCategoriesValue(cfv);
                        recordItem.save();
                        retries = MAX_ADD_CATEGORY_RETRIES;
                    } catch (CumulusException e) {
                        e.printStackTrace();
                        retries++;
                    }
                }
            }
            result = true;
        } catch (Exception e) {
            // Do nothing, caller can handle returned value
        } finally {
            if (recordItem != null && collectionManager != null) {
                collectionManager.releaseReadRecordItem(recordItem);
            }
        }
        return result;
    }

    public boolean removeRecordFromCategory(Connection connection, Integer categoryId, Integer recordId) {
        boolean result = false;
        RecordItem recordItem;
        RecordItemCollection collection;
        CumulusCollectionManager collectionManager;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            collection = (RecordItemCollection) collectionManager.borrowObjectToWrite(RecordItemCollection.class);
            if (collection != null) {
                synchronized (collection) {
                    try {
                        recordItem = collection.getRecordItemByID(recordId);
                        if (recordItem != null) {
                            CategoriesFieldValue cfv = recordItem.getCategoriesValue();
                            cfv.removeID(categoryId);
                            recordItem.setCategoriesValue(cfv);
                            recordItem.save();
                        }
                    } catch (ItemNotFoundException | CumulusException | InvalidArgumentException | PermissionDeniedException e) {
                        e.printStackTrace();
                    } finally {
                        collectionManager.returnWriteObject(collection);
                    }
                }
            }
        } catch (Exception e) {
            // Do nothing, caller can handle returned value
        }
        return result;
    }

    /**
     * Assumes authenticated already
     *
     * @param connection
     * @param username
     * @return
     */
    public Person userSingleSignOn(Connection connection, String username) {
        Person result = null;
        CumulusCollectionManager collectionManager;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            AuthenticationManager am = AuthenticationManager.getAuthenticationManager(collectionManager.getMasterServer());
            // TODO get these setup globally for each server (maybe via
            // the pool)
            UserFieldDefinition[] fieldDefs = am.getFieldDefinitions();
            com.canto.cumulus.usermanagement.User user = am.getUser(username);
            FieldValues fieldValues = user.getFieldValues();
            result = new Person();
            result.setUsername(username);
            result.setRoles(user.getRoleNames().getNames().toArray(new String[0]));
            HashMap<String, Object> userFields = new HashMap<>();
            for (UserFieldDefinition fieldDef : fieldDefs) {
                if (fieldDef.getGUID().equals(UID_USER_FIRST_NAME)) {
                    result.setFirstName(fieldValues.getValue(UID_USER_FIRST_NAME, ""));
                } else if (fieldDef.getGUID().equals(UID_USER_LAST_NAME)) {
                    result.setLastName(fieldValues.getValue(UID_USER_LAST_NAME, ""));
                } else if (fieldDef.getGUID().equals(UID_USER_EMAIL)) {
                    result.setEmail(fieldValues.getValue(UID_USER_EMAIL, ""));
                } else if (fieldDef.getGUID().equals(UID_USER_LOGIN_ACTIVE)) {
                    result.setLoginActive(fieldValues.getValue(UID_USER_LOGIN_ACTIVE, false));
                }

                //String fieldName = CumulusUtilities.normaliseFieldName(fieldDef.getName(Cumulus.getLanguageID()));
                String fieldName = fieldDef.getName(Cumulus.getLanguageID());
                com.canto.cumulus.usermanagement.FieldValue fv = fieldValues.getFieldValue(fieldDef.getGUID());
                if (fv != null && fv.fieldHasValue()) {
                    if (fieldDef.getType() == FieldTypeConstants.TypeEnum && fieldDef.getValueInterpretation() == FieldTypeConstants.VALUE_DEFAULT) {
                        userFields.put(fieldName, fieldDef.getStringEnumValue((Integer) fv.getValue(), Cumulus.getLanguageID()));
                    } else {
                        userFields.put(fieldName, fv.getValue());
                    }
                } else {
                    userFields.put(fieldName, "");
                }
            }
            result.setFields(userFields);
        } catch (CumulusException ce) {
            //logger.debug("attempt to validate connection: '" + connection.toString() + " failed");
            // authentication failed, so just return null - could be admin user
            // authentication attempt, which is not allowed
            ce.printStackTrace();
        } catch (Exception e) {
            //logger.debug("attempt to validate connection: '" + connection.toString() + " failed");
            e.printStackTrace();
        }
        return result;
    }

    public Person getUser(Connection connection, String username, String password) {
        Person result = null;
        CumulusCollectionManager collectionManager = null;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            Server server = Server.openConnection(false, connection.getServer(), username, password);
            Integer catalogId = server.findCatalogID(connection.getDatabase());
            Set<Integer> catalogIds = server.getCatalogIDs(false, false);
            boolean found = false;
            for (Integer testId : catalogIds) {
                if (catalogId == testId) {
                    found = true;
                    break;
                }
            }
            if (found) {
                // TODO if possible, do not return guest user
                //logger.debug("--- getting auth stuff");
                AuthenticationManager am = AuthenticationManager.getAuthenticationManager(collectionManager.getMasterServer());
                // TODO get these setup globally for each server (maybe via
                // the pool)
                UserFieldDefinition[] fieldDefs = am.getFieldDefinitions();
                com.canto.cumulus.usermanagement.User user = am.getUser(username);
                FieldValues fieldValues = user.getFieldValues();
                result = new Person();
                result.setUsername(username);
                result.setRoles(user.getRoleNames().getNames().toArray(new String[0]));
                HashMap<String, Object> userFields = new HashMap<>();
                for (UserFieldDefinition fieldDef : fieldDefs) {
                    if (fieldDef.getGUID().equals(UID_USER_FIRST_NAME)) {
                        result.setFirstName(fieldValues.getValue(UID_USER_FIRST_NAME, ""));
                    } else if (fieldDef.getGUID().equals(UID_USER_LAST_NAME)) {
                        result.setLastName(fieldValues.getValue(UID_USER_LAST_NAME, ""));
                    } else if (fieldDef.getGUID().equals(UID_USER_EMAIL)) {
                        result.setEmail(fieldValues.getValue(UID_USER_EMAIL, ""));
                    } else if (fieldDef.getGUID().equals(UID_USER_LOGIN_ACTIVE)) {
                        result.setLoginActive(fieldValues.getValue(UID_USER_LOGIN_ACTIVE, false));
                    }

                    //String fieldName = CumulusUtilities.normaliseFieldName(fieldDef.getName(Cumulus.getLanguageID()));
                    String fieldName = fieldDef.getName(Cumulus.getLanguageID());
                    com.canto.cumulus.usermanagement.FieldValue fv = fieldValues.getFieldValue(fieldDef.getGUID());
                    if (fv != null && fv.fieldHasValue()) {
                        if (fieldDef.getType() == FieldTypeConstants.TypeEnum && fieldDef.getValueInterpretation() == FieldTypeConstants.VALUE_DEFAULT) {
                            userFields.put(fieldName, fieldDef.getStringEnumValue((Integer) fv.getValue(), Cumulus.getLanguageID()));
                        } else {
                            userFields.put(fieldName, fv.getValue());
                        }
                    } else {
                        userFields.put(fieldName, "");
                    }
                }
                result.setFields(userFields);
            }
        } catch (LoginFailedException lfe) {
            //logger.debug("attempt to validate connection: '" + connection.toString() + " failed");
            // authentication failed, so just return null
        } catch (CumulusException ce) {
            //logger.debug("attempt to validate connection: '" + connection.toString() + " failed");
            // authentication failed, so just return null - could be admin user
            // authentication attempt, which is not allowed
            ce.printStackTrace();
        } catch (Exception e) {
            //logger.debug("attempt to validate connection: '" + connection.toString() + " failed");
            e.printStackTrace();
        }
        return result;
    }

    public boolean activateUser(Connection connection, String username, boolean activate) {
        boolean result = true;
        try {
            Server server = null;
            try {
                server = Server.openConnection(true, connection.getServer(), connection.getUsername(), connection.getPassword());
                AuthenticationManager am = AuthenticationManager.getAuthenticationManager(server);
                com.canto.cumulus.usermanagement.User user = am.getUser(username);
                com.canto.cumulus.usermanagement.FieldValue loginActiveField = user.getFieldValues().getFieldValue(UID_USER_LOGIN_ACTIVE);
                loginActiveField.setFieldValue(activate);
                user.save();
                result = true;
            } catch (LoginFailedException | PasswordExpiredException | ServerNotFoundException | CumulusException e) {
                e.printStackTrace();
            } finally {
                server = null;
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * Utility methods
     *
     * @param connection
     * @return
     */
    public DatabaseField[] getFields(Connection connection) {
        return getFields(connection, DamManager.ALL_FIELDS);
    }

    public void checkAllFieldsReady(Connection connection) {
        if (recordFields.get(connection) == null) {
            getFields(connection, DamManager.ALL_FIELDS);
        }
    }

    public DatabaseField[] getFields(Connection connection, String viewName) {
        DatabaseField[] result = null;
        View view = null;
        boolean getAllFields = DamManager.ALL_FIELDS.equals(viewName);
        if (getAllFields) {
            result = recordFields.get(connection);
        } else {
            view = views.get(viewName);
            HashMap<String, DatabaseField[]> vf = viewFields.get(connection);
            if (vf != null) {
                result = vf.get(viewName);
            }
        }
        //if (result != null || view == null) {
        if (result != null) {
            return result;
        }
        result = new DatabaseField[0];
        CumulusCollectionManager collectionManager = null;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            if (collectionManager.getOnDemand()) {
                // we need to open a connection, and close it later
                collectionManager.borrowOnDemand();
            }
            Layout layout = collectionManager.getRecordLayout();
            Set<GUID> guids = layout.getFieldUIDs();
            result = new DatabaseField[guids.size()];
            ArrayList<DatabaseField> fields = new ArrayList<>();
            List<Field> configFields = null;
            if (!getAllFields && view != null) {
                configFields = view.getField();
            }
            for (GUID guid : guids) {
                FieldDefinition fieldDefinition = layout.getFieldDefinition(guid);
                String fn = fieldDefinition.getName();
                Field configField = null;
                if (!getAllFields && configFields != null) {
                    for (Field fi : configFields) {
                        if (fn.equals(fi.getName())) {
                            configField = fi;
                            break;
                        }
                    }
                    if (configField == null) {
                        // not interested as field not in view
                        continue;
                    }
                }
                DatabaseField field = new DatabaseField();
                field.setName(fieldDefinition.getName());
                if (!getAllFields && configField != null && configField.getSimpleName() != null) {
                    field.setSimpleName(configField.getSimpleName());
                } else {
                    field.setSimpleName(CumulusUtilities.normaliseFieldName(field.getName()));
                }
                field.setDataType(fieldDefinition.getFieldType());
                field.setValueInterpretation(fieldDefinition.getValueInterpretation());
                field.setGuid((guid == null) ? null : guid.toString());
                //logger.debug("Database field named '" + field.getName() + "' has type: " + field.getDataType() + " and interpretation: " + field.getValueInterpretation()+ " in database: " + connection.getDatabase());
                if (field.isSimpleSelect() || field.isMultiSelect() || field.isRating()) {
                    Integer[] ids = fieldDefinition.getStringEnumIDs().toArray(new Integer[0]);
                    StringListValue[] listValues = new StringListValue[ids.length];
                    if (ids.length > 0) {
                        for (int j = 0; j < ids.length; j++) {
                            StringListValue listValue = new StringListValue();
                            listValue.setId(ids[j]);
                            listValue.setDisplayString(fieldDefinition.getStringEnumName(listValue.getId()));
                            listValues[j] = listValue;
                        }
                    }
                    field.setListValues(listValues);
                } else if (field.isLabelSelect()) {
                    Integer[] ids = fieldDefinition.getStringEnumIDs().toArray(new Integer[0]);
                    LabelValue labelValues[] = new LabelValue[ids.length];
                    if (ids.length > 0) {
                        for (int j = 0; j < ids.length; j++) {
                            LabelValue labelValue = new LabelValue();
                            labelValue.setId(ids[j]);
                            labelValue.setDisplayString("");
                            try {
                                labelValue.setColor(new Integer(fieldDefinition.getStringEnumName(labelValue.getId())));
                            } catch (InvalidArgumentException | NumberFormatException e1) {
                                // ignore
                            }
                            labelValues[j] = labelValue;
                        }
                    }
                    field.setListValues(labelValues);
                }
                if (field.getDataType() == FieldTypeConstants.TypeTable) {
                    field.setTableDefinition(setupTableDefinition(connection, fieldDefinition));
                }
                fields.add(field);
            }
            result = fields.toArray(new DatabaseField[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (getAllFields) {
            recordFields.put(connection, result);
            // Make a map for access via field name
            HashMap<String, DatabaseField> recordFieldsMap = new HashMap<>();
            for (DatabaseField field : result) {
                recordFieldsMap.put(field.getName(), field);
            }
            recordFieldsByName.put(connection, recordFieldsMap);
        } else {
            HashMap<String, DatabaseField[]> vf = new HashMap<>();
            vf.put(viewName, result);
            viewFields.put(connection, vf);
        }
        if (collectionManager != null && collectionManager.getOnDemand()) {
            // on demand, so drop the connection
            collectionManager.terminate();
        }

        return result;
    }

    public Map<String, String> getPreviews(Connection connection, String viewName) {
        Map<String, String> result = null;
        HashMap<String, HashMap<String, String>> connectionPreviews = previewFields.get(connection);
        if (connectionPreviews != null && connectionPreviews.get(viewName) != null) {
            return connectionPreviews.get(viewName);
        }
        try {
            connectionPreviews = new HashMap<>();
            View view = views.get(viewName);
            HashMap<String, String> fields = new HashMap<>();
            List<String> previews = view.getPreview();
            if (view.getPreview() != null && view.getPreview().size() > 0) {
                for (String preview : previews) {
                    String templateUrl = baseUrl;
                    switch (preview) {
                        case DamManager.DOWNLOAD:
                            templateUrl += "/" + serverPrefix + "file/" + DamManager.TEMPLATE_PARAM_CATALOG_NAME + "/get?id=" + DamManager.TEMPLATE_PARAM_ID;
                            fields.put(preview, templateUrl);
                            break;
                        case DamManager.THUMBNAIL:
                            templateUrl += "/" + serverPrefix + "preview/" + DamManager.TEMPLATE_PARAM_CATALOG_NAME + "/fetch?id=" + DamManager.TEMPLATE_PARAM_ID + "&name=thumbnail";
                            fields.put(preview, templateUrl);
                            break;
                        default:
                            templateUrl += "/" + serverPrefix + "preview/" + DamManager.TEMPLATE_PARAM_CATALOG_NAME + "/fetch?id=" + DamManager.TEMPLATE_PARAM_ID + "&name=" + DamManager.TEMPLATE_PARAM_NAME;
                            fields.put(preview, templateUrl);
                            break;
                    }
                }
            }
            connectionPreviews.put(viewName, fields);
            previewFields.put(connection, connectionPreviews);
            result = fields;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, String> getLinks(Connection connection, String viewName) {
        Map<String, String> result = null;
        HashMap<String, HashMap<String, String>> connectionLinks = linkFields.get(connection);
        if (connectionLinks != null && connectionLinks.get(viewName) != null) {
            return connectionLinks.get(viewName);
        }
        try {
            connectionLinks = new HashMap<>();
            View view = views.get(viewName);
            HashMap<String, String> fields = new HashMap<>();
            List<String> links = view.getLink();
            if (view.getPreview() != null && view.getPreview().size() > 0) {
                for (String link : links) {
                    String templateUrl = baseUrl + "/" + serverPrefix + "data/" + DamManager.TEMPLATE_PARAM_CATALOG_NAME + "/fetch?id=" + DamManager.TEMPLATE_PARAM_ID + "&view=" + link;
                    fields.put(link, templateUrl);
                }
            }
            connectionLinks.put(viewName, fields);
            linkFields.put(connection, connectionLinks);
            result = fields;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, String> getReferences(Connection connection, String viewName) {
        Map<String, String> result = null;
        HashMap<String, HashMap<String, String>> connectionRefs = referenceFields.get(connection);
        if (connectionRefs == null) {
            connectionRefs = new HashMap<>();
            referenceFields.put(connection, connectionRefs);
        } else if (connectionRefs.get(viewName) != null) {
            return connectionRefs.get(viewName);
        }
        try {
            referenceFields.put(connection, connectionRefs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private DatabaseTable setupTableDefinition(Connection connection, FieldDefinition fieldDefinition) {
        HashMap<String, DatabaseTable> fields = tableFields.get(connection);
        if (fields == null) {
            fields = new HashMap<>();
        }
        DatabaseTable result = fields.get(fieldDefinition.getFieldUID().toString());
        if (result != null) {
            return result;
        }
        result = new DatabaseTable();
        CumulusCollectionManager collectionManager = null;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            String tableName = fieldDefinition.getLayout().getTableName() + "/" + fieldDefinition.getFieldUID().toString();
            Layout tableLayout = collectionManager.getMasterCatalog().getLayout(tableName);
            Set<GUID> tableGuids = tableLayout.getFieldUIDs();
            DatabaseField[] columns = new DatabaseField[tableGuids.size() - 2]; // do not bother with Host and Item ids
            int i = 0;
            for (GUID tableGuid : tableGuids) {
                if (tableGuid.equals(GUID.UID_HOST_ITEM_ID) || tableGuid.equals(GUID.UID_ITEM_ID)) {
                    continue;
                }
                FieldDefinition tableFieldDefinition = tableLayout.getFieldDefinition(tableGuid);
                columns[i] = new DatabaseField();
                columns[i].setName(tableFieldDefinition.getName());
                columns[i].setSimpleName(CumulusUtilities.normaliseFieldName(columns[i].getName()));
                columns[i].setDataType(tableFieldDefinition.getFieldType());
                columns[i].setValueInterpretation(tableFieldDefinition.getValueInterpretation());
                columns[i].setGuid((tableGuid == null) ? null : tableGuid.toString());
                logger.debug(tableName + " table field: '" + columns[i].getName() + "' has type: " + columns[i].getDataType() + " and interpretation: " + columns[i].getValueInterpretation() + " in database: " + collectionManager.getMasterCatalog().getName());

                if (columns[i].getDataType() == FieldTypeConstants.TypeTable) {
                    columns[i].setTableDefinition(setupTableDefinition(connection, tableFieldDefinition));
                }
                i++;
            }
            result.setGuid(fieldDefinition.getFieldUID().toString());
            result.setName(fieldDefinition.getName());
            result.setColumns(columns);

        } catch (CumulusException | InvalidArgumentException e) {
            e.printStackTrace();

        }
        fields.put(fieldDefinition.getFieldUID().toString(), result);
        tableFields.put(connection, fields);

        return result;
    }

    public RecordItem findRecordByQuery(Connection connection, String query) {
        RecordItem result = null;
        CumulusCollectionManager collectionManager;
        try {
            collectionManager = getOrInitCollectionManager(connection);
            result = collectionManager.queryForRecord(query);
        } catch (QueryParserException | CumulusException e) {
            e.printStackTrace();
        }
        return result;
    }

    public QueryResult findRecords(Connection connection, String query, SearchDescriptor searchDescriptor) {
        QueryResult result = new QueryResult();
        CumulusCollectionManager collectionManager = null;
        ResultSet queryResults = null;
        try {
            DatabaseField[] fields = getFields(connection, searchDescriptor.getViewName());
            Map<String, String> previews = getPreviews(connection, searchDescriptor.getViewName());
            Map<String, String> links = getLinks(connection, searchDescriptor.getViewName());
            collectionManager = getOrInitCollectionManager(connection);
            SortRule sortBy = searchDescriptor.getSortRule();
            int count = searchDescriptor.getCount();
            int offset = searchDescriptor.getOffset();
            Locale locale = searchDescriptor.getLocale();
            if (sortBy != null) {
                try {
                    if ("ID".equals(sortBy.getFieldName())) {
                        sortBy.setFieldGuid(GUID.UID_ITEM_ID.toString());
                    } else {
                        checkAllFieldsReady(connection);
                        sortBy.setFieldGuid(recordFieldsByName.get(connection).get(sortBy.getFieldName()).getGuid());
                        //logger.debug("sorting results using field name: '"+sortBy.getFieldName()+"' and guid "+sortBy.getFieldGuid()+" and direction: "+sortBy.getDirection());
                    }
                } catch (Exception e) {
                    logger.error("problem getting sort field guid for field name: '" + sortBy.getFieldName() + "'");
                }
            }
            boolean ok = false;
            if (collectionManager != null) {
                try {
                    queryResults = collectionManager.findRecords(query, offset, count, sortBy, locale);
                    ok = true;
                } catch (QueryParserException qpe) {
                    logger.info("problem processing query: '" + query + "'");
                } finally {
                }
            } else {
                logger.error("Problem - no collection manager found");
            }
            if (ok && queryResults != null && collectionManager != null) {
                Layout layout = collectionManager.getRecordLayout();
                GUID[] guids = new GUID[fields.length];
                for (int f = 0; f < fields.length; f++) {
                    try {
                        String guidS = fields[f].getGuid();
                        if (guidS != null && !"".equals(guidS)) {
                            guids[f] = new GUID(guidS);
                        }
                    } catch (Exception e) {
                        // do nothing
                    }
                }
                // sort out paging
                result.setOffset(queryResults.getOffset());
                result.setTotal(queryResults.getTotalCount());
                result.setCount(queryResults.getRecords().length);
                if (queryResults.getOffset() < result.getTotal()) { // if offset out of range, return empty list
                    Record[] records = new Record[queryResults.getRecords().length];
                    for (int i = 0; i < queryResults.getRecords().length; i++) {
                        records[i] = new Record();
                        //RecordItem recordItem = thePool.getRecordItemById(recordId, false);
                        RecordItem recordItem = queryResults.getRecord(i);
                        if (recordItem != null) {
                            records[i].setConnection(connection.getName());
                            records[i].setId(String.valueOf(recordItem.getID()));
                            if (recordItem.hasValue(GUID.UID_REC_ASSET_REFERENCE)) {
                                AssetReference assetReference = recordItem.getAssetReferenceValue(GUID.UID_REC_ASSET_REFERENCE);
                                // just ignore failures, as then parts do not exist, which
                                // is ok.
                                try {
                                    String aref = assetReference.getDisplayString();
                                    String fileName = null;
                                    if (aref.startsWith("Vault")) {
                                        String[] bits = aref.split(",");
                                        fileName = bits[bits.length - 1];
                                    } else {
                                        String[] bits = aref.split(":");
                                        if (bits.length > 1) {
                                            // OS prefix, e.g. Windows, ;ac, Unix
                                            Path ar = FileSystems.getDefault().getPath(bits[bits.length - 1]);
                                            fileName = ar.getFileName().toString();
                                        } else {
                                            // no idea, can't work out the filename
                                        }
                                    }
                                    if (fileName != null && !"".equals(fileName)) {
                                        records[i].setFileName(fileName);
                                        String ext = "";
                                        if (fileName.lastIndexOf(".") > 0) {
                                            records[i].setExtension(fileName.substring(fileName.lastIndexOf(".")));
                                        }
                                    } else {
                                        records[i].setFileName("");
                                        records[i].setExtension("");

                                    }
                                } catch (CumulusException ce) {
                                }
                                try {
                                    records[i].setAssetReferenceWindows(assetReference.getPart(GUID.UID_AS_WIN_FILE).getDisplayString());
                                    Path ar = FileSystems.getDefault().getPath(records[i].getAssetReferenceWindows());
                                    if (ar != null) {
                                        String fileName = ar.getFileName().toString();
                                        records[i].setFileName(fileName);
                                        String ext = "";
                                        if (fileName.lastIndexOf(".") > 0) {
                                            records[i].setExtension(fileName.substring(fileName.lastIndexOf(".")));
                                        }
                                    }
                                } catch (CumulusException ce) {
                                }
                                try {
                                    records[i].setAssetReferenceMac(assetReference.getPart(GUID.UID_AS_MAC_FILE).getDisplayString());
                                } catch (CumulusException ce) {
                                }
                                try {
                                    records[i].setAssetReferenceUnix(assetReference.getPart(GUID.UID_AS_UNIX_FILE).getDisplayString());
                                } catch (CumulusException ce) {
                                }
                                try {
                                    records[i].setAssetReferenceVault(assetReference.getPart(GUID.UID_AS_VAULT).getDisplayString());
                                    //JPack vaultData = new JPack(assetReference.getPart(GUID.UID_AS_VAULT).getBinaryData());
                                    //logger.debug(" vault asset reference: "+vaultData.getXML());
                                    // best to get via XML, but for now hack it via the display stirng
                                    // String[] vaultBits =result.records[i].assetReferenceVault.split(", ");
                                    // result.records[i].assetReferenceWindows = "\\"+vaultBits[2]+"\\00000001.data";
                                } catch (CumulusException ce) {
                                }


                            }
                            for (int f = 0; f < fields.length; f++) {
                                DatabaseField field = fields[f];
                                if (guids[f] == null) {
                                    // return an undefined field type
                                    records[i].addField(field.getName(), new FieldValue());
                                    continue;
                                }
                                FieldValue fieldValue = getFieldValue(recordItem, guids[f], layout, locale);
                                fieldValue.setDataType(field.getDataType());
                                fieldValue.setValueInterpretation(field.getValueInterpretation());
                                records[i].addField(field.getName(), fieldValue);
                            }
                            // look for keywords
                            CategoriesFieldValue categories = recordItem.getCategoriesValue();
                            if (categories != null) {
                                Set<String> keywordSet = new HashSet<>(); // use a set to avoid duplicates
                                AllCategoriesItemCollection allCategoriesItemCollection = collectionManager.getAllCategoriesItemCollection();
                                for (Integer categoryId : categories.getIDs()) {
                                    CategoryItem categoryItem = allCategoriesItemCollection.getCategoryItemByID(categoryId);
                                    // logger.info("Category tree path: " +
                                    // categoryItem.getCategoryTreePath());
                                    String categoryTreePath = categoryItem.getCategoryTreePath();
                                    if (categoryTreePath.startsWith("$Keywords")) {
                                        String[] bits = categoryTreePath.split(":");
                                        for (int j = 1; j < bits.length; j++) { // ignore the first one of course
                                            keywordSet.add(bits[j]);
                                        }
                                    }
                                }
                                ArrayList<Category> cumulusCategories = new ArrayList<>();
                                for (String keyword : keywordSet) {
                                    Category cumulusCategory = new Category();
                                    cumulusCategory.setName(keyword);
                                    cumulusCategories.add(cumulusCategory);
                                }
                                cumulusCategories.trimToSize();
                                records[i].setKeywords(cumulusCategories.toArray(new Category[0]));
                            }

                            // do previews
                            Map<String, String> preview_links = new HashMap<>();
                            for (Map.Entry<String, String> preview : previews.entrySet()) {
                                String url = preview.getValue().replaceAll(DamManager.TEMPLATE_PARAM_CATALOG_NAME_REGEX, connection.getName());
                                url = url.replaceAll(DamManager.TEMPLATE_PARAM_ID_REGEX, String.valueOf(records[i].getId()));
                                url = url.replaceAll(DamManager.TEMPLATE_PARAM_NAME_REGEX, preview.getKey());
                                preview_links.put(preview.getKey(), url);
                            }
                            records[i].setPreviews(preview_links);

                            // do links
                            Map<String, String> record_links = new HashMap<>();
                            for (Map.Entry<String, String> link : links.entrySet()) {
                                String url = link.getValue().replaceAll(DamManager.TEMPLATE_PARAM_CATALOG_NAME_REGEX, String.valueOf(connection.getName()));
                                url = url.replaceAll(DamManager.TEMPLATE_PARAM_ID_REGEX, String.valueOf(records[i].getId()));
                                record_links.put(link.getKey(), url);
                            }
                            records[i].setLinks(record_links);
                        } else {
                            logger.info("connection pool did not return record - you may need to increase your Cumulus license count");
                        }
                    }
                    result.setRecords(records);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (collectionManager != null && queryResults != null && queryResults.getCollection() != null) {
                collectionManager.returnReadObject(queryResults.getCollection());
            }
        }
        return result;
    }

    protected void setFieldValue(Item item, GUID guid, FieldValue fieldValue, Layout layout) throws Exception {
        switch (fieldValue.getDataType()) {
            case FieldTypeConstants.TypeString:
                // TODO sort this out
                // Silently ignore errors - a one off tempory fix for Eniro as they are
                // setting the value
                // of a formula field "Eniro ProductId" directly"
                try {
                    item.setStringValue(guid, fieldValue.getStringValue());
                } catch (FieldNotFoundException | CumulusException se) {
                    // maybe a formula field
                    logger.info("problem setting string field: " + guid + " to value " + fieldValue.getStringValue());
                    se.printStackTrace();
                }
                break;
            case FieldTypeConstants.TypeInteger:
                if (layout.getFieldDefinition(guid).getValueInterpretation() == FieldTypeConstants.VALUE_DATA_SIZE) {
                    DataSizeFieldValue dsv = item.getDataSizeValue(guid);
                    dsv.setCoreValue(fieldValue.getDataSizeValue().getValue());
                    item.setDataSizeValue(guid, dsv);
                } else {
                    item.setIntValue(guid, fieldValue.getIntegerValue());
                }
                break;
            case FieldTypeConstants.TypeLong:
                item.setLongValue(guid, fieldValue.getLongValue());
                break;
            case FieldTypeConstants.TypeDouble:
                item.setDoubleValue(guid, fieldValue.getDoubleValue());
                break;
            case FieldTypeConstants.TypeBool:
                item.setBooleanValue(guid, fieldValue.getBooleanValue());
                break;
            case FieldTypeConstants.TypePicture:
                item.setPictureValue(guid, new Pixmap(fieldValue.getByteArrayValue()));
                break;
            case FieldTypeConstants.TypeDate:
                //logger.debug("got date value to set: " + fieldValue.dateTimeValue.getValue());
                //logger.debug("   --- java date value is: " + new Date(fieldValue.dateTimeValue.getValue()));
                if (fieldValue.getDateTimeValue().getValue() > 0) {
                    item.setDateValue(guid, new Date(fieldValue.getDateTimeValue().getValue()));
                }
                break;
            case FieldTypeConstants.TypeBinary:
                item.setBinaryValue(guid, fieldValue.getByteArrayValue());
                break;
            case FieldTypeConstants.TypeEnum:
                //switch (layout.getFieldDefinition(guid).getValueInterpretation()) {
                switch (fieldValue.getValueInterpretation()) {
                    case FieldTypeConstants.VALUE_DEFAULT:
                        StringEnumFieldValue ev = item.getStringEnumValue(guid);
                        ev.setID(fieldValue.getStringListValue()[0].getId());
                        item.setStringEnumValue(guid, ev);
                        break;
                    case FieldTypeConstants.VALUE_STRING_ENUM_RATING:
                        ev = item.getStringEnumValue(guid);
                        ev.setID(fieldValue.getStringListValue()[0].getId());
                        item.setStringEnumValue(guid, ev);
                        break;
                    case FieldTypeConstants.VALUE_STRING_ENUM_MULTIPLE_VALUES:
                        ev = item.getStringEnumValue(guid);
                        if (ev.supportsMultipleValues() && fieldValue.getStringListValue().length > 0) {
                            Set<Integer> ids = new HashSet<>();
                            if (ev.hasValue()) {
                                ids = ev.getIDs();
                                // we assume all values to be overwritten, so first clear the set
                                ids.clear();
                            }
                            for (StringListValue v : fieldValue.getStringListValue()) {
                                if (v.getId() >= 0) {
                                    ids.add(v.getId());
                                }
                            }
                            ev.setIDs(ids);
                            item.setStringEnumValue(guid, ev);
                        } else {
                            logger.info("attempt to set multiple values on single value string enum field");
                        }
                        break;
                    case FieldTypeConstants.VALUE_STRING_ENUM_LABEL:
                        break;
                    default:
                        break;
                }
                break;
            case FieldTypeConstants.TypeTable:
                ItemCollection tableCollection = item.getTableValue(guid);
                tableCollection.close();
                break;
            default:
                break;
        }
    }

    public static FieldValue getFieldValue(Item item, GUID guid, Layout layout, Locale locale) throws Exception {
        FieldValue result = new FieldValue();
        // if (item.hasValue(guid)) {
        result.setDataType(layout.getFieldDefinition(guid).getFieldType());
        switch (result.getDataType()) {
            case FieldTypeConstants.TypeString:
                if (item.hasValue(guid)) {
                    result.setStringValue(item.getStringValue(guid));
                }
                break;
            case FieldTypeConstants.TypeInteger:
                if (item.hasValue(guid)) {
                    if (layout.getFieldDefinition(guid).getValueInterpretation() == FieldTypeConstants.VALUE_DATA_SIZE) {
                        DataSizeValue v = new DataSizeValue();
                        v.setValue(item.getDataSizeValue(guid).getCoreValue());
                        v.setDisplayString(item.getDataSizeValue(guid).getDisplayString());
                        result.setDataSizeValue(v);
                    } else {
                        result.setIntegerValue(item.getIntValue(guid));
                    }
                }
                break;
            case FieldTypeConstants.TypeLong:
                if (item.hasValue(guid)) {
                    result.setLongValue(item.getLongValue(guid));
                }
                break;
            case FieldTypeConstants.TypeDouble:
                if (item.hasValue(guid)) {
                    result.setDoubleValue(item.getDoubleValue(guid));
                }
                break;
            case FieldTypeConstants.TypeBool:
                if (item.hasValue(guid)) {
                    result.setBooleanValue(item.getBooleanValue(guid));
                }
                break;
            case FieldTypeConstants.TypePicture:
                if (item.hasValue(guid)) {
                    result.setByteArrayValue(item.getPictureValue(guid).getData());
                }
                break;
            case FieldTypeConstants.TypeDate:
                if (item.hasValue(guid)) {
                    DateTime v = new DateTime();
                    v.setValue(item.getDateValue(guid).getTime());
                    result.setDateTimeValue(v);
                }
                break;
            case FieldTypeConstants.TypeBinary:
                if (item.hasValue(guid)) {
                    result.setByteArrayValue(item.getBinaryValue(guid));
                }
                break;
            case FieldTypeConstants.TypeEnum:
                switch (layout.getFieldDefinition(guid).getValueInterpretation()) {
                    case FieldTypeConstants.VALUE_DEFAULT:
                        StringListValue v[] = new StringListValue[1];
                        result.setValueInterpretation(FieldTypeConstants.VALUE_DEFAULT);
                        StringListValue wsStringListValue = new StringListValue();
                        if (item.hasValue(guid)) {
                            StringEnumFieldValue stringListValue = item.getStringEnumValue(guid);
                            try {
                                // may have no value, so catch and ignore (like a "hasValue(guid)" check
                                wsStringListValue.setId(stringListValue.getID());
                                wsStringListValue.setDisplayString(stringListValue.getDisplayString(locale));
                            } catch (Exception e1) {
                                // ignore
                            }
                        }
                        v[0] = wsStringListValue;
                        result.setStringListValue(v);
                        break;
                    case FieldTypeConstants.VALUE_STRING_ENUM_MULTIPLE_VALUES:
                        if (item.hasValue(guid)) {
                            FieldDefinition fieldDefinition = layout.getFieldDefinition(guid);
                            StringEnumFieldValue stringListValue = item.getStringEnumValue(guid);
                            Set<Integer> ids = stringListValue.getIDs();
                            if (ids.size() > 0) {
                                StringListValue[] slv = new StringListValue[ids.size()];
                                int c = 0;
                                for (Integer id : ids) {
                                    wsStringListValue = new StringListValue();
                                    wsStringListValue.setId(id);
                                    wsStringListValue.setDisplayString(fieldDefinition.getStringEnumName(id, LanguageManager.getCumulusLanguageId(locale)));
                                    slv[c++] = wsStringListValue;
                                }
                                result.setValueInterpretation(FieldTypeConstants.VALUE_STRING_ENUM_MULTIPLE_VALUES);
                                result.setStringListValue(slv);
                            } else {
                                // return a single null value, so client code does not break if no value set
                                StringListValue[] slv = new StringListValue[1];
                                slv[0] = new StringListValue();
                                result.setValueInterpretation(FieldTypeConstants.VALUE_STRING_ENUM_MULTIPLE_VALUES);
                                result.setStringListValue(slv);
                            }
                        } else {
                            // return a single null value, so client code does not break if no value set
                            StringListValue[] slv = new StringListValue[1];
                            slv[0] = new StringListValue();
                            result.setValueInterpretation(FieldTypeConstants.VALUE_STRING_ENUM_MULTIPLE_VALUES);
                            result.setStringListValue(slv);
                        }
                        break;
                    case FieldTypeConstants.VALUE_STRING_ENUM_LABEL:
                        LabelValue wsLabelValue = new LabelValue();
                        if (item.hasValue(guid)) {
                            LabelFieldValue labelValue = item.getLabelValue(guid);
                            wsLabelValue.setId(labelValue.getID());
                            wsLabelValue.setDisplayString(labelValue.getDisplayString(locale));
                            wsLabelValue.setColor(labelValue.getColor());
                        }
                        result.setLabelValue(wsLabelValue);
                        result.setValueInterpretation(FieldTypeConstants.VALUE_STRING_ENUM_LABEL);
                        break;
                    default:
                        StringListValue[] slv = new StringListValue[1];
                        wsStringListValue = new StringListValue();
                        if (item.hasValue(guid)) {
                            StringEnumFieldValue stringListValue = item.getStringEnumValue(guid);
                            wsStringListValue.setId(stringListValue.getID());
                            wsStringListValue.setDisplayString(stringListValue.getDisplayString(locale));
                        }
                        slv[0] = wsStringListValue;
                        result.setValueInterpretation(FieldTypeConstants.VALUE_DEFAULT);
                        result.setStringListValue(slv);
                        break;
                }
                break;
            case FieldTypeConstants.TypeTable:
                if (item.hasValue(guid)) {
                    result.setTableValue(getTable(item, guid, locale));
                }
                break;
            default:
                result.setDataType(FieldTypeConstants.TypeString);
                result.setStringValue("");
                break;
        }
        // }
        return result;
    }

    public static TableValue getTable(Item item, GUID guid, Locale locale) {
        return getTable(null, item, guid, null, locale);
    }

    public static TableValue getTable(Catalog catalog, Item item, GUID guid, String query, Locale locale) {
        TableValue result = new TableValue();
        ItemCollection tableCollection = null;
        try {
            if (item != null) {
                tableCollection = item.getTableValue(guid);
                if (query != null) {
                    // apply the query
                    tableCollection.find(query, EnumSet.of(FindFlag.FIND_MISSING_FIELDS_ARE_ERROR), CombineMode.FIND_NEW, locale);
                }
            } else {
                // catalog needs to exist for this
                String tableName = Cumulus.TABLE_NAME_ASSET_RECORDS + "/" + guid.toString();
                tableCollection = catalog.newItemCollection(tableName, query, EnumSet.of(FindFlag.FIND_MISSING_FIELDS_ARE_ERROR), locale);
            }
            Layout tableLayout = tableCollection.getLayout();
            // logger.debug("accessing table: " + tableLayout.getTableName());
            Set<GUID> tableGuids = tableLayout.getFieldUIDs();
            result.setColumnNames(new String[tableGuids.size()]);
            int cc = 0;
            for (GUID tableGuid : tableGuids) {
                result.getColumnNames()[cc++] = tableLayout.getFieldDefinition(tableGuid).getName();
            }
            result.setRows(new TableItemValue[tableCollection.getItemCount()]);
            int tc = 0;
            for (Item tableItem : tableCollection) {
                TableItemValue tableItemValue = new TableItemValue();
                tableItemValue.setId(tableItem.getID());
                tableItemValue.setHostId(tableItem.getIntValue(GUID.UID_HOST_ITEM_ID));
                tableItemValue.setColumns(new FieldValue[tableGuids.size()]);
                int colc = 0;
                for (GUID tableGuid : tableGuids) {
                    //logger.debug("getting table field for table item id: "+tableItem.getID()+", guid is: "+tableGuid);
                    FieldDefinition fieldDefinition = tableLayout.getFieldDefinition(tableGuid);
                    FieldValue fv = getFieldValue(tableItem, tableGuid, tableLayout, locale);
                    fv.setDataType(fieldDefinition.getFieldType());
                    fv.setValueInterpretation(fieldDefinition.getValueInterpretation());
                    tableItemValue.getColumns()[colc++] = fv;
                }
                result.getRows()[tc++] = tableItemValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (tableCollection != null) {
                tableCollection.close();
            }
        }
        return result;
    }

    public Locale getLocale(String locale) {
        return (locale != null && !"".equals(locale)) ? new Locale(locale) : Locale.getDefault();
    }

    public Record getFileMetadata(Connection connection, Integer id, String view, Locale locale) {
        Record result = null;
        CumulusCollectionManager collectionManager = getOrInitCollectionManager(connection);
        RecordItem recordItem = null;
        try {
            DatabaseField[] fields = getFields(connection, view);
            Map<String, String> previews = getPreviews(connection, view);
            Map<String, String> links = getLinks(connection, view);
            recordItem = collectionManager.getRecordItemById(id, false);
            if (recordItem != null) {
                Layout layout = collectionManager.getRecordLayout();
                GUID[] guids = new GUID[fields.length];
                for (int f = 0; f < fields.length; f++) {
                    try {
                        String guidS = fields[f].getGuid();
                        if (guidS != null && !"".equals(guidS)) {
                            guids[f] = new GUID(guidS);
                        }
                    } catch (Exception e) {
                        // do nothing
                    }
                }
                result = new Record();
                result.setId(String.valueOf(recordItem.getID()));
                if (recordItem.hasValue(GUID.UID_REC_ASSET_REFERENCE)) {
                    AssetReference assetReference = recordItem.getAssetReferenceValue(GUID.UID_REC_ASSET_REFERENCE);
                    // just ignore failures, as then parts do not exist, which is ok.
                    try {
                        result.setAssetReferenceWindows(assetReference.getPart(GUID.UID_AS_WIN_FILE).getDisplayString());
                    } catch (CumulusException ce) {
                    }
                    try {
                        result.setAssetReferenceMac(assetReference.getPart(GUID.UID_AS_MAC_FILE).getDisplayString());
                    } catch (CumulusException ce) {
                    }
                    try {
                        result.setAssetReferenceUnix(assetReference.getPart(GUID.UID_AS_UNIX_FILE).getDisplayString());
                    } catch (CumulusException ce) {
                    }
                    try {
                        result.setAssetReferenceVault(assetReference.getPart(GUID.UID_AS_VAULT).getDisplayString());
                        //JPack vaultData = new JPack(assetReference.getPart(GUID.UID_AS_VAULT).getBinaryData());
                        //logger.debug(" vault asset reference: "+vaultData.getXML());
                        // best to get via XML, but for now hack it via the display stirng
                        // String[] vaultBits =result.records[i].assetReferenceVault.split(", ");
                        // result.records[i].assetReferenceWindows = "\\"+vaultBits[2]+"\\00000001.data";
                    } catch (CumulusException ce) {
                    }
                }
                for (int f = 0; f < fields.length; f++) {
                    DatabaseField field = fields[f];
                    if (guids[f] == null) {
                        // return an undefined field type
                        result.addField(field.getName(), new FieldValue());
                        continue;
                    }
                    FieldValue fieldValue = getFieldValue(recordItem, guids[f], layout, locale);
                    fieldValue.setDataType(field.getDataType());
                    fieldValue.setValueInterpretation(field.getValueInterpretation());
                    result.addField(field.getName(), fieldValue);
                }
                // look for keywords
                CategoriesFieldValue categories = recordItem.getCategoriesValue();
                if (categories != null) {
                    Set<String> keywordSet = new HashSet<>(); // use a set to avoid duplicates
                    AllCategoriesItemCollection allCategoriesItemCollection = collectionManager.getAllCategoriesItemCollection();
                    for (Integer categoryId : categories.getIDs()) {
                        CategoryItem categoryItem = allCategoriesItemCollection.getCategoryItemByID(categoryId);
                        // logger.info("Category tree path: " +
                        // categoryItem.getCategoryTreePath());
                        String categoryTreePath = categoryItem.getCategoryTreePath();
                        if (categoryTreePath.startsWith("$Keywords")) {
                            String[] bits = categoryTreePath.split(":");
                            for (int j = 1; j < bits.length; j++) { // ignore the first one of course
                                keywordSet.add(bits[j]);
                            }
                        }
                    }
                    ArrayList<Category> cumulusCategories = new ArrayList<>();
                    for (String keyword : keywordSet) {
                        Category cumulusCategory = new Category();
                        cumulusCategory.setName(keyword);
                        cumulusCategories.add(cumulusCategory);
                    }
                    cumulusCategories.trimToSize();
                    result.setKeywords(cumulusCategories.toArray(new Category[0]));
                }

                // do previews
                Map<String, String> preview_links = new HashMap<>();
                for (Map.Entry<String, String> preview : previews.entrySet()) {
                    String url = preview.getValue().replaceAll(DamManager.TEMPLATE_PARAM_CATALOG_NAME_REGEX, connection.getName());
                    url = url.replaceAll(DamManager.TEMPLATE_PARAM_ID_REGEX, String.valueOf(result.getId()));
                    url = url.replaceAll(DamManager.TEMPLATE_PARAM_NAME_REGEX, preview.getKey());
                    preview_links.put(preview.getKey(), url);
                }
                result.setPreviews(preview_links);

                // do links
                Map<String, String> record_links = new HashMap<>();
                for (Map.Entry<String, String> link : links.entrySet()) {
                    String url = link.getValue().replaceAll(DamManager.TEMPLATE_PARAM_CATALOG_NAME_REGEX, String.valueOf(connection.getName()));
                    url = url.replaceAll(DamManager.TEMPLATE_PARAM_ID_REGEX, String.valueOf(result.getId()));
                    record_links.put(link.getKey(), url);
                }
                result.setLinks(record_links);
            } else {
                logger.info("connection pool did not return record - you may need to increase your Cumulus license count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    public byte[] getThumbnail(Connection connection, String id, Integer maxSize, SearchDescriptor searchDescriptor) {
        byte[] result = new byte[0];
        RecordItemCollection collection = null;
        RecordItem recordItem = null;
        CumulusCollectionManager collectionManager = getOrInitCollectionManager(connection);
        try {
            collection = (RecordItemCollection) collectionManager.borrowObjectToRead(RecordItemCollection.class);
            Integer rid = -1;
            if (id == null) {
                SortRule sortRule = searchDescriptor.getSortRule();
                if (sortRule != null) {
                    if ("".equals(sortRule.getFieldGuid())) {
                        sortRule.setFieldGuid(getFieldGuid(connection, sortRule.getFieldName()));
                    }
                }
                ResultSet queryResults = collectionManager.findRecords(searchDescriptor);
                if (queryResults.getRecords().length > 0) {
                    rid = queryResults.getRecord(0).getID();
                }
            } else {
                rid = new Integer(id);
            }
            if (rid >= 0) {
                recordItem = collectionManager.getRecordItemById(rid, false, false);
                Pixmap pixmap = recordItem.getPictureValue(GUID.UID_REC_THUMBNAIL);
                if (searchDescriptor.getPreviewFormat() == SearchDescriptor.PreviewFormats.Jpg) {
                    if (maxSize <= 0) {
                        result = recordItem.getPictureValue(GUID.UID_REC_THUMBNAIL).getData();
                    } else {
                        result = recordItem.getPictureValue(GUID.UID_REC_THUMBNAIL).getAsJPEG(maxSize);
                    }
                } else if (searchDescriptor.getPreviewFormat() == SearchDescriptor.PreviewFormats.Png) {
                    if (maxSize <= 0) {
                        maxSize = Math.max(pixmap.getHeight(), pixmap.getWidth());
                    }
                    result = pixmap.getAsPNG(maxSize, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (collection != null) {
                collectionManager.returnReadObject(collection);
            }
        }
        return result;
    }

    public boolean openConnection(Connection connection, String username, String password, Integer poolSize) throws DamManagerNotImplementedException {
        Boolean result = false;
        if (connection == null) {
            return false;
        }
        CumulusCollectionManager collectionManager = getCollectionManager(connection);
        Connection newConnection;
        try {
            if (collectionManager != null) {
                newConnection = collectionManager.getConnection();
            } else {
                collectionManager = new CumulusCollectionManager();
                newConnection = connection;
            }
            if (username != null && !"".equals(username)) {
                newConnection.setUsername(username);
            }
            if (password != null && !"".equals(password)) {
                newConnection.setPassword(password);
            }
            if (poolSize != null) {
                newConnection.setPoolSize(poolSize);
            }
            if (collectionManager.init(newConnection)) {
                collectionManagers.put(connection.getName(), collectionManager);
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean closeConnection(Connection connection) throws DamManagerNotImplementedException {
        Boolean result = false;
        if (connection == null) {
            return false;
        }
        CumulusCollectionManager collectionManager = getCollectionManager(connection);
        if (collectionManager != null) {
            try {
                collectionManager.terminate();
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
