package de.happybavarian07.adminpanel.language.expressionparser.operations;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public interface MathOperation {
    /**
     * Performs the mathematical operation on the given values.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return the result of the operation
     */
    double perform(double value1, double value2);

    /**
     * Gets the name of the mathematical operation.
     *
     * @return the name of the operation
     */
    String getName();
    // TODO: Extend MathOperation to support parsing and evaluating full mathematical functions, e.g., allowing users to define functions like [f(x)=x^2+2] in the configuration. Implement a system to recognize and load these function definitions at startup,
    //  with an additional config section for custom persistent placeholders, user-defined functions, and math operation presets (for convienence so we dont have to type Power(5,(Add5,3)) everytime).
    // TODO: Consider adding support for the following features:
    //  - Multi-variable functions and expressions (e.g., f(x, y) = x^2 + y^2)
    //  - Conditional operations (e.g., if-else logic within expressions)
    //  - Function composition and chaining
    //  - User-defined constants and variables
    //  - Integration with external math libraries for advanced operations
    //  - Runtime evaluation and caching of parsed functions for performance
}
