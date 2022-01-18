package dem.xbitly.eventplatform.tape;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dem.xbitly.eventplatform.bottomsheet.BottomSheetEditDialog;
import dem.xbitly.eventplatform.R;
import dem.xbitly.eventplatform.activities.CommentActivity;
import dem.xbitly.eventplatform.bottomsheet.BottomSheetEventDialog;

public class TapeAdapter extends RecyclerView.Adapter<TapeHolder> {

    private final int countElements;
    private final ArrayList<String> reviewsID = new ArrayList<>();
    private final ArrayList<String> invitesID = new ArrayList<>();
    private final String userID;
    private final Context context;
    FragmentManager fragmentManager;
    DatabaseReference ref, ref2, refLike;

    boolean e = false;

    private int count;

    public TapeAdapter(String[] sR, String[] sI, String userID, Context context, FragmentManager fragmentManager) {
        this.countElements = sR.length + sI.length;
        this.reviewsID.addAll(Arrays.asList(sR));
        this.invitesID.addAll(Arrays.asList(sI));
        this.userID = userID;
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public int getItemViewType(final int position) {

        int sizeR = reviewsID.size();
        int sizeI = invitesID.size();

        if (sizeI >= sizeR && sizeI != 0 && sizeR != 0) {
            int kf = sizeI / sizeR;
            if ((position+1)%(kf+1) == 0 && reviewsID.contains(Integer.toString((position+1)/(kf+1)))) {
                return R.layout.item_review;
            } else {
                return R.layout.item_invite;
            }
        }else if(sizeI < sizeR && sizeI != 0){
            int kf = sizeR / sizeI;
            if ((position+1)%(kf+1) == 0 && invitesID.contains(Integer.toString((position+1)/(kf+1)))) {
                return R.layout.item_invite;
            } else {
                return R.layout.item_review;
            }
        }else if(sizeI != 0){
            return R.layout.item_invite;
        }else if(sizeR != 0){
            return R.layout.item_review;
        } else {
            return 0;
        }

    }

    @NonNull
    @Override
    public TapeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if(viewType == R.layout.item_invite){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invite, parent, false);
        }
        else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        }
        return new TapeHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final TapeHolder holder, final int position) {

        int sizeR = reviewsID.size();
        int sizeI = invitesID.size();

        if (sizeI >= sizeR && sizeI != 0 && sizeR != 0) {
            int kf = sizeI / sizeR;
            if ((position+1)%(kf+1) == 0 && reviewsID.contains(Integer.toString((position+1)/(kf+1)))) {
                loadReview(holder, (position+1)/(kf+1) - 1);
            } else {
                loadInvite(holder, (position+1) - (position+1)/(kf+1) - 1);
            }
        }else if(sizeI < sizeR && sizeI != 0){
            int kf = sizeR / sizeI;
            if ((position+1)%(kf+1) == 0 && invitesID.contains(Integer.toString((position+1)/(kf+1)))) {
                loadInvite(holder, (position+1)/(kf+1) - 1);
            } else {
                loadReview(holder, (position+1) - (position+1)/(kf+1) - 1);
            }
        }else if(sizeI != 0){
            loadInvite(holder, position);
        }else if(sizeR != 0){
            loadReview(holder, position);
        }

    }

    private void loadInvite(@NonNull final TapeHolder holder, final int position){

        FirebaseDatabase dBase = FirebaseDatabase.getInstance();

        ref = dBase.getReference("Invite").child(invitesID.get(position));
        ref2 = dBase.getReference("Invite");
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                holder.getTimeAndData().setText(Objects.requireNonNull(snapshot2.child("date").getValue()).toString() + " " + Objects.requireNonNull(snapshot2.child("time").getValue()).toString());
                String str = Objects.requireNonNull(snapshot2.child("text").getValue()).toString();
                holder.getText().setText(str);

                ref = dBase.getReference("Users").child(Objects.requireNonNull(snapshot2.child("userID").getValue()).toString());
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot3) {
                        holder.getUsername().setText(Objects.requireNonNull(snapshot3.child("name").getValue()).toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                String eventID = Objects.requireNonNull(snapshot2.child("eventID").getValue()).toString();
                ref2 = dBase.getReference("PublicEvents").child(eventID);
                ref2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String go = "";
                        int ee = 0;
                        try {
                            go = Objects.requireNonNull(snapshot.child("go").getValue()).toString();
                            ee = Integer.parseInt(Objects.requireNonNull(snapshot.child("max_amount").getValue()).toString());
                        } catch (Exception e) {
                            ref2.child("go").setValue("");
                        }
                        if(go.contains(userID)){
                            holder.getButtonGo().setText("Refuse");
                            holder.getButtonGo().setBackgroundResource(R.drawable.go_btn_bg_success);
                        }
                        if(ee != 0){
                            holder.getCountUsers().setText((go.split(",").length-1)+"/"+ee);
                        }
                        String finalGo = go;
                        int finalEe = ee;
                        holder.getButtonGo().setOnClickListener(view1 -> {

                            ref2 = dBase.getReference("PublicEvents").child(Objects.requireNonNull(snapshot2.child("eventID").getValue()).toString());

                            if(!finalGo.contains(userID)) {
                                if(finalGo.split(",").length <= finalEe || finalEe == 0) {
                                    holder.getButtonGo().setText("Refuse");
                                    holder.getButtonGo().setBackgroundResource(R.drawable.go_btn_bg_success);

                                    ref2.child("go").setValue(finalGo + "," + userID);
                                    FirebaseDatabase.getInstance().getReference("Users").child(userID)
                                            .child("UserPrivateEvents").child("count").get().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()){
                                            count = Integer.parseInt(Objects.requireNonNull(task.getResult().getValue()).toString());
                                            count++;
                                            FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                                    .child("UserPrivateEvents").child("count").setValue(count);
                                            dBase.getReference("Invite").child(invitesID.get(position)).child("eventID").get().addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    String eventID1 = Objects.requireNonNull(task1.getResult().getValue()).toString();

                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .child("UserPrivateEvents").child(snapshot2.child("eventID").getValue().toString()).child("privacy").setValue("no");

                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .child("UserPrivateEvents").child(snapshot2.child("eventID").getValue().toString()).child("eventID").setValue(eventID1);
                                                }
                                            });
                                        }
                                    });

                                    dBase.getReference("Invite").child(invitesID.get(position)).child("eventID").get().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()){
                                            String eventID12 = Objects.requireNonNull(task.getResult().getValue()).toString();
                                            FirebaseDatabase.getInstance().getReference("PublicEvents").child(eventID12).child("chatID").get().addOnCompleteListener(task13 -> {
                                                if (task13.isSuccessful()){
                                                    String chatID = Objects.requireNonNull(task13.getResult().getValue()).toString();
                                                    FirebaseDatabase.getInstance().getReference("Chats").child(chatID).child("members").child("count").get().addOnCompleteListener(task12 -> {
                                                        if (task12.isSuccessful()){
                                                            int members_count = Integer.parseInt(Objects.requireNonNull(task12.getResult().getValue()).toString());
                                                            members_count++;
                                                            FirebaseDatabase.getInstance().getReference("Chats").child(chatID).child("members").child(Integer.toString(members_count))
                                                                    .setValue(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                                                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats")
                                                                    .child("count").get().addOnCompleteListener(task121 -> {
                                                                if (task121.isSuccessful()){
                                                                    count = Integer.parseInt(Objects.requireNonNull(task121.getResult().getValue()).toString());
                                                                    count++;
                                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats")
                                                                            .child("count").setValue(count);
                                                                    FirebaseDatabase.getInstance().getReference("PublicEvents").child(eventID12).child("name").get()
                                                                            .addOnCompleteListener(task1211 -> {
                                                                                if (task1211.isSuccessful()){
                                                                                    String name = Objects.requireNonNull(task1211.getResult().getValue()).toString();
                                                                                    HashMap<String, String> chatInfo = new HashMap<>();
                                                                                    chatInfo.put("chatID", chatID);
                                                                                    chatInfo.put("name", name);
                                                                                    chatInfo.put("privacy", "no");
                                                                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                                            .child("Chats").child("chats").child(snapshot2.child("eventID").getValue().toString()).setValue(chatInfo);
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    holder.getButtonGo().setBackgroundResource(R.drawable.go_btn_bg);
                                    FancyToast.makeText(context,"Max amount",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                                }
                            } else {
                                holder.getButtonGo().setText("I will go");
                                holder.getButtonGo().setBackgroundResource(R.drawable.go_btn_bg);
                                ref2.child("go").setValue(finalGo.replace(","+userID, ""));
                                FirebaseDatabase.getInstance().getReference("Users").child(userID)
                                        .child("UserPrivateEvents").child(eventID).setValue(null);
                                FirebaseDatabase.getInstance().getReference("Users").child(userID)
                                        .child("UserPrivateEvents").child("count").get().addOnCompleteListener(task -> {
                                            if (task.isSuccessful()){
                                                int count234 = Integer.parseInt(Objects.requireNonNull(task.getResult().getValue()).toString());
                                                count234--;
                                                FirebaseDatabase.getInstance().getReference("Users").child(userID)
                                                        .child("UserPrivateEvents").child("count").setValue(Integer.toString(count234));
                                            }
                                        });

                                FirebaseDatabase.getInstance().getReference("Users").child(userID)
                                        .child("Chats").child("chats").child(eventID).setValue(null);
                                FirebaseDatabase.getInstance().getReference("Users").child(userID)
                                        .child("Chats").child("count").get().addOnCompleteListener(task -> {
                                            if (task.isSuccessful()){
                                                int countt23 = Integer.parseInt(Objects.requireNonNull(task.getResult().getValue()).toString());
                                                countt23--;
                                                FirebaseDatabase.getInstance().getReference("Users").child(userID)
                                                        .child("Chats").child("count").setValue(countt23);
                                            }
                                        });
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.getButtonShare().setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, str);
                    Intent chosenIntent = Intent.createChooser(intent, "Send review");
                    context.startActivity(chosenIntent);
                });

                holder.getButtonMenu().setOnClickListener(view -> {
                    PopupMenu popup = new PopupMenu(context, view);
                    if (Objects.requireNonNull(snapshot2.child("userID").getValue()).toString().equals(userID)) {
                        popup.getMenu().add(Menu.NONE, 0, Menu.NONE, "About event");
                        popup.getMenu().add(Menu.NONE, 1, Menu.NONE, "Edit");
                    } else {
                        popup.getMenu().add(Menu.NONE, 0, Menu.NONE, "About event");
                    }
                    popup.show();
                    popup.setOnMenuItemClickListener(menuItem -> {
                        switch (menuItem.getItemId()) {
                            case 0: //About event
                                e = true;
                                ref2 = dBase.getReference("PublicEvents").child(eventID);
                                ref2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(e) {
                                            String go = Objects.requireNonNull(snapshot.child("go").getValue()).toString();
                                            boolean a = go.contains(userID);
                                            String text = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                            int count = go.split(",").length - 1;
                                            String maxCount = Objects.requireNonNull(snapshot.child("max_amount").getValue()).toString();
                                            String count_bs;
                                            if (maxCount.equals("0")) {
                                                count_bs = "Infinity";
                                            } else {
                                                count_bs = count + "/" + maxCount;
                                            }
                                            String time = Objects.requireNonNull(snapshot.child("time").getValue()).toString();
                                            String date = Objects.requireNonNull(snapshot.child("date").getValue()).toString();

                                            double latitude_d = Double.parseDouble(Objects.requireNonNull(snapshot.child("adress").child("latitude").getValue()).toString());
                                            double longitude_d = Double.parseDouble(Objects.requireNonNull(snapshot.child("adress").child("longitude").getValue()).toString());

                                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                                            List<Address> addresses;

                                            try {
                                                addresses = geocoder.getFromLocation(latitude_d, longitude_d, 1);

                                                String address = addresses.get(0).getAddressLine(0);

                                                BottomSheetEventDialog bottomSheetEventDialog = new BottomSheetEventDialog(eventID, text, address, count_bs, date, time, a, false, true);
                                                bottomSheetEventDialog.show(fragmentManager, "Event info");

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            e = false;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                break;
                            case 1: //Edit
                                BottomSheetEditDialog bottomSheetEditDialog = new BottomSheetEditDialog(invitesID.get(position), false);
                                bottomSheetEditDialog.show(fragmentManager, "Edit invite");
                                break;
                        }
                        return true;
                    });
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadReview(@NonNull final TapeHolder holder, final int position){

        FirebaseDatabase dBase = FirebaseDatabase.getInstance();

        ref = dBase.getReference("Reviews").child(reviewsID.get(position));
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                holder.getTimeAndData().setText(Objects.requireNonNull(snapshot2.child("date").getValue()).toString() + " " + Objects.requireNonNull(snapshot2.child("time").getValue()).toString());
                String str = Objects.requireNonNull(snapshot2.child("text").getValue()).toString();
                holder.getText().setText(str);

                String like = Objects.requireNonNull(snapshot2.child("like").getValue()).toString();
                String eventID = Objects.requireNonNull(snapshot2.child("eventID").getValue()).toString();

                ref = dBase.getReference("Users").child(Objects.requireNonNull(snapshot2.child("userID").getValue()).toString());
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot3) {
                        holder.getUsername().setText(Objects.requireNonNull(snapshot3.child("name").getValue()).toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                String[] split = like.split(",");
                holder.getLike().setText("Like: "  + (split.length-1));

                if(like.contains(userID)){
                    holder.getButtonLike().setImageResource(R.drawable.ic_like_pressed);
                } else {
                    holder.getButtonLike().setImageResource(R.drawable.ic_like);
                }

                holder.getButtonLike().setOnClickListener(view -> {

                    refLike = dBase.getReference("Reviews").child(reviewsID.get(position));

                    if(like.contains(userID)){

                        refLike.child("like").setValue(like.replace(","+userID, ""));

                    } else {

                        refLike.child("like").setValue(like+","+userID);

                    }

                });

                holder.getButtonComment().setOnClickListener(view -> {
                    Intent intent = new Intent(context, CommentActivity.class);
                    if(str.length() > 9){
                        intent.putExtra("name", str.substring(0, 7)+"...");
                    } else {
                        intent.putExtra("name", str);
                    }
                    intent.putExtra("id", reviewsID.get(position));
                    context.startActivity(intent);
                });

                holder.getButtonShare().setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, str);
                    Intent chosenIntent = Intent.createChooser(intent, "Send review");
                    context.startActivity(chosenIntent);
                });

                holder.getButtonMenu().setOnClickListener(view -> {
                    PopupMenu popup = new PopupMenu(context, view);
                    if (Objects.requireNonNull(snapshot2.child("userID").getValue()).toString().equals(userID)) {
                        popup.getMenu().add(Menu.NONE, 0, Menu.NONE, "About event");
                        popup.getMenu().add(Menu.NONE, 1, Menu.NONE, "Edit");
                    } else {
                        popup.getMenu().add(Menu.NONE, 0, Menu.NONE, "About event");
                    }
                    popup.show();
                    popup.setOnMenuItemClickListener(menuItem -> {
                        switch (menuItem.getItemId()) {
                            case 0: //About event
                                ref2 = dBase.getReference("PublicEvents").child(eventID);
                                ref2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String go = Objects.requireNonNull(snapshot.child("go").getValue()).toString();
                                        boolean a = go.contains(userID);
                                        String text = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                        int count = go.split(",").length-1;
                                        String maxCount = Objects.requireNonNull(snapshot.child("max_amount").getValue()).toString();
                                        String count_bs;
                                        if(maxCount.equals("0")){
                                            count_bs = "Infinity";
                                        } else {
                                            count_bs = count + "/" + maxCount;
                                        }
                                        String time = Objects.requireNonNull(snapshot.child("time").getValue()).toString();
                                        String date = Objects.requireNonNull(snapshot.child("date").getValue()).toString();
                                        double latitude_d = Double.parseDouble(Objects.requireNonNull(snapshot.child("adress").child("latitude").getValue()).toString());
                                        double longitude_d = Double.parseDouble(Objects.requireNonNull(snapshot.child("adress").child("longitude").getValue()).toString());

                                        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                                        List<Address> addresses;

                                        try {
                                            addresses = geocoder.getFromLocation(latitude_d, longitude_d, 1);

                                            String address = addresses.get(0).getAddressLine(0);

                                            BottomSheetEventDialog bottomSheetEventDialog = new BottomSheetEventDialog(eventID, text, address, count_bs, date, time, a, false, true);
                                            bottomSheetEventDialog.show(fragmentManager, "Event info");

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                break;
                            case 1: //Edit
                                BottomSheetEditDialog bottomSheetEditDialog = new BottomSheetEditDialog(reviewsID.get(position), true);
                                bottomSheetEditDialog.show(fragmentManager, "Edit review");
                                break;
                        }
                        return true;
                    });
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return countElements;
    }
}