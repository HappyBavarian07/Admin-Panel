package de.happybavarian07.adminpanel.utils;/*
 * @Author HappyBavarian07
 * @Date 04.09.2022 | 12:50
 */

import com.google.gson.Gson;

public class Serialization {
    static Gson gson = new Gson();

    public Serialization() {
    }

    public static String serialize(Object object) {
        return gson.toJson(object);
    }

    public static Object deserialize(String json, Class<?> clazz) {
        if(clazz == null || json.equals("")) return null;
        return gson.fromJson(json, clazz);
    }
}
