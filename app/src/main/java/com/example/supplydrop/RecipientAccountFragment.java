package com.example.supplydrop;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecipientAccountFragment extends Fragment {

    OkHttpClient client = new OkHttpClient();
    String baseUrl = "https://wmc.ms.wits.ac.za/students/sgroup2711/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.activity_recipient_account,container, false);
        // this will connect to the XML to this

        LinearLayout editProfileRow = view.findViewById(R.id.editProfileRow);
        LinearLayout logoutRow = view.findViewById(R.id.logoutRow);
        LinearLayout deleteAccountRow = view.findViewById(R.id.deleteAccountRow);

        editProfileRow.setOnClickListener(v ->
        {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivity(intent);
            // this basically opens to a new screen(the edit profile screen)
        });

        logoutRow.setOnClickListener(v ->
        {
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent); // takes out the prevous screens from memory
        });

        deleteAccountRow.setOnClickListener(v ->
        {
            new AlertDialog.Builder(requireContext()).setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) ->
                    {
                        deleteAccount();
                    })
                    .setNegativeButton("Cancel", null).show();
            //this is the pop-up incase someone clicks delete by mistake
        });

        return view;
    }

    private void deleteAccount()
    {
        SharedPreferences prefs = requireContext().getSharedPreferences("SupplyDropPrefs", android.content.Context.MODE_PRIVATE);
        int recipientId = prefs.getInt("recipient_id", -1);

        if (recipientId == -1) // if ID is not there default is -1, that is why i did that
        {
            Toast.makeText(requireContext(), "Error: recipient not found", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestBody = new FormBody.Builder().add("recipient_id", String.valueOf(recipientId)).build(); //sends recip to PHP

        Request request = new Request.Builder().url(baseUrl + "delete_recipient_account.php").post(requestBody).build(); // PHP requestto delete_recip_acc PHP file

        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) //if no internet
            {
                if (getActivity() == null)
                {
                    return;
                }
                getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Connection failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                final String body = response.body().string();
                if (getActivity() == null)
                {
                    return;
                }
                getActivity().runOnUiThread(() ->
                {
                    try
                    {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getBoolean("success"))
                        {
                            requireContext().getSharedPreferences("SupplyDropPrefs", android.content.Context.MODE_PRIVATE).edit().clear().apply();
                           //deletes the saved session so they are logged out
                            Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show();Intent intent = new Intent(requireContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent); // takes them back login
                        }
                        else
                        {
                            Toast.makeText(requireContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}