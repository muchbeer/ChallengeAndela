package muchbeer.raum.com.challengeandela.chatroom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.adapter.ChatAdapter;
import muchbeer.raum.com.challengeandela.chatviewmodel.ChatViewModel;
import muchbeer.raum.com.challengeandela.listener.ChatRecyclerClickListener;
import muchbeer.raum.com.challengeandela.models.ChatRoom;
import muchbeer.raum.com.challengeandela.utility.ChatroomListAdapter;
import muchbeer.raum.com.challengeandela.utility.NewChatRoomDialog;

public class ChatActivity extends AppCompatActivity implements ChatRecyclerClickListener{
    private static final String TAG =ChatActivity.class.getSimpleName();
    private ArrayList<ChatRoom> mChatRooms;
    private ChatRoom mChatPosition;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout mShimmerViewContainer;
    public static boolean isActivityRunning;

    private FloatingActionButton mFob;
    private ArrayList<ChatRoom> mChatrooms;
    private ChatroomListAdapter mAdapter;

    private ChatAdapter mChatRecyclerAdapter;
    private ChatViewModel mainActivityViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setContentView(R.layout.activity_chat);
        recyclerView = findViewById(R.id.recyclerView);
        mShimmerViewContainer = findViewById(R.id.loadContainer);
        mFob = findViewById(R.id.fob);

        mainActivityViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        init();
    }

    public void init() {

        retrieveChatRoomies();

        mFob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewChatRoomDialog dialog = new NewChatRoomDialog();
                dialog.show(getSupportFragmentManager(), getString(R.string.dialog_new_chatroom));
            }
        });
    }

    private void retrieveChatRoomies() {

        mainActivityViewModel.getAllChatRoomies().observe(this, roomies -> {
            if(roomies !=null ) {
                // stop animating Shimmer and hide the layout
                mShimmerViewContainer.stopShimmer();
                mShimmerViewContainer.setVisibility(View.GONE);

                mChatRooms = (ArrayList<ChatRoom>) roomies;
                Log.d(TAG, "the roomies value obtained:  " + mChatRooms);
                setChatPosition();
                initChatRecyclerView();
            } else {

                mShimmerViewContainer.startShimmer();
                mShimmerViewContainer.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Connection error: " + mainActivityViewModel.getErrorUpdates(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initChatRecyclerView() {
        mChatRecyclerAdapter = new ChatAdapter(this, mChatRooms, (ChatRecyclerClickListener) this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mChatRecyclerAdapter);
        mChatRecyclerAdapter.notifyDataSetChanged();
    }

    private void setChatPosition() {
        for(ChatRoom chatRoomie : mChatRooms){
            Log.d(TAG, "chat rooms are : " + chatRoomie.getChatroom_name());
            mChatPosition = chatRoomie;
        }
    }

    public void showDeleteChatroomDialog(String chatroomId){
        DeleteChatroomDialog dialog = new DeleteChatroomDialog();
        Bundle args = new Bundle();
        args.putString(getString(R.string.field_chatroom_id), chatroomId);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), getString(R.string.dialog_delete_chatroom));
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityRunning = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityRunning=false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mShimmerViewContainer.stopShimmer();
    }

    @Override
    public void click(ChatRoom chatposition) {
        Log.d(TAG, "onItemClick: selected chatroom: " + chatposition.getChatroom_id());
        Intent intent = new Intent(ChatActivity.this, ChatRoomActivity.class);
        intent.putExtra(getString(R.string.intent_chatroom), chatposition);
        startActivity(intent);
    }
}
