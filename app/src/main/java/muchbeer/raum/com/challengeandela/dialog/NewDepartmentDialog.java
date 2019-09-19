package muchbeer.raum.com.challengeandela.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.messagefirebase.AdminActivity;

import static android.text.TextUtils.isEmpty;

public class NewDepartmentDialog extends DialogFragment {

    private static final String TAG = "NewDepartmentDialog";


    //widgets
    private EditText mNewDepartment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_department, container, false);
        mNewDepartment = (EditText) view.findViewById(R.id.input_new_department);

        TextView confirmDialog = (TextView) view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmpty(mNewDepartment.getText().toString())){
                    Log.d(TAG, "onClick: adding new department to the list.");
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    reference
                            .child(getString(R.string.dbnode_departments))
                            .child(mNewDepartment.getText().toString())
                            .setValue(mNewDepartment.getText().toString());
                    getDialog().dismiss();

                    ((AdminActivity)getActivity()).getDepartments();
                }
            }
        });

        return view;
    }
}
