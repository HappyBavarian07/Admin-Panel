package de.happybavarian07.webui.utils;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class UserAuthUtils {
    private final UserManager userManager;
    private final RoleManager roleManager;


    public UserAuthUtils(File userFilePath, File roleFilePath, File tokenFile, JwtUtil jwtUtil) {
        this.userManager = new UserManager(userFilePath, tokenFile, jwtUtil);
        this.roleManager = new RoleManager(roleFilePath);
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void addUser(UUID uuid, String username, String password, List<String> roles) {
        userManager.addUser(uuid, username, password, generateSalt(), roles);
    }

    public String hashPassword(String password, String salt) {
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

    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public void removeUser(UUID uuid) {
        userManager.removeUser(uuid);
    }

    public User getUser(UUID uuid) {
        return userManager.getUser(uuid);
    }

    public UUID getUserUUID(String username) {
        return userManager.getUserUUID(username);
    }

    public boolean isValidUser(UUID uuid, String password, boolean hashedPassword) {
        return userManager.isValidUser(uuid, password, hashedPassword);
    }

    public boolean isValidUser(String username, String password, boolean hashedPassword) {
        return userManager.isValidUser(username, password, hashedPassword);
    }

    public List<String> getUserRoles(UUID uuid) {
        return userManager.getUserRoles(uuid);
    }

    public void setUserRoles(UUID uuid, List<String> roles) {
        userManager.setUserRoles(uuid, roles);
    }

    public void addUserRole(UUID uuid, String role) {
        userManager.addUserRole(uuid, role);
    }

    public void removeUserRole(UUID uuid, String role) {
        userManager.removeUserRole(uuid, role);
    }

    public boolean userHasRole(UUID uuid, String role) {
        return userManager.userHasRole(uuid, role);
    }

    public boolean userHasPermission(UUID uuid, Permissions permission) {
        List<String> roles = userManager.getUserRoles(uuid);
        if (roles != null) {
            // Sort the roles of the user according to the role hierarchy
            roles.sort(roleManager::isHigherRole);

            // Check the permissions of each role
            for (String role : roles) {
                Boolean hasPermission = roleManager.hasPermission(role, permission);
                if (hasPermission != null) {
                    return hasPermission;
                }
            }
        }
        return false;
    }

    public boolean isValidUserAuthToken(UUID userId, String authToken) {
        return userManager.userHasToken(userId, authToken);
    }
}