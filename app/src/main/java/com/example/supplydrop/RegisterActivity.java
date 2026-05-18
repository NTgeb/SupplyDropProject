package com.example.supplydrop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    OkHttpClient client;

    Button btnEnter;
    RadioGroup donorRecip;
    TextInputEditText edtName, edtEmail, edtUsername, edtCell, edtAddress, edtCity, edtPassword;
    String userType, name, email, password, username, cell, address, city;

    Validator val = new Validator();
    String postUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        client = new OkHttpClient();
        donorRecip = findViewById(R.id.rgbDonorRecip);
        edtName = findViewById(R.id.txtfName);
        edtEmail = findViewById(R.id.txtemail);
        edtPassword = findViewById(R.id.txtpassword);
        edtUsername = findViewById(R.id.txtusername);
        edtCell = findViewById(R.id.txtcellphone);
        edtAddress = findViewById(R.id.txtaddress);
        edtCity = findViewById(R.id.txtcity);
        btnEnter = findViewById(R.id.btnRegEnter);

        donorRecip.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selection = findViewById(checkedId);
            userType = selection.getText().toString();
        });

        btnEnter.setOnClickListener(v -> {
            name = edtName.getText() != null ? edtName.getText().toString().trim(): "";
            email = edtEmail.getText() != null ? edtEmail.getText().toString().trim(): "";
            password = edtPassword.getText() != null ? edtPassword.getText().toString().trim(): "";
            username = edtUsername.getText() != null ? edtUsername.getText().toString().trim(): "";
            cell = edtCell.getText() != null ? edtCell.getText().toString().trim(): "";
            address = edtAddress.getText() != null ? edtAddress.getText().toString().trim(): "";
            city = edtCity.getText() != null ? edtCity.getText().toString().trim(): "";

            if (userType == null || userType.isEmpty()) {
                Toast.makeText(this, "Please select Donor or Recipient",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            //Empty Fields
            boolean bEmpty = val.empty(name) || val.empty(email) || val.empty(password) ||
                            val.empty(username) || val.empty(cell) || val.empty(address)
                            || val.empty(city);
            if (bEmpty) {
                Toast.makeText(this, "Please fill in all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            //Valid email check
            TextInputLayout txtInLEmail = findViewById(R.id.TextInputLayoutEmail);
            if(!val.email(email)){
                txtInLEmail.setError("Please enter a valid email");
                edtEmail.requestFocus();
                return;
            }
            else{
                txtInLEmail.setError(null);
            }
            //Password Length
            TextInputLayout txtInLPassword = findViewById(R.id.TextInputLayoutPassword);
            if(!val.password(password)){
                txtInLPassword.setError("Password must be between 8-30 characters");
                edtPassword.requestFocus();
                return;
            }
            else{
                txtInLPassword.setError(null);
            }
            //Valid Cellphone
            TextInputLayout txtInLCell = findViewById(R.id.TextInputLayoutCellphone);
            if(!val.phone(cell)){
                txtInLCell.setError("Please enter a valid cellphone number");
                edtCell.requestFocus();
                return;
            }
            else{
                txtInLCell.setError(null);
            }


            postRegister(userType, name, email, password,
                    username, cell, address, city);
        });
    }

    public void postRegister(String userType, String name, String email,
                             String password, String username, String cell,
                             String address, String city) {

        RequestBody requestBody = new FormBody.Builder()
                .add("userType", userType)
                .add("email", email)
                .add("password", password)
                .add("fname", name)
                .add("username", username)
                .add("cellphone", cell)
                .add("address", address)
                .add("city", city)
                .build();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this,
                                "Connection failed",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                runOnUiThread(() -> {
                    if (responseBody.contains("successfully")) {
                        if (userType.equals("Recipient")) {
                            // Fetch recipient profile to store in SharedPreferences
                            fetchRecipientAfterRegister(email);
                        } else {
                            // Donors fetch donor profile then go to DonorActivity
                            fetchDonorAfterRegister(email);
                        }
                    } else {

                        TextInputLayout txtInLUsername = findViewById(R.id.TextInputLayoutUsername);
                        if(responseBody.contains("Username already taken. Please choose a different one.")){
                            txtInLUsername.setError("Username already taken. Please choose a different one.");
                            edtUsername.requestFocus();

                        }
                        else{
                            txtInLUsername.setError(null);
                            Toast.makeText(RegisterActivity.this,
                                    responseBody,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }

    private void fetchRecipientAfterRegister(String email) {
        RequestBody requestBody = new FormBody.Builder()
                .add("email", email)
                .build();
        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2711/get_recipient.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this,
                                "Registered but failed to load profile",
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
                            SharedPreferences prefs = getSharedPreferences(
                                    "SupplyDropPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("recipient_id",
                                    obj.getInt("recipient_id"));
                            editor.putString("email", email);
                            editor.putString("user_type", "Recipient");
                            editor.putString("full_name",
                                    obj.getString("full_name"));
                            editor.apply();

                            Intent intent = new Intent(RegisterActivity.this,
                                    SelectItemsActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void fetchDonorAfterRegister(String email) {
        RequestBody requestBody = new FormBody.Builder()
                .add("email", email)
                .build();
        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2711/get_donor.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this,
                                "Registered but failed to load profile",
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
                            SharedPreferences prefs = getSharedPreferences(
                                    "SupplyDropPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("donor_id",
                                    obj.getInt("donor_id"));
                            editor.putString("email", email);
                            editor.putString("user_type", "Donor");
                            editor.putString("full_name",
                                    obj.getString("full_name"));
                            editor.apply();

                            Intent intent = new Intent(RegisterActivity.this,
                                    DonorActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}