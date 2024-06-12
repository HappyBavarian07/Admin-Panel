package dte.hooksystem;

import static org.bukkit.ChatColor.GREEN;

import dte.hooksystem.utils.ModernJavaPlugin;

public class HookSystem extends ModernJavaPlugin
{
	private static HookSystem INSTANCE;
	
	@Override
	public void onEnable() 
	{
		INSTANCE = this;
		
		logToConsole(GREEN + "Listening to incoming Plugins' Hooks...");
	}
	
	public static HookSystem getInstance()
	{
		return INSTANCE;
	}
}