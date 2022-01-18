package dem.xbitly.eventplatform.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.Objects;

import dem.xbitly.eventplatform.R;

public class BottomSheetEditDialog extends BottomSheetDialogFragment {

    private String id;
    boolean isReview;
    DatabaseReference ref;

    boolean a = true;

    public BottomSheetEditDialog(String id, boolean isReview){

        this.id = id;
        this.isReview = isReview;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.bottom_sheet_edit_layout, container, false);

        ImageButton btnClose = v.findViewById(R.id.btn_close);
        EditText editText = v.findViewById(R.id.edittext);
        Button btnSave = v.findViewById(R.id.btn_save);
        Button btnCancel = v.findViewById(R.id.btn_cancel);
        FirebaseDatabase dBase = FirebaseDatabase.getInstance();

        if(isReview){
            editText.setHint("Reviews");
            ref = dBase.getReference("Reviews").child(id);
        } else {
            editText.setHint("Description");
            ref = dBase.getReference("Invite").child(id);
        }

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(a) {
                    editText.setText(Objects.requireNonNull(snapshot.child("text").getValue()).toString());
                    a = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnCancel.setOnClickListener(view -> dismiss());
        btnClose.setOnClickListener(view -> dismiss());

        btnSave.setOnClickListener(view -> {
            String text = editText.getText().toString();
            if(text.isEmpty()){
                FancyToast.makeText(v.getContext(),"Fields cannot be empty",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
            } else {
                ref.child("text").setValue(text);
                dismiss();
            }
        });

        return v;
    }

}
