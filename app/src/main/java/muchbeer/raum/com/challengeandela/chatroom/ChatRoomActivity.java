package muchbeer.raum.com.challengeandela.chatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.adapter.ChatMessageAdapter;
import muchbeer.raum.com.challengeandela.chatviewmodel.ChatViewModel;
import muchbeer.raum.com.challengeandela.firebaseauth.LoginActivity;
import muchbeer.raum.com.challengeandela.listener.ChatMessageRecyclerClick;
import muchbeer.raum.com.challengeandela.models.ChatMessage;
import muchbeer.raum.com.challengeandela.models.ChatRoom;
import muchbeer.raum.com.challengeandela.models.Users;

public class ChatRoomActivity extends AppCompatActivity implements ChatMessageRecyclerClick {
    private static final String TAG = ChatRoomActivity.class.getSimpleName();

    public static boolean isActivityRunning;

    //firebase
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mMessagesReference;

    //widgets
    private TextView mChatroomName;
    private RecyclerView recyclerView;
    private EditText mMessage;
    private ImageView mCheckmark;

    //vars
    private ChatRoom mChatroom;
    private ArrayList<ChatMessage> mMessagesList;
    private ChatMessageAdapter adapter;
    private ChatViewModel mainActivityViewModel;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

       Toolbar toolbar = findViewById(R.id.toolbar);
       setSupportActionBar(toolbar);

        mChatroomName = findViewById(R.id.text_chatroom_name);
        recyclerView =  findViewById(R.id.recyclerView);

        mMessage =  findViewById(R.id.input_message);
        mCheckmark =  findViewById(R.id.checkmark);

        mainActivityViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        setupFirebaseAuth();
        getChatroom();
        init();

        hideSoftKeyboard();
    }

    private void init(){

        mMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
           //     recyclerView.getLayoutManager().scrollToPosition(adapter.getItemCount() -1);
             }
        });

        mCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: initiated");
                if(!mMessage.getText().toString().equals("")){
                    String message = mMessage.getText().toString();
                    Log.d(TAG, "onClick: sending new message: " + message);

                    mainActivityViewModel.createNewMessages(message, mChatroom.getChatroom_id());
                    mMessage.setText("");
                    getChatroomMessages();
                }
            }
        });
    }

    /**
     * Retrieve the chatroom name using a query
     */
    private void getChatroom(){
        Log.d(TAG, "getChatroom: getting selected chatroom details");

        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.intent_chatroom))){
            ChatRoom chatroom = intent.getParcelableExtra(getString(R.string.intent_chatroom));
            Log.d(TAG, "getChatroom: chatroom: " + chatroom.getChatroom_id());
            mChatroom = chatroom;
            mChatroomName.setText(mChatroom.getChatroom_name());

            enableChatroomListener();
        }
    }


    private void getChatroomMessages(){
        mMessagesList = new ArrayList<>();
        if(mMessagesList.size() > 0){
            mMessagesList.clear();

        }
        Log.d(TAG, "getChatroomMessages: Method before LiveData");
        mainActivityViewModel.getAllMessage(mChatroom.getChatroom_id()).observe(this, messages -> {
            Log.d(TAG, "getChatroomMessages: found chatroom message: " + messages);

            if(messages !=null) {
                mMessagesList = (ArrayList) messages;
                Log.d(TAG, "mMessageList from the livedata is: " + mMessagesList);
                //query the users node to get the profile images and names
                getUserDetails();

                initMessageRecyclerView();

            }else {
                Log.d(TAG, "Error come is here: " );
            }

        });
      }

    private void initMessageRecyclerView() {
        Log.d(TAG, "received data from mMessageList is:  "+ mMessagesList );
      adapter = new ChatMessageAdapter(this, mMessagesList, (ChatMessageRecyclerClick) this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(mMessagesList.size() - 1);
        adapter.notifyDataSetChanged();

    }

    private void getUserDetails(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for(int i = 0; i < mMessagesList.size(); i++) {
            Log.d(TAG, "onDataChange: searching for userId: " + mMessagesList.get(i).getUser_id());
            final int j = i;
            if(mMessagesList.get(i).getUser_id() != null){
                Query query = reference.child(getString(R.string.dbnode_users))
                        .orderByKey()
                        .equalTo(mMessagesList.get(i).getUser_id());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                        Log.d(TAG, "onDataChange: found user id: "
                                + singleSnapshot.getValue(Users.class).getUser_id());
                        mMessagesList.get(j).setProfile_image(singleSnapshot.getValue(Users.class).getProfile_image());
                        mMessagesList.get(j).setName(singleSnapshot.getValue(Users.class).getName());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


     /*
            ----------------------------- Firebase setup ---------------------------------
    */

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }

    private void checkAuthenticationState(){
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Log.d(TAG, "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(ChatRoomActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());


                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(ChatRoomActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ChatRoomActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                // ...
            }
        };

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMessagesReference.removeEventListener(mValueEventListener);
    }

    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            getChatroomMessages();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void enableChatroomListener(){
         /*
            ---------- Listener that will watch the 'chatroom_messages' node ----------
         */
        mMessagesReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbnode_chatrooms))
                .child(mChatroom.getChatroom_id())
                .child(getString(R.string.field_chatroom_messages));

        mMessagesReference.addValueEventListener(mValueEventListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
        //vars
        isActivityRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityRunning = false;
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void click(ChatMessage message) {
        Toast.makeText(getApplicationContext(), "The click message is: " + message, Toast.LENGTH_LONG).show();
    }
}
