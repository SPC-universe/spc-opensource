package com.spc.spcfitsdk.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.spc.spcfitsdk.R;
import com.spc.spcfitsdk.model.ExampleManager;
import com.spc.spcfitsdk.model.SPCFitSDK.ActivityTracker;


public class RealTimeMeterModeFragment extends Fragment {

    public static final String CLASS = "RealTimeMeterMode";

    private TextView stepsTV;
    private TextView aerobicStepsTV;
    private TextView caloriesTV;
    private TextView distanceTV;
    private TextView activityTimeTV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_real_time_meter_mode, container, false);

        stepsTV = (TextView) view.findViewById(R.id.stepsTV);
        aerobicStepsTV = (TextView) view.findViewById(R.id.aStepsTV);
        caloriesTV = (TextView) view.findViewById(R.id.caloriesTV);
        distanceTV = (TextView) view.findViewById(R.id.KMTV);
        activityTimeTV = (TextView) view.findViewById(R.id.ATTV);

        Button startB = (Button) view.findViewById(R.id.startB);
        startB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ShowDeviceActivity) getActivity()).connected) {
                    ((ShowDeviceActivity) getActivity()).getExampleManager().getActivityTracker().startRealTimeMeterMode(ActivityTracker.LOW_PRIORITY);
                } else {
                    Toast.makeText(getActivity(), "Disconnected from the device", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, receiverIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    private static IntentFilter receiverIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ExampleManager.REAL_TIME_METER_MODE_RESPONSE);
        return intentFilter;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ExampleManager.REAL_TIME_METER_MODE_RESPONSE:
                    Bundle bundle = intent.getExtras();
                    stepsTV.setText(Integer.toString(bundle.getInt("steps")));
                    aerobicStepsTV.setText(Integer.toString(bundle.getInt("aerobicSteps")));
                    caloriesTV.setText(Integer.toString(bundle.getInt("cal")));
                    distanceTV.setText(Integer.toString(bundle.getInt("km")));
                    activityTimeTV.setText(Integer.toString(bundle.getInt("activityTime")));
                    break;
            }
        }
    };

}
