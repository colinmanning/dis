package com.setantamedia.fulcrum.dam.entities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONObject;

/**
 *
 * @author Colin Manning
 */
public abstract class DamEntity {

   private static DateFormat colinTimeFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
   protected static DateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
   protected static DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
   protected static DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
   public final static Integer DEFAULT_ID = -1;
   public final static String FIELD_ID = "id";
   public final static String FIELD_CONNECTION_NAME = "connectionName";
   private String connectionName = null;
   private String id = String.valueOf(DEFAULT_ID);

   public DamEntity() {
   }

   /**
    * Return the entity id
    *
    * @return the id
    */
   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   /**
    * Connection names need to be set if an application works with entities from different database connections. If a
    * single database connection, then ids will be unique, so this is not important in that case
    *
    * @return the connection name
    */
   public String getConnectionName() {
      return connectionName;
   }

   /**
    * Connection names need to be set if an application works with entities from different database connections. If a
    * single database connection, then ids will be unique, so this is not important in that case
    *
    * @param connectionName the name of the connection
    */
   public void setConnectionName(String connectionName) {
      this.connectionName = connectionName;
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
         result.put(FIELD_CONNECTION_NAME, connectionName);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   /**
    * Return the fields with the values converted to String, Also DO NOT put the entity ID in the returned data..
    */
   public abstract HashMap<String, String> getFields();

   /**
    * Return the fields with the values converted to String, using the database field namesand so can be directly used
    * in Database Manager method calls. Also DO NOT put the entity ID in the returned data..
    */
   public abstract HashMap<String, String> getDbFields();

   @Override
   public String toString() {
      return toJson().toString();
   }

   public static Integer processIntegerParameter(String paramValue) {
      return (paramValue == null || "".equals(paramValue) ? -1 : new Integer(paramValue));
   }

   public static Boolean processBooleanParameter(String paramValue) {
      return Boolean.valueOf(paramValue);
   }

   public static Date processDateParameter(String paramValue) throws ParseException {
      return (paramValue == null || "".equals(paramValue)) ? Calendar.getInstance().getTime() : colinTimeFormatter.parse(paramValue);
   }
}
