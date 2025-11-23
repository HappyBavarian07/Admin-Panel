package de.happybavarian07.adminpanel.utils.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <p>Validates files against pattern files using native format and regex rules.</p>
 * <ul>
 *   <li>Supports YAML, JSON, XML, and Properties file formats.</li>
 *   <li>Pattern files should contain regex expressions for each key or node to validate.</li>
 *   <li>Validation is performed recursively for nested structures (JSON, XML, YAML).</li>
 *   <li>Returns <code>true</code> only if all keys/nodes in the pattern file are present and match the regex in the runtime file.</li>
 * </ul>
 * <p>Example usage:</p>
 * <pre><code>
 * boolean valid = PatternFileValidator.matchesPattern(runtimeFile, patternStream);
 * </code></pre>
 */
public class PatternFileValidator {
    /**
     * <p>Validates a runtime file against a pattern file using the native format and regex rules.</p>
     * <pre><code>boolean valid = PatternFileValidator.matchesPattern(runtimeFile, patternStream);</code></pre>
     *
     * @param runtimeFile the file to validate
     * @param patternFile the pattern file as InputStream
     * @return true if the runtime file matches the pattern, false otherwise
     */
    public static boolean matchesPattern(File runtimeFile, InputStream patternFile) {
        String fileName = runtimeFile.getName();
        if (fileName.endsWith(".yml")) {
            return validateYaml(runtimeFile, patternFile);
        } else if (fileName.endsWith(".properties")) {
            return validateProperties(runtimeFile, patternFile);
        } else if (fileName.endsWith(".json")) {
            return validateJson(runtimeFile, patternFile);
        } else if (fileName.endsWith(".xml")) {
            return validateXml(runtimeFile, patternFile);
        }
        return false;
    }

    private static boolean validateProperties(File runtimeFile, InputStream patternStream) {
        try (FileInputStream runtimeStream = new FileInputStream(runtimeFile)) {
            Properties runtimeProps = new Properties();
            Properties patternProps = new Properties();
            runtimeProps.load(runtimeStream);
            patternProps.load(patternStream);
            for (String patternKey : patternProps.stringPropertyNames()) {
                String regexKey = patternKey;
                String regexValue = patternProps.getProperty(patternKey);
                boolean matched = false;
                for (String runtimeKey : runtimeProps.stringPropertyNames()) {
                    if (Pattern.matches(regexKey, runtimeKey)) {
                        String value = runtimeProps.getProperty(runtimeKey);
                        if (Pattern.matches(regexValue, value)) {
                            matched = true;
                            break;
                        }
                    }
                }
                if (!matched) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean validateYaml(File runtimeFile, InputStream patternStream) {
        try {
            FileConfiguration runtimeConfig = YamlConfiguration.loadConfiguration(runtimeFile);
            FileConfiguration patternConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(patternStream));
            Set<String> patternKeys = patternConfig.getKeys(true);
            for (String patternKey : patternKeys) {
                Object regexObj = patternConfig.get(patternKey);
                boolean matched = false;
                Set<String> runtimeKeys = runtimeConfig.getKeys(true);
                for (String runtimeKey : runtimeKeys) {
                    if (Pattern.matches(patternKey, runtimeKey)) {
                        Object valueObj = runtimeConfig.get(runtimeKey);
                        String regex = String.valueOf(regexObj);
                        String value = String.valueOf(valueObj);
                        if (Pattern.matches(regex, value)) {
                            matched = true;
                            break;
                        }
                    }
                }
                if (!matched) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean validateJson(File runtimeFile, InputStream patternStream) {
        try (FileReader runtimeReader = new FileReader(runtimeFile)) {
            JSONObject runtimeJson = new JSONObject(new JSONTokener(runtimeReader));
            JSONObject patternJson = new JSONObject(new JSONTokener(patternStream));
            return validateJsonRecursive(runtimeJson, patternJson, "");
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean validateJsonRecursive(JSONObject runtimeJson, JSONObject patternJson, String path) throws JSONException {
        Iterator<String> patternKeys = patternJson.keys();
        while (patternKeys.hasNext()) {
            String patternKey = patternKeys.next();
            Object patternValue = patternJson.get(patternKey);
            boolean matched = false;
            Iterator<String> runtimeKeys = runtimeJson.keys();
            while (runtimeKeys.hasNext()) {
                String runtimeKey = runtimeKeys.next();
                if (Pattern.matches(patternKey, runtimeKey)) {
                    Object runtimeValue = runtimeJson.get(runtimeKey);
                    if (patternValue instanceof JSONObject && runtimeValue instanceof JSONObject) {
                        if (validateJsonRecursive((JSONObject) runtimeValue, (JSONObject) patternValue, path + runtimeKey + ".")) {
                            matched = true;
                            break;
                        }
                    } else {
                        String regex = String.valueOf(patternValue);
                        String value = String.valueOf(runtimeValue);
                        if (Pattern.matches(regex, value)) {
                            matched = true;
                            break;
                        }
                    }
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private static boolean validateXml(File runtimeFile, InputStream patternStream) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document runtimeDoc = dBuilder.parse(runtimeFile);
            Document patternDoc = dBuilder.parse(patternStream);
            runtimeDoc.getDocumentElement().normalize();
            patternDoc.getDocumentElement().normalize();
            return validateXmlRecursive(runtimeDoc.getDocumentElement(), patternDoc.getDocumentElement());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean validateXmlRecursive(Node runtimeNode, Node patternNode) {
        NodeList patternChildren = patternNode.getChildNodes();
        for (int i = 0; i < patternChildren.getLength(); i++) {
            Node patternChild = patternChildren.item(i);
            if (patternChild.getNodeType() != Node.ELEMENT_NODE) continue;
            String patternName = patternChild.getNodeName();
            Node runtimeChild = null;
            NodeList runtimeChildren = runtimeNode.getChildNodes();
            for (int j = 0; j < runtimeChildren.getLength(); j++) {
                Node child = runtimeChildren.item(j);
                if (child.getNodeType() == Node.ELEMENT_NODE && Pattern.matches(patternName, child.getNodeName())) {
                    runtimeChild = child;
                    break;
                }
            }
            if (runtimeChild == null) {
                return false;
            }
            if (patternChild.hasChildNodes()) {
                if (!validateXmlRecursive(runtimeChild, patternChild)) {
                    return false;
                }
            } else {
                String regex = patternChild.getTextContent().trim();
                String value = runtimeChild.getTextContent().trim();
                if (!Pattern.matches(regex, value)) {
                    return false;
                }
            }
        }
        return true;
    }
}
