package dte.hooksystem.hook;

/**
 * Represents a functionality wrapper of a certain plugin.
 * The suggested name for implementors is: <i>pluginName</i>Hook (e.g EssentialsHook, WorldEditHook).
 */
public interface PluginHook
{
	/**
	 * Returns the name of the plugin this hook represents.
	 * 
	 * @return The hooked plugin's name.
	 */
	String getPluginName();

	/**
	 * Returns whether this hook can be used(e.g. can return false if a certain file is inaccessible).
	 * 
	 * @return Whether this hook is can be used.
	 */
	default boolean isAvailable()
	{
		return true;
	}

	/** 
	 * The only place where it's safe to(and the developer is supposed to) initialize stuff from the plugin's API(<i>e.g.</i> storing the API instance).
	 * If an Exception was thrown from this method, it would be caught and be friendly displayed in the Console.
	 * 
	 * @throws Exception if any Exception was thrown.
	 */
	default void init() throws Exception{}
	
	boolean equals(Object object);
	
	int hashCode();
}