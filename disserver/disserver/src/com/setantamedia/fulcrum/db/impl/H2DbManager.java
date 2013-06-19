package com.setantamedia.fulcrum.db.impl;

import com.setantamedia.fulcrum.common.*;
import com.setantamedia.fulcrum.config.Field;
import com.setantamedia.fulcrum.config.FulcrumConfig;
import com.setantamedia.fulcrum.config.View;
import com.setantamedia.fulcrum.db.DbManager;
import com.setantamedia.fulcrum.db.DbSessionData;
import com.setantamedia.fulcrum.models.core.Person;
import com.setantamedia.fulcrum.ws.types.DbRecord;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;
import java.io.BufferedReader;
import java.io.Reader;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.h2.jdbc.JdbcSQLException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

public class H2DbManager implements DbManager {

    private final static Logger logger = Logger.getLogger(H2DbManager.class);
    public final static int MAX_POOL_CONNECTIONS = 100;
    // key is database:connection
    private HashMap<String, JdbcConnectionPool> connectionPools = new HashMap<>();
    private FulcrumConfig config = null;
    private String baseUrl = null;
    private String schemaFile = null;
    private String customSchemaFile = null;
    private HashMap<String, Connection> connections = null;
    private Server server = null;
    private String databaseName = null;
    private List<View> configViews = null;
    private HashMap<String, View> views = null;
    private HashMap<String, String> viewsSql = null;
    private HashMap<String, DatabaseField[]> viewFields = new HashMap<>();
    private HashMap<Connection, HashMap<String, DatabaseField[]>> connectionTableFields = new HashMap<>();
    protected HashMap<String, Query> queries = new HashMap<>();
    private final static String H2_TYPE_INTEGER = "INTEGER";
    private final static String H2_TYPE_LONG = "BIGINT";
    private final static String H2_TYPE_STRING = "STRING";
    private final static String H2_TYPE_DATE = "DATE";
    private final static String H2_TYPE_BOOLEAN = "BOOLEAN";
    private final static String H2_TYPE_TIMESTAMP = "TIMESTAMP";
    private final static String H2_TYPE_CLOB = "CLOB";

    public H2DbManager() {
    }

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
            server = Server.createTcpServer(new String[]{"-tcpAllowOthers"});
            try {
                if (!server.isRunning(false)) {
                    server.start();
                }
            } catch (SQLException se) {
                // may already be running, just log
                logger.info("Attempt to start database server - probably already running");
            }
            for (Connection connection : connections.values()) {
                JdbcConnectionPool pool = JdbcConnectionPool.create(connection.getDatabase(), connection.getUsername(), connection.getPassword());
                pool.setMaxConnections(MAX_POOL_CONNECTIONS);
                connectionPools.put(connection.getName(), pool);
                connectionTableFields.put(connection, getConnectionTableFields(connection));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setConfigViews(List<View> configViews) {
        this.configViews = configViews;
    }

    @Override
    public HashMap<String, DatabaseField[]> getConnectionTableFields(Connection connection) throws SQLException {
        // first time, get them all
        HashMap<String, DatabaseField[]> result = new HashMap<>();
        java.sql.Connection sqlConnection = connectionPools.get(connection.getName()).getConnection();
        Statement statement = sqlConnection.createStatement();
        try {
            String query = "select table_name,column_name,type_name from information_schema.columns where table_catalog='" + databaseName.toUpperCase() + "' and table_schema='PUBLIC' order by table_name";
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
                field.setDataType(convertFieldType(results.getString("type_name")));
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
            case H2_TYPE_STRING:
                result = FieldTypeConstants.TypeString;
                break;
            case H2_TYPE_INTEGER:
                result = FieldTypeConstants.TypeInteger;
                break;
            case H2_TYPE_LONG:
                result = FieldTypeConstants.TypeLong;
                break;
            case H2_TYPE_DATE:
                result = FieldTypeConstants.TypeDate;
                break;
            case H2_TYPE_TIMESTAMP:
                result = FieldTypeConstants.TypeDate;
                break;
            case H2_TYPE_BOOLEAN:
                result = FieldTypeConstants.TypeBool;
                break;
            case H2_TYPE_CLOB:
                result = FieldTypeConstants.TypeClob;
                break;
        }

        return result;
    }

    @Override
    public DatabaseField[] getTableFields(Connection connection, String tableName) {
        // should be cached already
        return connectionTableFields.get(connection).get(tableName);
    }

    @Override
    public DatabaseField[] getViewFields(Connection connection, String viewName) {
        return getViewFields(connection, viewName, true);
    }

    @Override
    public DatabaseField[] getViewFields(Connection connection, String viewName, Boolean withId) {
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
                        DatabaseField[] dbFieldArray = connectionTableFields.get(connection).get(tableName.toUpperCase());
                        for (DatabaseField dbField : dbFieldArray) {
                            dbFields.put(dbField.getQualifiedName(), dbField);
                        }
                        for (Field field : fields) {
                            DatabaseField dbField = dbFields.get(tableName.toUpperCase() + "." + field.getName().toUpperCase());
                            if (dbField == null) {
                                logger.error("Could not get database field for : " + tableName.toUpperCase() + "." + field.getName().toUpperCase());
                                continue;
                            }
                            String simpleName = field.getSimpleName();
                            if (simpleName != null && !"".equals(simpleName)) {
                                dbField.setSimpleName(simpleName);
                                dbFields.put(field.getName().toUpperCase(), dbField);
                            } else {
                                dbField.setSimpleName(field.getName().toUpperCase());
                            }
                            viewDbFields.add(dbField);
                        }
                    } else {
                        for (String tname : tableNames) {
                            DatabaseField[] dbFieldArray = connectionTableFields.get(connection).get(tname.toUpperCase());
                            for (DatabaseField dbField : dbFieldArray) {
                                dbFields.put(dbField.getQualifiedName(), dbField);
                            }
                            for (Field field : fields) {
                                DatabaseField dbField = dbFields.get(field.getName().toUpperCase());
                                if (dbField == null) {
                                    // field from another table in the join or error
                                    continue;
                                }
                                String simpleName = field.getSimpleName();
                                if (simpleName != null && !"".equals(simpleName)) {
                                    dbField.setSimpleName(simpleName);
                                    dbFields.put(field.getName().toUpperCase(), dbField);
                                } else {
                                    dbField.setSimpleName(field.getName().toUpperCase());
                                }
                                viewDbFields.add(dbField);
                            }
                        }
                    }

                    result = viewDbFields.toArray(new DatabaseField[0]);
                    finaliseView(viewName, result, tableNames[0].toUpperCase(), withId);
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
        String sql = "";
        if (withId) {
            sql = mainTable + ".ID";
        }
        boolean first = true;
        for (DatabaseField field : fields) {
            if (first && !withId) {
                sql = field.getTableDefinition().getName() + "." + field.getName();
                first = false;
            } else {
                sql += "," + field.getTableDefinition().getName() + "." + field.getName();
            }
            if (field.getSimpleName() != null) {
                sql += " as " + field.getSimpleName();
            }
        }
        viewsSql.put(name, sql);
    }

    public void terminate() {
        try {
            for (JdbcConnectionPool pool : connectionPools.values()) {
                pool.dispose();
            }
            server.stop();
            server.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public DbSessionData login(String database, String connectionName, String username, String password) throws SQLException {
        DbSessionData result = null;
        java.sql.Connection sqlConnection = null;
        JdbcConnectionPool connectionPool = null;
        try {
            Connection connection = connections.get(connectionName);
            connectionPool = JdbcConnectionPool.create(connection.getDatabase(), username, password);
            connectionPool.setMaxConnections(MAX_POOL_CONNECTIONS);
            sqlConnection = connectionPool.getConnection();
            // ok, got here, so login succeeds - setup session data
            result = new DbSessionData();
            result.setH2ConnectionPool(connectionPool);
            result.setPerson(getPerson(sqlConnection, username));
        } catch (JdbcSQLException e) {
            // nothing to do, as login not authenticated, and can return null
            logger.info("Unable to authenticate user: " + username + " for connection: " + connectionName);
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        } finally {
            if (connectionPool != null) {
                connectionPool.dispose();
            }
            if (sqlConnection != null) {
                sqlConnection.close();
            }
        }
        return result;
    }

    @Override
    public void logout(DbSessionData sessionData) {
        try {
            if (sessionData.getH2ConnectionPool() != null) {
                sessionData.getH2ConnectionPool().dispose();
            }
            sessionData.setPerson(null);
            sessionData.setH2ConnectionPool(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public String getViewSql(String connectionName, String viewName) {
        return getViewSql(connectionName, viewName, true);
    }

    @Override
    public QueryResult fetch(DbSessionData sessionData, String connectionName, String query, SearchDescriptor searchDescriptor, String findMany) throws SQLException {
        return fetch(connectionName, query, searchDescriptor, findMany);
    }

    @Override
    public QueryResult fetch(String connectionName, String query, SearchDescriptor searchDescriptor, String findMany) throws SQLException {
        QueryResult result = null;
        Statement statement = null;
        java.sql.Connection sqlConnection = null;
        try {
            Connection connection = connections.get(connectionName);
            sqlConnection = connectionPools.get(connection.getName()).getConnection();
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

    private String streamClobToString(java.sql.Clob clob) throws Exception {
        if (clob == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        final Reader reader = clob.getCharacterStream();
        final BufferedReader br = new BufferedReader(reader);
        int b;
        while (-1 != (b = br.read())) {
            sb.append((char) b);
        }
        br.close();
        return sb.toString();
    }

    private String clobToString(java.sql.Clob clob) throws Exception {
        return (clob == null) ? "" : clob.getSubString(1, (int) clob.length());
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
                record.setId(String.valueOf(results.getInt("ID")));
            } catch (SQLException se) {
                // some tables may not have id field - e.g. many_to_many tables - just ignore
            }
            for (DatabaseField field : vf) {
                FieldValue fieldValue = new FieldValue();
                fieldValue.setDataType(field.getDataType());
                String fieldName = field.getTableDefinition().getName() + "." + field.getName();
                record.setTable(field.getTableDefinition().getName());
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
                        fieldValue.setStringValue(clobToString(results.getClob(fieldName)));
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
        Record result = null;
        java.sql.Connection sqlConnection = null;
        Statement statement = null;
        try {
            sqlConnection = connectionPools.get(connectionName).getConnection();
            // do all in a single transaction - assume that give us exclusive lock so we can get the id back
            sqlConnection.setAutoCommit(false);
            String valuesBit = "values (";
            String sql = "insert into " + table + " (";
            boolean first = true;
            for (Map.Entry<String, String> entry : columns.entrySet()) {
                String v = (entry.getValue() != null) ? entry.getValue().replaceAll("'", "''") : null;
                if (first) {
                    first = false;
                    sql += entry.getKey();
                    valuesBit += "'" + v + "'";
                } else {
                    sql += "," + entry.getKey();
                    valuesBit += ",'" + v + "'";
                }
            }
            sql += ") " + valuesBit + ")";
            statement = sqlConnection.createStatement();
            statement.execute(sql);
            statement.close();

            result = new Record();
            // get the id back - if there is one
            try {
                statement = sqlConnection.createStatement();
                sql = "select max(id) from " + table;
                statement.execute(sql);
                statement.close();
                statement = sqlConnection.createStatement();
                statement.execute(sql);
                ResultSet results = statement.getResultSet();
                results.next();
                result.setId(String.valueOf(results.getInt(1)));
                statement.close();
            } catch (SQLException se) {
                // ok, no id, so ignore
            }

            statement = sqlConnection.createStatement();
            sql = "commit";
            statement.execute(sql);
            statement.close();
        } catch (SQLException se) {
            se.printStackTrace();
            if (sqlConnection != null) {
                statement = sqlConnection.createStatement();
                statement.execute("rollback");
                statement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    public Boolean update(String connectionName, String table, Integer id, HashMap<String, String> columns) throws SQLException {
        Boolean result = false;
        java.sql.Connection sqlConnection = null;
        Statement statement = null;
        try {
            sqlConnection = connectionPools.get(connectionName).getConnection();
            // do all in a single transaction - assume that give us exclusive lock so we can get the id back
            String sql = "update " + table + " set ";
            boolean first = true;
            for (Map.Entry<String, String> entry : columns.entrySet()) {
                String v = (entry.getValue() != null) ? entry.getValue().replaceAll("'", "''") : null;
                if (first) {
                    first = false;
                    sql += entry.getKey() + "='" + v + "'";
                } else {
                    sql += "," + entry.getKey() + "='" + v + "'";
                }
            }
            sql += " where id='" + id + "'";
            statement = sqlConnection.createStatement();
            statement.execute(sql);
            statement.close();
            result = true;
        } catch (SQLException se) {
            se.printStackTrace();
            if (sqlConnection != null) {
                statement = sqlConnection.createStatement();
                statement.close();
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
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
    public Boolean createOrUpdate(String connectionName, String table, HashMap<String, String> primaryKey, HashMap<String, String> columns) throws SQLException {
        Boolean result = false;
        java.sql.Connection sqlConnection = null;
        Statement statement = null;
        try {
            sqlConnection = connectionPools.get(connectionName).getConnection();
            String sql = "select * from " + table + " where ";
            boolean first = true;
            for (Map.Entry<String, String> entry : primaryKey.entrySet()) {
                if (first) {
                    sql += entry.getKey() + "='" + entry.getValue() + "'";
                    first = false;
                } else {
                    sql += " and " + entry.getKey() + "='" + entry.getValue() + "'";
                }
            }
            statement = sqlConnection.createStatement();
            statement.execute(sql);
            ResultSet results = statement.getResultSet();
            if (!results.next()) {
                HashMap<String, String> alLColumns = columns;
                for (Map.Entry<String, String> entry : primaryKey.entrySet()) {
                    alLColumns.put(entry.getKey(), entry.getValue());
                }
                Record record = create(connectionName, table, alLColumns);
                result = (record != null);
            } else {
                update(connectionName, table, primaryKey, columns);
            }
            statement.close();
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
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
    public Boolean update(String connectionName, String table, HashMap<String, String> primaryKey, HashMap<String, String> columns) throws SQLException {
        Boolean result = false;
        java.sql.Connection sqlConnection = null;
        Statement statement = null;
        try {
            sqlConnection = connectionPools.get(connectionName).getConnection();
            // do all in a single transaction - assume that give us exclusive lock so we can get the id back
            String sql = "update " + table + " set ";
            boolean first = true;
            for (Map.Entry<String, String> entry : columns.entrySet()) {
                String v = (entry.getValue() != null) ? entry.getValue().replaceAll("'", "''") : null;
                if (first) {
                    first = false;
                    sql += entry.getKey() + "='" + v + "'";
                } else {
                    sql += "," + entry.getKey() + "='" + v + "'";
                }
            }
            sql += " where ";
            first = true;
            for (Map.Entry<String, String> entry : primaryKey.entrySet()) {
                if (first) {
                    sql += entry.getKey() + "='" + entry.getValue() + "'";
                    first = false;
                } else {
                    sql += " and " + entry.getKey() + "='" + entry.getValue() + "'";
                }
            }
            statement = sqlConnection.createStatement();
            statement.execute(sql);
            statement.close();
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
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
    @Override
    public Boolean delete(String connectionName, String table, Integer id, String condition) throws SQLException {
        Boolean result = false;
        java.sql.Connection sqlConnection = null;
        Statement statement = null;
        try {
            sqlConnection = connectionPools.get(connectionName).getConnection();
            // do all in a single transaction - assume that give us exclusive lock so we can get the id back
            String sql = "delete from " + table;
            if (id != null) {
                sql += " where id=" + id;
                statement = sqlConnection.createStatement();
                statement.execute(sql);
                statement.close();
                result = true;
            } else if (condition != null) {
                sql += " where " + condition;
                statement = sqlConnection.createStatement();
                statement.execute(sql);
                statement.close();
                result = true;
            }
        } catch (SQLException se) {
            if (sqlConnection != null) {
                statement = sqlConnection.createStatement();
                statement.close();
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
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
    public Person checkUser(String connectionName, String username, String password) throws SQLException {
        Person result = null;
        java.sql.Connection sqlConnection = null;
        try {
            try {
                Connection connection = connections.get(connectionName);
                sqlConnection = DriverManager.getConnection(connection.getDatabase(), username, password);
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
        Person result = null;
        Statement statement = null;
        try {
            String query = "select * from person where username='" + username + "'";
            statement = sqlConnection.createStatement();
            statement.execute(query);
            ResultSet results = statement.getResultSet();
            boolean ok = results.next();
            if (ok) {
                result = new Person();
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
            //e.printStackTrace();
            // just ignore, maybe person table not the default
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
            sqlConnection = connectionPools.get(connectionName).getConnection();
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
    public DatabaseField[] getFields(Connection connection, String viewName) {
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
    public void setConnections(HashMap<String, Connection> connections) {
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
    public void stop() {
        try {
            if (server != null) {
                server.stop();
                server.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean execute(String connectionName, String statement) throws SQLException {
        Boolean result = false;
        java.sql.Connection sqlConnection = null;
        Statement sqlStatement = null;
        try {
            sqlConnection = connectionPools.get(connectionName).getConnection();
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
    public Boolean changeUserPassword(String connectionName, String username, String oldPassword, String newPassword) throws SQLException {
        Boolean result = false;
        java.sql.Connection sqlConnection = null;
        try {
            try {
                Connection connection = connections.get(connectionName);
                sqlConnection = DriverManager.getConnection(connection.getDatabase(), username, oldPassword);
                // if no SQLException thrown, then username and password are good
				/*
                 * Parameter "table" will be the name of the table holding user information such as name, email etc.
                 * username is the mey into that table - it must be unique
                 */

                // OK, then valid user, lets' change the password

                result = execute(connectionName, "alter user " + username + " set password '" + newPassword + "'");
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
    public Boolean changeUserPasswordAdmin(String connectionName, String username, String password, String adminUsername, String adminPassword) throws SQLException {
        Boolean result = false;
        java.sql.Connection sqlConnection = null;
        try {
            try {
                Connection connection = connections.get(connectionName);
                sqlConnection = DriverManager.getConnection(connection.getDatabase(), adminUsername, adminPassword);
                result = execute(connectionName, "alter user " + username + " set password '" + password + "'");
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
        Boolean result = false;
        java.sql.Connection sqlConnection = null;
        try {
            try {
                Connection connection = connections.get(connectionName);
                sqlConnection = DriverManager.getConnection(connection.getDatabase(), adminUsername, adminPassword);
                result = execute(connectionName, "create user " + username + " password '" + password + "'");
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
}
