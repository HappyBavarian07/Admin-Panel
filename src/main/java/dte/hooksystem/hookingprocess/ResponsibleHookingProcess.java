package dte.hooksystem.hookingprocess;

import dte.hooksystem.exceptions.HookInitException;
import dte.hooksystem.exceptions.PluginAlreadyHookedException;
import dte.hooksystem.hook.ResponsibleHook;
import dte.hooksystem.missingpluginhandler.MissingPluginHandler;

public class ResponsibleHookingProcess extends HookingProcessDecorator<SimpleHookingProcess>
{
	private final MissingPluginHandler missingPluginHandler;
	
	public ResponsibleHookingProcess(SimpleHookingProcess simpleHookingProcess, MissingPluginHandler missingPluginHandler)
	{
		super(simpleHookingProcess);
		
		this.missingPluginHandler = missingPluginHandler;
	}
	public static ResponsibleHookingProcess decorating(SimpleHookingProcess simpleHookingProcess, ResponsibleHook responsibleHook) 
	{
		return new ResponsibleHookingProcess(simpleHookingProcess, responsibleHook.getMissingPluginHandler());
	}
	
	/**
	 * Finishing by initializing and registering the hook within the service.
	 * <p>
	 * If the plugin the hook represents is not on the server, its {@code missingPluginHandler} is executed.
	 * <p>
	 * //@param missingPluginHandler What happens if the plugin the hook represents is not on the server.
	 * @throws PluginAlreadyHookedException If the service already has a hook for the {@code hook}'s plugin.
	 * @throws HookInitException If there was a problem during the hook's {@code init()} method.
	 */
	public void finish() throws PluginAlreadyHookedException, HookInitException
	{
		this.hookingProcess.orElse(this.missingPluginHandler);
	}
}