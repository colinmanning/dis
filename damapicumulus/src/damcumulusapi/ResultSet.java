package damcumulusapi;

import com.canto.cumulus.ItemCollection;
import com.canto.cumulus.RecordItem;

/**
 * @author Colin Manning
 *
 */
public class ResultSet {

    private int offset = 0;
    private int count = 0;
    private int totalCount = 0;
    private RecordItem[] records = new RecordItem[0];
    private ItemCollection collection = null;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public RecordItem[] getRecords() {
        return records;
    }

    public void setRecords(RecordItem[] records) {
        this.records = records;
    }

    public void setRecord(int index, RecordItem record) {
        this.records[index] = record;
    }

    public RecordItem getRecord(int index) {
        return records[index];
    }

    public ItemCollection getCollection() {
        return collection;
    }

    public void setCollection(ItemCollection collection) {
        this.collection = collection;
    }
}
