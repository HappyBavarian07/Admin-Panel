package de.happybavarian07.adminpanel.mysql.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SQLConditionBuilder {
    private String databasePrefix;
    private ConditionPart rootCondition;

    private interface ConditionPart {
        String getDatabasePrefix();

        void setDatabasePrefix(String databasePrefix);

        String build(String databasePrefix);
    }

    private static class SimpleCondition implements ConditionPart {
        private final String condition;
        private String databasePrefix;

        public SimpleCondition(String condition) {
            this.condition = condition;
        }

        @Override
        public String getDatabasePrefix() {
            return databasePrefix;
        }

        @Override
        public void setDatabasePrefix(String databasePrefix) {
            this.databasePrefix = databasePrefix;
        }

        @Override
        public String build(String databasePrefix) {
            return condition;
        }
    }

    private static class CompositeCondition implements ConditionPart {
        private final String operator;
        private final List<ConditionPart> parts = new ArrayList<>();
        private String databasePrefix;

        public CompositeCondition(String operator, ConditionPart left, ConditionPart right) {
            this.operator = operator;
            parts.add(left);
            parts.add(right);
        }

        public void add(ConditionPart part) {
            parts.add(part);
        }

        @Override
        public String getDatabasePrefix() {
            return databasePrefix;
        }

        public void setDatabasePrefix(String databasePrefix) {
            this.databasePrefix = databasePrefix;
            for (ConditionPart part : parts) {
                part.setDatabasePrefix(databasePrefix);
            }
        }

        @Override
        public String build(String databasePrefix) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.size(); i++) {
                sb.append(parts.get(i).build(databasePrefix));
                if (i < parts.size() - 1) {
                    sb.append(" ").append(operator).append(" ");
                }
            }
            return sb.toString();
        }
    }

    private static class SQLQueryCondition implements ConditionPart {
        private final SQLQueryBuilder query;
        private String databasePrefix;

        public SQLQueryCondition(SQLQueryBuilder query) {
            this.query = query;
        }

        @Override
        public String getDatabasePrefix() {
            return databasePrefix;
        }

        @Override
        public void setDatabasePrefix(String databasePrefix) {
            this.databasePrefix = databasePrefix;
            query.setDatabasePrefix(databasePrefix);
        }

        @Override
        public String build(String databasePrefix) {
            query.setDatabasePrefix(databasePrefix);
            return query.build();
        }
    }

    public SQLConditionBuilder(String databasePrefix) {
        this.databasePrefix = databasePrefix;
    }

    public SQLConditionBuilder() {
        this.databasePrefix = "";
    }

    public void setDatabasePrefix(String prefix) {
        this.databasePrefix = prefix;
        // Rekursives Updaten aller Bedingungen, falls nötig
        updateDatabasePrefix(rootCondition, prefix);
    }

    private void updateDatabasePrefix(ConditionPart part, String prefix) {
        if (part == null) return;
        part.setDatabasePrefix(prefix);
    }

    public SQLConditionBuilder where(String condition) {
        this.rootCondition = new SimpleCondition(condition);
        return this;
    }

    public SQLConditionBuilder where(SQLQueryBuilder subQueryBuilder) {
        if (subQueryBuilder == null) {
            return this;
        }
        subQueryBuilder.setDatabasePrefix(this.databasePrefix);
        this.rootCondition = new SQLQueryCondition(subQueryBuilder);
        return this;
    }

    public SQLConditionBuilder and(SQLConditionBuilder conditionBuilder) {
        if (conditionBuilder == null || conditionBuilder.rootCondition == null) {
            return this;
        }
        if (this.rootCondition == null) {
            this.rootCondition = conditionBuilder.rootCondition;
        } else {
            this.rootCondition = new CompositeCondition("AND", this.rootCondition, conditionBuilder.rootCondition);
        }
        return this;
    }

    public SQLConditionBuilder and(SQLQueryBuilder subQueryBuilder) {
        if (subQueryBuilder == null) {
            return this;
        }
        subQueryBuilder.setDatabasePrefix(this.databasePrefix);
        if (this.rootCondition == null) {
            this.rootCondition = new SQLQueryCondition(subQueryBuilder);
            return this;
        }
        this.rootCondition = new CompositeCondition("AND", this.rootCondition, new SQLQueryCondition(subQueryBuilder));
        return this;
    }

    public SQLConditionBuilder or(SQLConditionBuilder conditionBuilder) {
        if (conditionBuilder == null || conditionBuilder.rootCondition == null) {
            return this;
        }
        if (this.rootCondition == null) {
            this.rootCondition = conditionBuilder.rootCondition;
        } else {
            this.rootCondition = new CompositeCondition("OR", this.rootCondition, conditionBuilder.rootCondition);
        }
        return this;
    }

    public SQLConditionBuilder or(SQLQueryBuilder subQueryBuilder) {
        if (subQueryBuilder == null) {
            return this;
        }
        subQueryBuilder.setDatabasePrefix(this.databasePrefix);
        if (this.rootCondition == null) {
            this.rootCondition = new SQLQueryCondition(subQueryBuilder);
            return this;
        }
        this.rootCondition = new CompositeCondition("OR", this.rootCondition, new SQLQueryCondition(subQueryBuilder));
        return this;
    }

    public SQLConditionBuilder nestedCondition(SQLConditionBuilder innerBuilder) {
        if (innerBuilder == null || innerBuilder.rootCondition == null) {
            return this;
        }
        String nested = "(" + innerBuilder.build() + ")";
        return this.where(nested);
    }

    public SQLConditionBuilder whereIn(String column, SQLQueryBuilder subQueryBuilder, String databasePrefix) {
        setDatabasePrefix(databasePrefix);
        subQueryBuilder.setDatabasePrefix(this.databasePrefix);
        return this.where(column + " IN (" + subQueryBuilder.build() + ")");
    }

    public SQLConditionBuilder exists(String subQuery) {
        return this.where("EXISTS (" + subQuery + ")");
    }

    public SQLConditionBuilder exists(SQLQueryBuilder subQueryBuilder, String databasePrefix) {
        setDatabasePrefix(databasePrefix);
        subQueryBuilder.setDatabasePrefix(this.databasePrefix);
        return this.where("EXISTS (" + subQueryBuilder.build() + ")");
    }

    public SQLConditionBuilder notExists(String subQuery) {
        return this.where("NOT EXISTS (" + subQuery + ")");
    }

    public SQLConditionBuilder notExists(SQLQueryBuilder subQueryBuilder, String databasePrefix) {
        setDatabasePrefix(databasePrefix);
        subQueryBuilder.setDatabasePrefix(this.databasePrefix);
        return this.where("NOT EXISTS (" + subQueryBuilder.build() + ")");
    }

    public String build() {
        if (this.rootCondition == null) {
            return "";
        }
        return this.rootCondition.build(this.databasePrefix);
    }

    // Neue Methode: Konvertiert den Condition-Teilbaum in ein JSONObject
    private JSONObject conditionToJson(ConditionPart part) throws JSONException {
        JSONObject obj = new JSONObject();
        if (part instanceof SimpleCondition) {
            obj.put("type", "simple");
            obj.put("condition", ((SimpleCondition) part).condition);
        } else if (part instanceof CompositeCondition comp) {
            obj.put("type", "composite");
            obj.put("operator", comp.operator);
            JSONArray partsArray = new JSONArray();
            for (ConditionPart cp : comp.parts) {
                partsArray.put(conditionToJson(cp));
            }
            obj.put("parts", partsArray);
        }
        return obj;
    }

    // Implementierung der toJSON-Methode
    public String toJSON() {
        try {
            JSONObject json = new JSONObject();
            json.put("databasePrefix", this.databasePrefix);
            if (this.rootCondition != null) {
                json.put("conditions", conditionToJson(this.rootCondition));
            } else {
                json.put("conditions", JSONObject.NULL);
            }
            return json.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // Minimal implementierte fromJSON-Methode
    // Hinweis: Diese Implementierung unterstützt nur rekonstruktive SimpleCondition und CompositeCondition
    public static SQLConditionBuilder fromJSON(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            String prefix = json.optString("databasePrefix", "");
            SQLConditionBuilder builder = new SQLConditionBuilder(prefix);
            if (!json.isNull("conditions")) {
                JSONObject cond = json.getJSONObject("conditions");
                builder.rootCondition = parseCondition(cond);
            }
            return builder;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static ConditionPart parseCondition(JSONObject obj) throws JSONException {
        String type = obj.getString("type");
        if ("simple".equals(type)) {
            return new SimpleCondition(obj.getString("condition"));
        } else if ("composite".equals(type)) {
            String operator = obj.getString("operator");
            JSONArray arr = obj.getJSONArray("parts");
            if (arr.length() < 2) {
                throw new JSONException("Composite condition needs at least two parts");
            }
            // Rekursiv: Erstelle zunächst aus den ersten beiden Teilen
            CompositeCondition cp = new CompositeCondition(operator, parseCondition(arr.getJSONObject(0)), parseCondition(arr.getJSONObject(1)));
            // Falls weitere Teile vorhanden sind, füge diese hinzu
            for (int i = 2; i < arr.length(); i++) {
                cp.add(parseCondition(arr.getJSONObject(i)));
            }
            return cp;
        }
        throw new JSONException("Unbekannter condition type: " + type);
    }

    public boolean isEmpty() {
        return this.rootCondition == null;
    }

    // Überschreibt toString() um die gebaute Condition zurückzugeben
    @Override
    public String toString() {
        return build();
    }
}
