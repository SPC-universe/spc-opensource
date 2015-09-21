package com.spc.spcfitsdk.model.SPCFitSDK;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.spc.spcfitsdk.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityTrackerManager{

    private static final String CLASS = "Activity Tracker Manager";

    public static final String REQUEST_BLUETOOTH = "REQUEST_BLUETOOTH";
    public static final String NO_BLUETOOTH = "NO_BLUETOOTH";
    public static final String NO_BLE = "NO_BLE";
    public static final String DEVICE_CONNECTED = "DEVICE_CONNECTED";
    public static final String DEVICE_DISCONNECTED = "DEVICE_DISCONNECTED";
    public static final String NEW_DEVICE_FOUND = "NEW_DEVICE_FOUND";
    public static final String DEVICE_NOT_FOUND = "DEVICE_NOT_FOUND";

    private Context context;

    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    private ActivityTrackerCallback activityTrackerCallback;

    public ActivityTrackerManager(Context context, ActivityTrackerCallback activityTrackerCallback) {
        this.context = context;
        this.activityTrackerCallback = activityTrackerCallback;

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            context.sendBroadcast(new Intent(NO_BLE));
        } else {
            if (bluetoothAdapter == null) {
                context.sendBroadcast(new Intent(NO_BLUETOOTH));
            }
        }
    }

    public boolean connect(final String address, final String serialNumber) {
        return connectToDeviceGatt(address, serialNumber);
    }

    public boolean connectToSerialNumber(final String serialNumber) {
        if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            final ActivityTrackerSeeker activityTrackerSeeker = ActivityTrackerSeeker.newInstance(ActivityTrackerSeeker.HIGH_PRIORITY);
            if (activityTrackerSeeker != null) {
                activityTrackerSeeker.startDeviceSearch(new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                        String scanAddress = device.getAddress();
                        String scanSerialNumber = device.getName();

                        if (scanSerialNumber != null && scanSerialNumber.length() > 0 && scanSerialNumber.equals(serialNumber)) {
                            activityTrackerSeeker.stopDeviceSearch();
                            connectToDeviceGatt(scanAddress, serialNumber);
                        }
                    }
                }, new ActivityTrackerSeeker.FinishCallback() {
                    @Override
                    public void onFinishWithoutResult() {
                        context.sendBroadcast(new Intent(DEVICE_NOT_FOUND));
                    }
                }, 20000);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean connectToDeviceGatt(String address, String serialNumber) {

        if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            if (bluetoothDevice!=null || bluetoothGatt != null) {
                disconnect();
            }

            bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

            ActivityTracker activityTracker = new ActivityTracker(address, serialNumber, activityTrackerCallback);

            bluetoothGatt = bluetoothDevice.connectGatt(context, false, activityTracker.getBluetoothGattCallback());
            return true;
        } else {
            return false;
        }
    }

    public void disconnect() {
        bluetoothDevice = null;
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            bluetoothGatt.disconnect();
        }
    }

    public boolean foundDevices() {
        if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            ActivityTrackerSeeker activityTrackerSeeker = ActivityTrackerSeeker.newInstance(ActivityTrackerSeeker.LOW_PRIORITY);
            if(activityTrackerSeeker != null) {
                activityTrackerSeeker.startDeviceSearch(new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                        final String deviceSerialNumber = device.getName();

                        if (deviceSerialNumber != null && deviceSerialNumber.length() > 0) {

                            String[] patterns = context.getResources().getStringArray(R.array.patterns);

                            for(String pattern: patterns){
                                Pattern patron = Pattern.compile(pattern);
                                Matcher matcher = patron.matcher(deviceSerialNumber);
                                if(matcher.find()){
                                    final Intent intent = new Intent(ActivityTrackerManager.NEW_DEVICE_FOUND);
                                    intent.putExtra("device",device);
                                    intent.putExtra("address",device.getAddress());
                                    intent.putExtra("serialNumber",device.getName());
                                    intent.putExtra("type",device.getType());
                                    context.sendBroadcast(intent);
                                }
                            }
                        }
                    }
                }, null, 20000);
            }
            return true;
        } else {
            return false;
        }
    }
}
