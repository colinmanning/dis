package com.setantamedia.dis.service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.setantamedia.dis.service.requests.LoginRequest;
import com.setantamedia.fulcrum.AdvancedServer;
import com.setantamedia.fulcrum.DamManager;
import com.setantamedia.fulcrum.actions.ActionProcessor;
import com.setantamedia.fulcrum.common.Connection;
import com.setantamedia.fulcrum.common.Dam;
import com.setantamedia.fulcrum.common.Database;
import com.setantamedia.fulcrum.common.DatabaseField;
import com.setantamedia.fulcrum.common.StringListValue;
import com.setantamedia.fulcrum.config.View;
import com.setantamedia.fulcrum.db.DbManager;
import com.setantamedia.fulcrum.db.DbSessionData;
import com.setantamedia.fulcrum.workflow.WorkflowManager;
import com.setantamedia.fulcrum.ws.AdminServlet;
import com.setantamedia.fulcrum.ws.BaseServlet;

/**
 * 
 * Run DIS as a standalone service, using DIS config file etc.
 * 
 * @author Colin Manning
 * 
 */
public class DisService {

   private static Logger logger = Logger.getLogger(DisService.class);

   public final static String DIS_DB_NAME = "dis-db";
   public final static String DIS_DB_CONNECTION_NAME = "main";

   public final static String MESSAGE_READY = "READY.";
   public final static String OPERATION_LOGIN = "LOGIN";
   public final static String OPERATION_LOGOUT = "LOGOUT";
   public final static String OPERATION_RUN_ACTION = "RUN_ACTION";
   public final static String OPERATION_SEARCH = "SEARCH";
   public final static String OPERATION_PREVIEW = "PREVIEW";
   public final static String OPERATION_DESCRIBE = "DESCRIBE";

   public final static String PARAMETER_USERNAME = "username";
   public final static String PARAMETER_PASSWORD = "password";
   public final static String PARAMETER_NAME = "name";
   public final static String PARAMETER_SESSIONID = "sessionid";
   public final static String PARAMETER_STATUS = "status";
   public final static String PARAMATER_RESULTS = "results";

   public final static String PARAMETER_VIEW = BaseServlet.PARAMETER_VIEW;
   public final static String PARAMETER_FORMAT = BaseServlet.PARAMETER_FORMAT;
   public final static String PARAMETER_CONNECTION = BaseServlet.PARAMETER_CONNECTION;
   public final static String PARAMETER_PATH = BaseServlet.PARAMETER_PATH;
   public final static String PARAMETER_OFFSET = BaseServlet.PARAMETER_OFFSET;
   public final static String PARAMETER_COUNT = BaseServlet.PARAMETER_COUNT;

   public final static String FORMAT_JSON = BaseServlet.FORMAT_JSON;
   
   public final static String JSON_FIELD_KEY_NAME = AdminServlet.JSON_FIELD_KEY_NAME;
   public final static String JSON_FIELD_KEY_SIMPLE_NAME = AdminServlet.JSON_FIELD_KEY_SIMPLE_NAME;
   public final static String JSON_FIELD_KEY_DISPLAY_NAME = AdminServlet.JSON_FIELD_KEY_DISPLAY_NAME;
   public final static String JSON_FIELD_KEY_CORE_FIELD = AdminServlet.JSON_FIELD_KEY_CORE_FIELD;
   public final static String JSON_FIELD_KEY_GUID = AdminServlet.JSON_FIELD_KEY_GUID;
   public final static String JSON_FIELD_KEY_DATA_TYPE = AdminServlet.JSON_FIELD_KEY_DATA_TYPE;
   public final static String JSON_FIELD_VIEW_NAMES = AdminServlet.JSON_FIELD_VIEW_NAMES;
   public final static String JSON_FIELD_KEY_VALUE_INTERPRETATION = AdminServlet.JSON_FIELD_KEY_VALUE_INTERPRETATION;
   public final static String JSON_FIELD_KEY_TABLE_COLUMNS = AdminServlet.JSON_FIELD_KEY_TABLE_COLUMNS;
   public final static String JSON_FIELD_KEY_LIST_VALUES = AdminServlet.JSON_FIELD_KEY_LIST_VALUES;
   public final static String JSON_FIELD_KEY_LIST_MULTI_SELECT = AdminServlet.JSON_FIELD_KEY_LIST_MULTI_SELECT;


   public final static String STATUS_OK = "OK";
   public final static String STATUS_ERROR = "ERROR";
   public final static String STATUS_NOT_AUTHORISED = "NOT_AUTHORISED";
   public final static String STATUS_BAD_FORMAT = "BAD_FORMAT";

   public final static String PROPERTY_PORT = "port";
   public final static String PROPERTY_DIS_CONFIG_FILE = "disConfigFile";
   public final static String PROPERTY_LOG4J_CONFIG_FILE = "log4jConfigFile";
   public final static String PROPERTY_DAM_ADMIN_PASSWORD = "damAdminPassword";
   private static DisService disService = null;
   private String configFilePath = null;
   private String logConfigFilePath = null;
   private String damAdminPassword = null;
   private String port = null;
   private boolean ready = false;
   private AdvancedServer server = null;
   private HashMap<String, Database> databases = null;
   private DbManager disDbManager = null;
   private HashMap<String, DisSession> sessions = null;
   private WorkflowManager workflowManager = null;
   private Dam dam = null;

   public DisService() {

   }

   /**
    * @param args
    *           Only argument is a properties file
    */
   public static void main(String[] args) {
      disService = new DisService();
      disService.init(args);
      disService.start();
   }

   public void init(String[] args) {
      try {
         ready = false;
         InputStream propsFile = new FileInputStream(args[0]);
         Properties properties = new Properties();
         properties.load(propsFile);
         configFilePath = properties.getProperty(PROPERTY_DIS_CONFIG_FILE);
         logConfigFilePath = properties.getProperty(PROPERTY_LOG4J_CONFIG_FILE);
         damAdminPassword = properties.getProperty(PROPERTY_DAM_ADMIN_PASSWORD);
         port = properties.getProperty(PROPERTY_PORT);
         InputStream configFileStream = new FileInputStream(configFilePath);
         sessions = new HashMap<>();
         server = new AdvancedServer();
         server.setup(configFileStream, logConfigFilePath, damAdminPassword);
         databases = server.getDatabases();
         disDbManager = databases.get(DIS_DB_NAME).getManager();
         workflowManager = server.getWorkflowManager();
         dam = server.getDam();

         logger.info("Welcome to DIS Server");
         // logger.info("server class name is: "+serverClassName);
         logger.info("server class via inspection is; " + server.getClass().toString());
         server.start();
         logger.info("temporary folder is: " + server.getTmpFolder());
         logger.info("previewcache folder is: " + server.getPreviewCacheFolder());
         logger.info("DIS Server initialised ok");
         ready = true;
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public void start() {
      System.out.println("Java Native Library Path: '"+System.getProperty("java.library.path")+"'");
      logger.info("Java Native Library Path: '"+System.getProperty("java.library.path")+"'");
      if (!ready) {
         logger.error("initialisation did not complete, cannot start");
      }
      logger.info("DIS Server starting on port: " + port);
      ServerSocket listener = null;
      try {
         listener = new ServerSocket(Integer.valueOf(port));
         boolean running = true;
         while (running) {
            try {
               new DisRequestHandler(this, listener.accept()).start();
            } catch (Exception e) {
               running = false;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         try {
            listener.close();
         } catch (Exception se) {
            // just ignore
         }
         disService.stop();
      }
   }

   public void stop() {
      logger.info("DIS Server stopped");
   }

   // Now the worker methods

   public DisSession doLogin(LoginRequest loginRequest) {
      DisSession result = null;
      try {
         DbSessionData sessionData = disDbManager.login(DIS_DB_NAME, DIS_DB_CONNECTION_NAME, loginRequest.getUsername(), loginRequest.getPassword());
         if (sessionData != null) {
            result = new DisSession();
            result.setUsername(loginRequest.getUsername());
            result.setDbSessionData(sessionData);
            sessions.put(result.getId(), result);
         }
      } catch (Exception e) {
         logger.error("Problem handling login request for user: '" + loginRequest.getUsername() + "'");
         e.printStackTrace();
      }
      return result;
   }

   public String doLogout(String sessionId) {
      String result = STATUS_ERROR;
      DisSession disSession = sessions.get(sessionId);
      if (disSession != null) {
         try {
            disDbManager.logout(disSession.getDbSessionData());
            result = STATUS_OK;
         } catch (Exception le) {
            // just ignore - error status will be returned
         }
      }
      sessions.remove(sessionId);
      return result;
   }

   public JSONObject doRunAction(String sessionId, String actionName, HashMap<String, String> params) {
      JSONObject result = new JSONObject();
      try {
         result.put(PARAMETER_STATUS, STATUS_ERROR);
         DisSession disSession = sessions.get(sessionId);
         if (disSession != null && disSession.isValid()) {
            ActionProcessor actionProcessor = workflowManager.getActionProcessor(actionName);
            if (actionProcessor != null) {
               JSONObject actionResults = actionProcessor.execute(params);
               if (actionResults != null) {
                  result.put(PARAMATER_RESULTS, actionResults);
               } else {
                  result.put(PARAMATER_RESULTS, new JSONObject());
               }
            }
            result.put(PARAMETER_STATUS, STATUS_OK);
         } else {
            logger.error("Invalid DIS session, cannot run action: '" + actionName + "'");
            result.put(PARAMETER_STATUS, STATUS_NOT_AUTHORISED);
         }
      } catch (JSONException e) {
         e.printStackTrace();
      }
      return result;
   }

   public JSONObject doDescribe(String sessionId, String connectionName, String viewName, String outputFormat) {
      JSONObject result = new JSONObject();
      try {
         result.put(PARAMETER_STATUS, STATUS_ERROR);
         String fmt = FORMAT_JSON;
         if (outputFormat != null && !"".equals(outputFormat)) {
            fmt = outputFormat;
         }
         if (viewName != null && !"".equals(viewName)) {
            if (FORMAT_JSON.equals(fmt)) {
               HashMap<String, Object> allFields = describeAsJson(dam.connections.get(connectionName), viewName);
               if (allFields != null && allFields.size() > 0) {
                  result.put(PARAMATER_RESULTS, new JSONObject(allFields));
                  result.put(PARAMETER_STATUS, STATUS_OK);
               } else {
                  result.put(PARAMETER_STATUS, STATUS_BAD_FORMAT);
                  result.put(PARAMATER_RESULTS, new JSONObject());                 
               }
            }
         } else {
            if (FORMAT_JSON.equals(fmt)) {
               HashMap<String, Map<String, Object>> allViewFields = describeAllAsJson(dam.manager, dam.connections.get(connectionName));
               result.put(PARAMATER_RESULTS, new JSONObject(allViewFields));
               result.put(PARAMETER_STATUS, STATUS_OK);
            } else {
               result.put(PARAMETER_STATUS, STATUS_BAD_FORMAT);
               result.put(PARAMATER_RESULTS, new JSONObject());               
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   /*
    * Utilities
    */
   private HashMap<String, Map<String, Object>> describeAllAsJson(DamManager manager, Connection connection) throws Exception {
      HashMap<String, Map<String, Object>> result = new HashMap<>();
      for (View view : manager.getViews().values()) {
         result.put(view.getName(), describeAsJson(connection, view.getName()));
      }
      return result;
   }

   private HashMap<String, Object> describeAsJson(Connection connection, String viewName) throws Exception {
      HashMap<String, Object> result = null;
      DatabaseField[] fields = dam.manager.getFields(connection, viewName);
      if (fields != null) {
         List<JSONObject> jsonFields = new ArrayList<>();
         result = new HashMap<>();
         for (DatabaseField field : fields) {
            HashMap<String, Object> fieldMap = new HashMap<>();
            fieldMap.put(JSON_FIELD_KEY_NAME, field.getName());
            fieldMap.put(JSON_FIELD_KEY_SIMPLE_NAME, field.getSimpleName());
            fieldMap.put(JSON_FIELD_KEY_DISPLAY_NAME, field.getDisplayName());
            fieldMap.put(JSON_FIELD_KEY_CORE_FIELD, field.getCoreField());
            fieldMap.put(JSON_FIELD_KEY_GUID, field.getGuid());
            fieldMap.put(JSON_FIELD_KEY_DATA_TYPE, field.getDataType());
            fieldMap.put(JSON_FIELD_KEY_VALUE_INTERPRETATION, field.getValueInterpretation());
            if (field.isSelect()) {
               StringListValue[] slv = field.getListValues();
               JSONObject[] lvs = new JSONObject[slv.length];
               int i = 0;
               for (StringListValue v : slv) {
                  lvs[i++] = new JSONObject(v);
               }
               fieldMap.put(JSON_FIELD_KEY_LIST_VALUES, new JSONArray(lvs));
            }
            fieldMap.put(JSON_FIELD_KEY_LIST_MULTI_SELECT, field.isMultiSelect());
            jsonFields.add(new JSONObject(fieldMap));
         }
         result.put("fields", new JSONArray(jsonFields));

         // previews, links and references
         result.put("previews", new JSONObject(dam.manager.getPreviews(connection, viewName)));
         result.put("links", new JSONObject(dam.manager.getLinks(connection, viewName)));
         result.put("references", new JSONObject(dam.manager.getReferences(connection, viewName)));
         result.put("name", viewName);
      }
      return result;
   }
}
