package dte.hooksystem.missingpluginhandler;

import org.bukkit.Bukkit;

import dte.hooksystem.missingpluginhandler.messager.MessagerHandler;

public class LogToConsoleHandler extends MessagerHandler
{
	@Override
	protected void sendMessage(String message) 
	{
		Bukkit.getConsoleSender().sendMessage(message);
	}
}