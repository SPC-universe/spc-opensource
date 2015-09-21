package com.spc.spcfitsdk.model.SPCFitSDK;

public class QueueAction implements Comparable<QueueAction> {
    private byte CMD;
    private Object[] params;
    private Integer priority;

    public QueueAction(byte CMD, int priority) {
        this.CMD = CMD;
        this.priority = priority;
        this.params = null;
    }

    public QueueAction(byte CMD, int priority, Object[] params) {
        this.CMD = CMD;
        this.priority = priority;
        this.params = params;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public byte getCMD() {
        return CMD;
    }

    public void setCMD(byte CMD) {
        this.CMD = CMD;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(QueueAction another) {
        return this.priority.compareTo(another.priority);
    }
}
