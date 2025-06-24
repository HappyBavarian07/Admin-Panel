package de.happybavarian07.adminpanel.language.expressionparser.interfaces;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public interface Condition {
    /**
     * Checks if the condition is met.
     *
     * @return true if the condition is met, false otherwise
     */
    boolean isTrue();

    /**
     * Gets the name of the condition.
     *
     * @return the name of the condition
     */
    String getName();
}
