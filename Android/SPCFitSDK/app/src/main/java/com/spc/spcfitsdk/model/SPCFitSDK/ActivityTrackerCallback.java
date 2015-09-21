package com.spc.spcfitsdk.model.SPCFitSDK;

import java.util.Calendar;
import java.util.HashMap;

public abstract class ActivityTrackerCallback {

    ///---Connection

    public void connected(ActivityTracker tracker) {}

    public void disconnected() {}

    public void deviceToConnectNotFound() {}


    ///---Bonding

    public void safeBondingSavePassword() {}

    public void safeBondingSendPassword(boolean error) {}

    public void safeBondingStatus(boolean error) {}


    ///---Time

    public void getTime(Calendar calendar) {}

    public void setTime() {}


    ///---Personal Information

    public void getPersonalInformation(int male, int age, int height, int weight, int stride, String deviceId) {}

    public void setPersonalInformation() {}


    ///---Goal

    public void getTargetSteps(int goal) {}

    public void setTargetSteps() {}


    ///---Sleep mode

    public void getSleepMonitorMode(boolean sleep) {}

    public void switchSleepMonitorMode() {}


    ///---Real time mode

    public void realTimeMeterMode(int steps, int aerobicSteps, int cal, int km, int activityTime) {}

    public void stopRealTimeMeterMode() {}


    ///---Data

    public void getCurrentActivityInformation(int steps, int aerobicSteps, int cal, int km, int activityTime) {}

    public void getTotalActivityData0(byte dayIndex, Calendar calendar, int steps, int aerobicSteps, int cal) {}
    public void getTotalActivityData1(byte dayIndex, Calendar calendar, int km, int activityTime) {}

    public void getDetailActivityDataWithOutData() {}
    public void getDetailActivityDataActivityData(int index, Calendar calendar, int steps, int aerobicSteps, int cal, int km) {}
    public void getDetailActivityDataSleepQuality(int index, Calendar calendar, HashMap<Calendar, Integer> hashMap) {}


    ///---ECG mode

    public void ECGMode(byte[] bytes) {}

    public void ECGModeRate(int heartRate) {}

    public void stopECGMode() {}

    public void deleteECGData() {}

    public void getECGData(Calendar calendar, int heartRate) {}
}
