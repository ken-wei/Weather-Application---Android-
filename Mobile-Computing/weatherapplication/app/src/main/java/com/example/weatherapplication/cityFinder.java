package com.example.weatherapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

public class cityFinder extends AppCompatActivity {
    EditText searchCity;
    ImageButton micButton;
    SpeechRecognizer speechRecognizer;
    int flag = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_finder);

        // Initialisation
        ImageView backButton = findViewById(R.id.backButton);
        searchCity = findViewById(R.id.searchCity);
        micButton = findViewById(R.id.micButton);


        // When click the backButton
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // End this activity and automatically redirect to MainActivity
                finish();
            }
        });


        // Pass the city name typed by users to the MainActivity to find its weather
        searchCity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Get city name typed by users
                String newCity = searchCity.getText().toString();

                // Pass from this activity to MainActivity
                Intent intent = new Intent(cityFinder.this, MainActivity.class);
                intent.putExtra("city", newCity);
                startActivity(intent);

                return false;
            }
        });


        // Voice Detection
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("cityFinderActivity", "beginning");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                Log.d("cityFinderActivity", "rmschanged");
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                Log.d("cityFinderActivity", "buffer");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d("cityFinderActivity", "end");
            }

            @Override
            public void onError(int error) {
                Log.d("cityFinderActivity", "error: " + error);
            }

            @Override
            public void onResults(Bundle results) {
                Log.d("cityFinderActivity", "results");
                ArrayList<String> data = results.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                searchCity.setText(data.get(0));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                Log.d("cityFinderActivity", "partialresults");
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.d("cityFinderActivity", "event");
            }
        });

        final Intent sIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        sIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString());


        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag == 0){
                    micButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_24));
                    speechRecognizer.startListening(sIntent);
                    flag = 1;
                }else{
                    micButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_off_24));
                    speechRecognizer.stopListening();
                    flag = 0;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT);
            }else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT);
            }
        }
    }
}