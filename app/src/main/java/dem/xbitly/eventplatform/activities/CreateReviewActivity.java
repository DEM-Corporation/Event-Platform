package dem.xbitly.eventplatform.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import dem.xbitly.eventplatform.databinding.ActivityCreateReviewBinding;
import dem.xbitly.eventplatform.network.NetworkManager;

public class CreateReviewActivity extends AppCompatActivity {

    private ActivityCreateReviewBinding binding;

    private DatabaseReference ref;
    private FirebaseAuth mAuth;

    boolean r = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityCreateReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkNetwork();

        FirebaseDatabase dBase = FirebaseDatabase.getInstance();
        ref = dBase.getReference("Reviews");
        mAuth = FirebaseAuth.getInstance();

        binding.backFromCreateReviewBtn.setOnClickListener(view -> onBackPressed());

        binding.eventName.setText(getIntent().getSerializableExtra("nameEvent").toString());

        binding.nextBtnFromEventdiskBtn.setOnClickListener(view -> {
            String review_text = binding.eventReview.getText().toString();
            if(!review_text.isEmpty()){
                Date date = new Date();
                SimpleDateFormat formatForDate = new SimpleDateFormat("dd.MM.yyyy");
                SimpleDateFormat formatForTime = new SimpleDateFormat("hh:mm");

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(r) {
                            int count = 0;
                            try {
                                count = Integer.parseInt(Objects.requireNonNull(snapshot.child("count").getValue()).toString());
                            } catch (Exception e) {
                                ref.child("count").setValue(0);
                            }

                            ref.child("count").setValue((count + 1));
                            ref.child(String.valueOf((count + 1))).child("date").setValue(formatForDate.format(date));
                            ref.child(String.valueOf((count + 1))).child("time").setValue(formatForTime.format(date));
                            ref.child(String.valueOf((count + 1))).child("text").setValue(review_text);
                            ref.child(String.valueOf((count + 1))).child("like").setValue("");
                            ref.child(String.valueOf((count + 1))).child("userID").setValue(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                            ref.child(String.valueOf((count + 1))).child("eventID").setValue(getIntent().getSerializableExtra("eventID").toString());

                            int finalCount = count;

                            ref = dBase.getReference("Users/"+Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

                            ref.addValueEventListener(new ValueEventListener() {

                                boolean e = true;

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                    String str;
                                    if(e) {
                                        if (Objects.requireNonNull(snapshot2.child("myReviews").getValue()).toString().equals("")){
                                            str = Objects.requireNonNull(snapshot2.child("myReviews").getValue()).toString() + (finalCount + 1);
                                        } else {
                                            str = Objects.requireNonNull(snapshot2.child("myReviews").getValue()).toString() + "," + (finalCount + 1);
                                        }
                                        ref.child("myReviews").setValue(str);
                                        e = false;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            r = false;

                            onBackPressed();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                FancyToast.makeText(getApplicationContext(),"Fields cannot be empty",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
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
                Intent in_intent = new Intent (CreateReviewActivity.this, InternetErrorConnectionActivity.class);
                startActivity(in_intent);
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
    }
}