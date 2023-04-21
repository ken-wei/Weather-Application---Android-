package com.example.weatherapplication;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TimePicker;

import java.util.Calendar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class notiActivity extends AppCompatActivity implements DatePicker.OnDateChangedListener, TimePicker.OnTimeChangedListener {


    public ArrayAdapter<String> adapter;
    //get button
    Button but_add;
    Button btn_chooseDate;
    Button btn_chooseTime;
    ListView alarmList;
    SharedPreferences sp;
    AlarmManager alarmManager;
    ImageView backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
         backButton = findViewById(R.id.backButton);
         but_add = findViewById(R.id.btn_add);
         btn_chooseDate = findViewById(R.id.btn_chooseDate);
         btn_chooseTime = findViewById(R.id.btn_chooseTime);
         alarmList = findViewById(R.id.alarmList);
         sp= getSharedPreferences("alarm",MODE_PRIVATE);
         alarmManager = (AlarmManager) notiActivity.this.getSystemService(Context.ALARM_SERVICE);
         Intent i = new Intent(notiActivity.this, AlarmService.class);
         notiActivity.this.startService(i);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(notiActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        //add button
        but_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             // TODO Auto-generated method stub
                addAlarm();
                Intent i = new Intent(notiActivity.this, AlarmService.class);
                notiActivity.this.startService(i);
           }
        });

        //choose date
        Calendar ca = Calendar.getInstance();
        int mYear = ca.get(Calendar.YEAR);
        int mMonth = ca.get(Calendar.MONTH);
        int mDay = ca.get(Calendar.DAY_OF_MONTH);


         DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                int mYear = year;
                int mMonth = monthOfYear;
                int mDay = dayOfMonth;
                String days;
                if (mMonth + 1 < 10) {
                    if (mDay < 10) {
                        days = new StringBuffer().append(mYear).append("-").append("0").
                                append(mMonth + 1).append("-").append("0").append(mDay).toString();
                    } else {
                        days = new StringBuffer().append(mYear).append("-").append("0").
                                append(mMonth + 1).append("-").append(mDay).toString();
                    }

                } else {
                    if (mDay < 10) {
                        days = new StringBuffer().append(mYear).append("-").
                                append(mMonth + 1).append("-").append("0").append(mDay).toString();
                    } else {
                        days = new StringBuffer().append(mYear).append("-").
                                append(mMonth + 1).append("-").append(mDay).toString();
                    }

                }

                btn_chooseDate.setText(days);
            }
        };

        btn_chooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(notiActivity.this, onDateSetListener, mYear, mMonth, mDay).show();
            }
        });

        //choose time
        int mHour = ca.get(Calendar.HOUR_OF_DAY);
        int mMinute = ca.get(Calendar.MINUTE);

        btn_chooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(notiActivity.this, new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int mHour = hourOfDay;
                        int mMinute = minute;
                        String time;
                        if (mHour < 10) {
                            if (mMinute < 10) {
                                time = new StringBuffer().append("0").
                                        append(mHour).append(":").append("0").append(mMinute).toString();
                            } else {
                                time = new StringBuffer().append("0").
                                        append(mHour).append(":").append(mMinute).toString();
                            }

                        } else {
                            if (mMinute < 10) {
                                time = new StringBuffer().append(mHour).append(":").append("0").append(mMinute).toString();
                            } else {
                                time = new StringBuffer().append(mHour).append(":").append(mMinute).toString();
                            }

                        }

                        btn_chooseTime.setText(time);
                    }
                }, mHour, mMinute,true).show();
            }
        });

        adapter = new ArrayAdapter<String>(notiActivity.this, R.layout.alarm_item);
        readData();
        alarmList.setAdapter(adapter);

        alarmList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view,final int p, long l) {
                new AlertDialog.Builder(notiActivity.this).setTitle("Are you sure to delete this alarm?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface,int i) {
                        deleteAlarm(p);
                    }
                }).setNegativeButton("Cancel",null).show();
                return true;
            }
        });
    }



    @Override
    public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {

    }

    @Override
    public void onTimeChanged(TimePicker timePicker, int i, int i1) {

    }

    //Warn user not to leave date and time blank
    public void addAlarm(){
        if(btn_chooseDate.getText().toString().equals("CHOOSE DATE") || btn_chooseTime.getText().toString().equals("CHOOSE TIME")){
            AlertDialog.Builder dialog =new AlertDialog.Builder(this)
                                            .setTitle("Warning")
                                            .setMessage("please choose date and time")
                                            .setPositiveButton("Close",null);
            dialog.show();
        }else{
            adapter.add((btn_chooseDate.getText().toString()+" "+btn_chooseTime.getText().toString()).toString());
            SharedPreferences.Editor editor=sp.edit();
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < adapter.getCount(); i++){
                sb.append(adapter.getItem(i)).append(",");
            }
            if(sb.length() > 1){
                editor.putString("alarm",sb.toString().substring(0,sb.length()-1));
            }else{
                editor.putString("alarm",null);
            }
            editor.commit();
            Log.i("Tag",sp.getString("alarm",null));
            btn_chooseDate.setText("CHOOSE DATE");
            btn_chooseTime.setText("CHOOSE TIME");
        }

    }

    public void readData(){
        String content = sp.getString("alarm",null);
        Calendar calendar = Calendar.getInstance();
        if(content != null){
            String[] alarmStrings = content.split(",");
            for(String s:alarmStrings){
                adapter.add(s);
            }
        }
    }

    public void deleteAlarm(int position){
        adapter.remove(adapter.getItem(position));
        SharedPreferences.Editor editor=sp.edit();
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < adapter.getCount(); i++){
            sb.append(adapter.getItem(i)).append(",");
        }
        if(sb.length() > 1){
            editor.putString("alarm",sb.toString().substring(0,sb.length()-1));

        }else{
            editor.putString("alarm",null);
        }
        editor.commit();
        Intent i = new Intent(notiActivity.this, AlarmService.class);
        notiActivity.this.startService(i);
    }
}
