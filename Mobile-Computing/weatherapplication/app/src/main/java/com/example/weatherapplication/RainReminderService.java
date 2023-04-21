package com.example.weatherapplication;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class RainReminderService extends Service {
    NotificationManager notifyManager;
    Notification notification;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startMyOwnForeground();
        } else {
            startForeground(1, new Notification());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String channelId = "service";
        String channelName = "Extreme Weather Reminder";
        NotificationChannel channel2 = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        notifyManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.createNotificationChannel(channel2);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Weather App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        notification.defaults = Notification.DEFAULT_ALL;
        startForeground(1,notification);
    }
  
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int Minutes = 1800*1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + Minutes;
        Intent i = new Intent(this, RainAlarmReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        System.out.println("30MIN");
        return super.onStartCommand(intent, flags, startId);
    }
}