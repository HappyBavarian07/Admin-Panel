package dte.hooksystem.hookingprocess;

/** 
 * A builder-style process of registering a {@code PluginHook} to a {@code HookService}.
 * <p>
 * The termination method(where the hook is registered) is determined by the implementors.
 */
public interface HookingProcess<HP extends HookingProcess<?>>
{
	
}