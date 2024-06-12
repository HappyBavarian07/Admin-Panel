package dte.hooksystem.utils;

import static dte.hooksystem.utils.ObjectUtils.ifNotNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Streams;

public class MessageStyle
{
	private String prefix, suffix;
	private final List<UnaryOperator<String>> finalTouches = new ArrayList<>();
	
	//this instance always returns the given message
	public static final MessageStyle RAW = new MessageStyle();
	
	public MessageStyle prefixedWith(String prefix) 
	{
		this.prefix = prefix;
		return this;
	}
	public MessageStyle suffixedWith(String suffix) 
	{
		this.suffix = suffix;
		return this;
	}
	public MessageStyle withPluginPrefix(Plugin plugin) 
	{
		return prefixedWith(String.format("[%s] ", plugin.getName()));
	}
	public MessageStyle colored(ChatColor color) 
	{
		return withFinalTouch(message -> color + message);
	}
	public MessageStyle withFinalTouch(UnaryOperator<String> finalTouch) 
	{
		this.finalTouches.add(finalTouch);
		return this;
	}
	public String apply(String message) 
	{
		if(this == RAW)
			return message;

		String finalMessage = buildFinalMessage(message);

		return applyFinalTouches(finalMessage);
	}
	public String[] apply(Iterable<String> rawMessages)
	{
		return Streams.stream(rawMessages)
				.map(this::apply)
				.toArray(String[]::new);
	}
	public MessageStyle copy() 
	{
		MessageStyle style = new MessageStyle();
		
		ifNotNull(this.prefix, style::prefixedWith);
		ifNotNull(this.suffix, style::suffixedWith);
		this.finalTouches.forEach(style::withFinalTouch);
		
		return style;
	}
	
	private String buildFinalMessage(String rawMessage) 
	{
		String prefix = defaultIfNull(this.prefix, "");
		String suffix = defaultIfNull(this.suffix, "");

		return String.format("%s%s%s", prefix, rawMessage, suffix);
	}
	private String applyFinalTouches(String text) 
	{
		for(UnaryOperator<String> touch : this.finalTouches) 
			text = touch.apply(text);

		return text;
	}
}