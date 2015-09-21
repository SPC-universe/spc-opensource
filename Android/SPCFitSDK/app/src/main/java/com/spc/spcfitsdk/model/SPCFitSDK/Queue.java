package com.spc.spcfitsdk.model.SPCFitSDK;


import java.util.concurrent.PriorityBlockingQueue;

public class Queue {

    private final PriorityBlockingQueue<QueueFIFOEntry<QueueAction>> queue;

    private ActivityTracker activityTracker;

    private QueueAction queueAction;
    private boolean rx;
    private boolean tx;

    public QueueAction getQueueAction(){
        return queueAction;
    }

    public Queue(ActivityTracker activityTracker) {
        queue = new PriorityBlockingQueue<>();
        this.activityTracker = activityTracker;
    }

    public void add(QueueAction queueAction) {
        queue.put(new QueueFIFOEntry<>(queueAction));
        next();
    }

    private void next() {
        if (activityTracker.isConnected()) {
            if (queue.size() != 0 && queueAction == null) {
                queueAction = queue.poll().getEntry();
                send();
            }
        }
    }

    private void stopAndNext() {
        queueAction = null;
        next();
    }

    public void rxDone(){
        rx = true;
        if (tx) {
            rx = false;
            tx = false;
            stopAndNext();
        }
    }

    public void txDone(){
        tx = true;
        if (rx) {
            rx = false;
            tx = false;
            stopAndNext();
        }
    }

    private void send() {
        activityTracker.send(queueAction);
    }
}
