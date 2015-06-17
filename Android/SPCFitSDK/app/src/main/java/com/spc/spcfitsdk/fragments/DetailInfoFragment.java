package com.spc.spcfitsdk.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.spc.spcfitsdk.R;
import com.spc.spcfitsdk.activities.ShowDeviceActivity;
import com.spc.spcfitsdk.activityTracker.ActivityTracker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DetailInfoFragment extends Fragment {

    public static final String CLASS = "DetailInfoFragment";

    private View view;

    private EditText dayET;
    private Button getDetailInfoB;

    private TextView noDataTV;

    private TableLayout activityTL;
    private TableLayout sleepQualityTL;



    public static DetailInfoFragment newInstance() {
        return new DetailInfoFragment();
    }

    public DetailInfoFragment() {
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
        view=inflater.inflate(R.layout.fragment_detail_info, container, false);

        dayET=(EditText)view.findViewById(R.id.dayET);

        noDataTV=(TextView)view.findViewById(R.id.noDataTV);

        getDetailInfoB=(Button)view.findViewById(R.id.getDetailInfoB);
        getDetailInfoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                if (((ShowDeviceActivity)getActivity()).connected) {
                    resetData();
                    try{
                        ((ShowDeviceActivity)getActivity()).activityTrackerManager.getDetailActivityData((byte)Integer.parseInt(dayET.getText().toString()));
                    }catch (NumberFormatException e){
                        Toast.makeText(getActivity(), "Fill in the blanks", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Disconnected from device", Toast.LENGTH_SHORT).show();
                }
            }
        });

        activityTL=(TableLayout)view.findViewById(R.id.activityTL);
        sleepQualityTL=(TableLayout)view.findViewById(R.id.sleepQualityTL);

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
        intentFilter.addAction(ActivityTracker.BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_WITHOUT_DATA);
        intentFilter.addAction(ActivityTracker.BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_ACTIVITY_DATA);
        intentFilter.addAction(ActivityTracker.BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_SLEEP_QUALITY);
        return intentFilter;
    }

    private void resetData(){
        noDataTV.setVisibility(View.GONE);
        activityTL.removeViews(1, activityTL.getChildCount() - 1);
        sleepQualityTL.removeViews(1, sleepQualityTL.getChildCount() - 1);
    }

    private void addTableRow(String[] params, TableLayout tableLayout){

        TableRow tr = new TableRow(view.getContext());

        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));


        for (int i = 0; i <params.length ; i++) {
            TextView textView= new TextView(view.getContext());
            textView.setText(params[i]);
            textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            textView.setLayoutParams(new TableRow.LayoutParams(i+1));

            int paddingPixel = 3;
            float density = view.getResources().getDisplayMetrics().density;
            int paddingDp = (int)(paddingPixel * density);
            textView.setPadding(0,paddingDp,0,0);

            textView.setGravity(Gravity.CENTER);

            tr.addView(textView);
        }

        tableLayout.addView(tr);

    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
//        Log.d(CLASS, action);
        if (ActivityTracker.BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_WITHOUT_DATA.equals(action)) {
            noDataTV.setVisibility(View.VISIBLE);
        } else {
            if (ActivityTracker.BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_ACTIVITY_DATA.equals(action)) {
                Bundle bundle = intent.getExtras();
                Calendar calendar=(Calendar)intent.getSerializableExtra("calendar");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm");
                String[] params ={
                        sdf.format(calendar.getTime()),
                        Integer.toString(bundle.getInt("steps")),
                        Integer.toString(bundle.getInt("aerobicSteps")),
                        Integer.toString(bundle.getInt("cal")),
                        Integer.toString(bundle.getInt("km"))
                };
                addTableRow(params,activityTL);
            } else {
                if (ActivityTracker.BLE_GET_DETAIL_ACTIVITY_DATA_RESPONSE_SLEEP_QUALITY.equals(action)) {
                    Bundle bundle = intent.getExtras();

                    Calendar calendar=(Calendar)intent.getSerializableExtra("calendar");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm");
                    String[] params ={
                            sdf.format(calendar.getTime()),
                            Integer.toString(bundle.getInt("sleepQuality"))
                    };
                    addTableRow(params,sleepQualityTL);
                }
            }

        }
        }
    };
}
