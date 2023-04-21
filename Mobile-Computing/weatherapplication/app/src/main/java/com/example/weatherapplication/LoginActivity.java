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

import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText loginEmail;
    TextInputEditText loginPass;
    TextView registerLink;
    Button loginBtn;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Input fields and navigation link on the Login UI Page
        loginEmail = findViewById(R.id.etLoginEmail);
        loginPass = findViewById(R.id.etLoginPass);
        registerLink = findViewById(R.id.tvRegisterHere);
        loginBtn = findViewById(R.id.btnLogin);

        auth = FirebaseAuth.getInstance();

        // Login the user and validate their credentials through firebase
        loginBtn.setOnClickListener(view -> {
            loginUser();
        });

        // Redirect to the signup page when register is tapped.
        registerLink.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        // Back button on the SignUp page
        ImageView backButton = findViewById(R.id.backButton);
        // When click the backButton returns to MainActivity
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(LoginActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

    }

    // Authentication of the user through firebase instance
    private void loginUser() {
        String email = loginEmail.getText().toString();
        String password = loginPass.getText().toString();

        if (TextUtils.isEmpty(email)) {
            loginEmail.setError("Enter a valid email");
            loginEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            loginPass.setError("Password cannot be empty");
            loginPass.requestFocus();
        } else {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}