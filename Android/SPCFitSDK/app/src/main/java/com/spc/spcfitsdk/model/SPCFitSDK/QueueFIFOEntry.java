package com.spc.spcfitsdk.model.SPCFitSDK;

import android.support.annotation.NonNull;

import java.util.concurrent.atomic.AtomicLong;

public class QueueFIFOEntry<Action extends Comparable<? super Action>> implements Comparable<QueueFIFOEntry<Action>> {
    final static AtomicLong seq = new AtomicLong();
    final long seqNum;
    final Action entry;

    public QueueFIFOEntry(Action entry) {
        seqNum = seq.getAndIncrement();
        this.entry = entry;
    }

    public Action getEntry() {
        return entry;
    }

    public int compareTo(@NonNull QueueFIFOEntry<Action> other) {
        int res = entry.compareTo(other.entry);
        if (res == 0 && other.entry != this.entry) {
            res = (seqNum < other.seqNum ? -1 : 1);
        }
        return res;
    }
}
