package com.setantamedia.fulcrum.common;



public class DatabaseTable {


   private String name = null;
   private String guid = null;

   private DatabaseField[] columns = null;

   public DatabaseTable() {
   }

   public DatabaseField[] getColumns() {
      return columns;
   }

   public DatabaseField getColumn(String columnName) {
      DatabaseField result = null;
      for (int i=0;i<columns.length;i++) {
         if (columnName.equals(columns[i].getName())) {
            result = columns[i];
            break;
         }
      }
      return result;
   }

   public void setColumns(DatabaseField[] columnns) {
      this.columns = columnns;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getGuid() {
      return guid;
   }

   public void setGuid(String guid) {
      this.guid = guid;
   }
}
