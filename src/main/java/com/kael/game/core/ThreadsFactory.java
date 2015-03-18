package com.kael.game.core;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @see DefaultThreadFactory 增加可辨识的线程名前缀
 */
public  class ThreadsFactory implements ThreadFactory {
    
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public ThreadsFactory(String name) {
        group = new ThreadGroup(name);
        namePrefix = name + "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                              namePrefix + threadNumber.getAndIncrement(),
                              0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
    
    public static boolean isSameGroup(Thread x, Thread y) {
        return x.getThreadGroup() == y.getThreadGroup();
    }
    
    public static boolean isSameGroup(Thread x, ThreadsFactory y) {
        return x.getThreadGroup() == y.group;
    }
    
    public static boolean isSameGroup(Thread x, TaskExecutor y) {
        return x.getThreadGroup() == y.threadsFactory.group;
    }
    
    public static boolean isSameGroup(Thread x, ActionExecutor y) {
        return x.getThreadGroup() == y.threadsFactory.group;
    }
    
}