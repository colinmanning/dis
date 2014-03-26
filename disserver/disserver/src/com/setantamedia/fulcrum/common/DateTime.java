package com.setantamedia.fulcrum.common;

public class DateTime {

   private long value = -1L;
   private String displayValue = "";

   public DateTime() {
   }

   public String getDisplayValue() {
      return displayValue;
   }

   public void setDisplayValue(String displayValue) {
      this.displayValue = displayValue;
   }

   public long getValue() {
      return value;
   }

   public void setValue(long value) {
      this.value = value;
   }

}
