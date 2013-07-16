package damcumulusapi;

import com.canto.cumulus.*;
import com.canto.cumulus.constants.CatalogingFlag;
import com.canto.cumulus.exceptions.InvalidArgumentException;
import com.canto.cumulus.exceptions.ItemNotFoundException;
import com.canto.cumulus.utils.ImagingPixmap;
import com.canto.cumulus.utils.PlatformUtils;
import com.setantamedia.fulcrum.DamManager;
import com.setantamedia.fulcrum.DamManagerNotImplementedException;
import com.setantamedia.fulcrum.common.*;
import com.setantamedia.fulcrum.config.FulcrumConfig;
import com.setantamedia.fulcrum.config.Preview;
import com.setantamedia.fulcrum.dam.entities.Folder;
import com.setantamedia.fulcrum.models.core.Person;
import com.setantamedia.fulcrum.ws.types.Category;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;
import com.setantamedia.fulcrum.ws.types.User;
import java.io.FileNotFoundException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.log4j.Logger;

public class CumulusManager extends DamManager {

   private final static Logger logger = Logger.getLogger(CumulusManager.class);
   public final static String FORMAT_PNG = "PNG";
   public final static String FORMAT_JPEG = "JPEG";
   public final static String DEFAULT_ASSET_HANDLING_SET = "Standard";
   private CumulusHelper helper = null;
   private Boolean cumulusStarted = false;
   private FileSystem fs = FileSystems.getDefault();
   public final static String CATEGORY_PROJECTS = "$Categories:Projects";

   public CumulusManager() {
      super();
   }

   /**
    * Initialisation the manager, to be called after all relevant setters have
    * been called, and makes sure the helper is correctly configured. In
    * particular, calling this before calling setConfig() is bad.
    */
   @Override
   public void init() {
      super.init();
      try {
         logger.info("Initialising Cumulus");
         Cumulus.CumulusStart();
         cumulusStarted = true;
         this.tmpDir = fs.getPath(config.getTmpFolder());
         helper = new CumulusHelper();
         logger.info("Cumulus Helper created");
         helper.setViews(views);
         helper.setTmpDir(tmpDir);
         helper.setBaseUrl(this.baseUrl);
         helper.setServerPrefix(this.serverPrefix);
         logger.info("tmpDir: " + tmpDir.toString());
         logger.info("baseUrl: " + this.baseUrl);
         logger.info("serverPrefix: " + this.serverPrefix);

         /*
          * log the custom parameters
          */
         for (Map.Entry<String, Connection> connectionEntry : connections.entrySet()) {
            logger.info("Custom param count: " + connectionEntry.getValue().getParams().size());
            logger.info("Connection: " + connectionEntry.getKey());
            for (Map.Entry<String, String> paramEntry : connectionEntry.getValue().getParams().entrySet()) {
               logger.info("   Custom Parameter: " + paramEntry.getKey() + " set to " + paramEntry.getValue());
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void terminate() {
      if (cumulusStarted) {
         Cumulus.CumulusStop();
      }
      super.terminate();
   }

   @Override
   public QueryResult categorySearch(Connection connection, String categoryId, SearchDescriptor searchDescriptor, Boolean recursive, Locale locale) {
      QueryResult result = null;
      CumulusCollectionManager collectionManager = helper.getOrInitCollectionManager(connection);
      MultiRecordItemCollection c = null;
      RecordItemCollection rc = null;
      try {
         String query = GUID.UID_REC_CATEGORIES.toString() + " == ";
         if (recursive) {
            query += "\":B " + categoryId + ":\"";
         } else {
            query += ":" + categoryId + ":";
         }
         String quickSearch = searchDescriptor.getFilter();
         if (quickSearch != null && !"".equals(quickSearch)) {
            c = null;
            rc = null;
            try {
               c = collectionManager.getMasterServer().newMultiRecordItemCollection();
               rc = (RecordItemCollection) collectionManager.cloneObjectToRead(RecordItemCollection.class);
               c.addItemCollection(rc);
               String qs = PlatformUtils.convertQuickSearchToComplexQuery(quickSearch, c);
               query += " && " + qs;
            } catch (Exception e) {
               e.printStackTrace();
            } finally {
               if (rc != null) {
                  rc.close();
               }
               if (c != null) {
                  c.close();
               }
            }
         }
         String namedQuery = searchDescriptor.getNamedQuery();
         if (namedQuery != null && !"".equals(namedQuery)) {
            query += " && " + namedQuery;
         }
         // System.out.println("Category Query: "+query);
         result = helper.findRecords(connection, query, searchDescriptor);
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (rc != null) {
            rc.close();
         }
         if (c != null) {
            c.close();
         }
      }
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.acalian.dam.ws.DamManager#textSearch(java.lang.String,
    * java.lang.String, com.acalian.dam.common.SearchDescriptor)
    */
   @Override
   public QueryResult textSearch(Connection connection, String text, SearchDescriptor searchDescriptor, Locale locale) {
      QueryResult result = null;
      CumulusCollectionManager collectionManager = helper.getOrInitCollectionManager(connection);
      String query;
      MultiRecordItemCollection c = null;
      RecordItemCollection rc = null;
      try {
         c = collectionManager.getMasterServer().newMultiRecordItemCollection();
         // make sure we clone for now, as multi catalog collection close the
         // added collections
         rc = (RecordItemCollection) collectionManager.cloneObjectToRead(RecordItemCollection.class);
         c.addItemCollection(rc);
         query = PlatformUtils.convertQuickSearchToComplexQuery(text, c);
         String filter = searchDescriptor.getFilter();
         if (filter != null && !"".equals(filter)) {
            // extra conditions specified on top of the quick search
            query += " && (" + filter + ")";
         }
         logger.debug("Query is: '" + query + "'");
         result = helper.findRecords(connection, query, searchDescriptor);
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (rc != null) {
            rc.close();
         }
         if (c != null) {
            c.close();
         }
      }
      return result;
   }

   @Override
   public byte[] getThumbnail(Connection connection, String id, Integer maxSize, SearchDescriptor searchDescriptor) {
      return helper.getThumbnail(connection, id, maxSize, searchDescriptor);
   }

   @Override
   public FileStreamer getFile(Connection connection, User user, String id, Integer version, String actionName) {
      FileStreamer result = null;
      try {
         result = helper.downloadAsset(connection, new Integer(id), version, actionName);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   @Override
   public String uploadFile(Connection connection, User user, Path file, String fileName, String uploadProfile, HashMap<String, String> fields) {
      String result = null;

      CumulusCollectionManager collectionManager = null;
      RecordItemCollection collection = null;
      CumulusImporter catalogingListener = null;
      try {
         collectionManager = helper.getOrInitCollectionManager(connection);
         collection = (RecordItemCollection) collectionManager.borrowObjectToWrite(RecordItemCollection.class);
         if (collection != null) {
            synchronized (collection) {
               try {
                  catalogingListener = new CumulusImporter();
                  collection.addCatalogingListener(catalogingListener);
                  logger.debug("starting upload: " + file.getFileName().toString() + " ...");
                  Asset asset = new Asset(collection.getCumulusSession(), file.toFile());
                  String ahs = uploadProfile;
                  if (ahs == null || "".equals(ahs)) {
                     ahs = DEFAULT_ASSET_HANDLING_SET;
                  }
                  collection.catalogAsset(asset, ahs, null, EnumSet.noneOf(CatalogingFlag.class), catalogingListener);
                  Integer cId = catalogingListener.getItemId();
                  logger.debug("upload: " + file.getFileName().toString() + " done - asset id is: " + cId);
                  result = cId.toString();
                  if (cId != -1 && (fields.size() > 0 || (fileName != null && !"".equals(fileName)))) {
                     updateAssetData(connection, result, fileName, fields);
                  }
               } catch (FileNotFoundException | InvalidArgumentException | CumulusException e) {
                  e.printStackTrace();
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (catalogingListener != null) {
            collection.removeCatalogingListener(catalogingListener);
         }
         collectionManager.returnWriteObject(collection);
      }
      return result;
   }

   @Override
   public String uploadFile(Connection connection, User user, Folder folder, Path file, String fileName, String uploadProfile, HashMap<String, String> fields) throws DamManagerNotImplementedException {
      String damId = uploadFile(connection, user, file, fileName, uploadProfile, fields);
      if (damId != null) {
         addRecordToCategory(connection, String.valueOf(folder.getId()), damId);
      }
      return damId;
   }

   @Override
   public boolean updateAssetData(Connection connection, String id, String fileName, HashMap<String, String> fields) {
      return helper.updateRecord(connection, null, null, id.toString(), fileName, fields);
   }

   @Override
   public Category createCategory(Connection connection, User user, String path) {
      return helper.createCategory(connection, path);
   }

   @Override
   public Folder createFolder(Connection connection, User user, String path) {
      Folder result;
      Category folderCategory = helper.createCategory(connection, path);
      result = new Folder();
      result.setId(String.valueOf(folderCategory.getId()));
      result.setName(folderCategory.getName());
      return result;
   }

   @Override
   public Category createSubCategory(Connection connection, User user, Integer parentId, String path) {
      return helper.createSubCategory(connection, parentId, path);
   }

   @Override
   public Category findCategories(Connection connection, String path) {
      Category result = null;
      CategoryItem rootCategory;
      CumulusCollectionManager collectionManager;
      try {
         collectionManager = helper.getOrInitCollectionManager(connection);
         CategoryItemCollection collection = collectionManager.getAllCategoriesItemCollection();
         if (collection != null) {
            rootCategory = collection.getCategoryItemByID(collection.getCategoryTreeItemIDByPath(path));
            result = CumulusUtilities.processCategories(rootCategory);
         }
      } catch (InvalidArgumentException | ItemNotFoundException | CumulusException e) {
         e.printStackTrace();
      }
      return result;
   }

   @Override
   public Date getModifiedTime(Connection connection, String id) throws DamManagerNotImplementedException {
      Date result = null;
      CumulusCollectionManager collectionManager = null;
      RecordItem recordItem = null;
      try {
         collectionManager = helper.getOrInitCollectionManager(connection);
         recordItem = collectionManager.getRecordToRead(new Integer(id), false);
         result = recordItem.getDateValue(GUID.UID_REC_ASSET_MODIFICATION_DATE);
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (collectionManager != null && recordItem != null) {
            collectionManager.releaseReadRecordItem(recordItem);
         }
      }
      return result;
   }

   @Override
   public byte[] previewFile(Connection connection, String id, Integer top, Integer left, Integer height, Integer width, Integer maxSize, Integer size, Integer rotate, String format, Integer quality, Path file, String actionName) {
      byte[] result = new byte[0];
      CumulusCollectionManager collectionManager = null;
      RecordItemCollection collection = null;
      RecordItem recordItem = null;
      try {
         collectionManager = helper.getOrInitCollectionManager(connection);
         collection = (RecordItemCollection) collectionManager.borrowObjectToRead(RecordItemCollection.class);
         ImagingPixmap pixmap = collectionManager.getPixmapById(collection, new Integer(id));
         if (pixmap == null) {
            return result;
         }
         try {
            int theWidth = pixmap.getWidth();
            int theHeight = pixmap.getHeight();

            // Cropping
            int cropTop = 0;
            int cropLeft = 0;
            if (top != null) {
               cropTop = Math.max(0, Math.min(top, height - 1));
            }
            if (left != null) {
               cropLeft = Math.max(0, Math.min(left, width - 1));
            }
            if (height != null) {
               theHeight = Math.max(0, Math.min(height, height));
            }
            if (width != null) {
               theWidth = Math.max(0, Math.min(width, width));
            }
            if (cropTop != 0 || cropLeft != 0 || theHeight != pixmap.getHeight() || theWidth != pixmap.getWidth()) {
               pixmap.crop(cropLeft, cropTop, theWidth, theHeight);
               width = pixmap.getWidth();
               height = pixmap.getHeight();
            }

            // Scaling
            if (maxSize != null && (maxSize < theWidth || maxSize < theHeight)) {
               if (theWidth > theHeight) {
                  theHeight = (int) ((long) maxSize * theHeight / theWidth);
                  theWidth = maxSize;
               } else {
                  // portrait
                  theWidth = (int) ((long) maxSize * theWidth / theHeight);
                  theHeight = maxSize;
               }
               pixmap.scale(theWidth, theHeight);
               theWidth = pixmap.getWidth();
               theHeight = pixmap.getHeight();
            } else if (size != null && (size < theWidth || size < theHeight)) {
               if (theWidth > theHeight) {
                  theWidth = (int) ((long) size * theWidth / theHeight);
                  theHeight = size;
               } else {
                  theHeight = (int) ((long) size * theHeight / theWidth);
                  theWidth = size;
               }
               pixmap.scale(theWidth, theHeight);
               theWidth = pixmap.getWidth();
               theHeight = pixmap.getHeight();
               // crop overlap
               if (theWidth > size) {
                  pixmap.crop((theWidth - size) / 2, 0, size, size);
               } else if (theHeight > size) {
                  pixmap.crop(0, (theHeight - size) / 2, size, size);
               }
            }

            // Rotating
            if (rotate != null && rotate != 0) {
               switch (rotate) {
               case 1:
                  pixmap.rotate(ImagingPixmap.RotateAngle.CW_90);
                  break;
               case 2:
                  pixmap.rotate(ImagingPixmap.RotateAngle.CW_180);
                  break;
               case 3:
                  pixmap.rotate(ImagingPixmap.RotateAngle.CW_270);
                  break;
               }
            }

            Integer cl = 7;
            if (quality != null && quality > 0 && quality <= 10) {
               cl = quality;
            }
            if (Files.exists(file)) {
               logger.debug("file: " + file.toString() + " exists in preview cache - this is a surprise - will try to delete.");
               Files.delete(file);
            }

            /*
             * TODO see if we can sort this out I don't see how we can do this,
             * as ImagingPixMap only works on RecordItems, and asset actions
             * return physical files So the preview would have to be cataloged
             * to do this, which is not on if (actionName != null &&
             * !"".equals(actionName)) { Path processedFile =
             * CumulusUtilities.doAssetAction(connection.getName(), recordItem,
             * actionName, tmpDir); }
             */

            logger.debug("about to do preview - file is: " + file.toString());
            if (FORMAT_PNG.equalsIgnoreCase(format)) {
               // may be transparent
               logger.debug("about to generate PNG preview file: " + file.toString());
               ImagingPixmap.ColorSpace colorSpace = pixmap.getColorSpace();
               pixmap.save(file.toFile(), ImagingPixmap.Format.PNG, colorSpace.equals(ImagingPixmap.ColorSpace.RGBA) ? colorSpace : ImagingPixmap.ColorSpace.RGB, ImagingPixmap.Compression.FLATE, cl, false, 0);
            } else {
               logger.debug("about to generate JPG  preview file: " + file.toString());
               pixmap.save(file.toFile(), ImagingPixmap.Format.JPEG, ImagingPixmap.ColorSpace.RGB, ImagingPixmap.Compression.JPEG, cl, false, 0);
            }
         } catch (Exception ed) {
            ed.printStackTrace();
         } finally {
            if (pixmap != null) {
               pixmap.close();
            }
            result = Files.readAllBytes(file);
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (collectionManager != null && recordItem != null) {
            collectionManager.releaseReadRecordItem(recordItem);
         }         
      }
      return result;
   }

   @Override
   public byte[] previewFile(Connection connection, String id, Path cacheFile, Integer maxSize, String actionName) {
      byte[] result = new byte[0];
      CumulusCollectionManager collectionManager = null;
      RecordItem recordItem = null;
      try {
         collectionManager = helper.getOrInitCollectionManager(connection);
         recordItem = collectionManager.getRecordItemById(new Integer(id), false);
         result = CumulusUtilities.getPreviewData(connection.getName(), recordItem, maxSize, actionName, tmpDir);
         Utilities.savePreview(cacheFile, result);
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (collectionManager != null && recordItem != null) {
            collectionManager.releaseReadRecordItem(recordItem);
         }
      }
      return result;
   }

   @Override
   public Path getTmpDir() {
      return tmpDir;
   }

   @Override
   public void setTmpDir(Path tmpDir) {
      this.tmpDir = tmpDir;
      helper.setTmpDir(tmpDir);
   }

   @Override
   public byte[] previewFile(Connection connection, String id, Preview previewData, Path file, String actionName) {
      if (previewData.getSize() != null) {
         return previewFile(connection, id, null, null, null, null, null, previewData.getSize(), previewData.getRotate(), previewData.getFormat(), previewData.getCompression(), file, actionName);
      }
      return previewFile(connection, id, previewData.getTop(), previewData.getLeft(), previewData.getHeight(), previewData.getWidth(), previewData.getMaxsize(), null, previewData.getRotate(), previewData.getFormat(), previewData.getCompression(), file, actionName);
   }

   @Override
   public DatabaseField[] getFields(Connection connection) {
      return getFields(connection, null);
   }

   @Override
   public void removeTemporaryFile(Path file) {
      // check file exists in the temp folder
      try {
         Path parentFolder = file.getParent();
         if (Files.exists(file)) {
            Files.delete(file);
            if (tmpDir.compareTo(parentFolder.getParent()) == 0) {
               // file created in sup folder in tmp, and should go - have been
               // done as GUID
               Files.delete(parentFolder);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
         logger.error("Problem deleting temporary file: " + file.toString());
      }
   }

   @Override
   public void setConfig(FulcrumConfig config) {
      this.config = config;
   }

   @Override
   public DatabaseField[] getFields(Connection connection, String view) {
      return helper.getFields(connection, view);
   }

   @Override
   public Map<String, String> getPreviews(Connection connection, String view) {
      return helper.getPreviews(connection, view);
   }

   @Override
   public Map<String, String> getLinks(Connection connection, String view) {
      return helper.getLinks(connection, view);
   }

   @Override
   public Map<String, String> getReferences(Connection connection, String view) {
      return helper.getReferences(connection, view);
   }

   @Override
   public Object getFieldValue(FieldValue v, boolean json) {
      return FieldUtilities.getFieldValue(v, json);
   }

   @Override
   public Record getFileMetadata(Connection connection, String id, String view, Locale locale) {
      return helper.getFileMetadata(connection, new Integer(id), view, locale);
   }

   @Override
   public boolean updateRecord(Connection connection, DatabaseField rootFieldDef, String tablePath, String recordKey, HashMap<String, String> fieldValues) {
      return helper.updateRecord(connection, rootFieldDef, tablePath, recordKey, null, fieldValues);
   }

   @Override
   public boolean addRecordToCategory(Connection connection, String categoryId, String recordId) {
      return helper.addRecordToCategories(connection, new Integer[] { new Integer(categoryId) }, new Integer(recordId));
   }

   @Override
   public boolean addRecordToCategories(Connection connection, String[] categoryIds, String recordId) {
      Integer[] cids = new Integer[categoryIds.length];
      for (int i = 0; i < cids.length; i++) {
         cids[i] = new Integer(categoryIds[i]);
      }
      return helper.addRecordToCategories(connection, cids, new Integer(recordId));
   }

   @Override
   public boolean removeRecordFromCategory(Connection connection, String categoryId, String recordId) {
      return helper.removeRecordFromCategory(connection, new Integer(categoryId), new Integer(recordId));
   }

   @Override
   public boolean registerConnection(String name, Connection connection) {
      boolean result = true;
      try {
         result = true;
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   @Override
   public boolean deregisterConnection(String name, Connection connection) {
      boolean result = true;
      try {
         result = true;
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   @Override
   public Person userSingleSignOn(Connection connection, String username) {
      return helper.userSingleSignOn(connection, username);
   }

   @Override
   public Person getUser(Connection connection, String username, String password) {
      return helper.getUser(connection, username, password);
   }

   @Override
   public boolean activateUser(Connection connection, String username, boolean activate) {
      return helper.activateUser(connection, username, activate);
   }

   @Override
   public DatabaseQuery[] getQueries(Connection connection) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public QueryResult querySearch(Connection connection, User user, String query, SearchDescriptor searchDescriptor, Locale locale) {
      QueryResult result = null;
      try {
         result = helper.findRecords(connection, query, searchDescriptor);
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
      }
      return result;
   }

   @Override
   public String queryForRecordId(Connection connection, String query) throws DamManagerNotImplementedException {
      String result = Record.NULL_RECORD_ID;
      try {
         RecordItem record = helper.findRecordByQuery(connection, query);
         result = String.valueOf(record.getID());
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
      }
      return result;
   }

   @Override
   public boolean incrementFieldValue(Connection connection, String id, String field, int amount) {
      return helper.incrementFieldValue(connection, id, field, amount);
   }

   @Override
   public boolean decrementFieldValue(Connection connection, String id, String field, int amount) {
      return helper.decrementFieldValue(connection, id, field, amount);
   }

   @Override
   public boolean openConnection(Connection connection, String username, String password, Integer poolSize) throws DamManagerNotImplementedException {
      return helper.openConnection(connection, username, password, poolSize);
   }

   @Override
   public boolean closeConnection(Connection connection) throws DamManagerNotImplementedException {
      return helper.closeConnection(connection);
   }
}