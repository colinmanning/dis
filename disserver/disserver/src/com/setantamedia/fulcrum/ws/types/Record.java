package com.setantamedia.fulcrum.ws.types;

import com.setantamedia.fulcrum.common.Common;
import com.setantamedia.fulcrum.common.FieldUtilities;
import com.setantamedia.fulcrum.common.FieldValue;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class Record {

   public final static String NULL_RECORD_ID = "-1";
   protected String connection = "";
   protected String id = NULL_RECORD_ID;
   protected String assetReferenceWindows = "";
   protected String assetReferenceMac = "";
   protected String assetReferenceUnix = "";
   protected String assetReferenceVault = "";
   protected String assetReferenceVaultAsFile = "";
   protected HashMap<String, FieldValue> fields = null;
   protected HashMap<String, String> xmlFields = null;
   protected HashMap<String, Record[]> tableFields = null;
   protected Map<String, String> previews = null;
   protected Map<String, String> links = null;
   protected Map<String, String> references = null;
   protected Map<String, QueryResult> kids = null;
   protected Category[] keywords = null;
   protected int version = -1;
   protected int parentId = -1;
   protected int[] variants = new int[0];
   protected String fileName = "";
   protected String extension = "";
   protected Boolean isFolder = false;

   public Record() {
      fields = new HashMap<>();
      tableFields = new HashMap<>();
      kids = new HashMap<>();
   }
   
   public String getExtension() {
      return extension;
   }

   public void setExtension(String extension) {
      this.extension = extension;
   }

   public String getFileName() {
      return fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }


   /**
    * @return the fields
    */
   public HashMap<String, FieldValue> getFields() {
      return fields;
   }

   /**
    * @return a field
    */
   public FieldValue getField(String name) {
      return fields.get(name);
   }

   /**
    * @return a field of children records
    */
   public Record[] getKidsField(String name) {
      return (fields.get(name) != null) ? fields.get(name).getKidsValue() : null;
   }
   
   public Object getFieldValue(String fieldName) {
       Object result = null;
       if (fields.containsKey(fieldName)) {
           result = fields.get(fieldName).getValue();
       }
       return result;
   }


   /**
    * @return a string field - most common scenario
    */
   public String getStringField(String name) {
      return fields.get(name).getStringValue();
   }

   /**
    * @return an integer field - a common scenario
    */
   public Integer getIntegerField(String name) {
      return fields.get(name).getIntegerValue();
   }

   /**
    * @return an long field - a common scenario
    */
   public Long getLongField(String name) {
      return fields.get(name).getLongValue();
   }

   /**
    * @return an boolean field - a common scenario
    */
   public Boolean getBooleanField(String name) {
      return fields.get(name).getBooleanValue();
   }

   /**
    * @return an date/time field - a common scenario
    */
   public Date getDateTimeField(String name) {
      return new Date(fields.get(name).getDateTimeValue().getValue());
   }

   /**
    * @return an data size field - a common scenario
    */
   public long getDataSizeField(String name) {
      return fields.get(name).getDataSizeValue().getValue();
   }

   /**
    * @param fields the fields to set
    */
   public void setFields(HashMap<String, FieldValue> fields) {
      this.fields = fields;
   }

   /**
    * @return the tableFields
    */
   public HashMap<String, Record[]> getTableFields() {
      return tableFields;
   }

   public void setTableFields(HashMap<String, Record[]> tableFields) {
      this.tableFields = tableFields;
   }

   public void addField(String name, FieldValue value) {
      fields.put(name, value);
   }

   public void addTableField(String name, Record[] value) {
      tableFields.put(name, value);
   }

   public void addXmlField(String name, String value) {
      xmlFields.put(name, value);
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getAssetReferenceWindows() {
      return assetReferenceWindows;
   }

   public void setAssetReferenceWindows(String assetReferenceWindows) {
      this.assetReferenceWindows = assetReferenceWindows;
   }

   public String getAssetReferenceMac() {
      return assetReferenceMac;
   }

   public void setAssetReferenceMac(String assetReferenceMac) {
      this.assetReferenceMac = assetReferenceMac;
   }

   public String getAssetReferenceUnix() {
      return assetReferenceUnix;
   }

   public void setAssetReferenceUnix(String assetReferenceUnix) {
      this.assetReferenceUnix = assetReferenceUnix;
   }

   public String getAssetReferenceVault() {
      return assetReferenceVault;
   }

   public void setAssetReferenceVault(String assetReferenceVault) {
      this.assetReferenceVault = assetReferenceVault;
   }

   public String getAssetReferenceVaultAsFile() {
      return assetReferenceVaultAsFile;
   }

   public void setAssetReferenceVaultAsFile(String assetReferenceVaultAsFile) {
      this.assetReferenceVaultAsFile = assetReferenceVaultAsFile;
   }

   public Category[] getKeywords() {
      return keywords;
   }

   public void setKeywords(Category[] keywords) {
      this.keywords = keywords;
   }

   public int getVersion() {
      return version;
   }

   public void setVersion(int version) {
      this.version = version;
   }

   public int getParentId() {
      return parentId;
   }

   public void setParentId(int parentId) {
      this.parentId = parentId;
   }

   public int[] getVariants() {
      return variants;
   }

   public void setVariants(int[] variants) {
      this.variants = variants;
   }

   public HashMap<String, String> getXmlFields() {
      return xmlFields;
   }

   public void setXmlFields(HashMap<String, String> xmlFields) {
      this.xmlFields = xmlFields;
   }

   public Map<String, String> getPreviews() {
      return previews;
   }

   public void setPreviews(Map<String, String> previews) {
      this.previews = previews;
   }

   public Map<String, String> getLinks() {
      return links;
   }

   public void setLinks(Map<String, String> links) {
      this.links = links;
   }

   public Map<String, String> getReferences() {
      return references;
   }

   public void setReferences(Map<String, String> references) {
      this.references = references;
   }

   public JSONObject toJson(int httpStatus) {
      HashMap<String, Object> jsonData = buildJsonMap();
      jsonData.put(Common.JSON_HTTP_STATUS, httpStatus);
      for (Map.Entry<String, QueryResult> entry : kids.entrySet()) {
         jsonData.put(entry.getKey(), entry.getValue().getRecords());
      }
      return new JSONObject(jsonData);
   }

   public JSONObject toJson() {
      HashMap<String, Object> jsonData = buildJsonMap();
      for (Map.Entry<String, QueryResult> entry : kids.entrySet()) {
         Record[] kidRecords = entry.getValue().getRecords();
         ArrayList<JSONObject> kr = new ArrayList<>();
         for (Record kidRecord:kidRecords) {
            kr.add(kidRecord.toJson());
         }
         jsonData.put(entry.getKey(), kr);
      }
      return new JSONObject(jsonData);
   }

   public HashMap<String, Object> buildJsonMap() {
      HashMap<String, Object> fieldMap = new HashMap<>();
      try {
         fieldMap.put(QueryResult.JSON_CONNECTION, connection);
         fieldMap.put(QueryResult.JSON_ID, id);
         fieldMap.put(QueryResult.JSON_FILE_NAME, fileName);
         fieldMap.put(QueryResult.JSON_FILE_EXTENSION, extension);
         fieldMap.put(QueryResult.JSON_FILE_ISFOLDER, isFolder);

         for (Map.Entry<String, FieldValue> field : getFields().entrySet()) {
            fieldMap.put(field.getKey(), FieldUtilities.getFieldValue(field.getValue(), true));
         }
         /*
          * should do this if keywords are in the view - leave for now
          *
          * if (record.getKeywords().length > 0) { ArrayList<String> keywords = new ArrayList<String>(); for (Category
          * category:record.getKeywords()) { if (!keywords.contains(category.getName())) {
          * keywords.add(category.getName()); } } fieldMap.put(JSON_KEYWORDS, new JSONArray(keywords)); }
          */

         // previews
         if (previews != null) {
            for (Map.Entry<String, String> preview : previews.entrySet()) {
               fieldMap.put(QueryResult.JSON_PREVIEW_PREFIX + preview.getKey(), preview.getValue());
            }
         }

         // links
         if (links != null) {
            for (Map.Entry<String, String> link : getLinks().entrySet()) {
               fieldMap.put(QueryResult.JSON_LINK_PREFIX + link.getKey(), link.getValue());
            }
         }

         // table fields
         if (tableFields != null) {
            for (Map.Entry<String, Record[]> tableField : tableFields.entrySet()) {
               ArrayList<JSONObject> jrecs = new ArrayList<>();
               for (Record rec:tableField.getValue()) {
                  jrecs.add(rec.toJson());
               }
               fieldMap.put(tableField.getKey(), new JSONArray(jrecs));
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return fieldMap;
   }

   public String getConnection() {
      return connection;
   }

   public void setConnection(String connection) {
      this.connection = connection;
   }

   public Boolean getIsFolder() {
      return isFolder;
   }

   public void setIsFolder(Boolean isFolder) {
      this.isFolder = isFolder;
   }

   public Map<String, QueryResult> getKids() {
      return kids;
   }

   public void setKids(Map<String, QueryResult> kids) {
      this.kids = kids;
   }

   public void addKid(String key, QueryResult kid) {
      kids.put(key, kid);
   }

   public QueryResult getKid(String key) {
      return kids.get(key);
   }

}
