package com.learntodroid.simplealarmclock.alarmslist;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.learntodroid.simplealarmclock.activities.MainActivity;
import com.learntodroid.simplealarmclock.data.Alarm;
import com.learntodroid.simplealarmclock.R;

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
import java.util.List;

public class AlarmsListFragment extends Fragment implements OnToggleAlarmListener {
    private AlarmRecyclerViewAdapter alarmRecyclerViewAdapter;
    private AlarmsListViewModel alarmsListViewModel;
    private RecyclerView alarmsRecyclerView;
    private Button addAlarm;

    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        alarmRecyclerViewAdapter = new AlarmRecyclerViewAdapter(this);
        alarmsListViewModel = ViewModelProviders.of(this).get(AlarmsListViewModel.class);
        alarmsListViewModel.getAlarmsLiveData().observe(this, new Observer<List<Alarm>>() {
            @Override
            public void onChanged(List<Alarm> alarms) {
                if (alarms != null) {
                    alarmRecyclerViewAdapter.setAlarms(alarms);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listalarms, container, false);

        alarmsRecyclerView = view.findViewById(R.id.fragment_listalarms_recylerView);
        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alarmsRecyclerView.setAdapter(alarmRecyclerViewAdapter);

        addAlarm = view.findViewById(R.id.fragment_listalarms_addAlarm);
        addAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new fetchData().start();
                Navigation.findNavController(v).navigate(R.id.action_alarmsListFragment_to_createAlarmFragment);
            }
        });

        return view;
    }

    @Override
    public void onToggle(Alarm alarm) {
        if (alarm.isStarted()) {
            alarm.cancelAlarm(getContext());
            alarmsListViewModel.update(alarm);
        } else {
            alarm.schedule(getContext());
            alarmsListViewModel.update(alarm);
        }
    }

    class fetchData extends Thread{

        String data = "";

        @Override
        public void run() {

            Log.e(TAG, "scheduleAlarm: {%s}" + data);

            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    progressDialog = new ProgressDialog(AlarmsListFragment.super.getActivity());
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
                Log.e(TAG, "scheduleAlarm: {%s}" + data);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                Log.e(TAG, "scheduleAlarm: {%s}" + bufferedReader.readLine());
                while ((line = bufferedReader.readLine()) != null) {
                    data += line;
                }

                Log.e(TAG, "scheduleAlarm: {%s}" + data);

                if (!data.isEmpty()){
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray alarmList = jsonObject.getJSONArray("alarm_list");

                    for (int i = 0; i < alarmList.length(); i++){
                        String time = alarmList.getString(i);
                        Log.e(TAG, "scheduleAlarm: {%s}" + time);
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } {
            }

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