package com.setantamedia.fulcrum.common;

public class TableValue {

   private String[] columnNames = new String[0];
   private TableItemValue[] rows = new TableItemValue[0];

   public TableValue() {
   }

   /**
    * @return the columnNames
    */
   public String[] getColumnNames() {
      return columnNames;
   }

   /**
    * @param columnNames
    *           the columnNames to set
    */
   public void setColumnNames(String[] columnNames) {
      this.columnNames = columnNames;
   }

   /**
    * @return the rows
    */
   public TableItemValue[] getRows() {
      return rows;
   }

   /**
    * @param rows
    *           the rows to set
    */
   public void setRows(TableItemValue[] rows) {
      this.rows = rows;
   }

}
