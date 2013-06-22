package damcumulusapi;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.canto.cumulus.ItemCollection;
import com.canto.cumulus.events.CatalogingEventObject;
import com.canto.cumulus.events.CatalogingListener;

public class CumulusImporter implements CatalogingListener<ItemCollection> {

   private static Logger logger = Logger.getLogger(CumulusImporter.class);

   public final static int NULL_ID = -1;
   private int itemId = NULL_ID;
   private HashSet<Integer> itemIds = new HashSet<Integer>();
   private boolean multiAsset = false;

   public CumulusImporter() {
   }

   public void catalogingStarted(CatalogingEventObject<ItemCollection> event) {
      itemId = NULL_ID;
      itemIds = new HashSet<Integer>();
      multiAsset = false;
   }

   public void catalogingFinished(CatalogingEventObject<ItemCollection> event) {
   }

   public void countingAssets(CatalogingEventObject<ItemCollection> event) {
   }

   public void assetAdded(CatalogingEventObject<ItemCollection> event) {
      // logger.debug(" --- added asset");
      if (itemId == NULL_ID) {
         itemId = event.getItemID();
         itemIds.add(itemId);
      } else {
         itemIds.add(event.getItemID());
      }
   }

   public void assetUpdated(CatalogingEventObject<ItemCollection> event) {
	      logger.error(" --- asset updated");
  }

   public void assetIgnored(CatalogingEventObject<ItemCollection> event) {
	      logger.error(" --- asset ignored");
  }

   public void assetFailed(CatalogingEventObject<ItemCollection> event) {
      logger.error(" --- asset failed with error: " + event.getErrorMessage());
   }

   public int getItemId() {
      return itemId;
   }

   public void setItemId(int itemId) {
      this.itemId = itemId;
   }

   public HashSet<Integer> getItemIds() {
      return itemIds;
   }

   public boolean isMultiAsset() {
      return multiAsset;
   }
}
