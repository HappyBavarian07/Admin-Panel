package dte.hooksystem.missingpluginhandler.factory;

import static dte.hooksystem.utils.MessageStyle.RAW;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import dte.hooksystem.hook.PluginHook;
import dte.hooksystem.missingpluginhandler.CompositeHandler;
import dte.hooksystem.missingpluginhandler.LogToConsoleHandler;
import dte.hooksystem.missingpluginhandler.LoggerMessageHandler;
import dte.hooksystem.missingpluginhandler.MissingPluginHandler;
import dte.hooksystem.missingpluginhandler.messager.MessagerHandler;
import dte.hooksystem.utils.MessageStyle;

public class MissingHandlersFactory
{
	//Container of static factory methods
	private MissingHandlersFactory(){}

	//Cached Stateless Handlers
	public static final MissingPluginHandler DO_NOTHING = (failedHook) -> {};
	
	/*
	 * General
	 */
	public static MissingPluginHandler disablePlugin(Plugin plugin) 
	{
		return (failedHook) -> Bukkit.getPluginManager().disablePlugin(plugin);
	}
	public static MissingPluginHandler run(Consumer<PluginHook> action)
	{
		return action::accept;
	}
	public static MissingPluginHandler run(Runnable code) 
	{
		return (failedHook) -> code.run();
	}
	public static MissingPluginHandler byOrder(MissingPluginHandler... handlers)
	{
		return CompositeHandler.of(handlers);
	}
	
	
	/*
	 * Console
	 */
	public static LogToConsoleHandler logToConsole(Plugin plugin, String... messages) 
	{
		return logToConsole(plugin, MessageStyle.RAW, messages);
	}
	public static LogToConsoleHandler logErrorToConsole(Plugin plugin, String... messages) 
	{
		return logToConsole(plugin, new MessageStyle().colored(ChatColor.RED), messages);
	}
	public static LogToConsoleHandler logToConsole(Plugin plugin, MessageStyle style, String... messages) 
	{
		LogToConsoleHandler handler = new LogToConsoleHandler();
		addStyledMessages(handler, style.copy().withPluginPrefix(plugin), messages);

		return handler;
	}
	

	/*
	 * java.util.logging.Logger
	 */
	public static LoggerMessageHandler log(Logger logger, Level logLevel, String... messages) 
	{
		return log(logger, logLevel, RAW, messages);
	}
	public static LoggerMessageHandler log(Logger logger, Level logLevel, MessageStyle style, String... messages) 
	{
		LoggerMessageHandler handler = new LoggerMessageHandler(logger, logLevel);
		addStyledMessages(handler, style, messages);

		return handler;
	}
	private static void addStyledMessages(MessagerHandler handler, MessageStyle style, String... messages) 
	{
		String[] styledMessages = style.apply(Arrays.asList(messages));

		handler.addMessages(styledMessages);
	}
}