package dem.xbitly.eventplatform.bottomnav.profile;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import dem.xbitly.eventplatform.R;
import dem.xbitly.eventplatform.activities.InternetErrorConnectionActivity;
import dem.xbitly.eventplatform.activities.MainActivity;
import dem.xbitly.eventplatform.activities.SettingsActivity;
import dem.xbitly.eventplatform.activities.StartActivity;
import dem.xbitly.eventplatform.network.NetworkManager;
import dem.xbitly.eventplatform.tape.TapeAdapter;
import dem.xbitly.eventplatform.viewmodels.MembersViewModel;
import dem.xbitly.eventplatform.viewmodels.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private RecyclerView rv;

    private TextView profile_name;

    private ProfileViewModel viewModel;

    private String[] reviews;
    private String[] invites;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        checkNetwork();

        FirebaseDatabase dBase = FirebaseDatabase.getInstance();
        DatabaseReference ref = dBase.getReference("Users");
        profile_name = root.findViewById(R.id.profile_name);
        rv = root.findViewById(R.id.profile_posts_recycler);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        viewModel.getReviews().observe(getActivity(), new Observer<String[]>() {
            @Override
            public void onChanged(String[] sstrs) {
                reviews = sstrs;
                viewModel.getInvites().observe(getActivity(), new Observer<String[]>() {
                    @Override
                    public void onChanged(String[] strs) {
                        invites = strs;
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(root.getContext());
                        linearLayoutManager.setReverseLayout(true);
                        linearLayoutManager.setStackFromEnd(true);
                        rv.setLayoutManager(linearLayoutManager);
                        rv.setHasFixedSize(true);
                        TapeAdapter tapeAdapter = new TapeAdapter(reviews, invites, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), root.getContext(), getParentFragmentManager());
                        rv.setAdapter(tapeAdapter);
                    }
                });
            }
        });

        viewModel.getUserName().observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                profile_name.setText(s);
            }
        });

        ImageButton logout_btn = root.findViewById(R.id.logout_btn);
        ImageButton settings_btn = root.findViewById(R.id.settings_btn);

        logout_btn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent (getContext(), StartActivity.class);
            startActivity(intent);
        });

        settings_btn.setOnClickListener(v -> {
            Intent intent = new Intent (getContext(), SettingsActivity.class);
            startActivity(intent);
        });

        return root;
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

//    @Override
//    public void onStart() {
//        super.onStart();
//        viewModel.getUserName().observe(getActivity(), new Observer<String>() {
//            @Override
//            public void onChanged(String usrname) {
//                username = usrname;
//                profile_name.setText(username);
//            }
//        });
//    }
}