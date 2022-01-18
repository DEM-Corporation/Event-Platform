package dem.xbitly.eventplatform.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import dem.xbitly.eventplatform.databinding.ActivityPublicEventBinding;
import dem.xbitly.eventplatform.network.NetworkManager;

public class PublicEventActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference ref;


    boolean a = true;


    Calendar dateAndTime = Calendar.getInstance();

        private HashMap<String, String> event_info;

    private ActivityPublicEventBinding binding;

    private int event_number;//номер евента



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublicEventBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        checkNetwork();

        event_info = new HashMap<>();

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        ref = database.getReference("PublicEvents");



        binding.eventDate.setEnabled(false);
        binding.eventTime.setEnabled(false);

        //раздел со всеми созданными евентами этого человека
        binding.infinityAmountBtn.setOnClickListener(v -> {
            binding.eventMaxAmount.setText("Infinity");
            binding.eventMaxAmount.setEnabled(false);

            event_info.put("max_amount", "0"); //если число участников может быть бесконечным, то записываем 0, что означает бесконечность
        });

        binding.backFromPublicEventBtn.setOnClickListener(v -> {
         onBackPressed();
        });

        binding.pickDateBtn.setOnClickListener(v -> setDate());

        binding.pickTimeBtn.setOnClickListener(v -> setTime());

        binding.nextBtnFromPublicEventBtn.setOnClickListener(v -> {

            if (binding.eventNamePublic.getText().toString().isEmpty() || binding.eventMaxAmount.getText().toString().isEmpty()
                    || binding.eventTime.getText().toString().isEmpty() || binding.eventDate.getText().toString().isEmpty()) { //нельзя, чтобы поля пустыми были
                FancyToast.makeText(getApplicationContext(),"Fields cannot be empty",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            } else {
                if (binding.eventMaxAmount.getText().toString().equals("Infinity")) {
                    event_info.put("max_amount", "0");
                } else {
                    event_info.put("max_amount", binding.eventMaxAmount.getText().toString());
                }
                event_info.put("name", binding.eventNamePublic.getText().toString());
                event_info.put("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());

                //если все хорошо, то создаем reference для этого мероприятия
                ref.addValueEventListener(new ValueEventListener(){

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(a) {
                            try {
                                event_number = Integer.parseInt(snapshot.child("count").getValue().toString());
                                ref = database.getReference("PublicEvents").child(String.valueOf(event_number));
                                ref.setValue(event_info).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {

                                        Intent intent = new Intent (PublicEventActivity.this, EventDescriptionActivity.class);
                                        intent.putExtra("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        intent.putExtra("eventID", event_number);
                                        intent.putExtra("event_name", binding.eventNamePublic.getText().toString());

                                        startActivity(intent);
                                    } else {
                                        FancyToast.makeText(getApplicationContext(),"Some errors",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                                    }
                                });
                                snapshot.getRef().child("count").setValue(event_number+2);
                                a = false;
                            } catch (Exception e) {
                                event_number = 0;
                                ref = database.getReference("PublicEvents").child(String.valueOf(event_number));
                                ref.setValue(event_info).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {

                                        Intent intent = new Intent (PublicEventActivity.this, EventDescriptionActivity.class);
                                        intent.putExtra("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        intent.putExtra("eventID", event_number);
                                        intent.putExtra("event_name", binding.eventNamePublic.getText().toString());

                                        startActivity(intent);
                                    } else {
                                        FancyToast.makeText(getApplicationContext(),"Some errors",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                                    }
                                });
                                snapshot.getRef().child("count").setValue(event_number+2);
                                a = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });



    }

    // отображаем диалоговое окно для выбора даты
    public void setDate() {
        new DatePickerDialog(PublicEventActivity.this, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // отображаем диалоговое окно для выбора времени
    public void setTime() {
        new TimePickerDialog(PublicEventActivity.this, t,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true)
                .show();
    }

    // установка обработчика выбора времени
    TimePickerDialog.OnTimeSetListener t=new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);
            SimpleDateFormat formatForTime = new SimpleDateFormat("hh:mm");
            Date date = new Date();
            date.setHours(hourOfDay);
            date.setMinutes(minute);
            event_info.put("time", formatForTime.format(date));

            binding.eventTime.setText(formatForTime.format(date));
        }
    };

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
        @SuppressLint("SetTextI18n")
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat formatForDate = new SimpleDateFormat("dd.MM.yyyy");
            Date date = new Date(year-1900, monthOfYear, dayOfMonth);
            event_info.put("date", formatForDate.format(date));

            binding.eventDate.setText(formatForDate.format(date));
        }
    };

    public void checkNetwork(){
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            if (!connected) {
                Intent in_intent = new Intent (PublicEventActivity.this, InternetErrorConnectionActivity.class);
                startActivity(in_intent);
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
    }

}