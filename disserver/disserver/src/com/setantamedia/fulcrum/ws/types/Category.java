package com.setantamedia.fulcrum.ws.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.setantamedia.fulcrum.common.FieldUtilities;
import com.setantamedia.fulcrum.common.FieldValue;
import org.json.JSONArray;
import org.json.JSONObject;

public class Category {

    public final static String ID = "id";
    public final static String NAME = "name";
    public final static String PARENT_ID = "parentId";
    public final static String PATH = "path";
    public final static String CUSTOM_ORDER = "customOrder";
    public final static String SUB_CATEGORIES = "subcategories";
    public final static String HAS_CHILDREN = "hasChildren";
    private int id = -1;
    private int parentId = -1;
    private String path = null;
    private String name = null;
    private int customOrder = -1;
    private boolean hasChildren = false;
    private ArrayList<Category> subCategories = new ArrayList<Category>();
    private HashMap<String, FieldValue> fields = new HashMap<>();

    public Category() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Category> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(ArrayList<Category> subCategories) {
        this.subCategories = subCategories;
    }

    public void addSubCategory(Category category) {
        subCategories.add(category);
    }

    public int getCustomOrder() {
        return customOrder;
    }

    public void setCustomOrder(int customOrder) {
        this.customOrder = customOrder;
    }

    public boolean isHasChildren() {
      return hasChildren;
   }

   public void setHasChildren(boolean hasChildren) {
      this.hasChildren = hasChildren;
   }

   public JSONObject toJson() {
        HashMap<String, Object> jsonData = buildJsonMap();
        ArrayList<JSONObject> jsonSubCategories = new ArrayList<>();
        for (Category subCategory : subCategories) {
            jsonSubCategories.add(subCategory.toJson());
        }
        if (jsonSubCategories.size() > 0) {
            jsonData.put(SUB_CATEGORIES, new JSONArray(jsonSubCategories));
        }
        return new JSONObject(jsonData);
    }

    public HashMap<String, Object> buildJsonMap() {
        HashMap<String, Object> fieldMap = new HashMap<>();
        try {
            fieldMap.put(ID, id);
            fieldMap.put(NAME, name);
            fieldMap.put(PARENT_ID, parentId);
            fieldMap.put(PATH, path);
            fieldMap.put(HAS_CHILDREN, hasChildren);

            for (Map.Entry<String, FieldValue> field : getFields().entrySet()) {
                FieldValue fv = field.getValue();
                try {
                    String simpleName = fv.getFieldDefinition().getSimpleName();
                    if (simpleName != null) {
                        fieldMap.put(simpleName, FieldUtilities.getFieldValue(fv, true));
                    } else {
                        fieldMap.put(field.getKey(), FieldUtilities.getFieldValue(fv, true));
                    }
                } catch (Exception e) {
                    fieldMap.put(field.getKey(), FieldUtilities.getFieldValue(fv, true));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fieldMap;
    }


    public HashMap<String, FieldValue> getFields() {
        return fields;
    }

    public void setFields(HashMap<String, FieldValue> fields) {
        this.fields = fields;
    }

    public void addField(String key, FieldValue value) {
        fields.put(key, value);
    }

    public void rermoveField(String key, Object value) {
        fields.remove(key);
    }

    public Object getField(String key) {
        return fields.get(key);
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
