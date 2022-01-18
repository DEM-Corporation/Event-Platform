package dem.xbitly.eventplatform.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shashank.sony.fancytoastlib.FancyToast;

import dem.xbitly.eventplatform.databinding.ActivityProfileSettingsBinding;
import dem.xbitly.eventplatform.network.NetworkManager;

public class ProfileSettings extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private FirebaseAuth mAuth;

    private ActivityProfileSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkNetwork();

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Users").child(mAuth.getCurrentUser().getUid());

        binding.maleCheck.setOnClickListener(v -> binding.femaleCheck.setChecked(false));

        binding.femaleCheck.setOnClickListener(v -> binding.maleCheck.setChecked(false));


        binding.backFromProfileSettingsBtn.setOnClickListener(v -> startActivity(new Intent(ProfileSettings.this, SettingsActivity.class)));


        binding.applyChangesProfileSettingsBtn.setOnClickListener(v -> {
            if (binding.usernameProfileSettings.getText().toString().isEmpty() || (!binding.maleCheck.isChecked() && !binding.femaleCheck.isChecked())){
                FancyToast.makeText(getApplicationContext(),"Fields cannot be empty",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            }
            else {
                String gender = binding.maleCheck.isChecked() ? "male" : "female";
                ref.child("gender").setValue(gender);
                ref.child("name").setValue( binding.usernameProfileSettings.getText().toString());
                startActivity(new Intent (ProfileSettings.this, SettingsActivity.class));
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
                Intent in_intent = new Intent (ProfileSettings.this, InternetErrorConnectionActivity.class);
                startActivity(in_intent);
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
    }
}