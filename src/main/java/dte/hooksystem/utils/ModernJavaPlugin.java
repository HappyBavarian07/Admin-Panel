package dte.hooksystem.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ModernJavaPlugin extends JavaPlugin
{
	private final String pluginName = getDescription().getName();
	
	/**
	 * Registers the provided {@code listeners} to this plugin, using a simplified syntax.
	 * 
	 * @param listeners The listeners to register.
	 */
	public void registerListeners(Listener... listeners) 
	{
		for(Listener listener : listeners)
			Bukkit.getPluginManager().registerEvents(listener, this);
	}
	
	/**
	 * Sends a colored message to the console, without omitting the plugin's prefix.
	 * 
	 * @param message The message to send.
	 */
	public void logToConsole(String message) 
	{
		String withPrefix = String.format("[%s] %s", this.pluginName, message);

		Bukkit.getConsoleSender().sendMessage(withPrefix);
	}
}