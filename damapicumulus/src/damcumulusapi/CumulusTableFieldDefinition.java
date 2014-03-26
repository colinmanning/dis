package damcumulusapi;

public class CumulusTableFieldDefinition {

   private String name = "";
   private String guid = "";

   private CumulusFieldDefinition[] columns = null;

   public CumulusTableFieldDefinition() {
   }

   public CumulusFieldDefinition getColumn(String columnName) {
      CumulusFieldDefinition result = null;
      for (int i = 0; i < columns.length; i++) {
         if (columnName.equals(columns[i].getName())) {
            result = columns[i];
            break;
         }
      }
      return result;
   }

   /**
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * @param name
    *           the name to set
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * @return the guid
    */
   public String getGuid() {
      return guid;
   }

   /**
    * @param guid
    *           the guid to set
    */
   public void setGuid(String guid) {
      this.guid = guid;
   }

   /**
    * @return the columns
    */
   public CumulusFieldDefinition[] getColumns() {
      return columns;
   }

   /**
    * @param columns
    *           the columns to set
    */
   public void setColumns(CumulusFieldDefinition[] columns) {
      this.columns = columns;
   }

}
