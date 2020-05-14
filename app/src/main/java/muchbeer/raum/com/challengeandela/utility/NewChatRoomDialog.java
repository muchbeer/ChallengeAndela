package muchbeer.raum.com.challengeandela.utility;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.chatroom.ChatActivity;
import muchbeer.raum.com.challengeandela.chatviewmodel.ChatViewModel;
import muchbeer.raum.com.challengeandela.models.ChatMessage;
import muchbeer.raum.com.challengeandela.models.ChatRoom;
import muchbeer.raum.com.challengeandela.models.Users;

public class NewChatRoomDialog extends DialogFragment {


    private static final String TAG = "NewChatroomDialog";

    private SeekBar mSeekBar;
    private EditText mChatroomName;
    private TextView mCreateChatroom, mSecurityLevel;
    private int mUserSecurityLevel;
    private int mSeekProgress;
    private ChatViewModel mainActivityViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_chatroom, container, false);
        mChatroomName = (EditText) view.findViewById(R.id.input_chatroom_name);
        mSeekBar = (SeekBar) view.findViewById(R.id.input_security_level);
        mCreateChatroom = (TextView) view.findViewById(R.id.create_chatroom);
        mSecurityLevel = (TextView) view.findViewById(R.id.security_level);
        mSeekProgress = 0;
        mSecurityLevel.setText(String.valueOf(mSeekProgress));

        mainActivityViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        getUserSecurityLevel();

        mCreateChatroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mChatroomName.getText().toString().equals("")){
                    Log.d(TAG, "onClick: creating new chat room");
                if(mUserSecurityLevel >= mSeekBar.getProgress()){

                        Log.d(TAG, "Satisfy the fully condition");

       mainActivityViewModel.createNewChatroomie(mChatroomName.getText().toString(),
                                                            String.valueOf(mSeekBar.getProgress()));

                        ((ChatActivity)getActivity()).init();
                        getDialog().dismiss();
                    }else{
                        Toast.makeText(getActivity(), "insuffient security level", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSeekProgress = progress;
                mSecurityLevel.setText(String.valueOf(mSeekProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return view;
    }

    private void getUserSecurityLevel(){
        mainActivityViewModel.getSecurityLevel().observe(getActivity(), valueSecurityLevel-> {
            Log.d(TAG, "onDataChange: users security level: " +valueSecurityLevel);

            mUserSecurityLevel = Integer.parseInt(valueSecurityLevel);
        });
    }

    /**
     * Return the current timestamp in the form of a string
     * @return
     */
    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return sdf.format(new Date());
    }

}
