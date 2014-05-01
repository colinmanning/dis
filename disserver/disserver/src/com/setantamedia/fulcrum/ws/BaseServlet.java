/**
 *
 */
package com.setantamedia.fulcrum.ws;

import com.setantamedia.fulcrum.CoreServer;
import com.setantamedia.fulcrum.common.*;
import com.setantamedia.fulcrum.config.FulcrumConfig;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public abstract class BaseServlet extends HttpServlet {

   private final static Logger logger = Logger.getLogger(BaseServlet.class);
   public final static String DIS_DB_NAME = "dis-db";
   public final static String DIS_DB_CONNECTION_NAME = "main";

   public final static String UTF_8 = "UTF-8";
   public final static String FORMAT_JPG = "jpg";
   public final static String FORMAT_PNG = "png";
   public final static String FULCRUM_PREFIX = "fulcrum_";
   public final static String PARAMETER_PAGE = "page";
   public final static String PARAMETER_PAGE_SIZE = "pageSize";
   public final static String PARAMETER_USERNAME = "username";
   public final static String PARAMETER_PASSWORD = "password";
   public final static String PARAMETER_CONNECTION = "connection";
   public final static String PARAMETER_READ_ONLY = "readonly";
   public final static String PARAMETER_CONNECTION_USERNAME = "conn_username";
   public final static String PARAMETER_CONNECTION_PASSWORD = "conn_password";
   public final static String PARAMETER_CONNECTION_POOLSIZE = "conn_poolsize";
   public final static String PARAMETER_SESSION = "session";
   public final static String PARAMETER_ID = "id";
   public final static String PARAMETER_GUID = "guid";
   public final static String PARAMETER_RECORD_ID = "recordid";
   public final static String PARAMETER_CATEGORY_ID = "categoryid";
   public final static String PARAMETER_CATEGORY_IDS = "categoryids";
   public final static String PARAMETER_PARENT_ID = "parentid";
   public final static String PARAMETER_NAME = "name";
   public final static String PARAMETER_PARENT = "parent";
   public final static String PARAMETER_OUTPUT = "output";
   public final static String PARAMETER_FORMAT = "format";
   public final static String PARAMETER_OFFSET = "offset";
   public final static String PARAMETER_COUNT = "count";
   public final static String PARAMETER_VIEW = "view";
   public final static String PARAMETER_QUERY = "query";
   public final static String PARAMETER_QUERY_NAME = "queryname";
   public final static String PARAMETER_FINDMANY = "findmany";
   public final static String PARAMETER_FILTER = "filter";
   public final static String PARAMETER_QUERYNAME = "queryname";
   public final static String PARAMETER_RECURSIVE = "recursive";
   public final static String PARAMETER_SORTFIELD = "sort";
   public final static String PARAMETER_SORTDIRECTION = "direction";
   public final static String PARAMETER_PROFILE = "profile";
   public final static String PARAMETER_ITEM = "item";
   public final static String PARAMETER_PATH = "path";
   public final static String PARAMETER_TEXT = "text";
   public final static String PARAMETER_HTML = "html";
   public final static String PARAMETER_ACTION = "action";
   public final static String PARAMETER_FLAVOUR = "flavour";
   public final static String PARAMETER_VERSION = "version";
   public final static String PARAMETER_FIXED = "fixed";
   public final static String PARAMETER_FORCE = "force";
   public final static String PARAMETER_CLEARCACHE = "clearcache";
   public final static String PARAMETER_MAXSIZE = "maxsize";
   public final static String PARAMETER_SIZE = "size";
   public final static String PARAMETER_ROTATE = "rotate";
   public final static String PARAMETER_TOP = "top";
   public final static String PARAMETER_LEFT = "left";
   public final static String PARAMETER_WIDTH = "width";
   public final static String PARAMETER_HEIGHT = "height";
   public final static String PARAMETER_COMPRESSION = "compression";
   public final static String PARAMETER_TABLE = "table";
   public final static String PARAMETER_FROM = "from";
   public final static String PARAMETER_TO = "to";
   public final static String PARAMETER_LOCATION = "location";
   public final static String PARAMETER_DESTINATION = "destination";
   public final static String PARAMETER_DESTINATION_FOLDER = "destination-folder";
   public final static String PARAMETER_SAVEAS = "saveas";
   public final static String PARAMETER_FOLDER_PATH = "folderpath";
   public final static String PARAMETER_CONTROL_FILE = "controlfile";
   public final static String PARAMETER_ACCESS_CODE = "accesscode";
   public final static String PARAMETER_CONDITION = "condition";
   public final static String PARAMETER_FIELD = "field";
   public final static String PARAMETER_AMOUNT = "amount";
   public final static String PARAMETER_XML = "xml";
   public final static String PARAMETER_JSON = "json";
   public final static String PARAMETER_RSS = "rss";
   public final static String PARAMETER_INHERIT_CATEGORY_FIELDS = "inheritcateogoryfields";
   public final static String PARAMETER_FIELD_KEY = "fieldkey";
   public final static String PARAMETER_FIELD_VALUE = "fieldvalue";
   public final static String PARAM_PROJECTNAME = "projectname";
   public final static String PARAMETER_CALLBACK = "callback";
   public final static String DIRECTION_ASCENDING = "ascending";
   public final static String DIRECTION_DESCENDING = "descending";
   public final static String FORMAT_CSHARP = "csharp";
   public final static String FORMAT_JSON = "json";
   public final static String FORMAT_XML = "xml";

   protected final static String ADDRESS_LOCALHOST = "localhost";
   protected final static String ADDRESS_LOOOPBACK = "127.0.0.1";
   protected String callback = null;
   protected FulcrumConfig fulcrumConfig = null;
   protected Dam dam = null;
   protected HashMap<String, Connection> connections = new HashMap<>();
   protected String serviceName = null;
   protected String connectionName = null;
   protected String operationName = null;
   protected Path tmpFolder = null;
   protected int prefixLength = 0;
   protected CoreServer mainServer;
   protected HashMap<String, String> configParams = null;
   protected boolean debugMode = false;
   protected FileSystem fs = FileSystems.getDefault();

   protected boolean isLocalhost(String ip) {
      return (ADDRESS_LOCALHOST.equals(ip) || ADDRESS_LOOOPBACK.equals(ip));
   }

   /**
    * Returns true if the request comes from a permitted remote address
    * localhost requests are always allowed
    * 
    * @param request
    * @return
    */
   protected boolean requestAllowed(String ip) {
      boolean result = false;
      if (isLocalhost(ip)) {
         result = true;
      } else {
         // check if access for service is allowed
         Connection con = dam.connections.get(connectionName);
         if (con == null) {
            logger.error("Canot find connection with name: '" + connectionName + "'");
         } else {
            Service service = con.getService(serviceName);
            if (service != null) {
               if (service.isPublicService()) {
                  result = true;
               } else if (service.isPublicService()) {
                  result = true;
               } else {
                  for (IPMatcher ipm : service.getIps()) {
                     try {
                        if (ipm.match(ip)) {
                           result = true;
                           break;
                        }
                     } catch (IPMatcherException e) {
                        logger.error("Problem validating ip address: " + ip);
                        e.printStackTrace();
                     }
                  }
               }
            }
            if (!result) {
               logger.info("Unauthorized attempt to access service '" + service + "' from IP address: " + ip);
            }
         }
      }
      return result;
   }

   public CoreServer getMainServer() {
      return mainServer;
   }

   public void setMainServer(CoreServer mainServer) {
      this.mainServer = mainServer;
   }

   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      try {
         prefixLength = FULCRUM_PREFIX.length();
         if (mainServer == null) {
            // e.g. if deployed to app server, and not running embedded - get
            // from Servlet Context
            ServletContext context = config.getServletContext();

            // make sure to use setters, in case of required side effects (e.g.
            // setting up tmp folder)
            logger.info("getting mainServer from servlet context");
            mainServer = (CoreServer) context.getAttribute(FulcrumServletContextListener.MAIN_SERVER);
            fulcrumConfig = mainServer.getFulcrumConfig();
            dam = mainServer.getDam();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   protected String[] getPathElements(HttpServletRequest request) {
      String[] result = null;
      String pathInfo = request.getPathInfo();
      if (pathInfo != null) {
         String decodedURL = null;
         try {
            decodedURL = URLDecoder.decode(pathInfo, "UTF-8");
            result = decodedURL.split("/");
         } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
         }
      }
      return result;
   }

   public void setFulcrumConfig(FulcrumConfig disConfig) {
      try {
         this.fulcrumConfig = disConfig;
         if (this.fulcrumConfig != null) {
            tmpFolder = fs.getPath(this.fulcrumConfig.getTmpFolder());
            if (!Files.exists(tmpFolder)) {
               Files.createDirectories(tmpFolder);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void setDam(Dam dam) {
      this.dam = dam;
   }

   public HashMap<String, String> getConfigParams() {
      return configParams;
   }

   public void setConfigParams(HashMap<String, String> configParams) {
      this.configParams = configParams;
   }

   public HashMap<String, Connection> getConnections() {
      return connections;
   }

   public void setConnections(HashMap<String, Connection> connections) {
      this.connections = connections;
   }

   public void addConnection(String name, Connection connection) {
      connections.put(name, connection);
   }

   @Override
   public String getServletInfo() {
      return "Fulcrum Base Servlet";
   }
}
