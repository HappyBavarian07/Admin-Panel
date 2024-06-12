package dte.hooksystem.hookingprocess;

public abstract class HookingProcessDecorator<HP extends HookingProcess<?>> implements HookingProcess<HP>
{
	protected final HP hookingProcess;
	
	protected HookingProcessDecorator(HP hookingProcess)
	{
		this.hookingProcess = hookingProcess;
	}
}