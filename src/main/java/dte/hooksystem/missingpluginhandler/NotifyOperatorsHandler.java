package dte.hooksystem.missingpluginhandler;

import dte.hooksystem.missingpluginhandler.messager.MessagerHandler;
import dte.hooksystem.utils.OnlineOperators;

public class NotifyOperatorsHandler extends MessagerHandler
{
	@Override
	protected void sendMessage(String message) 
	{
		OnlineOperators.get().forEach(operator -> operator.sendMessage(message));
	}
}