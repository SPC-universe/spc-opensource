package com.spc.spcfitsdk.controller;

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
import com.spc.spcfitsdk.model.ExampleManager;
import com.spc.spcfitsdk.model.SPCFitSDK.ActivityTracker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

public class DetailInfoFragment extends Fragment {

    public static final String CLASS = "DetailInfoFragment";

    private View view;

    private EditText dayET;

    private TextView noDataTV;

    private TableLayout activityTL;
    private TableLayout sleepQualityTL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_detail_info, container, false);

        dayET = (EditText)view.findViewById(R.id.dayET);

        noDataTV = (TextView)view.findViewById(R.id.noDataTV);

        Button getDetailInfoB = (Button) view.findViewById(R.id.getDetailInfoB);
        getDetailInfoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                if (((ShowDeviceActivity) getActivity()).connected) {
                    resetData();
                    try {
                        ((ShowDeviceActivity) getActivity()).getExampleManager().getActivityTracker().getDetailActivityData((byte) Integer.parseInt(dayET.getText().toString()), ActivityTracker.LOW_PRIORITY);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getActivity(), "Fill correctly the blanks", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Disconnected from the device", Toast.LENGTH_SHORT).show();
                }
            }
        });

        activityTL = (TableLayout)view.findViewById(R.id.activityTL);
        sleepQualityTL = (TableLayout)view.findViewById(R.id.sleepQualityTL);

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

    private static IntentFilter receiverIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ExampleManager.GET_DETAIL_ACTIVITY_DATA_RESPONSE_WITHOUT_DATA);
        intentFilter.addAction(ExampleManager.GET_DETAIL_ACTIVITY_DATA_RESPONSE_ACTIVITY_DATA);
        intentFilter.addAction(ExampleManager.GET_DETAIL_ACTIVITY_DATA_RESPONSE_SLEEP_QUALITY);
        return intentFilter;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ExampleManager.GET_DETAIL_ACTIVITY_DATA_RESPONSE_WITHOUT_DATA:
                    noDataTV.setVisibility(View.VISIBLE);
                    break;
                case ExampleManager.GET_DETAIL_ACTIVITY_DATA_RESPONSE_ACTIVITY_DATA:
                    Bundle bundle = intent.getExtras();
                    Calendar calendar = (Calendar) intent.getSerializableExtra("calendar");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm", Locale.getDefault());
                    String[] params = {
                            sdf.format(calendar.getTime()),
                            Integer.toString(bundle.getInt("steps")),
                            Integer.toString(bundle.getInt("aerobicSteps")),
                            Integer.toString(bundle.getInt("cal")),
                            Integer.toString(bundle.getInt("km"))
                    };
                    addTableRow(params, activityTL);
                    break;
                case ExampleManager.GET_DETAIL_ACTIVITY_DATA_RESPONSE_SLEEP_QUALITY:
                    HashMap<Calendar, Integer> hashMap = (HashMap<Calendar, Integer>) intent.getSerializableExtra("hashMap");

                    SortedSet<Calendar> keys = new TreeSet<>(hashMap.keySet());

                    for (Calendar key : keys) {
                        sdf = new SimpleDateFormat("yyyy MMM dd HH:mm", Locale.getDefault());
                        params = new String[]{
                                sdf.format(key.getTime()),
                                Integer.toString(hashMap.get(key))
                        };
                        addTableRow(params, sleepQualityTL);
                    }
                    break;
            }

        }
    };
}
