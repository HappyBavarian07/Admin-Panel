package de.happybavarian07.webui.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class RoleManager {
    private final File roleFile;
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, Role> roleMap;
    private List<String> roleHierarchy = new ArrayList<>();

    protected RoleManager(File filePath) {
        this.roleFile = filePath;
        this.roleMap = new HashMap<>();
        if (roleFile.exists()) {
            loadRoles();
        } else {
            loadDefaultRolesFromResources();
        }
        ensureDefaultRole();
    }

    public List<String> getRoleHierarchy() {
        return roleHierarchy;
    }

    public void setRoleHierarchy(List<String> roleHierarchy) {
        this.roleHierarchy = roleHierarchy;
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, "Role hierarchy changed", LogPrefix.WEBUI_AUTH);
        saveRoles();
    }


    public Boolean hasPermission(String name, Permissions permission) {
        Role role = roleMap.get(name);
        return role != null ? role.hasPermission(permission) : null;
    }

    public int isHigherRole(String role1, String role2) {
        int index1 = roleHierarchy.indexOf(role1);
        int index2 = roleHierarchy.indexOf(role2);
        return Integer.compare(index1, index2);
    }

    private void loadRoles() {
        try {
            // The roles.json file exists, load the roles from the file
            String fileContent = new String(Files.readAllBytes(Paths.get(roleFile.getPath())));
            Map<String, Object> fileMap = mapper.readValue(fileContent, new TypeReference<>() {
            });
            roleMap = mapper.convertValue(fileMap.get("roles"), new TypeReference<Map<String, Role>>() {
            });
            roleHierarchy = mapper.convertValue(fileMap.get("roleHierarchy"), new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureDefaultRole() {
        if (!roleMap.containsKey("default")) {
            addRole("default", Map.of(Permissions.SCP_ACCESS, true)); // Add default permissions here
        }
    }

    private void loadDefaultRolesFromResources() {
        AdminPanelMain.getPlugin().saveResource("webui/config/roles.json", true);
        loadRoles();
    }

    public void saveRoles() {
        try {
            Map<String, Object> fileMap = new HashMap<>();
            fileMap.put("roles", roleMap);
            fileMap.put("roleHierarchy", roleHierarchy);
            mapper.writerWithDefaultPrettyPrinter().writeValue(roleFile, fileMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addRole(String name, Map<Permissions, Boolean> permissions) {
        roleMap.put(name, new Role(name, permissions));
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, "Role " + name + " added", LogPrefix.WEBUI_AUTH);
        saveRoles();
    }

    public void removeRole(String name) {
        roleMap.remove(name);
        AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.INFO, "Role " + name + " removed", LogPrefix.WEBUI_AUTH);
        saveRoles();
    }

    public Role getRole(String name) {
        return roleMap.get(name);
    }

    public boolean roleExists(String name) {
        return roleMap.containsKey(name);
    }
}