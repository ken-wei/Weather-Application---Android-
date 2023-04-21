package com.example.weatherapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    final String API_KEY = "b0ee6c86015b8d44df8217529f05812a";
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";

    final long MIN_TIME = 5000; // 5 seconds
    final float MIN_DISTANCE = 1000; // 1 meter
    final int REQUEST_CODE = 101;

    String Location_Provider = LocationManager.GPS_PROVIDER;

    TextView city, weather, temperature, humidity, date, fav_text;
    ImageView menu,weatherIcon,fav_star,global;
    RelativeLayout cityFinder, addFavorite;

    LocationManager mLocationManager;
    LocationListener mLocationListener;

    FirebaseAuth auth;
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static String current_user;
    private static String current_city;
    private static String[] temp_loc_array;
    String longitude;
    String latitude;
    String weather_type;
    int temp;
    int humid;
    double lon, lat;

    Boolean add = false;
    Boolean read_complete = false;
    SharedPreferences sp;

    private Intent rainIntent;

    @Override
    protected void onStart() {
        super.onStart();
        checkUserLoggedIn(); // Check if user is logged in to update the UI
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        menu = findViewById(R.id.menu);
        global = findViewById(R.id.global);
        city = findViewById(R.id.cityName);
        weather = findViewById(R.id.weatherCondition);
        temperature = findViewById(R.id.temperature);
        humidity = findViewById(R.id.Humidity);
        date = findViewById(R.id.date);
        weatherIcon = findViewById(R.id.weatherIcon);
        cityFinder = findViewById(R.id.cityFinder);
        addFavorite = findViewById(R.id.addFavorite);
        fav_star = findViewById(R.id.fav_star);
        fav_text = findViewById(R.id.fav_text);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
                Menu menu = navigationView.getMenu();
                MenuItem loginBtn = menu.findItem(R.id.login);
                View header = navigationView.getHeaderView(0);
                TextView username = header.findViewById(R.id.userid);

                // Change the title based on user has logged in or not
                if (checkUserLoggedIn()) {
                    username.setText("" + auth.getCurrentUser().getEmail());
                    loginBtn.setTitle("Logout");
                } else {
                    username.setText("Guest");
                    loginBtn.setTitle("Login");
                }
                current_user = username.getText().toString();
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        global.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(read_complete){
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("current_city", current_city);
                    intent.putExtra("temp", temp);
                    intent.putExtra("humid", humid);
                    intent.putExtra("lon", lon);
                    intent.putExtra("lat", lat);
                    startActivity(intent);
                }else{
                    Toast.makeText(MainActivity.this,"Fetching GPS location now, please wait.",Toast.LENGTH_LONG).show();
                }
            }
        });

        // City finder button
        cityFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, cityFinder.class);
                startActivity(intent);
            }
        });

        // Add favorite button
        addFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(read_complete){
                    if(!checkUserLoggedIn()){
                        AlertDialog.Builder dialog =new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Warning")
                                .setMessage("Oops... Haven't Login Yet!")
                                .setPositiveButton("Go Login",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    }
                                });
                        dialog.show();
                    }else {
                        if (add == false) {
                            add = true;

                            fav_star.setVisibility(View.VISIBLE);
                            fav_text.setText("Favorite");
                            db.collection("user_data").document(current_user)
                                    .update("fav_locations", FieldValue.arrayUnion(current_city));
                        } else {
                            add = false;
                            fav_star.setVisibility(View.INVISIBLE);
                            fav_text.setText("Add Favorite");
                            db.collection("user_data").document(current_user)
                                    .update("fav_locations", FieldValue.arrayRemove(current_city));
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this,"Fetching GPS location now, please wait.",Toast.LENGTH_LONG).show();
                }
            }
        });

        setNavigationViewListener();
        Intent rainIntent = new Intent(this, RainReminderService.class);
        startService(rainIntent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkUserLoggedIn();
        // Use intent to get the city name
        Intent mainIntent = getIntent();
        String cityName = mainIntent.getStringExtra("city");

        if (cityName == null){
            getCurrentWeather();
        }else{
            getNewWeather(cityName);
        }
    }

    public void getNewWeather(String cityName){
        RequestParams params = new RequestParams();
        params.put("q", cityName);
        params.put("appid", API_KEY);
        doNetworking(params);
    }

    public void getCurrentWeather() {
        // Get the object of current location
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
                Log.d("MainActivity", ":)" + latitude);
                Log.d("MainActivity", ":)" + longitude);

                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", API_KEY);
                doNetworking(params);
                sp= getSharedPreferences("alarm",MODE_PRIVATE);
                SharedPreferences.Editor editor=sp.edit();
                editor.putString("lat",latitude);
                editor.putString("lon",longitude);
                editor.commit();

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                // User allowed us to fetch the location but we still cannot get the location
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentWeather();
            }else{
                // User denied the permission -> Compared to onProviderDisabled()
            }
        }

    }

    // Update current user after checking the firebase instance
    public boolean checkUserLoggedIn() {
        FirebaseUser user = auth.getCurrentUser();

        if (auth.getCurrentUser() != null) {
            current_user = auth.getCurrentUser().getEmail();
            return true;
        }else{

        }
        return false;
    }

    // logout the user using firebase instance
    public void logoutUser() {
        auth.signOut();
    }

    public void doNetworking(RequestParams params){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //super.onSuccess(statusCode, headers, response);
                weatherData data = weatherData.fromJson(response);
                updateUI(data);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

            }
        });
    }

    private void updateUI(weatherData data){
        date.setText(data.getCurrentDate());
        temperature.setText(data.getTemperature());
        temp = Integer.parseInt(data.getTemperature());
        current_city = data.getCity();
        city.setText(data.getCity());
        lon = data.getLon();
        lat = data.getLat();
        weather.setText(data.getWeatherType());
        weather_type = data.getWeatherType();
        humidity.setText(data.getHumidity());
        humid = data.getHumidityInt();
        int resourceID = getResources().getIdentifier(data.getIcon(), "drawable", getPackageName());
        weatherIcon.setImageResource(resourceID);
        if(current_user == null || current_user.equals("Guest")){

        }else{
            checkFav(current_city);
        }
        read_complete = true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.home: {
                break;
            }
            case R.id.favorite: {
                Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
                intent.putExtra("Username", getUsername());
                intent.putExtra("CurrentCity", getCurrent_city());
                startActivity(intent);
                break;
            }
            case R.id.schedule: {
                if(read_complete){
                    startActivity(new Intent(MainActivity.this, notiActivity.class));
                }else{
                    Toast.makeText(MainActivity.this,"Fetching GPS location now, please wait.",Toast.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.login: {
                if (checkUserLoggedIn()) { // Display favourite on the navigation drawer when logged
                    logoutUser();
                    fav_star.setVisibility(View.INVISIBLE);
                    fav_text.setText("Add Favorite");
                } else { // Redirect to login activity if user not logged in
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                break;
            }
        }
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationManager != null){
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    public void checkFav(String city){
        DocumentReference dr = db.collection("user_data").document(current_user);
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            String[] fav_location;
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                List<String> fav_loc_list = (List<String>) document.get("fav_locations");
                fav_location = fav_loc_list.toArray(new String[0]);
                setTempLoc(fav_location);
                if(Arrays.asList(fav_location).contains(current_city)){
                    add = true;
                    fav_star.setVisibility(View.VISIBLE);
                    fav_text.setText("Favorite");
                }else{
                    add = false;
                    fav_star.setVisibility(View.INVISIBLE);
                    fav_text.setText("Add Favorite");
                }
            }
        });
    }

    public static String getUsername(){
        return current_user;
    }

    public static String getCurrent_city(){
        return current_city;
    }

    public static void setTempLoc(String[] fav_location){
        temp_loc_array = Arrays.copyOf(fav_location, fav_location.length);
    }

}
