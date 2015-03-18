package com.kael.game.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);
    
    private final ActionQueue defaultQueue;
    private final ThreadPoolExecutor executor;
    private final String name;
    
    //delay load
    private DelayCheckThread delayCheckThread;
    
    final ThreadsFactory threadsFactory;
    
    /**
     * 执行task(主要是command task)队列的线程池
     * @param corePoolSize 最小线程数
     * @param maxPoolSize 最大线程数
     * @param name 名称
     */
    public ActionExecutor(int corePoolSize, int maxPoolSize, String name) {
        this.name = name == null ? "customer" : name;
        //超出corePoolSize数量之后的线程 超过5分钟空闲将被回收
        int keepAliveTime = 5;
        TimeUnit unit = TimeUnit.MINUTES;
        
        BlockingQueue<Runnable> workQueue = new LinkedTransferQueue<Runnable>();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
        
        threadsFactory = new ThreadsFactory(this.name);
        
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, threadsFactory, handler);
        defaultQueue = new ActionQueue(this);
    }
    
    public ActionQueue defaultQueue() {
        return this.defaultQueue;
    }
    
    void checkinAction(Runnable action) {
        this.defaultQueue.checkin(action);;
    }
    
    void checkinDelayAction(DelayAction action) {
        if(this.delayCheckThread == null) {
            configureDelayCheckTread();
        }
        
        this.delayCheckThread.checkin(action);
    }
    
    private synchronized void configureDelayCheckTread() {
        if(this.delayCheckThread == null) {
            this.delayCheckThread = new DelayCheckThread(name);
            this.delayCheckThread.start();
        }
    }

    public void execute(Runnable action) {
        executor.execute(action);
    }
    
    public void stop() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
        
        if(delayCheckThread != null)
            delayCheckThread.stopping();
    }
    
    static class DelayCheckThread extends Thread {

        private static final int ZIZZ_TIME = 80;//ms
        private ConcurrentLinkedDeque<DelayAction> queue;
        private boolean isRunning;

        public DelayCheckThread(String prefix) {
            super(prefix + "-thread-dc");
            queue = new ConcurrentLinkedDeque<>();
            isRunning = true;
            setPriority(Thread.MAX_PRIORITY); // 给予高优先级
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void stopping() {
            if (isRunning) {
                isRunning = false;
            }
        }

        @Override
        public void run() {
            while (isRunning) {
                try {
                    long zizzTime = ZIZZ_TIME;
                    if(!this.queue.isEmpty()) {
                        long start = System.currentTimeMillis();
                        int checked = checkActions();
                        long interval = System.currentTimeMillis() - start;
                        zizzTime -= interval;
                        if (interval > ZIZZ_TIME) {
                            logger.warn(getName() + " is spent too much time: " + interval + "ms, checked num = " + checked);
                        }
                    }
                    
                    if (zizzTime > 0) {
                        TimeUnit.MILLISECONDS.sleep(zizzTime);
                    }
                } catch (Exception e) {
                    logger.error(getName() + " Error. ", e);
                }
            }
        }

        /**
         * 返回检查完成的Action数量
         **/
        public int checkActions() {
            DelayAction last = this.queue.peekLast();
            if(last == null) {
                return 0;
            }
            
            int checked = 0;
            DelayAction current = null;
            
            while((current = this.queue.removeFirst()) != null) {
                try {
                    long begin = System.currentTimeMillis();
                    if (!current.tryExec(begin)) {
                        checkin(current);
                    }
                    checked++;
                    long end = System.currentTimeMillis();
                    if (end - begin > ZIZZ_TIME) {
                        logger.warn(current.toString() + " spent too much time. time :" + (end - begin));
                    }
                    if(current == last) {
                        break;
                    }
                } catch (Exception e) {
                    logger.error("检测action delay 异常" + current.toString(), e);
                }
            }
            return checked;
        }

        /**
         * 添加Action到队列
         * @param delayAction
         */
        public void checkin(DelayAction delayAction) {
            queue.addLast(delayAction);
        }

    }
    
}