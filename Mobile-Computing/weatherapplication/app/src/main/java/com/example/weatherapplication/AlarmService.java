package com.example.weatherapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import java.util.Calendar;

import androidx.annotation.RequiresApi;


public class AlarmService extends Service{

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager alarmManager;
        SharedPreferences sp;
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        sp= getSharedPreferences("alarm",MODE_PRIVATE);

        String content = sp.getString("alarm",null);
        Calendar calendar = Calendar.getInstance();
        Long now_mill = calendar.getTimeInMillis();
        if(content != null){
            String[] alarmStrings = content.split(",");
            for(String s:alarmStrings){
                String[] date_time = s.split(" ");
                String year, month,day, hour, minute;
                String date = date_time[0];
                String time = date_time[1];
                String[] _date = date.split("-");
                year = _date[0];
                month = _date[1];
                day = _date[2];
                String[] _time = time.split(":");
                hour = _time[0];
                minute = _time[1];
                calendar.set(Integer.valueOf(year).intValue(),Integer.valueOf(month).intValue()-1,Integer.valueOf(day).intValue(),Integer.valueOf(hour).intValue(),Integer.valueOf(minute).intValue(),0);
                Intent _intent = new Intent(this,AlarmReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(this,(int)(calendar.getTimeInMillis()/1000/60),_intent,0);
                if(now_mill < calendar.getTimeInMillis()){
                    alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pi);
                }

            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.cancel(pi);
    }

}
