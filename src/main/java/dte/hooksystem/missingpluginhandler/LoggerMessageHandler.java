package dte.hooksystem.missingpluginhandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import dte.hooksystem.missingpluginhandler.messager.MessagerHandler;

public class LoggerMessageHandler extends MessagerHandler
{
	private final Logger logger;
	private final Level logLevel;
	
	public LoggerMessageHandler(Logger logger, Level logLevel)
	{
		this.logger = logger;
		this.logLevel = logLevel;
	}
	
	@Override
	protected void sendMessage(String message) 
	{
		this.logger.log(this.logLevel, message);
	}
}