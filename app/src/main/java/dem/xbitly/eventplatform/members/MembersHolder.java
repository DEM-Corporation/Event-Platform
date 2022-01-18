package dem.xbitly.eventplatform.members;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dem.xbitly.eventplatform.R;

public class MembersHolder extends RecyclerView.ViewHolder{

    private final ImageView profilePhoto;
    private final TextView profileName;

    public MembersHolder(@NonNull View itemView) {
        super(itemView);
        profilePhoto = itemView.findViewById(R.id.profile_image);
        profileName = itemView.findViewById(R.id.username);
    }

    public ImageView getProfilePhoto() {
        return profilePhoto;
    }

    public TextView getProfileName() {
        return profileName;
    }
}
