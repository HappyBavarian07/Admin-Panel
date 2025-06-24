package de.happybavarian07.adminpanel.language.expressionparser.operations;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public class DivisionOperation implements MathOperation {
    @Override
    public double perform(double value1, double value2) {
        if (value2 == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return value1 / value2;
    }

    @Override
    public String getName() {
        return "Division";
    }
}