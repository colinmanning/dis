/**
 *
 */
package com.setantamedia.fulcrum.ws;

import com.setantamedia.fulcrum.common.Common;
import com.setantamedia.fulcrum.common.Session;
import com.setantamedia.fulcrum.common.Utilities;
import com.setantamedia.fulcrum.dam.entities.Folder;
import com.setantamedia.fulcrum.ws.types.Category;
import com.setantamedia.fulcrum.ws.types.Record;
import com.setantamedia.fulcrum.ws.types.User;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

@SuppressWarnings("serial")
/**
 * Supports data management functions on a Fulcrum Server:
 *
 * GET
 * http://server/dis/data/photo-archive/create?item=category&path=$Categories:Jobs:1234
 * GET http://server/dis/data/photo-archive/fetch?id=123&view=details
 */
public class DataServlet extends BaseServlet {

    private final static Logger logger = Logger.getLogger(DataServlet.class);
    public final static String SERVICE_NAME = "data";
    protected SimpleDateFormat timeFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
    public final static String ITEM_CATEGORY = "category";
    public final static String ITEM_FOLDER = "folder";
    public final static String ITEM_RECORD = "record";
    public final static String DEFAULT_ITEM = ITEM_RECORD;

    public enum Operations {

        create, update, delete, fetch, addrecordtocategory, addrecordtocategories, removerecordfromcategory,
        increment, decrement
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int status = HttpServletResponse.SC_EXPECTATION_FAILED;
        try {
            String[] pathElements = getPathElements(request);
            if (pathElements.length < 3) {
                logger.error("Invalid data service request");
                throw new Exception("Invalid data service request");
            }
            serviceName = SERVICE_NAME;
            connectionName = pathElements[1];
            operationName = pathElements[2];
            // do access control
            if (!requestAllowed(request.getRemoteAddr())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            Session sessionData = null;
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(Common.FULCRUM_SESSION_DATA) == null) {
                //status = HttpServletResponse.SC_UNAUTHORIZED;
                //response.setStatus(status);
                //return;
            } else {
                sessionData = (Session) session.getAttribute(Common.FULCRUM_SESSION_DATA);
            }
            User user = null;
            if (sessionData != null) {
                user = sessionData.getUser();
            }
            
            callback = request.getParameter(PARAMETER_CALLBACK);

            String itemName = request.getParameter(PARAMETER_ITEM);
            if (operationName.equals(Operations.create.toString())) {
                if (itemName == null) {
                    itemName = DEFAULT_ITEM;
                }
                if (ITEM_RECORD.equals(itemName)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else if (ITEM_CATEGORY.equals(itemName)) {
                    Category category = null;
                    String categoryPath = request.getParameter(PARAMETER_PATH);
                    if (request.getParameter(PARAMETER_PARENT_ID) != null) {
                        Integer parentId = new Integer(request.getParameter(PARAMETER_PARENT_ID));
                        category = dam.manager.createSubCategory(dam.connections.get(connectionName), user, parentId, categoryPath);
                    } else {
                        category = dam.manager.createCategory(dam.connections.get(connectionName), user, categoryPath);
                    }
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(category.toJson().toString());
                    status = HttpServletResponse.SC_OK;
                } else if (ITEM_FOLDER.equals(itemName)) {
                    Folder folder = null;
                    String folderPath = request.getParameter(PARAMETER_PATH);
                    folder = dam.manager.createFolder(dam.connections.get(connectionName), user, folderPath);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(folder.toJson().toString());
                    status = HttpServletResponse.SC_OK;
                }
            } else if (operationName.equals(Operations.fetch.toString())) {
                String viewName = request.getParameter(PARAMETER_VIEW);
                String id = request.getParameter(PARAMETER_ID);
                Record record = dam.manager.getFileMetadata(dam.connections.get(connectionName), id, viewName, Utilities.getLocale());
                response.setCharacterEncoding(UTF_8);
                response.setContentType("application/json;charset=UTF-8");
                String j = record.toJson().toString();
                if (callback != null) {
                   j = callback + "(" + j + ")";
                }
                response.getWriter().write(j);
                response.getWriter().flush();
                response.getWriter().close();
                status = HttpServletResponse.SC_OK;

            } else if (operationName.equals(Operations.addrecordtocategory.toString())) {
                String recordId = request.getParameter(PARAMETER_RECORD_ID);
                String categoryId = request.getParameter(PARAMETER_CATEGORY_ID);
                if (dam.manager.addRecordToCategory(dam.connections.get(connectionName), categoryId, recordId)) {
                    status = HttpServletResponse.SC_OK;
                }
            } else if (operationName.equals(Operations.addrecordtocategories.toString())) {
                String recordId = request.getParameter(PARAMETER_RECORD_ID);
                String categoryIds = request.getParameter(PARAMETER_CATEGORY_IDS);
                if (dam.manager.addRecordToCategories(dam.connections.get(connectionName), categoryIds.split(":"), recordId)) {
                    status = HttpServletResponse.SC_OK;
                }
            } else if (operationName.equals(Operations.removerecordfromcategory.toString())) {
                String recordId = request.getParameter(PARAMETER_RECORD_ID);
                String categoryId = request.getParameter(PARAMETER_CATEGORY_ID);
                if (dam.manager.removeRecordFromCategory(dam.connections.get(connectionName), categoryId, recordId)) {
                    status = HttpServletResponse.SC_OK;
                }
            } else if (operationName.equals(Operations.update.toString())) {
                String id = null;
                if (request.getParameter(PARAMETER_ID) != null) {
                    id = request.getParameter(PARAMETER_ID);
                }
                HashMap<String, String> fields = new HashMap<>();

                Enumeration paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String paramName = (String) paramNames.nextElement();
                    if (paramName.startsWith(FULCRUM_PREFIX)) {
                        // real parameters start with "dis_" - second bit is the real parameter name
                        fields.put(paramName.substring(prefixLength), request.getParameter(paramName));
                    }
                }

                boolean ok = dam.manager.updateAssetData(dam.connections.get(connectionName), id, null, fields);
                if (ok) {
                    status = HttpServletResponse.SC_OK;
                }
            } else if (operationName.equals(Operations.increment.toString())) {
                String recordId = request.getParameter(PARAMETER_ID);
                String fieldName = request.getParameter(PARAMETER_FIELD);
                int amount = (request.getParameter(PARAMETER_AMOUNT) != null) ? new Integer(request.getParameter(PARAMETER_AMOUNT)) : 1;
                if (dam.manager.incrementFieldValue(dam.connections.get(connectionName), recordId, fieldName, amount)) {
                    status = HttpServletResponse.SC_OK;
                }
            } else if (operationName.equals(Operations.decrement.toString())) {
                String recordId = request.getParameter(PARAMETER_ID);
                String fieldName = request.getParameter(PARAMETER_FIELD);
                int amount = (request.getParameter(PARAMETER_AMOUNT) != null) ? new Integer(request.getParameter(PARAMETER_AMOUNT)) : 1;
                if (dam.manager.decrementFieldValue(dam.connections.get(connectionName), recordId, fieldName, amount)) {
                    status = HttpServletResponse.SC_OK;
                }
            }
        } catch (Exception e) {
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
            if (pathElements.length < 3) {
                logger.error("Invalid data service request");
                throw new Exception("Invalid data service request");
            }
            serviceName = SERVICE_NAME;
            connectionName = pathElements[1];
            operationName = pathElements[2];
            // do access control
            if (!requestAllowed(request.getRemoteAddr())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String id = null;
            if (request.getParameter(PARAMETER_ID) != null) {
                id = request.getParameter(PARAMETER_ID);
            }
            HashMap<String, String> fields = new HashMap<>();

            Enumeration paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = (String) paramNames.nextElement();
                if (paramName.startsWith(FULCRUM_PREFIX)) {
                    // real parameters start with "dis_" - second bit is the real parameter name
                    fields.put(paramName.substring(prefixLength), request.getParameter(paramName));
                }
            }

            boolean ok = dam.manager.updateAssetData(dam.connections.get(connectionName), id, null, fields);
            if (ok) {
                status = HttpServletResponse.SC_OK;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.setStatus(status);
        }
    }
}
