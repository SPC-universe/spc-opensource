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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.spc.spcfitsdk.R;
import com.spc.spcfitsdk.model.ExampleManager;
import com.spc.spcfitsdk.model.SPCFitSDK.ActivityTracker;


public class GetSetPersonalInfoFragment extends Fragment {

    public static final String CLASS = "GetSetPersonalInfoFragment";

    private View view;

    private TextView sexTV;
    private TextView ageTV;
    private TextView heightTV;
    private TextView weightTV;
    private TextView strideTV;

    private Switch sexS;
    private EditText ageET;
    private EditText heightET;
    private EditText weightET;
    private EditText strideET;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_get_set_personal_info, container, false);

        sexTV = (TextView)view.findViewById(R.id.sexTV);
        ageTV = (TextView)view.findViewById(R.id.ageTV);
        heightTV = (TextView)view.findViewById(R.id.heightTV);
        weightTV = (TextView)view.findViewById(R.id.weightTV);
        strideTV = (TextView)view.findViewById(R.id.strideTV);

        Button getInfoB = (Button) view.findViewById(R.id.getInfoB);
        getInfoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ShowDeviceActivity) getActivity()).connected) {
                    ((ShowDeviceActivity) getActivity()).getExampleManager().getActivityTracker().getPersonalInformation(ActivityTracker.LOW_PRIORITY);
                } else {
                    Toast.makeText(getActivity(), "Disconnected from the device", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sexS = (Switch)view.findViewById(R.id.sexS);
        ageET = (EditText)view.findViewById(R.id.ageET);
        heightET = (EditText)view.findViewById(R.id.heightET);
        weightET = (EditText)view.findViewById(R.id.weightET);
        strideET = (EditText)view.findViewById(R.id.strideET);

        Button setInfoB = (Button) view.findViewById(R.id.setInfoB);
        setInfoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                if (((ShowDeviceActivity) getActivity()).connected) {
                    byte sex;
                    if (sexS.isChecked()) {
                        sex = (byte) 1;
                    } else {
                        sex = (byte) 0;
                    }
                    try {
                        ((ShowDeviceActivity) getActivity()).getExampleManager().getActivityTracker().setPersonalInformation(
                                sex,
                                (byte) Integer.parseInt(ageET.getText().toString()),
                                (byte) Integer.parseInt(heightET.getText().toString()),
                                (byte) Integer.parseInt(weightET.getText().toString()),
                                (byte) Integer.parseInt(strideET.getText().toString()),
                                ActivityTracker.LOW_PRIORITY);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getActivity(), "Fill correctly the blanks", Toast.LENGTH_SHORT).show();
                    }
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
        intentFilter.addAction(ExampleManager.GET_PERSONAL_INFORMATION_RESPONSE);
        intentFilter.addAction(ExampleManager.SET_PERSONAL_INFORMATION_RESPONSE);
        return intentFilter;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ExampleManager.GET_PERSONAL_INFORMATION_RESPONSE:
                    Bundle bundle = intent.getExtras();
                    ageTV.setText(Integer.toString(bundle.getInt("age")));
                    heightTV.setText(Integer.toString(bundle.getInt("height")));
                    weightTV.setText(Integer.toString(bundle.getInt("weight")));
                    strideTV.setText(Integer.toString(bundle.getInt("stride")));
                    int sex = bundle.getInt("male");
                    if (sex == 1) {
                        sexTV.setText("Man");
                    } else {
                        sexTV.setText("Woman");
                    }
                    break;
                case ExampleManager.SET_PERSONAL_INFORMATION_RESPONSE:
                    Toast.makeText(GetSetPersonalInfoFragment.this.getActivity(), "Personal information setted", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
}
