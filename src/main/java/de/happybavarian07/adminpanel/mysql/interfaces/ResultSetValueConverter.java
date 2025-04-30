package de.happybavarian07.adminpanel.mysql.interfaces;

import java.lang.reflect.Field;

/*
 * @Author HappyBavarian07
 * @Date April 15, 2025 | 15:31
 */
public interface ResultSetValueConverter {
    /**
     * Converts a given value to the expected type defined by the specified field.
     *
     * <p>This method is called by the SQL Executor when a column value is encountered that does not
     * match the standard types (e.g., UUID, Date, Boolean, Integer, Long, Double). This allows for custom
     * conversions for columns if a MySQL standard is insufficient.
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * // Assume a custom converter converts a JSON string into an object.
     * Field field = MyEntity.class.getDeclaredField("customField");
     * Object dbValue = resultSet.getObject("custom_column");
     * CustomType convertedValue = customConverter.convert(field, dbValue);
     * }</pre>
     *
     * @param field the field whose type is used as the target type for conversion
     * @param value the original value retrieved from the ResultSet
     * @param <T>   the expected target type based on the field's type
     * @return the converted value of type T or null if the input value is null
     */
    <T> T convert(Field field, Object value);
}
