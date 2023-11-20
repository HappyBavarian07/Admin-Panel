package de.happybavarian07.adminpanel.language;/*
 * @Author HappyBavarian07
 * @Date 25.04.2022 | 17:07
 */

import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.ChatColor;

public class Placeholder {
    private final String key;
    private final Object value;
    private final PlaceholderType type;

    public Placeholder(String key, Object value, PlaceholderType type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public PlaceholderType getType() {
        return type;
    }

    public String replace(String s) {
        if(!stringContainsPlaceholder(s)) return s;
        if(value == null) throw new NullPointerException("The Value of Key " + key + " is null");
        if(value instanceof String) {
            return s.replace(key, Utils.chat(value + ChatColor.getLastColors(s.split(key)[0])));
        }
        return s.replace(key, value.toString());
    }

    public boolean stringContainsPlaceholder(String s) {
        return s.contains(key);
    }
}
