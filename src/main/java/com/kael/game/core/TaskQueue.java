package com.kael.game.core;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskQueue {
    
    private TaskExecutor executor;
    private ConcurrentLinkedQueue<Task> queue;
    private AtomicBoolean isRunning;
    
    public TaskQueue(TaskExecutor executor) {
        this.executor = executor;
        this.queue = new ConcurrentLinkedQueue<>();
        this.isRunning = new AtomicBoolean(false);
    }
    
    void checkin(Task task) {
        this.queue.offer(task);
        
        if(this.isRunning.compareAndSet(false, true)){
           this.execNext();
        }
    }

    private void execNext() {
        Task next = this.queue.peek();
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
    
    void checkout(Runnable task) {
        this.queue.poll();
        
        this.execNext();
    }
    
    int size() {
        return queue.size();
    }
    
}