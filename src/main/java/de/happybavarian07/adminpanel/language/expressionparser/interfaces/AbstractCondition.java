package de.happybavarian07.adminpanel.language.expressionparser.interfaces;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public abstract class AbstractCondition implements Condition {
    private final String name;

    /**
     * Creates a new abstract condition.
     *
     * @param name the name of the condition
     */
    public AbstractCondition(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}