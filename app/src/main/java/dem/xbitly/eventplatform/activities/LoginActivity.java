package dem.xbitly.eventplatform.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.shashank.sony.fancytoastlib.FancyToast;

import dem.xbitly.eventplatform.databinding.ActivityLoginBinding;
import dem.xbitly.eventplatform.network.NetworkManager;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkNetwork();

        mAuth = FirebaseAuth.getInstance();

        binding.signInBtn.setOnClickListener(v -> {
            if (binding.emailEditSignIn.getText().toString().isEmpty() && binding.passwordSignIn.getText().toString().isEmpty()){
                Snackbar.make(v, "Fields cannot be empty", Snackbar.LENGTH_SHORT).show();
            }else {
                mAuth.signInWithEmailAndPassword(binding.emailEditSignIn.getText().toString(), binding.passwordSignIn.getText().toString())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Intent intent = new Intent (LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }else {
                                FancyToast.makeText(getApplicationContext(),"Fields cannot be empty",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                            }
                        });
            }
        });

        binding.backFromLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent (LoginActivity.this, StartActivity.class);
            startActivity(intent);
        });

    }
    public void checkNetwork(){
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            if (!connected) {
                Intent in_intent = new Intent (LoginActivity.this, InternetErrorConnectionActivity.class);
                startActivity(in_intent);
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
    }
}