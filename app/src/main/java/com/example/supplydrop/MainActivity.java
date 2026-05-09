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

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    OkHttpClient client;
    EditText emailtxt;
    EditText passwordtxt;
    String postUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/login.php";
    TextView textView;

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

        client = new OkHttpClient();
        Button btnEnter = findViewById(R.id.btnEnter);
        textView = findViewById(R.id.loginLbl);

        btnEnter.setOnClickListener(new View.OnClickListener() {//Calls the Post method once the user clicks on button
            @Override
            public void onClick(View v) {
                post();
            }
        });
    }

        public void post(){
            emailtxt = findViewById(R.id.emailTxt);
            passwordtxt = findViewById(R.id.passwordTxt);

            String email = emailtxt.getText().toString();
            String password = passwordtxt.getText().toString();

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
                    textView.setText("Failed");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {//Check if password is correct etc
                    final String responseBody = response.body().string();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(responseBody);
                        }
                    });

                }
            });
        }
}