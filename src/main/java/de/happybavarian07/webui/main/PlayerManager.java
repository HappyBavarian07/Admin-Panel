package de.happybavarian07.webui.main;/*
 * @Author HappyBavarian07
 * @Date 16.02.2024 | 14:50
 */

import java.util.UUID;

public class PlayerManager {
    private static PlayerManager instance;

    public PlayerManager() {
        instance = this;
    }

    public static PlayerManager getInstance() {
        return instance;
    }

    public void banPlayer(UUID uuid) {
        
    }
}
