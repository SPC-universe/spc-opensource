package com.spc.spcfitsdk.activityTracker;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

public class ActivityTrackerManager{

    private static ActivityTrackerManager activityTrackerManager;

    public static final String CLASS = "Activity Tracker Manager";

    private ActivityTracker activityTracker;

    private ActivityTrackerManager()
    {
    }

    private Context context;

    public static ActivityTrackerManager getInstance(Context context){
        if (activityTrackerManager == null)
        {
            activityTrackerManager = new ActivityTrackerManager(context);
        }
        return activityTrackerManager;
    }

    private ActivityTrackerManager(Context context){
        this.context=context;

        activityTrackerManager=new ActivityTrackerManager();

        Intent intent = new Intent(this.context, ActivityTracker.class);
        this.context.bindService(intent, activityTrackerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection activityTrackerServiceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service)
        {
            activityTracker = ((ActivityTracker.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            activityTracker = null;
        }

    };

    public void findDevices()
    {
        activityTracker.findDevices();
    }

    public void stopFindingDevices()
    {
        activityTracker.stopFindingDevices();
    }

    public void connectToDevice(BluetoothDevice device)
    {
        if(activityTracker.connectToDeviceGatt(device.getAddress())){
            Log.d(CLASS,"Connected");
        }else{
            Log.d(CLASS,"Error Connecting");
        }
    }

    public void setTime(Calendar calendar)
    {
        activityTracker.setTime(calendar);
    }

    public void getTime()
    {
        activityTracker.getTime();
    }

    public void startRealTimeMeterMode()
    {
        activityTracker.startRealTimeMeterMode();
    }

    public void getPersonalInformation()
    {
        activityTracker.getPersonalInformation();
    }

    public void setPersonalInformation(byte male, byte age, byte height, byte weight,byte stride)
    {
        activityTracker.setPersonalInformation(male, age, height, weight, stride);
    }

    public void getDetailActivityData(byte day)
    {
        activityTracker.getDetailActivityData(day);
    }

    public void getTargetSteps()
    {
        activityTracker.getTargetSteps();
    }

    public void setTargetSteps(int steps)
    {
        activityTracker.setTargetSteps(steps);
    }

    public void disconnect(){
        activityTracker.disconnectFromDeviceGatt();
    }

}
