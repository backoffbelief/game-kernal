package com.kael.game.core;


public abstract class DelayAction extends Action {
    
    long execTime;
    
    DelayAction(ActionQueue queue, long execTime) {
        super(queue);
        this.execTime = execTime;
    }
    
	public DelayAction(ActionQueue queue, int delay) {
	    this(queue, System.currentTimeMillis(), delay);
	}
	
	public DelayAction(ActionQueue queue, long curTime, int delay) {
		super(queue);
		calExecTime(curTime, delay);
	}

	private void calExecTime(long curTime, int delay) {
		if(delay > 0) {
		    this.execTime = curTime + delay;
		} else {
		    this.createTime = curTime;
		    this.execTime = 0;
		}
	}

	@Override
    public void checkin() {
	    if(this.execTime == 0) {//don`t need delay
	        queue.checkin(this);
	    } else {
	        queue.checkinDelayAction(this);
	    }
    }
	
	public void recheckin(int delay) {
		this.recheckin(System.currentTimeMillis(), delay);
	}
	
	public void recheckin(long curTime, int delay) {
		calExecTime(curTime, delay);
		checkin();
	}

    public boolean tryExec(long curTime) {
		if(curTime >= execTime) {
			createTime = curTime;
			getActionQueue().checkin(this);
			return true;
		}
		return false;
	}
}