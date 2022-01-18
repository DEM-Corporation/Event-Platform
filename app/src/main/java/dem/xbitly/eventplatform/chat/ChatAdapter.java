package dem.xbitly.eventplatform.chat;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import dem.xbitly.eventplatform.R;
import dem.xbitly.eventplatform.activities.ChatActivity;

public class ChatAdapter extends FirebaseRecyclerAdapter<Chat, ChatAdapter.viewHolder> {

    public ChatAdapter(@NonNull @NotNull FirebaseRecyclerOptions<Chat> options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(@NonNull @NotNull viewHolder viewHolder, int i, @NonNull @NotNull Chat chat) {
        viewHolder.name.setText(chat.getName());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String chat_id = getRef(i).getKey();
                boolean privacy = true;
                if (chat.getPrivacy().equals("no")){
                    privacy = false;
                }
                boolean finalPrivacy = privacy;
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child("chats").child(chat_id)
                        .child("chatID").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()){
                            Intent intent = new Intent (v.getContext(), ChatActivity.class);
                            intent.putExtra("chatID", Integer.parseInt(task.getResult().getValue().toString())); //chat id in all Chats
                            intent.putExtra("chatID2", chat_id);
                            intent.putExtra("privacy", finalPrivacy); //private event or not
                            v.getContext().startActivity(intent);
                        }else{
                            Snackbar.make(v, "Hm, something went wrong", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @NonNull
    @NotNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new viewHolder(view);
    }

    class viewHolder extends RecyclerView.ViewHolder {
        TextView name;
        public viewHolder(@NonNull View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.chat_name);
        }
    }
}
