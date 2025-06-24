package de.happybavarian07.adminpanel.language.expressionparser;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FunctionExample {
    public static void main(String[] args) {
        // Create a new ExpressionParser
        ExpressionParser parser = new ExpressionParser();

        // Get the function manager to register custom functions
        LanguageFunctionManager functionManager = parser.getFunctionManager();

        // Example 1: Simple math function
        String expression1 = "add(5, 3) * 2";
        parser.getFunctionManager().registerFunction("add", (interpreter, functionArgs, callType) -> {
            if (functionArgs.size() != 2) throw new RuntimeException("add() expects 2 arguments");
            if (!(functionArgs.get(0) instanceof Number) || !(functionArgs.get(1) instanceof Number)) {
                throw new RuntimeException("Both arguments to add() must be numbers");
            }
            return ((Number) functionArgs.get(0)).doubleValue() + ((Number) functionArgs.get(1)).doubleValue();
        });

        // Example 2: Function with player context
        // This would be used in a plugin where you have access to a Player object
        Player player = Bukkit.getPlayer("PlayerName");
        if (player != null) {
            // Set player variable for the expression
            parser.setVariable("player", player);

            // Example expression that checks if player has a permission and is holding a specific item
            String expression2 = "hasPermission(player, 'myplugin.special') && hasItem(player, 'DIAMOND_SWORD', 1)";

            try {
                Object result = parser.parse(expression2, Material.BARRIER);
                System.out.println("Result: " + result);
            } catch (Exception e) {
                System.err.println("Error evaluating expression: " + e.getMessage());
            }
        }

        // Example 3: Using the random function
        String expression3 = "random(1, 10) > 5 ? 'DIAMOND' : 'STONE'";
        try {
            Object result = parser.parse(expression3, Material.BARRIER);
            System.out.println("Random result: " + result);
        } catch (Exception e) {
            System.err.println("Error evaluating random expression: " + e.getMessage());
        }
    }
}
