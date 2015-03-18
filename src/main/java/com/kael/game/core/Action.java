package com.kael.game.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class Action implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(Action.class);

    protected ActionQueue queue;
    protected long createTime;

    public Action(ActionQueue queue) {
        this.queue = queue;
        createTime = System.currentTimeMillis();
    }

    public ActionQueue getActionQueue() {
        return queue;
    }
    
    public void checkin() {
        this.queue.checkin(this);
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
                logger.warn("execute action : " + this.toString() + ", interval : " + interval + ", leftTime : " + leftTime + ", size : " + queue.size());
            }
        } catch (Exception e) {
            logger.error("run action execute exception. action : " + this.toString(), e);
        } finally {
            queue.checkout(this);
        }
    }

    public abstract void exec();

}
