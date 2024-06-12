package de.happybavarian07.webui.main.handlers;/*
 * @Author HappyBavarian07
 * @Date 09.05.2024 | 13:45
 */

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WebUIUtils {

    public boolean isValidItemMap(Map<String, Object> itemMap) {
        // Check if the item map contains the required keys
        if (!itemMap.containsKey("type") || !itemMap.containsKey("amount")) {
            return false;
        }

        // Check if the type is a valid material
        try {
            ItemStack itemStack = new ItemStack(Material.valueOf((String) itemMap.get("type")));
        } catch (IllegalArgumentException e) {
            return false;
        }

        // Check if the amount is a valid integer
        try {
            Integer.parseInt((String) itemMap.get("amount"));
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public Map<String, Object> parseItemString(String itemString) {
        if (itemString == null || itemString.isEmpty()) {
            return null;
        }

        try {
            JSONObject jsonObject = new JSONObject(itemString);
            Map<String, Object> itemMap = new HashMap<>();

            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String key = it.next();
                Object value = jsonObject.get(key);
                itemMap.put(key, value);
            }

            return itemMap;
        } catch (JSONException e) {
            return null;
        }
    }

    public Map<String, String> parseData(String data) {
        Map<String, String> dataMap = new HashMap<>();

        if (data == null) return null;

        // Check if String starts with/Ends with [ or ] and remove them and even remove it when only one is present
        if (data.startsWith("[")) {
            data = data.substring(1);
        }
        if (data.endsWith("]")) {
            data = data.substring(0, data.length() - 1);
        }

        String[] pairs = data.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                dataMap.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8), URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            }
        }

        return dataMap;
    }
}
