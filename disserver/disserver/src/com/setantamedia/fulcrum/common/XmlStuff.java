/**
 * 
 */
package com.setantamedia.fulcrum.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class XmlStuff {

   public final static String TAG_CLOSE = " />";
   public final static String EQ = "=";
   public final static String Q = "=";
   public final static String LB = "<";
   public final static String RB = ">";

   public final static String TAG_RECORDS = "<rs>";
   public final static String TAG_RECORDS_END = "</rs>";
   public final static String TAG_RECORD = "<r>";
   public final static String TAG_RECORD_END = "</r>";
   public final static String TAG_TABLE = "<t>";
   public final static String TAG_TABLE_END = "</t>";
   public final static String TAG_LABEL = "<label>";
   public final static String TAG_LABEL_END = "</label>";
   public final static String TAG_COUNT = "<count>";
   public final static String TAG_COUNT_END = "/<count>";
   public final static String TAG_FIELD_OPEN = "<f ";
   public final static String TAG_ID = "<id>";
   public final static String TAG_ID_END = "</id>";
   public final static String TAG_COLOR = "<color>";
   public final static String TAG_COLOR_END = "</color>";
   public final static String TAG_DISPLAYSTRING = "<dstring>";
   public final static String TAG_DISPLAYSTRING_END = "</dstring>";
   public final static String TAG_DATASIZE = "<dsize>";
   public final static String TAG_DATASIZE_END = "</dsize>";
   public final static String TAG_DATETIME = "<dtime>";
   public final static String TAG_DATETIME_END = "</dtime>";
   public final static String TAG_STRINGLIST = "<slist>";
   public final static String TAG_STRINGLIST_END = "</slist>";
   public final static String TAG_STRINGLISTS = "<slists>";
   public final static String TAG_STRINGLISTS_END = "</slists>";
   public final static String TAG_VALUE = "<v>";
   public final static String TAG_VALUE_END = "</v>";

   public final static String ATT_NAME = "n";
   public final static String ATT_VALUE = "v";

   private DateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
   private DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
   private DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

   public XmlStuff() {
   }

   /**
    * @return the dateTimeFormatter
    */
   public DateFormat getDateTimeFormatter() {
      return dateTimeFormatter;
   }

   /**
    * @param dateTimeFormatter
    *           the dateTimeFormatter to set
    */
   public void setDateTimeFormatter(DateFormat dateTimeFormatter) {
      this.dateTimeFormatter = dateTimeFormatter;
   }

   /**
    * @return the dateFormatter
    */
   public DateFormat getDateFormatter() {
      return dateFormatter;
   }

   /**
    * @param dateFormatter
    *           the dateFormatter to set
    */
   public void setDateFormatter(DateFormat dateFormatter) {
      this.dateFormatter = dateFormatter;
   }

   /**
    * @return the timeFormatter
    */
   public DateFormat getTimeFormatter() {
      return timeFormatter;
   }

   /**
    * @param timeFormatter
    *           the timeFormatter to set
    */
   public void setTimeFormatter(DateFormat timeFormatter) {
      this.timeFormatter = timeFormatter;
   }

}
