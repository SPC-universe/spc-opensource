package com.spc.spcfitsdk.controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import com.spc.spcfitsdk.model.ExampleManager;
import com.spc.spcfitsdk.model.SPCFitSDK.ActivityTrackerManager;

import java.util.ArrayList;

public class MainActivity extends Activity {

    public static final String CLASS = "MainActivity";

    public static final int REQUEST_BLUETOOTH_RESPONSE = 3000;

    private ListView devicesLV;
    private DeviceListAdapter deviceListAdapter;

    private ExampleManager exampleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exampleManager = ExampleManager.getInstance(getApplicationContext());

        Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceListAdapter.clear();
                if (!exampleManager.getActivityTrackerManager().foundDevices()) {
                    sendBroadcast(new Intent(ActivityTrackerManager.REQUEST_BLUETOOTH));
                }
            }
        });

        devicesLV=(ListView)findViewById(R.id.dispositivesLV);

        devicesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ArrayList <String> device = deviceListAdapter.getDevice(position);
                if (device != null){
                    final Intent intent = new Intent(MainActivity.this, ShowDeviceActivity.class);
                    intent.putExtra("address", device.get(0));
                    intent.putExtra("serialNumber", device.get(1));
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
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_BLUETOOTH_RESPONSE && resultCode == Activity.RESULT_CANCELED)) {
            exampleManager.getActivityTrackerManager().foundDevices();
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    private class DeviceListAdapter extends BaseAdapter {
        private ArrayList<ArrayList <String>> devices;
        private LayoutInflater inflater;

        public DeviceListAdapter() {
            super();
            devices = new ArrayList<>();
            inflater = MainActivity.this.getLayoutInflater();
        }

        public void addDevice(String address, String serialNumber) {
            ArrayList <String> device = new ArrayList<>();
            device.add(address);
            device.add(serialNumber);
            if(!devices.contains(device)) {
                devices.add(device);
            }
        }

        public ArrayList<String> getDevice(int position) {
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
            if (view == null) {
                view = inflater.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            ArrayList<String> device = devices.get(i);
            final String deviceName = device.get(0);
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
            } else {
                viewHolder.deviceName.setText(R.string.unknown_device);
            }
            viewHolder.deviceAddress.setText(device.get(1));

            return view;
        }
    }

    private static IntentFilter receiverIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActivityTrackerManager.NEW_DEVICE_FOUND);
        intentFilter.addAction(ActivityTrackerManager.REQUEST_BLUETOOTH);
        intentFilter.addAction(ActivityTrackerManager.NO_BLUETOOTH);
        intentFilter.addAction(ActivityTrackerManager.NO_BLE);
        return intentFilter;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            switch (action) {
                case ActivityTrackerManager.NEW_DEVICE_FOUND:
                    String address = intent.getExtras().getString("address");
                    String serialNumber = intent.getExtras().getString("serialNumber");
                    deviceListAdapter.addDevice(address, serialNumber);
                    deviceListAdapter.notifyDataSetChanged();
                    break;
                case ActivityTrackerManager.REQUEST_BLUETOOTH:
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_BLUETOOTH_RESPONSE);
                    break;
                case ActivityTrackerManager.NO_BLUETOOTH:
                    Toast.makeText(MainActivity.this, "There is no Bluetooth", Toast.LENGTH_SHORT).show();
                    break;
                case ActivityTrackerManager.NO_BLE:
                    Toast.makeText(MainActivity.this, "There is no Bluetooth Low Energy", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
