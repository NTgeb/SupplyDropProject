package com.example.supplydrop;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {
    OkHttpClient client;
    EditText emailtxt;
    EditText passwordtxt;
    String postUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/login.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Switching the password visibility
        EditText passwordTxt = findViewById(R.id.passwordTxt);
        final boolean[] isPasswordVisible = {false};

        passwordTxt.setOnTouchListener((v,event) -> {//Cerating the view and event
            if(event.getAction() == MotionEvent.ACTION_UP){//If the action is the user lifting up their finger from the icon
                if(event.getRawX() >= (passwordTxt.getRight() - passwordTxt.getCompoundDrawables()[2].getBounds().width() )){// Finding the events x locatoin, if its greater than the position of the icon then execute the visibility code

                    if(isPasswordVisible[0]){//Hide password
                        passwordTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);//Changes the text type of the Plain Edit Text
                        passwordTxt.setCompoundDrawablesWithIntrinsicBounds(//Switches the icon to the visibilty option
                                R.drawable.password,0,R.drawable.baseline_visibility_24,0
                        );
                        isPasswordVisible[0] = false;//Changes the flag
                    }
                    else {//Show password
                        passwordTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordTxt.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.password,0,R.drawable.baseline_visibility_off_24,0
                        );
                        isPasswordVisible[0] = true;
                    }

                    passwordTxt.setSelection(passwordTxt.getText().length());//Sets the cursor to the end of the textbox
                    return true;//Returns true for tapping on the icon

                }
            }
            return false;//Returns false for tapping anywhere else than the icon
        });

        //Switching to the register page
        TextView registerTxt = findViewById(R.id.registerTxt);

        registerTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        //Checking the login through the database

        emailtxt = findViewById(R.id.emailTxt);
        passwordtxt = findViewById(R.id.passwordTxt);
        client = new OkHttpClient();
        Button btnEnter = findViewById(R.id.btnEnter);

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailtxt.getText().toString().trim();
                String password = passwordtxt.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "Please enter email and password",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                post(email, password);
            }
        });
    }

    public void post(String email, String password){

            RequestBody requestBody = new FormBody.Builder()
                    .add("email",email)
                    .add("password",password)
                    .build();
            Request request = new Request.Builder()
                    .url(postUrl)
                    .post(requestBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,
                                    "Connection failed. Check your internet.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {//Check if password is correct etc
                    final String responseBody = response.body().string();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                // Turn the response text into a JSON object we can read
                                JSONObject obj = new JSONObject(responseBody);

                                if (obj.getBoolean("success")) {
                                    String userType  = obj.getString("user_type");
                                    String userEmail = obj.getString("email");
                                    // Fetch full profile and store in SharedPreferences before navigating
                                    fetchUserProfile(userEmail, userType);
                                }

                                else
                                {
                                    // Login failed — show the error message that came from PHP
                                    Toast.makeText(MainActivity.this,
                                            obj.getString("message"),
                                            Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {
                                // Something went wrong reading the response
                                Toast.makeText(MainActivity.this,
                                        "Error: " + responseBody,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            });
        }

    private void fetchUserProfile(String email, String userType) {
        String profileUrl;
        if (userType.equals("Donor")) {
            profileUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/get_donor.php";
        } else {
            profileUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/get_recipient.php";
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("email", email)
                .build();
        Request request = new Request.Builder()
                .url(profileUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "Failed to load profile",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(responseBody);
                        if (obj.getBoolean("success")) {

                            // Save to SharedPreferences
                            SharedPreferences prefs = getSharedPreferences(
                                    "SupplyDropPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("email", email);
                            editor.putString("user_type", userType);

                            if (userType.equals("Donor")) {
                                editor.putInt("donor_id",
                                        obj.getInt("donor_id"));
                                editor.putString("full_name",
                                        obj.getString("full_name"));
                            } else {
                                editor.putInt("recipient_id",
                                        obj.getInt("recipient_id"));
                                editor.putString("full_name",
                                        obj.getString("full_name"));
                            }
                            editor.apply();

                            Toast.makeText(MainActivity.this,
                                    "Welcome!", Toast.LENGTH_SHORT).show();

                            Intent intent;
                            if (userType.equals("Donor")) {
                                intent = new Intent(MainActivity.this,
                                        DonorActivity.class);
                            } else {
                                intent = new Intent(MainActivity.this,
                                        RecipientActivity.class);
                            }
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Error: " + obj.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this,
                                "Error: " + responseBody,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}