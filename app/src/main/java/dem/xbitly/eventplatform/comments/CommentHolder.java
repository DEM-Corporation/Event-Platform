package dem.xbitly.eventplatform.comments;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dem.xbitly.eventplatform.R;

public class CommentHolder extends RecyclerView.ViewHolder {

    private final TextView text, username, timeAndData;
    private final ImageView photo;

    public CommentHolder(@NonNull View itemView) {
        super(itemView);
        text = itemView.findViewById(R.id.text);
        username = itemView.findViewById(R.id.username);
        timeAndData = itemView.findViewById(R.id.time_and_data);
        photo = itemView.findViewById(R.id.profile_image);
    }

    public TextView getUsername() {
        return username;
    }

    public TextView getText() {
        return text;
    }

    public TextView getTimeAndData() {
        return timeAndData;
    }

    public ImageView getPhoto() {
        return photo;
    }
}