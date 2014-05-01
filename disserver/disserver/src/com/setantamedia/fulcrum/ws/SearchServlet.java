package com.setantamedia.fulcrum.ws;

import com.setantamedia.fulcrum.common.*;
import com.setantamedia.fulcrum.ws.types.Category;
import com.setantamedia.fulcrum.ws.types.QueryResult;
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
 * Supports search functions on a DIS Server: GET
 * http://localhost/dis/search/photo-archive/query?view=overview&text=coins&offset=0&count=2
 * GET http://localhost/dis/search/photo-archive/category?path=$Categories:Web
 * Publish
 */
public class SearchServlet extends BaseServlet {

    private final static Logger logger = Logger.getLogger(SearchServlet.class);
    public final static String SERVICE_NAME = "search";
    protected SimpleDateFormat timeFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
    public final static String ITEM_CATEGORY = "category";
    public final static String ITEM_RECORD = "record";
    public final static String DEFAULT_ITEM = ITEM_RECORD;

    public enum Operations {

        query, fulltext, category, categoryquery
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.info("Initialising Search Servlet");
        super.init(config);
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int status = HttpServletResponse.SC_NOT_FOUND;
        try {
            String[] pathElements = getPathElements(request);
            if (pathElements.length < 3) {
                logger.error("Invalid search service request");
                throw new Exception("Invalid search service request");
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
            
            callback = request.getParameter(PARAMETER_CALLBACK);

            if (operationName.equals(Operations.query.toString())) {
                String query = request.getParameter(PARAMETER_QUERY);
                if (query == null) {
                    String queryname = request.getParameter(PARAMETER_QUERY_NAME);
                    Query namedQuery = dam.manager.getQuery(queryname);
                    HashMap<String, String> queryParams = new HashMap<>();
                    Enumeration paramNames = request.getParameterNames();
                    while (paramNames.hasMoreElements()) {
                        String paramName = (String) paramNames.nextElement();
                        if (PARAMETER_QUERY_NAME.equals(paramName)
                                || PARAMETER_VIEW.equals(paramName)
                                || PARAMETER_OFFSET.equals(paramName)
                                || PARAMETER_COUNT.equals(paramName)
                                || PARAMETER_FILTER.equals(paramName)) {
                            continue;
                        }
                        queryParams.put(paramName, request.getParameter(paramName));
                    }
                    query = namedQuery.buildInstance(queryParams);
                }
                SearchDescriptor sd = new SearchDescriptor();
                sd.setViewName(request.getParameter(PARAMETER_VIEW));
                if (request.getParameter(PARAMETER_PAGE) != null && request.getParameter(PARAMETER_PAGE_SIZE) != null) {
                   int ps = new Integer(request.getParameter(PARAMETER_PAGE_SIZE));
                   int p = new Integer(request.getParameter(PARAMETER_PAGE));
                   sd.setOffset((p-1) * ps);
                   sd.setCount(ps);
                } else {
                   if (request.getParameter(PARAMETER_OFFSET) != null) {
                       sd.setOffset(new Integer(request.getParameter(PARAMETER_OFFSET)));
                   }
                   if (request.getParameter(PARAMETER_COUNT) != null) {
                       sd.setCount(new Integer(request.getParameter(PARAMETER_COUNT)));
                   }
                }
                if (request.getParameter(PARAMETER_FILTER) != null) {
                    sd.setFilter(request.getParameter(PARAMETER_FILTER));
                }
                if (request.getParameter(PARAMETER_SORTFIELD) != null) {
                    SortRule sortRule = new SortRule();
                    sortRule.setFieldName(request.getParameter(PARAMETER_SORTFIELD));
                    if (request.getParameter(PARAMETER_SORTDIRECTION) != null) {
                        String d = request.getParameter(PARAMETER_SORTDIRECTION);
                        if (DIRECTION_ASCENDING.equals(d)) {
                            sortRule.setDirection(0);
                        } else if (DIRECTION_DESCENDING.equals(d)) {
                            sortRule.setDirection(1);
                        }
                    }
                    sd.setSortRule(sortRule);
                }
                User user = null;
                if (sessionData != null) {
                    user = sessionData.getUser();
                }
                QueryResult searchResult = dam.manager.querySearch(dam.connections.get(connectionName), user, query, sd, Utilities.getLocale());
                if (searchResult != null) {
                    response.setCharacterEncoding(UTF_8);
                    response.setContentType("application/json;charset=UTF-8");
                    String j = searchResult.toJson().toString();
                    if (callback != null) {
                       j = callback + "(" + j + ")";
                    }
                    response.getWriter().write(j);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().flush();
                    response.getWriter().close();
                    status = HttpServletResponse.SC_OK;
                } else {
                    logger.error("A search did not execute correctly");
                    status = HttpServletResponse.SC_EXPECTATION_FAILED;
                }
            } else if (operationName.equals(Operations.fulltext.toString())) {
                String searchText = request.getParameter(PARAMETER_TEXT);
                SearchDescriptor sd = new SearchDescriptor();
                sd.setViewName(request.getParameter(PARAMETER_VIEW));
                if (request.getParameter(PARAMETER_PAGE) != null && request.getParameter(PARAMETER_PAGE_SIZE) != null) {
                   int ps = new Integer(request.getParameter(PARAMETER_PAGE_SIZE));
                   int p = new Integer(request.getParameter(PARAMETER_PAGE));
                   sd.setOffset((p-1) * ps);
                   sd.setCount(ps);
                } else {
                   if (request.getParameter(PARAMETER_OFFSET) != null) {
                       sd.setOffset(new Integer(request.getParameter(PARAMETER_OFFSET)));
                   }
                   if (request.getParameter(PARAMETER_COUNT) != null) {
                       sd.setCount(new Integer(request.getParameter(PARAMETER_COUNT)));
                   }
                }
                if (request.getParameter(PARAMETER_FILTER) != null) {
                    sd.setFilter(request.getParameter(PARAMETER_FILTER));
                }
                if (request.getParameter(PARAMETER_SORTFIELD) != null) {
                    SortRule sortRule = new SortRule();
                    sortRule.setFieldName(request.getParameter(PARAMETER_SORTFIELD));
                    if (request.getParameter(PARAMETER_SORTDIRECTION) != null) {
                        String d = request.getParameter(PARAMETER_SORTDIRECTION);
                        if (DIRECTION_ASCENDING.equals(d)) {
                            sortRule.setDirection(0);
                        } else if (DIRECTION_DESCENDING.equals(d)) {
                            sortRule.setDirection(1);
                        }
                    }
                    sd.setSortRule(sortRule);
                }
                QueryResult searchResult = dam.manager.textSearch(dam.connections.get(connectionName), searchText, sd, Utilities.getLocale());
                if (searchResult != null) {
                    response.setCharacterEncoding(UTF_8);
                    response.setContentType("application/json;charset=UTF-8");
                    String j = searchResult.toJson().toString();
                    if (callback != null) {
                       j = callback + "(" + j + ")";
                    }
                    response.getWriter().write(j);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().flush();
                    response.getWriter().close();
                    status = HttpServletResponse.SC_OK;
                } else {
                    status = HttpServletResponse.SC_FORBIDDEN;
                }
            } else if (operationName.equals(Operations.categoryquery.toString())) {
                SearchDescriptor sd = new SearchDescriptor();
                sd.setViewName(request.getParameter(PARAMETER_VIEW));
                if (request.getParameter(PARAMETER_PAGE) != null && request.getParameter(PARAMETER_PAGE_SIZE) != null) {
                   int ps = new Integer(request.getParameter(PARAMETER_PAGE_SIZE));
                   int p = new Integer(request.getParameter(PARAMETER_PAGE));
                   sd.setOffset((p-1) * ps);
                   sd.setCount(ps);
                } else {
                   if (request.getParameter(PARAMETER_OFFSET) != null) {
                       sd.setOffset(new Integer(request.getParameter(PARAMETER_OFFSET)));
                   }
                   if (request.getParameter(PARAMETER_COUNT) != null) {
                       sd.setCount(new Integer(request.getParameter(PARAMETER_COUNT)));
                   }
                }
                if (request.getParameter(PARAMETER_FILTER) != null) {
                    sd.setFilter(request.getParameter(PARAMETER_FILTER));
                }
                if (request.getParameter(PARAMETER_SORTFIELD) != null) {
                    SortRule sortRule = new SortRule();
                    sortRule.setFieldName(request.getParameter(PARAMETER_SORTFIELD));
                    if (request.getParameter(PARAMETER_SORTDIRECTION) != null) {
                        String d = request.getParameter(PARAMETER_SORTDIRECTION);
                        if (DIRECTION_ASCENDING.equals(d)) {
                            sortRule.setDirection(0);
                        } else if (DIRECTION_DESCENDING.equals(d)) {
                            sortRule.setDirection(1);
                        }
                    }
                    sd.setSortRule(sortRule);
                }

                String queryname = request.getParameter(PARAMETER_QUERY_NAME);
                if (queryname != null) {
                    Query namedQuery = dam.manager.getQuery(queryname);
                    HashMap<String, String> queryParams = new HashMap<>();
                    Enumeration paramNames = request.getParameterNames();
                    while (paramNames.hasMoreElements()) {
                        String paramName = (String) paramNames.nextElement();
                        if (PARAMETER_QUERY_NAME.equals(paramName)
                                || PARAMETER_VIEW.equals(paramName)
                                || PARAMETER_OFFSET.equals(paramName)
                                || PARAMETER_COUNT.equals(paramName)
                                || PARAMETER_FILTER.equals(paramName)) {
                            continue;
                        }
                        queryParams.put(paramName, request.getParameter(paramName));
                    }
                    sd.setNamedQuery(namedQuery.buildInstance(queryParams));
                }

                Boolean recursive = (Utilities.isTrue(request.getParameter(PARAMETER_RECURSIVE)));
                String id = request.getParameter(PARAMETER_ID);
                QueryResult searchResult = dam.manager.categorySearch(dam.connections.get(connectionName), id, sd, recursive, Utilities.getLocale());
                if (searchResult != null) {
                    response.setCharacterEncoding(UTF_8);
                    response.setContentType("application/json;charset=UTF-8");
                    status = HttpServletResponse.SC_OK;
                    String j = searchResult.toJson().toString();
                    if (callback != null) {
                       j = callback + "(" + j + ")";
                    }
                    response.getWriter().write(j);
                    response.setStatus(status);
                    response.getWriter().flush();
                    response.getWriter().close();
                } else {
                    logger.error("A category search did not execute correctly");
                    status = HttpServletResponse.SC_EXPECTATION_FAILED;
                }
            } else if (operationName.equals(Operations.category.toString())) {
                String categoryPath = request.getParameter(PARAMETER_PATH);
                Integer categoryId = null;
                if (categoryPath == null) {
                   categoryId = new Integer(request.getParameter(PARAMETER_ID));
                }
                boolean recursive = true;
                if (request.getParameter(PARAMETER_RECURSIVE) != null) recursive = new Boolean(request.getParameter(PARAMETER_RECURSIVE));
                
                Category root = null;
                if (categoryPath != null) {
                   root = dam.manager.findCategories(dam.connections.get(connectionName), categoryPath, recursive);
                   
                } else if (categoryId != null) {
                   root = dam.manager.findCategories(dam.connections.get(connectionName), categoryId, recursive);                                    
                }
                
                if (root != null) {
                    response.setCharacterEncoding(UTF_8);
                    response.setContentType("application/json;charset=UTF-8");
                    status = HttpServletResponse.SC_OK;
                    String j = root.toJson().toString();
                    if (callback != null) {
                       j = callback + "(" + j + ")";
                    }
                    response.getWriter().write(j);
                    response.setStatus(status);
                    response.getWriter().flush();
                    response.getWriter().close();
                } else {
                    logger.error("A request for categories did not execute correctly");
                    status = HttpServletResponse.SC_EXPECTATION_FAILED;
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
        doGet(request, response);
    }
}
