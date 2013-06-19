package com.setantamedia.fulcrum.ws;

import com.setantamedia.fulcrum.AdvancedServer;
import com.setantamedia.fulcrum.CoreServer;
import com.setantamedia.fulcrum.DamManagerNotImplementedException;
import com.setantamedia.fulcrum.LocationManager;
import com.setantamedia.fulcrum.common.*;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;
import com.setantamedia.fulcrum.ws.types.User;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.log4j.Logger;
import org.json.JSONObject;

@SuppressWarnings("serial")
/**
 * Handle preview requests: GET
 * http://localhost/dis/file/photo-archive/get?id=123&actionName=compresssit
 * POST
 * http://localhost/dis/file/photo-archive/upload?uploadProfile=HighResImage
 */
public class FileServlet extends BaseServlet {

    public final static String SERVICE_NAME = "file";
    public final static int MAX_FILE_SIZE = 10000 * 1024 * 1024; // 10 GB
    public final static String DEFAULT_CONTROL_FILE_NAME = "filelist.csv";
    private static Logger logger = Logger.getLogger(FileServlet.class);
    private Path bigFileFolder = null;
    private LocationManager locationManager = null;
    private CoreServer server = null;

    public enum Operations {

        upload, uploadtolocation, get, deliver, copyfile, makefolder, deletefile, movefile, listfile
    }

    public FileServlet() {
        super();
        locationManager = new LocationManager();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.info("Initialising File Servlet");
        super.init(config);
        try {
            ServletContext context = config.getServletContext();
            tmpFolder = (Path) context.getAttribute(FulcrumServletContextListener.TMP_FOLDER);
            bigFileFolder = tmpFolder.resolve("big_files");
            if (!Files.exists(bigFileFolder)) {
                Files.createDirectories(bigFileFolder);
            }
            if (!Files.exists(bigFileFolder)) {
                Files.createDirectories(bigFileFolder);
            }
            server = (CoreServer) context.getAttribute(FulcrumServletContextListener.MAIN_SERVER);
            if (server instanceof AdvancedServer) {
                locationManager.setLocations(((AdvancedServer) server).getLocations());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            /*
             * HttpSession session = request.getSession(false); if (session == null ||
             * session.getAttribute(DbManager.DB_SESSION_DATA) == null) { status = HttpServletResponse.SC_UNAUTHORIZED;
             * response.setStatus(status); return; } DbSessionData sessionData = (DbSessionData)
             * session.getAttribute(DbManager.DB_SESSION_DATA);
             */
            String[] pathElements = getPathElements(request);
            if (pathElements.length < 3) {
                logger.error("Invalid file service request");
                throw new Exception("Invalid file service request");
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
            String folderPath = null;
            if (operationName.equals(Operations.makefolder.toString())) {
                String location = request.getParameter(PARAMETER_LOCATION);
                folderPath = request.getParameter(PARAMETER_FOLDER_PATH);
                String accessCode = request.getParameter(PARAMETER_ACCESS_CODE);
                response.setStatus((locationManager.makeLocationFolder(location, folderPath, accessCode)) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_FORBIDDEN);
            } else if (operationName.equals(Operations.copyfile.toString())) {
            } else if (operationName.equals(Operations.movefile.toString())) {
            } else if (operationName.equals(Operations.deletefile.toString())) {
            } else if (operationName.equals(Operations.get.toString())) {
                String id = request.getParameter(PARAMETER_ID);
                String actionName = request.getParameter(PARAMETER_ACTION);
                FileStreamer fstream = dam.manager.getFile(dam.connections.get(connectionName), user, id, null, actionName);
                response.setContentType("application/octet-stream");
                response.setBufferSize(Utilities.BUFFER_SIZE);
                InputStream is = fstream.getStream();
                ServletOutputStream os = response.getOutputStream();
                int totalCount = 0;
                try {
                    byte[] buffer = new byte[Utilities.BUFFER_SIZE];
                    int count = 0;
                    while (true) {
                        count = is.read(buffer);
                        if (count == 0) {
                            continue;
                        } else if (count == -1) {
                            // try again in case eof character in the stream
                            count = is.read(buffer);
                            if (count == -1) {
                                break;
                            }
                        }
                        os.write(buffer, 0, count);
                        totalCount += count;
                    }
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                    if (actionName != null && fs != null && fstream.getFile() != null) {
                        // ensure any temporary file is removed, only needed if an asset action is applied
                        dam.manager.removeTemporaryFile(fstream.getFile());
                    }
                }
                response.setContentLength(totalCount);
            } else if (operationName.equals(Operations.deliver.toString())) {
                String location = request.getParameter(PARAMETER_LOCATION);
                Location deliveryLocation = locationManager.getLocation(location);
                String actionName = request.getParameter(PARAMETER_ACTION);
                folderPath = request.getParameter(PARAMETER_FOLDER_PATH);
                String accessCode = request.getParameter(PARAMETER_ACCESS_CODE);
                String viewName = request.getParameter(PARAMETER_VIEW);
                String controlFileName = (request.getParameter(PARAMETER_CONTROL_FILE) != null) ? request.getParameter(PARAMETER_CONTROL_FILE) : DEFAULT_CONTROL_FILE_NAME;
                if (folderPath != null) {
                    // ensure folder path exists within the location
                    locationManager.makeLocationFolder(location, folderPath, accessCode);
                }
                Path deliveryPath = deliveryLocation.getFolder();
                if (folderPath != null) {
                    deliveryPath = deliveryPath.resolve(folderPath);
                }
                /*
                 * deliver method expects a query and a destination which is a location. The query is run, and then each
                 * asset found is delivered to the location.
                 */
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
                sd.setViewName(viewName);
                if (request.getParameter(PARAMETER_OFFSET) != null) {
                    sd.setOffset(new Integer(request.getParameter(PARAMETER_OFFSET)));
                }
                if (request.getParameter(PARAMETER_COUNT) != null) {
                    sd.setCount(new Integer(request.getParameter(PARAMETER_COUNT)));
                }
                if (request.getParameter(PARAMETER_FILTER) != null) {
                    sd.setFilter(request.getParameter(PARAMETER_FILTER));
                }

                QueryResult searchResult = dam.manager.querySearch(dam.connections.get(connectionName), user, query, sd, Utilities.getLocale());
                if (searchResult != null) {
                    response.setCharacterEncoding(UTF_8);

                    FileSaverThread fileSaver = new FileSaverThread();
                    fileSaver.setViewName(viewName);
                    fileSaver.setActionName(actionName);
                    fileSaver.setControlFileName(controlFileName);
                    fileSaver.setFolderPath(folderPath);
                    fileSaver.setDeliveryPath(deliveryPath);
                    fileSaver.setQuery(query);
                    fileSaver.setSearchResult(searchResult);
                    fileSaver.setUser(user);
                    new Thread(fileSaver).start();

                    response.setCharacterEncoding(UTF_8);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(searchResult.toJson().toString());
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().flush();
                    response.getWriter().close();
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    logger.error("A search did not execute correctly");
                    response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int status = HttpServletResponse.SC_NOT_FOUND;
        try {
            /*
             * HttpSession session = request.getSession(false); if (session == null ||
             * session.getAttribute(DbManager.DB_SESSION_DATA) == null) { status = HttpServletResponse.SC_UNAUTHORIZED;
             * response.setStatus(status); return; } DbSessionData sessionData = (DbSessionData)
             * session.getAttribute(DbManager.DB_SESSION_DATA);
             */
            String[] pathElements = getPathElements(request);
            if (pathElements.length < 3) {
                logger.error("Invalid file service request");
                throw new Exception("Invalid file service request");
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

            String uploadProfile = request.getParameter(PARAMETER_PROFILE);
            String fileName = request.getParameter(PARAMETER_NAME);
            /*
             * String actionName = request.getParameter(PARAMETER_ACTION); String id = null; if
             * (request.getParameter(PARAMETER_ID) != null) id = request.getParameter(PARAMETER_ID); Integer fileVersion =
             * null; if (request.getParameter(PARAMETER_VERSION) != null) fileVersion = new
             * Integer(request.getParameter(PARAMETER_VERSION));
             */

            HashMap<String, String> fields = new HashMap<>();
            Enumeration paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = (String) paramNames.nextElement();
                String[] paramValue = request.getParameterValues(paramName);
                switch (paramName) {
                    case PARAMETER_PROFILE:
                        continue;
                    case PARAMETER_NAME:
                        continue;
                    case PARAMETER_VERSION:
                        continue;
                    case PARAMETER_ID:
                        continue;
                    case PARAMETER_ACTION:
                        continue;
                }
                // must be a metadata field specified in the post body
                fields.put(paramName, paramValue[0]);
            }


            if (operationName.equals(Operations.uploadtolocation.toString())) {
                // simple request to upload to a location
                String accessCode = null;
                String location = null;
                String destination = null;
                String saveAs = null;
                String folderPath = null;
                DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
                fileItemFactory.setFileCleaningTracker(new FileCleaningTracker());
                fileItemFactory.setSizeThreshold(MAX_FILE_SIZE);
                fileItemFactory.setRepository(bigFileFolder.toFile());
                ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
                List items = uploadHandler.parseRequest(request);
                Iterator it = items.iterator();
                FileItem fileItem = null;
                while (it.hasNext()) {
                    FileItem item = (FileItem) it.next();
                    if (item.isFormField()) {
                        if (item.getFieldName().startsWith(FULCRUM_PREFIX)) {
                            // real parameters start with "dis_"
                            String[] bits = item.getFieldName().split("_");
                            switch (bits[1]) {
                                case PARAMETER_LOCATION:
                                    location = item.getString();
                                    break;
                                case PARAMETER_DESTINATION:
                                    destination = item.getString();
                                    break;
                                case PARAMETER_SAVEAS:
                                    saveAs = item.getString();
                                    break;
                                case PARAMETER_ACCESS_CODE:
                                    accessCode = item.getString();
                                    break;
                                case PARAMETER_FOLDER_PATH:
                                    folderPath = item.getString();
                                    break;
                            }
                        }
                    } else {
                        // the file
                        fileItem = item;
                    }
                }
                Location loc = locationManager.getLocation(location);
                Location dest = locationManager.getLocation(destination);
                if (loc != null && loc.getAccessCode() != null && accessCode != null && accessCode.equals(loc.getAccessCode())) {
                    if (loc.getFolder() != null) {
                        Path path = loc.getFolder();
                        if (folderPath != null) {
                            path = path.resolve(folderPath);
                        }
                        if (!Files.exists(path)) {
                            Files.createDirectories(path);
                        }
                        Path filePath = null;
                        if (fileItem != null) {
                            // the file.
                            if (saveAs != null) {
                                fileName = saveAs;
                            } else {
                                fileName = fileItem.getName();
                            }
                            filePath = path.resolve(fileName);
                            if (Files.exists(filePath)) {
                                //TODO make deletion of existing file an option via paraleter
                                // We need to do this as looks like JNotify in location monitors not triggering on JNotify.FILE:MODIFIED
                                Files.delete(filePath);
                            }
                            File file = filePath.toFile();
                            fileItem.write(file);
                            boolean ok = true;
                            Path destPath = null;
                            if (dest != null && dest.getAccessCode() != null && accessCode != null && accessCode.equals(dest.getAccessCode())) {
                                if (dest.getFolder() != null) {
                                    destPath = dest.getFolder();
                                    if (folderPath != null) {
                                        destPath = destPath.resolve(folderPath);
                                    }
                                    if (!Files.exists(destPath)) {
                                        Files.createDirectories(destPath);
                                    }
                                    Path toFile = destPath.resolve(destPath);
                                    Files.move(filePath, toFile);

                                }
                            }
                            status = (ok) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_EXPECTATION_FAILED;
                        }
                    }
                }
            } else if (operationName.equals(Operations.upload.toString())) {
                DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
                fileItemFactory.setFileCleaningTracker(new FileCleaningTracker());
                fileItemFactory.setSizeThreshold(MAX_FILE_SIZE);
                fileItemFactory.setRepository(bigFileFolder.toFile());
                ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
                Path tmpFile = null;
                List items = uploadHandler.parseRequest(request);
                Iterator it = items.iterator();
                while (it.hasNext()) {
                    FileItem item = (FileItem) it.next();
                    if (item.isFormField()) {
                        if (item.getFieldName().startsWith(FULCRUM_PREFIX)) {
                            // real parameters start with "dis_"
                            String[] bits = item.getFieldName().split("_");
                            fields.put(bits[1], item.getString());
                        } else if (PARAMETER_PROFILE.equals(item.getFieldName())) {
                            uploadProfile = item.getString("UTF-8");
                            continue;
                        } else if (PARAMETER_NAME.equals(item.getFieldName())) {
                            fileName = item.getString("UTF-8");
                        }
                    } else {
                        // the file. setting the name here is not safe if utf characters, only do it if none passed in as parameter
                        if (fileName == null || "".equals(fileName)) {
                            fileName = item.getName();
                        }
                        tmpFile = tmpFolder.resolve(fileName);
                        item.write(tmpFile.toFile());
                    }
                }
                try {
                    String rid = dam.manager.uploadFile(dam.connections.get(connectionName), user, tmpFile, fileName, uploadProfile, fields);
                    if (rid != null) {
                        HashMap<String, String> returnMap = new HashMap<>();
                        returnMap.put("id", rid);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(new JSONObject(returnMap).toString());
                        status = HttpServletResponse.SC_OK;
                    } else {
                        logger.error("failed to upload file with name: " + fileName + " to catalog: " + dam.connections.get(connectionName).getDatabase());
                        status = HttpServletResponse.SC_EXPECTATION_FAILED;
                    }
                } catch (DamManagerNotImplementedException | IOException e1) {
                    e1.printStackTrace();
                } finally {
                    // remote the temporary file
                    if (tmpFile != null && Files.exists(tmpFile)) {
                        Files.delete(tmpFile);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.setStatus(status);
        }
    }

    public HashMap<String, Location> getLocations() {
        return locationManager.getLocations();
    }

    public void setLocations(HashMap<String, Location> locations) {
        locationManager.setLocations(locations);
    }

    private class FileSaverThread implements Runnable {

        private String query = null;
        private String controlFileName = null;
        private String viewName = null;
        private QueryResult searchResult = null;
        private Path deliveryPath = null;
        private String folderPath = null;
        private String actionName = null;
        private User user = null;

        public FileSaverThread() {
        }

        public String getFolderPath() {
            return folderPath;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public void setFolderPath(String folderPath) {
            this.folderPath = folderPath;
        }

        public void setViewName(String viewName) {
            this.viewName = viewName;
        }

        public void setActionName(String actionName) {
            this.actionName = actionName;
        }

        public void setControlFileName(String controlFileName) {
            this.controlFileName = controlFileName;
        }

        public void setDeliveryPath(Path deliveryPath) {
            this.deliveryPath = deliveryPath;
            if (!Files.exists(deliveryPath)) {
                try {
                    Files.createDirectories(deliveryPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public void setSearchResult(QueryResult searchResult) {
            this.searchResult = searchResult;
        }

        @Override
        public void run() {
            String fileFieldName = dam.manager.getView(viewName).getFilenamefield();
            if (fileFieldName == null || "".equals(fileFieldName)) {
                fileFieldName = QueryResult.JSON_FILE_NAME;
            }
            Record[] records = searchResult.getRecords();
            String[] actualFileNames = new String[records.length];
            try {
                int fi = 0;
                for (Record record : records) {
                    FileStreamer fstream = null;
                    try {
                        fstream = dam.manager.getFile(dam.connections.get(connectionName), user, record.getId(), null, actionName);
                        if (fstream == null) {
                            logger.error("Problem getting file form connection: "+connectionName +" with id "+record.getId() + " - check logs, maybe issue with asset action");
                            continue;
                        }
                        String newFileName = record.getStringField(fileFieldName);
                        //String ext = record.getExtension();
                        String fName = fstream.getFile().getFileName().toString();
                        String ext = fName.substring(fName.lastIndexOf("."));
                        if (newFileName == null || "".equals(newFileName)) {
                            newFileName = record.getFileName();
                        }
                        
                        int extPos = newFileName.lastIndexOf(".");
                        if (actionName != null && !"".equals(actionName) && ext != null && !"".equals(ext)) {
                            // use the extension resulting form the asset action, which may be different from the original extewnsion
                            if (extPos <= 0) {
                                newFileName = newFileName + ext;
                            } else {
                                newFileName = newFileName.substring(0, newFileName.lastIndexOf(".")) + ext;
                            }
                        }
                        Path dp = deliveryPath.resolve(newFileName);

                        // check for duplicate file names
                        String baseFileName = newFileName;
                        int extPos1 = baseFileName.lastIndexOf(".");
                        if (extPos1 > 0) {
                            baseFileName = baseFileName.substring(0, extPos1);
                        }
                        int fv = 1;
                        while (Files.exists(dp)) {
                            newFileName = baseFileName + "_" + (fv++) + ext;
                            dp = deliveryPath.resolve(newFileName);
                        }
                        actualFileNames[fi++] = newFileName;
                        logger.info("Copying file to '" + dp + "'");
                        Files.copy(fstream.getStream(), dp, StandardCopyOption.REPLACE_EXISTING);
                        if (Files.exists(dp)) {
                            logger.info(dp + " created");
                        } else {
                            logger.info(dp + " not created");
                        }
                    } catch (DamManagerNotImplementedException | IOException fe) {
                        logger.error("Problem getting file for asset id: " + record.getId());
                        fe.printStackTrace();
                    } finally {
                        if (fstream != null) {
                            fstream.closeStream();
                            if (actionName != null && !"".equals(actionName)) {
                                if (!fstream.removeWorkDir()) {
                                    logger.error("Problem trying to remove temporary folder: " + fstream.getWorkDir());
                                }                             
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    //Path reportFile = deliveryPath.resolve(controlFileName);
                    Path reportFile = deliveryPath.resolve(controlFileName);
                    Files.deleteIfExists(reportFile);
                    ArrayList<String> reportLines = new ArrayList<>();
                    reportLines.add("Connection," + connectionName);
                    reportLines.add("Query," + query);
                    reportLines.add("Files found," + searchResult.getTotal());
                    reportLines.add("Files returned," + searchResult.getCount());
                    reportLines.add("Source Path," + deliveryPath.toString());
                    reportLines.add("Folder Path," + ((folderPath == null) ? "" : folderPath));
                    reportLines.add("Id,File Name");
                    // first do the report file, in case any transfers fail
                    int fi = 0;
                    for (Record record : records) {
                        reportLines.add(record.getId() + "," + actualFileNames[fi++]);
                    }
                    Path file = Files.write(reportFile, reportLines, Charset.forName("UTF-8"));
                    logger.info("file upload control file: " + file.toString() + " created");
                } catch (Exception ioe) {
                    logger.error(ioe.getMessage());
                    ioe.printStackTrace();
                }
            }
        }
    }
}
