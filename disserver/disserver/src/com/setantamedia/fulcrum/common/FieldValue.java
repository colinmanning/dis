package com.setantamedia.fulcrum.common;

import com.setantamedia.fulcrum.ws.types.Record;
import java.sql.Clob;

public class FieldValue {

    public final static String EMPTY_STRING = "";
    public final static Integer ONE = 1;
    public final static Integer INVALID_INTEGER = -1;
    public final static Integer DEFAULT_INTEGER = 0;
    public final static Long DEFAULT_LONG = 0L;
    public final static Double DEFAULT_DOUBLE = 0.0D;
    public final static Boolean TRUE = true;
    public final static Boolean FALSE = false;
    private DatabaseField fieldDefinition = null;
    private Integer dataType = INVALID_INTEGER;
    private Integer valueInterpretation = INVALID_INTEGER;
    private String stringValue = EMPTY_STRING;
    private Integer integerValue = new Integer(0);
    private Long longValue = DEFAULT_LONG;
    private Double doubleValue = DEFAULT_DOUBLE;
    private Boolean booleanValue = FALSE;
    private Clob clobValue = null;
    private DateTime dateTimeValue = new DateTime();
    private byte[] byteArrayValue = new byte[0];
    private StringListValue[] stringListValue = new StringListValue[0];
    private LabelValue labelValue = new LabelValue();
    private TableValue tableValue = new TableValue();
    private DataSizeValue dataSizeValue = new DataSizeValue();
    private Record[] kidsValue = null;

    public FieldValue() {
    }

    public boolean isDateField() {
        return (dataType == FieldTypeConstants.TypeDate);
    }

    public DatabaseField getFieldDefinition() {
        return fieldDefinition;
    }

    public void setFieldDefinition(DatabaseField fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    public Integer getDataType() {
        return dataType;
    }

    public Object getValue() {
        Object result = null;
        switch (dataType) {
            case FieldTypeConstants.TypeString:
                result = stringValue;
                break;
            case FieldTypeConstants.TypeInteger:
                result = integerValue;
                break;
            case FieldTypeConstants.TypeBool:
                result = booleanValue;
                break;
            case FieldTypeConstants.TypeLong:
                result = longValue;
                break;
            case FieldTypeConstants.TypeDouble:
                result = doubleValue;
                break;
            case FieldTypeConstants.TypeDate:
                result = dateTimeValue;
                break;
            case FieldTypeConstants.TypeClob:
                result = clobValue;
                break;
            case FieldTypeConstants.TypeBinary:
                result = byteArrayValue;
                break;
            case FieldTypeConstants.TypeEnum:
                switch (valueInterpretation) {
                   case FieldTypeConstants.VALUE_DEFAULT:
                        result = stringValue;
                    case FieldTypeConstants.VALUE_STRING_ENUM_LABEL:
                        result = labelValue;
                        break;
                    default:
                        break;
                }
                break;
            default:
                result = stringValue;
        }
        return result;
    }

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
     * @param valueInterpretation the valueInterpretation to set
     */
    public void setValueInterpretation(Integer valueInterpretation) {
        this.valueInterpretation = valueInterpretation;
    }

    public String getStringValue() {
        if (valueInterpretation == FieldTypeConstants.VALUE_STRING_CLOB) {
        }
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        if (valueInterpretation == FieldTypeConstants.VALUE_STRING_CLOB) {
        }
        this.stringValue = stringValue;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    /**
     * @return the doubleValue
     */
    public Double getDoubleValue() {
        return doubleValue;
    }

    /**
     * @param doubleValue the doubleValue to set
     */
    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    /**
     * @return the booleanValue
     */
    public Boolean getBooleanValue() {
        return booleanValue;
    }

    /**
     * @param booleanValue the booleanValue to set
     */
    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    /**
     * @return the dateTimeValue
     */
    public DateTime getDateTimeValue() {
        return dateTimeValue;
    }

    /**
     * @param dateTimeValue the dateTimeValue to set
     */
    public void setDateTimeValue(DateTime dateTimeValue) {
        this.dateTimeValue = dateTimeValue;
    }

    /**
     * @return the byteArrayValue
     */
    public byte[] getByteArrayValue() {
        return byteArrayValue;
    }

    /**
     * @param byteArrayValue the byteArrayValue to set
     */
    public void setByteArrayValue(byte[] byteArrayValue) {
        this.byteArrayValue = byteArrayValue;
    }

    /**
     * @return the stringListValue
     */
    public StringListValue[] getStringListValue() {
        return stringListValue;
    }

    /**
     * @param stringListValue the stringListValue to set
     */
    public void setStringListValue(StringListValue[] stringListValue) {
        this.stringListValue = stringListValue;
    }

    /**
     * @return the labelValue
     */
    public LabelValue getLabelValue() {
        return labelValue;
    }

    /**
     * @param labelValue the labelValue to set
     */
    public void setLabelValue(LabelValue labelValue) {
        this.labelValue = labelValue;
    }

    /**
     * @return the tableValue
     */
    public TableValue getTableValue() {
        return tableValue;
    }

    /**
     * @param tableValue the tableValue to set
     */
    public void setTableValue(TableValue tableValue) {
        this.tableValue = tableValue;
    }

    /**
     * @return the dataSizeValue
     */
    public DataSizeValue getDataSizeValue() {
        return dataSizeValue;
    }

    /**
     * @param dataSizeValue the dataSizeValue to set
     */
    public void setDataSizeValue(DataSizeValue dataSizeValue) {
        this.dataSizeValue = dataSizeValue;
    }

    public Record[] getKidsValue() {
        return kidsValue;
    }

    public void setKidsValue(Record[] kidsValue) {
        this.kidsValue = kidsValue;
    }

    public Clob getClobValue() {
        return clobValue;
    }

    public void setClobValue(Clob clobValue) {
        this.clobValue = clobValue;
    }
}
