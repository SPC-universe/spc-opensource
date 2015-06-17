package com.spc.spcfitsdk.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.spc.spcfitsdk.ActivityTrackerManager;

public class SPCFit extends CordovaPlugin {

    HashMap<String, Object> callbacks;

    String realTimeFunctionName;

    ActivityTrackerManager activityTrackerManager;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        activityTrackerManager = new ActivityTrackerManager(
                this.cordova.getActivity().getApplicationContext(),
                new ActivityTrackerManager.ActivityTrackerManagerCallback() {

                    @Override
                    public void findDevicesCallback(HashMap<String, BluetoothDevice> map){
                        JSONArray array = new JSONArray();
                        Iterator<String> keySetIterator = map.keySet().iterator();

                        while(keySetIterator.hasNext()){
                            String key = keySetIterator.next();

                            array.put(key);
                        }
                        ((CallbackContext) callbacks.get("findDevices")).success(array);
                    }

                    @Override
                    public void connectedCallback(boolean connected){
                        if(connected){
                            ((CallbackContext) callbacks.get("connect")).success();
                        } else {
                            ((CallbackContext) callbacks.get("connect")).error("Disconnected");
                        }
                    }

                    @Override
                    public void getTimeCallback(Calendar calendar){
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        ((CallbackContext) callbacks.get("getTime")).success(sdf.format(calendar.getTime()));
                    }

                    @Override
                    public void setTimeCallback(){
                        ((CallbackContext) callbacks.get("setTime")).success("Hora guardada");
                    }

                    @Override
                    public void getPersonalInformationCallback(boolean male, int age, int height, int weight, int stepLength, String deviceId){
                        try{
                            JSONObject object = new JSONObject();
                            object.put("stepLength", stepLength);
                            object.put("weight", weight);
                            object.put("height", height);
                            object.put("age", age);
                            object.put("man", male);
                            ((CallbackContext) callbacks.get("getPersonalInformation")).success(object);

                        } catch(Exception e){
                            ((CallbackContext) callbacks.get("getPersonalInformation")).error(e.getMessage());
                        }

                    }

                    @Override
                    public void setPersonalInformationCallback(){
                        ((CallbackContext) callbacks.get("setPersonalInformation")).success("Informacion personal guardada");
                    }

                    @Override
                    public void getTargetStepsCallback(int goal){
                        ((CallbackContext) callbacks.get("getTargetSteps")).success(goal);
                    }

                    @Override
                    public void setTargetStepsCallback(){
                        ((CallbackContext) callbacks.get("setTargetSteps")).success("Objetivo guardado");
                    }

                    @Override
                    public void getCurrentActivityInformationCallback (int steps, int aerobicSteps, int cal, int km, int activityTime){
                        try{
                            JSONObject object = new JSONObject();
                            object.put("steps", steps);
                            object.put("aerobicSteps", aerobicSteps);
                            object.put("calories", cal);
                            object.put("distance", km);
                            object.put("activityTime", activityTime);
                            ((CallbackContext) callbacks.get("getCurrentActivityInformation")).success(object);
                        } catch(Exception e){
                            ((CallbackContext) callbacks.get("getCurrentActivityInformation")).success(e.getMessage());
                        }

                    }

                    @Override
                    public void getTotalActivityData(byte dayIndex, Calendar calendar, int steps, int aerobicSteps, int cal, int km, int activityTime){
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        try{
                            JSONObject totalActivity = new JSONObject();
                            totalActivity.put("date", sdf.format(calendar.getTime()));
                            totalActivity.put("steps", steps);
                            totalActivity.put("aerobicSteps", aerobicSteps);
                            totalActivity.put("calories", cal);
                            totalActivity.put("distance", km);
                            totalActivity.put("activityTime", activityTime);
                            ((CallbackContext) callbacks.get("getTotalActivityData")).success(totalActivity);
                        } catch(Exception e){
                            ((CallbackContext) callbacks.get("getTotalActivityData")).success(e.getMessage());
                        }
                    }

                    @Override
                    public void getDetailActivityData(Calendar calendar,JSONArray activityDetailCollection, JSONArray sleepQualityDetailCollection){
                        try{
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            JSONObject detailActivity = new JSONObject();
                            detailActivity.put("date", sdf.format(calendar.getTime()));
                            detailActivity.put("activityDetail", activityDetailCollection);
                            detailActivity.put("sleepQualityDetail", sleepQualityDetailCollection);
                            ((CallbackContext) callbacks.get("getDetailActivityData")).success(detailActivity);
                        } catch(Exception e){
                            ((CallbackContext) callbacks.get("getDetailActivityData")).success(e.getMessage());
                        }
                    }

                    @Override
                    public void realTimeMeterModeCallback(int steps, int aerobicSteps, int cal, int km, int activityTime){
                        if(realTimeFunctionName != null){
                            if (steps !=0 || cal != 0 || km !=0){
                                final String script ="javascript:" + realTimeFunctionName + "( { steps:" + steps + ", aerobicSteps:" + aerobicSteps + ", cal:" + cal + ", km:" + km + ", activityTime:" + activityTime + " } );";

                                Handler mainHandler = new Handler(Looper.getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        SPCFit.this.webView.loadUrl(script);
                                    }
                                });
                            }
                        }
                    }
                }
        );
        callbacks = new HashMap<String, Object>();
    }

    public void keepCallbackAlive(CallbackContext callbackContext){
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        callbacks.put(action, callbackContext);
        keepCallbackAlive(callbackContext);

        if (action.equals("findDevices")) {
            return findDevices(data);
        } else if (action.equals("connect")) {
            return connect(data);
        } else if (action.equals("setTime")) {
            return setTime(data);
        } else if (action.equals("getTime")) {
            return getTime(data);
        } else if (action.equals("setPersonalInformation")) {
            return setPersonalInformation(data);
        } else if (action.equals("getPersonalInformation")) {
            return getPersonalInformation(data);
        } else if (action.equals("setTargetSteps")) {
            return setTargetSteps(data);
        } else if (action.equals("getTargetSteps")) {
            return getTargetSteps(data);
        } else if (action.equals("getCurrentActivityInformation")) {
            return getCurrentActivityInformation(data);
        } else if (action.equals("getTotalActivityData")) {
            return getTotalActivityData(data);
        } else if (action.equals("getDetailActivityData")) {
            return getDetailActivityData(data);
        } else if (action.equals("startRealTimeMeterMode")) {
            return startRealTimeMeterMode(data);
        } else if (action.equals("stopRealTimeMeterMode")) {
            return stopRealTimeMeterMode(data);
        } else {
            return false;
        }
    }

    private boolean findDevices(JSONArray data) throws JSONException {

        activityTrackerManager.fillDeviceList();

        return true;
    }

    private boolean connect(JSONArray data) throws JSONException {

        activityTrackerManager.connectToDeviceAddress(data.getString(0));

        return true;
    }

    private boolean setTime(JSONArray data) throws JSONException {
        try {

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(data.getString(0));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            activityTrackerManager.setTime(calendar);

            return true;
        } catch (Exception e) {
            ((CallbackContext) callbacks.get("setTime")).error("Error, La fecha a de ir con este formato 'yyyy-MM-dd HH:mm:ss'");
            return false;
        }
    }

    private boolean getTime(JSONArray data) throws JSONException {

        activityTrackerManager.getTime();

        return true;
    }

    private boolean setPersonalInformation(JSONArray data) throws JSONException {
        try {
            JSONObject object = data.getJSONObject(0);
            boolean male = object.getBoolean("man");
            int age = object.getInt("age");
            int height = object.getInt("height");
            int weight = object.getInt("weight");
            int stepLength = object.getInt("stepLength");

            activityTrackerManager.setPersonalInformation( male, age, height, weight, stepLength);
            return true;
        } catch (Exception e) {
            ((CallbackContext) callbacks.get("setPersonalInformation")).error("Error, "+ e.getMessage());
            return false;
        }
    }

    private boolean getPersonalInformation(JSONArray data) throws JSONException {

        activityTrackerManager.getPersonalInformation();

        return true;
    }

    private boolean setTargetSteps(JSONArray data) throws JSONException {

        activityTrackerManager.setTargetSteps(data.getInt(0));

        return true;
    }

    private boolean getTargetSteps(JSONArray data) throws JSONException {

        activityTrackerManager.getTargetSteps();

        return true;
    }

    private boolean getCurrentActivityInformation(JSONArray data) throws JSONException {

        activityTrackerManager.getCurrentActivityInformation();

        return true;
    }

    private boolean getTotalActivityData(JSONArray data) throws JSONException {

        activityTrackerManager.getTotalActivityData(data.getInt(0));

        return true;
    }

    private boolean getDetailActivityData(JSONArray data) throws JSONException {

        activityTrackerManager.getDetailActivityData(data.getInt(0));

        return true;
    }

    private boolean startRealTimeMeterMode(JSONArray data) throws JSONException {
        realTimeFunctionName = data.getString(0);

        activityTrackerManager.startRealTimeMeterMode();

        ((CallbackContext) callbacks.get("startRealTimeMeterMode")).success();
        return true;
    }

    private boolean stopRealTimeMeterMode(JSONArray data) throws JSONException {
        realTimeFunctionName = null;

        activityTrackerManager.stopRealTimeMeterMode();

        ((CallbackContext) callbacks.get("stopRealTimeMeterMode")).success();
        return true;
    }

}