package com.setantamedia.fulcrum.ws;

import com.setantamedia.fulcrum.common.SearchDescriptor;
import com.setantamedia.fulcrum.common.SearchDescriptor.PreviewFormats;
import com.setantamedia.fulcrum.common.SortRule;
import com.setantamedia.fulcrum.common.Utilities;
import com.setantamedia.fulcrum.config.Preview;
import com.setantamedia.fulcrum.previews.PreviewCache;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

@SuppressWarnings("serial")
/**
 * Handle preview requests: GET
 * http://localhost/dis/preview/photo-archive/fetch?name=thumbnail&maxsize=100&query=Caption%20^%20Cheetah&sort=Asset%20Modification%20Date&direction=ascending
 * GET http://localhost/dis/preview/photo-archive/fetch?id=234&name=medium
 * (where name is specified in the DIS config file as pre defined preview
 */
public class PreviewServlet extends BaseServlet {

    private final static Logger logger = Logger.getLogger(PreviewServlet.class);
    public final static String SERVICE_NAME = "preview";
    public final static String NAME_THUMBNAIL = "thumbnail";
    public final static String NAME_FULL = "full";
    public final static String DEFAULT_NAME = NAME_THUMBNAIL;
    public final static String DEFAULT_FORMAT = FORMAT_JPG;
    protected PreviewCache previewCache = null;

    public enum Operations {

        fetch
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.info("Initialising Preview Servlet");
        super.init(config);
        try {
            ServletContext context = config.getServletContext();
            previewCache = new PreviewCache(mainServer.getPreviewCacheFolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int returnStatus = HttpServletResponse.SC_NOT_FOUND;
        boolean clearCache = false;
        boolean force = false;
        try {
            String[] pathElements = getPathElements(request);
            if (pathElements.length < 3) {
                logger.error("Invalid preview service request");
                throw new Exception("Invalid preview service request");
            }
            serviceName = SERVICE_NAME;
            connectionName = pathElements[1];
            operationName = pathElements[2];
            // do access control
            if (!requestAllowed(request.getRemoteAddr())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            //response.setStatus(returnStatus);
            String actionName = (request.getParameter(PARAMETER_ACTION) != null) ? request.getParameter(PARAMETER_ACTION) : null;
            Integer rotate = (request.getParameter(PARAMETER_ROTATE) != null) ? new Integer(request.getParameter(PARAMETER_ROTATE)) : null;
            Integer maxSize = (request.getParameter(PARAMETER_MAXSIZE) != null) ? new Integer(request.getParameter(PARAMETER_MAXSIZE)) : null;
            Integer size = (request.getParameter(PARAMETER_SIZE) != null) ? new Integer(request.getParameter(PARAMETER_SIZE)) : null;
            Integer top = (request.getParameter(PARAMETER_TOP) != null) ? new Integer(request.getParameter(PARAMETER_TOP)) : null;
            Integer left = (request.getParameter(PARAMETER_LEFT) != null) ? new Integer(request.getParameter(PARAMETER_LEFT)) : null;
            Integer width = (request.getParameter(PARAMETER_WIDTH) != null) ? new Integer(request.getParameter(PARAMETER_WIDTH)) : null;
            Integer height = (request.getParameter(PARAMETER_HEIGHT) != null) ? new Integer(request.getParameter(PARAMETER_HEIGHT)) : null;
            Integer compression = (request.getParameter(PARAMETER_COMPRESSION) != null) ? new Integer(request.getParameter(PARAMETER_COMPRESSION)) : null;
            String format = (request.getParameter(PARAMETER_FORMAT) != null) ? request.getParameter(PARAMETER_FORMAT) : DEFAULT_FORMAT;
            clearCache = (request.getParameter(PARAMETER_CLEARCACHE) != null);
            force = (request.getParameter(PARAMETER_FORCE) != null);

            if (operationName.equals(Operations.fetch.toString())) {
                String previewName = DEFAULT_NAME;
                String filter = request.getParameter(PARAMETER_FILTER);
                if (filter == null) {
                    filter = request.getParameter(PARAMETER_QUERY);
                }
                SearchDescriptor searchDescriptor = new SearchDescriptor();
                searchDescriptor.setPreviewFormat(PreviewFormats.Jpg);
                if (request.getParameter(PARAMETER_FORMAT) != null && request.getParameter(PARAMETER_FORMAT).equalsIgnoreCase(FORMAT_PNG)) {
                    searchDescriptor.setPreviewFormat(PreviewFormats.Png);
                }

                String id = null;
                if (request.getParameter(PARAMETER_ID) != null) {
                    id = request.getParameter(PARAMETER_ID);
                } else {
                    searchDescriptor = new SearchDescriptor();
                    if (filter != null) {
                        searchDescriptor.setFilter(filter);
                    }
                    // queryName = request.getParameter(PARAMETER_QUERYNAME);
                    if (request.getParameter(PARAMETER_SORTFIELD) != null) {
                        SortRule sortRule = new SortRule();
                        sortRule.setFieldName(request.getParameter(PARAMETER_SORTFIELD));
                        if (request.getParameter(PARAMETER_SORTDIRECTION) != null) {
                            String sortDirection = request.getParameter(PARAMETER_SORTDIRECTION);
                            if (DIRECTION_ASCENDING.equals(sortDirection)) {
                                sortRule.setDirection(SortRule.SORT_ASCENDING);
                            } else if (DIRECTION_DESCENDING.equals(sortDirection)) {
                                sortRule.setDirection(SortRule.SORT_DESCENDING);
                            }
                        }
                        searchDescriptor.setSortRule(sortRule);
                    }
                    id = dam.manager.queryForRecordId(dam.connections.get(connectionName), filter);
                }
                if (request.getParameter(PARAMETER_NAME) != null) {
                    previewName = request.getParameter(PARAMETER_NAME);
                }

                byte[] preview = null;
                if (NAME_THUMBNAIL.equals(previewName)) {
                    if (maxSize == null) {
                        maxSize = -1;
                    }
                    preview = dam.manager.getThumbnail(dam.connections.get(connectionName), id, maxSize, searchDescriptor);
                } else {
                    boolean found = false;
                    Path cacheFile = null;
                    Path cachePath = previewCache.makeCachePath(connectionName, id);
                    if (clearCache) {
                        previewCache.clearPathForRecord(cachePath, id, previewName);
                    } else {
                        logger.debug("about to make cache file for asset - path, id, previewName: " + cachePath + ", " + id + ", " + previewName);
                        cacheFile = previewCache.makeCacheFile(cachePath, id, previewName, force);
                        if (Files.exists(cacheFile)) {
                            // check to see if the asset has been updated since the preview was created
                            BasicFileAttributes attributes = Files.readAttributes(cacheFile, BasicFileAttributes.class);
                            FileTime previewModificationTime = attributes.lastModifiedTime();
                            Date assetModificationTime = dam.manager.getModifiedTime(dam.connections.get(connectionName), id);
                            Boolean assetChanged = (assetModificationTime.getTime() > previewModificationTime.toMillis()) ? true : false;
                            if (!force && !assetChanged) {
                                preview = previewCache.getPreviewData(cacheFile);
                                found = true;
                            }
                        }
                        if (!found) {
                            if (id != null) {
                                if (NAME_FULL.equals(previewName)) {
                                    preview = dam.manager.previewFile(dam.connections.get(connectionName), id, cacheFile, maxSize, actionName);
                                } else {
                                    List<Preview> previewTypes = fulcrumConfig.getPreviews().getPreview();
                                    Preview namedPreview = null;
                                    for (Preview previewType : previewTypes) {
                                        if (previewName.equals(previewType.getName())) {
                                            namedPreview = previewType;
                                            break;
                                        }
                                    }
                                    if (namedPreview == null) {
                                        namedPreview = new Preview();
                                        namedPreview.setTop(top);
                                        namedPreview.setLeft(left);
                                        namedPreview.setWidth(width);
                                        namedPreview.setHeight(height);
                                        namedPreview.setRotate(rotate);
                                        namedPreview.setMaxsize(maxSize);
                                        namedPreview.setSize(size);
                                        namedPreview.setCompression(compression);
                                        namedPreview.setName(previewName);
                                        namedPreview.setFormat(format);
                                    }
                                    logger.debug("generasting preview from PreviewServlet for connection: " + connectionName);
                                    preview = dam.manager.previewFile(dam.connections.get(connectionName), id, namedPreview, cacheFile, actionName);
                                }
                            }
                        }
                    }
                }
                if (preview != null && preview.length > 0) {
                    response.setContentType(Utilities.getPreviewMimeType(format));
                    response.setContentLength(preview.length);
                    response.getOutputStream().write(preview);
                    returnStatus = HttpServletResponse.SC_OK;
                } else {
                    returnStatus = HttpServletResponse.SC_NOT_FOUND;
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            e.printStackTrace();
        } finally {
            if (returnStatus != HttpServletResponse.SC_OK) {
                response.sendError(returnStatus);
            }
            if (!clearCache) {
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
            response.setStatus(returnStatus);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}