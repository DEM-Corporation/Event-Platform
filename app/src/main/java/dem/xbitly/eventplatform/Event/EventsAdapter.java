package dem.xbitly.eventplatform.Event;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;

import dem.xbitly.eventplatform.R;
import dem.xbitly.eventplatform.comments.CommentHolder;

public class EventsAdapter extends RecyclerView.Adapter<EventsHolder>{

    ArrayList<Event> eventsList;
    GoogleMap googleMap;

    public EventsAdapter (ArrayList<Event> eventsList, GoogleMap googleMap){
        this.eventsList = eventsList;
        this.googleMap = googleMap;
        Log.d("map_debug", "constructor, eventslist size: " + eventsList.size());
    }

    public void filterList (ArrayList<Event> filteredEventsList){
        this.eventsList = filteredEventsList;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_search_item, parent, false);
        return new EventsHolder(itemView);
    }

    @Override
    public void onBindViewHolder(EventsHolder holder, int position) {
        holder.getName_txtv().setText(eventsList.get(position).name);
        holder.getDate_txtv().setText(eventsList.get(position).date);
        holder.getPrivacy_txtv().setText((eventsList.get(position).privacy)?"private":"public");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng m = new LatLng(eventsList.get(position).getLatitude(), eventsList.get(position).getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m, 15));
                //TODO: dismiss searchview before beginning of animating camera
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }
}
