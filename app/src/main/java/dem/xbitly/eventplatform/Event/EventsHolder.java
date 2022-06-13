package dem.xbitly.eventplatform.Event;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dem.xbitly.eventplatform.R;

public class EventsHolder extends RecyclerView.ViewHolder{
    private final TextView name_txtv, date_txtv, privacy_txtv;


    public EventsHolder(@NonNull View itemView) {
        super(itemView);

        name_txtv = itemView.findViewById(R.id.event_name_search);
        privacy_txtv = itemView.findViewById(R.id.event_privacy_search);
        date_txtv = itemView.findViewById(R.id.event_date_search);
    }

    public TextView getName_txtv() {
        return name_txtv;
    }

    public TextView getDate_txtv() {
        return date_txtv;
    }

    public TextView getPrivacy_txtv() {
        return privacy_txtv;
    }
}
