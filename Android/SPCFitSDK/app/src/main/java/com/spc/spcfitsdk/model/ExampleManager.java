package com.spc.spcfitsdk.model;

import android.content.Context;
import android.content.Intent;

import com.spc.spcfitsdk.model.SPCFitSDK.ActivityTracker;
import com.spc.spcfitsdk.model.SPCFitSDK.ActivityTrackerCallback;
import com.spc.spcfitsdk.model.SPCFitSDK.ActivityTrackerManager;

import java.util.Calendar;
import java.util.HashMap;

public class ExampleManager {

    public static final String SHOW_COUNTDOWN = "SHOW_COUNTDOWN";
    public static final String HIDE_COUNTDOWN = "HIDE_COUNTDOWN";
    public static final String GET_TIME_RESPONSE = "GET_TIME_RESPONSE";
    public static final String SET_TIME_RESPONSE = "SET_TIME_RESPONSE";
    public static final String REAL_TIME_METER_MODE_RESPONSE = "REAL_TIME_METER_MODE_RESPONSE";
    public static final String GET_PERSONAL_INFORMATION_RESPONSE = "GET_PERSONAL_INFORMATION_RESPONSE";
    public static final String SET_PERSONAL_INFORMATION_RESPONSE = "SET_PERSONAL_INFORMATION_RESPONSE";
    public static final String GET_TARGET_STEPS_RESPONSE = "GET_TARGET_STEPS_RESPONSE";
    public static final String SET_TARGET_STEPS_RESPONSE = "SET_TARGET_STEPS_RESPONSE";
    public static final String GET_DETAIL_ACTIVITY_DATA_RESPONSE_WITHOUT_DATA = "GET_DETAIL_ACTIVITY_DATA_RESPONSE_WITHOUT_DATA";
    public static final String GET_DETAIL_ACTIVITY_DATA_RESPONSE_ACTIVITY_DATA = "GET_DETAIL_ACTIVITY_DATA_RESPONSE_ACTIVITY_DATA";
    public static final String GET_DETAIL_ACTIVITY_DATA_RESPONSE_SLEEP_QUALITY = "GET_DETAIL_ACTIVITY_DATA_RESPONSE_SLEEP_QUALITY";

    Context context;

    private static ExampleManager instance;

    public static ExampleManager getInstance(Context context) {
        if(instance == null){
            return instance = new ExampleManager(context);
        }
        return instance;
    }

    private ActivityTracker activityTracker;
    private ActivityTrackerManager activityTrackerManager;

    private ExampleManager(Context context) {
        this.context = context;

        this.activityTrackerManager = new ActivityTrackerManager(context, new ActivityTrackerCallback() {
            @Override
            public void connected(ActivityTracker tracker) {
                activityTracker = tracker;
                activityTracker.safeBondingSendPassword("000001", ActivityTracker.HIGH_PRIORITY);
            }

            @Override
            public void disconnected() {
                ExampleManager.this.context.sendBroadcast(new Intent(ActivityTrackerManager.DEVICE_DISCONNECTED));
            }

            @Override
            public void deviceToConnectNotFound() {
                ExampleManager.this.context.sendBroadcast(new Intent(ActivityTrackerManager.DEVICE_NOT_FOUND));
            }

            @Override
            public void safeBondingSavePassword() {
                Intent intent = new Intent(SHOW_COUNTDOWN);
                ExampleManager.this.context.sendBroadcast(intent);
            }

            @Override
            public void safeBondingSendPassword(boolean error) {
                if(error){
                    activityTracker.safeBondingSavePassword("000001", ActivityTracker.HIGH_PRIORITY);
                } else {
                    ExampleManager.this.context.sendBroadcast(new Intent(ActivityTrackerManager.DEVICE_CONNECTED));
                }
            }

            @Override
            public void safeBondingStatus(boolean error) {
                if(!error){
                    activityTracker.safeBondingSendPassword("000001", ActivityTracker.HIGH_PRIORITY);

                    Intent intent = new Intent(HIDE_COUNTDOWN);
                    ExampleManager.this.context.sendBroadcast(intent);
                }
            }

            @Override
            public void setTime() {
                ExampleManager.this.context.sendBroadcast(new Intent(SET_TIME_RESPONSE));
            }

            @Override
            public void getTime(Calendar calendar) {
                Intent intent = new Intent(GET_TIME_RESPONSE);
                intent.putExtra("calendar", calendar);
                ExampleManager.this.context.sendBroadcast(intent);
            }

            @Override
            public void realTimeMeterMode(int steps, int aerobicSteps, int cal, int km, int activityTime) {
                Intent intent = new Intent(REAL_TIME_METER_MODE_RESPONSE);
                intent.putExtra("steps", steps);
                intent.putExtra("aerobicSteps", aerobicSteps);
                intent.putExtra("cal", cal);
                intent.putExtra("km", km);
                intent.putExtra("activityTime", activityTime);
                ExampleManager.this.context.sendBroadcast(intent);
            }

            @Override
            public void setPersonalInformation() {
                ExampleManager.this.context.sendBroadcast(new Intent(SET_PERSONAL_INFORMATION_RESPONSE));
            }

            @Override
            public void getPersonalInformation(int male, int age, int height, int weight, int stride, String deviceId) {
                Intent intent = new Intent(GET_PERSONAL_INFORMATION_RESPONSE);
                intent.putExtra("male", male);
                intent.putExtra("age", age);
                intent.putExtra("height", height);
                intent.putExtra("weight", weight);
                intent.putExtra("stride", stride);
                ExampleManager.this.context.sendBroadcast(intent);
            }

            @Override
            public void setTargetSteps() {
                ExampleManager.this.context.sendBroadcast(new Intent(SET_TARGET_STEPS_RESPONSE));
            }

            @Override
            public void getTargetSteps(int goal) {
                Intent intent = new Intent(GET_TARGET_STEPS_RESPONSE);
                intent.putExtra("goal", goal);
                ExampleManager.this.context.sendBroadcast(intent);
            }

            @Override
            public void getDetailActivityDataActivityData(int index, Calendar calendar, int steps, int aerobicSteps, int cal, int km) {
                Intent intent = new Intent(GET_DETAIL_ACTIVITY_DATA_RESPONSE_ACTIVITY_DATA);
                intent.putExtra("calendar", calendar);
                intent.putExtra("steps", steps);
                intent.putExtra("aerobicSteps", aerobicSteps);
                intent.putExtra("cal", cal);
                intent.putExtra("km", km);
                ExampleManager.this.context.sendBroadcast(intent);
            }

            @Override
            public void getDetailActivityDataSleepQuality(int index, Calendar calendar, HashMap<Calendar, Integer> hashMap) {
                Intent intent = new Intent(GET_DETAIL_ACTIVITY_DATA_RESPONSE_SLEEP_QUALITY);
                intent.putExtra("hashMap", hashMap);
                ExampleManager.this.context.sendBroadcast(intent);
            }

            @Override
            public void getDetailActivityDataWithOutData() {
                ExampleManager.this.context.sendBroadcast(new Intent(GET_DETAIL_ACTIVITY_DATA_RESPONSE_WITHOUT_DATA));
            }
        });
    }

    public ActivityTrackerManager getActivityTrackerManager() {
        return activityTrackerManager;
    }

    public ActivityTracker getActivityTracker() {
        return activityTracker;
    }
}
