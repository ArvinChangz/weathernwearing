package com.example.weather_wearing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.DialogFragment;

public class SettingsActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private TextView mTextView;
    private RadioGroup mRadioGroup;
    private TextView iTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar3 = findViewById(R.id.toolbar_Settings);
        setSupportActionBar(toolbar3);
        mRadioGroup = findViewById(R.id.radioGroup);

        mRadioGroup.setOnCheckedChangeListener(radGrpRegionOnCheckedChange); //設定單選選項監聽器
        SharedPreferences sharedPreferencess = getSharedPreferences("Perference", MODE_PRIVATE); //儲存偏好
        int savedRadioIndex = sharedPreferencess.getInt("radio", 0);
        RadioButton savedCheckedRadioButton = (RadioButton) mRadioGroup.getChildAt(savedRadioIndex);
        savedCheckedRadioButton.setChecked(true);

        Button button;
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingsActivity.this.finish();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("Setting", MODE_PRIVATE);

        //每日通知
        mTextView = findViewById(R.id.textView_setTime);
        Group buttonTimePicker = findViewById(R.id.set_g);
        mTextView.setText(mTextView.getText() + sharedPreferences.getString("time", "通知已關閉"));
        int refIds[] = buttonTimePicker.getReferencedIds();
        for (int id : refIds) {
            findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment timePicker = new TimePickerFragment();
                    timePicker.show(getSupportFragmentManager(), "time picker");
                }
            });
        }
        Group buttonCancelAlarm = findViewById(R.id.cancel_g);
        int refIdss[] = buttonCancelAlarm.getReferencedIds();
        for (int id : refIdss) {
            findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelAlarm();
                }
            });
        }
        //即時通知
        iTextView = findViewById(R.id.textView_immediate);
        Group buttonImmediate = findViewById(R.id.set_gg);
        iTextView.setText(sharedPreferences.getString("Immediate", "通知已關閉"));
        int refIdsss[] = buttonImmediate.getReferencedIds();
        for(int id : refIdsss){
            findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateImmediateText();
                    if(NotificationHelper.rain_Future >= 40 && !NotificationHelper.rainNow_Notification){
                        startImmediateAlarm();
                    }
                }
            });
        }
        Group button_immediate = findViewById(R.id.cancel_gg);
        int refIdssss[] = button_immediate.getReferencedIds();
        for (int id : refIdssss) {
            findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancelImmediateAlarm();
                }
            });
        }
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        updateTimeText(calendar);
        startAlarm(calendar);
    }

    private void updateTimeText(Calendar calendar) {
        String timeText = "通知時間：";
        SharedPreferences sharedPreferences = getSharedPreferences("Setting", MODE_PRIVATE);
        timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime());
        sharedPreferences.edit().putString("time", timeText).apply();
        String getTime = sharedPreferences.getString("time", "通知已關閉");
        mTextView.setText(getTime);
    }

    private void startAlarm(Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(SettingsActivity.this, AlertReceiver.class);
        intent.putExtra("type", "1");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(SettingsActivity.this, 1, intent, 0);
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(SettingsActivity.this, 1, intent, 0);

        alarmManager.cancel(pendingIntent);
        SharedPreferences sharedPreferences = getSharedPreferences("Setting", MODE_PRIVATE);
        sharedPreferences.edit().putString("time", "通知已關閉").apply();
        String closeTime = sharedPreferences.getString("time", "通知已關閉");
        mTextView.setText(closeTime);
    }

    //即時通知
    public void startImmediateAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(SettingsActivity.this, AlertReceiver.class);
        intent.putExtra("type", "2");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(SettingsActivity.this, 2, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP , System.currentTimeMillis(), pendingIntent);
    }

    private void updateImmediateText() {
        String timeText = "提醒已開啟";
        SharedPreferences sharedPreferences = getSharedPreferences("Setting", MODE_PRIVATE);
        sharedPreferences.edit().putString("Immediate", timeText).apply();
        String string = sharedPreferences.getString("Immediate", "提醒已關閉");
        iTextView.setText(string);
        NotificationHelper.ifRain_Notification = string;
    }

    private void cancelImmediateAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(SettingsActivity.this, 2, intent, 0);

        alarmManager.cancel(pendingIntent);
        SharedPreferences sharedPreferences = getSharedPreferences("Setting", MODE_PRIVATE);
        sharedPreferences.edit().putString("Immediate", "提醒已關閉").apply();
        String close = sharedPreferences.getString("Immediate", "提醒已關閉");
        iTextView.setText(close);
        NotificationHelper.ifRain_Notification = close;
    }

    //偏好設定
    private RadioGroup.OnCheckedChangeListener radGrpRegionOnCheckedChange =
            new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton checkedRadioButton = mRadioGroup.findViewById(checkedId);
                    int checkedIndex = mRadioGroup.indexOfChild(checkedRadioButton);
                    SharedPreferences sharedPreferencess = getSharedPreferences("Perference", MODE_PRIVATE);
                    sharedPreferencess.edit().putInt("radio", checkedIndex).apply();
                    mRadioGroup.check(checkedId);
                }
            };
}