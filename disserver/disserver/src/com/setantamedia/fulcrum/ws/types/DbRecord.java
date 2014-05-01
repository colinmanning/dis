package com.setantamedia.fulcrum.ws.types;

import com.setantamedia.fulcrum.common.FieldUtilities;
import com.setantamedia.fulcrum.common.FieldValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Extend the Record class to be more appropriate for returning relational database records.
 * The default record assume DAM objects, so we strip out other stuff not relevant for simple database records
 * @author Colin Manning
 */
public class DbRecord extends Record {
    
   protected String table = "";

    public DbRecord() {
        super();
    }

    @Override
    public JSONObject toJson() {
        HashMap<String, Object> jsonData = buildJsonMap();
        for (Map.Entry<String, QueryResult> entry : kids.entrySet()) {
            Record[] kidRecords = entry.getValue().getRecords();
            ArrayList<JSONObject> kr = new ArrayList<>();
            for (Record kidRecord : kidRecords) {
                kr.add(kidRecord.toJson());
            }
            jsonData.put(entry.getKey(), kr);
        }
        return new JSONObject(jsonData);
    }

    @Override
    public HashMap<String, Object> buildJsonMap() {
        HashMap<String, Object> fieldMap = new HashMap<>();
        try {
            fieldMap.put(QueryResult.JSON_CONNECTION, connection);
            fieldMap.put(QueryResult.JSON_TABLE, table);
            fieldMap.put(QueryResult.JSON_ID, id);

            for (Map.Entry<String, FieldValue> field : getFields().entrySet()) {
                fieldMap.put(field.getKey(), FieldUtilities.getFieldValue(field.getValue(), true));
            }

            // table fields
            if (tableFields != null) {
                for (Map.Entry<String, Record[]> tableField : tableFields.entrySet()) {
                    ArrayList<JSONObject> jrecs = new ArrayList<>();
                    for (Record rec : tableField.getValue()) {
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

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
