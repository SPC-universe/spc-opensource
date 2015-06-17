package com.spc.spcfitsdk.fragments;

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
import com.spc.spcfitsdk.activities.ShowDeviceActivity;
import com.spc.spcfitsdk.activityTracker.ActivityTracker;


public class RealTimeMeterModeFragment extends Fragment {

    public static final String CLASS = "RealTimeMeterMode";

    private TextView stepsTV;
    private TextView aStepsTV;
    private TextView caloriesTV;
    private TextView KMTV;
    private TextView ATTV;
    private Button startB;

    public static RealTimeMeterModeFragment newInstance() {
        return new RealTimeMeterModeFragment();
    }

    public RealTimeMeterModeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_real_time_meter_mode, container, false);

        stepsTV=(TextView) view.findViewById(R.id.stepsTV);
        aStepsTV=(TextView) view.findViewById(R.id.aStepsTV);
        caloriesTV=(TextView) view.findViewById(R.id.caloriesTV);
        KMTV=(TextView) view.findViewById(R.id.KMTV);
        ATTV=(TextView) view.findViewById(R.id.ATTV);

        startB=(Button) view.findViewById(R.id.startB);
        startB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ShowDeviceActivity)getActivity()).connected) {
                    ((ShowDeviceActivity)getActivity()).activityTrackerManager.startRealTimeMeterMode();
                } else {
                    Toast.makeText(getActivity(), "Disconnected from device", Toast.LENGTH_SHORT).show();
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
        intentFilter.addAction(ActivityTracker.BLE_REAL_TIME_METER_MODE_RESPONSE);
        return intentFilter;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
//            Log.d(CLASS, action);
            if (ActivityTracker.BLE_REAL_TIME_METER_MODE_RESPONSE.equals(action)) {
                Bundle bundle=intent.getExtras();
                stepsTV.setText(Integer.toString(bundle.getInt("steps")));
                aStepsTV.setText(Integer.toString(bundle.getInt("aerobicSteps")));
                caloriesTV.setText(Integer.toString(bundle.getInt("cal")));
                KMTV.setText(Integer.toString(bundle.getInt("km")));
                ATTV.setText(Integer.toString(bundle.getInt("activityTime")));
            }
        }
    };

}
