package de.happybavarian07.adminpanel.language.mysql.controller;/*
 * @Author HappyBavarian07
 * @Date 08.05.2024 | 16:45
 */

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SerializationManager {
    public static byte[] serialize(Object obj) throws IOException {
        if (obj instanceof MemorySection) {
            // Convert MemorySection to a serializable format (e.g., Map<String, Object>)
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, Object> entry : ((MemorySection) obj).getValues(false).entrySet()) {
                if (entry.getValue() instanceof MemorySection) {
                    map.put(entry.getKey(), serialize(entry.getValue()));
                } else {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            map.put("MemorySectionName", ((MemorySection) obj).getCurrentPath());
            os.writeObject(map);
            return out.toByteArray();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz, boolean throwException) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        Object deserializedObject = is.readObject();

        if (clazz.isInstance(deserializedObject)) {
            return clazz.cast(deserializedObject);
        } else if (deserializedObject instanceof Map && clazz.equals(MemorySection.class)) {
            // Convert Map<String, Object> back to MemorySection
            Map<String, Object> map = (Map<String, Object>) deserializedObject;
            MemorySection memorySection = (MemorySection) new MemoryConfiguration().createSection((String) map.get("MemorySectionName"));
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof byte[]) {
                    ByteArrayInputStream in2 = new ByteArrayInputStream((byte[]) entry.getValue());
                    ObjectInputStream is2 = new ObjectInputStream(in);
                    Object tempDeserializedObject = is.readObject();
                    memorySection.set(map.get("MemorySectionName") + "." + entry.getKey(), tempDeserializedObject);
                } else {
                    memorySection.set(map.get("MemorySectionName") + "." + entry.getKey(), entry.getValue());
                }
            }
            return clazz.cast(memorySection);
        } else {
            if (throwException) {
                throw new ClassCastException("Deserialized object is not of the expected type");
            } else {
                return deserializedObject == null ? null : (T) deserializedObject;
            }
        }
    }

    public static <T> T deserialize(String data, Class<T> clazz, boolean throwException) {
        try {
            return deserialize(data.getBytes(), clazz, throwException);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
