package com.spc.spcfitsdk.activityTracker;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

public class ActivityTracker extends Service {

    public static final String CLASS = "Activity Tracker";

    public static final String BLE_NEW_DEVICE = "BLE_NEW_DEVICE";
    public static final String BLE_CHECK_BLUETOOTH = "BLE_CHECK_BLUETOOTH";
    public static final String BLE_NO_BLUETOOTH = "BLE_NO_BLUETOOTH";
    public static final String BLE_NO_BLE = "BLE_NO_BLE";
    public static final String BLE_DEVICE_CONNECTED = "BLE_DEVICE_CONNECTED";
    public static final String BLE_DEVICE_CONNECTING = "BLE_DEVICE_CONNECTING";
    public static final String BLE_DEVICE_DISCONNECTED = "BLE_DEVICE_DISCONNECTED";

    public static String PEDOMETER_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static String PEDOMETER_CHARACTERISTIC_TX = "0000fff6-0000-1000-8000-00805f9b34fb";
    public static String PEDOMETER_CHARACTERISTIC_RX = "0000fff7-0000-1000-8000-00805f9b34fb";


    private static final long SCAN_PERIOD = 10000;
    private boolean scanning ;

    private final IBinder mBinder = new LocalBinder();

    private Handler handler;

    private BluetoothDevice device;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic txCharacteristic;
    private BluetoothGattCharacteristic rxCharacteristic;



    public ActivityTracker()
    {
    }

    public class LocalBinder extends Binder
    {
        public ActivityTracker getService() {
            return ActivityTracker.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        checkBLE();
        Log.d(CLASS, "Service started.");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(CLASS,"Service destroyed.");
    }

    private void sendNewDevice(BluetoothDevice device)
    {
        final Intent intent = new Intent(BLE_NEW_DEVICE);
        intent.putExtra("device",device);
        sendBroadcast(intent);
    }

    private void sendAction(final String action)
    {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public void checkBLE()
    {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            sendAction(BLE_NO_BLE);
        }else{
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();

            if (bluetoothAdapter == null) {
                sendAction(BLE_NO_BLUETOOTH);
            }else{
                Log.d(CLASS,"BluethootLE enabled");
            }
        }
    }

    public void findDevices()
    {
        if (!scanning) {
            if (!bluetoothAdapter.isEnabled()) {
                sendAction(BLE_CHECK_BLUETOOTH);
            }else{
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopFindingDevices();
                    }
                }, SCAN_PERIOD);

                bluetoothAdapter.startLeScan(leScanCallback);
                scanning = true;
            }
        }

    }

    public void stopFindingDevices()
    {
        if (scanning) {
            bluetoothAdapter.stopLeScan(leScanCallback);
            scanning = false;
        }
    }


    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    sendNewDevice(device);
                }
            };

    public boolean connectToDeviceGatt(String address) {
        sendAction(BLE_DEVICE_CONNECTING);

        device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        return true;
    }

    public void disconnectFromDeviceGatt(){
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback(){
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectFromDeviceGatt();
                sendAction(BLE_DEVICE_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                service=bluetoothGatt.getService(UUID.fromString(PEDOMETER_SERVICE));
                if (service != null){
                    txCharacteristic = service.getCharacteristic(UUID.fromString(PEDOMETER_CHARACTERISTIC_TX));
                    rxCharacteristic = service.getCharacteristic(UUID.fromString(PEDOMETER_CHARACTERISTIC_RX));

//                    bluetoothGatt.setCharacteristicNotification(txCharacteristic,true);
                    bluetoothGatt.setCharacteristicNotification(rxCharacteristic,true);

//                    BluetoothGattDescriptor descriptor = rxCharacteristic.getDescriptor(UUID.fromString(DESCRIPTOR_NOTIFICATION));
//                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                    bluetoothGatt.writeDescriptor(descriptor);

                    sendAction(BLE_DEVICE_CONNECTED);
                } else {
                    Log.d(CLASS, "Doesnt exist");
                }
            } else {
                Log.w(CLASS, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic,int status)
        {
            Log.d(CLASS, "onCharacteristicRead: " + bytesToString(characteristic.getValue()));

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic,int status)
        {
            Log.d(CLASS, "onCharacteristicWrite: " + bytesToString(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic)
        {
            Log.d(CLASS, "onCharacteristicChanged: " + bytesToString(characteristic.getValue()));

            parseResponse(characteristic.getValue());
        }


    };

    private void parseResponse(byte[] bytes)
    {
        final byte cmd = bytes[CMD];
        switch (cmd & CMD_ERROR_MASK){
            case CMD_SET_TIME:
                setTimeResponse(bytes);
                break;
            case CMD_GET_TIME:
                getTimeResponse(bytes);
                break;
            case CMD_SET_PERSONAL_INFORMATION:
                setPersonalInformationResponse(bytes);
                break;
            case CMD_GET_PERSONAL_INFORMATION:
                getPersonalInformationResponse(bytes);
                break;
            case CMD_GET_TOTAL_ACTIVITY_DATA:
                getTotalActivityDataResponse(bytes);
                break;
            case CMD_GET_DETAIL_ACTIVITY_DATA:
                getDetailActivityDataResponse(bytes);
                break;
            case CMD_DELETE_ACTIVITY_DATA:
                deleteActivityDataResponse(bytes);
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
            case CMD_QUERY_DATA_STORAGE:
                queryDataStorageResponse(bytes);
                break;
            case CMD_SET_TARGET_STEPS:
                setTargetStepsResponse(bytes);
                break;
            case CMD_GET_TARGET_STEPS:
                getTargetStepsResponse(bytes);
                break;
            case CMD_GET_ACTIVITY_GOAL_ACHIEVED_RATE:
                getActivityGoalAchievedRateResponse(bytes);
                break;
        }
    }

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


    private final byte CMD_SET_TIME = 0x01;

    public void setTime(Calendar calendar)
    {
        Log.d(CLASS,"setTime");

        byte[] dateBytes = BCDBytesFromDate(calendar);

        byte bytes[] = {  CMD_SET_TIME,
                            dateBytes[0],
                            dateBytes[1],
                            dateBytes[2],
                            dateBytes[3],
                            dateBytes[4],
                            dateBytes[5],
                            0, 0, 0, 0, 0, 0, 0, 0, 0 };

        sendDataWithCRC(bytes);
    }

    public static final String BLE_SET_TIME_RESPONSE = "BLE_SET_TIME_RESPONSE";

    private void setTimeResponse(byte[] bytes)
    {
        Log.d(CLASS,"setTimeResponse bytes: "+bytesToString(bytes));
    }

    private final byte CMD_GET_TIME = 0x41;

    public void getTime()
    {
        Log.d(CLASS,"getTime.");

        byte bytes[] = {  CMD_GET_TIME, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_GET_TIME_RESPONSE = "BLE_GET_TIME_RESPONSE";

    private void getTimeResponse(byte[] bytes)
    {
        Log.d(CLASS,"getTimeResponse bytes: "+bytesToString(bytes));

        byte year = byteFromBCD(bytes[AA]);
        byte month = byteFromBCD(bytes[BB]);
        byte day = byteFromBCD(bytes[CC]);
        byte hour = byteFromBCD(bytes[DD]);
        byte minute = byteFromBCD(bytes[EE]);
        byte second = byteFromBCD(bytes[FF]);

        Calendar calendar = new GregorianCalendar(2000+year,month-1,day,hour,minute,second);

        Intent intent = new Intent(BLE_GET_TIME_RESPONSE);
        intent.putExtra("calendar",calendar);
        sendBroadcast(intent);
    }

    private final byte CMD_SET_PERSONAL_INFORMATION= 0x02;

    public void setPersonalInformation(byte male, byte age, byte height, byte weight,byte stride)
    {
        Log.d(CLASS,"setPersonalInformation.");

        byte bytes[] = {  CMD_SET_PERSONAL_INFORMATION, male, age, height, weight, stride, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_SET_PERSONAL_INFORMATION_RESPONSE = "BLE_SET_PERSONAL_INFORMATION_RESPONSE";

    private void setPersonalInformationResponse(byte[] bytes)
    {
        Log.d(CLASS,"setPersonalInformationResponse bytes: "+bytesToString(bytes));
    }

    private final byte CMD_GET_PERSONAL_INFORMATION= 0x42;

    public void getPersonalInformation()
    {
        Log.d(CLASS,"getPersonalInformation.");

        byte bytes[] = {  CMD_GET_PERSONAL_INFORMATION, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_GET_PERSONAL_INFORMATION_RESPONSE = "BLE_GET_PERSONAL_INFORMATION_RESPONSE";

    private void getPersonalInformationResponse(byte[] bytes)
    {
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

        Log.d(CLASS,"Male: "+male+" Age: "+age+" Height: "+height+" Weight: "+weight+" Stride: "+stride);

        Intent intent = new Intent(BLE_GET_PERSONAL_INFORMATION_RESPONSE);
        intent.putExtra("male", male);
        intent.putExtra("age", age);
        intent.putExtra("height", height);
        intent.putExtra("weight", weight);
        intent.putExtra("stride", stride);
        sendBroadcast(intent);
    }

    private final byte CMD_GET_TOTAL_ACTIVITY_DATA= 0x07;

    public void getTotalActivityData(byte day)
    {
        Log.d(CLASS,"getTotalActivityData.");

        byte bytes[] = {  CMD_GET_TOTAL_ACTIVITY_DATA, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_GET_TOTAL_ACTIVITY_DATA_RESPONSE_0 = "BLE_GET_TOTAL_ACTIVITY_DATA_RESPONSE_0";
    public static final String BLE_GET_TOTAL_ACTIVITY_DATA_RESPONSE_1 = "BLE_GET_TOTAL_ACTIVITY_DATA_RESPONSE_1";

    private void getTotalActivityDataResponse(byte[] bytes)
    {
        Log.d(CLASS,"getTotalActivityDataResponse bytes: "+bytesToString(bytes));

        byte dayIndex = byteFromBCD(bytes[BB]);
        byte year = byteFromBCD(bytes[CC]);
        byte month = byteFromBCD(bytes[DD]);
        byte day = byteFromBCD(bytes[EE]);

        Calendar calendar = new GregorianCalendar(year,month,day);
        if(bytes[AA] == 0){
            int steps = intFrom3Bytes(bytes[FF],bytes[GG],bytes[HH]);
            int aerobicSteps = intFrom3Bytes(bytes[II],bytes[JJ],bytes[KK]);
            int cal = intFrom3Bytes(bytes[LL],bytes[MM],bytes[NN]);

            Intent intent = new Intent(BLE_GET_TOTAL_ACTIVITY_DATA_RESPONSE_0);
            intent.putExtra("dayIndex", dayIndex);
            intent.putExtra("calendar", calendar);
            intent.putExtra("steps", steps);
            intent.putExtra("aerobicSteps", aerobicSteps);
            intent.putExtra("cal", cal);
            sendBroadcast(intent);

        }
        if(bytes[AA] == 1){
            int km = intFrom3Bytes(bytes[FF],bytes[GG],bytes[HH]);
            int activityTime = intFrom2Bytes(bytes[II],bytes[JJ]);

            Intent intent = new Intent(BLE_GET_TOTAL_ACTIVITY_DATA_RESPONSE_1);
            intent.putExtra("dayIndex", dayIndex);
            intent.putExtra("calendar", calendar);
            intent.putExtra("km", km);
            intent.putExtra("activityTime", activityTime);
            sendBroadcast(intent);
        }
    }

    private final byte CMD_GET_DETAIL_ACTIVITY_DATA = 0x43;


    private final byte D_ACTIVITY_DATA = 0x00;
    private final byte D_SLEEP_QUALITY_DATA = (byte)0xff;
    private final byte D_TYPE_INDEX = 1;
    private final byte D_WITH_DATA = (byte)0xf0;
    private final byte D_WITHOUT_DATA = (byte)0xff;
    private final byte D_AA = 2;
    private final byte D_BB = 3;
    private final byte D_CC = 4;
    private final byte D_DD = 5;
    private final byte D_EE = 6;
    private final byte D_FF = 7;
    private final byte D_GG = 8;
    private final byte D_HH = 9;
    private final byte D_II = 10;
    private final byte D_JJ = 11;
    private final byte D_KK = 12;
    private final byte D_LL = 13;
    private final byte D_MM = 14;

    public void getDetailActivityData(byte day)
    {
        Log.d(CLASS,"getDetailActivityData.");

        byte bytes[] = {  CMD_GET_DETAIL_ACTIVITY_DATA, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_WITHOUT_DATA = "BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_WITHOUT_DATA";
    public static final String BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_ACTIVITY_DATA = "BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_ACTIVITY_DATA";
    public static final String BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_SLEEP_QUALITY = "BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_SLEEP_QUALITY";

    private void getDetailActivityDataResponse(byte[] bytes)
    {
        Log.d(CLASS,"getDetailActivityDataResponse bytes: "+bytesToString(bytes));

        if (bytes[D_TYPE_INDEX] == D_WITHOUT_DATA) {
            sendAction(BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_WITHOUT_DATA);
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

                Intent intent = new Intent(BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_ACTIVITY_DATA);
                intent.putExtra("calendar", calendar);
                intent.putExtra("steps", steps);
                intent.putExtra("aerobicSteps", aerobicSteps);
                intent.putExtra("cal", cal);
                intent.putExtra("km", km);
                sendBroadcast(intent);
            }
            if (bytes[D_EE] == D_SLEEP_QUALITY_DATA) {
                for(int i = 0; i != 8; i++) {
                    int sleepQuality=bytes[D_FF + i];

                    Calendar calendar = new GregorianCalendar(2000+year, month-1, day, hour, (minute + (2 * i)), 0);

                    Intent intent = new Intent(BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_SLEEP_QUALITY);
                    intent.putExtra("calendar", calendar);
                    intent.putExtra("sleepQuality", sleepQuality);
                    sendBroadcast(intent);
                }
            }
        }
    }

    private final byte CMD_DELETE_ACTIVITY_DATA = 0x04;

    public void deleteActivityData(byte day)
    {
        Log.d(CLASS,"deleteActivityData.");

        byte bytes[] = {  CMD_DELETE_ACTIVITY_DATA, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_DELETE_ACTIVITY_DATA_RESPONSE = "BLE_DELETE_ACTIVITY_DATA_RESPONSE";

    private void deleteActivityDataResponse(byte[] bytes)
    {
        Log.d(CLASS,"deleteActivityDataResponse bytes: "+bytesToString(bytes));
    }

    private final byte CMD_START_REAL_TIME_METER_MODE_AND_UPDATES = 0x09;

    public void startRealTimeMeterMode()
    {
        Log.d(CLASS,"startRealTimeMeterMode.");

        byte bytes[] = {  CMD_START_REAL_TIME_METER_MODE_AND_UPDATES, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_REAL_TIME_METER_MODE_RESPONSE = "BLE_REAL_TIME_METER_MODE_RESPONSE";

    private void realTimeMeterModeResponse(byte[] bytes)
    {
        Log.d(CLASS,"realTimeMeterModeResponse bytes: "+bytesToString(bytes));

        int steps = intFrom3Bytes(bytes[AA], bytes[BB], bytes[CC]);
        int aerobicSteps = intFrom3Bytes(bytes[DD], bytes[EE], bytes[FF]);
        int cal = intFrom3Bytes(bytes[GG], bytes[HH], bytes[II]);
        int km = intFrom3Bytes(bytes[JJ], bytes[KK], bytes[LL]);
        int activityTime = intFrom2Bytes(bytes[MM], bytes[NN]);

        Log.d(CLASS,"Steps: "+steps+" Calories: "+cal+" KM: "+km);

        Intent intent = new Intent(BLE_REAL_TIME_METER_MODE_RESPONSE);
        intent.putExtra("steps", steps);
        intent.putExtra("aerobicSteps", aerobicSteps);
        intent.putExtra("cal", cal);
        intent.putExtra("km", km);
        intent.putExtra("activityTime", activityTime);
        sendBroadcast(intent);
    }

    private final byte CMD_STOP_REAL_TIME_METER_MODE = 0x0A;

    public void stopRealTimeMeterMode()
    {
        Log.d(CLASS,"stopRealTimeMeterMode.");

        byte bytes[] = {  CMD_STOP_REAL_TIME_METER_MODE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_STOP_REAL_TIME_METER_MODE_RESPONSE = "BLE_STOP_REAL_TIME_METER_MODE_RESPONSE";

    private void stopRealTimeMeterModeResponse(byte[] bytes)
    {
        Log.d(CLASS,"stopRealTimeMeterModeResponse bytes: "+bytesToString(bytes));
    }

    private final byte CMD_GET_CURRENT_ACTIVITY_INFORMATION = 0x48;

    public void getCurrentActivityInformation()
    {
        Log.d(CLASS,"getCurrentActivityInformation.");

        byte bytes[] = {  CMD_GET_CURRENT_ACTIVITY_INFORMATION, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_GET_CURRENT_ACTIVITY_INFORMATION_RESPONSE = "BLE_GET_CURRENT_ACTIVITY_INFORMATION_RESPONSE";

    private void getCurrentActivityInformationResponse(byte[] bytes)
    {
        Log.d(CLASS,"getCurrentActivityInformation bytes: "+bytesToString(bytes));

    }

    private final byte CMD_QUERY_DATA_STORAGE = 0x46;

    public void queryDataStorage()
    {
        Log.d(CLASS,"queryDataStorage.");

        byte bytes[] = {  CMD_QUERY_DATA_STORAGE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_QUERY_DATA_STORAGE_RESPONSE = "BLE_QUERY_DATA_STORAGE_RESPONSE";

    private void queryDataStorageResponse(byte[] bytes)
    {
        Log.d(CLASS,"queryDataStorageResponse bytes: "+bytesToString(bytes));

    }

    private final byte CMD_SET_TARGET_STEPS = 0x0B;

    public void setTargetSteps(int steps)
    {
        Log.d(CLASS,"setTargetSteps.");

        byte steps1=(byte)(0xff & (steps>>16));
        byte steps2=(byte)(0xff & (steps>>8));
        byte steps3=(byte)(0xff & steps);

        byte bytes[] = {  CMD_SET_TARGET_STEPS, steps1, steps2, steps3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_SET_TARGET_STEPS_RESPONSE = "BLE_SET_TARGET_STEPS_RESPONSE";

    private void setTargetStepsResponse(byte[] bytes)
    {
        Log.d(CLASS,"setTargetStepsResponse bytes: "+bytesToString(bytes));

    }

    private final byte CMD_GET_TARGET_STEPS = 0x4B;

    public void getTargetSteps()
    {
        Log.d(CLASS,"getTargetSteps.");

        byte bytes[] = {  CMD_GET_TARGET_STEPS, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_GET_TARGET_STEPS_RESPONSE = "BLE_GET_TARGET_STEPS_RESPONSE";

    private void getTargetStepsResponse(byte[] bytes)
    {
        Log.d(CLASS,"getTargetStepsResponse bytes: "+bytesToString(bytes));

        int steps = intFrom3Bytes(bytes[AA], bytes[BB], bytes[CC]);

        Intent intent = new Intent(BLE_GET_TARGET_STEPS_RESPONSE);
        intent.putExtra("steps", steps);
        sendBroadcast(intent);

    }

    private final byte CMD_GET_ACTIVITY_GOAL_ACHIEVED_RATE = 0x08;

    public void getActivityGoalAchievedRate(byte day)
    {
        Log.d(CLASS,"getActivityGoalAchievedRate.");

        byte bytes[] = {  CMD_GET_ACTIVITY_GOAL_ACHIEVED_RATE, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        sendDataWithCRC(bytes);
    }

    public static final String BLE_GET_ACTIVITY_GOAL_ACHIEVED_RATE_RESPONSE = "BLE_GET_ACTIVITY_GOAL_ACHIEVED_RATE_RESPONSE";

    private void getActivityGoalAchievedRateResponse(byte[] bytes)
    {
        Log.d(CLASS,"getActivityGoalAchievedRateResponse bytes: "+bytesToString(bytes));

        byte dayIndex = byteFromBCD(bytes[AA]);
        byte year = byteFromBCD(bytes[BB]);
        byte month = byteFromBCD(bytes[CC]);
        byte day = byteFromBCD(bytes[DD]);
        if (year != 0 && month != 0 && day != 0) {
            Calendar calendar = new GregorianCalendar(2000+year,month-1,day);

            byte goalAchievedRate = bytes[EE];

            int activitySpeed = intFrom2Bytes(bytes[FF], bytes[GG]);
            int ex = intFrom3Bytes(bytes[HH], bytes[II], bytes[JJ]);
            int goalFinishedPercent = intFrom2Bytes(bytes[KK], bytes[LL]);

            Intent intent = new Intent(BLE_GET_ACTIVITY_GOAL_ACHIEVED_RATE_RESPONSE);
            intent.putExtra("dayIndex", dayIndex);
            intent.putExtra("date", calendar);
            intent.putExtra("goalAchievedRate", goalAchievedRate);
            intent.putExtra("activitySpeed", activitySpeed);
            intent.putExtra("ex", ex);
            intent.putExtra("goalFinishedPercent", goalFinishedPercent);
            sendBroadcast(intent);
        }


    }

    private void sendDataWithCRC(byte[] bytes)
    {
        bytes[CRC] = calculateCRC(bytes);

        txCharacteristic.setValue(bytes);
        bluetoothGatt.writeCharacteristic(txCharacteristic);
    }

    private byte calculateCRC(byte[] bytes)
    {
        byte crc = 0;
        for(int i = 0; i != 15; ++i) {
            crc += bytes[i];
        }
        return crc;
    }

    private byte byteFromBCD(byte bcd)
    {
        byte high = (byte)((bcd & 0xf0) >> 4);
        byte low = (byte)(bcd & 0x0f);
        return (byte)((10 * high) + low);
    }

    private byte BCDFromByte(byte byteValue)
    {
        byte high = (byte)(byteValue / 10);
        byte low = (byte)(byteValue % 10);
        return (byte)((high << 4) + low);
    }

    private int intFrom3Bytes(byte a, byte b, byte c)
    {
        return 256 * 256 * (a & 0xff) + 256 * (b & 0xff) + (c & 0xff);
    }

    private int intFrom2Bytes(byte a, byte b)
    {
        return 256 * (a & 0xff) + (b & 0xff);
    }

    private byte[] BCDBytesFromDate(Calendar calendar)
    {
        byte[] BCDArray = new byte[6];
        BCDArray[0] = BCDFromByte((byte)(calendar.get(Calendar.YEAR)-2000));
        BCDArray[1] = BCDFromByte((byte)(calendar.get(Calendar.MONTH)+1));
        BCDArray[2] = BCDFromByte((byte)calendar.get(Calendar.DAY_OF_MONTH));
        BCDArray[3] = BCDFromByte((byte)calendar.get(Calendar.HOUR_OF_DAY));
        BCDArray[4] = BCDFromByte((byte)calendar.get(Calendar.MINUTE));
        BCDArray[5] = BCDFromByte((byte)calendar.get(Calendar.SECOND));
        return BCDArray;
    }

    private String bytesToString(byte[] bytes)
    {
        String bytesString = "";
        for (int i = 0; i < bytes.length; i++) {
            bytesString += String.format("%02d" , (bytes[i] & 0xff));
            if (i != bytes.length - 1) {
                bytesString += " : ";
            }
        }
        return bytesString;
    }
}
