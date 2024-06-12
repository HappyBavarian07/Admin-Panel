package dte.hooksystem.service;

import static java.util.stream.Collectors.toList;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.plugin.Plugin;

import dte.hooksystem.hook.PluginHook;
import dte.hooksystem.hook.ResponsibleHook;
import dte.hooksystem.hookingprocess.ResponsibleHookingProcess;
import dte.hooksystem.hookingprocess.SimpleHookingProcess;
import dte.hooksystem.missingpluginhandler.MissingPluginHandler;

/**
 * This class provides a common skeletal implementation for {@link HookService} that minimizes the effort of implementing it.
 * <p>
 * The method {@link HookService#register(PluginHook, MissingPluginHandler)} must be overriden to add the actual registration of the hook.
 */
public abstract class AbstractHookService implements HookService
{
	private final Plugin owningPlugin;

	protected AbstractHookService(Plugin owningPlugin) 
	{
		this.owningPlugin = owningPlugin;
	}

	@Override
	public Plugin getOwningPlugin() 
	{
		return this.owningPlugin;
	}

	@Override
	public SimpleHookingProcess register(PluginHook hook)
	{
		Objects.requireNonNull(hook);
		
		return new SimpleHookingProcess(hook, this);
	}
	
	@Override
	public ResponsibleHookingProcess register(ResponsibleHook responsibleHook)
	{
		Objects.requireNonNull(responsibleHook);
		
		SimpleHookingProcess simpleHookingProcess = register((PluginHook) responsibleHook);
		
		return ResponsibleHookingProcess.decorating(simpleHookingProcess, responsibleHook);
	}

	@Override
	public <H extends PluginHook> Optional<H> query(Class<H> hookClass) 
	{
		List<H> hooksFound = queryAll(hookClass);

		if(hooksFound.isEmpty() || hooksFound.size() > 1)
			return Optional.empty();

		return Optional.of(hooksFound.get(0)).filter(PluginHook::isAvailable);
	}

	@Override
	public <T> List<T> queryAll(Class<T> parent)
	{
		return getHooks().stream()
				.filter(hook -> parent.isAssignableFrom(hook.getClass()))
				.map(parent::cast)
				.collect(toList());
	}

	@Override
	public <T> Optional<T> safeQuery(Class<T> hookClass, Consumer<List<T>> conflictsHandler)
	{
		List<T> hooksFound = queryAll(hookClass);

		if(hooksFound.isEmpty())
			return Optional.empty();

		if(hooksFound.size() == 1)
			return Optional.of(hooksFound.get(0));

		conflictsHandler.accept(hooksFound);
		return Optional.empty();
	}

	@Override
	public <T> Optional<T> query(Class<T> hookClass, Function<List<T>, T> conflictResolver)
	{
		List<T> hooksFound = queryAll(hookClass);

		return hooksFound.isEmpty() ? Optional.empty() : Optional.of(conflictResolver.apply(hooksFound));
	}
	
	@Override
	public boolean isHooked(Plugin plugin) 
	{
		String pluginName = Objects.requireNonNull(plugin).getName();

		return getHooks().stream().anyMatch(hook -> hook.getPluginName().equals(pluginName));
	}
	
	@Override
	public int size() 
	{
		return getHooks().size();
	}

	@Override
	public Iterator<PluginHook> iterator() 
	{
		return getHooks().iterator();
	}
}
