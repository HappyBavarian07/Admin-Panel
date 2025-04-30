package de.happybavarian07.adminpanel.mysql.annotations;

import de.happybavarian07.adminpanel.mysql.interfaces.ResultSetValueConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name();

    boolean nullable() default true;

    boolean unique() default false;

    boolean autoIncrement() default false;

    boolean primaryKey() default false;

    Class<ResultSetValueConverter> converter() default ResultSetValueConverter.class;
}
