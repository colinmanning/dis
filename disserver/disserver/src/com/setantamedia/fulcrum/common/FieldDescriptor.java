package com.setantamedia.fulcrum.common;

import java.util.HashMap;

public class FieldDescriptor {

   private Integer type = FieldTypeConstants.TypeString;
   private String guid = null;
   private String name = null;
   private HashMap<String, String> displayNames = new HashMap<>();

   public FieldDescriptor() {

   }

   public Integer getType() {
      return type;
   }

   public void setType(Integer type) {
      this.type = type;
   }

   public String getGuid() {
      return guid;
   }

   public void setGuid(String guid) {
      this.guid = guid;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public HashMap<String, String> getDisplayNames() {
      return displayNames;
   }

   public void setDisplayNames(HashMap<String, String> displayNames) {
      this.displayNames = displayNames;
   }

}
