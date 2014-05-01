package com.setantamedia.fulcrum.common;

import java.util.Date;

public class DateTime {

   private long value = -1L;
   private String displayValue = "";
   private Date dateValue = null;

   public DateTime() {
   }

   public String getDisplayValue() {
      return displayValue;
   }

   public void setDisplayValue(String displayValue) {
      this.displayValue = displayValue;
   }

   public Date getDateValue() {
	   return this.dateValue;
   }
   
   public long getValue() {
      return value;
   }

   public void setValue(long value) {
	  this.dateValue = new Date(value);
      this.value = value;
   }

}
