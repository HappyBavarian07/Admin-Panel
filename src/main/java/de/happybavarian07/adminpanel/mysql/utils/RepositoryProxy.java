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

    private static final Map<Class<?>, Class<?>> entityTypeCache = new HashMap<>();
    private final Class<?> repositoryInterface;
    private final SQLExecutor sqlExecutor;
    private final Map<Class<?>, String> tableNameCache = new HashMap<>();
    private String databasePrefix;

    public RepositoryProxy(Class<?> repositoryInterface, String databasePrefix, SQLExecutor sqlExecutor) {
        this.repositoryInterface = repositoryInterface;
        this.databasePrefix = databasePrefix;
        this.sqlExecutor = sqlExecutor;
    }

    public static <T> T create(Class<T> repositoryInterface, String databasePrefix, SQLExecutor sqlExecutor) {
        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[]{repositoryInterface},
                new RepositoryProxy(repositoryInterface, databasePrefix, sqlExecutor)
        );
    }

    public String getDatabasePrefix() {
        return databasePrefix;
    }

    public void setDatabasePrefix(String databasePrefix) {
        this.databasePrefix = databasePrefix;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

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
        } else if (method.getName().startsWith("countBy")) {
            Class<?> entityType = getEntityTypeFromRepository(repositoryInterface);
            String tableName = getTableName(entityType);
            String sql = parseCountMethodNameToQuery(method.getName(), tableName);
            ResultSet resultSet = sqlExecutor.executeQuery(sql, args);
            return extractCountResult(resultSet);
        } else if (method.getName().startsWith("existsBy")) {
            Class<?> entityType = getEntityTypeFromRepository(repositoryInterface);
            String tableName = getTableName(entityType);
            String sql = parseExistsMethodNameToQuery(method.getName(), tableName);
            ResultSet resultSet = sqlExecutor.executeQuery(sql, args);
            return extractExistsResult(resultSet);
        } else if (method.getName().startsWith("deleteBy")) {
            Class<?> entityType = getEntityTypeFromRepository(repositoryInterface);
            String tableName = getTableName(entityType);
            String sql = parseDeleteMethodNameToQuery(method.getName(), tableName);
            sqlExecutor.executeUpdate(sql, args);
            return null;
        } else if (method.getName().startsWith("countDistinct")) {
            Class<?> entityType = getEntityTypeFromRepository(repositoryInterface);
            String tableName = getTableName(entityType);
            String sql = parseCountDistinctMethodToQuery(method.getName(), tableName);
            ResultSet resultSet = (args == null || args.length == 0) ?
                    sqlExecutor.executeQuery(sql) :
                    sqlExecutor.executeQuery(sql, args);
            return extractCountResult(resultSet);
        } else if (method.getName().startsWith("findFirst") || method.getName().startsWith("findTop")) {
            Class<?> entityType = getEntityTypeFromRepository(repositoryInterface);
            String tableName = getTableName(entityType);
            String sql = parseLimitedFindMethodToQuery(method.getName(), tableName);
            ResultSet resultSet = sqlExecutor.executeQuery(sql, args);
            List<?> results = sqlExecutor.mapResultSet(resultSet, entityType);
            if (method.getReturnType() == Optional.class) {
                return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            }
            return results.isEmpty() ? null : results.get(0);
        } else if (method.getName().startsWith("sumBy") || method.getName().startsWith("avgBy") ||
                method.getName().startsWith("maxBy") || method.getName().startsWith("minBy")) {
            Class<?> entityType = getEntityTypeFromRepository(repositoryInterface);
            String tableName = getTableName(entityType);
            String sql = parseAggregateMethodToQuery(method.getName(), tableName);
            ResultSet resultSet = sqlExecutor.executeQuery(sql, args);
            return extractAggregateResult(resultSet, method.getReturnType());
        } else if (method.getName().contains("OrderBy")) {
            Class<?> entityType = getEntityTypeFromRepository(repositoryInterface);
            String tableName = getTableName(entityType);
            String sql = parseOrderedMethodToQuery(method.getName(), tableName);
            ResultSet resultSet = sqlExecutor.executeQuery(sql, args);
            return sqlExecutor.mapResultSet(resultSet, entityType);
        } else if (isSpecialCountMethod(method)) {
            Class<?> entityType = getEntityTypeFromRepository(repositoryInterface);
            String tableName = getTableName(entityType);
            String sql = parseSpecialCountMethod(method.getName(), tableName, args);
            ResultSet resultSet = (args == null || args.length == 0) ?
                    sqlExecutor.executeQuery(sql) :
                    sqlExecutor.executeQuery(sql, args);
            return extractCountResult(resultSet);
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
            sqlExecutor.executeQuery("SELECT 1").close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isCrudMethod(Method method) {
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
            String existsQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumnName + " = ?";
            try (ResultSet rs = sqlExecutor.executeQuery(existsQuery, idValue)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }
        }

        if (exists) {

            StringBuilder updateQuery = new StringBuilder("UPDATE " + tableName + " SET ");
            List<Object> params = new ArrayList<>();

            for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
                if (!entry.getKey().equals(idColumnName)) {
                    updateQuery.append(entry.getKey()).append(" = ?, ");
                    params.add(entry.getValue());
                }
            }

            updateQuery.setLength(updateQuery.length() - 2); 
            updateQuery.append(" WHERE ").append(idColumnName).append(" = ?");
            params.add(idValue);

            sqlExecutor.executeUpdate(updateQuery.toString(), params.toArray());
        } else {

            StringBuilder insertQuery = new StringBuilder("INSERT INTO " + tableName + " (");
            StringBuilder valuePlaceholders = new StringBuilder(") VALUES (");
            List<Object> params = new ArrayList<>();

            for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
                insertQuery.append(entry.getKey()).append(", ");
                valuePlaceholders.append("?, ");
                params.add(entry.getValue());
            }

            insertQuery.setLength(insertQuery.length() - 2);
            valuePlaceholders.setLength(valuePlaceholders.length() - 2); 
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

        String conditions = methodName.substring(6);


        List<String> conditionsList = new ArrayList<>();
        String currentLogicalOperator = " AND ";


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


        conditionsList.add(conditions.substring(lastEnd));


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


        columnName = camelToSnakeCase(columnName);

        sqlBuilder.append(columnName);

        if (operator.equals(" IS NULL") || operator.equals(" IS NOT NULL")) {
            sqlBuilder.append(operator);
        } else {
            sqlBuilder.append(operator).append("?");
        }
    }

    private String camelToSnakeCase(String camelCase) {

        String result = Character.toLowerCase(camelCase.charAt(0)) + camelCase.substring(1);


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


        String tableName = camelToSnakeCase(entityClass.getSimpleName());
        tableNameCache.put(entityClass, tableName);
        return databasePrefix + tableName;
    }

    private Class<?> getEntityTypeFromRepository(Class<?> repositoryClass) {

        if (entityTypeCache.containsKey(repositoryClass)) {
            return entityTypeCache.get(repositoryClass);
        }


        for (Type type : repositoryClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType parameterizedType) {
                if (Repository.class.equals(parameterizedType.getRawType())) {
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?> entityType) {

                        entityTypeCache.put(repositoryClass, entityType);
                        return entityType;
                    }
                }
            }
        }

        for (Class<?> exportedInterface : repositoryClass.getInterfaces()) {
            try {
                Class<?> entityType = getEntityTypeFromRepository(exportedInterface);

                entityTypeCache.put(repositoryClass, entityType);
                return entityType;
            } catch (IllegalArgumentException ignored) {

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
        return "id";
    }

    private String parseCountMethodNameToQuery(String methodName, String tableName) {

        String conditions = methodName.substring(8);


        List<String> conditionsList = new ArrayList<>();
        String currentLogicalOperator = " AND ";


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


        conditionsList.add(conditions.substring(lastEnd));


        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName).append(" WHERE ");

        for (int i = 0; i < conditionsList.size(); i++) {
            String condition = conditionsList.get(i);
            appendCondition(sqlBuilder, condition);

            if (i < conditionsList.size() - 1) {
                sqlBuilder.append(currentLogicalOperator);
            }
        }

        return sqlBuilder.toString();
    }

    private Long extractCountResult(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return resultSet.getLong(1);
        }
        return 0L;
    }

    private String parseExistsMethodNameToQuery(String methodName, String tableName) {

        String conditions = methodName.substring(8);


        List<String> conditionsList = new ArrayList<>();
        String currentLogicalOperator = " AND ";


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


        conditionsList.add(conditions.substring(lastEnd));


        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName).append(" WHERE ");

        for (int i = 0; i < conditionsList.size(); i++) {
            String condition = conditionsList.get(i);
            appendCondition(sqlBuilder, condition);

            if (i < conditionsList.size() - 1) {
                sqlBuilder.append(currentLogicalOperator);
            }
        }

        return sqlBuilder.toString();
    }

    private Boolean extractExistsResult(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return resultSet.getInt(1) > 0;
        }
        return false;
    }

    private String parseDeleteMethodNameToQuery(String methodName, String tableName) {

        String conditions = methodName.substring(8);


        List<String> conditionsList = new ArrayList<>();
        String currentLogicalOperator = " AND ";


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


        conditionsList.add(conditions.substring(lastEnd));


        StringBuilder sqlBuilder = new StringBuilder("DELETE FROM ").append(tableName).append(" WHERE ");

        for (int i = 0; i < conditionsList.size(); i++) {
            String condition = conditionsList.get(i);
            appendCondition(sqlBuilder, condition);

            if (i < conditionsList.size() - 1) {
                sqlBuilder.append(currentLogicalOperator);
            }
        }

        return sqlBuilder.toString();
    }

    private String parseCountDistinctMethodToQuery(String methodName, String tableName) {
        if (methodName.equals("countDistinctPlayers")) {
            return "SELECT COUNT(DISTINCT player_uuid) FROM " + tableName;
        } else if (methodName.equals("countDistinctPermissions")) {
            return "SELECT COUNT(DISTINCT permission) FROM " + tableName;
        }

        String conditions = methodName.substring(13);
        String distinctColumn = camelToSnakeCase(conditions.split("By")[0]);

        if (conditions.contains("By")) {
            String whereConditions = conditions.substring(conditions.indexOf("By") + 2);
            List<String> conditionsList = parseConditions(whereConditions);
            StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(DISTINCT ").append(distinctColumn).append(") FROM ").append(tableName).append(" WHERE ");

            for (int i = 0; i < conditionsList.size(); i++) {
                appendCondition(sqlBuilder, conditionsList.get(i));
                if (i < conditionsList.size() - 1) {
                    sqlBuilder.append(" AND ");
                }
            }
            return sqlBuilder.toString();
        } else {
            return "SELECT COUNT(DISTINCT " + distinctColumn + ") FROM " + tableName;
        }
    }

    private boolean isSpecialCountMethod(Method method) {
        String methodName = method.getName();
        return methodName.equals("countEntriesByUUID") ||
                methodName.equals("countTruePermissions") ||
                methodName.equals("countFalsePermissions") ||
                methodName.equals("countPermissionsStartingWith") ||
                methodName.equals("countPlayersWithPermission");
    }

    private String parseSpecialCountMethod(String methodName, String tableName, Object[] args) {
        return switch (methodName) {
            case "countEntriesByUUID" -> "SELECT COUNT(*) FROM " + tableName + " WHERE player_uuid = ?";
            case "countTruePermissions" -> "SELECT COUNT(*) FROM " + tableName + " WHERE value = true";
            case "countFalsePermissions" -> "SELECT COUNT(*) FROM " + tableName + " WHERE value = false";
            case "countPermissionsStartingWith" ->
                    "SELECT COUNT(*) FROM " + tableName + " WHERE permission LIKE CONCAT(?, '%')";
            case "countPlayersWithPermission" ->
                    "SELECT COUNT(DISTINCT player_uuid) FROM " + tableName + " WHERE permission = ?";
            default -> throw new IllegalArgumentException("Unknown special count method: " + methodName);
        };
    }

    private List<String> parseConditions(String conditions) {
        List<String> conditionsList = new ArrayList<>();
        Pattern pattern = Pattern.compile("(And|Or)(?=[A-Z])");
        Matcher matcher = pattern.matcher(conditions);

        int lastEnd = 0;
        while (matcher.find()) {
            conditionsList.add(conditions.substring(lastEnd, matcher.start()));
            lastEnd = matcher.end();
        }
        conditionsList.add(conditions.substring(lastEnd));
        return conditionsList;
    }

    private String parseLimitedFindMethodToQuery(String methodName, String tableName) {
        String baseQuery;
        int limit = 1;

        if (methodName.startsWith("findFirst")) {
            String conditions = methodName.substring(9);
            if (conditions.startsWith("By")) {
                conditions = conditions.substring(2);
                List<String> conditionsList = parseConditions(conditions);
                StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ").append(tableName).append(" WHERE ");

                for (int i = 0; i < conditionsList.size(); i++) {
                    appendCondition(sqlBuilder, conditionsList.get(i));
                    if (i < conditionsList.size() - 1) {
                        sqlBuilder.append(" AND ");
                    }
                }
                baseQuery = sqlBuilder.toString();
            } else {
                baseQuery = "SELECT * FROM " + tableName;
            }
        } else if (methodName.startsWith("findTop")) {
            Pattern topPattern = Pattern.compile("findTop(\\d+)(.*)");
            Matcher matcher = topPattern.matcher(methodName);
            if (matcher.matches()) {
                limit = Integer.parseInt(matcher.group(1));
                String remaining = matcher.group(2);
                if (remaining.startsWith("By")) {
                    String conditions = remaining.substring(2);
                    List<String> conditionsList = parseConditions(conditions);
                    StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ").append(tableName).append(" WHERE ");

                    for (int i = 0; i < conditionsList.size(); i++) {
                        appendCondition(sqlBuilder, conditionsList.get(i));
                        if (i < conditionsList.size() - 1) {
                            sqlBuilder.append(" AND ");
                        }
                    }
                    baseQuery = sqlBuilder.toString();
                } else {
                    baseQuery = "SELECT * FROM " + tableName;
                }
            } else {
                baseQuery = "SELECT * FROM " + tableName;
            }
        } else {
            baseQuery = "SELECT * FROM " + tableName;
        }

        return baseQuery + " LIMIT " + limit;
    }

    private String parseAggregateMethodToQuery(String methodName, String tableName) {
        String aggregateFunction;
        String columnName = null;
        String conditions;

        if (methodName.startsWith("sumBy")) {
            aggregateFunction = "SUM";
            conditions = methodName.substring(5);
        } else if (methodName.startsWith("avgBy")) {
            aggregateFunction = "AVG";
            conditions = methodName.substring(5);
        } else if (methodName.startsWith("maxBy")) {
            aggregateFunction = "MAX";
            conditions = methodName.substring(5);
        } else if (methodName.startsWith("minBy")) {
            aggregateFunction = "MIN";
            conditions = methodName.substring(5);
        } else {
            throw new IllegalArgumentException("Unknown aggregate function in method: " + methodName);
        }

        String[] parts = conditions.split("And|Or");
        if (parts.length > 0) {
            columnName = camelToSnakeCase(parts[0]);
        }

        if (columnName == null) {
            columnName = "*";
        }

        return "SELECT " + aggregateFunction + "(" + columnName + ") FROM " + tableName;
    }

    private String parseOrderedMethodToQuery(String methodName, String tableName) {
        String[] parts = methodName.split("OrderBy");
        String basePart = parts[0];
        String orderPart = parts.length > 1 ? parts[1] : "";

        StringBuilder sqlBuilder = new StringBuilder();

        if (basePart.startsWith("findBy")) {
            String conditions = basePart.substring(6);
            if (!conditions.isEmpty()) {
                List<String> conditionsList = parseConditions(conditions);
                sqlBuilder.append("SELECT * FROM ").append(tableName).append(" WHERE ");

                for (int i = 0; i < conditionsList.size(); i++) {
                    appendCondition(sqlBuilder, conditionsList.get(i));
                    if (i < conditionsList.size() - 1) {
                        sqlBuilder.append(" AND ");
                    }
                }
            } else {
                sqlBuilder.append("SELECT * FROM ").append(tableName);
            }
        } else {
            sqlBuilder.append("SELECT * FROM ").append(tableName);
        }

        if (!orderPart.isEmpty()) {
            String direction = "ASC";
            if (orderPart.endsWith("Desc")) {
                direction = "DESC";
                orderPart = orderPart.substring(0, orderPart.length() - 4);
            } else if (orderPart.endsWith("Asc")) {
                orderPart = orderPart.substring(0, orderPart.length() - 3);
            }

            String orderColumn = camelToSnakeCase(orderPart);
            sqlBuilder.append(" ORDER BY ").append(orderColumn).append(" ").append(direction);
        }

        return sqlBuilder.toString();
    }

    private Object extractAggregateResult(ResultSet resultSet, Class<?> returnType) throws SQLException {
        if (resultSet.next()) {
            Object value = resultSet.getObject(1);
            if (value == null) return null;

            if (returnType == Integer.class || returnType == int.class) {
                return resultSet.getInt(1);
            } else if (returnType == Long.class || returnType == long.class) {
                return resultSet.getLong(1);
            } else if (returnType == Double.class || returnType == double.class) {
                return resultSet.getDouble(1);
            } else if (returnType == Float.class || returnType == float.class) {
                return resultSet.getFloat(1);
            } else {
                return value;
            }
        }
        return null;
    }
}
