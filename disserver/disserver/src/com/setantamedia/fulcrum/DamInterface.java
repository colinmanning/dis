package com.setantamedia.fulcrum;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.setantamedia.fulcrum.common.Connection;
import com.setantamedia.fulcrum.common.DatabaseField;
import com.setantamedia.fulcrum.common.DatabaseQuery;
import com.setantamedia.fulcrum.common.FieldValue;
import com.setantamedia.fulcrum.common.FileStreamer;
import com.setantamedia.fulcrum.common.SearchDescriptor;
import com.setantamedia.fulcrum.config.FulcrumConfig;
import com.setantamedia.fulcrum.config.Preview;
import com.setantamedia.fulcrum.config.View;
import com.setantamedia.fulcrum.models.core.Person;
import com.setantamedia.fulcrum.ws.types.Category;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;

public interface DamInterface {
	
	   public abstract boolean registerConnection(String name, Connection connection);
	   
	   public abstract boolean deregisterConnection(String name, Connection connection);
	   
	   public abstract void init();

	   public abstract void terminate();

	   public abstract void setConfig(FulcrumConfig config);
	   
	   public abstract void setRoot(String root);
	   
	   public abstract String setRoot();
	   
	   public abstract void setDataFolder(String dataFolder);
	   
	   public abstract void setSchemaFile(String schemaFile);
	   
	   public abstract void setCustomSchemaFile(String customSchemaFile);
	   
	   public abstract QueryResult querySearch(Connection connection, String query, SearchDescriptor searchDescriptor, Locale locale);
	   
	   public abstract QueryResult textSearch(Connection connection, String text, SearchDescriptor searchDescriptor, Locale locale);

	   public abstract QueryResult categorySearch(Connection connection, String categoryId, SearchDescriptor searchDescriptor, Boolean recursive, Locale locale);

	   public abstract byte[] getThumbnail(Connection connection, String id, Integer maxSize, SearchDescriptor searchDescriptor);
	   
	   public abstract byte[] previewFile(Connection connection, String id, File cacheFile);

	   public abstract byte[] previewFile(Connection connection, String id, Preview previewData, File file);
	   
	   public abstract byte[] previewFile(Connection connection, String id,Integer top, Integer left, Integer height, Integer width, Integer maxSize, Integer size, Integer rotate, String format, Integer quality, File file);
	   
	   public abstract String uploadFile(Connection connection, InputStream inputStream, String fileName, String uploadProfile, HashMap<String, String> fields);

	   public abstract String uploadFile(Connection connection, File file, String fileName, String uploadProfile, HashMap<String, String> fields);

	   public abstract FileStreamer getFile(Connection connection, String id, Integer version, String actionName);

	   public abstract boolean updateRecord(Connection connection, DatabaseField rootFieldDef, String tablePath, String recordKey, HashMap<String, String> fieldValues);

	   public abstract boolean addCategoryFile(Connection connection, String categoryId, String recordId); 
	   
	   public abstract boolean removeRecordFromCategory(Connection connection, String categoryId, String recordId); 
	   
	   public abstract Record getFileMetadata(Connection connection, String id, String view, Locale locale);

	   public abstract boolean updateFileMetadata(Connection connection, String id, HashMap<String, String> fields);
	   
	   public abstract Category createCategory(Connection connection, String path);
	   
	   public abstract Category createSubCategory(Connection connection, Integer parentId, String path);
	      
	   public abstract Category findCategories(Connection connection, String path);
	   
	   public abstract DatabaseField[] getFields(Connection connection);
	   
	   public abstract DatabaseField[] getFields(Connection connection, String view);
	   
	   public abstract DatabaseQuery[] getQueries(Connection connection);
	   
	   public abstract Map<String, String> getPreviews(Connection connection, String view);
	   
	   public abstract Map<String, String> getLinks(Connection connection, String view);
	   
	   public abstract Map<String, String> getReferences(Connection connection, String view);
	   
	   public abstract void removeTemporaryFile(File file);
	   
	   public abstract Object getFieldValue(FieldValue v, boolean json);
	   
	   public abstract Person getUser(Connection connection, String username, String password);
	   
	   public abstract boolean activateUser(Connection connection, String username, boolean activate);
	   
	   public abstract void setConfigViews(List<View> configViews);
	   
	   public abstract View[] getConfigViews();
	   
		public abstract String getImageToolPath();
		
		public abstract void setImageToolPath(String imageToolPath);

		public abstract String getMetadataToolPath();

		public abstract void setMetadataToolPath(String metadataToolPath);

}
