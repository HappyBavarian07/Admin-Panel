package de.happybavarian07.adminpanel.language.mysql.utils;/*
 * @Author HappyBavarian07
 * @Date 08.05.2024 | 17:35
 */

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean daemonThread;

    public CustomThreadFactory(String namePrefix, boolean daemonThread) {
        this.namePrefix = namePrefix;
        this.daemonThread = daemonThread;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread t = new Thread(r, namePrefix + "-thread-" + threadNumber.getAndIncrement());
        t.setDaemon(daemonThread);
        t.setUncaughtExceptionHandler((t1, e) -> System.out.println("Uncaught exception in thread: " + t1.getName() + " : " + e.getMessage()));
        return t;
    }
}
