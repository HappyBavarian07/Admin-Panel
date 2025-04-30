package de.happybavarian07.adminpanel.mysql.utils;

import de.happybavarian07.adminpanel.mysql.SQLExecutor;
import de.happybavarian07.adminpanel.mysql.annotations.Column;
import de.happybavarian07.adminpanel.mysql.annotations.Query;
import de.happybavarian07.adminpanel.mysql.annotations.Table;
import de.happybavarian07.adminpanel.mysql.repository.Repository;

import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepositoryProxy implements InvocationHandler {
    private final Class<?> repositoryInterface;
    private final SQLExecutor sqlExecutor;
    private String databasePrefix;
    private final Map<Class<?>, String> tableNameCache = new HashMap<>();
    // Neuer Cache für Entity-Typen
    private static final Map<Class<?>, Class<?>> entityTypeCache = new HashMap<>();

    public RepositoryProxy(Class<?> repositoryInterface, String databasePrefix, SQLExecutor sqlExecutor) {
        this.repositoryInterface = repositoryInterface;
        this.databasePrefix = databasePrefix;
        this.sqlExecutor = sqlExecutor;
    }

    public void setDatabasePrefix(String databasePrefix) {
        this.databasePrefix = databasePrefix;
    }

    public String getDatabasePrefix() {
        return databasePrefix;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Behandlung für die isDatabaseReady-Methode
        if (method.getName().equals("isDatabaseReady")) {
            return isDatabaseReady();
        }

        if (method.isAnnotationPresent(Query.class)) {
            String sql = method.getAnnotation(Query.class).value();
            ResultSet resultSet = sqlExecutor.executeQuery(sql, args);
            return sqlExecutor.mapResultSet(resultSet, method.getReturnType());
        } else if (method.getName().startsWith("findBy")) {
            Class<?> entityType = getEntityTypeFromRepository(repositoryInterface);
            String tableName = getTableName(entityType);
            String sql = parseMethodNameToQuery(method.getName(), tableName);
            ResultSet resultSet = sqlExecutor.executeQuery(sql, args);
            return sqlExecutor.mapResultSet(resultSet, entityType);
        } else if (isCrudMethod(method)) {
            return handleCrudOperation(method, args, proxy);
        }
        throw new UnsupportedOperationException("Method not supported: " + method.getName());
    }

    /**
     * Prüft, ob die Datenbankverbindung bereit ist.
     *
     * @return true, wenn die Datenbankverbindung funktioniert und Abfragen ausgeführt werden können,
     * false, wenn Probleme mit der Verbindung bestehen
     */
    private boolean isDatabaseReady() {
        try {
            // Einfache Testabfrage ausführen
            sqlExecutor.executeQuery("SELECT 1").close();
            return true;
        } catch (Exception e) {
            // Bei Fehlern ist die Datenbank nicht bereit
            return false;
        }
    }

    private boolean isCrudMethod(Method method) {
        // Prüfen, ob die Methode zu den CRUD-Operationen gehört
        return Arrays.asList("save", "findById", "findAll", "count", "delete", "existsById").contains(method.getName());
    }

    private Object handleCrudOperation(Method method, Object[] args, Object proxy) throws SQLException {
        String methodName = method.getName();
        Class<?> entityType = getEntityTypeFromRepository(repositoryInterface);
        String tableName = getTableName(entityType);

        return switch (methodName) {
            case "save" -> handleSave(args[0], entityType, tableName);
            case "findById" -> handleFindById(args[0], entityType, tableName);
            case "findAll" -> handleFindAll(entityType, tableName);
            case "count" -> handleCount(tableName);
            case "delete" -> handleDelete(args[0], entityType, tableName);
            case "existsById" -> handleExistsById(args[0], entityType, tableName);
            default -> throw new UnsupportedOperationException("CRUD method not implemented: " + methodName);
        };
    }

    private <T> T handleSave(Object entity, Class<T> entityType, String tableName) throws SQLException {
        Map<String, Object> columnValues = extractColumnValues(entity);
        String idColumnName = getPrimaryKeyColumnName(entityType);
        Object idValue = columnValues.get(idColumnName);
        boolean exists = false;

        if (idValue != null) {
            // Prüfen, ob die Entity bereits existiert
            String existsQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumnName + " = ?";
            try (ResultSet rs = sqlExecutor.executeQuery(existsQuery, idValue)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }
        }

        if (exists) {
            // UPDATE
            StringBuilder updateQuery = new StringBuilder("UPDATE " + tableName + " SET ");
            List<Object> params = new ArrayList<>();

            for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
                if (!entry.getKey().equals(idColumnName)) {
                    updateQuery.append(entry.getKey()).append(" = ?, ");
                    params.add(entry.getValue());
                }
            }

            updateQuery.setLength(updateQuery.length() - 2); // Entferne letztes Komma und Leerzeichen
            updateQuery.append(" WHERE ").append(idColumnName).append(" = ?");
            params.add(idValue);

            sqlExecutor.executeUpdate(updateQuery.toString(), params.toArray());
        } else {
            // INSERT
            StringBuilder insertQuery = new StringBuilder("INSERT INTO " + tableName + " (");
            StringBuilder valuePlaceholders = new StringBuilder(") VALUES (");
            List<Object> params = new ArrayList<>();

            for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
                insertQuery.append(entry.getKey()).append(", ");
                valuePlaceholders.append("?, ");
                params.add(entry.getValue());
            }

            insertQuery.setLength(insertQuery.length() - 2); // Entferne letztes Komma und Leerzeichen
            valuePlaceholders.setLength(valuePlaceholders.length() - 2); // Entferne letztes Komma und Leerzeichen
            valuePlaceholders.append(")");

            sqlExecutor.executeUpdate(insertQuery.toString() + valuePlaceholders, params.toArray());
        }

        return (T) entity;
    }

    private <T> Optional<T> handleFindById(Object id, Class<T> entityType, String tableName) throws SQLException {
        String idColumnName = getPrimaryKeyColumnName(entityType);
        String query = "SELECT * FROM " + tableName + " WHERE " + idColumnName + " = ?";
        ResultSet rs = sqlExecutor.executeQuery(query, id);
        List<T> results = sqlExecutor.mapResultSet(rs, entityType);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    private <T> Iterable<T> handleFindAll(Class<T> entityType, String tableName) throws SQLException {
        String query = "SELECT * FROM " + tableName;
        ResultSet rs = sqlExecutor.executeQuery(query);
        return sqlExecutor.mapResultSet(rs, entityType);
    }

    private long handleCount(String tableName) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName;
        try (ResultSet rs = sqlExecutor.executeQuery(query)) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    private Object handleDelete(Object entity, Class<?> entityType, String tableName) throws SQLException {
        Map<String, Object> columnValues = extractColumnValues(entity);
        String idColumnName = getPrimaryKeyColumnName(entityType);
        Object idValue = columnValues.get(idColumnName);

        if (idValue != null) {
            String deleteQuery = "DELETE FROM " + tableName + " WHERE " + idColumnName + " = ?";
            sqlExecutor.executeUpdate(deleteQuery, idValue);
        }

        return null;
    }

    private boolean handleExistsById(Object id, Class<?> entityType, String tableName) throws SQLException {
        String idColumnName = getPrimaryKeyColumnName(entityType);
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumnName + " = ?";
        try (ResultSet rs = sqlExecutor.executeQuery(query, id)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private String parseMethodNameToQuery(String methodName, String tableName) {
        // Entfernen des "findBy"-Präfixes
        String conditions = methodName.substring(6); // "findBy".length() = 6

        // Aufteilen nach "And" und "Or"
        List<String> conditionsList = new ArrayList<>();
        String currentLogicalOperator = " AND ";

        // Komplexere Condition-Parsing mit RegEx
        Pattern pattern = Pattern.compile("(And|Or)(?=[A-Z])");
        Matcher matcher = pattern.matcher(conditions);

        int lastEnd = 0;
        while (matcher.find()) {
            String condition = conditions.substring(lastEnd, matcher.start());
            conditionsList.add(condition);

            if (matcher.group().equals("Or")) {
                currentLogicalOperator = " OR ";
            } else {
                currentLogicalOperator = " AND ";
            }

            lastEnd = matcher.end();
        }

        // Letzte Bedingung hinzufügen
        conditionsList.add(conditions.substring(lastEnd));

        // SQL-Abfrage erstellen
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ").append(tableName).append(" WHERE ");

        for (int i = 0; i < conditionsList.size(); i++) {
            String condition = conditionsList.get(i);
            appendCondition(sqlBuilder, condition);

            if (i < conditionsList.size() - 1) {
                sqlBuilder.append(currentLogicalOperator);
            }
        }

        return sqlBuilder.toString();
    }

    private void appendCondition(StringBuilder sqlBuilder, String condition) {
        // Erkennung von Operatoren wie GreaterThan, LessThan, etc.
        Map<String, String> operators = new HashMap<>();
        operators.put("GreaterThan", " > ");
        operators.put("LessThan", " < ");
        operators.put("GreaterThanEqual", " >= ");
        operators.put("LessThanEqual", " <= ");
        operators.put("NotEqual", " <> ");
        operators.put("Like", " LIKE ");
        operators.put("NotLike", " NOT LIKE ");
        operators.put("IsNull", " IS NULL");
        operators.put("IsNotNull", " IS NOT NULL");
        operators.put("Between", " BETWEEN ? AND ?");
        operators.put("In", " IN (?)");

        String operator = " = ";
        String columnName = condition;

        for (Map.Entry<String, String> entry : operators.entrySet()) {
            if (condition.endsWith(entry.getKey())) {
                operator = entry.getValue();
                columnName = condition.substring(0, condition.length() - entry.getKey().length());
                break;
            }
        }

        // Kleinschreibung für den ersten Buchstaben, dann CamelCase zu Snake_case
        columnName = camelToSnakeCase(columnName);

        sqlBuilder.append(columnName);

        if (operator.equals(" IS NULL") || operator.equals(" IS NOT NULL")) {
            sqlBuilder.append(operator);
        } else {
            sqlBuilder.append(operator).append("?");
        }
    }

    private String camelToSnakeCase(String camelCase) {
        // Erster Buchstabe in Kleinbuchstaben
        String result = Character.toLowerCase(camelCase.charAt(0)) + camelCase.substring(1);

        // Spezialbehandlung für Akronyme wie UUID, damit diese nicht zerlegt werden
        // Entfernt zusätzliche Unterstriche zwischen aufeinanderfolgenden Großbuchstaben
        // z.B. "playerUUID" -> "player_uuid" anstatt "player_u_u_i_d"
        StringBuilder snakeCase = new StringBuilder();
        boolean lastWasUpper = false;

        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            if (Character.isUpperCase(c)) {
                if (!lastWasUpper && i > 0) {
                    snakeCase.append('_');
                }
                snakeCase.append(Character.toLowerCase(c));
                lastWasUpper = true;
            } else {
                snakeCase.append(c);
                lastWasUpper = false;
            }
        }

        return snakeCase.toString();
    }

    private String getTableName(Class<?> entityClass) {
        if (tableNameCache.containsKey(entityClass)) {
            return databasePrefix + tableNameCache.get(entityClass);
        }

        if (entityClass.isAnnotationPresent(Table.class)) {
            Table table = entityClass.getAnnotation(Table.class);
            String tableName = table.name();
            tableNameCache.put(entityClass, tableName);
            return databasePrefix + tableName;
        }

        // Fallback: Klassenname als Tabellennamen verwenden
        String tableName = camelToSnakeCase(entityClass.getSimpleName());
        tableNameCache.put(entityClass, tableName);
        return databasePrefix + tableName;
    }

    private Class<?> getEntityTypeFromRepository(Class<?> repositoryClass) {
        // Aus dem Cache abrufen, falls vorhanden
        if (entityTypeCache.containsKey(repositoryClass)) {
            return entityTypeCache.get(repositoryClass);
        }

        // Direkt versuchen, den generischen Typ aus den direkt implementierten Interfaces zu extrahieren
        for (Type type : repositoryClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType parameterizedType) {
                if (Repository.class.equals(parameterizedType.getRawType())) {
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?> entityType) {
                        // Im Cache speichern
                        entityTypeCache.put(repositoryClass, entityType);
                        return entityType;
                    }
                }
            }
        }
        // Rekursiver Fallback: Überprüfen der erweiterten Interfaces
        for (Class<?> exportedInterface : repositoryClass.getInterfaces()) {
            try {
                Class<?> entityType = getEntityTypeFromRepository(exportedInterface);
                // Im Cache speichern
                entityTypeCache.put(repositoryClass, entityType);
                return entityType;
            } catch (IllegalArgumentException ignored) {
                // Nächsten Interface prüfen
            }
        }
        throw new IllegalArgumentException("Could not determine entity type from repository: " + repositoryClass.getName());
    }

    private Map<String, Object> extractColumnValues(Object entity) {
        Map<String, Object> values = new HashMap<>();

        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name();

                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    values.put(columnName, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Could not access field: " + field.getName(), e);
                }
            }
        }

        return values;
    }

    private String getPrimaryKeyColumnName(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column.primaryKey()) {
                    return column.name();
                }
            }
        }
        return "id"; // Standard-ID-Spaltenname als Fallback
    }

    public static <T> T create(Class<T> repositoryInterface, String databasePrefix, SQLExecutor sqlExecutor) {
        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[]{repositoryInterface},
                new RepositoryProxy(repositoryInterface, databasePrefix, sqlExecutor)
        );
    }
}

