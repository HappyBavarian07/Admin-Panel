package dte.hooksystem.hook;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * This class provides a convenient skeletal implementation for the {@link PluginHook} interface:
 * <ul>
 * 	<li>The provided implementations of <i>hashCode()</i> and <i>equals()</i> depend on the plugin's name.</li>
 * 	<li>Bukkit's Services API was simplified, and can be accessed by protected methods.</li>
 * </ul>
 * Here is an imaginary implementation for Essentials:
 * <pre>
 * public class EssentialsHook extends AbstractPluginHook {
 * 	
 * 	private AFKManager afkManager;
 * 
 * 	<i>//super the constructor with the plugin's name</i>
 * 
 * 	public EssentialsHook() {
 * 		super("Essentials"); 
 * 	}
 * 	
 * 	<i>//Add some API-accessing methods</i>
 * 
 * 	public boolean isAFK(Player player) {
 * 		return this.afkManager.isAFK(player);
 * 	}
 * 
 * 	<i>//Setup afkManager field. This runs ONLY if Essentials is present!</i>
 * 
 * 	{@code @Override}
 * 	public void init() throws Exception {
 * 		this.afkManager = EssentialsAPI.getAfkManager();
 * 	}
 * }
 * </pre>
 */
public abstract class AbstractPluginHook implements PluginHook
{
	private final String pluginName;

	public AbstractPluginHook(String pluginName)
	{
		this.pluginName = pluginName;
	}

	@Override
	public String getPluginName()
	{
		return this.pluginName;
	}
	
	protected <T> T queryProvider(Class<T> providerClass) 
	{
		RegisteredServiceProvider<T> registration = Bukkit.getServicesManager().getRegistration(providerClass);
		
		return registration == null ? null : registration.getProvider();
	}
	
	@Override
	public int hashCode()
	{
		return getPluginName().hashCode();
	}

	@Override
	public boolean equals(Object object)
	{
		if(this == object)
			return true;

		if(object == null)
			return false;

		if(getClass() != object.getClass())
			return false;

		PluginHook other = (PluginHook) object;

		return Objects.equals(this.pluginName, other.getPluginName());
	}
}