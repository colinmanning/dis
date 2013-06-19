package com.setantamedia.fulcrum.common;

public class TableItemValue {

   private Integer hostId = FieldValue.INVALID_INTEGER;
   private Integer id = FieldValue.INVALID_INTEGER;
   private FieldValue[] columns = new FieldValue[0];

   public TableItemValue() {
   }

   /**
    * @return the hostId
    */
   public Integer getHostId() {
      return hostId;
   }

   /**
    * @param hostId
    *           the hostId to set
    */
   public void setHostId(Integer hostId) {
      this.hostId = hostId;
   }

   /**
    * @return the id
    */
   public Integer getId() {
      return id;
   }

   /**
    * @param id
    *           the id to set
    */
   public void setId(Integer id) {
      this.id = id;
   }

   /**
    * @return the columns
    */
   public FieldValue[] getColumns() {
      return columns;
   }

   /**
    * @param columns
    *           the columns to set
    */
   public void setColumns(FieldValue[] columns) {
      this.columns = columns;
   }

}
