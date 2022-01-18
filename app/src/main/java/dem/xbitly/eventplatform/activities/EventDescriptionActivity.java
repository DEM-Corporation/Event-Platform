package dem.xbitly.eventplatform.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import dem.xbitly.eventplatform.R;
import dem.xbitly.eventplatform.databinding.ActivityEventDescriptionBinding;
import dem.xbitly.eventplatform.network.NetworkManager;

public class EventDescriptionActivity extends AppCompatActivity {

    private ActivityEventDescriptionBinding binding;
    private DatabaseReference ref, ref2;
    boolean r = true;

    private int count;

    private FusedLocationProviderClient fusedLocationClient;

    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDescriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkNetwork();

        FirebaseDatabase dBase = FirebaseDatabase.getInstance();
        ref = dBase.getReference("Invite");
        ref2 = dBase.getReference("Users/"+getIntent().getSerializableExtra("userID").toString());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        binding.backFromEventDescBtn.setOnClickListener(v -> {
            onBackPressed();
        });

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                        }
                    }
                });

        binding.nextBtnFromEventdiskBtn.setOnClickListener(v -> {
            if (!binding.eventDesc.getText().toString().isEmpty()){
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
                            ref.child(String.valueOf((count + 1))).child("text").setValue(binding.eventDesc.getText().toString());
                            ref.child(String.valueOf((count + 1))).child("userID").setValue(getIntent().getSerializableExtra("userID").toString());
                            ref.child(String.valueOf((count + 1))).child("eventID").setValue(getIntent().getSerializableExtra("eventID").toString());
                            FirebaseDatabase.getInstance().getReference("PublicEvents").child(Integer.toString(getIntent().getIntExtra("eventID", 0)))
                                    .child("go").setValue(" ," + getIntent().getSerializableExtra("userID").toString());

                            int finalCount = count;

                            ref2.addValueEventListener(new ValueEventListener() {

                                boolean e = true;

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                    String str;
                                    if(e) {
                                        if (Objects.requireNonNull(snapshot2.child("myInvites").getValue()).toString().equals("")){
                                            str = Objects.requireNonNull(snapshot2.child("myInvites").getValue()).toString() + (finalCount + 1);
                                        } else {
                                            str = Objects.requireNonNull(snapshot2.child("myInvites").getValue()).toString() + "," + (finalCount + 1);
                                        }
                                        ref2.child("myInvites").setValue(str);
                                        e = false;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                            r = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Intent intent = new PlacePicker.IntentBuilder()
                        .setLatLong(latitude, longitude)
                        .showLatLong(true)
                        .setMapType(MapType.NORMAL)
                        .setFabColor(R.color.blue)
                        .setMarkerDrawable(R.drawable.ic_location_marker)
                        .build(EventDescriptionActivity.this);

                startActivityForResult(intent, Constants.PLACE_PICKER_REQUEST);

            }else{
                FancyToast.makeText(getApplicationContext(),"Fields cannot be empty",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                AddressData addressData = data.getParcelableExtra(Constants.ADDRESS_INTENT);
                double latitude = addressData.getLatitude();
                double longitude = addressData.getLongitude();
                FirebaseDatabase.getInstance().getReference("PublicEvents")
                        .child(Integer.toString(getIntent().getIntExtra("eventID", 0)))
                        .child("adress").child("latitude").setValue(latitude);
                FirebaseDatabase.getInstance().getReference("PublicEvents")
                        .child(Integer.toString(getIntent().getIntExtra("eventID", 0)))
                        .child("adress").child("longitude").setValue(longitude);

                FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("Chats").child("count").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        count = Integer.parseInt(Objects.requireNonNull(task.getResult().getValue()).toString());
                        count++;
                        FirebaseDatabase.getInstance().getReference("Chats").child("count").get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()){
                                int count2 = Integer.parseInt(Objects.requireNonNull(task1.getResult().getValue()).toString());
                                count2++;

                                FirebaseDatabase.getInstance().getReference("PublicEvents")
                                        .child(Integer.toString(getIntent().getIntExtra("eventID", 0))).child("chatID").setValue(count2);

                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("UserPrivateEvents")
                                        .child("count").get().addOnCompleteListener(task11 -> {
                                            if (task11.isSuccessful()){
                                                int count = Integer.parseInt(task11.getResult().getValue().toString());
                                                count++;
                                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("UserPrivateEvents")
                                                        .child("count").setValue(count);
                                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("UserPrivateEvents")
                                                        .child(Integer.toString(getIntent().getIntExtra("eventID", 0))).child("eventID").setValue(getIntent().getIntExtra("eventID", 0));
                                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("UserPrivateEvents")
                                                        .child(Integer.toString(getIntent().getIntExtra("eventID", 0))).child("privacy").setValue("no");
                                            }
                                        });

                                int finalCount = count2;
                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("count")
                                        .get().addOnCompleteListener(task112 -> {
                                            if (task112.isSuccessful()){
                                                int chatID = Integer.parseInt(Objects.requireNonNull(task112.getResult().getValue()).toString());
                                                chatID++;

                                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("count")
                                                        .setValue(chatID);
                                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("chats")
                                                        .child(Integer.toString(getIntent().getIntExtra("eventID", 0))).child("chatID").setValue(finalCount);
                                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("chats")
                                                        .child(Integer.toString(getIntent().getIntExtra("eventID", 0))).child("name").setValue(getIntent().getStringExtra("event_name"));
                                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("chats")
                                                        .child(Integer.toString(getIntent().getIntExtra("eventID", 0))).child("privacy").setValue("no");

                                            }
                                        });

                                FirebaseDatabase.getInstance().getReference("Chats").child("count").setValue(count2);
                                FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(count2)).child("event_number").setValue(Integer.toString(getIntent().getIntExtra("event_number", 0)));
                                FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(count2)).child("members").child("count").setValue(1);
                                FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(count2)).child("members").child("1").setValue(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                                FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(count2)).child("messages").child("count").setValue(1);
                                FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(count2)).child("messages").child("all_messages").child("1")
                                        .child("from").setValue("App");
                                FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(count2)).child("privacy").child("no");
                                FirebaseDatabase.getInstance().getReference("Chats").child(String.valueOf(count2)).child("messages").child("all_messages").child("1")
                                        .child("userID").setValue("app");
                                FirebaseDatabase.getInstance().getReference("Chats").child(String.valueOf(count2)).child("messages").child("all_messages").child("1")
                                        .child("text").setValue("Welcome to the chat of the event. Please be polite to the rest of the chat");
                                FirebaseDatabase.getInstance().getReference("Chats").child(String.valueOf(count2)).child("messages").child("all_messages").child("1")
                                        .child("time").setValue("");
                                FirebaseDatabase.getInstance().getReference("Chats").child(String.valueOf(count2)).child("privacy").setValue("no");

                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("count").setValue(count);

                                FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(count2)).child("event_number").setValue(Integer.toString(getIntent().
                                        getIntExtra("eventID", 0)));
                            }
                        });
                    }
                });

                Intent intent = new Intent (EventDescriptionActivity.this, UsersInvitationActivity.class);
                intent.putExtra("privacy", false);
                intent.putExtra("event_number", getIntent().getIntExtra("eventID", 0));
                startActivity(intent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void checkNetwork(){
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            if (!connected) {
                Intent in_intent = new Intent (EventDescriptionActivity.this, InternetErrorConnectionActivity.class);
                startActivity(in_intent);
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
    }
}