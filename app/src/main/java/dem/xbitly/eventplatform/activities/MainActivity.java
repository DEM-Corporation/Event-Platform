package dem.xbitly.eventplatform.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dem.xbitly.eventplatform.R;
import dem.xbitly.eventplatform.bottomsheet.BottomSheetEventDialog;
import dem.xbitly.eventplatform.databinding.ActivityMainBinding;
import dem.xbitly.eventplatform.network.NetworkManager;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        checkNetwork();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        mAuth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        } else {
            BottomNavigationView navView = findViewById(R.id.nav_view);
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_profile, R.id.navigation_message)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavigationUI.setupWithNavController(navView, navController);
        }

        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
            @Override
            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                Uri deepLink = null;
                if (pendingDynamicLinkData != null){
                    deepLink = pendingDynamicLinkData.getLink();
                }
                if (deepLink != null){
                    String event_number_in_private_events = deepLink.getQueryParameter("eventID");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                    // set the custom dialog layout
                    final View customLayout = getLayoutInflater().inflate(R.layout.accept_invite_dialog, null);
                    builder.setView(customLayout);
                    Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(customLayout);
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(false);
                    Button yes = customLayout.findViewById(R.id.btn_accept_invite_link);
                    Button no = customLayout.findViewById(R.id.btn_decline_invite_link);
                    TextView name = customLayout.findViewById(R.id.event_name_txt);

                    if (Integer.parseInt(event_number_in_private_events) == 0 || Integer.parseInt(event_number_in_private_events) % 2 == 0){
                        FirebaseDatabase.getInstance().getReference("PublicEvents").child(event_number_in_private_events).child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()){
                                    name.setText("Do you accept invite to " + task.getResult().getValue().toString() + " event?");
                                }
                            }
                        });
                    }else{
                        FirebaseDatabase.getInstance().getReference("PrivateEvents").child(event_number_in_private_events).child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()){
                                    name.setText("Do you accept invite to " + task.getResult().getValue().toString() + " event?");
                                }
                            }
                        });
                    }

                    name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Integer.parseInt(event_number_in_private_events) == 0 || Integer.parseInt(event_number_in_private_events) % 2 == 0){
                                FirebaseDatabase.getInstance().getReference("PublicEvents").child(event_number_in_private_events).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        String name = snapshot.child("name").getValue().toString();
                                        String time = snapshot.child("time").getValue().toString();
                                        String date = snapshot.child("date").getValue().toString();
                                        String longitude = snapshot.child("adress").child("longitude").getValue().toString();
                                        String latitude = snapshot.child("adress").child("latitude").getValue().toString();
                                        double latitude_d = Double.parseDouble(Objects.requireNonNull(latitude));
                                        double longitude_d = Double.parseDouble(Objects.requireNonNull(longitude));
                                        Geocoder geocoder;
                                        List<Address> addresses;
                                        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                                        String go = Objects.requireNonNull(snapshot.child("go").getValue()).toString();
                                        int count = go.split(",").length - 1;
                                        String count_bs = Integer.toString(count);

                                        try {
                                            addresses = geocoder.getFromLocation(latitude_d, longitude_d, 1);

                                            String address = addresses.get(0).getAddressLine(0);

                                            BottomSheetEventDialog bottomSheetEventDialog = new BottomSheetEventDialog(event_number_in_private_events, name,
                                                    address, count_bs, date, time, false, true, false);
                                            bottomSheetEventDialog.show(getSupportFragmentManager(), "Event info");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                            }else{
                                FirebaseDatabase.getInstance().getReference("PrivateEvents").child(event_number_in_private_events).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        String name = snapshot.child("name").getValue().toString();
                                        String time = snapshot.child("time").getValue().toString();
                                        String date = snapshot.child("date").getValue().toString();
                                        String longitude = snapshot.child("adress").child("longitude").getValue().toString();
                                        String latitude = snapshot.child("adress").child("latitude").getValue().toString();
                                        double latitude_d = Double.parseDouble(Objects.requireNonNull(latitude));
                                        double longitude_d = Double.parseDouble(Objects.requireNonNull(longitude));
                                        Geocoder geocoder;
                                        List<Address> addresses;
                                        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                                        String go = Objects.requireNonNull(snapshot.child("go").getValue()).toString();
                                        int count = go.split(",").length - 1;
                                        String count_bs = Integer.toString(count);

                                        try {
                                            addresses = geocoder.getFromLocation(latitude_d, longitude_d, 1);

                                            String address = addresses.get(0).getAddressLine(0);

                                            BottomSheetEventDialog bottomSheetEventDialog = new BottomSheetEventDialog(event_number_in_private_events, name,
                                                    address, count_bs, date, time, false, true, false);
                                            bottomSheetEventDialog.show(getSupportFragmentManager(), "Event info");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                            }

                        }
                    });
                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            writeDataToDB(event_number_in_private_events);
                            dialog.dismiss();
                        }
                    });

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });




                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                System.out.println("ERROR!!!!");
            }
        });
    }

    public void writeDataToDB(String event_number_in_private_events){
        if (Integer.parseInt(event_number_in_private_events) == 0 || Integer.parseInt(event_number_in_private_events) % 2 == 0){
            FirebaseDatabase.getInstance().getReference("PublicEvents").child(event_number_in_private_events).child("go").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()){
                        String go = task.getResult().getValue().toString();
                        if (!go.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            FirebaseDatabase.getInstance().getReference("PublicEvents").child(event_number_in_private_events).child("go")
                                    .setValue(go + "," + FirebaseAuth.getInstance().getCurrentUser().getUid());
                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("UserPrivateEvents").child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()){
                                        int count = Integer.parseInt(task.getResult().getValue().toString());
                                        count++;
                                        String eventId = Integer.toString(count);
                                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("UserPrivateEvents").child(event_number_in_private_events).child("eventID").setValue(event_number_in_private_events);
                                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("UserPrivateEvents").child(event_number_in_private_events).child("privacy").setValue("yes");
                                        FirebaseDatabase.getInstance().getReference("PublicEvents").child(event_number_in_private_events).child("name")
                                                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                                if (task.isSuccessful()){
                                                    String name = task.getResult().getValue().toString();
                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("chats")
                                                            .child(event_number_in_private_events).child("name").setValue(name);
                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("chats")
                                                            .child(event_number_in_private_events).child("privacy").setValue("yes");
                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats")
                                                            .child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                                            if (task.isSuccessful()){
                                                                int count_j = Integer.parseInt(task.getResult().getValue().toString());
                                                                count_j++;
                                                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats")
                                                                        .child("count").setValue(count_j);

                                                                FirebaseDatabase.getInstance().getReference("PublicEvents").child(event_number_in_private_events).child("chatID").get()
                                                                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                                                                if (task.isSuccessful()){
                                                                                    String chatID = task.getResult().getValue().toString();
                                                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("chats")
                                                                                            .child(event_number_in_private_events).child("chatID").setValue(chatID);

                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("UserPrivateEvents").child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                                if (task.isSuccessful()){
                                                    int count_o = Integer.parseInt(task.getResult().getValue().toString());
                                                    count_o++;
                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .child("UserPrivateEvents").child("count").setValue(count_o);


                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }else{
            FirebaseDatabase.getInstance().getReference("PrivateEvents").child(event_number_in_private_events).child("go").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()){
                        String go = task.getResult().getValue().toString();
                        if (!go.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            FirebaseDatabase.getInstance().getReference("PrivateEvents").child(event_number_in_private_events).child("go")
                                    .setValue(go + "," + FirebaseAuth.getInstance().getCurrentUser().getUid());
                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("UserPrivateEvents").child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()){
                                        int count = Integer.parseInt(task.getResult().getValue().toString());
                                        count++;
                                        String eventId = Integer.toString(count);
                                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("UserPrivateEvents").child(event_number_in_private_events).child("eventID").setValue(event_number_in_private_events);
                                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("UserPrivateEvents").child(event_number_in_private_events).child("privacy").setValue("yes");
                                        FirebaseDatabase.getInstance().getReference("PrivateEvents").child(event_number_in_private_events).child("name")
                                                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                                if (task.isSuccessful()){
                                                    String name = task.getResult().getValue().toString();
                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("chats")
                                                            .child(event_number_in_private_events).child("name").setValue(name);
                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("chats")
                                                            .child(event_number_in_private_events).child("privacy").setValue("yes");
                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats")
                                                            .child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                                            if (task.isSuccessful()){
                                                                int count_j = Integer.parseInt(task.getResult().getValue().toString());
                                                                count_j++;
                                                                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats")
                                                                        .child("count").setValue(count_j);

                                                                FirebaseDatabase.getInstance().getReference("PrivateEvents").child(event_number_in_private_events).child("chatID").get()
                                                                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                                                                if (task.isSuccessful()){
                                                                                    String chatID = task.getResult().getValue().toString();
                                                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("chats")
                                                                                            .child(event_number_in_private_events).child("chatID").setValue(chatID);

                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("UserPrivateEvents").child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                                                if (task.isSuccessful()){
                                                    int count_o = Integer.parseInt(task.getResult().getValue().toString());
                                                    count_o++;
                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .child("UserPrivateEvents").child("count").setValue(count_o);


                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }

    }

    public void checkNetwork(){
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            if (!connected) {
                Intent in_intent = new Intent (MainActivity.this, InternetErrorConnectionActivity.class);
                startActivity(in_intent);
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
    }

}