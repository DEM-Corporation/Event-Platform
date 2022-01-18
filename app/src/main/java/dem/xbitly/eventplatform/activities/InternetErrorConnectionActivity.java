package dem.xbitly.eventplatform.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

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
            if(NetworkManager.isNetworkAvailable(this)){
                onBackPressed();
            }
        });
    }
}