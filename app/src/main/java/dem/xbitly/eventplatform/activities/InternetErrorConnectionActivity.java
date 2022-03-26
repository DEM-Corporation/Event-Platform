package dem.xbitly.eventplatform.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import dem.xbitly.eventplatform.databinding.ActivityInternetErrorConnectionBinding;
import dem.xbitly.eventplatform.network.NetworkManager;

public class InternetErrorConnectionActivity extends AppCompatActivity {

    private ActivityInternetErrorConnectionBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInternetErrorConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRetry.setOnClickListener(view -> {
            if(checkNetwork()){
                Intent intent = new Intent(InternetErrorConnectionActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean checkNetwork(){
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return false;
    }
}