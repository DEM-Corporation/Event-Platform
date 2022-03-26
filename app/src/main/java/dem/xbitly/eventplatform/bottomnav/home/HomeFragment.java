package dem.xbitly.eventplatform.bottomnav.home;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import dem.xbitly.eventplatform.R;
import dem.xbitly.eventplatform.activities.CommentActivity;
import dem.xbitly.eventplatform.activities.InternetErrorConnectionActivity;
import dem.xbitly.eventplatform.activities.MainActivity;
import dem.xbitly.eventplatform.activities.UsersInvitationActivity;
import dem.xbitly.eventplatform.network.NetworkManager;
import dem.xbitly.eventplatform.tape.TapeAdapter;

public class HomeFragment extends Fragment {

    private RecyclerView rv;
    private boolean isUpdateRV = true;

    private SwipeRefreshLayout refresh;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseDatabase dBase = FirebaseDatabase.getInstance();
        DatabaseReference ref = dBase.getReference("Reviews");
        DatabaseReference ref2 = dBase.getReference("Invite");

        rv = root.findViewById(R.id.home_posts_recycler);
        refresh = root.findViewById(R.id.refresh_tape);

        refresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkNetwork();
                refresh.setRefreshing(true);
                isUpdateRV = true;
                updateRecycler(ref, ref2, root);
            }
        });

        updateRecycler(ref, ref2, root);

        return root;
    }

    private void updateRecycler(DatabaseReference ref, DatabaseReference ref2, View root){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<String> s = new ArrayList<>();

                for (int i = 0; i < Integer.parseInt(Objects.requireNonNull(snapshot.child("count").getValue()).toString()); i++) {
                    s.add((i+1)+"");
                }

                ref2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        ArrayList<String> s1 = new ArrayList<>();

                        for (int i = 0; i < Integer.parseInt(Objects.requireNonNull(snapshot.child("count").getValue()).toString()); i++) {
                            s1.add((i+1)+"");
                        }

                        String[] ss = s.toArray(new String[0]);
                        String[] ss1 = s1.toArray(new String[0]);

                        if(isUpdateRV) {

                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(root.getContext());
                            linearLayoutManager.setReverseLayout(true);
                            linearLayoutManager.setStackFromEnd(true);
                            rv.setLayoutManager(linearLayoutManager);
                            rv.setHasFixedSize(true);
                            try {
                                isUpdateRV = false;
                                TapeAdapter tapeAdapter = new TapeAdapter(ss, ss1, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), root.getContext(), getParentFragmentManager());
                                rv.setAdapter(tapeAdapter);
                            } catch (Exception e){
                                isUpdateRV = true;
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                refresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void checkNetwork(){
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            if (!connected) {
                Intent in_intent = new Intent (getContext(), InternetErrorConnectionActivity.class);
                startActivity(in_intent);
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
    }
}