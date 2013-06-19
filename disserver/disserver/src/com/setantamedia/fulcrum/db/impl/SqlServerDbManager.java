package com.setantamedia.fulcrum.db.impl;

import com.setantamedia.fulcrum.common.*;
import com.setantamedia.fulcrum.config.Field;
import com.setantamedia.fulcrum.config.FulcrumConfig;
import com.setantamedia.fulcrum.config.View;
import com.setantamedia.fulcrum.db.DbManager;
import static com.setantamedia.fulcrum.db.DbManager.ALL_FIELDS;
import com.setantamedia.fulcrum.db.DbSessionData;
import com.setantamedia.fulcrum.models.core.Person;
import com.setantamedia.fulcrum.ws.types.DbRecord;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class SqlServerDbManager implements DbManager {

    private final static Logger logger = Logger.getLogger(SqlServerDbManager.class);
    private FulcrumConfig config = null;
    private String baseUrl = null;
    private String schemaFile = null;
    private String customSchemaFile = null;
    private HashMap<String, com.setantamedia.fulcrum.common.Connection> connections = null;
    private String databaseName = null;
    private List<View> configViews = null;
    private HashMap<String, View> views = null;
    private HashMap<String, String> viewsSql = null;
    private HashMap<String, DatabaseField[]> viewFields = new HashMap<>();
    private HashMap<com.setantamedia.fulcrum.common.Connection, HashMap<String, DatabaseField[]>> connectionTableFields = new HashMap<>();
    protected HashMap<String, Query> queries = new HashMap<>();
    private final static String SQLS_TYPE_INTEGER = "int";
    private final static String SQLS_TYPE_LONG = "bigint";
    private final static String SQLS_TYPE_STRING = "varchar";
    private final static String SQLS_TYPE_DATE = "date";
    private final static String SQLS_TYPE_BOOLEAN = "bit";
    private final static String SQLS_TYPE_TIMESTAMP = "timestamp";
    private final static String SQLS_TYPE_CLOB = "varbinary";
    private final static String SQLS_TYPE_UNIQUEID = "uniqueidentifier";

    @Override
    public void init() {
        try {
            this.views = new HashMap<>();
            this.viewsSql = new HashMap<>();
            if (configViews != null) {
                for (View vl : configViews) {
                    views.put(vl.getName(), vl);
                }
            }
            for (Connection connection : connections.values()) {
                connectionTableFields.put(connection, getConnectionTableFields(connection));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void setConfigViews(List<View> configViews) {
        this.configViews = configViews;
    }

    @Override
    public HashMap<String, DatabaseField[]> getConnectionTableFields(com.setantamedia.fulcrum.common.Connection connection) throws SQLException {
        // first time, get them all
        HashMap<String, DatabaseField[]> result = new HashMap<>();
        java.sql.Connection sqlConnection = null;
        Statement statement = null;
        try {
            sqlConnection = getJdbcConnection(connection.getName());
            statement = sqlConnection.createStatement();
            String query = "select table_name,column_name,data_type from information_schema.columns where table_schema='dbo' order by table_name";
            statement.execute(query);
            ResultSet results = statement.getResultSet();
            ArrayList<DatabaseField> fields = new ArrayList<>();
            String tableName = "";
            while (results.next()) {
                if (!tableName.equals(results.getString("table_name"))) {
                    if (!"".equals(tableName)) {
                        result.put(tableName, fields.toArray(new DatabaseField[0]));
                    }
                    fields = new ArrayList<>();
                    tableName = results.getString("table_name");
                }
                DatabaseField field = new DatabaseField();
                DatabaseTable dbTable = new DatabaseTable();
                dbTable.setName(tableName);
                field.setTableDefinition(dbTable);
                field.setName(results.getString("column_name"));
                field.setDataType(convertFieldType(results.getString("data_type")));
                field.setValueInterpretation(FieldTypeConstants.VALUE_DEFAULT);
                fields.add(field);
            }
            if (!"".equals(tableName)) {
                result.put(tableName, fields.toArray(new DatabaseField[0]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            statement.close();
            sqlConnection.close();
        }
        return result;
    }

    private Integer convertFieldType(String typeName) {
        Integer result = FieldTypeConstants.TypeString;
        switch (typeName) {
            case SQLS_TYPE_STRING:
                result = FieldTypeConstants.TypeString;
                break;
            case SQLS_TYPE_INTEGER:
                result = FieldTypeConstants.TypeInteger;
                break;
            case SQLS_TYPE_LONG:
                result = FieldTypeConstants.TypeLong;
                break;
            case SQLS_TYPE_DATE:
                result = FieldTypeConstants.TypeDate;
                break;
            case SQLS_TYPE_TIMESTAMP:
                result = FieldTypeConstants.TypeDate;
                break;
            case SQLS_TYPE_BOOLEAN:
                result = FieldTypeConstants.TypeBool;
                break;
            case SQLS_TYPE_CLOB:
                result = FieldTypeConstants.TypeClob;
                break;
        }

        return result;
    }

    @Override
    public DatabaseField[] getTableFields(com.setantamedia.fulcrum.common.Connection connection, String tableName) {
        // should be cached already
        return connectionTableFields.get(connection).get(tableName);
    }

    @Override
    public DatabaseField[] getViewFields(Connection connection, String viewName) {
        return getViewFields(connection, viewName, true);
    }

    @Override
    public DatabaseField[] getViewFields(com.setantamedia.fulcrum.common.Connection connection, String viewName, Boolean withId) {
        DatabaseField[] result = null;
        try {
            View view = views.get(viewName);
            if (view != null) {
                String tableName = view.getTable();
                String[] tableNames = tableName.split(",");
                DatabaseField[] vf = viewFields.get(viewName);
                if (vf != null) {
                    result = vf;
                } else {
                    List<Field> fields = view.getField();
                    ArrayList<DatabaseField> viewDbFields = new ArrayList<>();
                    HashMap<String, DatabaseField> dbFields = new HashMap<>();
                    if (tableNames.length == 1) {
                        DatabaseField[] dbFieldArray = connectionTableFields.get(connection).get(tableName);
                        for (DatabaseField dbField : dbFieldArray) {
                            dbFields.put(dbField.getQualifiedName(), dbField);
                        }
                        for (Field field : fields) {
                            DatabaseField dbField = dbFields.get(tableName + "." + field.getName());
                            if (dbField == null) {
                                logger.error("Could not get database field for : " + tableName + "." + field.getName());
                                continue;
                            }
                            String simpleName = field.getSimpleName();
                            if (simpleName != null && !"".equals(simpleName)) {
                                dbField.setSimpleName(simpleName);
                                dbFields.put(field.getName(), dbField);
                            } else {
                                dbField.setSimpleName(field.getName());
                            }
                            viewDbFields.add(dbField);
                        }
                    } else {
                        for (String tname : tableNames) {
                            DatabaseField[] dbFieldArray = connectionTableFields.get(connection).get(tname);
                            for (DatabaseField dbField : dbFieldArray) {
                                dbFields.put(dbField.getQualifiedName(), dbField);
                            }
                            for (Field field : fields) {
                                DatabaseField dbField = dbFields.get(field.getName());
                                if (dbField == null) {
                                    // field from another table in the join or error
                                    continue;
                                }
                                String simpleName = field.getSimpleName();
                                if (simpleName != null && !"".equals(simpleName)) {
                                    dbField.setSimpleName(simpleName);
                                    dbFields.put(field.getName().toUpperCase(), dbField);
                                } else {
                                    dbField.setSimpleName(field.getName());
                                }
                                viewDbFields.add(dbField);
                            }
                        }
                    }

                    result = viewDbFields.toArray(new DatabaseField[0]);
                    finaliseView(viewName, result, tableNames[0], withId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void finaliseView(String name, DatabaseField[] fields, String mainTable, Boolean withId) {
        // cache fields, and build sql for query
        viewFields.put(name, fields);
        String sql ="";
        if (withId) {
            sql = mainTable + ".id";
        }
        for (DatabaseField field : fields) {
            sql += "," + field.getTableDefinition().getName() + "." + field.getName();
            if (field.getSimpleName() != null) {
                sql += " as " + field.getSimpleName();
            }
        }
        viewsSql.put(name, sql);
    }

    @Override
    public DbSessionData login(String database, String connectionName, String username, String password) throws SQLException {
        DbSessionData result = null;
        java.sql.Connection sqlConnection = null;
        try {
            com.setantamedia.fulcrum.common.Connection connection = connections.get(connectionName);
            sqlConnection = getJdbcConnection(connectionName, username, password);
            if (sqlConnection != null) {
                result = new DbSessionData();
                result.setJdbcConnection(sqlConnection);
                Person person = getPerson(sqlConnection, username);
                person.setPassword(password);
                result.setPerson(person);
            }
        } catch (SQLException e) {
            // nothing to do, as login not authenticated, and can return null
        } catch (Exception e) {
            if (sqlConnection != null && !sqlConnection.isClosed()) {
                sqlConnection.close();
            }
            result = null;
            e.printStackTrace();
        } finally {
            if (sqlConnection != null) {
                sqlConnection.close();
            }
        }
        return result;
    }

    public java.sql.Connection getJdbcConnection(String connectionName, String username, String password) throws SQLException {
        java.sql.Connection result = null;
        try {
            com.setantamedia.fulcrum.common.Connection connection = connections.get(connectionName);
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String connectionUrl = connection.getDatabase() + ";username=" + username + ";password=" + password;
            result = DriverManager.getConnection(connectionUrl);
        } catch (SQLException e) {
            // nothing to do, as login not authenticated, and can return null
        } catch (Exception e) {
            result = null;
            e.printStackTrace();
        }
        return result;
    }

    public java.sql.Connection getJdbcConnection(String connectionName) throws SQLException {
        com.setantamedia.fulcrum.common.Connection connection = connections.get(connectionName);
        return getJdbcConnection(connectionName, connection.getUsername(), connection.getPassword());
    }

    @Override
    public View getView(String connectionName, String viewName) {
        View result = views.get(viewName);
        if (result == null) {
            // view not yet set-up, do it now
            getViewFields(connections.get(connectionName), viewName);
            result = views.get(viewName);
        }
        return result;
    }

    @Override
    public String getViewSql(String connectionName, String viewName) {
        return getViewSql(connectionName, viewName, true);
    }
    @Override
    public String getViewSql(String connectionName, String viewName, Boolean withId) {
        String result = viewsSql.get(viewName);
        if (result == null) {
            // view not yet set-up, do it now
            getViewFields(connections.get(connectionName), viewName, withId);
            result = viewsSql.get(viewName);
        }
        return result;
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
    public void logout(DbSessionData sessionData) {
        try {
            if (sessionData.getJdbcConnection() != null && !sessionData.getJdbcConnection().isClosed()) {
                sessionData.getJdbcConnection().close();
            }
            sessionData.setPerson(null);
            sessionData.setH2ConnectionPool(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public QueryResult fetch(String connectionName, String query, SearchDescriptor searchDescriptor, String findMany) throws SQLException {
        QueryResult result = null;
        Statement statement = null;
        java.sql.Connection sqlConnection = null;
        try {
            Connection connection = connections.get(connectionName);
            sqlConnection = getJdbcConnection(connectionName);
            statement = sqlConnection.createStatement();
            statement.execute(query);
            ResultSet results = statement.getResultSet();
            result = new QueryResult();
            result.setRecords(buildRecords(connection, searchDescriptor.getViewName(), results, findMany));
            result.setTotal(result.getRecords().length);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            statement.close();
            sqlConnection.close();
        }
        return result;
    }

    @Override
    public QueryResult fetch(DbSessionData sessionData, String connectionName, String query, SearchDescriptor searchDescriptor, String findMany) throws SQLException {
        QueryResult result = null;
        Statement statement = null;
        java.sql.Connection sqlConnection = null;
        try {
            Connection connection = connections.get(connectionName);
            Person person = sessionData.getPerson();
            if (person != null) {
                String username = (!"".equals(person.getUsername())) ? person.getUsername() : connection.getUsername();
                String password = (!"".equals(person.getPassword())) ? person.getPassword() : connection.getPassword();
                sqlConnection = getJdbcConnection(connectionName, person.getUsername(), person.getPassword());
            } else {
                sqlConnection = getJdbcConnection(connectionName);
            }
            statement = sqlConnection.createStatement();
            statement.execute(query);
            ResultSet results = statement.getResultSet();
            result = new QueryResult();
            result.setRecords(buildRecords(connection, searchDescriptor.getViewName(), results, findMany));
            result.setTotal(result.getRecords().length);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            statement.close();
            sqlConnection.close();
        }
        return result;
    }

    private Record[] buildRecords(Connection connection, String viewName, ResultSet results, String findMany) throws Exception {
        Record[] result = null;
        DatabaseField[] vf = getViewFields(connection, viewName);
        ArrayList<DbRecord> records = new ArrayList<>();
        String connectionName = connection.getName();
        while (results.next()) {
            DbRecord record = new DbRecord();
            record.setConnection(connectionName);
            try {
                // What if a join and there ate 2 ID fields - will we get the forst or what ?
                record.setId(String.valueOf(results.getInt("id")));
            } catch (SQLException se) {
                // some tables may not have id field - e.g. many_to_many tables - just ignore
            }
            for (DatabaseField field : vf) {
                FieldValue fieldValue = new FieldValue();
                fieldValue.setDataType(field.getDataType());
                //String fieldName = field.getTableDefinition().getName() + "." + field.getName();
                record.setTable(field.getTableDefinition().getName());
                String fieldName = field.getName();
                fieldValue.setValueInterpretation(field.getValueInterpretation());
                switch (fieldValue.getDataType()) {
                    case FieldTypeConstants.TypeString:
                        fieldValue.setStringValue(results.getString(fieldName));
                        break;
                    case FieldTypeConstants.TypeInteger:
                        fieldValue.setIntegerValue(results.getInt(fieldName));
                        break;
                    case FieldTypeConstants.TypeLong:
                        fieldValue.setLongValue(results.getLong(fieldName));
                        break;
                    case FieldTypeConstants.TypeBool:
                        fieldValue.setBooleanValue(results.getBoolean(fieldName));
                        break;
                    case FieldTypeConstants.TypeDate:
                        DateTime dateTime = new DateTime();
                        java.sql.Timestamp ts = results.getTimestamp(fieldName);
                        if (ts != null) {
                            dateTime.setValue(ts.getTime());
                        }
                        fieldValue.setDateTimeValue(dateTime);
                        break;
                    case FieldTypeConstants.TypeClob:
                        break;
                    default:
                        break;
                }
                record.addField(field.getSimpleName(), fieldValue);
            }
            if (findMany != null) {
                String[] allbits = findMany.split(";");
                for (String fm : allbits) {
                    String[] bits = fm.split(":");
                    if (bits.length == 3) {
                        // tableName:foreignKeyField:view;tableName:foreignKeyField:view ...
                        getViewFields(connection, bits[2]); // make sure views are defined ok
                        String query = "select " + viewsSql.get(bits[2]) + " from " + bits[0] + " where " + bits[1] + " = " + record.getId();
                        SearchDescriptor searchDescriptor = new SearchDescriptor();
                        searchDescriptor.setViewName(bits[2]);
                        QueryResult kids = fetch(connection.getName(), query, searchDescriptor, null);
                        if (kids != null) {
                            FieldValue fieldValue = new FieldValue();
                            fieldValue.setDataType(FieldTypeConstants.TypeRecords);
                            fieldValue.setKidsValue(kids.getRecords());
                            record.addField(bits[0], fieldValue);
                        }
                    } else if (bits.length == 5) {
                        // find via "many to many" pivot table
                        // pivotTableName:keyIntoPivotTable;keyIntoTable:tableName;view;pivotTableName:keyIntoPivotTable ...
                        String pivotTableName = bits[0];
                        String pivotTableKey = bits[1];
                        String targetTableKey = bits[2];
                        String targetTableName = bits[3];
                        String targetViewName = bits[4];
                        getViewFields(connection, viewName); // make sure views are defined ok
                        String query = "select " + viewsSql.get(targetViewName) + " from " + targetTableName + " where id in ";
                        query += "(select " + targetTableKey + " from " + pivotTableName + " where " + pivotTableKey + "=" + record.getId() + ")";
                        //System.out.println("query: "+query);

                        SearchDescriptor searchDescriptor = new SearchDescriptor();
                        searchDescriptor.setViewName(targetViewName);
                        QueryResult kids = fetch(connection.getName(), query, searchDescriptor, null);
                        if (kids != null) {
                            FieldValue fieldValue = new FieldValue();
                            fieldValue.setDataType(FieldTypeConstants.TypeRecords);
                            fieldValue.setKidsValue(kids.getRecords());
                            record.addField(targetTableName, fieldValue);
                        }
                    }
                }
            }
            records.add(record);
        }
        result = records.toArray(new Record[0]);
        return result;
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
    public Boolean execute(String connectionName, String statement) throws SQLException {
        Boolean result = false;
        java.sql.Connection sqlConnection = null;
        Statement sqlStatement = null;
        try {
            sqlConnection = getJdbcConnection(connectionName);
            sqlStatement = sqlConnection.createStatement();
            sqlStatement.execute(statement);
            sqlStatement.close();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlStatement != null && !sqlStatement.isClosed()) {
                sqlStatement.close();
            }
            if (sqlConnection != null) {
                sqlConnection.close();
            }
        }
        return result;
    }

    @Override
    public Boolean delete(String connectionName, String table, Integer id, String condition) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Person checkUser(String connectionName, String username, String password) throws SQLException {
        Person result = null;
        java.sql.Connection sqlConnection = null;
        try {
            try {
                com.setantamedia.fulcrum.common.Connection connection = connections.get(connectionName);
                sqlConnection = getJdbcConnection(connectionName, username, password);
                // if no SQLException thrown, then username and password are good
				/*
                 * Parameter "table" will be the name of the table holding user information such as name, email etc.
                 * username is the mey into that table - it must be unique
                 */
                result = getPerson(connectionName, username);
            } catch (Exception e) {
                // do nothing
            } finally {
                if (sqlConnection != null) {
                    sqlConnection.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlConnection != null) {
                sqlConnection.close();
            }
        }
        return result;
    }

    @Override
    public Person getPerson(java.sql.Connection sqlConnection, String username) throws SQLException {
        Person result = new Person();
        result.setUsername(username);
        Statement statement = null;
        try {
            String query = "select * from person where username='" + username + "'";
            statement = sqlConnection.createStatement();
            statement.execute(query);
            ResultSet results = statement.getResultSet();
            boolean ok = results.next();
            if (ok) {
                result.setId(results.getInt("id"));
                result.setFirstName(results.getString("first_name"));
                result.setLastName(results.getString("last_name"));
                result.setEmail(results.getString("email"));
                result.setUsername(results.getString("username"));
                result.setSsoUsername(results.getString("ssousername"));
                result.setDamAccess(results.getString("damaccess"));
                result.setPrimaryRole(results.getString("primary_role"));
                result.setResetPassword(results.getBoolean("reset_password"));
            }

        } catch (Exception e) {
            // do nothing, probabl means DIS database has no person table, so no problem
            //e.printStackTrace();
        } finally {
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
            if (sqlConnection != null) {
                sqlConnection.close();
            }
        }
        return result;
    }

    @Override
    public Person getPerson(String connectionName, String username) throws SQLException {
        Person result = null;
        java.sql.Connection sqlConnection = null;
        try {
            com.setantamedia.fulcrum.common.Connection connection = connections.get(connectionName);
            sqlConnection = getJdbcConnection(connectionName, connection.getUsername(), connection.getPassword());
            result = getPerson(sqlConnection, username);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlConnection != null) {
                sqlConnection.close();
            }
        }
        return result;
    }

    @Override
    public DatabaseField[] getFields(com.setantamedia.fulcrum.common.Connection connection, String viewName) {
        DatabaseField[] result = null;
        // Elvis works with a single connection (for now)
        boolean getAllFields = ALL_FIELDS.equals(viewName);
        if (getAllFields) {
        } else {
            result = viewFields.get(viewName);
        }
        if (result != null) {
            return result;
        }
        ArrayList<DatabaseField> dbFields = new ArrayList<>();
        result = dbFields.toArray(new DatabaseField[0]);
        viewFields.put(viewName, result);
        return result;
    }

    @Override
    public void setConnections(HashMap<String, com.setantamedia.fulcrum.common.Connection> connections) {
        this.connections = connections;
    }

    @Override
    public FulcrumConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(FulcrumConfig config) {
        this.config = config;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String getSchemaFile() {
        return schemaFile;
    }

    @Override
    public void setSchemaFile(String schemaFile) {
        this.schemaFile = schemaFile;
    }

    @Override
    public String getCustomSchemaFile() {
        return customSchemaFile;
    }

    @Override
    public void setCustomSchemaFile(String customSchemaFile) {
        this.customSchemaFile = customSchemaFile;
    }

    @Override
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public void setQueries(HashMap<String, Query> queries) {
        this.queries = queries;
    }

    @Override
    public HashMap<String, Query> getQueries() {
        return queries;
    }

    @Override
    public Query getQuery(String name) {
        return queries.get(name);
    }

    @Override
    public Boolean createUserAdmin(String connectionName, String username, String password, String adminUsername, String adminPassword) throws SQLException {
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

}
