package dem.xbitly.eventplatform.Message;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import dem.xbitly.eventplatform.R;

public class MessageAdapter extends FirebaseRecyclerAdapter<Message, MessageAdapter.viewHolder>{
    ArrayList<String> ids;
    public MessageAdapter(@NonNull @NotNull FirebaseRecyclerOptions<Message> options, ArrayList<String> ids) {
        super(options);
        this.ids = ids;
    }


    @Override
    protected void onBindViewHolder(@NonNull @NotNull MessageAdapter.viewHolder viewHolder, int i, @NonNull @NotNull Message msg) {

        if (msg.getUserID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            viewHolder.text.setText(msg.getText());
            viewHolder.time.setText(msg.getTime());
        }else {
            viewHolder.from.setText(msg.getFrom());
            viewHolder.text.setText(msg.getText());
            viewHolder.time.setText(msg.getTime());
        }

    }

    @NonNull
    @NotNull
    @Override
    public MessageAdapter.viewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == R.layout.message_from_me_single_layout){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_from_me_single_layout, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        }
        return new MessageAdapter.viewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {

        if (ids.get(position).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            return R.layout.message_from_me_single_layout;
        }else{
            return R.layout.message_single_layout;
        }
    }

    class viewHolder extends RecyclerView.ViewHolder {
        TextView text, from, time;
        public viewHolder(@NonNull View itemView){
            super(itemView);
            if (itemView.equals(R.layout.message_from_me_single_layout)){
                text = itemView.findViewById(R.id.message_text);
                time = itemView.findViewById(R.id.time_txt);
            }
            else {
                from = itemView.findViewById(R.id.message_author);
                text = itemView.findViewById(R.id.message_text);
                time = itemView.findViewById(R.id.time_txt);
            }

        }
    }
}
