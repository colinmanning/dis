package com.setantamedia.fulcrum.models.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import org.json.JSONObject;


public class Entity {
   protected static DateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
   protected static DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
   protected static DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
   public final static Integer DEFAULT_ID = -1;
   public final static String FIELD_ID = "id";
   private Integer id = DEFAULT_ID;
   
   private HashMap<String, Object> fields = new HashMap<>();
   
   public Entity() {
      
   }

   
   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }
   
   public HashMap<String, Object> getFields() {
      return fields;
   }

   public void setFields(HashMap<String, Object> fields) {
      this.fields = fields;
   }
   
   public void addField(String name, String value) {
      this.fields.put(name, value);
   }
   /**
    * Returns a JSONObject version of the entity. Unlike the getFields() method, data fields should be of the expected
    * type. Should be called by sub classes before sub classes add their own fields
    *
    * @return
    */
   public JSONObject toJson() {
      JSONObject result = new JSONObject();
      try {
         result.put(FIELD_ID, id);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

}
