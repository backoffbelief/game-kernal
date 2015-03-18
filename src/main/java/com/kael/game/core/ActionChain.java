package com.kael.game.core;

public class ActionChain {
    
    private ChainAction head;
    
    public void append(Action action) {
        ChainAction ca = new ChainAction(action);
        if(this.head == null) {
            this.head = ca;
        } else {
            ChainAction next = this.head;
            while(next.next != null) {
                next = next.next;
            }
            next.next = ca;
        }
    }
    
    public void checkin() {
        if(head != null) {
            head.checkin();
        }
    }
    
    private class ChainAction extends Action {
        private final Action action;
        private ChainAction next;
        public ChainAction(Action action) {
            super(action.queue);
            this.action = action;
        }
        @Override
        public void run() {
            super.run();
            
            if(next != null)
                next.checkin();
        }
        @Override
        public void exec() {
            action.exec();
        }
    }
    
}