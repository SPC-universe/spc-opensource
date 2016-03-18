package com.spc.spcfitsdk.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.content.DialogInterface;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.CountDownTimer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.spc.spcfitsdk.ActivityTracker;
import com.spc.spcfitsdk.ActivityTrackerManager;
import com.spc.spcfitsdk.ActivityTrackerCallback;

public class SPCFit extends CordovaPlugin {

    HashMap<String, Object> callbacks;

    String realTimeFunctionName;
    String ecgModeFunctionName;

    ActivityTrackerManager activityTrackerManager;
    ActivityTracker activityTracker;

    boolean connected;

    JSONArray connectData;

    Context context;

    Activity activity;

    String connectedSerialNumber;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.activity = this.cordova.getActivity();
        this.context = this.cordova.getActivity().getApplicationContext();
        activityTrackerManager = new ActivityTrackerManager(
                context,
                new ActivityTrackerCallback() {

                    @Override
                    public void connected(ActivityTracker tracker) {

                        activityTracker = tracker;
                        SPCFit.this.connected = true;
                        if (activityTracker.getModel() == ActivityTracker.SPC_FIT_PRO) {
                            ((CallbackContext) callbacks.get("connect")).success();
                        } else {
                            sendBonding();
                        }

                    }

                    @Override
                    public void disconnected() {
                        SPCFit.this.connectedSerialNumber = null;
                        SPCFit.this.connected = false;
                        ((CallbackContext) callbacks.get("connect")).error("Disconnected");
                    }

                    @Override
                    public void deviceToConnectNotFound() {
                        try{
                            connect(SPCFit.this.connectData);
                        }catch (Exception e){
                        }
                    }

                    @Override
                    public void foundDevice(BluetoothDevice device) {
                        //TODO
                    }

                    @Override
                    public void foundDevices(ArrayList<BluetoothDevice> devices) {
                        JSONArray array = new JSONArray();
                        for (BluetoothDevice device:devices) {
                            array.put(device.getName());
                        }

                        ((CallbackContext) callbacks.get("findDevices")).success(array);
                    }

                    @Override
                    public void safeBondingSendPassword(boolean error) {
                        if (error) {
                            saveBonding();
                        } else {
                            ((CallbackContext) callbacks.get("connect")).success();
                        }
                    }

                    @Override
                    public void safeBondingStatus(boolean error) {
                        if (!error) {
                            sendBonding();
                            hideCountDown();
                        }
                    }

                    @Override
                    public void getTime(Calendar calendar) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        ((CallbackContext) callbacks.get("getTime")).success(sdf.format(calendar.getTime()));
                    }

                    @Override
                    public void setTime(){
                        ((CallbackContext) callbacks.get("setTime")).success("Hora guardada");
                    }

                    @Override
                    public void getPersonalInformation(int male, int age, int height, int weight, int stride, String deviceId) {
                        try{
                            boolean maleBoolean = false;
                            if (male == 1) {
                                maleBoolean = true;
                            }
                            JSONObject object = new JSONObject();
                            object.put("stepLength", stride);
                            object.put("weight", weight);
                            object.put("height", height);
                            object.put("age", age);
                            object.put("man", maleBoolean);
                            ((CallbackContext) callbacks.get("getPersonalInformation")).success(object);

                        } catch(Exception e){
                            ((CallbackContext) callbacks.get("getPersonalInformation")).error(e.getMessage());
                        }
                    }

                    @Override
                    public void setPersonalInformation(){
                        ((CallbackContext) callbacks.get("setPersonalInformation")).success("Informacion personal guardada");
                    }

                    @Override
                    public void getTargetSteps(int goal){
                        ((CallbackContext) callbacks.get("getTargetSteps")).success(goal);
                    }

                    @Override
                    public void setTargetSteps(){
                        ((CallbackContext) callbacks.get("setTargetSteps")).success("Objetivo guardado");
                    }

                    @Override
                    public void getSleepMonitorMode(boolean sleep) {
                        String mode = "false";
                        if (sleep) {
                            mode = "true";
                        }
                        ((CallbackContext) callbacks.get("getSleepMonitorMode")).success(mode);
                    }

                    @Override
                    public void switchSleepMonitorMode() {
                        ((CallbackContext) callbacks.get("switchSleepMonitorMode")).success("Modo sueño cambiado");
                    }

                    @Override
                    public void realTimeMeterMode(int steps, int aerobicSteps, int cal, int km, int activityTime){
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

                    @Override
                    public void getCurrentActivityInformation (int steps, int aerobicSteps, int cal, int km, int activityTime){
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

                    private int steps;
                    private int aerobicSteps;
                    private int cal;

                    @Override
                    public void getTotalActivityData0(byte dayIndex, Calendar calendar, int steps, int aerobicSteps, int cal) {
                        this.steps = steps;
                        this.aerobicSteps = aerobicSteps;
                        this.cal = cal;
                    }

                    @Override
                    public void getTotalActivityData1(byte dayIndex, Calendar calendar, int km, int activityTime) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        try{
                            JSONObject totalActivity = new JSONObject();
                            totalActivity.put("date", sdf.format(calendar.getTime()));
                            totalActivity.put("steps", this.steps);
                            totalActivity.put("aerobicSteps", this.aerobicSteps);
                            totalActivity.put("calories", this.cal);
                            totalActivity.put("distance", km);
                            totalActivity.put("activityTime", activityTime);
                            ((CallbackContext) callbacks.get("getTotalActivityData")).success(totalActivity);
                        } catch(Exception e){
                            ((CallbackContext) callbacks.get("getTotalActivityData")).success(e.getMessage());
                        }

                    }

                    @Override
                    public void getDetailActivityDataWithOutData() {
                        try{
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            JSONObject detailActivity = new JSONObject();
                            detailActivity.put("date", null);
                            detailActivity.put("activityDetail", null);
                            detailActivity.put("sleepQualityDetail", null);
                            ((CallbackContext) callbacks.get("getDetailActivityData")).success(detailActivity);
                        } catch(Exception e){
                            ((CallbackContext) callbacks.get("getDetailActivityData")).success(e.getMessage());
                        }
                    }

                    JSONArray activityDetail;
                    JSONArray sleepQualityDetail;

                    @Override
                    public void getDetailActivityDataActivityData(int index, Calendar calendar, int steps, int aerobicSteps, int cal, int km) {

                        if (this.activityDetail == null) {
                            this.activityDetail = new JSONArray();
                            this.sleepQualityDetail = new JSONArray();
                        }
                        try {
                            if ((steps != 0) || (cal != 0) || (km != 0)) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                                JSONObject object = new JSONObject();
                                object.put("date", sdf.format(calendar.getTime()));
                                object.put("steps", steps);
                                object.put("aerobicSteps", aerobicSteps);
                                object.put("calories", cal);
                                object.put("distance", km);

                                this.activityDetail.put(object);
                            }
                        }
                        catch (JSONException e) {
                        }
                        if (index == 95) {
                            try{
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                JSONObject detailActivity = new JSONObject();
                                detailActivity.put("date", sdf.format(calendar.getTime()));
                                detailActivity.put("activityDetail", this.activityDetail);
                                detailActivity.put("sleepQualityDetail", this.sleepQualityDetail);
                                ((CallbackContext) callbacks.get("getDetailActivityData")).success(detailActivity);
                            } catch(Exception e) {
                                ((CallbackContext) callbacks.get("getDetailActivityData")).success(e.getMessage());
                            }
                            this.activityDetail = null;
                            this.sleepQualityDetail = null;
                        }
                    }

                    @Override
                    public void getDetailActivityDataSleepQuality(int index, Calendar calendar, HashMap<Calendar, Integer> hashMap) {
                        if (this.sleepQualityDetail == null) {
                            this.activityDetail = new JSONArray();
                            this.sleepQualityDetail = new JSONArray();
                        }

                        JSONArray result = new JSONArray();

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            SortedSet<Calendar> keys = new TreeSet<Calendar>(hashMap.keySet());
                            for (Calendar key : keys) {
                                JSONObject object = new JSONObject();
                                object.put("date", sdf.format(key.getTime()));
                                object.put("quality", hashMap.get(key));
                                sleepQualityDetail.put(object);
                            }
                        }
                        catch (JSONException e) {
                        }

                        if (index == 95) {
                            try{
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                JSONObject detailActivity = new JSONObject();
                                detailActivity.put("date", sdf.format(calendar.getTime()));
                                detailActivity.put("activityDetail", this.activityDetail);
                                detailActivity.put("sleepQualityDetail", this.sleepQualityDetail);
                                ((CallbackContext) callbacks.get("getDetailActivityData")).success(detailActivity);
                            } catch(Exception e) {
                                ((CallbackContext) callbacks.get("getDetailActivityData")).success(e.getMessage());
                            }
                            this.activityDetail = null;
                            this.sleepQualityDetail = null;
                        }
                    }

                    @Override
                    public void ECGMode(byte[] bytes) {
                        if(ecgModeFunctionName != null){
                            String data = "";

                            for (byte b:bytes) {
                                data += b + ",";
                            }
                            data = data.substring(0, data.length()-1);
                            final String script ="javascript:" + ecgModeFunctionName + "( [" + data + "] );";
                            Log.e("DATA", script);

                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    SPCFit.this.webView.loadUrl(script);
                                }
                            });
                        }
                    }

                    @Override
                    public void ECGMode(int heartRate) {
                        if(ecgModeFunctionName != null){

                            final String script ="javascript:" + ecgModeFunctionName + "( [" + heartRate + "] );";

                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    SPCFit.this.webView.loadUrl(script);
                                }
                            });
                        }
                    }

                    @Override
                    public void getECGData(Calendar calendar, int heartRate) {
                        try{
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            JSONObject object = new JSONObject();
                            object.put("date", sdf.format(calendar.getTime()));
                            object.put("heartRate", heartRate);
                            ((CallbackContext) callbacks.get("getECGData")).success(object);

                        } catch(Exception e){
                            ((CallbackContext) callbacks.get("getECGData")).error(e.getMessage());
                        }
                    }

                    @Override
                    public void stopECGMode() {
                        ((CallbackContext) callbacks.get("stopECGMode")).success();
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
            SPCFit.this.connectData = data;
            return connect(data);
        } else if (action.equals("setPassword")) {
            return setPassword(data);
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
        } else if (action.equals("getSleepMonitorMode")) {
            return getSleepMonitorMode(data);
        } else if (action.equals("switchSleepMonitorMode")) {
            return switchSleepMonitorMode(data);
        } else if (action.equals("startECGMode")) {
            return startECGMode(data);
        } else if (action.equals("getECGData")) {
            return getECGData(data);
        }else if (action.equals("stopECGMode")) {
            return stopECGMode(data);
        } else {
            return false;
        }
    }

    private boolean findDevices(JSONArray data) throws JSONException {

        activityTrackerManager.findDevices();

        return true;
    }

    private boolean connect(JSONArray data) throws JSONException {

        String serialNumber = data.getString(0);

        if(this.connectedSerialNumber == null){
            this.connectedSerialNumber = serialNumber;
            activityTrackerManager.connectToSerialNumber(serialNumber);
        } else if(this.connected && !connectedSerialNumber.equals(serialNumber)) {
            this.connectedSerialNumber = serialNumber;
            activityTrackerManager.connectToSerialNumber(serialNumber);
        } else if(!this.connected) {
            this.connectedSerialNumber = serialNumber;
            activityTrackerManager.connectToSerialNumber(serialNumber);
        } else {
            ((CallbackContext) callbacks.get("connect")).success();
        }
        return true;
    }

    private boolean setTime(JSONArray data) throws JSONException {
        try {

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(data.getString(0));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            activityTracker.setTime(calendar, ActivityTracker.HIGH_PRIORITY);

            return true;
        } catch (Exception e) {
            ((CallbackContext) callbacks.get("setTime")).error("Error, la fecha ha de ir con este formato 'yyyy-MM-dd HH:mm:ss'");
            return false;
        }
    }

    private boolean getTime(JSONArray data) throws JSONException {

        activityTracker.getTime(ActivityTracker.LOW_PRIORITY);

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

            byte sex = (byte) 0;
            if (male) {
                sex = (byte) 1;
            }

            activityTracker.setPersonalInformation( sex, (byte) age, (byte) height, (byte) weight, (byte) stepLength, ActivityTracker.HIGH_PRIORITY);
            return true;
        } catch (Exception e) {
            ((CallbackContext) callbacks.get("setPersonalInformation")).error("Error, "+ e.getMessage());
            return false;
        }
    }

    private boolean getPersonalInformation(JSONArray data) throws JSONException {

        activityTracker.getPersonalInformation(ActivityTracker.LOW_PRIORITY);

        return true;
    }

    private boolean setTargetSteps(JSONArray data) throws JSONException {

        activityTracker.setTargetSteps(data.getInt(0), ActivityTracker.HIGH_PRIORITY);

        return true;
    }

    private boolean getTargetSteps(JSONArray data) throws JSONException {

        activityTracker.getTargetSteps(ActivityTracker.LOW_PRIORITY);

        return true;
    }

    private boolean getCurrentActivityInformation(JSONArray data) throws JSONException {

        activityTracker.getCurrentActivityInformation(ActivityTracker.LOW_PRIORITY);

        return true;
    }

    private boolean getTotalActivityData(JSONArray data) throws JSONException {

        activityTracker.getTotalActivityData(data.getInt(0), ActivityTracker.LOW_PRIORITY);

        return true;
    }

    private boolean getDetailActivityData(JSONArray data) throws JSONException {

        activityTracker.getDetailActivityData(data.getInt(0), ActivityTracker.LOW_PRIORITY);

        return true;
    }

    private boolean startRealTimeMeterMode(JSONArray data) throws JSONException {
        realTimeFunctionName = data.getString(0);

        activityTracker.startRealTimeMeterMode(ActivityTracker.LOW_PRIORITY);

        ((CallbackContext) callbacks.get("startRealTimeMeterMode")).success();
        return true;
    }

    private boolean stopRealTimeMeterMode(JSONArray data) throws JSONException {
        realTimeFunctionName = null;

        activityTracker.stopRealTimeMeterMode(ActivityTracker.LOW_PRIORITY);

        ((CallbackContext) callbacks.get("stopRealTimeMeterMode")).success();
        return true;
    }

    private boolean getSleepMonitorMode(JSONArray data) throws JSONException {

        activityTracker.getSleepMonitorMode(ActivityTracker.LOW_PRIORITY);

        return true;
    }

    private boolean switchSleepMonitorMode(JSONArray data) throws JSONException {

        activityTracker.switchSleepMonitorMode(ActivityTracker.LOW_PRIORITY);

        return true;
    }

    private boolean startECGMode(JSONArray data) throws JSONException {
        ecgModeFunctionName = data.getString(0);

        activityTracker.startECGMode(ActivityTracker.LOW_PRIORITY);

        ((CallbackContext) callbacks.get("startECGMode")).success();
        return true;
    }

    private boolean getECGData(JSONArray data) throws JSONException {

        activityTracker.getECGData(data.getInt(0), ActivityTracker.LOW_PRIORITY);

        return true;
    }

    private boolean stopECGMode(JSONArray data) throws JSONException {

        activityTracker.stopECGMode(ActivityTracker.LOW_PRIORITY);

        return true;
    }

    private boolean setPassword(JSONArray data) throws JSONException {

        String password = data.getString(0);

        if(password.length()!= 6){
            ((CallbackContext) callbacks.get("setPassword")).error("Error, la contraseña debe tener 6 digitos");
        } else {
            bondingPassword = password;
            ((CallbackContext) callbacks.get("setPassword")).success();
        }

        return true;
    }

    //BONDING

    String bondingPassword = "123456";

    public void sendBonding() {
        activityTracker.safeBondingSendPassword(bondingPassword, ActivityTracker.HIGH_PRIORITY);
    }

    public void saveBonding() {
        showCountDown();
        activityTracker.safeBondingSavePassword(bondingPassword, ActivityTracker.HIGH_PRIORITY);
    }


    AlertDialog alertDialog;

    CountDownTimer countDownTimer;

    String action = "";

    public void showCountDown(){
        switch (activityTracker.getModel()){
            case ActivityTracker.SPC_FIT:
                action = "For linking, move the bracelet twice before %1$d seconds";
                break;
            case ActivityTracker.SPC_FIT_PULSE:
                action = "For linking, press the screen before %1$d seconds";
                break;
        }

        Runnable runnable = new Runnable() {
            public void run() {
                alertDialog = new AlertDialog.Builder(SPCFit.this.activity)
                        .setMessage(String.format(action, 10))
                        .setCancelable(false)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (countDownTimer != null) {
                                    countDownTimer.cancel();
                                }
                                activityTrackerManager.disconnect();
                            }
                        })
                        .show();


            final int interval = 1000;

            countDownTimer = new CountDownTimer(10000, interval) {
                @Override
                public void onTick(long millisUntilFinished) {
                    alertDialog.setMessage(String.format(action, millisUntilFinished/interval));
                }

                @Override
                public void onFinish() {
                    alertDialog.dismiss();
                    alertDialog = new AlertDialog.Builder(SPCFit.this.activity)
                            .setMessage("The time has finished, Want to try again?")
                            .setCancelable(false)
                            .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                    saveBonding();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    activityTrackerManager.disconnect();
                                }
                            })
                            .show();
                }
            }.start();
            };
        };

        activity.runOnUiThread(runnable);
    }

    public void hideCountDown() {
        if(countDownTimer != null){
            countDownTimer.cancel();
        }
        if(alertDialog != null ) {
            alertDialog.dismiss();
        }
    }



}
