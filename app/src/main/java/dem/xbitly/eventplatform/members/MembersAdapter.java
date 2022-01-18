package dem.xbitly.eventplatform.members;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import dem.xbitly.eventplatform.R;

public class MembersAdapter extends RecyclerView.Adapter<MembersHolder> {

    private String id[];

    public MembersAdapter(String[] id){
        this.id = id;
    }

    @NonNull
    @Override
    public MembersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_person, parent, false);
        return new MembersHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersHolder holder, int position) {
        FirebaseDatabase.getInstance().getReference("Users").child(id[position+1]).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println(id);
                String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                holder.getProfileName().setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.item_person;
    }

    @Override
    public int getItemCount() {
        return id.length-1;
    }
}
