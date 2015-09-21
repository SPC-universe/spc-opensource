package com.spc.spcfitsdk.model.SPCFitSDK;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.UUID;

public class ActivityTracker{

    private static final String CLASS = "Activity Tracker";

    public static String PEDOMETER_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static String PEDOMETER_CHARACTERISTIC_TX = "0000fff6-0000-1000-8000-00805f9b34fb";
    public static String PEDOMETER_CHARACTERISTIC_RX = "0000fff7-0000-1000-8000-00805f9b34fb";
    public static String DESCRIPTOR_NOTIFICATION = "00002902-0000-1000-8000-00805f9b34fb";

    public static final int SPC_FIT_PRO = 9602;
    public static final int SPC_FIT = 9603;
    public static final int SPC_FIT_PULSE = 9604;

    public static final int HIGH_PRIORITY = 0;
    public static final int LOW_PRIORITY = 1;

    private BluetoothGattCharacteristic txCharacteristic;
    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bluetoothAdapter;

    private ActivityTrackerCallback callback;

    private Queue queue;

    private boolean connected;

    private String address;
    private String serialNumber;

    private int model;

    public ActivityTracker(String address, String serialNumber, ActivityTrackerCallback activityTrackerCallback) {
        this.address = address;
        this.serialNumber = serialNumber;
        this.callback = activityTrackerCallback;

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.queue = new Queue(this);

        setModel(serialNumber);
    }


////---- Getters & Setters

    private void setModel(String serialNumber) {
        if(serialNumber.substring(0, 2).equals("A0")){
            this.model = SPC_FIT_PRO;
        } else if(serialNumber.substring(0, 2).equals("A1")) {
            this.model = SPC_FIT;
        } else if(serialNumber.substring(0, 2).equals("A2")) {
            this.model = SPC_FIT_PULSE;
        }
    }

    public int getModel() {
        return model;
    }

    public String getAddress() {
        return address;
    }

    public String getSerialNumber() {
        return serialNumber;
    }


////---- BluetoothGattCallback

    protected BluetoothGattCallback getBluetoothGattCallback() {
        return new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                bluetoothGatt = gatt;
                switch (status) {
                    case 133:
                        closeGatt();
                        callback.deviceToConnectNotFound();
                        break;
                    default:
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            bluetoothGatt.discoverServices();
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            closeGatt();
                            callback.disconnected();
                        }
                        break;
                }

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(PEDOMETER_SERVICE));
                    if (service != null){
                        txCharacteristic = service.getCharacteristic(UUID.fromString(PEDOMETER_CHARACTERISTIC_TX));
                        BluetoothGattCharacteristic rxCharacteristic = service.getCharacteristic(UUID.fromString(PEDOMETER_CHARACTERISTIC_RX));

                        bluetoothGatt.setCharacteristicNotification(rxCharacteristic, true);

                        BluetoothGattDescriptor descriptor = rxCharacteristic.getDescriptor(UUID.fromString(DESCRIPTOR_NOTIFICATION));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bluetoothGatt.writeDescriptor(descriptor);
                    } else {
                        disconnect();
                    }
                } else {
                    disconnect();
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                if(descriptor.getUuid().equals(UUID.fromString(DESCRIPTOR_NOTIFICATION))){
                    connected = true;
                    callback.connected(ActivityTracker.this);
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                queue.txDone();
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                parseResponse(characteristic.getValue());
            }
        };
    }


////---- Conexión

    public boolean isConnected() {
        return connected;
    }

    private void disconnect() {
        this.address = null;
        this.serialNumber = null;

        if (bluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            bluetoothGatt.disconnect();
        }
    }

    private void closeGatt() {
        connected = false;

        if (bluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            bluetoothGatt.close();
        }
        bluetoothGatt = null;
    }


////---- Metodos para añadir a la cola de llamadas.

    public void safeBondingSavePassword(String password, int priority) {
        Object[] params = {password};
        queue.add(new QueueAction(ActivityTracker.CMD_SAFE_BONDING_SAVE_PASSWORD, priority, params));
    }

    public void safeBondingSendPassword(String password, int priority) {
        Object[] params = {password};
        queue.add(new QueueAction(ActivityTracker.CMD_SAFE_BONDING_SEND_PASSWORD, priority, params));
    }

    public void safeBondingStatus(int priority){
        queue.add(new QueueAction(ActivityTracker.CMD_SAFE_BONDING_STATUS, priority));
    }

    public void getTime(int priority) {
        queue.add(new QueueAction(ActivityTracker.CMD_GET_TIME, priority));
    }

    public void setTime(Calendar calendar, int priority) {
        Object[] params={calendar};
        queue.add(new QueueAction(ActivityTracker.CMD_SET_TIME, priority, params));
    }

    public void getPersonalInformation(int priority) {
        queue.add(new QueueAction(ActivityTracker.CMD_GET_PERSONAL_INFORMATION, priority));
    }

    public void setPersonalInformation(byte sex, byte age, byte height, byte weight, byte stride, int priority) {
        Object[] params = {sex, age, height, weight, stride};
        queue.add(new QueueAction(ActivityTracker.CMD_SET_PERSONAL_INFORMATION, priority, params));
    }

    public void getTargetSteps(int priority) {
        queue.add(new QueueAction(ActivityTracker.CMD_GET_TARGET_STEPS, priority));
    }

    public void setTargetSteps(int goal, int priority) {
        Object[] params={goal};
        queue.add(new QueueAction(ActivityTracker.CMD_SET_TARGET_STEPS, priority, params));
    }

    public void getSleepMonitorMode(int priority) {
        queue.add(new QueueAction(ActivityTracker.CMD_GET_SLEEP_MONITOR_MODE, priority));
    }

    public void switchSleepMonitorMode(int priority) {
        queue.add(new QueueAction(ActivityTracker.CMD_SWITCH_SLEEP_MONITOR_MODE, priority));
    }

    public void startRealTimeMeterMode(int priority) {
        queue.add(new QueueAction(ActivityTracker.CMD_START_REAL_TIME_METER_MODE_AND_UPDATES, priority));
    }

    public void stopRealTimeMeterMode(int priority) {
        queue.add(new QueueAction(ActivityTracker.CMD_STOP_REAL_TIME_METER_MODE, priority));
    }

    public void getCurrentActivityInformation(int priority) {
        queue.add(new QueueAction(ActivityTracker.CMD_GET_CURRENT_ACTIVITY_INFORMATION, priority));
    }

    public void getTotalActivityData(int day, int priority) {
        Object[] params={day};
        queue.add(new QueueAction(ActivityTracker.CMD_GET_TOTAL_ACTIVITY_DATA, priority, params));
    }

    public void getDetailActivityData(int day, int priority) {
        Object[] params={day};
        queue.add(new QueueAction(ActivityTracker.CMD_GET_DETAIL_ACTIVITY_DATA, priority, params));
    }

    public void startECGMode(int priority) {
        queue.add(new QueueAction(ActivityTracker.CMD_START_ECG_MODE, priority));
    }

    public void stopECGMode(int priority) {
        queue.add(new QueueAction(ActivityTracker.CMD_STOP_ECG_MODE, priority));
    }

    public void getECGData(int index, int priority) {
        Object[] params={index};
        queue.add(new QueueAction(ActivityTracker.CMD_GET_ECG_DATA, priority, params));
    }

    public void deleteECGData(int priority) {
        queue.add(new QueueAction(ActivityTracker.CMD_DELETE_ECG_DATA, priority));
    }


////---- Enviar comando al dispositivo

    protected void send(QueueAction queueAction) {

        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON){

            switch (queueAction.getCMD()) {
                case CMD_SAFE_BONDING_SEND_PASSWORD:
                    String sendPassword = ((String) queueAction.getParams()[0]);
                    safeBondingSendPassword(sendPassword);
                    break;
                case CMD_SAFE_BONDING_SAVE_PASSWORD:
                    String savePassword = ((String) queueAction.getParams()[0]);
                    safeBondingSavePassword(savePassword);
                    break;
                case CMD_SAFE_BONDING_STATUS:
                    safeBondingStatus();
                    break;
                case CMD_SET_TIME:
                    setTime((Calendar) queueAction.getParams()[0]);
                    break;
                case CMD_GET_TIME:
                    getTime();
                    break;
                case CMD_SET_PERSONAL_INFORMATION:
                    setPersonalInformation(
                            (byte) queueAction.getParams()[0],
                            (byte) queueAction.getParams()[1],
                            (byte) queueAction.getParams()[2],
                            (byte) queueAction.getParams()[3],
                            (byte) queueAction.getParams()[4]
                    );
                    break;
                case CMD_GET_PERSONAL_INFORMATION:
                    getPersonalInformation();
                    break;
                case CMD_SET_TARGET_STEPS:
                    setTargetSteps(((int) queueAction.getParams()[0]));
                    break;
                case CMD_GET_TARGET_STEPS:
                    getTargetSteps();
                    break;
                case CMD_GET_SLEEP_MONITOR_MODE:
                    getSleepMonitorMode();
                    break;
                case CMD_SWITCH_SLEEP_MONITOR_MODE:
                    switchSleepMonitorMode();
                    break;
                case CMD_START_REAL_TIME_METER_MODE_AND_UPDATES:
                    startRealTimeMeterMode();
                    break;
                case CMD_STOP_REAL_TIME_METER_MODE:
                    stopRealTimeMeterMode();
                    break;
                case CMD_GET_CURRENT_ACTIVITY_INFORMATION:
                    getCurrentActivityInformation();
                    break;
                case CMD_GET_TOTAL_ACTIVITY_DATA:
                    getTotalActivityData((byte) ((int) queueAction.getParams()[0]));
                    break;
                case CMD_GET_DETAIL_ACTIVITY_DATA:
                    getDetailActivityData((byte) ((int) queueAction.getParams()[0]));
                    break;
                case CMD_START_ECG_MODE:
                    startECGMode();
                    break;
                case CMD_STOP_ECG_MODE:
                    stopECGMode();
                    break;
                case CMD_DELETE_ECG_DATA:
                    deleteECGData();
                    break;
                case CMD_GET_ECG_DATA:
                    getECGData((byte) ((int) queueAction.getParams()[0]));
                    break;
            }

        }
    }


////---- Recibir comando del dispositivo

    private void parseResponse(byte[] bytes) {

        boolean ECGModeDone = false;

        final byte cmd = bytes[CMD];

        switch (cmd) {
            case CMD_ECG_MODE_UPDATES:
                ECGModeDone = true;
                ECGModeResponse(bytes);
                break;
            case CMD_ECG_MODE_RATE_UPDATES:
                ECGModeDone = true;
                ECGModeRateResponse(bytes);
                break;
            case CMD_GET_ECG_DATA:
            case CMD_GET_ECG_DATA_ERROR:
                ECGModeDone = true;
                getECGDataResponse(bytes);
                break;
            case CMD_DELETE_ECG_DATA:
            case CMD_DELETE_ECG_DATA_ERROR:
                ECGModeDone = true;
                deleteECGDataResponse(bytes);
                break;
            case CMD_STOP_ECG_MODE_OK:
            case CMD_STOP_ECG_MODE_ERROR:
                ECGModeDone = true;
                stopECGModeResponse(bytes);
                break;
        }

        if (!ECGModeDone) {
            switch (cmd & CMD_ERROR_MASK) {
                case CMD_SAFE_BONDING_SAVE_PASSWORD:
                    safeBondingSavePasswordResponse(bytes);
                    break;
                case CMD_SAFE_BONDING_SEND_PASSWORD:
                    safeBondingSendPasswordResponse(bytes);
                    break;
                case CMD_SAFE_BONDING_STATUS:
                    safeBondingStatusResponse(bytes);
                    break;
                case CMD_GET_TIME:
                    getTimeResponse(bytes);
                    break;
                case CMD_SET_TIME:
                    setTimeResponse(bytes);
                    break;
                case CMD_GET_PERSONAL_INFORMATION:
                    getPersonalInformationResponse(bytes);
                    break;
                case CMD_SET_PERSONAL_INFORMATION:
                    setPersonalInformationResponse(bytes);
                    break;
                case CMD_GET_TARGET_STEPS:
                    getTargetStepsResponse(bytes);
                    break;
                case CMD_SET_TARGET_STEPS:
                    setTargetStepsResponse(bytes);
                    break;
                case CMD_GET_SLEEP_MONITOR_MODE:
                    getSleepMonitorModeResponse(bytes);
                    break;
                case CMD_SWITCH_SLEEP_MONITOR_MODE:
                    switchSleepMonitorModeResponse(bytes);
                    break;
                case CMD_START_REAL_TIME_METER_MODE_AND_UPDATES:
                    realTimeMeterModeResponse(bytes);
                    break;
                case CMD_STOP_REAL_TIME_METER_MODE:
                    stopRealTimeMeterModeResponse(bytes);
                    break;
                case CMD_GET_CURRENT_ACTIVITY_INFORMATION:
                    getCurrentActivityInformationResponse(bytes);
                    break;
                case CMD_GET_TOTAL_ACTIVITY_DATA:
                    getTotalActivityDataResponse(bytes);
                    break;
                case CMD_GET_DETAIL_ACTIVITY_DATA:
                    getDetailActivityDataResponse(bytes);
                    break;
            }
        }

    }


////---- Comandos

    private final byte CMD = 0;
    private final byte AA = 1;
    private final byte BB = 2;
    private final byte CC = 3;
    private final byte DD = 4;
    private final byte EE = 5;
    private final byte FF = 6;
    private final byte GG = 7;
    private final byte HH = 8;
    private final byte II = 9;
    private final byte JJ = 10;
    private final byte KK = 11;
    private final byte LL = 12;
    private final byte MM = 13;
    private final byte NN = 14;
    private final byte CRC = 15;
    private final byte CMD_ERROR_MASK = 0x7f;


    public static final byte CMD_SAFE_BONDING_SAVE_PASSWORD = 0x20;

    private void safeBondingSavePassword(String password) {
        Log.d(CLASS,"CMD: "+CMD_SAFE_BONDING_SAVE_PASSWORD+" safeBondingSavePassword.");

        byte[] pass = new byte[6];

        for (int i = 0; i < 6;++i) {
            pass[i] =(byte) password.charAt(i);
        }

        byte bytes[] = {CMD_SAFE_BONDING_SAVE_PASSWORD,
                pass[0],
                pass[1],
                pass[2],
                pass[3],
                pass[4],
                pass[5],
                0, 0, 0, 0, 0, 0, 0, 0, 0 };

        sendDataWithCRC(bytes);
        queue.rxDone();
    }

    private void safeBondingSavePasswordResponse(byte[] bytes) {
        Log.d(CLASS,"safeBondingSavePasswordResponse bytes: "+bytesToString(bytes));
        callback.safeBondingSavePassword();
    }


    public static final byte CMD_SAFE_BONDING_SEND_PASSWORD = 0x6A;

    private void safeBondingSendPassword(String password) {
        Log.d(CLASS,"CMD: "+CMD_SAFE_BONDING_SEND_PASSWORD+" safeBondingSendPassword.");

        byte[] pass = new byte[6];

        for (int i = 0; i < 6;++i) {
            pass[i] =(byte) password.charAt(i);
        }

        byte bytes[] = {CMD_SAFE_BONDING_SEND_PASSWORD,
                pass[0],
                pass[1],
                pass[2],
                pass[3],
                pass[4],
                pass[5],
                0, 0, 0, 0, 0, 0, 0, 0, 0 };

        sendDataWithCRC(bytes);
    }

    private void safeBondingSendPasswordResponse(byte[] bytes) {
        Log.d(CLASS, "safeBondingSendPasswordResponse bytes: " + bytesToString(bytes));

        final byte cmd = bytes[CMD];

        boolean error = (cmd & 0x80) != 0;

        queue.rxDone();

        callback.safeBondingSendPassword(error);

    }


    public static final byte CMD_SAFE_BONDING_STATUS = 0x21;

    private void safeBondingStatus() {
        Log.d(CLASS,"CMD: "+CMD_SAFE_BONDING_STATUS+" safeBondingStatus.");

        byte bytes[] = {CMD_SAFE_BONDING_STATUS, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);


    }

    private void safeBondingStatusResponse(byte[] bytes) {
        Log.d(CLASS,"safeBondingStatusResponse bytes: "+bytesToString(bytes));

        final byte cmd = bytes[CMD];

        boolean error = (cmd & 0x80) != 0;

        callback.safeBondingStatus(error);
    }


    public static final byte CMD_SET_TIME = 0x01;

    private void setTime(Calendar calendar) {
        Log.d(CLASS,"CMD: "+CMD_SET_TIME+" setTime.");

        byte[] dateBytes = BCDBytesFromDate(calendar);

        byte bytes[] = {CMD_SET_TIME,
                dateBytes[0],
                dateBytes[1],
                dateBytes[2],
                dateBytes[3],
                dateBytes[4],
                dateBytes[5],
                0, 0, 0, 0, 0, 0, 0, 0, 0 };

        sendDataWithCRC(bytes);
    }

    private void setTimeResponse(byte[] bytes) {
        Log.d(CLASS,"setTimeResponse bytes: "+bytesToString(bytes));
        callback.setTime();
        queue.rxDone();
    }


    public static final byte CMD_GET_TIME = 0x41;

    private void getTime() {
        Log.d(CLASS,"CMD: "+CMD_GET_TIME+" getTime.");

        byte bytes[] = {CMD_GET_TIME, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void getTimeResponse(byte[] bytes) {
        Log.d(CLASS,"getTimeResponse bytes: "+bytesToString(bytes));

        byte year = byteFromBCD(bytes[AA]);
        byte month = byteFromBCD(bytes[BB]);
        byte day = byteFromBCD(bytes[CC]);
        byte hour = byteFromBCD(bytes[DD]);
        byte minute = byteFromBCD(bytes[EE]);
        byte second = byteFromBCD(bytes[FF]);

        Calendar calendar = new GregorianCalendar(2000 + year, month - 1, day, hour, minute, second);

        callback.getTime(calendar);
        queue.rxDone();
    }


    public static final byte CMD_SET_PERSONAL_INFORMATION= 0x02;

    private void setPersonalInformation(byte male, byte age, byte height, byte weight,byte stride) {
        Log.d(CLASS,"CMD: "+CMD_SET_PERSONAL_INFORMATION+" setPersonalInformation.");

        byte bytes[] = {CMD_SET_PERSONAL_INFORMATION, male, age, height, weight, stride, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void setPersonalInformationResponse(byte[] bytes) {
        Log.d(CLASS,"setPersonalInformationResponse bytes: "+bytesToString(bytes));

        callback.setPersonalInformation();
        queue.rxDone();
    }


    public static final byte CMD_GET_PERSONAL_INFORMATION= 0x42;

    private void getPersonalInformation() {
        Log.d(CLASS,"CMD: "+CMD_GET_PERSONAL_INFORMATION+" getPersonalInformation.");

        byte bytes[] = {CMD_GET_PERSONAL_INFORMATION, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void getPersonalInformationResponse(byte[] bytes) {
        Log.d(CLASS,"getPersonalInformationResponse bytes: "+bytesToString(bytes));

        int male = bytes[AA] & 0xff;
        int age = bytes[BB] & 0xff;
        int height = bytes[CC] & 0xff;
        int weight = bytes[DD] & 0xff;
        int stride = bytes[EE] & 0xff;
        String deviceId=String.format("%02d %02d %02d %02d %02d %02d"
                , bytes[FF]
                , bytes[GG]
                , bytes[HH]
                , bytes[II]
                , bytes[JJ]
                , bytes[KK]);

        callback.getPersonalInformation(male, age, height, weight, stride, deviceId);
        queue.rxDone();
    }


    public static final byte CMD_SET_TARGET_STEPS = 0x0B;

    private void setTargetSteps(int steps) {
        Log.d(CLASS,"CMD: "+CMD_SET_TARGET_STEPS+" setTargetSteps.");

        byte steps1=(byte)(0xff & (steps>>16));
        byte steps2=(byte)(0xff & (steps>>8));
        byte steps3=(byte)(0xff & steps);

        byte bytes[] = {CMD_SET_TARGET_STEPS, steps1, steps2, steps3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void setTargetStepsResponse(byte[] bytes) {
        Log.d(CLASS,"setTargetStepsResponse bytes: "+bytesToString(bytes));
        callback.setTargetSteps();
        queue.rxDone();
    }


    public static final byte CMD_GET_TARGET_STEPS = 0x4B;

    private void getTargetSteps() {
        Log.d(CLASS,"CMD: "+CMD_GET_TARGET_STEPS+" getTargetSteps.");

        byte bytes[] = {CMD_GET_TARGET_STEPS, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void getTargetStepsResponse(byte[] bytes) {
        Log.d(CLASS,"getTargetStepsResponse bytes: "+bytesToString(bytes));

        int goal = intFrom3Bytes(bytes[AA], bytes[BB], bytes[CC]);

        callback.getTargetSteps(goal);
        queue.rxDone();

    }

    public static final byte CMD_GET_SLEEP_MONITOR_MODE = 0x6B;

    private void getSleepMonitorMode() {
        Log.d(CLASS,"CMD: "+CMD_GET_SLEEP_MONITOR_MODE+" getSleepMonitorMode.");

        byte bytes[] = {CMD_GET_SLEEP_MONITOR_MODE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void getSleepMonitorModeResponse(byte[] bytes) {
        Log.d(CLASS,"getSleepMonitorModeResponse bytes: "+bytesToString(bytes));
        boolean sleep = bytes[AA] != 0;

        callback.getSleepMonitorMode(sleep);
        queue.rxDone();
    }


    public static final byte CMD_SWITCH_SLEEP_MONITOR_MODE = 0x49;

    private void switchSleepMonitorMode() {
        Log.d(CLASS,"CMD: "+CMD_SWITCH_SLEEP_MONITOR_MODE+" switchSleepMonitorMode.");

        byte bytes[] = {CMD_SWITCH_SLEEP_MONITOR_MODE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void switchSleepMonitorModeResponse(byte[] bytes) {
        Log.d(CLASS,"switchSleepMonitorModeResponse bytes: "+bytesToString(bytes));
        callback.switchSleepMonitorMode();
        queue.rxDone();
    }


    public static final byte CMD_START_REAL_TIME_METER_MODE_AND_UPDATES = 0x09;

    private void startRealTimeMeterMode() {
        Log.d(CLASS,"CMD: "+CMD_START_REAL_TIME_METER_MODE_AND_UPDATES+" startRealTimeMeterMode.");

        byte bytes[] = {CMD_START_REAL_TIME_METER_MODE_AND_UPDATES, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void realTimeMeterModeResponse(byte[] bytes) {
        Log.d(CLASS,"realTimeMeterModeResponse bytes: "+bytesToString(bytes));

        boolean bug = (bytes[AA] & CMD_ERROR_MASK) != 0;

        if(!bug){
            int steps = intFrom3Bytes(bytes[AA], bytes[BB], bytes[CC]);
            int aerobicSteps = intFrom3Bytes(bytes[DD], bytes[EE], bytes[FF]);
            int cal = intFrom3Bytes(bytes[GG], bytes[HH], bytes[II]);
            int km = intFrom3Bytes(bytes[JJ], bytes[KK], bytes[LL]);
            int activityTime = intFrom2Bytes(bytes[MM], bytes[NN]);

            Log.d(CLASS, "Steps: " + steps + " Calories: " + cal + " KM: " + km);

            if (!(steps == 0 && cal == 0 && km == 0)){
                callback.realTimeMeterMode(steps, aerobicSteps, cal, km, activityTime);
            }

            if (queue.getQueueAction()!= null && queue.getQueueAction().getCMD() == CMD_START_REAL_TIME_METER_MODE_AND_UPDATES){
                queue.rxDone();
            }
        } else {
            Log.d(CLASS, "RealTimeMeterMode Bug");
        }
    }


    public static final byte CMD_STOP_REAL_TIME_METER_MODE = 0x0A;

    private void stopRealTimeMeterMode() {
        Log.d(CLASS,"CMD: "+CMD_STOP_REAL_TIME_METER_MODE+" stopRealTimeMeterMode.");

        byte bytes[] = {CMD_STOP_REAL_TIME_METER_MODE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void stopRealTimeMeterModeResponse(byte[] bytes) {
        Log.d(CLASS,"stopRealTimeMeterModeResponse bytes: "+bytesToString(bytes));
        callback.stopRealTimeMeterMode();
        queue.rxDone();
    }


    public static final byte CMD_GET_CURRENT_ACTIVITY_INFORMATION = 0x48;

    private void getCurrentActivityInformation() {
        Log.d(CLASS,"CMD: "+CMD_GET_CURRENT_ACTIVITY_INFORMATION+" getCurrentActivityInformation.");

        byte bytes[] = {CMD_GET_CURRENT_ACTIVITY_INFORMATION, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void getCurrentActivityInformationResponse(byte[] bytes) {
        Log.d(CLASS,"getCurrentActivityInformationResponse bytes: "+bytesToString(bytes));

        int steps = intFrom3Bytes(bytes[AA], bytes[BB], bytes[CC]);
        int aerobicSteps = intFrom3Bytes(bytes[DD], bytes[EE], bytes[FF]);
        int cal = intFrom3Bytes(bytes[GG], bytes[HH], bytes[II]);
        int km = intFrom3Bytes(bytes[JJ], bytes[KK], bytes[LL]);
        int activityTime = intFrom2Bytes(bytes[MM], bytes[NN]);

        Log.d(CLASS,"Steps: "+steps+" Calories: "+cal+" KM: "+km);
        callback.getCurrentActivityInformation(steps, aerobicSteps, cal, km, activityTime);
        queue.rxDone();
    }


    public static final byte CMD_GET_TOTAL_ACTIVITY_DATA= 0x07;

    private void getTotalActivityData(byte day) {
        Log.d(CLASS,"CMD: "+CMD_GET_TOTAL_ACTIVITY_DATA+" getTotalActivityData.");

        byte bytes[] = {CMD_GET_TOTAL_ACTIVITY_DATA, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void getTotalActivityDataResponse(byte[] bytes) {

        byte dayIndex = bytes[BB];
        byte year = byteFromBCD(bytes[CC]);
        byte month = byteFromBCD(bytes[DD]);
        byte day = byteFromBCD(bytes[EE]);

        Calendar calendar = new GregorianCalendar(2000 + year, month - 1, day);
        if (bytes[AA] == 0) {
            int steps = intFrom3Bytes(bytes[FF],bytes[GG],bytes[HH]);
            int aerobicSteps = intFrom3Bytes(bytes[II],bytes[JJ],bytes[KK]);
            int cal = intFrom3Bytes(bytes[LL],bytes[MM],bytes[NN]);

            callback.getTotalActivityData0(dayIndex, calendar, steps, aerobicSteps, cal);
        }
        if (bytes[AA] == 1) {
            int km = intFrom3Bytes(bytes[FF],bytes[GG],bytes[HH]);
            int activityTime = intFrom2Bytes(bytes[II],bytes[JJ]);

            callback.getTotalActivityData1(dayIndex, calendar, km, activityTime);
            queue.rxDone();
        }
    }


    public static final byte CMD_GET_DETAIL_ACTIVITY_DATA = 0x43;

    private void getDetailActivityData(byte day) {
        Log.d(CLASS,"CMD: "+CMD_GET_DETAIL_ACTIVITY_DATA+" getActivityDetailData.");

        byte bytes[] = {  CMD_GET_DETAIL_ACTIVITY_DATA, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void getDetailActivityDataResponse(byte[] bytes) {

        byte D_ACTIVITY_DATA = 0x00;
        byte D_SLEEP_QUALITY_DATA = (byte)0xff;
        byte D_TYPE_INDEX = 1;
        byte D_WITH_DATA = (byte)0xf0;
        byte D_WITHOUT_DATA = (byte)0xff;
        byte D_AA = 2;
        byte D_BB = 3;
        byte D_CC = 4;
        byte D_DD = 5;
        byte D_EE = 6;
        byte D_FF = 7;
        byte D_GG = 8;
        byte D_HH = 9;
        byte D_II = 10;
        byte D_JJ = 11;
        byte D_KK = 12;
        byte D_LL = 13;
        byte D_MM = 14;

        if (bytes[D_TYPE_INDEX] == D_WITHOUT_DATA) {
            Log.d(CLASS,"getDetailActivityDataResponse NO_DATA");
            callback.getDetailActivityDataWithOutData();
            queue.rxDone();
        }
        if (bytes[D_TYPE_INDEX] == D_WITH_DATA) {

            byte year = byteFromBCD(bytes[D_AA]);
            byte month = byteFromBCD(bytes[D_BB]);
            byte day = byteFromBCD(bytes[D_CC]);

            byte index = bytes[D_DD];
            byte hour = (byte)(index / 4);
            byte minute = (byte)(15 * (index % 4));

            if (bytes[D_EE] == D_ACTIVITY_DATA) {
                Calendar calendar = new GregorianCalendar(2000 + year, month - 1, day, hour, minute);

                int cal= intFrom2Bytes(bytes[D_GG],bytes[D_FF]);
                int steps= intFrom2Bytes(bytes[D_II],bytes[D_HH]);
                int km= intFrom2Bytes(bytes[D_KK],bytes[D_JJ]);
                int aerobicSteps= intFrom2Bytes(bytes[D_MM],bytes[D_LL]);

                callback.getDetailActivityDataActivityData(index, calendar, steps, aerobicSteps, cal, km);

            }
            if (bytes[D_EE] == D_SLEEP_QUALITY_DATA) {
                HashMap<Calendar, Integer> hashMap = new HashMap<>();
                Calendar calendar;

                for(int i = 0; i != 8; i++) {
                    int sleepQuality=bytes[D_FF + i];
                    calendar = new GregorianCalendar(2000+year, month-1, day, hour, (minute + (2 * i)), 0);
                    hashMap.put(calendar, sleepQuality);
                }

                calendar = new GregorianCalendar(2000+year, month-1, day, 0, 0, 0);
                callback.getDetailActivityDataSleepQuality(index, calendar, hashMap);
            }
            if(index == 95){
                queue.rxDone();
            }
        }
    }


    public static final byte CMD_START_ECG_MODE = (byte) 0x99;

    private void startECGMode() {
        Log.d(CLASS,"CMD: "+CMD_START_ECG_MODE+" startECGMode.");
        byte bytes[] = {CMD_START_ECG_MODE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);

        queue.rxDone();
    }


    public static final byte CMD_ECG_MODE_UPDATES = (byte) 0xA9;

    private void ECGModeResponse(byte[] bytes) {
        Log.d(CLASS, "ECGMode bytes: " + bytesToString(bytes));

        byte[] responseBytes = new byte[14];

        System.arraycopy(bytes, 1, responseBytes, 0, 14);

        callback.ECGMode(responseBytes);

    }


    public static final byte CMD_ECG_MODE_RATE_UPDATES = (byte) 0x94;

    private void ECGModeRateResponse(byte[] bytes) {
        Log.d(CLASS, "ECGModeRate bytes: " + bytesToString(bytes));

        int heartRate = intFromByte(bytes[AA]);

        callback.ECGModeRate(heartRate);

    }


    public static final byte CMD_STOP_ECG_MODE = (byte) 0x98;
    public static final byte CMD_STOP_ECG_MODE_OK = (byte) 0xA8;
    public static final byte CMD_STOP_ECG_MODE_ERROR = (byte) 0xBA;

    private void stopECGMode(){
        Log.d(CLASS, "CMD: " + CMD_STOP_ECG_MODE + " stopECGMode.");
        byte bytes[] = {CMD_STOP_ECG_MODE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void stopECGModeResponse(byte[] bytes) {
        Log.d(CLASS, "stopECGMode bytes: " + bytesToString(bytes));
        callback.stopECGMode();

        queue.rxDone();
    }


    public static final byte CMD_DELETE_ECG_DATA = (byte) 0x97;
    public static final byte CMD_DELETE_ECG_DATA_ERROR = (byte) 0xA7;

    private void deleteECGData(){
        Log.d(CLASS,"CMD: "+CMD_DELETE_ECG_DATA+" deleteECGData.");

        byte bytes[] = {  CMD_DELETE_ECG_DATA, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void deleteECGDataResponse(byte[] bytes){
        Log.d(CLASS, "deleteECGData bytes: " + bytesToString(bytes));
        callback.deleteECGData();
        queue.rxDone();
    }


    public static final byte CMD_GET_ECG_DATA = (byte) 0x96;
    public static final byte CMD_GET_ECG_DATA_ERROR = (byte) 0xA6;

    private void getECGData(byte index) {
        Log.d(CLASS,"CMD: "+CMD_GET_ECG_DATA+" getECGData.");

        byte bytes[] = {  CMD_GET_ECG_DATA, index, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    private void getECGDataResponse(byte[] bytes) {
        Log.d(CLASS, "getECGData bytes: " + bytesToString(bytes));

        byte year = byteFromBCD(bytes[BB]);
        byte month = byteFromBCD(bytes[CC]);
        byte day = byteFromBCD(bytes[DD]);
        byte hour = byteFromBCD(bytes[EE]);
        byte minute = byteFromBCD(bytes[FF]);
        byte second = byteFromBCD(bytes[GG]);

        Calendar calendar = new GregorianCalendar(2000 + year, month - 1, day, hour, minute, second);

        int heartRate = intFromByte(bytes[HH]);

        if (year != 0 && month != 0 && day != 0 && hour != 0 && minute != 0 && second != 0 ) {
            callback.getECGData(calendar, heartRate);
        } else {
            callback.getECGData(null, heartRate);
        }

        queue.rxDone();
    }


////---- Herramientas para la comunicación

    private void sendDataWithCRC(byte[] bytes) {
        bytes[CRC] = calculateCRC(bytes);
        if (txCharacteristic!=null && bluetoothGatt != null) {
            txCharacteristic.setValue(bytes);
            bluetoothGatt.writeCharacteristic(txCharacteristic);
        }
    }

    private byte calculateCRC(byte[] bytes) {
        byte crc = 0;
        for(int i = 0; i != 15; ++i) {
            crc += bytes[i];
        }
        return crc;
    }

    private byte byteFromBCD(byte bcd) {
        byte high = (byte)((bcd & 0xf0) >> 4);
        byte low = (byte)(bcd & 0x0f);
        return (byte)((10 * high) + low);
    }

    private byte BCDFromByte(byte byteValue) {
        byte high = (byte)(byteValue / 10);
        byte low = (byte)(byteValue % 10);
        return (byte)((high << 4) + low);
    }

    private int intFromByte(byte a) {
        return a & 0xff;
    }

    private int intFrom2Bytes(byte a, byte b) {
        return 256 * (a & 0xff) + (b & 0xff);
    }

    private int intFrom3Bytes(byte a, byte b, byte c) {
        return 256 * 256 * (a & 0xff) + 256 * (b & 0xff) + (c & 0xff);
    }

    private int intFrom4Bytes(byte a, byte b, byte c, byte d) {
        return 256 * 256 * (a & 0xff) + 256 * 256 * (b & 0xff) + 256 * (c & 0xff) + (d & 0xff);
    }

     private byte[] BCDBytesFromDate(Calendar calendar) {
        byte[] BCDArray = new byte[6];
        BCDArray[0] = BCDFromByte((byte)(calendar.get(Calendar.YEAR)-2000));
        BCDArray[1] = BCDFromByte((byte)(calendar.get(Calendar.MONTH)+1));
        BCDArray[2] = BCDFromByte((byte)calendar.get(Calendar.DAY_OF_MONTH));
        BCDArray[3] = BCDFromByte((byte)calendar.get(Calendar.HOUR_OF_DAY));
        BCDArray[4] = BCDFromByte((byte)calendar.get(Calendar.MINUTE));
        BCDArray[5] = BCDFromByte((byte)calendar.get(Calendar.SECOND));
        return BCDArray;
    }

    private String bytesToString(byte[] bytes) {
        String bytesString = "";
        for (int i = 0; i < bytes.length; i++) {
            bytesString += String.format("%02d" , (bytes[i] & 0xff));
            if (i != bytes.length - 1) {
                bytesString += " : ";
            }
        }
        return bytesString;
    }

    private static byte[] stringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}