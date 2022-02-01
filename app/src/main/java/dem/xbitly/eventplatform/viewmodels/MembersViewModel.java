package dem.xbitly.eventplatform.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import dem.xbitly.eventplatform.activities.MembersActivity;

public class MembersViewModel extends ViewModel {

    MutableLiveData<String> ids;
    String path;
    String eventID;
    boolean e = true;

    public LiveData<String> getIds(){
        if (ids==null){
            ids = new MutableLiveData<>();
            loadIds();
        }
        return ids;
    }

    public void setPath(String path){
        this.path=path;
    }

    public void setEventID(String eventId){
        this.eventID=eventId;
    }

    private void loadIds(){
        e = true;
        FirebaseDatabase.getInstance().getReference(path).child(eventID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if (e){
                     String go = Objects.requireNonNull(snapshot.child("go").getValue()).toString();
                     ids.setValue(go);
                     e = false;
                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
