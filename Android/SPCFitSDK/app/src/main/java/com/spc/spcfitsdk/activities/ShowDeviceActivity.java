package com.spc.spcfitsdk.activities;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.spc.spcfitsdk.R;
import com.spc.spcfitsdk.activityTracker.ActivityTracker;
import com.spc.spcfitsdk.activityTracker.ActivityTrackerManager;
import com.spc.spcfitsdk.fragments.DetailInfoFragment;
import com.spc.spcfitsdk.fragments.GetSetGoalFragment;
import com.spc.spcfitsdk.fragments.GetSetPersonalInfoFragment;
import com.spc.spcfitsdk.fragments.GetSetTimeFragment;
import com.spc.spcfitsdk.fragments.RealTimeMeterModeFragment;


public class ShowDeviceActivity extends FragmentActivity {

    public static final String CLASS = "ShowDeviceActivity";

    public ActivityTrackerManager activityTrackerManager;

    private BluetoothDevice device;

    public boolean connected=false;

    private TextView name;
    private TextView address;
    private Button connect;




    private FragmentTabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_device);

        device=getIntent().getParcelableExtra("device");

        activityTrackerManager = ActivityTrackerManager.getInstance(getApplicationContext());
        activityTrackerManager.connectToDevice(device);

        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);

        tabHost.setup(this,
                getSupportFragmentManager(),android.R.id.tabcontent);

        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("Time"),
                GetSetTimeFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("Real Time"),
                RealTimeMeterModeFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("Personal Info"),
                GetSetPersonalInfoFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("tab4").setIndicator("Goal"),
                GetSetGoalFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("tab5").setIndicator("Detail Info"),
                DetailInfoFragment.class, null);

        tabHost.getTabWidget().setStripEnabled(false);
        tabHost.getTabWidget().setDividerDrawable(null);

        name=(TextView)findViewById(R.id.nameTV);
        name.setText(device.getName());
        address=(TextView)findViewById(R.id.addressTV);
        address.setText(device.getAddress());

        connect=(Button)findViewById(R.id.connect);
        connect.setEnabled(false);


        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                connect.setText("Conectando");
                connect.setEnabled(false);

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, receiverIntentFilter());
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

    private static IntentFilter receiverIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActivityTracker.BLE_DEVICE_CONNECTING);
        intentFilter.addAction(ActivityTracker.BLE_DEVICE_CONNECTED);
        intentFilter.addAction(ActivityTracker.BLE_DEVICE_DISCONNECTED);
        return intentFilter;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
//        Log.d(CLASS, action);
        if (ActivityTracker.BLE_DEVICE_CONNECTED.equals(action)) {
            connect.setText("Conectado");
            connected=true;
        } else {
            if (ActivityTracker.BLE_DEVICE_DISCONNECTED.equals(action)) {
                connect.setText("Desconectado");
                connect.setEnabled(true);
                connected=false;
            } else {
                if (ActivityTracker.BLE_DEVICE_CONNECTING.equals(action)) {
                    connect.setText("Conectando...");
                    connect.setEnabled(false);
                    connected=false;
                }
            }
        }
        }
    };

    @Override
    public void onBackPressed() {
        activityTrackerManager.disconnect();
        super.onBackPressed();
    }
}
