package dte.hooksystem.utils;

import java.util.function.Consumer;

public class ObjectUtils
{
	//Container of static methods
	private ObjectUtils(){}
	
	public static <T> void ifNotNull(T object, Consumer<T> action) 
	{
		if(object != null)
			action.accept(object);
	}
}