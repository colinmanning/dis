package com.setantamedia.fulcrum.db;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.setantamedia.fulcrum.common.Connection;
import com.setantamedia.fulcrum.common.DatabaseField;
import com.setantamedia.fulcrum.common.Query;
import com.setantamedia.fulcrum.common.SearchDescriptor;
import com.setantamedia.fulcrum.config.FulcrumConfig;
import com.setantamedia.fulcrum.config.View;
import com.setantamedia.fulcrum.models.core.Person;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;

public interface DbManager {

    public final static String ALL_FIELDS = "all";
    public final static String DB_SESSION_DATA = "DBMANAGER_SESSION_DATA";

    public void init();

    public void stop();

    public void setConfigViews(List<View> configViews);

    public HashMap<String, DatabaseField[]> getConnectionTableFields(Connection connection) throws SQLException;

    public DatabaseField[] getTableFields(Connection connection, String tableName);

    public DatabaseField[] getViewFields(Connection connection, String viewName, Boolean withId);
    
    public DatabaseField[] getViewFields(Connection connection, String viewName);

    public DbSessionData login(String database, String connectionName, String username, String password) throws SQLException;

    /**
     * Return a view object
     *
     * @param connectionName The name of the relevant connection
     * @param viewName the name of the view defined in the configuration file
     * @return the view object or null if the view cannot be found
     */
    public View getView(String connectionName, String viewName);

    /**
     * Return the sql to return the view fields - usually prepended to a filter
     * clause by a calling method
     *
     * @param connectionName The name of the relevant connection
     * @param viewName the name of the view defined in the configuration file
     * @param withId if true then assume there is an id field
     * @return the sql to return the fiew fields
     */
    public String getViewSql(String connectionName, String viewName, Boolean withId);

    /**
     * Return the sql to return the view fields - usually prepended to a filter
     * clause by a calling method
     *
     * @param connectionName The name of the relevant connection
     * @param viewName the name of the view defined in the configuration file
      * @return the sql to return the fiew fields
     */
    public String getViewSql(String connectionName, String viewName);

    /**
     * Change the database password for a specified user
     *
     * @param database
     * @param connectionName
     * @param username
     * @param oldPassword
     * @param newPassword
     * @return
     * @throws SQLException
     */
    public Boolean changeUserPassword(String connectionName, String username, String oldPassword, String newPassword) throws SQLException;

    public Boolean changeUserPasswordAdmin(String connectionName, String username, String password, String adminUsername, String adminPassword) throws SQLException;
    
    public Boolean createUserAdmin(String connectionName, String username, String password, String adminUsername, String adminPassword) throws SQLException;

    public void logout(DbSessionData sessionData);

    public QueryResult fetch(String connectionName, String query, SearchDescriptor searchDescriptor, String findMany) throws SQLException;

    public QueryResult fetch(DbSessionData sessionData, String connectionName, String query, SearchDescriptor searchDescriptor, String findMany) throws SQLException;

    public Record create(String connectionName, String table, HashMap<String, String> columns) throws SQLException;

    public Boolean update(String connectionName, String table, Integer id, HashMap<String, String> columns) throws SQLException;
    
    public Boolean update(String connectionName, String table, HashMap<String, String> primaryKey, HashMap<String, String> columns) throws SQLException;
    
    public Boolean createOrUpdate(String connectionName, String table, HashMap<String, String> primaryKey, HashMap<String, String> columns) throws SQLException;

    /**
     * Execute a statement in the database -
     *
     * @param connectionName
     * @param statement
     * @return
     * @throws SQLException
     */
    public Boolean execute(String connectionName, String statement) throws SQLException;

    /**
     *
     * Delete a single record - note this may fail if there are relevant
     * integrity constraints in place You may need to implement the delete
     * cascading logic, using the condition clause
     *
     * @param database
     * @param connectionName
     * @param table
     * @param id an optional id - any condition clause will be ignored
     * @param condition an optional "where" clause
     * @return
     * @throws SQLException
     */
    public Boolean delete(String connectionName, String table, Integer id, String condition) throws SQLException;

    public Person checkUser(String connectionName, String username, String password) throws SQLException;

    public Person getPerson(java.sql.Connection sqlConnection, String username) throws SQLException;

    public Person getPerson(String connectionName, String username) throws SQLException;

    public DatabaseField[] getFields(Connection connection, String viewName);

    public void setConnections(HashMap<String, Connection> connections);

    public FulcrumConfig getConfig();

    public void setConfig(FulcrumConfig config);

    public String getBaseUrl();

    public void setBaseUrl(String baseUrl);

    public String getSchemaFile();

    public void setSchemaFile(String schemaFile);

    public String getCustomSchemaFile();

    public void setCustomSchemaFile(String customSchemaFile);

    public void setDatabaseName(String databaseName);

    public void setQueries(HashMap<String, Query> queries);

    public HashMap<String, Query> getQueries();

    public Query getQuery(String name);
}
