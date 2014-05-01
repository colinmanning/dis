package com.setantamedia.fulcrum.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import com.setantamedia.fulcrum.ws.types.Record;


public class FieldUtilities {

	public final static String JSON_BINARY_VALUE = "<binary>";
	public final static String JSON_PICTURE_VALUE = "<picture>";

	public final static String NULL_VALUE = "null";
	public final static DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	public final static TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

	public FieldUtilities() {
		ISO_DATE_FORMAT.setTimeZone(UTC_TIME_ZONE);
	}
	
	public static String getISODateString(Date dt) {
		return ISO_DATE_FORMAT.format(dt);
	}

	public static Object getFieldValue(FieldValue v, boolean json) {
		Object result = null;
		try {
			switch (v.getDataType()) {
			case FieldTypeConstants.TypeString:
				result = v.getStringValue();
				break;
			case FieldTypeConstants.TypeInteger:
				switch (v.getValueInterpretation()) {
				case FieldTypeConstants.VALUE_DEFAULT:
					result = v.getIntegerValue();
					break;
				case FieldTypeConstants.VALUE_DATA_SIZE:
					result = (json) ? buildJsonDataSizeValue(v.getDataSizeValue()) : v.getDataSizeValue().getValue();
					break;
				default:
					result = v.getIntegerValue();
					break;
				}
				break;
			case FieldTypeConstants.TypeLong:
				result = v.getLongValue();
				break;
			case FieldTypeConstants.TypeDouble:
				result = v.getDoubleValue();
				break;
			case FieldTypeConstants.TypeBool:
				result = v.getBooleanValue();
				break;
			case FieldTypeConstants.TypePicture:
				result = (json) ? JSON_PICTURE_VALUE : v.getByteArrayValue();
				break;
			case FieldTypeConstants.TypeDate:
				//result = v.getDateTimeValue();
				//result = (json) ? "/Date(" + v.getDateTimeValue().getValue() + ")/" : v.getDateTimeValue().getValue();
				result = (json) ? getISODateString(v.getDateTimeValue().getDateValue()) : v.getDateTimeValue().getValue();
				break;
			case FieldTypeConstants.TypeBinary:
				result = (json) ? JSON_BINARY_VALUE : v.getByteArrayValue();
				break;
			case FieldTypeConstants.TypeEnum:
				switch (v.getValueInterpretation()) {
				case FieldTypeConstants.VALUE_DEFAULT:
					result = (json) ? buildJsonStringListValue(v.getStringListValue()[0]) : v.getStringListValue();
					break;
				case FieldTypeConstants.VALUE_STRING_ENUM_MULTIPLE_VALUES:
					result = (json) ? buildJsonStringListValueArray(v.getStringListValue()) : v.getStringListValue();
					break;
				case FieldTypeConstants.VALUE_STRING_ENUM_LABEL:
					result = (json) ? buildJsonLabel(v.getLabelValue()) : v.getLabelValue();
					break;
				default:
					result = (json) ? buildJsonStringListValue(v.getStringListValue()[0]) : v.getStringListValue();
					break;
				}
				break;
			case FieldTypeConstants.TypeTable:
				result = (json) ? buildJsonTable(v.getTableValue(), true) : v.getTableValue();
				break;
			case FieldTypeConstants.TypeRecords:
				result = (json) ? buildJsonRecords(v.getKidsValue(), true) : v.getKidsValue();
				break;
			default:
				result = v.getStringValue();
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result == null) result = JSONObject.NULL;
		return result;
	}

	public static FieldValue createTableFieldValue(DatabaseTable tableFieldDef, DatabaseField fieldDef, String columnName, String value) throws Exception {
		FieldValue result = null;
		//DatabaseTable tableFieldDef = tableFields.get(connection).get(fieldDef.getGuid());
		for (DatabaseField fd : tableFieldDef.getColumns()) {
			if (columnName.equals(fd.getName())) {
				result = createFieldValue(fd, value);
				break;
			}
		}
		return result;
	}

	public static FieldValue createFieldValue(DatabaseField fieldDefinition, String value) throws Exception {
		FieldValue result = new FieldValue();
		result.setFieldDefinition(fieldDefinition);
		result.setDataType(fieldDefinition.getDataType());
		switch (fieldDefinition.getDataType()) {
		case FieldTypeConstants.TypeString:
			result.setStringValue(value);
			break;
		case FieldTypeConstants.TypeInteger:
			result.setIntegerValue(new Integer(value));
			break;
		case FieldTypeConstants.TypeLong:
			result.setLongValue(new Long(value));
			break;
		case FieldTypeConstants.TypeDate:
			DateTime v = new DateTime();
			v.setValue(new Long(value));
			result.setDateTimeValue(v);
			break;
		case FieldTypeConstants.TypeDateTime:
			v = new DateTime();
			v.setValue(new Long(value));
			result.setDateTimeValue(v);
			break;
		case FieldTypeConstants.TypeTimestamp:
			v = new DateTime();
			v.setValue(new Long(value));
			result.setDateTimeValue(v);
			break;
		case FieldTypeConstants.TypeDouble:
			result.setDoubleValue(new Double(value));
			break;
		case FieldTypeConstants.TypeBool:
			result.setBooleanValue(Utilities.isTrue(value));
			break;
		case FieldTypeConstants.TypeEnum:
			switch (fieldDefinition.getValueInterpretation()) {
			case FieldTypeConstants.VALUE_DEFAULT:
				StringListValue[] slv = new StringListValue[1];
				slv[0] = new StringListValue();
				slv[0].setId(new Integer(value));
				result.setStringListValue(slv);
				result.setValueInterpretation(FieldTypeConstants.VALUE_DEFAULT);
				break;
			case FieldTypeConstants.VALUE_STRING_ENUM_RATING:
				slv = new StringListValue[1];
				slv[0] = new StringListValue();
				slv[0].setId(new Integer(value));
				String ratingString = "";
				for (int i=0;i<slv[0].getId();i++) ratingString +="*";
				slv[0].setDisplayString(ratingString);
				result.setStringListValue(slv);
				result.setValueInterpretation(FieldTypeConstants.VALUE_STRING_ENUM_RATING);
				break;
			case FieldTypeConstants.VALUE_STRING_ENUM_MULTIPLE_VALUES:
				// expect a comma separated array of integers
				String[] ids = null;
				try {
					if (!"".equals(value) && !NULL_VALUE.equalsIgnoreCase(value)) {
						ids = value.split(",");
						slv = new StringListValue[ids.length];
						for (int i=0;i<ids.length;i++) {
							slv[i] = new StringListValue();
							slv[i].setId(("".equals(ids[i])) ? -1 : new Integer(ids[i]));
						}
						result.setStringListValue(slv);
						result.setValueInterpretation(FieldTypeConstants.VALUE_STRING_ENUM_MULTIPLE_VALUES);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case FieldTypeConstants.VALUE_STRING_ENUM_LABEL:
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		return result;
	}

	public static JSONArray buildJsonTable(TableValue table) throws Exception {
		return buildJsonTable(table, false);
	}

	public static JSONArray buildJsonTable(TableValue table, boolean isCsharp) throws Exception {
		ArrayList<HashMap> rows = new ArrayList<HashMap>();
		if (table != null) {
			for (int r = 0; r < table.getRows().length; r++) {
				HashMap<String, Object> row = new HashMap<String, Object>();
				if (isCsharp) {
					for (int c = 0; c < table.getColumnNames().length; c++) {
						row.put(table.getColumnNames()[c].replaceAll(" ", "_"), getFieldValue(table.getRows()[r].getColumns()[c], true));
					}
				} else {
					for (int c = 0; c < table.getColumnNames().length; c++) {
						row.put(table.getColumnNames()[c], getFieldValue(table.getRows()[r].getColumns()[c], true));
					}
				}
				rows.add(row);
			}
		}
		return new JSONArray(rows);
	}

	public static JSONArray buildJsonRecords(Record[] records, boolean isCsharp) throws Exception {
		ArrayList<JSONObject> rows = new ArrayList<JSONObject>();
		if (records != null) {
			for (Record record:records) {
				rows.add(record.toJson());
			}
		}
		return new JSONArray(rows);
	}

	public static Object buildJsonLabel(LabelValue label) throws Exception {
		HashMap<String, Object> v = new HashMap<String, Object>();
		v.put("Id", label.getId());
		v.put("DisplayString", label.getDisplayString());
		v.put("Color", label.getColor());
		return new JSONObject(v);
	}

	public static Object buildJsonDataSizeValue(DataSizeValue dataSize) throws Exception {
		HashMap<String, Object> v = new HashMap<String, Object>();
		v.put("Value", dataSize.getValue());
		v.put("DisplayString", dataSize.getDisplayString());
		return new JSONObject(v);
	}

	public static Object buildJsonDateTimeValue(DateTime dateTime) throws Exception {
		HashMap<String, Object> v = new HashMap<String, Object>();
		v.put("Value", dateTime.getValue());
		// this is the "sort of standard" JSON date format
		//v.put("DisplayValue", "\\/Date(" + dateTime.getValue() + ")\\/");
		v.put("DisplayValue", getISODateString(dateTime.getDateValue()));

		return new JSONObject(v);
	}

	public static Object buildJsonStringListValue(StringListValue stringList) throws Exception {
		Object result = null;
		if (stringList != null) {
			HashMap<String, Object> v = new HashMap<String, Object>();
			v.put("Id", stringList.getId());
			v.put("DisplayString", stringList.getDisplayString());
			result = new JSONObject(v);
		}
		return result;
	}

	public static Object buildJsonStringListValueArray(StringListValue[] stringList) throws Exception {
		Object result = null;
		HashMap values[] = new HashMap[stringList.length];
		for (int i = 0; i < stringList.length; i++) {
			HashMap<String, Object> v = new HashMap<String, Object>();
			v.put("Id", stringList[i].getId());
			v.put("DisplayString", stringList[i].getDisplayString());
			values[i] = v;
		}
		result = new JSONArray(values);
		return result;
	}

}