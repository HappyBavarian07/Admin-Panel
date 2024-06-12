package dte.hooksystem.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

public class ArrayUtils
{
	//Container of static methods
	private ArrayUtils(){}
	
	@SafeVarargs
	public static <T, C extends Collection<T>> C to(Supplier<C> baseSupplier, T... array)
	{
		C collection = baseSupplier.get();

		collection.addAll(Arrays.asList(array));

		return collection;
	}
}