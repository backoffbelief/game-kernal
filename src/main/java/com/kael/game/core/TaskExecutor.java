package com.kael.game.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
public class TaskExecutor {

    private final TaskQueue defaultQueue;
    private final ThreadPoolExecutor executor;
    
    final ThreadsFactory threadsFactory;
    
    /**
     * 执行task(主要是command task)队列的线程池
     * @param corePoolSize 最小线程数
     * @param maxPoolSize 最大线程数
     * @param prefix 线程名前缀
     */
    public TaskExecutor(int corePoolSize, int maxPoolSize, String prefix) {
        if(prefix == null) prefix = "customer";
        //超出corePoolSize数量之后的线程 超过5分钟空闲将被回收
        int keepAliveTime = 5;
        TimeUnit unit = TimeUnit.MINUTES;
        
        BlockingQueue<Runnable> workQueue = new LinkedTransferQueue<Runnable>();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
        threadsFactory = new ThreadsFactory(prefix);
        
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, threadsFactory, handler);
        defaultQueue = new TaskQueue(this);
    }
    
    public TaskQueue defaultQueue() {
        return this.defaultQueue;
    }
    
    public void execute(Runnable task) {
        executor.execute(task);
    }
    
    public void stop() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }
    
}