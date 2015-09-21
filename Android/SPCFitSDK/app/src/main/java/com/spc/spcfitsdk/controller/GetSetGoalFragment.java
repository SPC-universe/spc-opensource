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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spc.spcfitsdk.R;
import com.spc.spcfitsdk.model.ExampleManager;
import com.spc.spcfitsdk.model.SPCFitSDK.ActivityTracker;

public class GetSetGoalFragment extends Fragment {

    public static final String CLASS = "GetSetGoalFragment";

    private static final int MIN_GOAL = 5000;

    private TextView getGoalTV;
    private TextView setGoalTV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_set_goal, container, false);

        getGoalTV = (TextView) view.findViewById(R.id.getGoalTV);

        Button getGoalB = (Button) view.findViewById(R.id.getGoalB);
        getGoalB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ShowDeviceActivity) getActivity()).connected) {
                    ((ShowDeviceActivity) getActivity()).getExampleManager().getActivityTracker().getTargetSteps(ActivityTracker.LOW_PRIORITY);
                } else {
                    Toast.makeText(getActivity(), "Disconnected from the device", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setGoalTV = (TextView) view.findViewById(R.id.setGoalTV);
        setGoalTV.setText(Integer.toString(MIN_GOAL));

        SeekBar goalSB = (SeekBar) view.findViewById(R.id.goalSB);
        goalSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = MIN_GOAL;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress + MIN_GOAL;
                setGoalTV.setText(Integer.toString(progressChanged));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button setGoalB = (Button) view.findViewById(R.id.setGoalB);
        setGoalB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ShowDeviceActivity) getActivity()).connected) {
                    ((ShowDeviceActivity) getActivity()).getExampleManager().getActivityTracker().setTargetSteps(Integer.parseInt(setGoalTV.getText().toString()), ActivityTracker.LOW_PRIORITY);
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
        intentFilter.addAction(ExampleManager.GET_TARGET_STEPS_RESPONSE);
        intentFilter.addAction(ExampleManager.SET_TARGET_STEPS_RESPONSE);
        return intentFilter;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ExampleManager.GET_TARGET_STEPS_RESPONSE:
                    Bundle bundle = intent.getExtras();
                    getGoalTV.setText(Integer.toString(bundle.getInt("goal")));
                    break;
                case ExampleManager.SET_TARGET_STEPS_RESPONSE:
                    Toast.makeText(GetSetGoalFragment.this.getActivity(), "Goal setted", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
}
