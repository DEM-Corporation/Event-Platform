package dem.xbitly.eventplatform.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import dem.xbitly.eventplatform.R;
import dem.xbitly.eventplatform.databinding.ActivityEventPrivateBinding;
import dem.xbitly.eventplatform.network.NetworkManager;
import ru.semkin.yandexplacepicker.PlaceParcelable;
import ru.semkin.yandexplacepicker.YandexPlacePicker;

public class PrivateEventActivity extends AppCompatActivity {

    private ActivityEventPrivateBinding binding; //ViewBinding

    //Database
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference ref;

    private int event_number; //?????????? ????????????

    Calendar dateAndTime = Calendar.getInstance();

    //All info about event
    private HashMap<String, String> event_info = new HashMap<>();

    private FusedLocationProviderClient fusedLocationClient;

    private double latitude;
    private double longitude;

    boolean a = true;

    private Date time;
    private Date date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventPrivateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkNetwork();

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        binding.eventDate.setEnabled(false);
        binding.eventTime.setEnabled(false);

        ref = database.getReference("PrivateEvents");

        binding.backFromPrivateEventBtn.setOnClickListener(v -> {
            onBackPressed();
        });



        binding.pickDateBtn.setOnClickListener(v -> setDate());

        binding.pickTimeBtn.setOnClickListener(v -> setTime());

        binding.nextBtnFromEventBtn.setOnClickListener(v -> {
            if (binding.eventNamePrivate.getText().toString().isEmpty()|| binding.eventTime.getText().toString().isEmpty()
                    || binding.eventDate.getText().toString().isEmpty()) { //????????????, ?????????? ???????? ?????????????? ????????
                FancyToast.makeText(getApplicationContext(),"Fields cannot be empty",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            } else {
                Date nd = new Date();
                if (nd.after(date)){
                    FancyToast.makeText(getApplicationContext(),"Event is outdated, check time and date",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                }
                else if (nd == date && nd.after(time)){
                    FancyToast.makeText(getApplicationContext(),"Event is outdated, check time and date",FancyToast.LENGTH_LONG,FancyToast.ERROR, false).show();
                }
                else{
                    //???????? ?????? ????????????, ???? ?????????????? reference ?????? ?????????? ??????????????????????
                    ref.addValueEventListener(new ValueEventListener(){

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(a) {
                                try {
                                    event_number = Integer.parseInt(snapshot.child("count").getValue().toString());
                                    ref = database.getReference("PrivateEvents").child(String.valueOf(event_number));
                                    ref.setValue(event_info).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()){

                                            YandexPlacePicker.IntentBuilder builder = new YandexPlacePicker.IntentBuilder()
                                                    .setYandexMapsKey("89430e9a-570b-4942-bd42-dea53319a059");
                                            Intent placeINtent = builder.build(PrivateEventActivity.this);
                                            startActivityForResult(placeINtent, 1);
                                        }else {
                                            FancyToast.makeText(getApplicationContext(),"Some errors",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                                        }
                                    });

                                    ref.child("name").setValue(binding.eventNamePrivate.getText().toString());
                                    ref.child("userID").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    snapshot.getRef().child("count").setValue(event_number + 2);
                                    a = false;
                                } catch (Exception e) {
                                    event_number = 1;
                                    ref = database.getReference("PrivateEvents").child(String.valueOf(event_number));
                                    ref.setValue(event_info).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()){
                                            YandexPlacePicker.IntentBuilder builder = new YandexPlacePicker.IntentBuilder()
                                                    .setYandexMapsKey("89430e9a-570b-4942-bd42-dea53319a059");
                                            Intent placeINtent = builder.build(PrivateEventActivity.this);
                                            startActivityForResult(placeINtent, 1);
                                        }else {
                                            FancyToast.makeText(getApplicationContext(),"Some errors",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                                        }
                                    });

                                    ref.child("name").setValue(binding.eventNamePrivate.getText().toString());
                                    ref.child("userID").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    snapshot.getRef().child("count").setValue(event_number + 2);
                                    a = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }


    // ???????????????????? ???????????????????? ???????? ?????? ???????????? ????????
    public void setDate() {
        new DatePickerDialog(PrivateEventActivity.this, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // ???????????????????? ???????????????????? ???????? ?????? ???????????? ??????????????
    public void setTime() {
        new TimePickerDialog(PrivateEventActivity.this, t,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true)
                .show();
    }

    // ?????????????????? ?????????????????????? ???????????? ??????????????
    TimePickerDialog.OnTimeSetListener t=new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);
            SimpleDateFormat formatForTime = new SimpleDateFormat("hh:mm");
            time = new Date();
            time.setHours(hourOfDay);
            time.setMinutes(minute);
            event_info.put("time", formatForTime.format(time));

            binding.eventTime.setText(formatForTime.format(time));
        }
    };

    // ?????????????????? ?????????????????????? ???????????? ????????
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
        @SuppressLint("SetTextI18n")
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat formatForDate = new SimpleDateFormat("dd.MM.yyyy");
            date = new Date(year-1900, monthOfYear, dayOfMonth);
            event_info.put("date", formatForDate.format(date));

            binding.eventDate.setText(formatForDate.format(date));
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1){
            if (data!=null){
                PlaceParcelable place = YandexPlacePicker.getPlace(data);
                double latitude = place.getPoint().getLatitude();
                double longitude = place.getPoint().getLongitude();

                ref.child("adress").child("latitude").setValue(latitude);
                ref.child("adress").child("longitude").setValue(longitude);
                ref.child("go").setValue("," + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

                Intent intent = new Intent (PrivateEventActivity.this, UsersInvitationActivity.class);
                intent.putExtra("event_number", event_number);
                intent.putExtra("event_name", binding.eventNamePrivate.getText().toString());
                intent.putExtra("privacy", true);
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
                Intent in_intent = new Intent (PrivateEventActivity.this, InternetErrorConnectionActivity.class);
                startActivity(in_intent);
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
    }

}