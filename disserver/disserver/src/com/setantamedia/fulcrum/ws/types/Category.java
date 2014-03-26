package com.setantamedia.fulcrum.ws.types;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class Category {

    public final static String ID = "id";
    public final static String NAME = "name";
    public final static String CUSTOM_ORDER = "customOrder";
    public final static String SUB_CATEGORIES = "subcategories";
    public final static String HAS_CHILDREN = "hasChildren";
    private int id = -1;
    private String name = null;
    private int customOrder = -1;
    private boolean hasChildren = false;
    private ArrayList<Category> subCategories = new ArrayList<Category>();

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
        HashMap<String, Object> jsonData = new HashMap<>();
        jsonData.put(ID, id);
        jsonData.put(NAME, name);
        jsonData.put(CUSTOM_ORDER, customOrder);
        jsonData.put(HAS_CHILDREN, hasChildren);
        ArrayList<JSONObject> jsonSubCategories = new ArrayList<>();
        for (Category subCategory : subCategories) {
            jsonSubCategories.add(subCategory.toJson());
        }
        if (jsonSubCategories.size() > 0) {
            jsonData.put(SUB_CATEGORIES, new JSONArray(jsonSubCategories));
        }
        return new JSONObject(jsonData);
    }
}
