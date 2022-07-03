package dem.xbitly.eventplatform.bottomnav.map;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import dem.xbitly.eventplatform.Event.Event;
import dem.xbitly.eventplatform.Event.EventsAdapter;
import dem.xbitly.eventplatform.activities.MembersActivity;
import dem.xbitly.eventplatform.bottomsheet.BottomSheetEventDialog;
import dem.xbitly.eventplatform.activities.InternetErrorConnectionActivity;
import dem.xbitly.eventplatform.activities.PrivateEventActivity;
import dem.xbitly.eventplatform.activities.PublicEventActivity;
import dem.xbitly.eventplatform.R;
import dem.xbitly.eventplatform.network.NetworkManager;

public class MapFragment extends Fragment implements LocationListener {

    private RelativeLayout create_event;

    private LocationManager locationManager;

    private Context context;

    private HashMap<String, String> event_info = new HashMap<>();

    private boolean e=true;
    private boolean a=true;

    private FloatingActionButton open_search_view_btn;

    private EventsAdapter eventsAdapter;

    private ArrayList<Event> eventsList = new ArrayList<>();

    private FrameLayout search_fragment;

    private FragmentManager fragmentManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_map, container, false);
        this.context = root.getContext();

        checkNetwork();

        //Check permissions
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }

        open_search_view_btn = root.findViewById(R.id.search_btn);
        open_search_view_btn.setTag("find");
        search_fragment = root.findViewById(R.id.search_fragment);

        fragmentManager = getParentFragmentManager();

        create_event = root.findViewById(R.id.create_event_btn);
        create_event.setOnClickListener(new View.OnClickListener() { //кнопка создания мероприятия
            @Override
            public void onClick(View v) {
                // create an alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                // set the custom dialog layout
                final View customLayout = getLayoutInflater().inflate(R.layout.create_event_dialog, null);
                customLayout.setBackgroundResource(R.drawable.create_event_dialog_shape);
                builder.setView(customLayout);

                Dialog dialog = new Dialog(getActivity());

                dialog.setContentView(customLayout);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.create_event_dialog_shape);

                dialog.show();
                dialog.setCanceledOnTouchOutside(false);

                Button public_btn = customLayout.findViewById(R.id.create_event_public);
                Button private_btn = customLayout.findViewById(R.id.create_event_private);

                public_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getContext(), PublicEventActivity.class));
                    }
                });

                private_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getContext(), PrivateEventActivity.class));
                    }
                });
            }
        });

        open_search_view_btn.setImageResource(R.drawable.ic_icon_find);

        return root;
    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {


        @Override
        public void onMapReady(GoogleMap googleMap) {


            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("UserPrivateEvents").addValueEventListener(new ValueEventListener() { //загрузка мероприятий на карту
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    try {
                        int n = Integer.parseInt(snapshot.child("count").getValue().toString());

                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            try {
                                String privacy = snapshot1.child("privacy").getValue().toString();


                                if (privacy.equals("yes")){
                                    String num = "1" + snapshot1.child("eventID").getValue().toString();//1 в начале строки означает, что privacy мероприятия =1, значит евент закрытый
                                    FirebaseDatabase.getInstance().getReference("PrivateEvents").child(snapshot1.child("eventID").getValue().toString())
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                    double longitude = Double.parseDouble(snapshot.child("adress").child("longitude").getValue().toString());
                                                    double latitude = Double.parseDouble(snapshot.child("adress").child("latitude").getValue().toString());
                                                    LatLng marker = new LatLng(latitude, longitude);
                                                    googleMap.addMarker(new MarkerOptions().position(marker)
                                                            .icon(getBitmapFromVectorDrawable(context, R.drawable.ic_location_marker_green))).setTag(num);
                                                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker));

                                                    String event_name = snapshot.child("name").getValue().toString();
                                                    String date = snapshot.child("date").getValue().toString();
                                                    eventsList.add(new Event(event_name, true, date, longitude, latitude));
                                                }

                                                @Override
                                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                                }
                                            });
                                }else{
                                    String num = "0" + snapshot1.child("eventID").getValue().toString(); //0 в начале строки означает, что privacy мероприятия =0, значит евент публичный
                                    FirebaseDatabase.getInstance().getReference("PublicEvents").child(snapshot1.child("eventID").getValue().toString())
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                    double longitude = Double.parseDouble(snapshot.child("adress").child("longitude").getValue().toString());
                                                    double latitude = Double.parseDouble(snapshot.child("adress").child("latitude").getValue().toString());
                                                    LatLng marker = new LatLng(latitude, longitude);
                                                    googleMap.addMarker(new MarkerOptions().position(marker)
                                                            .icon(getBitmapFromVectorDrawable(context, R.drawable.ic_location_marker))).setTag(num);
                                                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker));

                                                    String event_name = snapshot.child("name").getValue().toString();
                                                    String date = snapshot.child("date").getValue().toString();
                                                    eventsList.add(new Event(event_name, false, date, longitude, latitude));
                                                }

                                                @Override
                                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                                }
                                            });
                                }

                            } catch (Exception e) {

                            }
                        }
                    }catch (Exception e){

                    }

                    open_search_view_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FragmentTransaction ft = fragmentManager.beginTransaction();

                            if (open_search_view_btn.getTag().equals("find")){
                                ft.replace(R.id.search_fragment, new MapSearchFragment(eventsList, googleMap,
                                        getParentFragmentManager(), open_search_view_btn));
                                ft.commit();
                                open_search_view_btn.setImageResource(R.drawable.ic_baseline_close);
                                open_search_view_btn.setTag("close");
                            }else{
                                ft.remove(fragmentManager.findFragmentById(R.id.search_fragment));
                                ft.commit();
                                open_search_view_btn.setImageResource(R.drawable.ic_icon_find);
                                open_search_view_btn.setTag("find");
                            }
                        }
                    });


                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() { //при нажатии на маркер какого-либо мероприятия показываем информацию про него
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            String tag = (String)marker.getTag();
                            char privacy = tag.charAt(0);
                            String eventID = tag.substring(1,  tag.length());
                            System.out.println(eventID);

                            if (privacy == '1'){
                                a = true;
                                FirebaseDatabase.getInstance().getReference("PrivateEvents").child(eventID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (a){
                                            String name = snapshot.child("name").getValue().toString();
                                            String chatID2 = eventID;
                                            String chatID = snapshot.child("chatID").getValue().toString();
                                            String go = snapshot.child("go").getValue().toString();
                                            int count = go.split(",").length - 1;
                                            String time = snapshot.child("time").getValue().toString();
                                            String date = snapshot.child("date").getValue().toString();
                                            String longitude = snapshot.child("adress").child("longitude").getValue().toString();
                                            String latitude = snapshot.child("adress").child("latitude").getValue().toString();
                                            double latitude_d = Double.parseDouble(latitude);
                                            double longitude_d = Double.parseDouble(longitude);

                                            Geocoder geocoder;
                                            List<Address> addresses;
                                            geocoder = new Geocoder(getContext(), Locale.getDefault());

                                            try {
                                                addresses = geocoder.getFromLocation(latitude_d, longitude_d, 1);

                                                String address = addresses.get(0).getAddressLine(0);

                                                BottomSheetEventDialog bottomSheetEventDialog = new BottomSheetEventDialog(eventID, name,
                                                        address, Integer.toString(count), date, time, false, true, true);
                                                bottomSheetEventDialog.show(getParentFragmentManager(), "Event info");
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            a = false;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }else{
                                e = true;
                                FirebaseDatabase.getInstance().getReference("PublicEvents").child(eventID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (e){
                                            String name = snapshot.child("name").getValue().toString();
                                            String chatID2 = eventID;
                                            String chatID = snapshot.child("chatID").getValue().toString();
                                            String go = snapshot.child("go").getValue().toString();
                                            int count = go.split(",").length - 1;
                                            String time = snapshot.child("time").getValue().toString();
                                            String date = snapshot.child("date").getValue().toString();
                                            String longitude = snapshot.child("adress").child("longitude").getValue().toString();
                                            String latitude = snapshot.child("adress").child("latitude").getValue().toString();
                                            double latitude_d = Double.parseDouble(latitude);
                                            double longitude_d = Double.parseDouble(longitude);

                                            Geocoder geocoder;
                                            List<Address> addresses;
                                            geocoder = new Geocoder(getContext(), Locale.getDefault());

                                            try {
                                                addresses = geocoder.getFromLocation(latitude_d, longitude_d, 1);

                                                String address = addresses.get(0).getAddressLine(0);

                                                BottomSheetEventDialog bottomSheetEventDialog = new BottomSheetEventDialog(eventID, name,
                                                        address, Integer.toString(count), date, time, true, false, true);
                                                bottomSheetEventDialog.show(getParentFragmentManager(), "Event info");
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            e = false;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            return false;
                        }
                    });
                }


                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

        }
    };

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static BitmapDescriptor getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void checkNetwork(){
        if(!NetworkManager.isNetworkAvailable(this.getContext())){
            Intent in_intent = new Intent (this.getContext(), InternetErrorConnectionActivity.class);
            startActivity(in_intent);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }


}
