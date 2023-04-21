package com.example.weatherapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class FavoriteActivity extends AppCompatActivity {

    final String API_KEY = "b0ee6c86015b8d44df8217529f05812a";
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListView listView;
    private String[] fav_city;

    List<favCityData> mDatas = new LinkedList<>();
    private GestureDetector mDetector;

    private customAdapter Adapter;
    private int taskNum;

    private TextView alert;
    String current_user;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        listView = findViewById(R.id.listView);
        alert = findViewById(R.id.alert);
        current_user = getIntent().getStringExtra("Username");

        ImageView backButton = findViewById(R.id.backButton);
        // When click the backButton
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(FavoriteActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        if(current_user.equals("Guest")){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Oops... Haven't Login Yet!")
                    .setPositiveButton("Go Login",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(FavoriteActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    });
            dialog.show();
        }else {
            db.collection("user_data").document(current_user)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                List<String> fav_loc_list = (List<String>) document.get("fav_locations");
                                fav_city = fav_loc_list.toArray(new String[0]);
                                if (fav_city.length == 0){
                                    alert.setVisibility(View.VISIBLE);
                                }else {
                                    for (int i = 0; i < fav_city.length; i++) {
                                        mDatas.add(new favCityData(null, null, null));
                                    }

                                    taskNum = fav_city.length;

                                    for (int i = 0; i < fav_city.length; i++) {
                                        String[] fav_data = new String[3];
                                        int[] fav_icon = new int[1];
                                        getNewWeather(fav_city[i], i, fav_data, fav_icon, fav_city.length);
                                    }
                                    createDetector();

                                    listView.setOnTouchListener(new View.OnTouchListener() {

                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            // TODO Auto-generated method stub
                                            mDetector.onTouchEvent(event);
                                            return false;
                                        }
                                    });
                                }
                            } else {
                                System.out.println("Error getting documents.");
                            }
                        }
                    });
        }

    }

    public void getNewWeather(String cityName, int index, String[] fav_data, int[] fav_icon, int length){
        RequestParams params = new RequestParams();
        params.put("q", cityName);
        params.put("appid", API_KEY);
        doNetworking(params, index, fav_data, fav_icon, length);
    }

    public void doNetworking(RequestParams params, int index, String[] fav_data, int[] fav_icon, int length){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                weatherData data = weatherData.fromJson(response);
                int resourceID = getResources().getIdentifier(data.getIcon(), "drawable", getPackageName());
                fav_data[0] = data.getWeatherType();
                fav_data[1] = data.getTemperature() + "°C";
                fav_data[2] = data.getHumidity();
                fav_icon[0] = resourceID;

                mDatas.set(index, new favCityData(fav_city[index], fav_data, fav_icon));

                taskNum--;
                if(taskNum == 0){
                    Adapter = new customAdapter(getApplicationContext(), mDatas);
                    listView.setAdapter(Adapter);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(FavoriteActivity.this, "Oops! something goes wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class favCityData {
        private String fav_city;
        private String[] fav_data;
        int[] fav_icon;

        favCityData(String fav_city, String[] fav_data, int[] fav_icon){
            this.fav_city = fav_city;
            this.fav_data = fav_data;
            this.fav_icon = fav_icon;
        }
    }


    public class customAdapter extends BaseAdapter {

        private List<favCityData> mDatas;
        private final Context context;

        public customAdapter(@NonNull Context context, List<favCityData> mDatas) {
            this.context = context;
            this.mDatas = mDatas;

        }

        @Override
        public int getCount() {
            return mDatas == null ? 0 : mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int i, View convertView, ViewGroup viewGroup){

            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.favorite_item, viewGroup, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.location.setText(mDatas.get(i).fav_city);
            viewHolder.weatherCondition.setText(mDatas.get(i).fav_data[0]);
            viewHolder.temperature.setText(mDatas.get(i).fav_data[1]);
            viewHolder.humidity.setText(mDatas.get(i).fav_data[2]);
            viewHolder.weather_icon.setImageResource(mDatas.get(i).fav_icon[0]);

            return convertView;
        }


    }

    private final class ViewHolder {
        ImageView weather_icon;
        TextView location;
        TextView weatherCondition;
        TextView temperature;
        TextView humidity;
        Button deleteButton;

        ViewHolder(View v) {
            weather_icon = v.findViewById(R.id.icon);
            deleteButton = v.findViewById(R.id.deleteButton);
            location = v.findViewById(R.id.location);
            weatherCondition = v.findViewById(R.id.weatherCondition);
            temperature = v.findViewById(R.id.temperature);
            humidity = v.findViewById(R.id.humidity);
        }
    }


    public void createDetector(){

        mDetector = new GestureDetector(FavoriteActivity.this, new GestureDetector.OnGestureListener() {

            private int distance = 100;
            /** max velocity of fling */
            private int velocity = 200;
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Intent intent = new Intent(FavoriteActivity.this, MainActivity.class);
                intent.putExtra("city", fav_city[listView.pointToPosition((int)e.getX(), (int)e.getY())]);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {
                // TODO Auto-generated method stub
                // e1：the first ACTION_DOWN MotionEvent
                // e2：the last  ACTION_MOVE MotionEvent

                int pos = listView.pointToPosition((int)e1.getX(), (int)e1.getY());

                // fling from right to left
                if (e1.getX() - e2.getX() > distance
                        && Math.abs(velocityX) > velocity
                        && Math.abs(e1.getY() - e2.getY()) < 100) {
                    onRTLFling(pos);
                }
                // fling from left to right
                if (e2.getX() - e1.getX() > distance
                        && Math.abs(velocityX) > velocity
                        && Math.abs(e1.getY() - e2.getY()) < 100) {
                    onLTRFling(pos);
                }
                return false;
            }
        });



    }

    public void onRTLFling(int position){
        View tmpView = findView(position, listView);
        TextView textView = tmpView.findViewById(R.id.location);
        String city = textView.getText().toString();
        Button deleteButton = tmpView.findViewById(R.id.deleteButton);
        deleteButton.setVisibility(View.VISIBLE);

        for(int i = 0; i < fav_city.length; i++){
            if (i != position){
                View tmp = findView(i, listView);
                tmp.findViewById(R.id.deleteButton).setVisibility(View.INVISIBLE);
            }

        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(FavoriteActivity.this)
                        .setTitle("Warning")
                        .setMessage("Are you sure to remove " + city + " from your favorite list?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("Confirm",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteFavorite(city);

                                int index = (int)listView.getItemIdAtPosition(position);

                                mDatas.remove( mDatas.get(index));

                                List<String> list1=Arrays.asList(fav_city);
                                List<String> arrList = new ArrayList<String>(list1);
                                arrList.remove(arrList.get(index));
                                fav_city = arrList.toArray(new String[0]);

                                for(int i=0; i< fav_city.length; i++){
                                    View tmpView = findView(i, listView);
                                    Button deleteButton = tmpView.findViewById(R.id.deleteButton);
                                    deleteButton.setVisibility(View.INVISIBLE);
                                }

                                Adapter.notifyDataSetChanged();

                                if(fav_city.length == 0){
                                    alert.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                dialog.show();

            }
        });
    }

    public void onLTRFling(int position){
        View tmpView = findView(position, listView);
        Button deleteButton = tmpView.findViewById(R.id.deleteButton);
        deleteButton.setVisibility(View.INVISIBLE);
    }

    public void deleteFavorite(String city){
        db.collection("user_data").document(current_user)
                .update("fav_locations", FieldValue.arrayRemove(city));
    }

    private View findView(int position, ListView listView) {
        int firstListItemPosition = listView.getFirstVisiblePosition();
        int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
        if (position < firstListItemPosition || position > lastListItemPosition) {
            return listView.getAdapter().getView(position, null, listView);
        } else {
            final int childIndex = position - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

}
