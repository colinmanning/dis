package com.setantamedia.fulcrum;

import com.setantamedia.fulcrum.common.*;
import com.setantamedia.fulcrum.config.FulcrumConfig;
import com.setantamedia.fulcrum.config.Preview;
import com.setantamedia.fulcrum.config.View;
import com.setantamedia.fulcrum.models.core.Person;
import com.setantamedia.fulcrum.ws.types.Category;
import com.setantamedia.fulcrum.dam.entities.Folder;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;
import com.setantamedia.fulcrum.ws.types.User;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DamManager {

    public final static String TEMPLATE_PARAM_CATALOG_NAME = "${catalog}";
    public final static String TEMPLATE_PARAM_CATALOG_NAME_REGEX = "\\$\\{catalog\\}";
    public final static String TEMPLATE_PARAM_ID = "${id}";
    public final static String TEMPLATE_PARAM_ID_REGEX = "\\$\\{id\\}";
    public final static String TEMPLATE_PARAM_NAME = "${name}";
    public final static String TEMPLATE_PARAM_NAME_REGEX = "\\$\\{name\\}";
    public final static String TEMPLATE_PARAM_RECORD_NAME = "${recordname}";
    public final static String TEMPLATE_PARAM_RECORD_NAME_REGEX = "\\$\\{recordname\\}";
    public final static String THUMBNAIL = "thumbnail";
    public final static String DOWNLOAD = "download";
    public final static String ALL_FIELDS = "all";
    
    public final static String FIELD_MIME_TYPE = "dis-mime-type";
    public final static String MIME_TYPE_JPEG = "image/jpeg";
    public final static String MIME_TYPE_PNG = "image/png";
    public final static String MIME_TYPE_OCTET_STREAM = "application/octet-stream";
    public final static String MIME_TYPE_HTML = "text/html";
    public final static String MIME_TYPE_PDF = "application/pdf";
    public final static String MIME_TYPE_TEXT = "text/plain";
    public final static String MIME_TYPE_JSON= "application/json;charset=UTF-8";
    
    public final static String REPORT_CONNECTION_NAME = "connection_name";
    public final static String REPORT_DATABASE_NAME = "database_name";
    public final static String REPORT_RECORD_COUNT = "record_count";
    public final static String REPORT_CATEGORY_PATH = "category_path";
    
    public String FIELD_TITLE = "Title";
    public String FIELD_DESCRIPTION = "Description";
 
    protected HashMap<String, View> views = new HashMap<>();
    protected HashMap<String, Query> queries = new HashMap<>();
    protected HashMap<String, String> params = new HashMap<>();
    protected HashMap<String, Connection> connections = new HashMap<>();
    protected FulcrumConfig config = null;
    protected String baseUrl = null;
    protected String serverPrefix = null;
    protected Path tmpDir = null;
    protected String adminPassword = null;

    public DamManager() {
    }

    public boolean registerConnection(String name, Connection connection) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public boolean deregisterConnection(String name, Connection connection) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public void init() {
    }

    public void terminate() {
    }

    public void setConfig(FulcrumConfig config) {
        this.config = config;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setServerPrefix(String serverPrefix) {
        this.serverPrefix = serverPrefix;
    }

    public String getServerPrefix() {
        return serverPrefix;
    }
    
    public HashMap<String, Object> runCatalogReport(Connection connection, String username, String password) throws DamManagerNotImplementedException {
       throw new DamManagerNotImplementedException();       
    }
    
    public HashMap<String, Object> runCategoryReport(Connection connection, String path, String username, String password) throws DamManagerNotImplementedException {
       throw new DamManagerNotImplementedException();       
    }
    
    public boolean openConnection(Connection connection, String username, String password, Integer poolSize) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();       
    }

    public boolean closeConnection(Connection connection) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();       
    }

    public String queryForRecordId(Connection connection, String query) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public QueryResult querySearch(Connection connection, User user, String query, SearchDescriptor searchDescriptor, Locale locale) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public QueryResult textSearch(Connection connection, String text, SearchDescriptor searchDescriptor, Locale locale) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public QueryResult categorySearch(Connection connection, String categoryId, SearchDescriptor searchDescriptor, Boolean recursive, Locale locale) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public byte[] getThumbnail(Connection connection, String id, Integer maxSize, SearchDescriptor searchDescriptor) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public byte[] previewFile(Connection connection, String id, Path cacheFile, Integer maxSize, String actionName) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public byte[] previewFile(Connection connection, String id, Preview previewData, Path file, String actionName) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public byte[] previewFile(Connection connection, String id, Integer top, Integer left, Integer height, Integer width, Integer maxSize, Integer size, Integer rotate, String format, Integer quality, Path file, String actionName) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public Date getModifiedTime(Connection connection, String id) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public String uploadFile(Connection connection, User user, InputStream inputStream, String fileName, String uploadProfile, HashMap<String, String> fields) {
        String result = null;
        try {
            Path tmpFile = makeTmpFile(inputStream, fileName);
            result = uploadFile(connection, user, tmpFile, fileName, uploadProfile, fields);
            Files.delete(tmpFile);
        } catch (DamManagerNotImplementedException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String uploadFile(Connection connection, User user, Path file, String fileName, String uploadProfile, HashMap<String, String> fields) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public String uploadFile(Connection connection, User user, Folder folder, Path file, String fileName, String uploadProfile, HashMap<String, String> fields) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public FileStreamer getFile(Connection connection, User user, String id, Integer version, String actionName) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }
    
    public FileStreamer getCroppedFile(Connection connection, User user, String id, Integer version, String actionName,
          Integer top, Integer left, Integer width, Integer height) throws DamManagerNotImplementedException {
       throw new DamManagerNotImplementedException();
    }

    public FileStreamer getFile(Connection connection, User user, String fieldKey, String keyValue, Integer version, String actionName) throws DamManagerNotImplementedException {
       throw new DamManagerNotImplementedException();
    }
    
    public boolean updateRecord(Connection connection, DatabaseField rootFieldDef, String tablePath, String recordKey, HashMap<String, String> fieldValues) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public boolean addRecordToCategory(Connection connection, String categoryId, String recordId) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public boolean addRecordToCategories(Connection connection, String[] categoryIds, String recordId) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public boolean removeRecordFromCategory(Connection connection, String categoryId, String recordId) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public Record getFileMetadata(Connection connection, String id, String view, Locale locale) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public boolean updateAssetData(Connection connection, String id, String fileName, HashMap<String, String> fields) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public boolean incrementFieldValue(Connection connection, String id, String field, int amount) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public boolean decrementFieldValue(Connection connection, String id, String field, int amount) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public Folder createFolder(Connection connection, User user, String path) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }
    
    public Folder createFolder(Connection connection, User user, Integer pathId) throws DamManagerNotImplementedException {
       throw new DamManagerNotImplementedException();
   }

    public Folder createSubFolder(Connection connection, User user, Folder parent, String path) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }
    public Category createCategory(Connection connection, User user, String path) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }
    
    /*
     * Keep this version for backward compilation compatability
     */
    public Category createCategory(Connection connection, String path) throws DamManagerNotImplementedException {
       throw new DamManagerNotImplementedException();
   }
 

    public Category createSubCategory(Connection connection, User user, Integer parentId, String path) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public Category findCategories(Connection connection, String path) throws DamManagerNotImplementedException {
        return findCategories(connection, path, true);
    }

    public Category findCategories(Connection connection, String path, boolean recursive) throws DamManagerNotImplementedException {
       throw new DamManagerNotImplementedException();
   }

    public Category findCategories(Connection connection, Integer id) throws DamManagerNotImplementedException {
       return findCategories(connection, id, true);
    }
    
    public Category findCategories(Connection connection, Integer id, boolean recursive) throws DamManagerNotImplementedException {
       throw new DamManagerNotImplementedException();
   }

    public DatabaseField[] getFields(Connection connection) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public DatabaseField[] getFields(Connection connection, String view) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public DatabaseQuery[] getQueries(Connection connection) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public Map<String, String> getPreviews(Connection connection, String view) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public Map<String, String> getLinks(Connection connection, String view) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public Map<String, String> getReferences(Connection connection, String view) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public void removeTemporaryFile(Path file) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public Object getFieldValue(FieldValue v, boolean json) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public Person userSingleSignOn(Connection connection, String username) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public Person getUser(Connection connection, String username, String password) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public boolean activateUser(Connection connection, String username, boolean activate) throws DamManagerNotImplementedException {
        throw new DamManagerNotImplementedException();
    }

    public void setQueries(HashMap<String, Query> queries) {
        this.queries = queries;
    }

    public void setViews(HashMap<String, View> views) {
        this.views = views;
    }

    public HashMap<String, View> getViews() {
        return views;
    }

    public View getView(String name) {
        return this.views.get(name);
    }

    public HashMap<String, Query> getQueries() {
        // TODO Auto-generated method stub
        return this.queries;
    }

    public Query getQuery(String name) {
        return this.queries.get(name);
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public HashMap<String, String> getParams() {
        // TODO Auto-generated method stub
        return this.params;
    }

    public String getParam(String name) {
        return this.params.get(name);
    }

    public HashMap<String, Connection> getConnections() {
        return connections;
    }

    public void setConnections(HashMap<String, Connection> connections) {
        this.connections = connections;
    }

    public Connection getConnection(String name) {
        return connections.get(name);
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public Path getTmpDir() {
        return tmpDir;
    }

    public void setTmpDir(Path tmpDir) {
        this.tmpDir = tmpDir;
    }

    protected Path makeTmpFile(InputStream in, String fileName) {
        Path result = null;
        try {
            result = tmpDir.resolve(fileName);
            byte[] fileData = Utilities.getBytesFromInputStream(in);
            in.close();
            try (OutputStream os = Files.newOutputStream(result)) {
                os.write(fileData);
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
