package com.spc.spcfitsdk.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.spc.spcfitsdk.R;
import com.spc.spcfitsdk.model.ExampleManager;
import com.spc.spcfitsdk.model.SPCFitSDK.ActivityTracker;
import com.spc.spcfitsdk.model.SPCFitSDK.ActivityTrackerManager;


public class ShowDeviceActivity extends FragmentActivity {

    public static final String CLASS = "ShowDeviceActivity";

    public static final int REQUEST_BLUETOOTH_RESPONSE = 3000;

    private ExampleManager exampleManager;

    public ExampleManager getExampleManager() {
        return exampleManager;
    }

    String deviceAddress;
    String deviceSerialNumber;

    public boolean connected = false;

    private Button connectB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_device);

        exampleManager = ExampleManager.getInstance(getApplicationContext());

        deviceAddress = getIntent().getExtras().getString("address");
        deviceSerialNumber = getIntent().getExtras().getString("serialNumber");

        FragmentTabHost tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);

        tabHost.setup(this,
                getSupportFragmentManager(), android.R.id.tabcontent);

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

        TextView name = (TextView) findViewById(R.id.nameTV);
        name.setText(deviceSerialNumber);
        TextView address = (TextView) findViewById(R.id.addressTV);
        address.setText(deviceAddress);

        connectB = (Button)findViewById(R.id.connect);

        connectB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    disconnect();
                } else {
                    connect();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, receiverIntentFilter());
        if (exampleManager.getActivityTracker() != null && exampleManager.getActivityTracker().isConnected()){
            setButtonWhenConnect();
        } else {
            setButtonWhenDisconnect();
        }

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
        if (!(requestCode == REQUEST_BLUETOOTH_RESPONSE && resultCode == Activity.RESULT_CANCELED)) {
            connect();
        }
    }

    @Override
    public void onBackPressed() {
        exampleManager.getActivityTrackerManager().disconnect();
        super.onBackPressed();
    }

    private void connect(){
        if (!exampleManager.getActivityTrackerManager().connect(deviceAddress, deviceSerialNumber)) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_BLUETOOTH_RESPONSE);
        } else {
            connectB.setText("Connecting ...");
            connectB.setEnabled(false);
        }
    }

    private void disconnect(){
        exampleManager.getActivityTrackerManager().disconnect();
        connectB.setText("Connect");
        connectB.setEnabled(true);
        connected = false;
    }

    private void setButtonWhenConnect(){
        connectB.setText("Disconnect");
        connectB.setEnabled(true);
        connected = true;
    }

    private void setButtonWhenDisconnect(){
        connectB.setText("Connect");
        connectB.setEnabled(true);
        connected = false;
    }

    AlertDialog alertDialog;

    CountDownTimer countDownTimer;

    String action = "";

    public void showCountDown (){

        switch (exampleManager.getActivityTracker().getModel()){
            case ActivityTracker.SPC_FIT:
                action = "mueva la pulsera 2 veces";
                break;
            case ActivityTracker.SPC_FIT_PULSE:
                action = "pulse la pantalla";
                break;
        }

        alertDialog = new AlertDialog.Builder(this)
                .setMessage("Para vincular, " + action + " antes de 10s")
                .setCancelable(false)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        exampleManager.getActivityTrackerManager().disconnect();
                    }
                })
                .show();

        final int interval = 1000;

        countDownTimer = new CountDownTimer(10000, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                alertDialog.setMessage("Para vincular, " +ShowDeviceActivity.this.action + " antes de "+ (millisUntilFinished/interval)+"s");
            }

            @Override
            public void onFinish() {
                alertDialog.dismiss();
                alertDialog = new AlertDialog.Builder(ShowDeviceActivity.this)
                        .setMessage("El tiempo se ha acabado, ¿Desea volver a intentarlo?")
                        .setCancelable(false)
                        .setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                exampleManager.getActivityTracker().safeBondingSendPassword("000001", ActivityTracker.HIGH_PRIORITY);
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                exampleManager.getActivityTrackerManager().disconnect();
                            }
                        })
                        .show();
            }
        }.start();
    }

    public void hideCountDown() {
        if(countDownTimer != null){
            countDownTimer.cancel();
        }
        if(alertDialog != null ) {
            alertDialog.dismiss();
        }
    }

    private static IntentFilter receiverIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActivityTrackerManager.DEVICE_CONNECTED);
        intentFilter.addAction(ActivityTrackerManager.DEVICE_DISCONNECTED);
        intentFilter.addAction(ExampleManager.SHOW_COUNTDOWN);
        intentFilter.addAction(ExampleManager.HIDE_COUNTDOWN);
        return intentFilter;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ActivityTrackerManager.DEVICE_CONNECTED:
                    setButtonWhenConnect();
                    break;
                case ActivityTrackerManager.DEVICE_DISCONNECTED:
                    setButtonWhenDisconnect();
                    break;
                case ExampleManager.SHOW_COUNTDOWN:
                    showCountDown();
                    break;
                case ExampleManager.HIDE_COUNTDOWN:
                    hideCountDown();
                    break;
            }
        }
    };

}
