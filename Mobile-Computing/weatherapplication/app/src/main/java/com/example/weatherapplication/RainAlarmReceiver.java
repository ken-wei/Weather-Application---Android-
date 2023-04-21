package com.example.weatherapplication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.MODE_PRIVATE;

public class RainAlarmReceiver extends BroadcastReceiver {
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    final String API_KEY = "b0ee6c86015b8d44df8217529f05812a";
    // TS
    String[] thunderstorm_type = {"thunderstorm with light rain", "thunderstorm with rain", "thunderstorm with heavy rain", "light thunderstorm", "thunderstorm", "heavy thunderstorm", "ragged thunderstorm", "thunderstorm with light drizzle", "thunderstorm with drizzle", "thunderstorm with heavy drizzle"};
    ArrayList<String> thunderstorm_type_list = new ArrayList<>(Arrays.asList(thunderstorm_type));
    // Rain
    String[] rain_type = {"light rain", "moderate rain", "heavy intensity rain", "very heavy rain", "extreme rain", "freezing rain", "light intensity shower rain", "shower rain", "heavy intensity shower rain", "ragged shower rain"};
    ArrayList<String> rain_type_list = new ArrayList<>(Arrays.asList(rain_type));
    // Drizzle
    String[] drizzle_type = {"light intensity drizzle", "drizzle", "heavy intensity drizzle", "light intensity drizzle rain", "drizzle rain", "heavy intensity drizzle rain", "shower rain and drizzle", "heavy shower rain and drizzle", "shower drizzle rain"};
    ArrayList<String> drizzle_type_list = new ArrayList<>(Arrays.asList(drizzle_type));
    // Snow
    String[] snow_type = {"light snow", "Snow", "Heavy snow", "Sleet", "Light shower sleet", "Shower sleet", "Light rain and snow", "Rain and snow", "Light shower snow","Shower snow","Heavy shower snow"};
    ArrayList<String> snow_type_list = new ArrayList<>(Arrays.asList(snow_type));
    Notification notification;
    NotificationManager notifyManager;
    LocationManager mLocationManager;
    LocationListener mLocationListener;
    final long MIN_TIME = 5000; // 5 seconds
    final float MIN_DISTANCE = 1000; // 1 meter
    String Location_Provider = LocationManager.GPS_PROVIDER;
    weatherData data;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = context.getSharedPreferences("alarm", MODE_PRIVATE);
        String lat = sp.getString("lat",null);
        String lon = sp.getString("lon",null);
        RequestParams params = new RequestParams();
        params.put("lat", lat);
        params.put("lon", lon);
        params.put("appid", API_KEY);

        getCurrentWeather(context);
        String channelId = "service";
        String channelName = "Extreme Weather Reminder";
        NotificationChannel channel2 = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.createNotificationChannel(channel2);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //super.onSuccess(statusCode, headers, response);
                data = weatherData.fromJson(response);
                if (rain_type_list.contains(data.getWeatherType())){
                    notification = new NotificationCompat.Builder(context, "service")
                            .setAutoCancel(true)
                            .setContentTitle("Rain Reminder")
                            .setContentText("It's going to rain! Don't forget to bring an umbrella with you.")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("It's going to rain! Don't forget to bring an umbrella with you."))
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setTimeoutAfter(1800*1000)
                            .build();
                    notification.defaults = Notification.DEFAULT_ALL;
                    notifyManager.notify(2, notification);

                    Intent i = new Intent(context, RainReminderService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(i);
                    } else {
                        context.startService(i);
                    }
                }else if(thunderstorm_type_list.contains(data.getWeatherType())){
                    notification = new NotificationCompat.Builder(context, "service")
                            .setAutoCancel(true)
                            .setContentTitle("Thunderstorm Reminder")
                            .setContentText("A thunderstorm is coming! STAY HOME!!!")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("A thunderstorm is coming! STAY HOME!!!"))
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setTimeoutAfter(1800*1000)
                            .build();
                    notification.defaults = Notification.DEFAULT_ALL;
                    notifyManager.notify(2, notification);
                    Intent i = new Intent(context, RainReminderService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(i);
                    } else {
                            context.startService(i);
                    }
                }else if(drizzle_type_list.contains(data.getWeatherType())){
                    notification = new NotificationCompat.Builder(context, "service")
                            .setAutoCancel(true)
                            .setContentTitle("Drizzle Reminder")
                            .setContentText("It's going to drizzle! Don't forget to bring an umbrella with you.")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("It's going to drizzle! Don't forget to bring an umbrella with you."))
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setTimeoutAfter(1800*1000)
                            .build();
                    notification.defaults = Notification.DEFAULT_ALL;
                    notifyManager.notify(2, notification);
                    Intent i = new Intent(context, RainReminderService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(i);
                    } else {
                        context.startService(i);
                    }
                }else if(snow_type_list.contains(data.getWeatherType())){
                    notification = new NotificationCompat.Builder(context, "service")
                            .setAutoCancel(true)
                            .setContentTitle("Snow Reminder")
                            .setContentText("It's going to snow! Bundle up and bring an umbrella with you.")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("It's going to snow! Bundle up and bring an umbrella with you."))
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setTimeoutAfter(1800*1000)
                            .build();
                    notification.defaults = Notification.DEFAULT_ALL;
                    notifyManager.notify(2, notification);
                    Intent i = new Intent(context, RainReminderService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(i);
                    } else {
                        context.startService(i);
                    }
                }else{
                    notification = new NotificationCompat.Builder(context, "service")
                            .setAutoCancel(true)
                            .setContentTitle("No Extreme Weather")
                            .setContentText("Have fun outside and stay safe!!")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("Have fun outside and stay safe!!"))
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setTimeoutAfter(1800*1000)
                            .build();
                    notification.defaults = Notification.DEFAULT_ALL;
                    notifyManager.notify(2, notification);
                    Intent i2 = new Intent(context, RainReminderService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(i2);
                    } else {
                        context.startService(i2);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                System.out.println("failure");
            }
        });
    }

    public void getCurrentWeather(Context context) {
        // Get the object of current location
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());
                Log.d("MainActivity", ":)" + latitude);
                Log.d("MainActivity", ":)" + longitude);

                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", API_KEY);
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