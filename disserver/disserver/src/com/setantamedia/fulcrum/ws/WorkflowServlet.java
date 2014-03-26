/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.setantamedia.fulcrum.ws;

import com.setantamedia.fulcrum.AdvancedServer;
import com.setantamedia.fulcrum.actions.ActionProcessor;
import com.setantamedia.fulcrum.common.Database;
import com.setantamedia.fulcrum.db.DbManager;
import com.setantamedia.fulcrum.db.DbSessionData;
import com.setantamedia.fulcrum.workflow.WorkflowManager;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 *
 * @author Colin Manning
 */
@SuppressWarnings("serial")
public class WorkflowServlet extends BaseServlet {

    private final static Logger logger = Logger.getLogger(WorkflowServlet.class);
    public final static String SERVICE_NAME = "wf";
    private AdvancedServer server = null;
    private HashMap<String, Database> databases = null;
    private DbManager disDbManager = null;

    public enum Items {

        workflow, task, action, user
    }

    public enum Operations {

        execute, start, stop, next, login, logout
    }

    public WorkflowServlet() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            // needs to be an advanced server, so if not, let it crash
            ServletContext context = config.getServletContext();
            tmpFolder = (Path) context.getAttribute(FulcrumServletContextListener.TMP_FOLDER);
            server = (AdvancedServer) context.getAttribute(FulcrumServletContextListener.MAIN_SERVER);
            databases = server.getDatabases();
            if (databases.containsKey(DIS_DB_NAME)) {
               disDbManager = databases.get(DIS_DB_NAME).getManager();
            }
        } catch (Exception e) {
            logger.info("Failed to initialise workflow servlet, maybe no database configured");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int status = HttpServletResponse.SC_EXPECTATION_FAILED;
        try {
            String[] pathElements = getPathElements(request);
            if (pathElements.length < 2) {
                logger.error("Invalid workflow service request");
                throw new Exception("Invalid workflow service request");
            }
            serviceName = SERVICE_NAME;
            
            /*
             * Access Control requires a Connection for now, so bypass this for now
             * 
            // do access control
            if (!requestAllowed(request.getRemoteAddr())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            */
            
            Items item = null;
            Operations operation = null;
            try {
                item = Items.valueOf(pathElements[1]);
            } catch (IllegalArgumentException ie) {
                logger.info("invalid item in url path: " + pathElements[1]);
                throw ie;
            }
            try {
                operation = Operations.valueOf(pathElements[2]);
            } catch (IllegalArgumentException ie) {
                logger.info("invalid operation in url path: " + pathElements[2]);
                throw ie;
            }
            WorkflowManager workflowManager = server.getWorkflowManager();
            switch (item) {
                case user:
                    switch (operation) {
                        case login:
                            break;
                        case logout:
                            break;
                        default:
                            break;
                    }
                    break;
                case action:
                    switch (operation) {
                        case execute:
                            DbSessionData sessionData = null;
                            HttpSession session = request.getSession(false);
                            if (session == null || session.getAttribute(DbManager.DB_SESSION_DATA) == null) {
                                String username = request.getParameter(PARAMETER_USERNAME);
                                String password = request.getParameter(PARAMETER_PASSWORD);
                                if (username != null && password != null && disDbManager != null) {
                                    sessionData = disDbManager.login(DIS_DB_NAME, DIS_DB_CONNECTION_NAME, username, password);
                                }
                                if (sessionData == null) {
                                    status = HttpServletResponse.SC_UNAUTHORIZED;
                                    response.setStatus(status);
                                    return;
                                }
                            } else {
                                sessionData = (DbSessionData) session.getAttribute(DbManager.DB_SESSION_DATA);
                            }
                            String actionName = request.getParameter(PARAMETER_NAME);
                            HashMap<String, String> params = new HashMap<>();
                            Enumeration<String> paramNames = request.getParameterNames();
                            while (paramNames.hasMoreElements()) {
                                String paramName = (String) paramNames.nextElement();
                                if (PARAMETER_NAME.equals(paramName)
                                        || PARAMETER_USERNAME.equals(paramName)
                                        || PARAMETER_PASSWORD.equals(paramName)) {
                                    continue;
                                }
                                params.put(paramName, request.getParameter(paramName));
                            }
                            ActionProcessor actionProcessor = workflowManager.getActionProcessor(actionName);
                            if (actionProcessor != null) {
                                JSONObject actionResults = actionProcessor.execute(params);
                                if (actionResults != null) {
                                    response.setCharacterEncoding(UTF_8);
                                    response.setContentType("application/json;charset=UTF-8");
                                    response.getWriter().write(actionResults.toString());
                                    response.setStatus(HttpServletResponse.SC_OK);
                                    response.getWriter().flush();
                                    response.getWriter().close();
                                    status = HttpServletResponse.SC_OK;
                                } else {
                                }
                            } else {
                                JSONObject errorReport = new JSONObject();
                                errorReport.put("status", "FAILED");
                                errorReport.put("errorText", "The action processot '"+actionName+"' could not be found.");
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.setStatus(status);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}