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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GetSetTimeFragment extends Fragment {

    public static final String CLASS = "GetSetTimeFragment";

    private View view;

    private TextView getTimeTV;
    private Button getTimeB;

    private Button setTimeB;

    public static GetSetTimeFragment newInstance() {
        return new GetSetTimeFragment();
    }

    public GetSetTimeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.fragment_get_set_time, container, false);

        getTimeTV=(TextView)view.findViewById(R.id.getTimeTV);

        setTimeB=(Button)view.findViewById(R.id.setTimeB);
        setTimeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ShowDeviceActivity)getActivity()).connected) {
                    ((ShowDeviceActivity)getActivity()).activityTrackerManager.setTime(Calendar.getInstance());
                } else {
                    Toast.makeText(getActivity(), "Disconnected from device", Toast.LENGTH_SHORT).show();
                }
            }
        });
        getTimeB=(Button)view.findViewById(R.id.getTimeB);
        getTimeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ShowDeviceActivity)getActivity()).connected) {
                    ((ShowDeviceActivity)getActivity()).activityTrackerManager.getTime();
                } else {
                    Toast.makeText(getActivity(),"Disconnected from device",Toast.LENGTH_SHORT).show();
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
        intentFilter.addAction(ActivityTracker.BLE_GET_TIME_RESPONSE);
        return intentFilter;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
//        Log.d(CLASS, action);
        if (ActivityTracker.BLE_GET_TIME_RESPONSE.equals(action)) {
            Calendar calendar=(Calendar)intent.getSerializableExtra("calendar");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
            getTimeTV.setText(sdf.format(calendar.getTime()));
        }
        }
    };

}
