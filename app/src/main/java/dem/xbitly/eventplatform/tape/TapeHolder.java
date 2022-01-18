package dem.xbitly.eventplatform.tape;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dem.xbitly.eventplatform.R;

public class TapeHolder extends RecyclerView.ViewHolder {

    private final TextView text, username, timeAndData, countUsers, like;
    private final ImageView photo;
    private final ImageButton buttonLike, buttonShare, buttonComment, buttonMenu;
    private final Button buttonGo;

    public TapeHolder(@NonNull View itemView) {
        super(itemView);
        text = itemView.findViewById(R.id.text);
        username = itemView.findViewById(R.id.username);
        timeAndData = itemView.findViewById(R.id.time_and_data);
        countUsers = itemView.findViewById(R.id.count);
        photo = itemView.findViewById(R.id.profile_image);
        buttonLike = itemView.findViewById(R.id.like);
        buttonComment = itemView.findViewById(R.id.comment);
        buttonShare = itemView.findViewById(R.id.share);
        buttonGo = itemView.findViewById(R.id.will_go);
        buttonMenu = itemView.findViewById(R.id.menu);
        like = itemView.findViewById(R.id.like_txt);
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

    public TextView getCountUsers() {
        return countUsers;
    }

    public ImageView getPhoto() {
        return photo;
    }

    public ImageButton getButtonLike() {
        return buttonLike;
    }

    public ImageButton getButtonShare() {
        return buttonShare;
    }

    public ImageButton getButtonComment() {
        return buttonComment;
    }

    public ImageButton getButtonMenu() {
        return buttonMenu;
    }

    public Button getButtonGo() {
        return buttonGo;
    }

    public TextView getLike() {
        return like;
    }
}