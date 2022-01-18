package dem.xbitly.eventplatform.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import dem.xbitly.eventplatform.databinding.ActivityLoginBinding;
import dem.xbitly.eventplatform.databinding.ActivityMembersBinding;
import dem.xbitly.eventplatform.members.MembersAdapter;

public class MembersActivity extends AppCompatActivity {

    ActivityMembersBinding binding;
    String id[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMembersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkNetwork();

        binding.backFromChatBtn.setOnClickListener(view -> onBackPressed());

        String path;
        if(getIntent().getBooleanExtra("private", false)){
            path = "PrivateEvents";
        } else {
            path = "PublicEvents";
        }

        FirebaseDatabase.getInstance().getReference(path).child(getIntent().getStringExtra("eventID")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String go = Objects.requireNonNull(snapshot.child("go").getValue()).toString();
                id = go.split(",");
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MembersActivity.this);
                binding.membersRv.setLayoutManager(linearLayoutManager);
                MembersAdapter membersAdapter = new MembersAdapter(id);
                binding.membersRv.setAdapter(membersAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
                Intent in_intent = new Intent (MembersActivity.this, InternetErrorConnectionActivity.class);
                startActivity(in_intent);
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
    }
}