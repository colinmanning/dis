package com.setantamedia.fulcrum.common;

public class DataSizeValue {

   private long value = -1L;
   private String displayString = FieldValue.EMPTY_STRING;

   public DataSizeValue() {
   }

   /**
    * @return the value
    */
   public long getValue() {
      return value;
   }

   /**
    * @param value
    *           the value to set
    */
   public void setValue(long value) {
      this.value = value;
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
