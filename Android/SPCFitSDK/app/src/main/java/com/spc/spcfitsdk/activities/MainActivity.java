package com.spc.spcfitsdk.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spc.spcfitsdk.R;
import com.spc.spcfitsdk.activityTracker.ActivityTracker;
import com.spc.spcfitsdk.activityTracker.ActivityTrackerManager;

import java.util.ArrayList;


public class MainActivity extends Activity {

    public static final String CLASS = "MainActivity";

    private Button searchButton;
    private ListView devicesLV;
    private DeviceListAdapter deviceListAdapter;

    private ActivityTrackerManager activityTrackerManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityTrackerManager = ActivityTrackerManager.getInstance(getApplicationContext());

        searchButton=(Button)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceListAdapter.clear();
                activityTrackerManager.findDevices();
            }
        });


        devicesLV=(ListView)findViewById(R.id.dispositivesLV);

        devicesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = deviceListAdapter.getDevice(position);
                if (device != null){
                    activityTrackerManager.stopFindingDevices();
                    final Intent intent = new Intent(MainActivity.this, ShowDeviceActivity.class);
                    intent.putExtra("device", device);
                    startActivity(intent);
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver,receiverIntentFilter());
        deviceListAdapter = new DeviceListAdapter();
        devicesLV.setAdapter(deviceListAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static IntentFilter receiverIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActivityTracker.BLE_NEW_DEVICE);
        intentFilter.addAction(ActivityTracker.BLE_CHECK_BLUETOOTH);
        intentFilter.addAction(ActivityTracker.BLE_NO_BLUETOOTH);
        intentFilter.addAction(ActivityTracker.BLE_NO_BLE);
        return intentFilter;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
//            Log.d(CLASS, action);
            if (ActivityTracker.BLE_NEW_DEVICE.equals(action)) {
                deviceListAdapter.addDevice((BluetoothDevice)intent.getParcelableExtra("device"));
                deviceListAdapter.notifyDataSetChanged();
            } else {
                if (ActivityTracker.BLE_CHECK_BLUETOOTH.equals(action)) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 0);
                } else {
                    if (ActivityTracker.BLE_NO_BLUETOOTH.equals(action)) {
                        Toast.makeText(MainActivity.this, "There is no Bluetooth", Toast.LENGTH_SHORT).show();
                    } else {
                        if (ActivityTracker.BLE_NO_BLE.equals(action)) {
                            Toast.makeText(MainActivity.this, "There is no Bluetooth Low Energy", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    };



    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    private class DeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> devices;
        private LayoutInflater inflater;

        public DeviceListAdapter() {
            super();
            devices = new ArrayList<>();
            inflater = MainActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!devices.contains(device)) {
                devices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return devices.get(position);
        }

        public void clear() {
            devices.clear();
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int i) {
            return devices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = inflater.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = devices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

}
