package damcumulusapi;

import com.canto.cumulus.*;
import com.canto.cumulus.constants.CombineMode;
import com.canto.cumulus.constants.FindFlag;
import com.canto.cumulus.constants.SortDirection;
import com.canto.cumulus.exceptions.ItemNotFoundException;
import com.canto.cumulus.exceptions.LoginFailedException;
import com.canto.cumulus.exceptions.PasswordExpiredException;
import com.canto.cumulus.exceptions.PermissionDeniedException;
import com.canto.cumulus.exceptions.QueryParserException;
import com.canto.cumulus.exceptions.ServerNotFoundException;
import com.canto.cumulus.utils.ImagingPixmap;
import com.setantamedia.fulcrum.DatabaseManager;
import com.setantamedia.fulcrum.common.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;

public class CumulusCollectionManager extends DatabaseManager {

   private static Logger logger = Logger.getLogger(CumulusCollectionManager.class);
   public final static int CLONE_COUNT = 15;
   public final static int MIN_READ_ONLY_COUNT = 0; // if read only did not use license, could set this to something useful
   private final ConcurrentLinkedQueue<ItemCollection> recordPool = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<ItemCollection> categoryPool = new ConcurrentLinkedQueue<>();
   private Connection connection = null;
   private Server[] servers = null;
   private Catalog[] catalogs = null;
   private Server server = null;
   private Catalog catalog = null;
   private RecordItemCollection recordCollection = null;
   private CategoryItemCollection categoryCollection = null;
   private Layout recordLayout = null;
   private Layout categoryLayout = null;
   private int serverCount = 0;
   private ServerMonitor monitor = null;
   private Boolean onDemand = false;

   public Connection getConnection() {
      return connection;
   }

   public void setConnection(Connection connection) {
      this.connection = connection;
   }

   public CumulusCollectionManager() {
   }

   public Layout getRecordLayout() {
      return recordLayout;
   }

   public Layout getCategoryLayout() {
      return categoryLayout;
   }

   @Override
   public boolean init(Connection connection) {
      boolean result = false;
      // in case of re-entrant call, make sure old stuff is killed off
      terminate();
      this.connection = connection;
      this.serverCount = connection.getPoolSize();

      try {
         if (serverCount >= 0) {
            onDemand = false;
            try {
               if (connection.isReadOnly()) {
                  server = Server.openConnection(false, connection.getServer(), connection.getUsername(), connection.getPassword());
               } else {
                  server = Server.openConnection(true, connection.getServer(), connection.getUsername(), connection.getPassword());
               }
            } catch (CumulusException e) {
               e.printStackTrace();
            }
            this.connection.setId(server.findCatalogID(connection.getDatabase()));
            catalog = server.openCatalog(this.connection.getId());
            recordCollection = catalog.newRecordItemCollection(true);
            categoryCollection = catalog.newCategoryItemCollection();
            recordCollection.findAll();
            categoryCollection.findAll();
            recordLayout = recordCollection.getLayout();
            categoryLayout = categoryCollection.getLayout();
            recordPool.add(recordCollection.clone());
            categoryPool.add(categoryCollection.clone());
            for (int j = 0; j < CLONE_COUNT; j++) {
               recordPool.add(recordCollection.clone());
               categoryPool.add(categoryCollection.clone());
            }

            if (connection.isReadOnly() && serverCount < MIN_READ_ONLY_COUNT) {
               serverCount = MIN_READ_ONLY_COUNT;
            }
            if (serverCount > 0) {
               servers = new Server[serverCount];
               catalogs = new Catalog[serverCount];
               for (int i = 0; i < this.serverCount; i++) {
                  if (connection.isReadOnly()) {
                     servers[i] = Server.openConnection(false, connection.getServer(), connection.getUsername(), connection.getPassword());
                  } else {
                     servers[i] = Server.openConnection(true, connection.getServer(), connection.getUsername(), connection.getPassword());
                  }
                  catalogs[i] = servers[i].openCatalog(this.connection.getId());
                  setupServerCollections(catalogs[i]);
               }
            }
            monitor = new ServerMonitor();
            monitor.start();
         } else {
            onDemand = true;
         }

         result = true;
      } catch (LoginFailedException | PasswordExpiredException | ServerNotFoundException | CumulusException e) {
         e.printStackTrace();
      }
      return result;
   }

   private void setupServerCollections(Catalog catalog) {
      RecordItemCollection rc = catalog.newRecordItemCollection(true);
      CategoryItemCollection cc = catalog.newCategoryItemCollection();
      recordPool.add(rc);
      categoryPool.add(cc);
      for (int j = 1; j < CLONE_COUNT; j++) {
         recordPool.add(rc.clone());
         categoryPool.add(cc.clone());
      }
   }

   /**
    * Terminate the pool and close down the pool monitor
    */
   public void terminate() {
      try {
         if (monitor != null) {
            monitor.interrupt();
         }
         if (recordCollection != null) {
            recordCollection.close();
         }
         if (categoryCollection != null) {
            categoryCollection.close();
         }
         while (!recordPool.isEmpty()) {
            recordPool.remove().close();
         }
         while (!categoryPool.isEmpty()) {
            categoryPool.remove().close();
         }
         recordPool.clear();
         categoryPool.clear();
         if (server != null) {
            server.sendServerModuleMessage("{8c94922f-c958-4444-a232-c095b17bce17}", null);
         }
         if (servers != null) {
            for (Server s : servers) {
               s.sendServerModuleMessage("{8c94922f-c958-4444-a232-c095b17bce17}", null);
            }
         }
         server = null;
         servers = null;
         catalogs = null;
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (monitor != null) {
            monitor.interrupt();
         }
      }
   }

   public AllCategoriesItemCollection getAllCategoriesItemCollection(boolean toWrite) {
      AllCategoriesItemCollection result = null;
      if (onDemand) {
         borrowOnDemand(CategoryItemCollection.class);
         result = catalog.getAllCategoriesItemCollection();
      } else {
         result = (toWrite) ? catalogs[0].getAllCategoriesItemCollection() : catalog.getAllCategoriesItemCollection();
      }
      return result;
   }

   public AllCategoriesItemCollection getAllCategoriesItemCollection() {
       return getAllCategoriesItemCollection(false);
   }

   public Catalog getMasterCatalog() {
      return catalog;
   }

   public Server getMasterServer() {
      return server;
   }

   public ImagingPixmap getPixmapById(RecordItemCollection collection, int id) {
      ImagingPixmap result = null;
      RecordItem recordItem = null;
      if (collection != null) {
         synchronized (collection) {
            try {
               if (collection.hasItem(id)) {
                  recordItem = collection.getRecordItemByID(id);
               } else {
                  collection.addItemByID(id);
                  recordItem = collection.getRecordItemByID(id);
               }
            } catch (ItemNotFoundException | CumulusException e) {
               // do find all and try again
               collection.findAll();
               if (collection.hasItem(id)) {
                  recordItem = collection.getRecordItemByID(id);
               } else {
                  collection.addItemByID(id);
                  recordItem = collection.getRecordItemByID(id);
               }
            } finally {
               if (recordItem != null) {
                  result = ImagingPixmap.getPixmap(recordItem);
               }
            }
         }
      }
      return result;
   }

   public RecordItem getRecordToRead(int id, boolean keepCollectionAlive) throws Exception {
      RecordItem result = null;
      RecordItemCollection collection = (RecordItemCollection) borrowObjectToRead(RecordItemCollection.class);
      if (collection != null) {
         synchronized (collection) {
            try {
               if (collection.hasItem(id)) {
                  result = collection.getRecordItemByID(id);
               } else {
                  collection.addItemByID(id);
                  result = collection.getRecordItemByID(id);
               }
            } catch (ItemNotFoundException | CumulusException e) {
               // do find all and try again
               collection.findAll();
               if (collection.hasItem(id)) {
                  result = collection.getRecordItemByID(id);
               } else {
                  collection.addItemByID(id);
                  result = collection.getRecordItemByID(id);
               }
            } finally {
               if (!keepCollectionAlive) {
                  returnReadObject(collection);
               }
            }
         }
      }
      return result;

   }

   public RecordItem getRecordItemById(int id, boolean toWrite) throws Exception {
      return getRecordItemById(id, toWrite, false);
   }

   public RecordItem getRecordItemById(int id, boolean toWrite, boolean returnObject) throws Exception {
      RecordItem result = null;
      RecordItemCollection collection = null;
      if (!toWrite) {
         collection = (RecordItemCollection) borrowObjectToRead(RecordItemCollection.class);
      } else {
         collection = (RecordItemCollection) borrowObjectToWrite(RecordItemCollection.class);
      }
      if (collection != null) {
         synchronized (collection) {
            try {
               if (collection.hasItem(id)) {
                  result = collection.getRecordItemByID(id);
               } else {
                  collection.addItemByID(id);
                  result = collection.getRecordItemByID(id);
               }
            } catch (ItemNotFoundException | CumulusException e) {
               // do find all and try again
               collection.findAll();
               if (collection.hasItem(id)) {
                  result = collection.getRecordItemByID(id);
               } else {
                  collection.addItemByID(id);
                  result = collection.getRecordItemByID(id);
               }
            } finally {
               if (returnObject) {
                  if (!toWrite) {
                     returnReadObject(collection);
                  } else {
                     returnWriteObject(collection);
                  }
               }
            }
         }
      }
      return result;
   }

   public Integer findRecord(String query, String locale) throws QueryParserException {
      Integer result = -1;
      RecordItemCollection collection = null;
      synchronized (collection) {
         try {
            collection = (RecordItemCollection) borrowObjectToRead(RecordItemCollection.class);
            collection.find(query, EnumSet.of(FindFlag.FIND_MISSING_FIELDS_REPLACE), CombineMode.FIND_NEW, Utilities.getLocale(locale));
            if (collection.getItemCount() > 0) {
               result = collection.getItemIDs(0, 1).get(0);
            } else {
               // retry
               returnReadObject(collection);
               collection = (RecordItemCollection) borrowObjectToRead(RecordItemCollection.class, true);
               collection.find(query, EnumSet.of(FindFlag.FIND_MISSING_FIELDS_REPLACE), CombineMode.FIND_NEW, Utilities.getLocale(locale));
               if (collection.getItemCount() > 0) {
                  result = collection.getItemIDs(0, 1).get(0);
               }
            }
         } catch (QueryParserException | CumulusException e) {
            e.printStackTrace();
         } finally {
            returnReadObject(collection);
         }
      }
      return result;
   }

   public RecordItem queryForRecord(String query) throws QueryParserException, CumulusException {
      RecordItem result = null;
      RecordItemCollection collection = null;
      try {
         collection = (RecordItemCollection) borrowObjectToRead(RecordItemCollection.class, false);
         if (collection != null) {
            synchronized (collection) {
               try {
                  collection.find(query, EnumSet.of(FindFlag.FIND_MISSING_FIELDS_REPLACE), CombineMode.FIND_NEW, Locale.getDefault());
                  List<Integer> ids = collection.getItemIDs(0, 1);
                  if (ids.size() > 0) {
                     result = collection.getRecordItemByID(ids.get(0));
                  }
               } catch (QueryParserException | CumulusException e) {
                  e.printStackTrace();
               } finally {
                  returnReadObject(collection);
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   public ResultSet findRecords(String query, Integer offset, Integer count, SortRule sortBy, Locale locale) throws QueryParserException, CumulusException {
      ResultSet result = new ResultSet();
      RecordItemCollection collection = null;
      try {
         collection = (RecordItemCollection) borrowObjectToRead(RecordItemCollection.class, false);
         if (collection != null) {
            synchronized (collection) {
               try {
                  result = findRecords(collection, Common.QueryTypes.New, query, offset, count, sortBy, locale);
               } catch (QueryParserException | CumulusException e) {
                  e.printStackTrace();
               } finally {
                  if (result != null) {
                     result.setCollection(collection);
                  } else {
                     returnReadObject(collection);
                  }
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   public ResultSet findRecords(SearchDescriptor searchDescriptor) throws QueryParserException, CumulusException {
      ResultSet result = new ResultSet();
      RecordItemCollection collection = null;
      try {
         collection = (RecordItemCollection) borrowObjectToRead(RecordItemCollection.class, false);
         if (collection != null) {
            synchronized (collection) {
               try {
                  result = findRecords(collection, Common.QueryTypes.New, searchDescriptor.getFilter(), searchDescriptor.getOffset(), searchDescriptor.getCount(), searchDescriptor.getSortRule(),
                        searchDescriptor.getLocale());
               } catch (Exception e) {
                  e.printStackTrace();
               } finally {
                  returnReadObject(collection);
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   public ResultSet findRecords(RecordItemCollection collection, Common.QueryTypes queryType, String query, Integer offset, Integer count, SortRule sortRule, Locale locale)
         throws QueryParserException, CumulusException {
      ResultSet result = new ResultSet();
      try {
         CombineMode combineMode = CombineMode.FIND_NEW;
         if (queryType != null) {
            switch (queryType) {
            case New:
               combineMode = CombineMode.FIND_NEW;
               break;
            case Narrow:
               combineMode = CombineMode.FIND_NARROW;
               break;
            case Broaden:
               combineMode = CombineMode.FIND_BROADEN;
               break;
            case Page:
               combineMode = null;
               break;
            default:
               break;
            }
         }

         if (combineMode != null) {
            collection.find(query, EnumSet.of(FindFlag.FIND_MISSING_FIELDS_REPLACE), combineMode, locale);
            try {
               if (sortRule != null && !"".equals(sortRule.getFieldGuid())) {
                  SortDirection sortDirection = SortDirection.DESCENDING;
                  if (sortRule.isAscending()) {
                     sortDirection = SortDirection.ASCENDING;
                  }
                  ArrayList<SortFieldDescriptor> sortDescriptors = new ArrayList<>();
                  SortFieldDescriptor sortDescriptor = new SortFieldDescriptor(new GUID(sortRule.getFieldGuid()), sortDirection);
                  sortDescriptors.add(sortDescriptor);
                  collection.setMultiSorting(sortDescriptors);
               }
            } catch (Exception se) {
            }
         }
         int collectionOffset = (offset <= 0) ? 0 : offset;
         int collectionCount = (count <= 0) ? collection.getItemCount() : count;
         if (collectionOffset < collection.getItemCount()) {
            if (collectionOffset + collectionCount > collection.getItemCount()) {
               collectionCount = collection.getItemCount() - collectionOffset;
            }
            result.setOffset(collectionOffset);
            result.setTotalCount(collection.getItemCount());
            Integer[] ids = collection.getItemIDs(collectionOffset, collectionCount).toArray(new Integer[0]);
            result.setCount(ids.length);
            result.setRecords(new RecordItem[result.getCount()]);
            for (int i = 0; i < result.getCount(); i++) {
               result.setRecord(i, (RecordItem) collection.getItemByID(ids[i]));
            }
            result.setCount(result.getRecords().length);
         }
      } catch (QueryParserException | CumulusException | ItemNotFoundException e) {
         e.printStackTrace();
      }
      return result;
   }

   /**
    * 
    * @param recordItem
    */
   public void releaseReadRecordItem(RecordItem recordItem) {
      if (recordItem != null) {
         returnReadObject(recordItem.getItemCollection());
      }
   }

   /**
    * 
    * @param recordItem
    */
   public void releaseWriteRecordItem(RecordItem recordItem) {
      if (recordItem != null) {
         returnWriteObject(recordItem.getItemCollection());
      }
   }

   public void releaseCategoryItem(CategoryItem categoryItem, boolean toWrite) {
      if (categoryItem != null) {
         returnObject(categoryItem.getItemCollection(), toWrite);
      }
   }

   public void releaseReadCategoryItem(CategoryItem categoryItem) {
      if (categoryItem != null) {
         returnReadObject(categoryItem.getItemCollection());
      }
   }

   public void releaseWriteCategoryItem(CategoryItem categoryItem) {
      if (categoryItem != null) {
         returnWriteObject(categoryItem.getItemCollection());
      }
   }

   public void releaseRecordItem(RecordItem recordItem, boolean toWrite) {
      if (recordItem != null) {
         returnObject(recordItem.getItemCollection(), toWrite);
      }
   }

   public CategoryItem getCategoryItemById(int id, boolean toWrite) {
      CategoryItem result = null;
      CategoryItemCollection collection = null;
      synchronized (collection) {
         try {
            if (!toWrite) {
               collection = (CategoryItemCollection) borrowObjectToRead(CategoryItemCollection.class);
            } else {
               collection = (CategoryItemCollection) borrowObjectToWrite(CategoryItemCollection.class);
            }
            result = collection.getCategoryItemByID(id);
            if (result == null) {
               // masterCategoryCollection.addItemByID(id);
               collection.addItemByID(id);
               result = collection.getCategoryItemByID(id);
               // result = masterCategoryCollection.getCategoryItemByID(id);
            }
         } catch (ItemNotFoundException | CumulusException e) {
            // do find all and try again
            collection.findAll();
            if (collection.hasItem(id)) {
               result = collection.getCategoryItemByID(id);
            } else {
               collection.addItemByID(id);
               result = collection.getCategoryItemByID(id);
            }
         } finally {
            if (collection != null) {
               if (!toWrite) {
                  returnReadObject(collection);
               } else {
                  returnWriteObject(collection);
               }
            }
         }
      }
      return result;
   }

   public void deleteCategoryItems(Integer[] ids) {
      CategoryItemCollection collection = null;
      try {
         collection = (CategoryItemCollection) borrowObjectToWrite(CategoryItemCollection.class);
         synchronized (collection) {
            for (Integer id : ids) {
               CategoryItem item = null;
               try {
                  collection.getCategoryItemByID(id);
               } catch (ItemNotFoundException | CumulusException e) {
               }
               if (item == null) {
                  collection.addItemByID(id);
                  item = collection.getCategoryItemByID(id);
               }
               if (item != null) {
                  item.deleteItem();
               }
            }
         }
      } catch (ItemNotFoundException | CumulusException | PermissionDeniedException e) {
         e.printStackTrace();
      } finally {
         if (collection != null) {
            returnWriteObject(collection);
         }
      }
   }

   public void deleteRecordItems(Integer[] ids, boolean deleteAssets) {
      RecordItemCollection collection = null;
      try {
         collection = (RecordItemCollection) borrowObjectToWrite(RecordItemCollection.class);
         synchronized (collection) {
            for (Integer id : ids) {
               logger.debug("attempt to delete record with id: " + id + " from catalog: '" + catalog.getName() + "'");
               RecordItem item = null;
               try {
                  collection.getRecordItemByID(id);
               } catch (ItemNotFoundException | CumulusException e) {
                  // probably needs to be added to collection, so just ignore
               }
               if (item == null) {
                  collection.addItemByID(id);
                  item = collection.getRecordItemByID(id);
               }
               if (item != null) {
                  item.deleteItem(deleteAssets);
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (collection != null) {
            returnWriteObject(collection);
         }
      }
   }

   @SuppressWarnings("rawtypes")
   public ItemCollection borrowObjectToRead(Class collectionClass) {
      return borrowObjectToRead(collectionClass, false);
   }

   @SuppressWarnings("rawtypes")
   public ItemCollection cloneObjectToRead(Class collectionClass) {
      ItemCollection result = null;
      if (collectionClass == RecordItemCollection.class) {
         result = recordCollection.clone();
      } else if (collectionClass == CategoryItemCollection.class) {
         result = categoryCollection.clone();
      }
      return result;
   }

   @SuppressWarnings("rawtypes")
   public ItemCollection borrowObjectToRead(Class collectionClass, boolean refetch) {
      ItemCollection result = null;
      // try the read queue first, and if none, then try the write queue
      if (onDemand) {
         // we need to try to grab a license
         result = borrowOnDemand(collectionClass);
      } else {
         if (collectionClass == RecordItemCollection.class) {
            result = recordPool.remove();
         } else if (collectionClass == CategoryItemCollection.class) {
            result = categoryPool.remove();
         }
      }
      if (result != null && refetch) {
         result.findAll();
      }
      return result;
   }

   public ItemCollection borrowOnDemand(Class collectionClass) {
      ItemCollection result = null;
      try {
         try {
            if (connection.isReadOnly()) {
               server = Server.openConnection(false, connection.getServer(), connection.getUsername(), connection.getPassword());
            } else {
               server = Server.openConnection(true, connection.getServer(), connection.getUsername(), connection.getPassword());
            }
         } catch (LoginFailedException | PasswordExpiredException | ServerNotFoundException | CumulusException e) {
            e.printStackTrace();
         }
         this.connection.setId(server.findCatalogID(connection.getDatabase()));
         catalog = server.openCatalog(this.connection.getId());
         if (collectionClass == RecordItemCollection.class) {
            result = recordCollection = catalog.newRecordItemCollection(true);
            recordLayout = recordCollection.getLayout();
         } else if (collectionClass == CategoryItemCollection.class) {
            result = categoryCollection = catalog.newCategoryItemCollection();
            categoryLayout = categoryCollection.getLayout();
         }

      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   /**
    * Sometime on demand needs to be setup to get layouts etc, so do that here
    */
   public ItemCollection borrowOnDemand() {
      ItemCollection result = null;
      try {
         try {
            if (connection.isReadOnly()) {
               server = Server.openConnection(false, connection.getServer(), connection.getUsername(), connection.getPassword());
            } else {
               server = Server.openConnection(true, connection.getServer(), connection.getUsername(), connection.getPassword());
            }
         } catch (LoginFailedException | PasswordExpiredException | ServerNotFoundException | CumulusException e) {
            e.printStackTrace();
         }
         this.connection.setId(server.findCatalogID(connection.getDatabase()));
         catalog = server.openCatalog(this.connection.getId());
         result = recordCollection = catalog.newRecordItemCollection(true);
         recordLayout = recordCollection.getLayout();
         categoryCollection = catalog.newCategoryItemCollection();
         categoryLayout = categoryCollection.getLayout();

      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;

   }

   @SuppressWarnings("rawtypes")
   public ItemCollection borrowObject(Class collectionClass, boolean toWrite) {
      if (!toWrite) {
         return borrowObjectToRead(collectionClass);
      }
      return borrowObjectToWrite(collectionClass);
   }

   @SuppressWarnings("rawtypes")
   public ItemCollection borrowObjectToWrite(Class collectionClass) {
      ItemCollection result = null;
      if (connection.isReadOnly()) {
         logger.info("attempt to get writable object from read only catalog definition");
         return result;
      }
      if (onDemand) {
         // we need to try to grab a license
         result = borrowOnDemand(collectionClass);
      } else {
         try {
            if (collectionClass == RecordItemCollection.class) {
               result = recordPool.remove();
            } else if (collectionClass == CategoryItemCollection.class) {
               result = categoryPool.remove();
            }
         } catch (Exception e) {
            // this is bad, we need to tidy up, maybe best to clse down and re
            // initialise as we are leaking
            // or check for pool isEmpty() above and deal with it
         }
      }
      return result;
   }

   public void returnReadObject(ItemCollection collection) {
      if (collection == null) {
         return;
      }
      try {
         if (onDemand) {
            terminate();
         } else {
            if (collection instanceof RecordItemCollection) {
               recordPool.add(collection);
            } else if (collection instanceof CategoryItemCollection) {
               categoryPool.add(collection);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public RecordItem getRecordItemByID(RecordItemCollection collection, int id) {
      RecordItem result = null;
      try {
         result = collection.getRecordItemByID(id);
      } catch (ItemNotFoundException e) {
         collection.findAll();
         result = collection.getRecordItemByID(id);
      }
      return result;
   }

   public void returnObject(ItemCollection collection, boolean toWrite) {
      if (onDemand) {
         terminate();
      } else {
         if (!toWrite) {
            returnReadObject(collection);
         } else {
            returnWriteObject(collection);
         }
      }
   }

   public void returnWriteObject(ItemCollection collection) {
      if (collection == null) {
         return;
      }
      if (onDemand) {
         terminate();
      } else {
         try {
            if (collection instanceof RecordItemCollection) {
               recordPool.add(collection);
            } else if (collection instanceof CategoryItemCollection) {
               categoryPool.add(collection);
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   private void serverReconnect() throws Exception {
      synchronized (this) {
         logger.info("Server trying to reconnect");
         try {
            recordCollection.close();
            categoryCollection.close();
         } catch (Exception e) {
         }

         server = null;
         catalog = null;
         server = Server.openConnection(!connection.isReadOnly(), connection.getServer(), connection.getUsername(), connection.getPassword());
         catalog = server.openCatalog(connection.getId());
         recordCollection = catalog.newRecordItemCollection(false);
         categoryCollection = catalog.newCategoryItemCollection();
         recordCollection.findAll();
         categoryCollection.findAll();
         recordLayout = recordCollection.getLayout();
         categoryLayout = categoryCollection.getLayout();
      }
   }

   private void closeServerCollections(ConcurrentLinkedQueue<ItemCollection> queue, Server server) {
      try {
         synchronized (queue) {
            ItemCollection ic = queue.remove();
            while (ic != null) {
               if (ic.getCatalog().getServer().equals(server)) {
                  ic.close();
               }
               ic = queue.remove();
            }
         }
      } catch (Exception re) {
      }
   }

   private void reconnectServer(int number) throws Exception {
      synchronized (this) {
         logger.info("Reconnecting server: " + number);
         try {
            Server server = servers[number];
            closeServerCollections(recordPool, server);
            closeServerCollections(categoryPool, server);
         } catch (Exception re) {
         }
         catalogs[number] = null;
         servers[number] = null;
         servers[number] = Server.openConnection(true, connection.getServer(), connection.getUsername(), connection.getPassword());
         catalogs[number] = servers[number].openCatalog(connection.getId());
         setupServerCollections(catalogs[number]);
      }
   }

   private class ServerMonitor extends Thread {

      public final static int LOG_CYCLE = 10;
      public final static int DEFAULT_FREQUENCY = 30000;
      private int frequency = DEFAULT_FREQUENCY;

      public ServerMonitor() {
      }

      @Override
      public void run() {
         int count = 0;
         while (true) {
            try {
               if (server == null || !server.isAlive()) {
                  serverReconnect();
               }
               if (!connection.isReadOnly() && servers != null) {
                  for (int i = 0; i < servers.length; i++) {
                     if (!servers[i].isAlive()) {
                        logger.info("trying to reconnect server");
                        reconnectServer(i);
                     }
                  }
               }
               if (server != null && server.isAlive()) {
                  if (recordCollection != null) {
                     recordCollection.findAll();
                  }
                  if (categoryCollection != null) {
                     categoryCollection.findAll();
                  }
               }
               count = (count >= LOG_CYCLE) ? 0 : count++;
               frequency = DEFAULT_FREQUENCY;
            } catch (Exception e) {
               logger.info("Connection pool error - database '" + connection.getDatabase() + "' - look in the log files.");
               e.printStackTrace();
               frequency += 100;
            } finally {
               try {
                  sleep(frequency);
               } catch (InterruptedException e1) {
                  // e1.printStackTrace();
                  // logger.info("ServerMonitor ending.");
                  break;
               }
            }
         }
      }
   }

   public Boolean isOnDemand() {
      return getOnDemand();
   }

   public Boolean getOnDemand() {
      return onDemand;
   }

   public void setOnDemand(Boolean onDemand) {
      this.onDemand = onDemand;
   }
}
