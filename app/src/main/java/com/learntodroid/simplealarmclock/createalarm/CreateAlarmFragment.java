package com.learntodroid.simplealarmclock.createalarm;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.learntodroid.simplealarmclock.R;
import com.learntodroid.simplealarmclock.activities.MainActivity;
import com.learntodroid.simplealarmclock.alarmslist.AlarmsListFragment;
import com.learntodroid.simplealarmclock.data.Alarm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateAlarmFragment extends Fragment {
    @BindView(R.id.fragment_createalarm_timePicker) TimePicker timePicker;
    @BindView(R.id.fragment_createalarm_title) EditText title;
    @BindView(R.id.fragment_createalarm_scheduleAlarm) Button scheduleAlarm;
    @BindView(R.id.fragment_createalarm_recurring) CheckBox recurring;
    @BindView(R.id.fragment_createalarm_checkMon) CheckBox mon;
    @BindView(R.id.fragment_createalarm_checkTue) CheckBox tue;
    @BindView(R.id.fragment_createalarm_checkWed) CheckBox wed;
    @BindView(R.id.fragment_createalarm_checkThu) CheckBox thu;
    @BindView(R.id.fragment_createalarm_checkFri) CheckBox fri;
    @BindView(R.id.fragment_createalarm_checkSat) CheckBox sat;
    @BindView(R.id.fragment_createalarm_checkSun) CheckBox sun;
    @BindView(R.id.fragment_createalarm_recurring_options) LinearLayout recurringOptions;

    private CreateAlarmViewModel createAlarmViewModel;
    private Handler mainHandler = new Handler();
    private ProgressDialog progressDialog;

    private class AlarmInfo{
        int id;
        String title, hour, minute;
        boolean recurring;

        private AlarmInfo(int id, String title, String hour, String minute, boolean recurring) {
            this.id = id;
            this.title = title;
            this.hour = hour;
            this.minute = minute;
            this.recurring = recurring;
        }
    }

    private ArrayList<AlarmInfo> alarmInfoArrayList = new ArrayList<AlarmInfo>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAlarmViewModel = ViewModelProviders.of(this).get(CreateAlarmViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_createalarm, container, false);

        ButterKnife.bind(this, view);

        recurring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    recurringOptions.setVisibility(View.VISIBLE);
                } else {
                    recurringOptions.setVisibility(View.GONE);
                }
            }
        });

        scheduleAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new fetchData().start();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                scheduleAlarm();
                Navigation.findNavController(v).navigate(R.id.action_createAlarmFragment_to_alarmsListFragment);
            }
        });

        return view;
    }

    private void scheduleAlarm() {

        //int id = new Random().nextInt(Integer.MAX_VALUE);
        int id = 0;
        String alarmTitle = "miss you";
        String hour = "8";
        String minute = "12";
        boolean alarmRecurring = false;

        Log.e(TAG, "alarmInfoArrayList: " + alarmInfoArrayList.size());
        for (AlarmInfo alarmInfo: alarmInfoArrayList) {

            id = alarmInfo.id;
            alarmTitle = alarmInfo.title;
            hour = alarmInfo.hour;
            minute = alarmInfo.minute;
            alarmRecurring = alarmInfo.recurring;

            Alarm alarm = new Alarm(
                    id,
                    /*TimePickerUtil.getTimePickerHour(timePicker),
                    TimePickerUtil.getTimePickerMinute(timePicker),
                    */
                    Integer.parseInt(hour),
                    Integer.parseInt(minute),
                    /*title.getText().toString(),*/
                    alarmTitle,
                    System.currentTimeMillis(),
                    true,
                    /*recurring.isChecked(),*/
                    alarmRecurring,
                    mon.isChecked(),
                    tue.isChecked(),
                    wed.isChecked(),
                    thu.isChecked(),
                    fri.isChecked(),
                    sat.isChecked(),
                    sun.isChecked()
            );

            createAlarmViewModel.insert(alarm);

            alarm.schedule(getContext());
        }
    }

    class fetchData extends Thread{

        String data = "";

        @Override
        public void run() {

            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    progressDialog = new ProgressDialog(CreateAlarmFragment.super.getActivity());
                    progressDialog.setMessage("Fetching Data");
                    Log.e(TAG, "fetching data");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });

            try {
                URL url = new URL("https://api.npoint.io/a8a8f02e3866f35b7585");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    data += line;
                }

                Log.e(TAG, "scheduleAlarm: {%s}" + data);

                if (!data.isEmpty()){
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray alarmList = jsonObject.getJSONArray("alarm_list");

                    Log.e(TAG, "alarm_list len: " + alarmList.length());

                    for (int i = 0; i < alarmList.length(); i++) {

                        JSONObject alarm = alarmList.getJSONObject(i);

                        int id = alarm.getInt("id");
                        String title = alarm.getString("title");
                        String hour = alarm.getString("time").split(":")[0];
                        String minute = alarm.getString("time").split(":")[1];
                        boolean recurring = alarm.getBoolean("recurring");
                        Log.e(TAG, "alarmInfo: " + id + title + hour + minute + recurring);

                        AlarmInfo alarmInfo = new AlarmInfo(id, title, hour, minute, recurring);
                        alarmInfoArrayList.add(alarmInfo);
                    }
                }
                Log.e(TAG, "alarmInfoArrayList: " + alarmInfoArrayList.size());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } {
            }

            //scheduleAlarm();

            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }
}
