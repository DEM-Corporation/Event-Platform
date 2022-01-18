package dem.xbitly.eventplatform.bottomsheet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import dem.xbitly.eventplatform.R;
import dem.xbitly.eventplatform.activities.CommentActivity;
import dem.xbitly.eventplatform.activities.CreateReviewActivity;
import dem.xbitly.eventplatform.activities.MainActivity;

public class BottomSheetEventDialog extends BottomSheetDialogFragment {

    private String id, name, address, count_people, date, time;
    private boolean userIsGo, eventIsPrivate, refuse;

    public BottomSheetEventDialog(String id, String name, String address, String count_people, String date, String time, boolean userIsGo, boolean eventIsPrivate, boolean refuse){

        this.id = id;
        this.name = name;
        this.address = address;
        this.count_people = count_people;
        this.date = date;
        this.time = time;
        this.userIsGo = userIsGo;
        this.eventIsPrivate = eventIsPrivate;
        this.refuse = refuse;
    }

    public BottomSheetEventDialog(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        ImageButton buttonClose = v.findViewById(R.id.btn_close);
        TextView name = v.findViewById(R.id.name);
        TextView address = v.findViewById(R.id.place);
        TextView count_people = v.findViewById(R.id.count_people);
        TextView date = v.findViewById(R.id.date);
        TextView time = v.findViewById(R.id.time);
        TextView event_private = v.findViewById(R.id.text_private);
        RelativeLayout buttonAddReview = v.findViewById(R.id.btn_add_review);
        RelativeLayout buttonRefuse = v.findViewById(R.id.btn_refuse);

        if(eventIsPrivate){
            event_private.setVisibility(View.VISIBLE);
        } else {
            event_private.setVisibility(View.GONE);
        }

        address.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Address", address.getText().toString());
            clipboard.setPrimaryClip(clip);
            FancyToast.makeText(v.getContext(),"Address copied",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show();
        });

        buttonClose.setOnClickListener(view -> {
            dismiss();
        });

        if (refuse){
            buttonRefuse.setVisibility(View.VISIBLE);
            buttonRefuse.setOnClickListener(view -> {
                if (this.eventIsPrivate){
                    String id = this.id;
                    FirebaseDatabase.getInstance().getReference("PrivateEvents").child(this.id).child("go").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()){
                                String finalGo = task.getResult().getValue().toString();
                                FirebaseDatabase.getInstance().getReference("PrivateEvents").child(id).child("go")
                                        .setValue(finalGo.replace(","+FirebaseAuth.getInstance().getCurrentUser().getUid(), ""));
                            }
                        }
                    });
                }else{
                    String id = this.id;
                    FirebaseDatabase.getInstance().getReference("PublicEvents").child(this.id).child("go").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()){
                                String finalGo = task.getResult().getValue().toString();
                                FirebaseDatabase.getInstance().getReference("PublicEvents").child(id).child("go")
                                        .setValue(finalGo.replace(","+FirebaseAuth.getInstance().getCurrentUser().getUid(), ""));
                            }
                        }
                    });
                }

                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase.getInstance().getReference("Users").child(userID)
                        .child("UserPrivateEvents").child(this.id).setValue(null);
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
                        .child("Chats").child("chats").child(this.id).setValue(null);
                FirebaseDatabase.getInstance().getReference("Users").child(userID)
                        .child("Chats").child("count").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        int countt23 = Integer.parseInt(Objects.requireNonNull(task.getResult().getValue()).toString());
                        countt23--;
                        FirebaseDatabase.getInstance().getReference("Users").child(userID)
                                .child("Chats").child("count").setValue(countt23);
                    }
                });

                Intent intent = new Intent (v.getContext(), MainActivity.class);
                v.getContext().startActivity(intent);
            });
        }else{
            buttonRefuse.setVisibility(View.GONE);
        }



        if (userIsGo) {
            buttonAddReview.setVisibility(View.VISIBLE);
            buttonAddReview.setOnClickListener(view -> {
                Intent intent = new Intent(v.getContext(), CreateReviewActivity.class);
                if(this.name.length() > 9){
                    intent.putExtra("nameEvent", this.name.substring(0, 7)+"...");
                } else {
                    intent.putExtra("nameEvent", this.name);
                }
                intent.putExtra("eventID", this.id);
                v.getContext().startActivity(intent);
            });
        } else {
            buttonRefuse.setVisibility(View.GONE);
            buttonAddReview.setVisibility(View.GONE);
        }

        name.setText(this.name);
        address.setText(this.address);
        count_people.setText(this.count_people);
        date.setText(this.date);
        time.setText(this.time);

        return v;
    }
}
