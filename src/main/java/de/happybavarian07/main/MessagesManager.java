package de.happybavarian07.main;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class MessagesManager {
	
	private Main plugin;
	private FileConfiguration dataConfig = null;
	private File configFile = null;
	
	public MessagesManager(Main plugin) {
		this.plugin = plugin;
		// saves/initializes the Config
		saveDefaultConfig();
	}
	
	public void reloadConfig() {
		if(this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), "messages.yml");
		
		this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);
		
		InputStream defaultStream = this.plugin.getResource("messages.yml");
		if(defaultStream != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
			this.dataConfig.setDefaults(defaultConfig);
		}
	}
	
	public FileConfiguration getConfig() {
		if(this.dataConfig == null)
			reloadConfig();
		
		return this.dataConfig;
	}
	
	public void saveConfig() {
		if(this.dataConfig == null || this.configFile == null)
			return;
		
		try {
			this.getConfig().save(this.configFile);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not save Config to " + this.configFile, e);
		}
	}
	
	public void saveDefaultConfig() {
		if(this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), "messages.yml");
		
		if(!this.configFile.exists()) {
			this.plugin.saveResource("messages.yml", false);
		}
	}
}
