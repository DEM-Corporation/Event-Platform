package dem.xbitly.eventplatform.comments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import dem.xbitly.eventplatform.R;

public class CommentAdapter extends RecyclerView.Adapter<CommentHolder> {

    private final int countElements;
    private final FirebaseDatabase dBase;
    private DatabaseReference ref, ref1;

    public CommentAdapter(int countElements, FirebaseDatabase dBase, DatabaseReference ref) {
        this.countElements = countElements;
        this.dBase = dBase;
        this.ref = ref;
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.item_comment;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentHolder holder, final int position) {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.getText().setText(Objects.requireNonNull(snapshot.child(String.valueOf(position + 1)).child("text").getValue()).toString());
                holder.getTimeAndData().setText(String.format("%s %s", Objects.requireNonNull(snapshot.child(String.valueOf(position + 1)).child("date").getValue()).toString(), Objects.requireNonNull(snapshot.child(String.valueOf(position + 1)).child("time").getValue()).toString()));
                ref1 = dBase.getReference("Users").child(Objects.requireNonNull(snapshot.child(String.valueOf(position + 1)).child("autor").getValue()).toString());
                ref1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot3) {
                        holder.getUsername().setText(Objects.requireNonNull(snapshot3.child("name").getValue()).toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
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