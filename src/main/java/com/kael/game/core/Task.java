package com.kael.game.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class Task implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(Task.class);

    private TaskQueue queue;
    protected long createTime;

    public Task(TaskQueue queue) {
        this.queue = queue;
        createTime = System.currentTimeMillis();
    }

    public TaskQueue getTaskQueue() {
        return queue;
    }

    public void checkin() {
        queue.checkin(this);
    }
    
    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            exec();
            long end = System.currentTimeMillis();
            long interval = end - start;
            long leftTime = end - createTime;
            if (interval >= 1000) {
                logger.warn("execute task : " + this.toString() + ", interval : " + interval + ", leftTime : " + leftTime + ", size : " + queue.size());
            }
        } catch (Exception e) {
            logger.error("run task execute exception. task : " + this.toString(), e);
        } finally {
            queue.checkout(this);
        }
    }

    protected abstract void exec();
    
}
