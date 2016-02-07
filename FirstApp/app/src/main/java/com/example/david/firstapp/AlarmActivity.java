package com.example.david.firstapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.Calendar;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by David on 2/2/2016.
 */
public class AlarmActivity extends Activity {

    @Bind(R.id.timePicker)
    TimePicker timePicker;
    @Bind(R.id.setAlarmButton)
    Button setAlarmButton;
    @Bind(R.id.alarmInfo)
    TextView alarmInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_screen);
        ButterKnife.bind(this);

        Calendar cal = Calendar.getInstance();
        timePicker.setCurrentHour(Calendar.HOUR);
        timePicker.setCurrentMinute(Calendar.MINUTE);
    }

    @OnClick(R.id.setAlarmButton)
    void onClickButton() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, timePicker.getCurrentHour());
        cal.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        setAlarm(cal);
    }

    private void setAlarm(Calendar cal) {
        alarmInfo.setText("\n\nAlarm set for: " + cal.getTime());
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(AlarmActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(AlarmActivity.this, 0, myIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }
}
