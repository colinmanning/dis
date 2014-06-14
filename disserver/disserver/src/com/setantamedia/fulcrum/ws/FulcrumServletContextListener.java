package com.setantamedia.fulcrum.ws;

import com.setantamedia.fulcrum.AdvancedServer;
import com.setantamedia.fulcrum.CoreServer;
import java.io.InputStream;
import java.nio.file.Path;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class FulcrumServletContextListener implements ServletContextListener {

    private static Logger logger = Logger.getLogger(FulcrumServletContextListener.class);
    public final static String PROPERTY_CONFIG_FILE = "dis-config-file";
    public final static String PROPERTY_LOG_CONFIG_FILE = "dis-log-file";
    public final static String PROPERTY_DAM_ADMIN_PASSWORD = "dam-admin-password";
    public final static String SERVER_CLASS = "server-class";
    public final static String FULCRUM_CONFIG = "fulcrum-config";
    public final static String FULCRUM_DAM = "fulcrum-dam";
    public final static String TMP_FOLDER = "tmp-folder";
    public final static String DB_FOLDER = "db-folder";
    public final static String ACCESS_TOKEN = "access-token";
    public final static String ADMIN_USER = "admin-user";
    public final static String ACCESS_CONTROL = "access-control";
    public final static String PUBLIC = "public";
    public final static String PREVIEW_CACHE_FOLDER = "preview-cache-folder";
    public final static String MAIN_SERVER = "main-server";
    
    public final static String DEFAULT_CONFIG_FILE = "/WEB-INF/conf/dis-config.xml";
    public final static String DEFAULT_LOG_COFIG_FILE = "/WEB-INF/conf/dis-log4j.xml";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            ServletContext context = event.getServletContext();
            String configFilePath = (context.getInitParameter(PROPERTY_CONFIG_FILE) != null) ? context.getInitParameter(PROPERTY_CONFIG_FILE) : DEFAULT_CONFIG_FILE;
            String logConfigFilePath = (context.getInitParameter(PROPERTY_LOG_CONFIG_FILE) != null) ? context.getInitParameter(PROPERTY_LOG_CONFIG_FILE) : DEFAULT_LOG_COFIG_FILE;
            String damAdminPassword = (context.getInitParameter(PROPERTY_DAM_ADMIN_PASSWORD) != null) ? context.getInitParameter(PROPERTY_DAM_ADMIN_PASSWORD) : null;

            InputStream configFileStream = context.getResourceAsStream(configFilePath);
            String log4jFilePath = context.getRealPath(logConfigFilePath);

            //String serverClassName = context.getInitParameter(SERVER_CLASS);
            //CoreServer server = (CoreServer) Class.forName(serverClassName).newInstance();
            AdvancedServer server = new AdvancedServer();
            server.setup(configFileStream, log4jFilePath, damAdminPassword);
            logger.info("Welcome to DIS Server");
            //logger.info("server class name is: "+serverClassName);
            logger.info("server class via inspection is; "+server.getClass().toString());
            server.start();
            context.setAttribute(FULCRUM_CONFIG, server.getFulcrumConfig());
            context.setAttribute(FULCRUM_DAM, server.getDam());
            context.setAttribute(TMP_FOLDER, server.getTmpFolder());
            context.setAttribute(PREVIEW_CACHE_FOLDER, server.getPreviewCacheFolder());
            logger.info("temporary folder is: "+server.getTmpFolder());
            logger.info("previewcache folder is: "+server.getPreviewCacheFolder());
            logger.info("Storing Main Server in servlet context with attribute key: " + MAIN_SERVER + " preview cache folder: " + server.getPreviewCacheFolder().toString());
            context.setAttribute(MAIN_SERVER, server);
            logger.info("DIS Server started ok");
        } catch (Exception e) {
            logger.info("problem initialising the Main Server: " + e.getMessage());
            e.printStackTrace();
            logger.info("Problem starting DIS Server");
        }
        logger.info("DIS web application is starting.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        logger.info("DIS web application is shutting down.");
        ServletContext context = event.getServletContext();
        CoreServer server = (CoreServer) context.getAttribute(MAIN_SERVER);
        if (server != null) {
            server.stop();
        }
    }
}
