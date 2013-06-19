package com.setantamedia.fulcrum;

import com.setantamedia.fulcrum.actions.ActionProcessor;
import com.setantamedia.fulcrum.common.Dam;
import com.setantamedia.fulcrum.common.Database;
import com.setantamedia.fulcrum.common.Location;
import com.setantamedia.fulcrum.config.*;
import com.setantamedia.fulcrum.db.DbManager;
import com.setantamedia.fulcrum.locationmonitor.FileProcessor;
import com.setantamedia.fulcrum.locationmonitor.LocationMonitor;
import com.setantamedia.fulcrum.workflow.WorkflowManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.FactoryConfigurationError;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

@SuppressWarnings("CallToThreadDumpStack")
public class AdvancedServer extends CoreServer {

    private final static Logger logger = Logger.getLogger(AdvancedServer.class);
    protected Database database = null;
    protected HashMap<String, Database> databases = null;
    protected HashMap<String, Location> locations = null;
    protected HashMap<String, Action> actions = null;
    protected HashMap<String, Workflow> workflows = null;
    protected WorkflowManager workflowManager = null;

    public AdvancedServer() {
    }

    public AdvancedServer(InputStream configFileStream) {
        super();
        setup(configFileStream, null, null);
    }

    @Override
    public final void setup(InputStream configFileStream, String log4jFile, String adminPassword) {
        dam = null;
        try {
            logger.info("log4j file path is: "+log4jFile);
            if (log4jFile != null) {
                log4jConfigPath = log4jFile;
            } else {
                log4jConfigPath = CONFIG_FILE;
            }
            Path log4jConfigFile = fs.getPath(log4jConfigPath);
            if (Files.exists(log4jConfigFile)) {
                logger.info("configuring log4j with file: "+log4jConfigFile.toString());
                DOMConfigurator.configure(log4jConfigPath);
            }
            this.damAdminPassword = adminPassword;
            workflowManager = new WorkflowManager();
            processConfig(configFileStream);
            dam = dams.get(fulcrumConfig.getUseDam());
                logger.info("DAM to use is: "+fulcrumConfig.getUseDam());
            if (dam != null) {
                logger.info("About to initialise the DAM Manager");
                dam.manager.setAdminPassword(this.damAdminPassword);
                dam.manager.init();
            }
            initAdvancedStuff(fulcrumConfig, dam);
        } catch (Exception e) {
            logger.info("Problem creating MainServer");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            logger.info("Hello from Fulcrum Advanced Server");
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
                instance = new AdvancedServer(configFileStream);
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

    protected final void initAdvancedStuff(FulcrumConfig fulcrumConfig, Dam dam) throws Exception {
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

    protected final void processConfig(InputStream configFileStream) throws Exception {
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
            databases = processDatabases(fulcrumConfig.getDatabases());

            // databases need to be set up already, as required for location file processors
            locations = processLocations(fulcrumConfig.getLocations());
            actions = processActions(fulcrumConfig.getActions());
            workflows = processWorkflows(fulcrumConfig.getWorkflows());
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Action> getActions() {
        return actions;
    }

    public void setActions(HashMap<String, Action> actions) {
        this.actions = actions;
    }

    public HashMap<String, Workflow> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(HashMap<String, Workflow> workflows) {
        this.workflows = workflows;
    }

    protected HashMap<String, Action> processActions(ActionList actions) {
        HashMap<String, Action> result = new HashMap<>();
        if (actions == null) {
            return result;
        }
        try {
            for (com.setantamedia.fulcrum.config.Action actionConfig : actions.getAction()) {
                Action action = processAction(actionConfig);
                if (action != null && action.getName() != null) {
                    result.put(action.getName(), action);
                } else {
                    logger.error("Invalid action definition in config file");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    protected Action processAction(com.setantamedia.fulcrum.config.Action action) {
        Action result = null;
        try {
            ParamList paramList = action.getParams();
            HashMap<String, String> params = new HashMap<>();
            if (paramList != null) {
                for (Param param : paramList.getParam()) {
                    params.put(param.getName(), param.getValue());
                }
            }
            Class actionClass = Class.forName(action.getProcessClass());
            ActionProcessor actionProcessor = (ActionProcessor) actionClass.newInstance();
            actionProcessor.setName(action.getName());
            actionProcessor.setParams(params);
            actionProcessor.setDam(dams.get(fulcrumConfig.getUseDam()));
            actionProcessor.setMainServer(this);
            actionProcessor.init();
            workflowManager.addActionProcessor(actionProcessor);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected HashMap<String, Workflow> processWorkflows(WorkflowList workflows) {
        HashMap<String, Workflow> result = new HashMap<>();
        if (workflows == null) {
            return result;
        }
        try {
            for (com.setantamedia.fulcrum.config.Workflow workflowConfig : workflows.getWorkflow()) {
                Workflow workflow = processWorkflow(workflowConfig);
                if (workflow != null && workflow.getName() != null) {
                    result.put(workflow.getName(), workflow);
                } else {
                    logger.error("Invalid worflow definition in config file");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    protected Workflow processWorkflow(com.setantamedia.fulcrum.config.Workflow action) {
        Workflow result = null;
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected HashMap<String, Location> processLocations(LocationList locations) {
        HashMap<String, Location> result = new HashMap<>();
        if (locations == null) {
            return result;
        }
        try {
            for (com.setantamedia.fulcrum.config.Location locationConfig : locations.getLocation()) {
                Location location = processLocation(locationConfig);
                if (location != null && location.getName() != null) {
                    result.put(location.getName(), location);
                } else {
                    logger.error("Invalid location definition in config file");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    protected Location processLocation(com.setantamedia.fulcrum.config.Location location) {
        Location result = null;
        try {
            Path folder = fs.getPath(location.getFolder());
            if (!Files.exists(folder)) {
                logger.info("Creating server folder " + folder.toString() + " for location: " + location.getName());
                Files.createDirectories(folder);
            }
            Path folderTmpFolder = folder.resolve(LocationMonitor.TMP_FOLDER);
            if (!Files.exists(folderTmpFolder)) {
                logger.info("Creating server folder " + folderTmpFolder.toString() + " for location: " + location.getName());
                Files.createDirectories(folderTmpFolder);
            }
            Monitor monitor = location.getMonitor();
            if (monitor == null) {
                result = new Location();
            } else {
                LocationMonitor locationMonitor = new LocationMonitor();
                locationMonitor.setFolder(folder);
                locationMonitor.setInitialScan(monitor.isInitialScan());
                locationMonitor.setMonitorSubFolders(monitor.isScanSubFolders());
                ParamList paramList = monitor.getParams();
                HashMap<String, String> params = new HashMap<>();
                if (paramList != null) {
                    for (Param param : paramList.getParam()) {
                        params.put(param.getName(), param.getValue());
                    }
                }
                Class processorClass = Class.forName(monitor.getProcessClass());
                FileProcessor fileProcessor = (FileProcessor) processorClass.newInstance();
                fileProcessor.setRootFolder(folder);
                fileProcessor.setDam(dams.get(fulcrumConfig.getUseDam()));
                fileProcessor.setPreviewCacheFolder(fulcrumConfig.getPreviewCacheFolder());
                fileProcessor.setParams(params);

                // databases will have already been setup
                fileProcessor.setDatabases(databases);
                fileProcessor.setMainServer(this);
                locationMonitor.setLocationListener(fileProcessor);
                locationMonitor.init();
                result = locationMonitor;
            }
            result.setFolder(folder);
            result.setName(location.getName());
            result.setAccessCode(location.getAccessCode());
            if (result.getName() == null) {
                logger.error("Location with no name in config file - ignoring");
                result = null;
            } else {
                logger.info("Location with name: " + result.getName() + " configured for folder: " + location.getFolder());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected HashMap<String, Database> processDatabases(DatabaseList databases) {
        HashMap<String, Database> result = new HashMap<>();
        if (databases == null) {
            return result;
        }
        try {
            for (com.setantamedia.fulcrum.config.Database databaseConfig : databases.getDatabase()) {
                Database aDatabase = processDatabase(databaseConfig);
                if (aDatabase != null && aDatabase.getName() != null) {
                    result.put(aDatabase.getName(), aDatabase);
                } else {
                    logger.error("Invalid database definition in config file");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    protected Database processDatabase(com.setantamedia.fulcrum.config.Database database) {
        Database result = new Database();
        try {
            result.setName(database.getName());
            result.setDriver(database.getManagerClass());
            //result.manager = new H2DbManager();
            DbManager dbManager = (DbManager) Class.forName(database.getManagerClass()).newInstance();
            dbManager = (DbManager) Class.forName(database.getManagerClass()).newInstance();
            dbManager.setDatabaseName(database.getName());
            dbManager.setConfig(fulcrumConfig);
            dbManager.setBaseUrl(database.getBaseUrl());
            dbManager.setSchemaFile(database.getSchemaFile());
            dbManager.setCustomSchemaFile(database.getCustomSchemaFile());
            if (database.getViews() != null) {
                // views are optional
                dbManager.setConfigViews(database.getViews().getView());
            }
            dbManager.setConnections(processConnections(null, database.getConnections()));
            dbManager.setQueries(queries);
            dbManager.init();
            result.setManager(dbManager);
            if (result.getName() == null) {
                logger.error("Database with no name in config file - ignoring");
                result = null;
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void start() {
        super.start();
        try {
            startLocationMonitors();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        super.stop();
        try {
            stopLocationMonitors();
            stopDatabases();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void startLocationMonitors() {
        if (locations != null && locations.size() > 0) {
            for (Location location : locations.values()) {
                if (location instanceof LocationMonitor) {
                    LocationMonitor lm = (LocationMonitor) location;
                    lm.getLocationListener().setFulcrumConfig(fulcrumConfig);
                    lm.getLocationListener().init();
                    lm.start();
                }
            }
        }
    }

    protected void stopLocationMonitors() {
        for (Location location : locations.values()) {
            if (location instanceof LocationMonitor) {
                LocationMonitor lm = (LocationMonitor) location;
                lm.stop();
            }
        }
    }

    protected void stopDatabases() {
        for (Database db : databases.values()) {
            db.getManager().stop();
        }
    }

    public static CoreServer getInstance() {
        return instance;
    }

    public HashMap<String, Location> getLocations() {
        return locations;
    }

    public Database getDatabase(String name) {
        return databases.get(name);
    }

    public HashMap<String, Database> getDatabases() {
        return databases;
    }

    public WorkflowManager getWorkflowManager() {
        return workflowManager;
    }
}
