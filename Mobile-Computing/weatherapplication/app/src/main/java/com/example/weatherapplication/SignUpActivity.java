package com.example.weatherapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    TextInputEditText regEmail;
    TextInputEditText regPass;
    TextView loginLink;
    Button registerBtn;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Input fields and links on the sign up page
        regEmail = findViewById(R.id.etRegEmail);
        regPass = findViewById(R.id.etRegPass);
        loginLink = findViewById(R.id.tvLoginHere);
        registerBtn = findViewById(R.id.btnRegister);

        auth = FirebaseAuth.getInstance();

        // Validate the input fields and register the user
        registerBtn.setOnClickListener(view -> {
            registerUser();
        });

        // Redirect to login page
        loginLink.setOnClickListener(view -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        });

        ImageView backButton = findViewById(R.id.backButton);
        // When click the backButton
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(SignUpActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }

    // User account creation if fields are valid and creates their FireStore collection to store fav
    private void registerUser() {
        String email = regEmail.getText().toString();
        String password = regPass.getText().toString();
        Map<String, Object> user_data = new HashMap<>();
        ArrayList<String> fav_locations = new ArrayList<String>();
        db = FirebaseFirestore.getInstance();

        if (TextUtils.isEmpty(email)) {
            regEmail.setError("Enter a valid email");
        } else if (TextUtils.isEmpty(password)) {
            regPass.setError("Password cannot be empty");
        } else {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        user_data.put("fav_locations", fav_locations);
                        db.collection("user_data").document(email).set(user_data);

                        Toast.makeText(SignUpActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    } else {
                        Toast.makeText(SignUpActivity.this, "Register failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}