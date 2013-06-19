package com.setantamedia.fulcrum.common;

public class StringListValue {

   private int id = -1;
   private String displayString = FieldValue.EMPTY_STRING;

   public StringListValue() {
   }

   /**
    * @return the id
    */
   public int getId() {
      return id;
   }

   /**
    * @param id
    *           the id to set
    */
   public void setId(int id) {
      this.id = id;
   }

   /**
    * @return the displayString
    */
   public String getDisplayString() {
      return displayString;
   }

   /**
    * @param displayString
    *           the displayString to set
    */
   public void setDisplayString(String displayString) {
      this.displayString = displayString;
   }

}
