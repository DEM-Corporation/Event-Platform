package dem.xbitly.eventplatform.activities;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.shashank.sony.fancytoastlib.FancyToast;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dem.xbitly.eventplatform.Message.Message;
import dem.xbitly.eventplatform.Message.MessageAdapter;
import dem.xbitly.eventplatform.bottomsheet.BottomSheetEventDialog;
import dem.xbitly.eventplatform.databinding.ActivityChatBinding;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;

    private RecyclerView recView;

    private MessageAdapter adapter;

    private ArrayList<String> messages = new ArrayList<>();

    private int count;

    private FirebaseRecyclerOptions<Message> options;

    private boolean t = true;
    private boolean r = true;
    private boolean u = true;

    private boolean privacy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recView = binding.messageRecView;
        checkNetwork();

         options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(FirebaseDatabase.getInstance().getReference("Chats")
                                .child(Integer.toString(getIntent().getIntExtra("chatID", 0))).child("messages").child("all_messages"), Message.class)
                        .build();
        recView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        readMessages();

        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("chats").child(getIntent().getStringExtra("chatID2"))
                .child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    binding.nameTxt.setText(task.getResult().getValue().toString());
                }
            }
        });

        if (!getIntent().getBooleanExtra("privacy", true)){
            privacy = false;
        }else{
            privacy = true;
        }


        binding.messageBtnSend.setOnClickListener(v -> {
            if (binding.messageEdit.getText().toString().length() != 0){
                FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(getIntent().getIntExtra("chatID", 0)))
                        .child("messages").child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()){
                            count = Integer.parseInt(task.getResult().getValue().toString());
                            count++;
                            FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(getIntent().getIntExtra("chatID", 0)))
                                    .child("messages").child("count").setValue(count);
                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<DataSnapshot> task2) {
                                    if (task2.isSuccessful()){
                                        String time = new SimpleDateFormat("HH:mm").format(new Date());
                                        HashMap<String, String> messageInfo = new HashMap<>();
                                        messageInfo.put("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        messageInfo.put("from", task2.getResult().getValue().toString());
                                        messageInfo.put("text", binding.messageEdit.getText().toString());
                                        messageInfo.put("time", time);
                                        FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(getIntent().getIntExtra("chatID", 0)))
                                                .child("messages").child("all_messages").child(Integer.toString(count)).setValue(messageInfo);
                                        binding.messageEdit.setText("");
                                        recView.scrollToPosition(adapter.getItemCount()-1);
                                    }

                                }
                            });

                        }
                    }
                });
            } else {
                FancyToast.makeText(getApplicationContext(),"Fields cannot be empty",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            }
        });

        binding.eventInfoBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getApplicationContext(), v);
            popup.getMenu().add(Menu.NONE, 0, Menu.NONE, "About event");
            popup.getMenu().add(Menu.NONE, 1, Menu.NONE, "Members");
            t = true;
            FirebaseDatabase.getInstance().getReference().child("Chats").child(Integer.toString(getIntent().getIntExtra("chatID", 0)))
                    .child("event_number").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (t){
                                String eventID = snapshot.getValue().toString();
                                if (Integer.parseInt(eventID)%2!=0){
                                    r = true;
                                    FirebaseDatabase.getInstance().getReference().child("PrivateEvents").child(eventID)
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (r){
                                                        String userID = snapshot.child("userID").getValue().toString();
                                                        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(userID)){
                                                            popup.getMenu().add(Menu.NONE, 2, Menu.NONE, "Copy invitation link");
                                                        }
                                                        r=false;
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                }else{
                                    r = true;
                                    FirebaseDatabase.getInstance().getReference().child("PublicEvents").child(eventID)
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (r){
                                                        String userID = snapshot.child("userID").getValue().toString();
                                                        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(userID)){
                                                            popup.getMenu().add(Menu.NONE, 2, Menu.NONE, "Copy invitation link");
                                                        }
                                                        r=false;
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                }
                                t=false;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            popup.show();
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(getIntent().getIntExtra("chatID", 0)))
                            .child("event_number").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            int event_number = Integer.parseInt(Objects.requireNonNull(task.getResult().getValue()).toString());
                            HashMap<String, String> eventInfo = new HashMap<>();


                            if (privacy) {
                                FirebaseDatabase.getInstance().getReference("PrivateEvents").child(Integer.toString(event_number))
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                String name = snapshot.child("name").getValue().toString();
                                                String go = snapshot.child("go").getValue().toString();
                                                int count = go.split(",").length - 1;
                                                String time = snapshot.child("time").getValue().toString();
                                                String date = snapshot.child("date").getValue().toString();
                                                String latitude = snapshot.child("adress").child("latitude").getValue().toString();
                                                String longitude = snapshot.child("adress").child("longitude").getValue().toString();
                                                double latitude_d = Double.parseDouble(latitude);
                                                double longitude_d = Double.parseDouble(longitude);
                                                Geocoder geocoder;
                                                List<Address> addresses;
                                                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                                                String count_bs = Integer.toString(count);

                                                try {
                                                    addresses = geocoder.getFromLocation(latitude_d, longitude_d, 1);

                                                    String address = addresses.get(0).getAddressLine(0);

                                                    BottomSheetEventDialog bottomSheetEventDialog = new BottomSheetEventDialog(Integer.toString(event_number), name,
                                                            address, count_bs,date, time, false, true, false);
                                                    bottomSheetEventDialog.show(getSupportFragmentManager(), "Event info");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                            }
                                        });
                            } else {
                                FirebaseDatabase.getInstance().getReference("PublicEvents").child(Integer.toString(event_number))
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                String name = snapshot.child("name").getValue().toString();
                                                String go = snapshot.child("go").getValue().toString();
                                                int count = go.split(",").length - 1;
                                                String time = snapshot.child("time").getValue().toString();
                                                String date = snapshot.child("date").getValue().toString();
                                                String latitude = snapshot.child("adress").child("latitude").getValue().toString();
                                                String longitude = snapshot.child("adress").child("longitude").getValue().toString();
                                                double latitude_d = Double.parseDouble(latitude);
                                                double longitude_d = Double.parseDouble(longitude);
                                                Geocoder geocoder;
                                                List<Address> addresses;
                                                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                                String maxCount = Objects.requireNonNull(snapshot.child("max_amount").getValue()).toString();
                                                String count_bs;
                                                if (maxCount.equals("0")) {
                                                    count_bs = "Infinity";
                                                } else {
                                                    count_bs = count + "/" + maxCount;
                                                }

                                                try {
                                                    addresses = geocoder.getFromLocation(latitude_d, longitude_d, 1);

                                                    String address = addresses.get(0).getAddressLine(0);

                                                    if (!getSupportFragmentManager().isDestroyed()){
                                                        BottomSheetEventDialog bottomSheetEventDialog = new BottomSheetEventDialog(Integer.toString(event_number), name,
                                                                address, count_bs, date, time, true, false, false);
                                                        bottomSheetEventDialog.show(getSupportFragmentManager(), "Event info");
                                                    }

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
                } else if (item.getItemId()==1){

                    FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(getIntent().getIntExtra("chatID", 0)))
                            .child("event_number").get().addOnCompleteListener(task -> {
                                Intent intent = new Intent (ChatActivity.this, MembersActivity.class);
                                intent.putExtra("eventID", Objects.requireNonNull(task.getResult().getValue()).toString());
                                intent.putExtra("private", privacy);
                                startActivity(intent);
                            });

                } else if (item.getItemId()==2){
                    u=true;
                    FirebaseDatabase.getInstance().getReference().child("Chats").child(Integer.toString(getIntent().getIntExtra("chatID", 0)))
                            .child("invitationLink")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (u) {
                                        String invitationLink = snapshot.getValue().toString();
                                        if (!invitationLink.equals("")) {
                                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText("InvitationLink", invitationLink);
                                            clipboard.setPrimaryClip(clip);
                                            FancyToast.makeText(getApplicationContext(), "Invitation link copied to clipboard", FancyToast.LENGTH_SHORT).show();

                                        }
                                        u=false;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
                return false;
            });
        });

        binding.backFromChatBtn.setOnClickListener(view -> onBackPressed());

    }

    public void checkNetwork(){
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            if (!connected) {
                Intent in_intent = new Intent (ChatActivity.this, InternetErrorConnectionActivity.class);
                startActivity(in_intent);
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();

        readMessages();
    }

    public void readMessages(){
        FirebaseDatabase.getInstance().getReference("Chats").child(Integer.toString(getIntent().getIntExtra("chatID", 0))).child("messages").child("all_messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                messages.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Message message = snapshot1.getValue(Message.class);
                    messages.add(message.getUserID());
                }
                adapter = new MessageAdapter(options, messages);
                adapter.startListening();
                recView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}