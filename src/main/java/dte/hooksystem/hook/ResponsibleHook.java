package dte.hooksystem.hook;

import dte.hooksystem.missingpluginhandler.MissingPluginHandler;

/**
 * A hook that decides what happens if the plugin it represents is not on the server.
 */
public interface ResponsibleHook extends PluginHook
{
	MissingPluginHandler getMissingPluginHandler();
}