package dem.xbitly.eventplatform.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileViewModel extends ViewModel {
    MutableLiveData<String> userName;
    MutableLiveData<String[]> reviews;
    MutableLiveData<String[]> invites;
    boolean e = true;
    private void loadUserName(){
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName.setValue(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public LiveData<String> getUserName(){
        if (userName==null){
            userName = new MutableLiveData<>();
            loadUserName();
        }
        return userName;
    }
    public LiveData<String[]> getReviews(){
        if (reviews == null){
            reviews = new MutableLiveData<>();
            loadReviews();
        }
        return reviews;
    }
    public LiveData<String[]> getInvites(){
        if (invites == null){
            invites = new MutableLiveData<>();
            loadInvites();
        }
        return invites;
    }
    private void loadReviews(){
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviews.setValue(!Objects.requireNonNull(snapshot.child("myReviews").getValue().toString()).equals("") ? Objects.requireNonNull(snapshot.child("myReviews").getValue()).toString().split(",") : new String[0]);
                        Log.d("array log", "REVIEWS array length: " + reviews.getValue().length);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadInvites(){
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        invites.setValue(!Objects.requireNonNull(snapshot.child("myInvites").getValue().toString()).equals("") ? Objects.requireNonNull(snapshot.child("myInvites").getValue()).toString().split(",") : new String[0]);
                        Log.d("array log", "INVITES array length: " + invites.getValue().length);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}
