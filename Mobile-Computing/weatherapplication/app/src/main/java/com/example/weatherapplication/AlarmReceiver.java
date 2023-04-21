package com.example.weatherapplication;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import cz.msebera.android.httpclient.Header;

import static android.content.Context.MODE_PRIVATE;

public class AlarmReceiver extends BroadcastReceiver {
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    final String API_KEY = "b0ee6c86015b8d44df8217529f05812a";
    NotificationManager notifyManager;
    Notification notification;
    LocationManager mLocationManager;
    LocationListener mLocationListener;
    final long MIN_TIME = 5000; // 5 seconds
    final float MIN_DISTANCE = 1000; // 1 meter
    String Location_Provider = LocationManager.GPS_PROVIDER;
    weatherData data;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(PendingIntent.getBroadcast(context,getResultCode(),new Intent(context,AlarmReceiver.class),0));

        SharedPreferences sp = context.getSharedPreferences("alarm", MODE_PRIVATE);
        String lat = sp.getString("lat",null);
        String lon = sp.getString("lon",null);
        RequestParams params = new RequestParams();
        params.put("lat", lat);
        params.put("lon", lon);
        params.put("appid", API_KEY);
        doNetworking(params,context);

        String channelId = "subscribe";
        String channelName = "Schedule";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);

        notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.createNotificationChannel(channel);
    }

    public void doNetworking(RequestParams params,Context context) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                data = weatherData.fromJson(response);
                notification = new NotificationCompat.Builder(context, "subscribe")
                        .setAutoCancel(true)
                        .setContentTitle("Today Weather")
                        .setContentText("Weather:"+data.getWeatherType()+",Temperature:"+data.getTemperature()+"°C"+", Humidity:"+data.getHumidity())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Weather:"+data.getWeatherType()+",Temperature:"+data.getTemperature()+"°C"+", Humidity:"+data.getHumidity()))
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .build();
                notifyManager.notify(3, notification);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                notification = new NotificationCompat.Builder(context, "subscribe")
                        .setAutoCancel(true)
                        .setContentTitle("Today Weather")
                        .setContentText("Get weather date failure.  Please check your GPS signal or internet connection.")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Get weather date failure.  Please check your GPS signal or internet connection."))
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .build();
                notifyManager.notify(3, notification);
            }
        });
    }

    public void getCurrentWeather(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());
                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", API_KEY);
                doNetworking(params,context);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                // User allowed us to fetch the location but we still cannot get the location
            }
        };
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

}
