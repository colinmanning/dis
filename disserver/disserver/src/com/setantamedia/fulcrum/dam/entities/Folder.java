package com.setantamedia.fulcrum.dam.entities;

import com.setantamedia.fulcrum.ws.types.Record;
import java.util.HashMap;
import org.json.JSONObject;

/**
 *
 * @author Colin Manning
 */
public class Folder extends DamEntity {
    public final static String FIELD_NAME = "name";
    public final static String DB_FIELD_NAME = "name";
    private String name = "";
    private String database = null;
    private String root = null;

    public Folder() {
    }

    @Override
    public HashMap<String, String> getFields() {
        HashMap<String, String> result = new HashMap<>();
        result.put(FIELD_NAME, name);
        return result;
    }

    @Override
    public HashMap<String, String> getDbFields() {
        HashMap<String, String> result = new HashMap<>();
        result.put(DB_FIELD_NAME, name);
        return result;
    }

    @Override
    public JSONObject toJson() {
        JSONObject result = super.toJson();
        try {
            result.put(FIELD_NAME, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Category buildFromRecord(Record record) {
        Category result = new Category();
        result.setId(record.getId());
        result.setName(record.getStringField(FIELD_NAME));
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }
}    
