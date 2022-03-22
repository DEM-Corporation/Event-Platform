package dem.xbitly.eventplatform.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import dem.xbitly.eventplatform.databinding.ActivityStartBinding;
import dem.xbitly.eventplatform.network.NetworkManager;

public class StartActivity extends AppCompatActivity {

    private ActivityStartBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartBinding.inflate(getLayoutInflater());

        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        if (prefs.getBoolean("dark_theme", false)==true){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(binding.getRoot());

        binding.toSignInBtn.setOnClickListener(v -> {
            Intent in_intent = new Intent (StartActivity.this, LoginActivity.class);
            startActivity(in_intent);
        });

        binding.toSignUpBtn.setOnClickListener(v -> {
            Intent up_intent = new Intent (StartActivity.this, RegisterActivity.class);
            startActivity(up_intent);
        });

        checkNetwork();
    }

    public void checkNetwork(){
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            if (!connected) {
                Intent in_intent = new Intent (StartActivity.this, InternetErrorConnectionActivity.class);
                startActivity(in_intent);
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
    }
}