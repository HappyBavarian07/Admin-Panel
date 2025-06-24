package de.happybavarian07.adminpanel.language.expressionparser.operations;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public class SubtractionOperation implements MathOperation {
    @Override
    public double perform(double value1, double value2) {
        return value1 - value2;
    }

    @Override
    public String getName() {
        return "Subtraction";
    }
}