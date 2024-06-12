package de.happybavarian07.webui.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;

public class UserManager {
    private final File userFile;
    private final File tokenFile;
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<UUID, String> userTokens = new HashMap<>();
    private Map<UUID, User> userMap = new HashMap<>();
    private final JwtUtil jwtUtil;


    protected UserManager(File filePath, File tokenFile, JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.userFile = filePath;
        if (userFile.exists()) {
            loadUsers();
        } else {
            loadDefaultUsersFromResources();
        }
        this.tokenFile = tokenFile;
        if (tokenFile.exists()) {
            loadTokens();
        } else {
            loadDefaultTokensFromResources();
        }
    }

    private void loadUsers() {
        try {
            String fileContent = new String(Files.readAllBytes(Paths.get(userFile.getPath())));
            TypeReference<HashMap<UUID, User>> typeRef = new TypeReference<>() {
            };
            userMap = mapper.readValue(fileContent, typeRef);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDefaultUsersFromResources() {
        AdminPanelMain.getPlugin().saveResource("webui/config/users.json", true);
        loadUsers();
    }

    private void loadDefaultTokensFromResources() {
        AdminPanelMain.getPlugin().saveResource("webui/config/tokens.json", true);
        loadTokens();
    }

    public void saveUsers() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(userFile, userMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addUser(UUID uuid, String username, String password, String salt, List<String> roles) {
        if (userExists(uuid)) return;
        if (userNameExists(username)) return;
        User user = new User(uuid, username, hashPassword(password, salt), roles);
        user.setSalt(salt);
        userMap.put(uuid, user);
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, String.format("User %s (UUID: %s) was added.", user.getUsername(), user.getUUID()), LogPrefix.WEBUI_AUTH);
        saveUsers();
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean userNameExists(String username) {
        for (Map.Entry<UUID, User> entry : userMap.entrySet()) {
            if (entry.getValue().getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public void removeUser(UUID uuid) {
        User user = userMap.remove(uuid);
        if(user != null) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, String.format("User %s (UUID: %s) was removed.", user.getUsername(), user.getUUID()), LogPrefix.WEBUI_AUTH);
        }
        saveUsers();
    }

    public User getUser(UUID uuid) {
        return userMap.get(uuid);
    }

    public boolean userExists(UUID uuid) {
        return userMap.containsKey(uuid);
    }

    public boolean isValidUser(UUID uuid, String password, boolean hashedPassword) {
        User user = userMap.get(uuid);
        if (!hashedPassword) {
            return user != null && user.getHashedPassword().equals(hashPassword(password, user.getSalt()));
        } else {
            return user != null && user.getHashedPassword().equals(password);
        }
    }

    public boolean isValidUser(String username, String password, boolean hashedPassword) {
        for (Map.Entry<UUID, User> entry : userMap.entrySet()) {
            if (entry.getValue().getUsername().equalsIgnoreCase(username)) {
                if (!hashedPassword) {
                    return entry.getValue().getHashedPassword().equals(hashPassword(password, entry.getValue().getSalt()));
                } else {
                    return entry.getValue().getHashedPassword().equals(password);
                }
            }
        }
        return false;
    }

    public List<String> getUserRoles(UUID uuid) {
        User user = userMap.get(uuid);
        return user != null ? user.getRoles() : null;
    }

    public void setUserRoles(UUID uuid, List<String> roles) {
        User user = userMap.get(uuid);
        if (user != null) {
            user.setRoles(roles);
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, String.format("User %s (UUID: %s) roles were changed to %s.", user.getUsername(), user.getUUID(), roles.toString()), LogPrefix.WEBUI_AUTH);
            saveUsers();
        }
    }

    public void addUserRole(UUID uuid, String role) {
        User user = userMap.get(uuid);
        if (user != null) {
            user.addRole(role);
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, String.format("User %s (UUID: %s) role %s was added.", user.getUsername(), user.getUUID(), role), LogPrefix.WEBUI_AUTH);
            saveUsers();
        }
    }

    public void removeUserRole(UUID uuid, String role) {
        User user = userMap.get(uuid);
        if (user != null) {
            user.removeRole(role);
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, String.format("User %s (UUID: %s) role %s was removed.", user.getUsername(), user.getUUID(), role), LogPrefix.WEBUI_AUTH);
            saveUsers();
        }
    }

    public boolean userHasRole(UUID uuid, String role) {
        User user = userMap.get(uuid);
        return user != null && user.getRoles().contains(role);
    }

    public UUID getUserUUID(String username) {
        for (Map.Entry<UUID, User> entry : userMap.entrySet()) {
            if (entry.getValue().getUsername().equalsIgnoreCase(username)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getUserToken(UUID userId) {
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, String.format("User %s (UUID: %s) requested a token.", getUser(userId).getUsername(), userId), LogPrefix.WEBUI_AUTH);
        // Check if the stored token is expired
        if (jwtUtil.isTokenExpired(userTokens.get(userId))) {
            // If the token is expired, remove it
            removeUserToken(userId);
            return null;
        }

        return userTokens.get(userId);
    }

    public void addUserToken(UUID userId, String token) {

        // Check if the stored token is expired
        if (jwtUtil.isTokenExpired(token)) {
            return;
        }

        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, String.format("User %s (UUID: %s) added a token.", getUser(userId).getUsername(), userId), LogPrefix.WEBUI_AUTH);

        userTokens.put(userId, token);
        saveTokens();
    }

    public void removeUserToken(UUID userId) {
        userTokens.remove(userId);
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, String.format("User %s (UUID: %s) removed a token.", getUser(userId).getUsername(), userId), LogPrefix.WEBUI_AUTH);
        saveTokens();
    }

    public boolean userHasToken(UUID userId, String token) {
        if (!userTokens.containsKey(userId)) return false;
        if (userTokens.get(userId) == null) return false;

        // Check if the stored token is expired
        if (jwtUtil.isTokenExpired(token)) {
            // If the token is expired, remove it
            removeUserToken(userId);
            return false;
        }

        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, String.format("User %s (UUID: %s) checked a token.", getUser(userId).getUsername(), userId), LogPrefix.WEBUI_AUTH);

        //System.out.println("Token: " + token);
        //System.out.println("UserToken: " + userTokens.get(userId));
        //System.out.println("Equals: " + userTokens.get(userId).equals(token));
        //System.out.println("Tokens: " + userTokens.toString());
        return userTokens.get(userId).equals(token);
    }

    private void loadTokens() {
        //System.out.println("Loading tokens");
        try {
            String fileContent = new String(Files.readAllBytes(Paths.get(tokenFile.getPath())));
            TypeReference<HashMap<UUID, String>> typeRef = new TypeReference<>() {
            };
            userTokens = mapper.readValue(fileContent, typeRef);
            //System.out.println("Loaded tokens");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveTokens() {
        try {
            //System.out.println("Saving tokens");
            mapper.writerWithDefaultPrettyPrinter().writeValue(tokenFile, userTokens);
            //System.out.println("Saved tokens");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}