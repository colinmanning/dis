package com.setantamedia.dis.service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.setantamedia.dis.service.requests.LoginRequest;
import com.setantamedia.fulcrum.AdvancedServer;
import com.setantamedia.fulcrum.common.Database;
import com.setantamedia.fulcrum.db.DbManager;
import com.setantamedia.fulcrum.db.DbSessionData;

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

   public final static String PARAMETER_USERNAME = "username";
   public final static String PARAMETER_PASSWORD = "password";
   public final static String PARAMETER_NAME = "name";
   public final static String PARAMETER_SESSIONID = "sessionid";

   public final static String STATUS_OK = "OK";
   public final static String STATUS_NOT_AUTHORISED = "NOT_AUTHORISED";

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
            sessions.put(result.getId(), result);
         }
      } catch (Exception e) {
         logger.error("Problem handling login request for user: '" + loginRequest.getUsername() + "'");
         e.printStackTrace();
      }
      return result;
   }

   public void doLogout(String sessionId) {
      DisSession disSession = sessions.get(sessionId);
      if (disSession != null) {
         disDbManager.logout(disSession.getDbSessionData());
      }
      sessions.remove(sessionId);
   }

   public void doRunAction(String sessionId, String actionName) {
      DisSession disSession = sessions.get(sessionId);
      if (disSession != null && disSession.isValid()) {
      } else {
         logger.error("Invalid DIS session, cannot run action: '"+actionName+"'");
      }
     
   }
}
