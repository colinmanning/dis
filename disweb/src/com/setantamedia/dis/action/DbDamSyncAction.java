package com.setantamedia.dis.action;

import com.setantamedia.fulcrum.DamManager;
import com.setantamedia.fulcrum.DamManagerNotImplementedException;
import com.setantamedia.fulcrum.actions.ActionProcessor;
import com.setantamedia.fulcrum.common.Connection;
import com.setantamedia.fulcrum.common.Query;
import com.setantamedia.fulcrum.common.SearchDescriptor;
import com.setantamedia.fulcrum.db.DbManager;
import com.setantamedia.fulcrum.ws.BaseServlet;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Colin Manning
 */
public class DbDamSyncAction extends ActionProcessor {

    private static Logger logger = Logger.getLogger(DbDamSyncAction.class);
    public final static String PARAM_DB_NAME = "dbName";
    public final static String PARAM_DB_QUERY = "dbQuery";
    public final static String PARAM_DAM_QUERY = "damQuery";
    public final static String PARAM_BIT_APPEND = "append";
    public final static String PARAM_DB_KEY_FIELD = "dbKeyField";
    public final static String PARAM_DAM_KEY_FIELD = "damKeyField";
    public final static String PARAM_DB_VIEW = "dbView";
    public final static String PARAM_DAM_VIEW = "damView";
    public final static String PARAM_BIT_DB_FIELD = "dbField";
    public final static String PARAM_BIT_REPLACE = "replace";
    public final static String PARAM_URL_DAM = "dam";
    public final static String PARAM_URL_DB = "db";
    private String dbName = null;
    private String dbConnectionName = null;
    private String damConnectionName = null;
    private String dbQueryName = null;
    private String damQueryName = null;
    private String dbKeyField = null;
    private String damKeyField = null;
    private DbManager dbManager = null;
    private DamManager damManager = null;
    private Connection damConnection = null;
    private Query damNamedQuery = null;
    private Query dbNamedQuery = null;
    private String damViewName = null;
    private String dbViewName = null;
    private ArrayList<FieldMap> fieldMaps = new ArrayList<>();

    private class FieldMap {

        private boolean append = false;
        private String dbField = null;
        private String damField = null;

        public FieldMap() {
        }

        public boolean isAppend() {
            return append;
        }

        public void setAppend(boolean append) {
            this.append = append;
        }

        public String getDbField() {
            return dbField;
        }

        public void setDbField(String dbField) {
            this.dbField = dbField;
        }

        public String getDamField() {
            return damField;
        }

        public void setDamField(String damField) {
            this.damField = damField;
        }
    }

    public DbDamSyncAction() {
        super();

    }

    @Override
    public void init() {
        super.init();
        // params should be ready
        dbName = params.get(PARAM_DB_NAME);
        dbQueryName = params.get(PARAM_DB_QUERY);
        damQueryName = params.get(PARAM_DAM_QUERY);
        dbKeyField = params.get(PARAM_DB_KEY_FIELD);
        damKeyField = params.get(PARAM_DAM_KEY_FIELD);
        damViewName = params.get(PARAM_DAM_VIEW);
        dbViewName = params.get(PARAM_DB_VIEW);

        dbManager = mainServer.getDatabase(dbName).getManager();
        damManager = dam.manager;

        damNamedQuery = damManager.getQuery(damQueryName);
        dbNamedQuery = dbManager.getQuery(dbQueryName);

        // get the field mappings
        String mapKey = PARAM_BIT_DB_FIELD + ":";
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (param.getKey().startsWith(mapKey)) {
                String[] dbBits = param.getKey().split(":");
                String[] damBits = param.getValue().split(":");
                FieldMap fieldMap = new FieldMap();
                fieldMap.setDbField(dbBits[1]);
                fieldMap.setDamField(damBits[1]);
                if (damBits.length > 2) {
                    fieldMap.setAppend(PARAM_BIT_APPEND.equals(damBits[2]));
                }
                fieldMaps.add(fieldMap);
            }
        }
    }

    @Override
    public JSONObject execute(HashMap<String, String> urlParams) {
        JSONObject result = new JSONObject();
        int processedCount = 0;
        int totalCount = 0;
        String damQuery = "";
        String dbQuery = "";
        damConnectionName = null;
        dbConnectionName = null;
        damConnection = null;
        boolean tmpConnection = false;
        try {
            String connUsername = null;
            String connPassword = null;
            Integer connPoolSize = 0;
            logger.info("Action '" + name + "'started");
            for (Map.Entry<String, String> entry : urlParams.entrySet()) {
                logger.info("   --- url param: " + entry.getKey() + " has value: " + entry.getValue());
                if (PARAM_URL_DAM.equals(entry.getKey())) {
                    damConnectionName = entry.getValue();
                    damConnection = dam.getConnection(damConnectionName);
                } else if (PARAM_URL_DB.equals(entry.getKey())) {
                    dbConnectionName = entry.getValue();
                } else if (BaseServlet.PARAMETER_CONNECTION_USERNAME.equals(entry.getKey())) {
                    connUsername = entry.getValue();
                } else if (BaseServlet.PARAMETER_CONNECTION_PASSWORD.equals(entry.getKey())) {
                    connPassword = entry.getValue();
                } else if (BaseServlet.PARAMETER_CONNECTION_POOLSIZE.equals(entry.getKey())) {
                    connPoolSize = new Integer(entry.getValue());
                }
            }
            if (dbConnectionName == null || damConnectionName == null || damConnection == null) {
                String errorMessage = "Cannot run action, invalid DAM and/or DB connection";
                logger.error(errorMessage);
                result.put("status", "FAILED");
                result.put("errorMessage", errorMessage);
                return result;
            }
            if (connUsername != null && connPassword != null) {
                if (damManager.openConnection(damConnection, connUsername, connPassword, connPoolSize)) {
                    tmpConnection = true;
                } else {
                    String errorMessage = "Cannot run action, cannot open DAM connection connection";
                    logger.error(errorMessage);
                    result.put("status", "FAILED");
                    result.put("errorMessage", errorMessage);
                    return result;
                }
            }
            HashMap<String, String> damQueryParams = new HashMap<>();
            HashSet<String> ignoreParams = new HashSet<>();
            damQuery = buildNamedQuery(damNamedQuery, ignoreParams, damQueryParams);
            result.put("actionName", this.getClass().getName());
            logger.info("   --- dam query: " + damQuery);
            logger.info("   --- dam view name: " + damViewName);
            SearchDescriptor dbSearchDescriptor = new SearchDescriptor();
            dbSearchDescriptor.setViewName(dbViewName);
            SearchDescriptor searchDescriptor = new SearchDescriptor();
            searchDescriptor.setViewName(damViewName);
            QueryResult qr = damManager.querySearch(damConnection, null, damQuery, searchDescriptor, Locale.getDefault());
            totalCount = qr.getRecords().length;
            logger.info("   --- dam query records returned: " + totalCount);
            for (Record damRecord : qr.getRecords()) {
                HashMap<String, String> dbQueryParams = new HashMap<>();
                String keyFieldValue = damRecord.getFieldValue(damKeyField).toString();
                if (keyFieldValue == null || "".equals(keyFieldValue)) {
                    // ignore null or empty values
                    continue;
                }
                logger.info("Processing DAM record with id: " + damRecord.getId());
                dbQueryParams.put(dbKeyField, keyFieldValue);
                dbQuery = buildNamedQuery(dbNamedQuery, ignoreParams, dbQueryParams);
                logger.info("      --- db query: " + dbQuery);
                logger.info("      --- db view name: " + dbViewName);
                QueryResult dbQr = dbManager.fetch(dbConnectionName, dbQuery, dbSearchDescriptor, null);
                logger.info("      --- db query records returned: " + dbQr.getRecords().length);
                if (dbQr.getRecords().length > 0) {
                    // actually should be only 1, but we will take the first one anyway, and log an issue
                    if (dbQr.getRecords().length > 1) {
                        logger.info("WARNING - Query: '" + dbQuery + "' returned more than one result, which is not good - maybe de-duping required");
                    }
                    Record dbRecord = dbQr.getRecords()[0];
                    HashMap<String, String> damFields = new HashMap<>();
                    for (FieldMap fieldMap : fieldMaps) {
                        String v = dbRecord.getFieldValue(fieldMap.getDbField()).toString();
                        if (fieldMap.isAppend()) {
                            String exstingV = damRecord.getFieldValue(fieldMap.getDamField()).toString();
                            v = exstingV + " " + v;
                        }
                        damFields.put(fieldMap.getDamField(), v);
                    }
                    if (!damManager.updateAssetData(damConnection, damRecord.getId(), null, damFields)) {
                        logger.error("Problem updating DAM record for key field: '" + damKeyField + "' with value '" + keyFieldValue + "'");
                    }
                    processedCount++;
                }
            }
            result.put("status", "OK");
        } catch (JSONException | DamManagerNotImplementedException | SQLException e) {
            logger.info("Problem with action: " + e.getMessage());
            try {
                result.put("status", "FAILED");
                result.put("errorMessage", e.getMessage());
            } catch (JSONException je) {
                // just ignore - should never get here
            }
            e.printStackTrace();
        } finally {
            try {
                if (tmpConnection) {
                    damManager.closeConnection(damConnection);
                }
                result.put(PARAM_DAM_QUERY, damNamedQuery);
                result.put(PARAM_DB_QUERY, dbNamedQuery);
                result.put("totalRecords", totalCount);
                result.put("processedRecords", processedCount);
            } catch (JSONException | DamManagerNotImplementedException e) {
                // just ignore - should never get here
            }
            logger.info("Action '" + name + "'done");
        }
        return result;
    }
}
