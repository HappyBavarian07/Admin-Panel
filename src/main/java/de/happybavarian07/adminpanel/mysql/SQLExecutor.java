package de.happybavarian07.adminpanel.mysql;

import de.happybavarian07.adminpanel.mysql.annotations.Column;
import de.happybavarian07.adminpanel.mysql.annotations.Entity;
import de.happybavarian07.adminpanel.mysql.annotations.Table;
import de.happybavarian07.adminpanel.mysql.exceptions.MySQLSystemExceptions;
import de.happybavarian07.adminpanel.mysql.interfaces.ResultSetValueConverter;
import de.happybavarian07.adminpanel.mysql.utils.DatabaseProperties;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class SQLExecutor {
    private final Map<String, Connection> connections = new HashMap<>();
    private final DatabaseProperties dbProperties;
    private String defaultConnection;

    public SQLExecutor(DatabaseProperties dbProperties) {
        this.dbProperties = dbProperties;
    }

    public void addConnection(String name, Connection connection) {
        connections.put(name, connection);
        if (defaultConnection == null) {
            defaultConnection = name;
        }
    }

    public Connection getConnection(String name) {
        return connections.getOrDefault(name, connections.get(defaultConnection));
    }

    public void executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = getConnection(defaultConnection).prepareStatement(sql)) {
            bindParameters(stmt, params);
            stmt.executeUpdate();
        }
    }

    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = getConnection(defaultConnection).prepareStatement(sql);
        bindParameters(stmt, params);
        return stmt.executeQuery();
    }

    private void bindParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    public void generateSchema(Class<?> entityClass) throws SQLException {
        if (!entityClass.isAnnotationPresent(Entity.class) || !entityClass.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Class must be annotated with @Entity and @Table");
        }
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        String tableName = tableAnnotation.name();
        String schema = tableAnnotation.schema();
        String fullTableName = (!schema.isEmpty() ? schema + "." : "") + dbProperties.getDatabasePrefix() + tableName;
        Connection conn = getConnection(defaultConnection);
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet tables = meta.getTables(conn.getCatalog(), schema, dbProperties.getDatabasePrefix() + tableName, new String[]{"TABLE"});
        // Debugwise list result set
        while (tables.next()) {
            System.out.println("Table: " + tables.getString("TABLE_NAME"));
        }
        if (tables.next()) {
            // Tabelle existiert: Migration – fehlende Spalten hinzufügen
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Column.class)) continue;
                Column colAnn = field.getAnnotation(Column.class);
                String columnName = colAnn.name();
                ResultSet cols = meta.getColumns(conn.getCatalog(), schema, dbProperties.getDatabasePrefix() + tableName, columnName);
                if (!cols.next()) {
                    String alterSQL = "ALTER TABLE " + fullTableName + " ADD COLUMN " + getColumnDefinition(field) + ";";
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate(alterSQL);
                    }
                }
                cols.close();
            }
        } else {
            // Tabelle existiert nicht: CREATE TABLE ausführen
            String createTableSQL = generateCreateTableSQL(entityClass, tableName, schema);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(createTableSQL);
            }
        }
        tables.close();
    }

    private String getColumnDefinition(Field field) {
        if (!field.isAnnotationPresent(Column.class)) {
            throw new IllegalArgumentException("Field " + field.getName() + " hat keine Column-Annotation");
        }
        Column column = field.getAnnotation(Column.class);
        // Nutze den angegebenen Spaltennamen oder den Feldnamen
        String columnName = column.name().isEmpty() ? field.getName() : column.name();
        // Hole den SQL-Datentyp (methode getSQLType() existiert ja bereits)
        String sqlType = getSQLType(field);

        StringBuilder definition = new StringBuilder();
        definition.append(columnName).append(" ").append(sqlType);

        // Ist die Spalte nicht nullable -> NOT NULL
        if (!column.nullable()) {
            definition.append(" NOT NULL");
        }

        // Ist die Spalte als UNIQUE markiert -> UNIQUE
        if (column.unique()) {
            definition.append(" UNIQUE");
        }

        // Primärschlüssel (PRIMARY KEY)
        if (column.primaryKey()) {
            definition.append(" PRIMARY KEY");
        }

        // AUTO_INCREMENT falls gewünscht (Achtung: MySQL benötigt das typischerweise für numerische Felder)
        if (column.autoIncrement()) {
            definition.append(" AUTO_INCREMENT");
        }

        return definition.toString();
    }


    private String generateCreateTableSQL(Class<?> entityClass, String tableName, String schema) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        if (!schema.isEmpty()) {
            sql.append(schema).append(".");
        }
        // Hier wird zusätzlich der Database-Prefix genutzt
        sql.append(dbProperties.getDatabasePrefix()).append(tableName).append(" (");

        Field[] fields = entityClass.getDeclaredFields();
        List<String> columnDefinitions = new ArrayList<>();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                columnDefinitions.add(getColumnDefinition(field));
            }
        }

        // Die einzelnen Spaltendefinitionen werden durch Kommas getrennt
        sql.append(String.join(", ", columnDefinitions));
        sql.append(");");

        return sql.toString();
    }


    private String getSQLType(Field field) {
        Class<?> type = field.getType();
        if (type == int.class || type == Integer.class) {
            return "INT";
        } else if (type == long.class || type == Long.class) {
            return "BIGINT";
        } else if (type == double.class || type == Double.class) {
            return "DOUBLE";
        } else if (type == float.class || type == Float.class) {
            return "FLOAT";
        } else if (type == boolean.class || type == Boolean.class) {
            return "BOOLEAN";
        } else if (type == String.class) {
            return "VARCHAR(255)";
        } else if (type == Date.class) {
            return "DATETIME";
        } else if (type == UUID.class) {
            return "VARCHAR(36)";
        } else if (type.isEnum()) {
            return "VARCHAR(255)";
        } else if (type == byte[].class) {
            return "BLOB";
        } else if (type == short.class || type == Short.class) {
            return "SMALLINT";
        } else if (type == byte.class || type == Byte.class) {
            return "TINYINT";
        }
        throw new IllegalArgumentException("Unsupported field type: " + type);
    }

    public void close() throws MySQLSystemExceptions.DatabaseConnectionException {
        for (Connection connection : connections.values()) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new MySQLSystemExceptions.DatabaseConnectionException("Failed to close connection: " + e.getMessage(), e);
            }
        }
    }

    public void setDefaultConnection(String name) {
        if (connections.containsKey(name)) {
            defaultConnection = name;
        } else {
            throw new IllegalArgumentException("Connection not found: " + name);
        }
    }

    public String getDefaultConnection() {
        return defaultConnection;
    }

    public Map<String, Connection> getConnections() {
        return connections;
    }

    public void setConnections(Map<String, Connection> connections) {
        this.connections.clear();
        this.connections.putAll(connections);
    }

    public void removeConnection(String name) {
        connections.remove(name);
        if (defaultConnection != null && defaultConnection.equals(name)) {
            defaultConnection = null;
        }
    }

    public void clearConnections() {
        connections.clear();
        defaultConnection = null;
    }

    public void executeBatchUpdate(String sql, List<Object[]> paramsList) throws SQLException {
        try (PreparedStatement stmt = getConnection(defaultConnection).prepareStatement(sql)) {
            for (Object[] params : paramsList) {
                bindParameters(stmt, params);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void executeBatchUpdate(String sql, Object[][] paramsArray) throws SQLException {
        try (PreparedStatement stmt = getConnection(defaultConnection).prepareStatement(sql)) {
            for (Object[] params : paramsArray) {
                bindParameters(stmt, params);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void executeBatchQuery(String sql, List<Object[]> paramsList) throws SQLException {
        try (PreparedStatement stmt = getConnection(defaultConnection).prepareStatement(sql)) {
            for (Object[] params : paramsList) {
                bindParameters(stmt, params);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void executeBatchQuery(String sql, Object[][] paramsArray) throws SQLException {
        try (PreparedStatement stmt = getConnection(defaultConnection).prepareStatement(sql)) {
            for (Object[] params : paramsArray) {
                bindParameters(stmt, params);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void executeTransaction(List<String> sqlStatements) throws SQLException {
        Connection connection = getConnection(defaultConnection);
        try {
            connection.setAutoCommit(false);
            for (String sql : sqlStatements) {
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void executeTransaction(String... sqlStatements) throws SQLException {
        executeTransaction(Arrays.asList(sqlStatements));
    }

    public void executeTransaction(PreparedStatement... preparedStatements) throws SQLException {
        Connection connection = getConnection(defaultConnection);
        try {
            connection.setAutoCommit(false);
            for (PreparedStatement stmt : preparedStatements) {
                stmt.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void executeTransaction(Runnable... operations) throws SQLException {
        Connection connection = getConnection(defaultConnection);
        try {
            connection.setAutoCommit(false);
            for (Runnable operation : operations) {
                operation.run();
            }
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException("Transaction failed", e);
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public <T> List<T> mapResultSet(ResultSet resultSet, Class<T> type) throws SQLException {
        List<T> results = new ArrayList<>();
        try {
            while (resultSet.next()) {
                T instance = type.getDeclaredConstructor().newInstance();
                for (Field field : type.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Column.class)) {
                        Column column = field.getAnnotation(Column.class);
                        String columnName = column.name();
                        Object value = resultSet.getObject(columnName);
                        ResultSetValueConverter converter = null;
                        if (!column.converter().equals(ResultSetValueConverter.class)) {
                            converter = column.converter().getDeclaredConstructor().newInstance();
                        }
                        value = convertValue(field, value, Optional.ofNullable(converter));
                        field.setAccessible(true);
                        field.set(instance, value);
                    }
                }
                results.add(instance);
            }
        } catch (Exception e) {
            throw new SQLException("Error mapping ResultSet to object", e);
        }
        return results;
    }

    private Object convertValue(Field field, Object value, Optional<ResultSetValueConverter> converter) throws MySQLSystemExceptions.OutputConversionException {
        if (value != null) {
            Class<?> type = field.getType();
            // UUID conversion from String
            switch (type.getSimpleName()) {
                case "UUID":
                    if (value instanceof String) {
                        return UUID.fromString((String) value);
                    }
                    break;
                case "Date":
                    if (value instanceof Timestamp) {
                        return new Date(((Timestamp) value).getTime());
                    }
                    if (value instanceof String) {
                        try {
                            return new Date(Long.parseLong((String) value));
                        } catch (NumberFormatException e) {
                            throw new MySQLSystemExceptions.OutputConversionException(
                                    "Failed to convert value to Date: " + value, e);
                        }
                    }
                    break;
                case "Boolean":
                case "boolean":
                    if (value instanceof Number) {
                        return ((Number) value).intValue() != 0;
                    }
                    if (value instanceof String) {
                        return Boolean.parseBoolean((String) value);
                    }
                    break;
                case "Integer":
                case "int":
                    if (value instanceof Number) {
                        return ((Number) value).intValue();
                    }
                    if (value instanceof String) {
                        return Integer.parseInt((String) value);
                    }
                    break;
                case "Long":
                case "long":
                    if (value instanceof Number) {
                        return ((Number) value).longValue();
                    }
                    if (value instanceof String) {
                        return Long.parseLong((String) value);
                    }
                    break;
                case "Double":
                case "double":
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                    if (value instanceof String) {
                        return Double.parseDouble((String) value);
                    }
                    break;
                default:
                    // Use the custom converter if provided
                    if (converter.isPresent()) {
                        return converter.get().convert(field, value);
                    }
                    break;
            }
            return value;
        }
        return null;
    }

    public void setDatabasePrefix(String prefix) {
        dbProperties.setDatabasePrefix(prefix);
    }
}
