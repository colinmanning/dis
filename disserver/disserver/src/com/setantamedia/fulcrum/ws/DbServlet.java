package com.setantamedia.fulcrum.ws;

import com.setantamedia.fulcrum.AdvancedServer;
import com.setantamedia.fulcrum.common.Database;
import com.setantamedia.fulcrum.common.Query;
import com.setantamedia.fulcrum.common.SearchDescriptor;
import com.setantamedia.fulcrum.db.DbManager;
import com.setantamedia.fulcrum.db.DbSessionData;
import com.setantamedia.fulcrum.models.core.Person;
import static com.setantamedia.fulcrum.ws.BaseServlet.PARAMETER_COUNT;
import static com.setantamedia.fulcrum.ws.BaseServlet.PARAMETER_FILTER;
import static com.setantamedia.fulcrum.ws.BaseServlet.PARAMETER_OFFSET;
import static com.setantamedia.fulcrum.ws.BaseServlet.PARAMETER_QUERY_NAME;
import static com.setantamedia.fulcrum.ws.BaseServlet.PARAMETER_USERNAME;
import static com.setantamedia.fulcrum.ws.BaseServlet.PARAMETER_VIEW;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Supports data management functions on a Fulcrum Server:
 *
 * GET http://server/dis/db/print/point-portal/fetch?q=select = from print_jobs
 * GET
 * http://server/dis/db/print/point-portal/validateuser?username=colin&password=pass
 */
public class DbServlet extends BaseServlet {

    private final static Logger logger = Logger.getLogger(DbServlet.class);
    public final static String SERVICE_NAME = "db";
    protected SimpleDateFormat timeFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
    protected String dbName = null;
    private HashMap<String, Database> databases = null;

    public enum Operations {

        create, update, delete, fetch, validateuser, login, logout
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (mainServer instanceof AdvancedServer) {
            databases = ((AdvancedServer) mainServer).getDatabases();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int status = HttpServletResponse.SC_EXPECTATION_FAILED;
        try {
            String[] pathElements = getPathElements(request);
            serviceName = SERVICE_NAME;
            dbName = pathElements[1];
            connectionName = pathElements[2];
            operationName = pathElements[3];
            // do access control
            if (!requestAllowed(request.getRemoteAddr())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            DbManager dbManager = databases.get(dbName).getManager();
            if (operationName.equals(Operations.fetch.toString())) {
                DbSessionData sessionData = null;
                HttpSession session = request.getSession(false);
                if (session == null || session.getAttribute(DbManager.DB_SESSION_DATA) == null) {
                    // See if there is a username and password, and if so, try to login
                    String username = request.getParameter(PARAMETER_USERNAME);
                    String password = request.getParameter(PARAMETER_PASSWORD);
                    if (username != null && password != null) {
                        sessionData = dbManager.login(dbName, connectionName, username, password);
                    }
                    if (sessionData == null) {
                        status = HttpServletResponse.SC_UNAUTHORIZED;
                        response.setStatus(status);
                        return;
                    }
                } else {
                    sessionData = (DbSessionData) session.getAttribute(DbManager.DB_SESSION_DATA);
                }
                String query = request.getParameter(PARAMETER_QUERY);
                String view = request.getParameter(PARAMETER_VIEW);
                // findMany syntax to query for children via foreign key - tableName:foreignKeyField:view;tableName:foreignKeyField:view ...
                String findMany = request.getParameter(PARAMETER_FINDMANY);
                if (query == null) {
                    // try named query)
                    String queryname = request.getParameter(PARAMETER_QUERY_NAME);
                    Query namedQuery = dbManager.getQuery(queryname);
                    HashMap<String, String> queryParams = new HashMap<>();
                    Enumeration paramNames = request.getParameterNames();
                    while (paramNames.hasMoreElements()) {
                        String paramName = (String) paramNames.nextElement();
                        if (PARAMETER_QUERY_NAME.equals(paramName)
                                || PARAMETER_VIEW.equals(paramName)
                                || PARAMETER_OFFSET.equals(paramName)
                                || PARAMETER_COUNT.equals(paramName)
                                || PARAMETER_FILTER.equals(paramName)
                                || PARAMETER_USERNAME.equals(paramName)
                                || PARAMETER_PASSWORD.equals(paramName)) {
                            continue;
                        }
                        queryParams.put(paramName, request.getParameter(paramName));
                    }
                    query = namedQuery.buildInstance(queryParams);
                }
                if (query != null) {
                    SearchDescriptor searchDescriptor = new SearchDescriptor();
                    searchDescriptor.setViewName(view);
                    QueryResult searchResult = dbManager.fetch(sessionData, connectionName, query, searchDescriptor, findMany);
                    response.setCharacterEncoding(UTF_8);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(searchResult.toJson().toString());
                    response.getWriter().flush();
                    response.getWriter().close();
                    status = HttpServletResponse.SC_OK;
                } else {
                    // try named query
                }
            } else if (operationName.equals(Operations.delete.toString())) {
                HttpSession session = request.getSession(false);
                if (session == null || session.getAttribute(DbManager.DB_SESSION_DATA) == null) {
                    status = HttpServletResponse.SC_UNAUTHORIZED;
                    response.setStatus(status);
                    return;
                }
                Integer id = (request.getParameter(PARAMETER_ID) != null) ? new Integer(request.getParameter(PARAMETER_ID)) : null;
                String condition = request.getParameter(PARAMETER_CONDITION);
                String table = request.getParameter(PARAMETER_TABLE);
                // findMany syntax to query for children via foreign key - tableName:foreignKeyField:view;tableName:foreignKeyField:view ...
                Boolean ok = dbManager.delete(connectionName, table, id, condition);
                status = (ok) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST;
            } else if (operationName.equals(Operations.validateuser.toString())) {
                String username = request.getParameter(PARAMETER_USERNAME);
                String password = request.getParameter(PARAMETER_PASSWORD);
                Person person = dbManager.checkUser(connectionName, username, password);
                if (person != null) {
                    response.setCharacterEncoding(UTF_8);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(new JSONObject(person).toString());
                    response.getWriter().flush();
                    response.getWriter().close();
                    status = HttpServletResponse.SC_OK;
                } else {
                    status = HttpServletResponse.SC_UNAUTHORIZED;
                }
            } else if (operationName.equals(Operations.login.toString())) {
                // check for any existing session, and invalidate
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.setAttribute(DbManager.DB_SESSION_DATA, null);
                    session.invalidate();
                }
                String username = request.getParameter(PARAMETER_USERNAME);
                String password = request.getParameter(PARAMETER_PASSWORD);
                DbSessionData sessionData = dbManager.login(dbName, connectionName, username, password);
                if (sessionData != null) {
                    session = request.getSession(true);
                    session.setAttribute(DbManager.DB_SESSION_DATA, sessionData);
                    HashMap<String, Object> jsonData = new HashMap<>();
                    jsonData.put("sessionid", session.getId());
                    Person p = sessionData.getPerson();
                    jsonData.put("person", sessionData.getPerson().toJson());
                    response.setCharacterEncoding(UTF_8);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(new JSONObject(jsonData).toString());
                    response.getWriter().flush();
                    response.getWriter().close();
                    status = HttpServletResponse.SC_OK;
                } else if (operationName.equals(Operations.logout.toString())) {
                    session = request.getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }
                } else {
                    status = HttpServletResponse.SC_UNAUTHORIZED;
                }
            }
        } catch (SQLException | IOException | NumberFormatException e) {
            e.printStackTrace();
        } finally {
            response.setStatus(status);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int status = HttpServletResponse.SC_EXPECTATION_FAILED;
        try {
            String[] pathElements = getPathElements(request);
            if (pathElements.length < 4) {
                logger.error("Invalid db service request");
                throw new Exception("Invalid db service request");
            }
            serviceName = SERVICE_NAME;
            dbName = pathElements[1];
            connectionName = pathElements[2];
            operationName = pathElements[3];
            // do access control
            if (!requestAllowed(request.getRemoteAddr())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            DbManager dbManager = databases.get(dbName).getManager();
            if (operationName.equals(Operations.create.toString())) {
                HttpSession session = request.getSession(false);
                if (session == null || session.getAttribute(DbManager.DB_SESSION_DATA) == null) {
                    status = HttpServletResponse.SC_UNAUTHORIZED;
                    response.setStatus(status);
                    return;
                }
                String table = request.getParameter(PARAMETER_TABLE);
                HashMap<String, String> fields = new HashMap<>();

                Enumeration paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String paramName = (String) paramNames.nextElement();
                    if (paramName.startsWith(FULCRUM_PREFIX)) {
                        // real parameters start with "dis_" - second bit is the real parameter name
                        fields.put(paramName.substring(prefixLength), request.getParameter(paramName));
                    }
                }
                table = request.getParameter(PARAMETER_TABLE);
                Record record = dbManager.create(connectionName, table, fields);
                if (record != null) {
                    response.setCharacterEncoding(UTF_8);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(record.toJson().toString());
                    response.getWriter().flush();
                    response.getWriter().close();
                    status = HttpServletResponse.SC_OK;
                }
            } else if (operationName.equals(Operations.update.toString())) {
                HttpSession session = request.getSession(false);
                if (session == null || session.getAttribute(DbManager.DB_SESSION_DATA) == null) {
                    status = HttpServletResponse.SC_UNAUTHORIZED;
                    response.setStatus(status);
                    return;
                }
                String table = request.getParameter(PARAMETER_TABLE);
                Integer id = new Integer(request.getParameter(PARAMETER_ID));
                HashMap<String, String> fields = new HashMap<>();

                Enumeration paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String paramName = (String) paramNames.nextElement();
                    if (paramName.startsWith(FULCRUM_PREFIX)) {
                        // real parameters start with "dis_" - second bit is the real parameter name
                        fields.put(paramName.substring(prefixLength), request.getParameter(paramName));
                    }
                }
                Boolean ok = dbManager.update(connectionName, table, id, fields);
                status = (ok) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_MODIFIED;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.setStatus(status);
        }
    }

    public void setDatabases(HashMap<String, Database> databases) {
        this.databases = databases;
    }
}
