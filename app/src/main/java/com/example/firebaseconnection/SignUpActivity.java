package com.example.firebaseconnection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.provider.Contacts;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class SignUpActivity extends AppCompatActivity {
    EditText signUpName, signUpEmail, signUpUsername, signUpPassword;
    Button btnSignUp, btnLoginRedirect, btnGoogleRegister;
    FirebaseDatabase database;
    private FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    static String UID = null;
    static boolean isRegistered = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        btnSignUp = findViewById(R.id.btnSignUp);
        btnLoginRedirect = findViewById(R.id.btnLoginRedirect);
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister);

        btnSignUp.setOnClickListener(v -> {
            registerAccount("jeastel", "aizerner", "jeastel@gmail.com", "jeastel");
        });

        btnLoginRedirect.setOnClickListener(v -> {
            addUserToFirestore("123", "jeastel@gmail.com", "aizerner", "jeastel");
            Intent intent =  new Intent(SignUpActivity.this, LogInActivity.class);
            startActivity(intent);
        });

        btnGoogleRegister.setOnClickListener(v -> {
            ThirdPartyAuth googleAuth = new ThirdPartyAuth();
            googleAuth.googleAuth();
            String googleEmail = googleAuth.getGoogleMail();
            String googleDisplayName = googleAuth.getGoogleDisplayname();
            String UID = googleAuth.getUID();
            addUserToFirestore(UID, googleEmail, googleDisplayName, googleDisplayName);
        });
    }

    private void onCompleteRegistration(boolean isSuccessful, FirebaseUser user) {
        if (isSuccessful && user != null) {
            addUserToFirestore(user.getUid(), "jeastel@gmail.com", "aizerner", "jeastel");
            Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(SignUpActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void registerAccount(String name, String username, String email, String password) {
        Log.d("TAG", "Starting registration");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("TAG", "createUserWithEmail:success");
                        Toast.makeText(SignUpActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        onCompleteRegistration(true, user);
                    } else {
                        Log.w("TAG", "createUserWithEmail:failure", task.getException());
                        onCompleteRegistration(false, null);
                    }
                });
    }


    private void addUserToFirestore(String userId, String email, String username, String name) {
        Log.i("TAG", "Adding user to Firestore with ID: " + userId);
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("username", username);
        userData.put("name", name);
        userData.put("coins", 14);
        userData.put("cats", new HashMap<String, String>());
        userData.put("tasks", new HashMap<String, Object>());

        if (firebaseFirestore == null) {
            Log.e("TAG", "Firestore is not initialized");
            return;
        }

        firebaseFirestore.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d("TAG", "User added to Firestore"))
                .addOnFailureListener(e -> Log.e("TAG", "Error adding user to Firestore", e));
    }
}