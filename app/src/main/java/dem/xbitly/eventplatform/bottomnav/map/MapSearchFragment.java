package dem.xbitly.eventplatform.bottomnav.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;

import dem.xbitly.eventplatform.Event.Event;
import dem.xbitly.eventplatform.Event.EventsAdapter;
import dem.xbitly.eventplatform.R;

public class MapSearchFragment extends Fragment {

    ArrayList<Event> eventsList;
    RecyclerView events_rv;
    SearchView events_search;
    GoogleMap googleMap;
    EventsAdapter eventsAdapter;

    public MapSearchFragment(ArrayList<Event> eventsList, GoogleMap googleMap){
        this.eventsList = eventsList;
        this.googleMap = googleMap;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_search_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        events_rv = view.findViewById(R.id.events_rv);
        events_search = view.findViewById(R.id.events_map_search_view);

        // for tests
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));
        eventsList.add(new Event("hellooooo", false, "14.06.2022", 1, 1));

        eventsAdapter = new EventsAdapter(eventsList, googleMap);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider_rv));
        events_rv.addItemDecoration(itemDecorator);
        events_rv.setLayoutManager(linearLayoutManager);
        events_rv.setAdapter(eventsAdapter);

        events_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
    }

    private void filter(String eventName){
        ArrayList<Event> filteredEventsList = new ArrayList<>();

        for (Event event : eventsList){
            if (event.getName().toLowerCase().contains(eventName.toLowerCase())){
                filteredEventsList.add(event);
            }
        }
        if (filteredEventsList.isEmpty()){
            FancyToast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
        }else{
            eventsAdapter.filterList(filteredEventsList);
        }
    }
}