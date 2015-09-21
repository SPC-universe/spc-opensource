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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GetSetTimeFragment extends Fragment {

    public static final String CLASS = "GetSetTimeFragment";

    private TextView getTimeTV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_set_time, container, false);

        getTimeTV = (TextView) view.findViewById(R.id.getTimeTV);

        Button setTimeB = (Button) view.findViewById(R.id.setTimeB);
        setTimeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ShowDeviceActivity) getActivity()).connected) {
                    ((ShowDeviceActivity) getActivity()).getExampleManager().getActivityTracker().setTime(Calendar.getInstance(), ActivityTracker.LOW_PRIORITY);
                } else {
                    Toast.makeText(getActivity(), "Disconnected from the device", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button getTimeB = (Button) view.findViewById(R.id.getTimeB);
        getTimeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ShowDeviceActivity) getActivity()).connected) {
                    ((ShowDeviceActivity) getActivity()).getExampleManager().getActivityTracker().getTime(ActivityTracker.LOW_PRIORITY);
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
        intentFilter.addAction(ExampleManager.GET_TIME_RESPONSE);
        intentFilter.addAction(ExampleManager.SET_TIME_RESPONSE);
        return intentFilter;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ExampleManager.GET_TIME_RESPONSE:
                    Calendar calendar = (Calendar) intent.getSerializableExtra("calendar");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss", Locale.getDefault());
                    getTimeTV.setText(sdf.format(calendar.getTime()));
                    break;
                case ExampleManager.SET_TIME_RESPONSE:
                    Toast.makeText(GetSetTimeFragment.this.getActivity(), "Time setted", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

}
