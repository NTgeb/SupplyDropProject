package com.example.supplydrop;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class RegisterActivity extends AppCompatActivity {

    OkHttpClient client;

    Button btnEnter;
    RadioGroup donorRecip;
    EditText edtName,edtEmail,edtUsername,edtCell,edtAddress,edtCity,edtPassword;
    String userType,name,email,password,username,cell,address,city;


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
        edtName = findViewById(R.id.fullnameTxt);
        edtEmail = findViewById(R.id.txtemail);
        edtPassword = findViewById(R.id.passwordRegTxt);
        edtUsername = findViewById(R.id.usernameTxt);
        edtCell = findViewById(R.id.cellphoneTxt);
        edtAddress = findViewById(R.id.txtAdress);
        edtCity = findViewById(R.id.cityTxt);
        btnEnter = findViewById(R.id.btnRegEnter);

        donorRecip.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                RadioButton selection = findViewById(checkedId);

                userType = selection.getText().toString();
            }
        });

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = edtName.getText().toString();
                email = edtEmail.getText().toString();
                password = edtPassword.getText().toString();
                username = edtUsername.getText().toString();
                cell = edtCell.getText().toString();
                address = edtAddress.getText().toString();
                city = edtCity.getText().toString();

                postRegister(userType,name,email,password,username,cell,address,city);

            }
        });

    }

    public void postRegister(String userType,String name,String email,String password,String username,String cell,String address,String city){

                RequestBody requestBody = new FormBody.Builder()
                        .add("userType" , userType)
                        .add("email" , email)
                        .add("password",password)
                        .add("fname" , name)
                        .add("username" , username)
                        .add("cellphone" , cell)
                        .add("address" , address)
                        .add("city" , city)
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
                                Toast.makeText(RegisterActivity.this,
                                        "Connection failed",
                                        Toast.LENGTH_SHORT).show();

                            }
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        final String responseBody = response.body().string();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this,
                                        responseBody,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });


    }


}