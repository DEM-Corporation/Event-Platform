package dem.xbitly.eventplatform.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import dem.xbitly.eventplatform.databinding.ActivitySettingsBinding;
import dem.xbitly.eventplatform.network.NetworkManager;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkNetwork();

        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        if (prefs.getBoolean("dark_theme", false)){
            binding.darkThemeSwitch.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            binding.darkThemeSwitch.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        binding.backFromSettingsBtn.setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, MainActivity.class)));

        binding.toProfileSettingsBtn.setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, ProfileSettings.class)));

        binding.darkThemeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    if (!prefs.getBoolean("dark_theme", false)){
                        binding.darkThemeSwitch.setChecked(true);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("dark_theme", true);
                        editor.apply();
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                    Log.d("dark_theme", "theme was changed to dark theme");
                }else{
                    if (prefs.getBoolean("dark_theme", false)){
                        binding.darkThemeSwitch.setChecked(false);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("dark_theme", false);
                        editor.apply();
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    Log.d("dark_theme", "theme was changed to light theme");
                }
            }
        });
    }

    public void checkNetwork(){
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            if (!connected) {
                Intent in_intent = new Intent (SettingsActivity.this, InternetErrorConnectionActivity.class);
                startActivity(in_intent);
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
    }
}