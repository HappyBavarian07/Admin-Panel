package dte.hooksystem.exceptions;

import org.bukkit.plugin.Plugin;

import dte.hooksystem.hook.PluginHook;

/**
 * Thrown when a developer attempts to register another hook for the same plugin.
 */
public class PluginAlreadyHookedException extends HookingException
{
	private final Plugin plugin;
	
	private static final long serialVersionUID = -8548600593534415155L;
	
	public PluginAlreadyHookedException(PluginHook hook, Plugin plugin)
	{
		super(hook, "Tried to register a hook for %plugin% but one already exists!");
		
		this.plugin = plugin;
	}
	public Plugin getPlugin() 
	{
		return this.plugin;
	}
}