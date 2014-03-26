/**
 *
 */
package com.setantamedia.fulcrum.ws.types;

import com.setantamedia.fulcrum.common.Common;
import com.setantamedia.fulcrum.common.XmlStuff;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class QueryResult {

   public final static String JSON_TOTAL = "total";
   public final static String JSON_COUNT = "count";
   public final static String JSON_RECORDS = "records";
   public final static String JSON_KEYWORDS = "keywords";
   public final static String JSON_PREVIEWS = "previews";
   public final static String JSON_LINKS = "links";
   public final static String JSON_REFERENCES = "references";
   public final static String JSON_PREVIEW_PREFIX = "preview_";
   public final static String JSON_LINK_PREFIX = "link_";
   // the hard coded fields - use uppercase strings
   public final static String JSON_CONNECTION = "CONNECTION";
   public final static String JSON_FILE_NAME = "FILENAME";
   public final static String JSON_FILE_EXTENSION = "FILEEXTENSION";
   public final static String JSON_FILE_ISFOLDER = "ISFOLDER";
   public final static String JSON_ID = "ID";
   public final static String JSON_TABLE = "TABLE";
   public final static String JSON_DOWNLOAD_LINK = "DOWNLOAD_LINK";
   private int total = 0;
   private int offset = 0;
   private int count = 0;
   private Record[] records = null;

   public QueryResult() {
      records = new Record[0];
   }

   /**
    * @return the count
    */
   public int getCount() {
      return count;
   }

   /**
    * @param count the count to set
    */
   public void setCount(int count) {
      this.count = count;
   }

   /**
    * @return the records
    */
   public Record[] getRecords() {
      return records;
   }

   /**
    * @param records the records to set
    */
   public void setRecords(Record[] records) {
      this.records = records;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(XmlStuff.TAG_RECORDS);
      sb.append(XmlStuff.TAG_COUNT).append(count).append(XmlStuff.TAG_COUNT_END);
      for (Record record : records) {
         sb.append(XmlStuff.TAG_RECORD);
         for (Map.Entry<String, String> entry : record.getXmlFields().entrySet()) {
            sb.append(XmlStuff.TAG_FIELD_OPEN + XmlStuff.ATT_NAME + XmlStuff.Q).append(entry.getKey()).append(XmlStuff.RB).append(entry.getValue()).append(XmlStuff.TAG_CLOSE);
         }
         sb.append(XmlStuff.TAG_RECORD_END);
      }
      sb.append(XmlStuff.TAG_RECORDS_END);
      return sb.toString();
   }

   public HashMap<String, Object> buildJsonMap() {
      HashMap<String, Object> result = new HashMap<>();
      result.put(JSON_TOTAL, total);
      List<JSONObject> jsonRecords = new ArrayList<>();
      if (records != null && records.length > 0) {
         for (Record record : records) {
            jsonRecords.add(record.toJson());
         }
         result.put(JSON_COUNT, records.length);
      } else {
         result.put(JSON_COUNT, 0);
      }
      result.put(JSON_RECORDS, jsonRecords);
      return result;
   }

   public JSONObject toJson() {
      HashMap<String, Object> jsonData = buildJsonMap();
      return new JSONObject(jsonData);
   }
   
   public JSONObject toJson(int httpStatus) {
      HashMap<String, Object> jsonData = buildJsonMap();
      jsonData.put(Common.JSON_HTTP_STATUS, httpStatus);
      return new JSONObject(jsonData);
   }

   public int getOffset() {
      return offset;
   }

   public void setOffset(int offset) {
      this.offset = offset;
   }

   public int getTotal() {
      return total;
   }

   public void setTotal(int total) {
      this.total = total;
   }

}
