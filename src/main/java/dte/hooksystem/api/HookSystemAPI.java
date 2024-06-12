package dte.hooksystem.api;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.Plugin;

import dte.hooksystem.service.HookService;
import dte.hooksystem.service.MapHookService;

public class HookSystemAPI 
{
	//Container of API methods
	private HookSystemAPI(){}
	
	private static final Map<Plugin, HookService> PLUGINS_SERVICES = new HashMap<>();
	
	/**
	 * Returns the responsible service for managing the hooks of the provided {@code plugin}.
	 * 
	 * @param owningPlugin The plugin which the returned service serves.
	 * @return The hooks manager of the provided {@code plugin}.
	 */
	public static HookService getService(Plugin owningPlugin) 
	{
		return PLUGINS_SERVICES.computeIfAbsent(owningPlugin, MapHookService::new);
	}
}