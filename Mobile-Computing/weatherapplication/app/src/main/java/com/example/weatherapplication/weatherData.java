package com.example.weatherapplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class weatherData {
    private String currentDate, temperature, bodyTemp, humidity, icon, city, weatherType;
    private int condition;
    private double lon, lat;

    public static weatherData fromJson(JSONObject jsonObject){
        try{
            weatherData data = new weatherData();
            data.city = jsonObject.getString("name");
            data.condition = jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
            data.weatherType = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
            data.icon = updateWeatherIcon(data.condition);
            double temperatureResult = jsonObject.getJSONObject("main").getDouble("temp")-273.15;
            int roundedTemp = (int)Math.rint(temperatureResult);
            data.temperature = Integer.toString(roundedTemp);
            data.lon = jsonObject.getJSONObject("coord").getDouble("lon");
            data.lat = jsonObject.getJSONObject("coord").getDouble("lat");
            int humidityResult = jsonObject.getJSONObject("main").getInt("humidity");
            data.humidity = Integer.toString(humidityResult);
            int dateResult = jsonObject.getInt("dt");
            Date date = new Date(dateResult*1000L);
            SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            data.currentDate = jdf.format(date);

            return data;

        }catch(JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    private static String updateWeatherIcon(int condition) {
        if (condition >= 0 && condition <= 300) {
            return "lightening";
        } else if (condition >= 300 && condition <= 500) {
            return "rainy";
        } else if (condition >= 500 && condition <= 600) {
            return "rainy";
        } else if (condition >= 600 && condition <= 700) {
            return "snowy";
        } else if (condition >= 700 && condition <= 771) {
            return "fog"; // Missing
        } else if (condition >= 772 && condition < 800) {
            return "overcast"; // Missing
        } else if (condition == 800) {
            return "sunny";
        } else if (condition >= 801 && condition <= 804) {
            return "cloudy"; // Missing
        } else if (condition >= 900 && condition <= 902) {
            return "lightening";
        } else if (condition == 903) {
            return "snowy";
        } else if (condition == 904) {
            return "sunny";
        } else if (condition >= 905 && condition <= 1000) {
            return "lightening";
        }
        return "question";
    }

    public String getTemperature() {
        return temperature;
    }

    public String getIcon() {
        return icon;
    }

    public String getCity() {
        return city;
    }

    public String getWeatherType() {
        return weatherType;
    }

    public String getHumidity() { return humidity+"%"; }

    public int getHumidityInt() { return Integer.parseInt(humidity); }

    public String getCurrentDate() { return currentDate; }

    public double getLon() { return lon;}

    public double getLat() { return lat;}
}
