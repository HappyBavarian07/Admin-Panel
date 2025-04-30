package de.happybavarian07.adminpanel.utils.dependencyloading.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Wrapper annotation for multiple {@link Dependency} annotations.
 *
 * <p>This annotation allows you to define multiple dependencies at the class level.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>
 *     {@code
 *     @Dependencies(
 *     {@Dependency(
 *        group = "com.example",
 *        artifact = "example-library",
 *        version = "1.0.0",
 *        exclusions = {"excluded-artifact"},
 *        repository = @Repository(name = "ExampleRepo", url = "https://example.com/repo"),
 *        appendToParentClassLoader = true,
 *        optional = false,
 *        addonDependency = true,
 *        logTransistive = true
 *     )
 *     })
 *     public class ExampleClass {
 *     // Class implementation
 *     }
 *     }
 *        </pre>
 *
 * <p>This annotation is used to specify multiple dependencies for automatic dependency management.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Dependencies {
    Dependency[] value();
}
