package de.happybavarian07.adminpanel.utils.dependencyloading;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * EzlibAppender class to append classes.<br>
 * This is from the very very cool library called Ezlib from saicone or Rubenicos.<br><br>
 * Here is the Github-Link, if you wanna check it out: <a href="https://github.com/saicone/ezlib/">https://github.com/saicone/ezlib/</a>
 *
 * @author Rubenicos | saicone and now HappyBavarian07
 */
public class ClassAppender {

    private static final Unsafe unsafe;
    private static final MethodHandles.Lookup lookup;

    static {
        Unsafe u = null;
        MethodHandles.Lookup l = null;
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            u = (Unsafe) field.get(null);

            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            l = (MethodHandles.Lookup) u.getObject(u.staticFieldBase(lookupField), u.staticFieldOffset(lookupField));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        unsafe = u;
        lookup = l;
    }

    /**
     * Append a URL into loader parent class loader.
     *
     * @param url URL to append.
     * @throws Throwable If any error occurs on reflected method invoking.
     */
    public void append(URL url) throws Throwable {
        append(url, ClassAppender.class.getClassLoader());
    }

    /**
     * Append a URL into defined class loader.
     *
     * @param url    URL to append.
     * @param loader Class loader to append.
     * @throws Throwable If any error occurs on reflected method invoking.
     */
    public void append(URL url, ClassLoader loader) throws Throwable {
        if (ServerPlatformDetector.isHybridServer()) {
            appendForHybridServer(url, loader);
        } else {
            try {
                append(url, loader, URLClassLoader.class);
            } catch (Throwable t) {
                Object ucp = getLoaderUcp(loader);
                append(url, ucp, ucp.getClass());
            }
        }
    }

    private void appendForHybridServer(URL url, ClassLoader loader) throws Throwable {
        try {
            Object ucp = getLoaderUcp(loader);
            append(url, ucp, ucp.getClass());
        } catch (Throwable t) {
            try {
                append(url, loader, URLClassLoader.class);
            } catch (Throwable t2) {
                throw new RuntimeException("Failed to append URL on hybrid server: " + ServerPlatformDetector.getPlatformName(), t2);
            }
        }
    }

    /**
     * Append a URL into defined class loader and class to find the "addURL" method.
     *
     * @param url    URL to append
     * @param loader Class loader to append.
     * @param clazz  Class to find the "addURL" method.
     * @throws Throwable If any error occurs on reflected method invoking.
     */
    public void append(URL url, Object loader, Class<?> clazz) throws Throwable {
        MethodHandle addURL = lookup.findVirtual(clazz, "addURL", MethodType.methodType(void.class, URL.class));
        append(url, loader, addURL);
    }

    /**
     * Append a URL into defined class loader and MethodHandle.
     *
     * @param url    URL to append
     * @param loader Class loader to append.
     * @param addURL Method to invoke with loader.
     * @throws Throwable If any error occurs on reflected method invoking.
     */
    public void append(URL url, Object loader, MethodHandle addURL) throws Throwable {
        addURL.invoke(loader, url);
    }

    /**
     * Get the URLClassPath from defined class loader.
     *
     * @param loader Class loader to get URLClassPath.
     * @return An object representing URLClassPath inside class loader.
     */
    public static Object getLoaderUcp(ClassLoader loader) {
        Class<?> clazz;
        Field field = null;
        try {
            // Try to get ucp field from URLClassLoader
            field = URLClassLoader.class.getDeclaredField("ucp");
        } catch (NoSuchFieldError | NoSuchFieldException e) {
            // Make a recursive look hover provided ClassLoader
            clazz = loader.getClass();
            while (field == null) {
                try {
                    field = clazz.getDeclaredField("ucp");
                } catch (NoSuchFieldError | NoSuchFieldException ex) {
                    clazz = clazz.getSuperclass();

                    if (clazz == Object.class) {
                        throw new NullPointerException("Can't find URLClassPath field from " + loader.getClass().getName() + " class");
                    }
                }
            }
        }
        return unsafe.getObject(loader, unsafe.objectFieldOffset(field));
    }
}