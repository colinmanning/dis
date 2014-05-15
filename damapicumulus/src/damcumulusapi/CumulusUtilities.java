package damcumulusapi;

import com.canto.cumulus.*;
import com.canto.cumulus.constants.CombineMode;
import com.canto.cumulus.constants.FindFlag;
import com.canto.cumulus.exceptions.FieldNotFoundException;
import com.canto.cumulus.exceptions.ItemNotFoundException;
import com.canto.cumulus.exceptions.QueryParserException;
import com.setantamedia.fulcrum.common.*;
import com.setantamedia.fulcrum.ws.types.Category;
import com.setantamedia.fulcrum.ws.types.Record;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;

public class CumulusUtilities {

   private static XmlStuff xmlStuff = new XmlStuff();

   public static byte[] getBytesFromAsset(Asset asset) throws IOException {
      byte[] result = null;
      InputDataStream is = asset.openInputDataStream();
      try {
         result = Utilities.getBytesFromInputStream(is);
      } finally {
         if (is != null) {
            is.close();
         }
      }
      return result;
   }

   public static FieldValue createCumulusFieldValue(DatabaseField fieldDefinition, String value) throws Exception {
      FieldValue result = new FieldValue();
      result.setFieldDefinition(fieldDefinition);
      Integer dataType = fieldDefinition.getDataType();
      switch (dataType) {
         case FieldTypes.FieldTypeString:
            result.setStringValue(value);
            result.setDataType(dataType);
            break;
         case FieldTypes.FieldTypeInteger:
            result.setIntegerValue(new Integer(value));
            result.setDataType(dataType);
            break;
         case FieldTypes.FieldTypeLong:
            result.setLongValue(new Long(value));
            result.setDataType(dataType);
            break;
         case FieldTypes.FieldTypeDate:
            result.setDateTimeValue(new DateTime());
            result.getDateTimeValue().setValue(new Long(value));
            result.setDataType(dataType);
            break;
         case FieldTypes.FieldTypeDouble:
            result.setDoubleValue(new Double(value));
            result.setDataType(dataType);
            break;
         case FieldTypes.FieldTypeBool:
            result.setBooleanValue(Utilities.isTrue(value));
            result.setDataType(dataType);
            break;
         case FieldTypes.FieldTypeEnum:
            int valueInterpretation = fieldDefinition.getValueInterpretation();
            switch (valueInterpretation) {
               case FieldTypes.VALUE_INTERPRETATION_DEFAULT:
                  StringListValue[] stringListValue = new StringListValue[1];
                  stringListValue[0] = new StringListValue();
                  stringListValue[0].setId(new Integer(value));
                  result.setStringListValue(stringListValue);
                  result.setDataType(dataType);
                  result.setValueInterpretation(valueInterpretation);
                  break;
               case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_RATING:
                  stringListValue = new StringListValue[1];
                  stringListValue[0] = new StringListValue();
                  stringListValue[0].setId(new Integer(value));
                  String ratingString = "";
                  for (int i = 0; i < stringListValue[0].getId(); i++) {
                     ratingString += "*";
                  }
                  stringListValue[0].setDisplayString(ratingString);
                  result.setStringListValue(stringListValue);
                  result.setDataType(dataType);
                  result.setValueInterpretation(valueInterpretation);
                  break;
               case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_MULTIPLE_VALUES:
                  // expect a comma separated array of integers
                  try {
                     String[] ids = value.split(",");
                     stringListValue = new StringListValue[ids.length];
                     for (int i = 0; i < ids.length; i++) {
                        stringListValue[i] = new StringListValue();
                        stringListValue[i].setId(("".equals(ids[i])) ? -1 : new Integer(ids[i]));
                     }
                     result.setStringListValue(stringListValue);
                     result.setDataType(dataType);
                     result.setValueInterpretation(valueInterpretation);
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
                  break;
               case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_LABEL:
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

   public static String buildXmlAssetReferenceValue(AssetReferenceValue assetReference) throws Exception {
      return "";
   }

   public static String buildXmlLabel(LabelValue label) throws Exception {
      StringBuilder sb = new StringBuilder();
      sb.append(XmlStuff.TAG_LABEL);
      sb.append(XmlStuff.TAG_ID).append(label.getId()).append(XmlStuff.TAG_ID_END);
      sb.append(XmlStuff.TAG_DISPLAYSTRING).append(label.getDisplayString()).append(XmlStuff.TAG_DISPLAYSTRING_END);
      sb.append(XmlStuff.TAG_COLOR).append(label.getColor()).append(XmlStuff.TAG_COLOR_END);
      sb.append(XmlStuff.TAG_LABEL);
      return sb.toString();
   }

   public static String buildXmlDataSizeValue(DataSizeValue dataSize) throws Exception {
      StringBuilder sb = new StringBuilder();
      sb.append(XmlStuff.TAG_DATASIZE);
      sb.append(XmlStuff.TAG_VALUE).append(dataSize.getValue()).append(XmlStuff.TAG_VALUE_END);
      sb.append(XmlStuff.TAG_DISPLAYSTRING).append(dataSize.getDisplayString()).append(XmlStuff.TAG_DISPLAYSTRING_END);
      sb.append(XmlStuff.TAG_DATASIZE_END);
      return sb.toString();
   }

   public static String buildXmlDateTimeValue(DateTime dateTime) throws Exception {
      StringBuilder sb = new StringBuilder();
      sb.append(XmlStuff.TAG_DATETIME);
      sb.append(XmlStuff.TAG_VALUE).append(dateTime.getValue()).append(XmlStuff.TAG_VALUE_END);
      sb.append(XmlStuff.TAG_DISPLAYSTRING).append(xmlStuff.getDateTimeFormatter().format(dateTime.getValue())).append(XmlStuff.TAG_DISPLAYSTRING_END);
      sb.append(XmlStuff.TAG_DATETIME_END);
      return sb.toString();
   }

   public static String buildXmlStringListValue(StringListValue stringList) throws Exception {
      StringBuilder sb = new StringBuilder();
      if (stringList != null) {
         sb.append(XmlStuff.TAG_STRINGLIST);
         sb.append(XmlStuff.TAG_ID).append(stringList.getId()).append(XmlStuff.TAG_ID_END);
         sb.append(XmlStuff.TAG_DISPLAYSTRING).append(stringList.getDisplayString()).append(XmlStuff.TAG_DISPLAYSTRING_END);
         sb.append(XmlStuff.TAG_STRINGLIST_END);
      }
      return sb.toString();
   }

   public static String buildXmlStringListValueArray(StringListValue[] stringList) throws Exception {
      StringBuilder sb = new StringBuilder();
      sb.append(XmlStuff.TAG_STRINGLISTS);
      for (int i = 0; i < stringList.length; i++) {
         sb.append(buildXmlStringListValue(stringList[i]));
      }
      sb.append(XmlStuff.TAG_STRINGLISTS_END);
      return sb.toString();
   }

   public static Object getCumulusFieldValue(FieldValue v, boolean xml) {
      Object result = null;
      try {
         switch (v.getDataType()) {
            case FieldTypes.FieldTypeString:
               result = v.getStringValue();
               break;
            case FieldTypes.FieldTypeInteger:
               result = v.getIntegerValue();
               break;
            case FieldTypes.FieldTypeLong:
               switch (v.getValueInterpretation()) {
                  case FieldTypes.VALUE_INTERPRETATION_DEFAULT:
                     result = v.getLongValue();
                     break;
                  case FieldTypes.VALUE_INTERPRETATION_DATA_SIZE:
                     result = (xml) ? buildXmlDataSizeValue(v.getDataSizeValue()) : v.getDataSizeValue().getValue();
                     break;
                  default:
                     result = v.getLongValue();
                     break;
               }
               break;
            case FieldTypes.FieldTypeDouble:
               result = v.getDoubleValue();
               break;
            case FieldTypes.FieldTypeBool:
               result = v.getBooleanValue();
               break;
            case FieldTypes.FieldTypeDate:
               result = v.getDateTimeValue();
               // result = (json) ? buildJsonDateTimeValue(v.dateTimeValue) :
               // v.dateTimeValue.getValue();
               result = (xml) ? "/Date(" + v.getDateTimeValue().getValue() + ")/" : v.getDateTimeValue().getValue();
               break;
            case FieldTypes.FieldTypeEnum:
               switch (v.getValueInterpretation()) {
                  case FieldTypes.VALUE_INTERPRETATION_DEFAULT:
                     result = (xml) ? buildXmlStringListValue(v.getStringListValue()[0]) : v.getStringListValue();
                     break;
                  case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_MULTIPLE_VALUES:
                     result = (xml) ? buildXmlStringListValueArray(v.getStringListValue()) : v.getStringListValue();
                     break;
                  case FieldTypes.VALUE_INTERPRETATION_STRING_ENUM_LABEL:
                     result = (xml) ? buildXmlLabel(v.getLabelValue()) : v.getLabelValue();
                     break;
                  default:
                     result = (xml) ? buildXmlStringListValue(v.getStringListValue()[0]) : v.getStringListValue();
                     break;
               }
               break;
            case FieldTypes.FieldTypeTable:
               result = (xml) ? buildXmlTable(v.getTableValue(), true) : v.getTableValue();
               break;
            case FieldTypes.FieldTypeBinary:
               result = (xml) ? "<binary>" : v.getByteArrayValue();
               break;
            case FieldTypes.FieldTypePicture:
               result = (xml) ? "<picture>" : v.getByteArrayValue();
               break;
            default:
               result = v.getStringValue();
               break;
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   /**
    *
    * @param table
    * @return
    * @throws Exception
    */
   public static Record[] buildXmlTable(TableValue table) throws Exception {
      return buildXmlTable(table, false);
   }

   /**
    * Ensure field name can be used as a variable name in various programming environments, so no spaces or special
    * characters.
    *
    * " " => "_" "(" => "_LRB_" ")" => "_RRB_" "[" => "_LSB_" "]" => "_RSB_" "/" => "_FWD_" "\" => "_BWD_" "-" =>
    * "_HYP_"
    *
    * @param fieldName
    * @return
    */
   public static String normaliseFieldName(String fieldName) {
      return fieldName.replaceAll(" ", "_").replaceAll("\\(", "_LRB_").replaceAll("\\)", "_RRB_").replaceAll("\\[", "_LSB_").replaceAll("\\]", "_RSB_").replaceAll("\\/", "_FWD_")
              .replaceAll("\\\\", "_BWD_").replaceAll("\\-", "_HYP_");
   }

   /**
    * Revert normalised field names to original field name.
    *
    * " " => "_" "(" => "_LRB_" ")" => "_RRB_" "[" => "_LSB_" "]" => "_RSB_" "/" => "_FWD_" "\" => "_BWD_" "-" =>
    * "_HYP_"
    *
    * @param fieldName
    * @return
    */
   public static String denormaliseFieldName(String fieldName) {
      return fieldName.replaceAll("_LRB_", "\\(").replaceAll("_RRB_", "\\)").replaceAll("_LSB_", "\\[").replaceAll("_RSB_", "\\]").replaceAll("_FWD_", "\\/").replaceAll("_BWD_", "\\\\")
              .replaceAll("_HYP_", "\\-").replaceAll("_", " ");
   }

   /**
    *
    * @param table
    * @param isCsharp
    * @return
    * @throws Exception
    */
   public static Record[] buildXmlTable(TableValue table, boolean isCsharp) throws Exception {
      Record[] result = new Record[table.getRows().length];
      for (int i = 0; i < table.getRows().length; i++) {
         Record record = new Record();
         if (isCsharp) {
            for (int c = 0; c < table.getColumnNames().length; c++) {
               record.addXmlField(denormaliseFieldName(table.getColumnNames()[c]), getCumulusFieldValue(table.getRows()[i].getColumns()[c], true).toString());
            }
         } else {
            for (int c = 0; c < table.getColumnNames().length; c++) {
               record.addXmlField(table.getColumnNames()[c], getCumulusFieldValue(table.getRows()[i].getColumns()[c], true).toString());
            }
         }
         result[i] = record;
      }
      return result;
   }

   public static Path doAssetAction(String connectionName, RecordItem recordItem, String actionName, Path folder) {
      Path result = null;
      try {
         if (actionName != null && !"".equals(actionName)) {
            String fileGuid = Utilities.generateGuid();
            Path workDir = folder.resolve(connectionName + "_" + recordItem.getID() + "_" + fileGuid);
            while (Files.exists(workDir)) {
            // avoid clashes by generating new guids till we avoid a clash - don't really expect clashes
               workDir = folder.resolve(connectionName + "_" + recordItem.getID() + "_" + fileGuid);
            }
            Files.createDirectories(workDir); // and if someone else grapped this one, it will fail here also
            Asset destinationAsset = new Asset(recordItem.getCumulusSession(), workDir.toFile());
            AssetCollection assetCollection = recordItem.doAssetAction(destinationAsset, actionName);
            if (assetCollection != null) {
               Iterator<Asset> it = assetCollection.iterator();
               if (it.hasNext()) {
                  Asset previewAsset = it.next();
                  result = FileSystems.getDefault().getPath(previewAsset.getAsFile().getAbsolutePath());
               }
            }
         }
      } catch (IOException | CumulusException e) {
         e.printStackTrace();
      }
      return result;
   }

   /**
    * Gets full preview for an asset
    *
    * @return
    * @throws Exception
    */
   public static byte[] getPreviewData(String connectionName, RecordItem recordItem, Integer maxSize, String actionName, Path folder) throws Exception {
      byte[] result = null;
      if (actionName != null && !"".equals(actionName)) {
         Path processedFile = doAssetAction(connectionName, recordItem, actionName, folder);
         result = Utilities.getBytesFromPath(processedFile);
         Files.deleteIfExists(processedFile);
      } else {
         result = Pixmap.createFromAsset(recordItem.getAssetReferenceValue(GUID.UID_REC_ASSET_REFERENCE).getAsset(true), null).getAsJPEG();
      }
      return result;
   }

   public static void savePreview(Path file, byte[] data, RecordItem recordItem, String assetAction, Path tmpDir) {
      try {
         if (assetAction != null && !"".equals(assetAction)) {
            String fileGuid = Utilities.generateGuid();
            String fileName = fileGuid + "_" + recordItem.getStringValue(GUID.UID_REC_RECORD_NAME);
            Path filePath = tmpDir.resolve(fileName);
            Asset destinationAsset = new Asset(recordItem.getCumulusSession(), filePath.toFile());
            AssetCollection assetCollection = recordItem.doAssetAction(destinationAsset, assetAction);
            if (assetCollection != null) {
               Iterator<Asset> it = assetCollection.iterator();
               if (it.hasNext()) {
                  //Asset previewAsset = it.next();
                  try (OutputStream fos = Files.newOutputStream(file)) {
                     fos.write(data);
                     fos.flush();
                  }
                  Files.deleteIfExists(filePath);
               }
            }
         } else {
            try (OutputStream fos = Files.newOutputStream(file)) {
               fos.write(data);
               fos.flush();
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static Object getCumulusFieldValue(FieldValue v) {
       return getCumulusFieldValue(v, false);
   }

    public static Category processCategories(CategoryItem rootCategory, boolean recursive) {
        return processCategories(rootCategory, false, recursive, null, null, null);
    }

    public static Category processCategories(CategoryItem rootCategory, boolean detailed, boolean recursive, DatabaseField[] fields, GUID[] categoryFieldGuids, Layout layout) {
      Category result = new Category();
      result.setId(rootCategory.getID());
      result.setName(rootCategory.getStringValue(GUID.UID_CAT_NAME));
      result.setHasChildren(rootCategory.getHasSubCategories());
      if (rootCategory.hasValue(GUID.UID_CAT_CUSTOM_ORDER)) {
         result.setCustomOrder(rootCategory.getIntValue(GUID.UID_CAT_CUSTOM_ORDER));
      }
      CategoryItem childItem = rootCategory.getFirstChildCategoryItem();
      while (childItem != null) {
         Category category = setupCategory(childItem, detailed, fields, categoryFieldGuids, layout);
         if (recursive) {
            result.addSubCategory(processCategories(childItem, detailed, recursive, fields, categoryFieldGuids, layout));
         } else {
            result.addSubCategory(category);
         }
         childItem = childItem.getNextSiblingCategoryItem();
      }
      return result;
   }

    public static Category setupCategory(CategoryItem childItem) {
        return setupCategory(childItem, false);
    }
    public static Category setupCategory(CategoryItem childItem, boolean detailed) {
        return setupCategory(childItem, false, null, null, null);
    }

    public static Category setupCategory(CategoryItem childItem, boolean detailed, DatabaseField[] fields, GUID[] categoryFieldGuids, Layout layout) {
        Category result = new Category();
        result.setId(childItem.getID());
        result.setName(childItem.getStringValue(GUID.UID_CAT_NAME));
        result.setHasChildren(childItem.getHasSubCategories());
        if (childItem.hasValue(GUID.UID_CAT_CUSTOM_ORDER)) {
            result.setCustomOrder(childItem.getIntValue(GUID.UID_CAT_CUSTOM_ORDER));
        }
        if (detailed) {
            try {
                for (int f = 0; f < fields.length; f++) {
                    DatabaseField field = fields[f];
                    FieldValue fieldValue = CumulusHelper.getFieldValue(childItem, categoryFieldGuids[f], layout, Locale.getDefault());
                    fieldValue.setFieldDefinition(field);
                    fieldValue.setDataType(field.getDataType());
                    fieldValue.setValueInterpretation(field.getValueInterpretation());
                    result.addField(field.getName(), fieldValue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Category findCategory(CategoryItemCollection collection, int id) {
      Category result = new Category();
      CategoryItem category = null;
      try {
         category = collection.getCategoryItemByID(id);
         result = new Category();
         result.setId(category.getID());
         result.setName(category.getStringValue(GUID.UID_CAT_NAME));
      } catch (ItemNotFoundException | CumulusException | FieldNotFoundException e) {
         e.printStackTrace();
      }
      return result;
   }
}
