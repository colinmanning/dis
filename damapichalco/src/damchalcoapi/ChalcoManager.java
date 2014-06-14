package damchalcoapi;

import com.setantamedia.fulcrum.DamManager;
import com.setantamedia.fulcrum.common.*;
import com.setantamedia.fulcrum.config.Preview;
import com.setantamedia.fulcrum.config.View;
import com.setantamedia.fulcrum.config.Field;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;
import com.setantamedia.fulcrum.ws.types.User;
import damchalcoapi.soap.*;
import damchalcoapi.soap.admin.*;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

public class ChalcoManager extends DamManager {

    public final static String PREVIEW_ICON = "icon";
    public final static String PREVIEW_THUMB = "thumb";
    public final static String PREVIEW_NORMAL = "normal";
    public final static String PREVIEW_BIG = "big";

    public final static int FILE_CODE_PLACEHOLDER = 100;
    public final static String FILE_TYPE_PLACEHOLDER = "Placeholder";

    public final static int FILE_CODE_JPG = 12;
    public final static String FILE_TYPE_JPG = "Jpg";

    public final static int FILE_CODE_MP4 = 80;
    public final static String FILE_TYPE_MP4 = "MP4";

    private final static Logger logger = Logger.getLogger(ChalcoManager.class);
    private Boolean cchalcoStarted = false;
    private FileSystem fs = FileSystems.getDefault();

    private HttpContext localContext = new BasicHttpContext();

    private HashMap<Connection, HashMap<String, DatabaseField[]>> connectionViews =
            new HashMap<Connection, HashMap<String, DatabaseField[]>>();

    private HashMap<Connection, HashMap<String, HashMap<String, String>>> previewFields = new HashMap<>();
    private HashMap<Connection, HashMap<String, HashMap<String, String>>> linkFields = new HashMap<>();
    private HashMap<Connection, HashMap<String, HashMap<String, String>>> referenceFields = new HashMap<>();


    public ChalcoManager() {
        super();
    }

    /**
     * Initialisation the manager, to be called after all relevant setters have
     * been called, and makes sure the helper is correctly configured. In
     * particular, calling this before calling setConfig() is bad.
     */
    @Override
    public void init() {
        super.init();
        try {
            logger.info("Initialising Chalco");
            this.tmpDir = fs.getPath(config.getTmpFolder());
            logger.info("tmpDir: " + tmpDir.toString());

            cchalcoStarted = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public DatabaseField[] getFields(Connection connection, String view) {
        DatabaseField[] result = null;
        HashMap<String, DatabaseField[]> cviews = connectionViews.get(connection);
        if (cviews != null) {
            result = cviews.get(view);
            return result;
        } else {
            cviews = new HashMap<String, DatabaseField[]>();
        }
        try {
            ArrayList<DatabaseField> flist = new ArrayList<>();
            if (DamManager.ALL_FIELDS.equals(view)) {
                MetadataSoap service = new Metadata().getMetadataSoap();
                ArrayOfResponseOfField results = service.getDbSchema();
                List<ResponseOfField> l = results.getResponseOfField();
                result = new DatabaseField[l.size()+1];
                for (int i = 0; i < l.size(); i++) {
                    ResponseOfField assetField = l.get(i);
                    DatabaseField field = new DatabaseField();
                    field.setName(assetField.getName());
                    field.setDisplayName(assetField.getName());
                    field.setSimpleName(normaliseFieldName(field.getName()));
                    field.setGuid("");
                    field.setValueInterpretation(0);
                    switch (assetField.getType()) {
                        case "Text":
                            field.setDataType(FieldTypeConstants.TypeString);
                            break;
                        case "Numeric":
                            field.setDataType(FieldTypeConstants.TypeInteger);
                            break;
                        case "DateTime":
                            field.setDataType(FieldTypeConstants.TypeDateTime);
                            break;
                        default:
                            field.setDataType(FieldTypeConstants.TypeString);
                            break;
                    }
                    result[i] = field;
                }
                // Hack to handle Field Type field for initial Chalco demos (this size-1
                DatabaseField hackField = new DatabaseField();
                hackField.setName("File Type");
                hackField.setDisplayName("File Type");
                hackField.setSimpleName(normaliseFieldName("File_Type"));
                hackField.setDataType(FieldTypeConstants.TypeString);
                hackField.setGuid("");
                hackField.setValueInterpretation(0);
                result[result.length-1] = hackField;

            } else {
                DatabaseField[] allFields = null;
                if (views != null) {
                    views.get(DamManager.ALL_FIELDS);
                }
                if (allFields == null) {
                    // get all fields first time only
                    allFields = getFields(connection, DamManager.ALL_FIELDS);
                    View configView = views.get(view);
                    if (configView != null) {
                        List<Field> configFields = configView.getField();
                        result = new DatabaseField[configFields.size()];
                        for (Field configField : configFields) {
                            for (int i = 0; i < allFields.length; i++) {
                                if (configField.getName().equals(allFields[i].getName())) {
                                    flist.add(allFields[i]);
                                    break;
                                }
                            }
                        }
                    }
                }
                result = flist.toArray(new DatabaseField[0]);
            }
            cviews.put(view, result);
            connectionViews.put(connection, cviews);
        } catch ( Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public Map<String, String> getPreviews(Connection connection, String viewName) {
        Map<String, String> result = null;
        if (viewName == null) {
            return result;
        }
        HashMap<String, HashMap<String, String>> connectionPreviews = previewFields.get(connection);
        if (connectionPreviews != null && connectionPreviews.get(viewName) != null) {
            return connectionPreviews.get(viewName);
        }
        try {
            connectionPreviews = new HashMap<>();
            View view = views.get(viewName);
            HashMap<String, String> fields = new HashMap<>();
            List<String> previews = view.getPreview();
            if (view.getPreview() != null && view.getPreview().size() > 0) {
                for (String preview : previews) {
                    String templateUrl = baseUrl;
                    switch (preview) {
                        case DamManager.DOWNLOAD:
                            templateUrl += "/" + serverPrefix + "file/" + DamManager.TEMPLATE_PARAM_CATALOG_NAME + "/get/" + DamManager.TEMPLATE_PARAM_RECORD_NAME + "?id=" + DamManager.TEMPLATE_PARAM_ID;
                            fields.put(preview, templateUrl);
                            break;
                        case DamManager.THUMBNAIL:
                            templateUrl += "/" + serverPrefix + "preview/" + DamManager.TEMPLATE_PARAM_CATALOG_NAME + "/fetch?id=" + DamManager.TEMPLATE_PARAM_ID + "&name=thumbnail";
                            fields.put(preview, templateUrl);
                            break;
                        default:
                            templateUrl += "/" + serverPrefix + "preview/" + DamManager.TEMPLATE_PARAM_CATALOG_NAME + "/fetch?id=" + DamManager.TEMPLATE_PARAM_ID + "&name=" + DamManager.TEMPLATE_PARAM_NAME;
                            fields.put(preview, templateUrl);
                            break;
                    }
                }
            }
            connectionPreviews.put(viewName, fields);
            previewFields.put(connection, connectionPreviews);
            result = fields;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Map<String, String> getLinks(Connection connection, String viewName) {
        Map<String, String> result = null;
        if (viewName == null) {
            return result;
        }
        HashMap<String, HashMap<String, String>> connectionLinks = linkFields.get(connection);
        if (connectionLinks != null && connectionLinks.get(viewName) != null) {
            return connectionLinks.get(viewName);
        }
        try {
            connectionLinks = new HashMap<>();
            View view = views.get(viewName);
            HashMap<String, String> fields = new HashMap<>();
            List<String> links = view.getLink();
            if (view.getPreview() != null && view.getPreview().size() > 0) {
                for (String link : links) {
                    String templateUrl = baseUrl + "/" + serverPrefix + "data/" + DamManager.TEMPLATE_PARAM_CATALOG_NAME + "/fetch?id=" + DamManager.TEMPLATE_PARAM_ID + "&view=" + link;
                    fields.put(link, templateUrl);
                }
            }
            connectionLinks.put(viewName, fields);
            linkFields.put(connection, connectionLinks);
            result = fields;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Map<String, String> getReferences(Connection connection, String viewName) {
        Map<String, String> result = null;
        HashMap<String, HashMap<String, String>> connectionRefs = referenceFields.get(connection);
        if (connectionRefs == null) {
            connectionRefs = new HashMap<>();
            referenceFields.put(connection, connectionRefs);
        } else if (connectionRefs.get(viewName) != null) {
            return connectionRefs.get(viewName);
        }
        try {
            referenceFields.put(connection, connectionRefs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public byte[] previewFile(Connection connection, String id, Preview previewData, Path file, String actionName) {
        byte[] result = new byte[0];
        try {
            AssetSoap service = new Asset().getAssetSoap();
            result = service.preview(new Integer(id), previewData.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public byte[] getThumbnail(Connection connection, String id, Integer maxSize, SearchDescriptor searchDescriptor) {
        byte[] result = new byte[0];
        try {
            AssetSoap service = new Asset().getAssetSoap();
            result = service.preview(new Integer(id), PREVIEW_THUMB);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public byte[] previewFile(Connection connection, String id, Path cacheFile, Integer maxSize, String actionName) {
        byte[] result = new byte[0];
        try {
            /*
             * This is a big hack just to get first iteration testing working for demo
             * Chalco api needs to support passing maxsize parameter
             */

            String previewName = PREVIEW_ICON;
            if (maxSize > 400) {
                previewName = PREVIEW_BIG;
            } else if (maxSize > 200) {
                previewName = PREVIEW_NORMAL;

            } else if (maxSize > 80) {
                previewName = PREVIEW_THUMB;
            }

            AssetSoap service = new Asset().getAssetSoap();
            result = service.preview(new Integer(id), previewName);
            Utilities.savePreview(cacheFile, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void terminate() {
        super.terminate();
    }

    @Override
    public QueryResult querySearch(Connection connection, User user, String query, SearchDescriptor searchDescriptor, Locale locale) {
        QueryResult result = null;
        try {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    @Override
    public Record getFileMetadata(Connection connection, String id, String view, Locale locale) {
        Record result = null;
        try {
            AssetSoap service = new Asset().getAssetSoap();
            ArrayOfResponseOfAsset results = service.textSearch("id:" + id, 0, 1);
            List<ResponseOfAsset> l = results.getResponseOfAsset();
            if (l.size() > 0) {
                ResponseOfAsset asset = l.get(0);
                result = new Record();
                result.setId(String.valueOf(asset.getID()));
                result.setFileName(asset.getName());
                ArrayOfResponseOfMetadata data = asset.getMetadata();
                List<ResponseOfMetadata> metadataFields = data.getResponseOfMetadata();
                int fileTypeId = asset.getFileType();
                String fileType = "Unknown";
                switch (fileTypeId) {
                    case FILE_CODE_JPG:
                        fileType = FILE_TYPE_JPG;
                        break;
                    case FILE_CODE_MP4:
                        fileType = FILE_TYPE_MP4;
                        break;
                    case FILE_CODE_PLACEHOLDER:
                        fileType = FILE_TYPE_PLACEHOLDER;
                        break;
                    default:
                        fileType = String.valueOf(fileTypeId);
                        break;
                }
                FieldValue fv = new FieldValue();
                fv.setStringValue(fileType);
                result.addField("File Type", fv);
                for (ResponseOfMetadata metadata : metadataFields) {
                    FieldValue fieldValue = new FieldValue();
                    switch (metadata.getType()) {
                        case "Text":
                            fieldValue.setStringValue((String) metadata.getValue());
                            break;
                        case "Numeric":
                            fieldValue.setIntegerValue((Integer) metadata.getValue());
                            break;
                        case "DateTime":
                            //fieldValue.setDateTimeValue((DateTime) metadata.getValue());
                            break;
                        default:
                            continue;
                    }
                    result.addField(metadata.getName(), fieldValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    private String getAssetFieldType(ResponseOfAsset asset) {
        String result = "Unknown";
        int fileTypeId = asset.getFileType();
        switch (fileTypeId) {
            case 12:
                result = "Jpg";
                break;
            case 80:
                result = "MP4";
                break;
            case 100:
                result = "Placeholder";
                break;
            default:
                result = String.valueOf(fileTypeId);
                break;
        }
        return result;
    }

    private Record assetToRecord(ResponseOfAsset asset) {
        Record result = new Record();
        try {
            result.setId(String.valueOf(asset.getID()));
            result.setFileName(asset.getName());
            ArrayOfResponseOfMetadata data = asset.getMetadata();
            List<ResponseOfMetadata> metadataFields = data.getResponseOfMetadata();
            String fileType = getAssetFieldType(asset);
            FieldValue fv = new FieldValue();
            fv.setStringValue(fileType);
            result.addField("File Type", fv);
            for (ResponseOfMetadata metadata : metadataFields) {
                FieldValue fieldValue = new FieldValue();
                switch (metadata.getType()) {
                    case "Text":
                        fieldValue.setStringValue((String) metadata.getValue());
                        break;
                    case "Numeric":
                        fieldValue.setIntegerValue((Integer) metadata.getValue());
                        break;
                    case "DateTime":
                        //fieldValue.setDateTimeValue((DateTime) metadata.getValue());
                        break;
                    default:
                        continue;
                }
                result.addField(metadata.getName(), fieldValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    @Override
    public QueryResult textSearch(Connection connection, String text, SearchDescriptor searchDescriptor, Locale locale) {
        QueryResult result = new QueryResult();
        try {
            AssetSoap service = new Asset().getAssetSoap();
            ArrayOfResponseOfAsset results = service.textSearch(text, 0, 5);

            List<ResponseOfAsset> l = results.getResponseOfAsset();
            Record[] records = new Record[l.size()];
            for (int i = 0; i < l.size(); i++) {
                Record record = assetToRecord(l.get(i));
                records[i] = record;
            }
            result.setOffset(0);
            result.setCount(l.size());
            result.setTotal(l.size());
            result.setRecords(records);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    @Override
    public FileStreamer getFile(Connection connection, User user, String id, Integer version, String actionName) {
        FileStreamer result = null;
        try {
            AssetSoap service = new Asset().getAssetSoap();
            byte[] fileData = service.download(new Integer(id));
            result = new FileStreamer();
            result.setStream(new ByteArrayInputStream(fileData));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Ensure field name can be used as a variable name in various programming environments, so no spaces or special
     * characters.
     * <p/>
     * " " => "_" "(" => "_LRB_" ")" => "_RRB_" "[" => "_LSB_" "]" => "_RSB_" "/" => "_FWD_" "\" => "_BWD_" "-" =>
     * "_HYP_"
     *
     * @param fieldName
     * @return
     */
    public static String normaliseFieldName(String fieldName) {
        return fieldName.replaceAll(" ", "_").replaceAll("\\(", "_LRB_").replaceAll("\\)", "_RRB_").replaceAll("\\[", "_LSB_").replaceAll("\\]", "_RSB_").replaceAll("\\/", "_FWD_")
                .replaceAll("\\\\", "_BWD_").replaceAll("\\-", "_HYP_");
    }

    /**
     * Revert normalised field names to original field name.
     * <p/>
     * " " => "_" "(" => "_LRB_" ")" => "_RRB_" "[" => "_LSB_" "]" => "_RSB_" "/" => "_FWD_" "\" => "_BWD_" "-" =>
     * "_HYP_"
     *
     * @param fieldName
     * @return
     */
    private static String denormaliseFieldName(String fieldName) {
        return fieldName.replaceAll("_LRB_", "\\(").replaceAll("_RRB_", "\\)").replaceAll("_LSB_", "\\[").replaceAll("_RSB_", "\\]").replaceAll("_FWD_", "\\/").replaceAll("_BWD_", "\\\\")
                .replaceAll("_HYP_", "\\-").replaceAll("_", " ");
    }


}
