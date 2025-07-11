package de.happybavarian07.adminpanel.mysql.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SQLQueryBuilder {
    public enum QueryType {
        SELECT, INSERT, UPDATE, DELETE, CUSTOM
    }

    private QueryType queryType;
    private final List<String> selectColumns = new ArrayList<>();
    private String databasePrefix;
    private String tableName;
    private final List<String> joinClauses = new ArrayList<>();
    private String whereClause;
    private final List<String> groupByColumns = new ArrayList<>();
    private final List<String> orderByColumns = new ArrayList<>();
    private int limitValue = -1;
    private final List<String> insertColumns = new ArrayList<>();
    private final List<String> updateColumns = new ArrayList<>();
    private final List<String> customSql = new ArrayList<>();
    private final List<String> valuePlaceholders = new ArrayList<>();

    // Neue Variable für ON DUPLICATE KEY UPDATE
    private String duplicateKeyUpdateClause = "";
    private String duplicateKeyUpdateColumns = "";
    private boolean isSQLiteDatabase = false;

    public SQLQueryBuilder(String databasePrefix) {
        this.queryType = QueryType.SELECT;
        this.databasePrefix = databasePrefix;
    }

    public SQLQueryBuilder() {
        this.queryType = QueryType.SELECT;
        this.databasePrefix = "";
    }

    // Neue Setter-Methode zum Setzen des Database Prefix
    public SQLQueryBuilder setDatabasePrefix(String prefix) {
        this.databasePrefix = prefix;
        return this;
    }

    // SELECT methods
    public SQLQueryBuilder select(String... columns) {
        this.queryType = QueryType.SELECT;
        selectColumns.clear();
        selectColumns.addAll(Arrays.asList(columns));
        return this;
    }

    // Tabelle wird als roher Wert gespeichert; Präfixierung erfolgt später beim build()
    public SQLQueryBuilder from(String tableName) {
        this.tableName = tableName;
        return this;
    }

    // JOIN methods: Speicherung des rohen TableNames, keine Präfixierung hier
    public SQLQueryBuilder innerJoin(String table, String onCondition) {
        joinClauses.add("INNER JOIN " + table + " ON " + onCondition);
        return this;
    }

    public SQLQueryBuilder leftJoin(String table, String onCondition) {
        joinClauses.add("LEFT JOIN " + table + " ON " + onCondition);
        return this;
    }

    public SQLQueryBuilder rightJoin(String table, String onCondition) {
        joinClauses.add("RIGHT JOIN " + table + " ON " + onCondition);
        return this;
    }

    public SQLQueryBuilder fullJoin(String table, String onCondition) {
        joinClauses.add("FULL JOIN " + table + " ON " + onCondition);
        return this;
    }

    public SQLQueryBuilder join(String joinType, String table, String onCondition) {
        joinClauses.add(joinType + " JOIN " + table + " ON " + onCondition);
        return this;
    }

    // WHERE, GROUP BY, ORDER BY, LIMIT methods
    public SQLQueryBuilder where(String condition) {
        this.whereClause = condition;
        return this;
    }

    public SQLQueryBuilder groupBy(String... columns) {
        groupByColumns.clear();
        groupByColumns.addAll(Arrays.asList(columns));
        return this;
    }

    public SQLQueryBuilder orderBy(String... columns) {
        orderByColumns.clear();
        orderByColumns.addAll(Arrays.asList(columns));
        return this;
    }

    public SQLQueryBuilder limit(int limit) {
        this.limitValue = limit;
        return this;
    }

    // INSERT methods: Tabelle als roher Wert speichern
    public SQLQueryBuilder insertInto(String tableName) {
        this.queryType = QueryType.INSERT;
        this.tableName = tableName;
        return this;
    }

    public SQLQueryBuilder columns(String... columns) {
        insertColumns.clear();
        insertColumns.addAll(Arrays.asList(columns));
        return this;
    }

    public SQLQueryBuilder values(String... placeholders) {
        valuePlaceholders.clear();
        valuePlaceholders.addAll(Arrays.asList(placeholders));
        return this;
    }

    // Neue Setter-Methode für ON DUPLICATE KEY UPDATE
    public SQLQueryBuilder duplicateKeyUpdate(String clause, String columns, boolean isSQLiteDatabase) {
        this.duplicateKeyUpdateClause = clause;
        this.duplicateKeyUpdateColumns = columns;
        this.isSQLiteDatabase = isSQLiteDatabase;
        return this;
    }

    // UPDATE methods
    public SQLQueryBuilder update(String tableName) {
        this.queryType = QueryType.UPDATE;
        this.tableName = tableName;
        return this;
    }

    public SQLQueryBuilder set(String... columnValuePairs) {
        updateColumns.clear();
        updateColumns.addAll(Arrays.asList(columnValuePairs));
        return this;
    }

    // DELETE methods
    public SQLQueryBuilder deleteFrom(String tableName) {
        this.queryType = QueryType.DELETE;
        this.tableName = tableName;
        return this;
    }

    // CUSTOM SQL
    public SQLQueryBuilder addRawSql(String sql) {
        this.queryType = QueryType.CUSTOM;
        customSql.add(sql);
        return this;
    }

    public SQLQueryBuilder count() {
        this.queryType = QueryType.SELECT;
        selectColumns.clear();
        selectColumns.add("COUNT(*)");
        return this;
    }

    public SQLQueryBuilder countDistinct(String column) {
        this.queryType = QueryType.SELECT;
        selectColumns.clear();
        selectColumns.add("COUNT(DISTINCT " + column + ")");
        return this;
    }

    public SQLQueryBuilder sum(String column) {
        this.queryType = QueryType.SELECT;
        selectColumns.clear();
        selectColumns.add("SUM(" + column + ")");
        return this;
    }

    public SQLQueryBuilder avg(String column) {
        this.queryType = QueryType.SELECT;
        selectColumns.clear();
        selectColumns.add("AVG(" + column + ")");
        return this;
    }

    public SQLQueryBuilder max(String column) {
        this.queryType = QueryType.SELECT;
        selectColumns.clear();
        selectColumns.add("MAX(" + column + ")");
        return this;
    }

    public SQLQueryBuilder min(String column) {
        this.queryType = QueryType.SELECT;
        selectColumns.clear();
        selectColumns.add("MIN(" + column + ")");
        return this;
    }

    // Helper: Präfixiert einen Tabellennamen falls noch nicht vorhanden
    private String applyPrefix(String table) {
        if (!databasePrefix.isEmpty() && !table.startsWith(databasePrefix)) {
            return databasePrefix + table;
        }
        return table;
    }

    // BUILD methods für die verschiedenen Query-Typen
    private String buildSelectQuery() {
        StringBuilder query = new StringBuilder("SELECT ");
        query.append(selectColumns.isEmpty() ? "*" : String.join(", ", selectColumns));
        query.append(" FROM ").append(applyPrefix(tableName));
        for (String join : joinClauses) {
            query.append(" ").append(join);
        }
        if (whereClause != null && !whereClause.isEmpty()) {
            query.append(" WHERE ").append(whereClause);
        }
        if (!groupByColumns.isEmpty()) {
            query.append(" GROUP BY ").append(String.join(", ", groupByColumns));
        }
        if (!orderByColumns.isEmpty()) {
            query.append(" ORDER BY ").append(String.join(", ", orderByColumns));
        }
        if (limitValue > -1) {
            query.append(" LIMIT ").append(limitValue);
        }
        return query.toString();
    }

    public String buildInsertQuery() {
        StringBuilder query = new StringBuilder("INSERT ");
        query.append("INTO ").append(applyPrefix(tableName));
        if (!insertColumns.isEmpty()) {
            query.append(" (").append(String.join(", ", insertColumns)).append(")");
        }
        // Änderung: Wenn eine WHERE-Klausel vorliegt, benutze "FROM DUAL" statt "FROM " + Tabelle
        if (whereClause != null && !whereClause.isEmpty()) {
            String placeholders = !valuePlaceholders.isEmpty()
                    ? String.join(", ", valuePlaceholders)
                    : String.join(", ", insertColumns);
            query.append(" SELECT ").append(placeholders)
                    .append(" FROM DUAL")
                    .append(" WHERE ").append(whereClause);
        } else {
            if (!valuePlaceholders.isEmpty()) {
                query.append(" VALUES (").append(String.join(", ", valuePlaceholders)).append(")");
            } else {
                String placeholders = insertColumns.stream().map(col -> "?").collect(Collectors.joining(", "));
                query.append(" VALUES (").append(placeholders).append(")");
            }
        }
        // Anhängen der ON DUPLICATE KEY UPDATE Klausel, falls vorhanden
        if (duplicateKeyUpdateClause != null && !duplicateKeyUpdateClause.isEmpty()) {
            if (isSQLiteDatabase) {
                query.append("ON CONFLICT(").append(duplicateKeyUpdateColumns).append(") DO UPDATE SET ").append(duplicateKeyUpdateClause);
            } else {
                query.append(" ON DUPLICATE KEY UPDATE ").append(duplicateKeyUpdateClause);
            }
        }
        return query.toString();
    }

    private String buildUpdateQuery() {
        StringBuilder query = new StringBuilder("UPDATE ")
                .append(applyPrefix(tableName))
                .append(" SET ");
        if (!updateColumns.isEmpty()) {
            query.append(String.join(", ", updateColumns));
        }
        if (whereClause != null && !whereClause.isEmpty()) {
            query.append(" WHERE ").append(whereClause);
        }
        return query.toString();
    }

    private String buildDeleteQuery() {
        StringBuilder query = new StringBuilder("DELETE FROM ")
                .append(applyPrefix(tableName));
        if (whereClause != null && !whereClause.isEmpty()) {
            query.append(" WHERE ").append(whereClause);
        }
        return query.toString();
    }

    private String buildCustomQuery() {
        return String.join(" ", customSql);
    }

    // Haupterzeugungsmethode
    public String build() {
        return build(this.queryType);
    }

    public String build(QueryType type) {
        return switch (type) {
            case SELECT -> buildSelectQuery();
            case INSERT -> buildInsertQuery();
            case UPDATE -> buildUpdateQuery();
            case DELETE -> buildDeleteQuery();
            case CUSTOM -> buildCustomQuery();
            default -> throw new IllegalStateException("Unknown query type: " + type);
        };
    }

    // Neue Methode: Übergebener SQLConditionBuilder wird mit dem aktuellen Prefix synchronisiert,
    // anschließend wird dessen build()-Ergebnis als WHERE-Klausel verwendet, falls nicht bereits gesetzt.
    public String build(SQLConditionBuilder conditionBuilder) {
        if (conditionBuilder != null) {
            conditionBuilder.setDatabasePrefix(this.databasePrefix);
            // Nur überschreiben, falls noch keine explizite WHERE-Klausel gesetzt wurde
            if (this.whereClause == null || this.whereClause.isEmpty()) {
                this.whereClause = conditionBuilder.build();
            }
        }
        return build();
    }
}
