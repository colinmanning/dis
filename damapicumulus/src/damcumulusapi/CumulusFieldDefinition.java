package damcumulusapi;

import com.setantamedia.fulcrum.common.FieldTypeConstants;
import com.setantamedia.fulcrum.common.FieldValue;
import com.setantamedia.fulcrum.common.StringListValue;

public class CumulusFieldDefinition {

   private String memberName = "";
   private String name = "";
   private String guid = FieldValue.EMPTY_STRING;
   private Integer dataType = FieldValue.INVALID_INTEGER;
   private Integer valueInterpretation = FieldValue.INVALID_INTEGER;
   private StringListValue[] listValues = null;
   private CumulusTableFieldDefinition tableDefinition = null;

   public CumulusFieldDefinition() {
   }

   public boolean isDateField() {
      return (dataType == FieldTypeConstants.TypeDate);
   }

   public boolean isSelect() {
      return (dataType == FieldTypeConstants.TypeEnum);
   }

   public boolean isSimpleSelect() {
      return (dataType == FieldTypeConstants.TypeEnum && valueInterpretation == FieldTypeConstants.VALUE_DEFAULT);
   }

   public boolean isLabelSelect() {
      return (dataType == FieldTypeConstants.TypeEnum && valueInterpretation == FieldTypeConstants.VALUE_STRING_ENUM_LABEL);
   }

   public boolean isMultiSelect() {
      return (dataType == FieldTypeConstants.TypeEnum && valueInterpretation == FieldTypeConstants.VALUE_STRING_ENUM_MULTIPLE_VALUES);
   }

   /**
    * @return the memberName
    */
   public String getMemberName() {
      return memberName;
   }

   /**
    * @param memberName
    *           the memberName to set
    */
   public void setMemberName(String memberName) {
      this.memberName = memberName;
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
    * @return the dataType
    */
   public Integer getDataType() {
      return dataType;
   }

   /**
    * @param dataType
    *           the dataType to set
    */
   public void setDataType(Integer dataType) {
      this.dataType = dataType;
   }

   /**
    * @return the valueInterpretation
    */
   public Integer getValueInterpretation() {
      return valueInterpretation;
   }

   /**
    * @param valueInterpretation
    *           the valueInterpretation to set
    */
   public void setValueInterpretation(Integer valueInterpretation) {
      this.valueInterpretation = valueInterpretation;
   }

   /**
    * @return the listValues
    */
   public StringListValue[] getListValues() {
      return listValues;
   }

   /**
    * @param listValues
    *           the listValues to set
    */
   public void setListValues(StringListValue[] listValues) {
      this.listValues = listValues;
   }

   /**
    * @return the tableDefinition
    */
   public CumulusTableFieldDefinition getTableDefinition() {
      return tableDefinition;
   }

   /**
    * @param tableDefinition
    *           the tableDefinition to set
    */
   public void setTableDefinition(CumulusTableFieldDefinition tableDefinition) {
      this.tableDefinition = tableDefinition;
   }

}
