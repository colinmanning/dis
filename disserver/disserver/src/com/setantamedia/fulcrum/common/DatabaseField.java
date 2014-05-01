package com.setantamedia.fulcrum.common;


public class DatabaseField {

	/**
	 * the name to be used for member variables to reference this name (handle speces in field names etc.)
	 * TODO implement this mapping (via web.xml configuration)
	 * TODO make into bean
	 */
	private String displayName = null;
	private String simpleName = null;
	private String name = null;
	private String guid = null;
	private Integer dataType = null;
	private Integer valueInterpretation = null;
	private StringListValue[] listValues = null;
	private DatabaseTable tableDefinition = null;
	private Boolean coreField = true;

	public DatabaseField() {
	}

   public String getQualifiedName() {
      return tableDefinition.getName() + "." + name;
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

	public boolean isRating() {
		return (dataType == FieldTypeConstants.TypeEnum && valueInterpretation == FieldTypeConstants.VALUE_STRING_ENUM_RATING);
	}

	public String getSimpleName() {
		return simpleName;
	}

	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
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

	public Integer getDataType() {
		return dataType;
	}

	public void setDataType(Integer dataType) {
		this.dataType = dataType;
	}

	public Integer getValueInterpretation() {
		return valueInterpretation;
	}

	public void setValueInterpretation(Integer valueInterpretation) {
		this.valueInterpretation = valueInterpretation;
	}

	public StringListValue[] getListValues() {
		return listValues;
	}

	public void setListValues(StringListValue[] listValues) {
		this.listValues = listValues;
	}

	public DatabaseTable getTableDefinition() {
		return tableDefinition;
	}

	public void setTableDefinition(DatabaseTable tableDefinition) {
		this.tableDefinition = tableDefinition;
	}

	public Boolean getCoreField() {
		return coreField;
	}

	public void setCoreField(Boolean coreField) {
		this.coreField = coreField;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}

