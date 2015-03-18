package com.kael.game.core;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActionQueue {
    
    private ActionExecutor executor;
    private ConcurrentLinkedQueue<Runnable> queue;
    private AtomicBoolean isRunning;
    
    public ActionQueue(ActionExecutor executor) {
        this.executor = executor;
        this.queue = new ConcurrentLinkedQueue<>();
        this.isRunning = new AtomicBoolean(false);
    }
    
    public ActionQueue getActionQueue() {
        return this;
    }
    
    void checkinDelayAction(DelayAction action) {
        executor.checkinDelayAction(action);
    }
    
    void checkin(Runnable action) {
        this.queue.offer(action);
        
        if(this.isRunning.compareAndSet(false, true)){
           this.execNext();
        }
    }

    private void execNext() {
        Runnable next = this.queue.peek();
        if(next != null) {
            executor.execute(next);
        } else {
            this.isRunning.set(false);
            
            //double check
            next = this.queue.peek();
            if(next != null && this.isRunning.compareAndSet(false, true)) {
                executor.execute(next);
            }
        }
    }
    
    void checkout(Runnable action) {
        this.queue.poll();
        
        this.execNext();
    }
    
    int size() {
        return queue.size();
    }
    
}