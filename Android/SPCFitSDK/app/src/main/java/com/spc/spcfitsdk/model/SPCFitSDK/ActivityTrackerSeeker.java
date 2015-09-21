package com.spc.spcfitsdk.model.SPCFitSDK;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

public class ActivityTrackerSeeker {

    private static final String CLASS = "ActivityTrackerSeeker";
    public final static int HIGH_PRIORITY = 0;
    public final static int LOW_PRIORITY = 1;

    public interface FinishCallback {
        void onFinishWithoutResult();
    }

    private BluetoothAdapter.LeScanCallback scanCallback;
    private FinishCallback finishCallback;

    Runnable runnable;

    private boolean scanning;

    private int priority;

    private Handler scanHandler;

    public ActivityTrackerSeeker() {
        scanHandler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                finishDeviceSearch();
            }
        };
    }

    static ActivityTrackerSeeker activityTrackerSeeker;

    public static ActivityTrackerSeeker newInstance(int priority) {

        if (activityTrackerSeeker == null){
            activityTrackerSeeker = new ActivityTrackerSeeker();
            activityTrackerSeeker.priority = priority;
            return activityTrackerSeeker;
        } else {
            if(activityTrackerSeeker.priority < priority) {
                return null;
            } else {
                activityTrackerSeeker.stopDeviceSearch();
                activityTrackerSeeker = new ActivityTrackerSeeker();
                activityTrackerSeeker.priority = priority;
                return activityTrackerSeeker;
            }
        }

    }

    public void startDeviceSearch(BluetoothAdapter.LeScanCallback scanCallback, FinishCallback finishCallback, int scanPeriod){
        this.scanCallback = scanCallback;
        this.finishCallback = finishCallback;
        if (!scanning) {
            scanHandler.postDelayed(runnable, scanPeriod);
            BluetoothAdapter.getDefaultAdapter().startLeScan(scanCallback);
            scanning = true;
        }
    }

    public void stopDeviceSearch() {
        if (scanning) {
            scanHandler.removeCallbacks(runnable);
            BluetoothAdapter.getDefaultAdapter().stopLeScan(scanCallback);
            scanning = false;
            this.priority = 10;
        }
    }

    public void finishDeviceSearch() {
        if (scanning) {
            scanHandler.removeCallbacks(runnable);
            BluetoothAdapter.getDefaultAdapter().stopLeScan(scanCallback);
            scanning = false;
            if(finishCallback != null){
                finishCallback.onFinishWithoutResult();
            }
            this.priority = 10;
        }
    }

}
