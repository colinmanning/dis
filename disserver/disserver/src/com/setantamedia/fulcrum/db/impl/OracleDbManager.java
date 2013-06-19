package com.setantamedia.fulcrum.db.impl;

import com.setantamedia.fulcrum.common.Connection;
import com.setantamedia.fulcrum.common.DatabaseField;
import com.setantamedia.fulcrum.common.Query;
import com.setantamedia.fulcrum.common.SearchDescriptor;
import com.setantamedia.fulcrum.config.FulcrumConfig;
import com.setantamedia.fulcrum.config.View;
import com.setantamedia.fulcrum.db.DbManager;
import com.setantamedia.fulcrum.db.DbSessionData;
import com.setantamedia.fulcrum.models.core.Person;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class OracleDbManager implements DbManager {
    
    private final static Logger logger = Logger.getLogger(OracleDbManager.class);
    private FulcrumConfig config = null;

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setConfigViews(List<View> configViews) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HashMap<String, DatabaseField[]> getConnectionTableFields(Connection connection) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DatabaseField[] getTableFields(Connection connection, String tableName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DatabaseField[] getViewFields(Connection connection, String viewName, Boolean withId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DatabaseField[] getViewFields(Connection connection, String viewName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DbSessionData login(String database, String connectionName, String username, String password) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public View getView(String connectionName, String viewName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getViewSql(String connectionName, String viewName, Boolean withId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getViewSql(String connectionName, String viewName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean changeUserPassword(String connectionName, String username, String oldPassword, String newPassword) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean changeUserPasswordAdmin(String connectionName, String username, String password, String adminUsername, String adminPassword) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean createUserAdmin(String connectionName, String username, String password, String adminUsername, String adminPassword) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void logout(DbSessionData sessionData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult fetch(String connectionName, String query, SearchDescriptor searchDescriptor, String findMany) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public QueryResult fetch(DbSessionData sessionData, String connectionName, String query, SearchDescriptor searchDescriptor, String findMany) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Record create(String connectionName, String table, HashMap<String, String> columns) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean update(String connectionName, String table, Integer id, HashMap<String, String> columns) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean update(String connectionName, String table, HashMap<String, String> primaryKey, HashMap<String, String> columns) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean createOrUpdate(String connectionName, String table, HashMap<String, String> primaryKey, HashMap<String, String> columns) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean execute(String connectionName, String statement) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean delete(String connectionName, String table, Integer id, String condition) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Person checkUser(String connectionName, String username, String password) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Person getPerson(java.sql.Connection sqlConnection, String username) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Person getPerson(String connectionName, String username) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DatabaseField[] getFields(Connection connection, String viewName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setConnections(HashMap<String, Connection> connections) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FulcrumConfig getConfig() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setConfig(FulcrumConfig config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getBaseUrl() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBaseUrl(String baseUrl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSchemaFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSchemaFile(String schemaFile) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCustomSchemaFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCustomSchemaFile(String customSchemaFile) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDatabaseName(String databaseName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setQueries(HashMap<String, Query> queries) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HashMap<String, Query> getQueries() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query getQuery(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
