package com.setantamedia.fulcrum;

import com.setantamedia.fulcrum.common.Connection;
import com.setantamedia.fulcrum.common.Dam;
import com.setantamedia.fulcrum.common.*;
import com.setantamedia.fulcrum.common.Query;
import com.setantamedia.fulcrum.common.SortRule;
import com.setantamedia.fulcrum.config.Service;
import com.setantamedia.fulcrum.config.*;
import com.setantamedia.fulcrum.ws.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.FactoryConfigurationError;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 *
 * @author Colin Manning
 */
public class CoreServer {

   private final static Logger logger = Logger.getLogger(CoreServer.class);
   public final static String VERSION_NUMBER = "3.2.0.0";
   public final static String CONFIG_FILE = "dis-config.xml";
   public final static String LOG4J_CONFIG_FILE = "dis-log4j.xml";
   protected FulcrumConfig fulcrumConfig = null;
   protected Path tmpFolder = null;
   protected Path previewCacheFolder = null;
   protected static CoreServer instance = null;
   protected Dam dam = null;
   protected HashMap<String, Dam> dams = null;
   protected HashMap<String, Query> queries = null;
   protected String log4jConfigPath = null;
   protected String damAdminPassword = null;
   protected JAXBContext jaxbContext = null;
   protected static Unmarshaller unmarshaller = null;
   protected static FileSystem fs = FileSystems.getDefault();

   public CoreServer() {
   }

   public void setup(InputStream configFileStream, String log4jFile, String adminPassword) {
      dam = null;
      try {
         if (log4jFile != null) {
            log4jConfigPath = log4jFile;
         } else {
            log4jConfigPath = CONFIG_FILE;
         }
         Path log4jConfigFile = fs.getPath(log4jConfigPath);
         if (Files.exists(log4jConfigFile)) {
            DOMConfigurator.configure(log4jConfigPath);
         }
         this.damAdminPassword = adminPassword;

         processCoreConfig(configFileStream);
         if (fulcrumConfig.getUseDam() != null && !"".equals(fulcrumConfig.getUseDam())) {
            dam = dams.get(fulcrumConfig.getUseDam());
            if (dam != null) {
               logger.info("About to initialise the DAM Manager: " + dam.getName());
               dam.manager.init();
               dam.manager.setAdminPassword(this.damAdminPassword);
            }
            initStuff(fulcrumConfig, dam);
         } else {
            // Multiple DAMS
            for (Dam d : dams.values()) {
               logger.info("About to initialise the DAM Manager: " + d.getName());
               d.manager.init();
            }
         }
      } catch (Exception e) {
         logger.info("Problem creating CoreServer");
         e.printStackTrace();
      }

   }

   public CoreServer(InputStream configFileStream) {
      super();
      setup(configFileStream, null, null);
   }

   public static void main(String[] args) {
      try {
         logger.info("Hello from Fulcrum Core Server");
         String configFilePath = null;
         if (args.length > 0) {
            configFilePath = args[0];
         }
         if (configFilePath == null) {
            configFilePath = "conf/" + CONFIG_FILE;
         }
         Path configFile = fs.getPath(configFilePath);

         String log4jConfigPath = null;
         if (args.length > 1) {
            log4jConfigPath = args[1];
         }
         if (log4jConfigPath == null) {
            log4jConfigPath = "conf/" + LOG4J_CONFIG_FILE;
         }
         Path log4jConfigFile = fs.getPath(log4jConfigPath);
         if (Files.exists(log4jConfigFile)) {
            DOMConfigurator.configure(log4jConfigPath);
         }

         if (Files.exists(configFile)) {
            logger.info("Config file is: " + configFile.toString());
            InputStream configFileStream = Files.newInputStream(configFile, StandardOpenOption.READ);
            instance = new CoreServer(configFileStream);
            if (instance != null) {
               instance.start();
            }
         } else {
            logger.error("Config file: " + configFile.toString() + " does not exist!");
         }
      } catch (FactoryConfigurationError | IOException e) {
         e.printStackTrace();
      }
   }

   public void stop() {
      try {
         if (instance != null) {
            if (instance.dam != null && instance.dam.manager != null) {
               instance.dam.manager.terminate();
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   protected final void initStuff(FulcrumConfig fulcrumConfig, Dam dam) throws Exception {
      try {
         previewCacheFolder = fs.getPath(fulcrumConfig.getPreviewCacheFolder());
         if (fulcrumConfig.isStartWebservice()) {

            Path rootDir = fs.getPath(fulcrumConfig.getDocroot());
            if (!Files.exists(rootDir)) {
               Files.createDirectories(rootDir);
            }
         }
         // Do not start it here, do it in calling code as other stuff may have to happen
         //start();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void start() {
      try {
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   protected final void processCoreConfig(InputStream configFileStream) throws Exception {
      try {
         jaxbContext = JAXBContext.newInstance("com.setantamedia.fulcrum.config");
         unmarshaller = jaxbContext.createUnmarshaller();
         fulcrumConfig = (FulcrumConfig) unmarshaller.unmarshal(configFileStream);

         tmpFolder = fs.getPath(fulcrumConfig.getTmpFolder());
         if (!Files.exists(tmpFolder)) {
            Files.createDirectories(tmpFolder);
            logger.info("Creating temporary file cache folder: " + tmpFolder.toString());
         }
         previewCacheFolder = fs.getPath(fulcrumConfig.getPreviewCacheFolder());
         if (!Files.exists(previewCacheFolder)) {
            Files.createDirectories(previewCacheFolder);
            logger.info("Creating preview cache folder: " + previewCacheFolder.toString());
         }
         queries = processQueries(fulcrumConfig.getQueries());

         // ok to setup DAMs and databases now
         dams = processDams(fulcrumConfig.getDams());
      } catch (JAXBException | IOException e) {
         e.printStackTrace();
      }
   }

   protected HashMap<String, Dam> processDams(DamList dams) {
      HashMap<String, Dam> result = new HashMap<>();
      if (dams == null) {
         return result;
      }
      try {
         for (com.setantamedia.fulcrum.config.Dam damConfig : dams.getDam()) {
            Dam aDam = processDam(damConfig);
            if (aDam != null && aDam.getName() != null) {
               result.put(aDam.getName(), aDam);
            } else {
               logger.error("Invalid dam definition in config file");
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   @SuppressWarnings("rawtypes")
   protected Dam processDam(com.setantamedia.fulcrum.config.Dam dam) {
      Dam result = new Dam();
      try {
         result.setName(dam.getName());
         Class managerClass = Class.forName(dam.getManagerClass());
         result.manager = (DamManager) managerClass.newInstance();
         result.manager.setConfig(fulcrumConfig);
         result.manager.setServerPrefix(fulcrumConfig.getServerPrefix());
         result.manager.setBaseUrl(dam.getBaseUrl());

         //TODO this is redundant, but some DAMManagers may need the connection information at this stage - to be refactored
         result.manager.setConnections(result.connections = processConnections(result, dam.getConnections()));
         HashMap<String, View> configViews = new HashMap<>();
         for (View v : dam.getViews().getView()) {
            configViews.put(v.getName(), v);
         }
         result.manager.setViews(configViews);
         result.manager.setQueries(queries);
         if (result.getName() == null) {
            logger.error("Dam with no name in config file - ignoring");
            result = null;
         }
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
         e.printStackTrace();
      }
      return result;
   }

   public HashMap<String, Connection> processConnections(Dam connectionDam, ConnectionList connections) {
      HashMap<String, Connection> result = new HashMap<>();
      if (connections == null) {
         return result;
      }
      try {
         for (com.setantamedia.fulcrum.config.Connection connectionConfig : connections.getConnection()) {
            Connection connection = processConnection(dam, connectionConfig);
            if (connection != null && connection.getName() != null) {
               result.put(connection.getName(), connection);
            } else {
               logger.error("Invalid connection definition in config file");
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   public Connection processConnection(Dam connectionDam, com.setantamedia.fulcrum.config.Connection connection) {
      Connection result = new Connection();
      try {
         result.setDam(connectionDam);
         result.setName(connection.getName());
         result.setServer(connection.getServer());
         result.setUsername(connection.getUser());
         result.setPassword(connection.getPassword());
         result.setReadOnly(connection.isReadOnly());
         result.setPoolSize(connection.getPoolSize());
         ParamList paramList = connection.getParams();
         HashMap<String, String> params = new HashMap<>();
         if (paramList != null) {
            for (Param param : paramList.getParam()) {
               params.put(param.getName(), param.getValue());
            }
         }
         result.setParams(params);

         result.setDatabase(connection.getDatabase());
         if (result.getName() == null) {
            logger.error("Connection with no name in config file - ignoring");
            result = null;
         }

         // set up service access rules
         AccessControl accessControl = connection.getAccessControl();
         if (accessControl == null) {
            // assume fully open connection
            accessControl = new AccessControl();
            accessControl.setPublic(true);
         }
         if (accessControl.isPublic()) {
            // fully, open so enable all services
            com.setantamedia.fulcrum.common.Service service = new com.setantamedia.fulcrum.common.Service();
            service.setName(SearchServlet.SERVICE_NAME);
            service.setPublicService(true);
            result.addService(service);

            service = new com.setantamedia.fulcrum.common.Service();
            service.setName(PreviewServlet.SERVICE_NAME);
            service.setPublicService(true);
            result.addService(service);

            service = new com.setantamedia.fulcrum.common.Service();
            service.setName(FileServlet.SERVICE_NAME);
            service.setPublicService(true);
            result.addService(service);

            service = new com.setantamedia.fulcrum.common.Service();
            service.setName(AdminServlet.SERVICE_NAME);
            service.setPublicService(true);
            result.addService(service);

            service = new com.setantamedia.fulcrum.common.Service();
            service.setName(DataServlet.SERVICE_NAME);
            service.setPublicService(true);
            result.addService(service);

            service = new com.setantamedia.fulcrum.common.Service();
            service.setName(DbServlet.SERVICE_NAME);
            service.setPublicService(true);
            result.addService(service);

         } else if (accessControl.getService() != null) {
            for (Service configService : accessControl.getService()) {
               com.setantamedia.fulcrum.common.Service service = new com.setantamedia.fulcrum.common.Service();
               service.setName(configService.getName());
               if (configService.isPublic()) {
                  service.setPublicService(true);
               } else {
                  String network = configService.getNetwork();
                  if (network != null) {
                     String[] ips = network.split(",");
                     for (String ip : ips) {
                        try {
                           IPMatcher ipMatcher = new IPMatcher(ip);
                           service.addIpMatcher(ipMatcher);
                        } catch (IPMatcherException e) {
                           logger.error("Invalid ip address specification in config file: " + ip);
                           e.printStackTrace();
                        }
                     }
                  }

                  String users = configService.getUsers();
                  if (users != null) {
                     String[] userNames = users.split(",");
                     for (String user : userNames) {
                        service.addUser(user);
                     }
                  }

                  String roles = configService.getRoles();
                  if (roles != null) {
                     String[] roleNames = roles.split(",");
                     for (String role : roleNames) {
                        service.addRole(role);
                     }
                  }
               }
               result.addService(service);
            }
         } else {
            // Access Control with no services but not publin - hmmm
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   protected HashMap<String, Query> processQueries(QueryList queries) {
      HashMap<String, Query> result = new HashMap<>();
      if (queries == null) {
         return result;
      }
      try {
         for (com.setantamedia.fulcrum.config.Query queryConfig : queries.getQuery()) {
            Query query = processQuery(queryConfig);
            if (query != null && query.getName() != null) {
               result.put(query.getName(), query);
            } else {
               logger.error("Invalid query definition in config file");
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   @SuppressWarnings("rawtypes")
   protected Query processQuery(com.setantamedia.fulcrum.config.Query query) {
      Query result = new Query();
      try {
         result.setName(query.getName());
         result.setText(query.getText());
         com.setantamedia.fulcrum.config.SortRule sortRuleConfig = query.getSortRule();
         if (sortRuleConfig != null) {
            SortRule sortRule = new SortRule();
            sortRule.setFieldName(sortRuleConfig.getField());
            String direction = sortRuleConfig.getDirection();
            if (direction != null) {
               sortRule.setDirection(direction);
            }
         }
         if (result.getName() == null) {
            logger.error("Query with no name in config file - ignoring");
            result = null;
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   public HashMap<String, Dam> getDams() {
      return dams;
   }

   public void setDams(HashMap<String, Dam> dams) {
      this.dams = dams;
   }

   public FulcrumConfig getFulcrumConfig() {
      return fulcrumConfig;
   }

   public void setFulcrumConfig(FulcrumConfig fulcrumConfig) {
      this.fulcrumConfig = fulcrumConfig;
   }

   public String getLog4jConfigPath() {
      return log4jConfigPath;
   }

   public void setLog4jConfigPath(String log4jConfigPath) {
      this.log4jConfigPath = log4jConfigPath;
   }

   public Path getPreviewCacheFolder() {
      return previewCacheFolder;
   }

   public void setPreviewCacheFolder(Path previewCacheFolder) {
      this.previewCacheFolder = previewCacheFolder;
   }

   public HashMap<String, Query> getQueries() {
      return queries;
   }

   public void setQueries(HashMap<String, Query> queries) {
      this.queries = queries;
   }

   public Path getTmpFolder() {
      return tmpFolder;
   }

   public void setTmpFolder(Path tmpFolder) {
      this.tmpFolder = tmpFolder;
   }

   public Dam getDam() {
      return dam;
   }

   public void setDam(Dam dam) {
      this.dam = dam;
   }
}
