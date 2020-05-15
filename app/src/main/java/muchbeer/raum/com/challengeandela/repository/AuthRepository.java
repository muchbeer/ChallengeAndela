package muchbeer.raum.com.challengeandela.repository;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.models.ChatMessage;
import muchbeer.raum.com.challengeandela.models.ChatRoom;
import muchbeer.raum.com.challengeandela.models.Users;
import muchbeer.raum.com.challengeandela.utility.FirebaseUtil;

public class AuthRepository {

    private static final String LOG_TAG = AuthRepository.class.getSimpleName();
    private ArrayList<ChatRoom> mChatList = new ArrayList<>();
    private ArrayList<ChatMessage> mMessageList = new ArrayList<>();

    private ChatRoom mChatroom;
    private Application application;
    DatabaseReference reference;
    Query queryChat, querySecurityLevel, queryChatMessage;
    private MutableLiveData<List<ChatRoom>> chatLiveData=new MutableLiveData<>();
    private  MutableLiveData<List<ChatMessage>> messagesLiveData = new MutableLiveData<>();
    private MutableLiveData<String> chatSecurityLevel = new MutableLiveData<>();
    private MutableLiveData<String> mChatError = new MutableLiveData<>();
    private String mUserSecurityLevel;

    public AuthRepository(Application application) {
        this.application = application;
        reference = FirebaseUtil.getDatabase().getReference();
    }

    public void createNewMessage(String message, String chatroom_id) {

        ChatMessage newMessage = new ChatMessage();
        newMessage.setMessage(message);
        newMessage.setTimestamp(getTimestamp());
        newMessage.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //get a database reference
        reference= FirebaseUtil.getDatabase().getReference()
                .child(application.getString(R.string.dbnode_chatrooms))
                .child(chatroom_id)
                .child(application.getString(R.string.field_chatroom_messages));

        //create the new messages id
        String newMessageId = reference.push().getKey();
        Log.d(LOG_TAG, "NewMessageID is: " + newMessageId+ " theMessage is: " + newMessage);

        //insert the new message into the chatroom
        reference
                .child(newMessageId)
                .setValue(newMessage);
    }


    public void createNewChatroom(String chatroomName, String securityLevel) {

        //get the new chatroom unique id
        String chatroomId = reference
                .child(application.getString(R.string.dbnode_chatrooms))
                .push().getKey();
        Log.d(LOG_TAG, "The key that will be pushed is:  " + chatroomId);

        //create the chatroom
        ChatRoom chatroom = new ChatRoom();
        chatroom.setSecurity_level(securityLevel);
        chatroom.setChatroom_name(chatroomName);
        chatroom.setCreator_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        chatroom.setChatroom_id(chatroomId);

        //insert the new chatroom into the database
        reference
                .child(application.getString(R.string.dbnode_chatrooms))
                .child(chatroomId)
                .setValue(chatroom);

        //create a unique id for the message
        String messageId = reference
                .child(application.getString(R.string.dbnode_chatrooms))
                .child(chatroomId)
                .child(application.getString(R.string.field_chatroom_messages))
                .push().getKey();
        Log.d(LOG_TAG, "The messageId is: "+ messageId +"   compare the chatroomID:  " + chatroom);
        //insert the first message into the chatroom
        ChatMessage message = new ChatMessage();

        message.setMessage("Welcome to the new chatroom!");
        message.setTimestamp(getTimestamp());
        reference
                .child(application.getString(R.string.dbnode_chatrooms))
                .child(chatroomId)
                .child(application.getString(R.string.field_chatroom_messages))
                .child(messageId)
                .setValue(message);
    }


    public MutableLiveData<String> getSecurityLevel() {

        querySecurityLevel = reference.child(application.getString(R.string.dbnode_users))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        querySecurityLevel.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot:  dataSnapshot.getChildren()){

            mUserSecurityLevel = singleSnapshot.getValue(Users.class).getSecurity_level();
            chatSecurityLevel.postValue(mUserSecurityLevel);
                    Log.d(LOG_TAG, "The user Security level is: "+ mUserSecurityLevel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

      return  chatSecurityLevel;
    }

    public MutableLiveData<List<ChatMessage>> RetrievedChatMessages(String chatroom_id,
                                                              final Set<String> mMessageIdSet) {


        queryChatMessage = reference.child(application.getString(R.string.dbnode_chatrooms))
                .child(chatroom_id)
                .child(application.getString(R.string.field_chatroom_messages));

        queryChatMessage.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {

                    DataSnapshot snapshot = singleSnapshot;
      Log.d(LOG_TAG, "onDataChange: found chatroom message: " + singleSnapshot.getValue());

                    try {
                        //need to catch null pointer here because the initial welcome message to the
                        //chatroom has no user id
                        ChatMessage message = new ChatMessage();
                        String userId = snapshot.getValue(ChatMessage.class).getUser_id();

                        if(!mMessageIdSet.contains(snapshot.getKey())) {
                            mMessageIdSet.add(snapshot.getKey());
                            if (userId != null) { //check and make sure it's not the first message (has no user id)
                                message.setMessage(snapshot.getValue(ChatMessage.class).getMessage());
                                message.setUser_id(snapshot.getValue(ChatMessage.class).getUser_id());
                                message.setTimestamp(snapshot.getValue(ChatMessage.class).getTimestamp());
                                mMessageList.add(message);
                                messagesLiveData.postValue(mMessageList);
                            } else {
                                message.setMessage(snapshot.getValue(ChatMessage.class).getMessage());
                                message.setTimestamp(snapshot.getValue(ChatMessage.class).getTimestamp());
                                mMessageList.add(message);
                                messagesLiveData.postValue(mMessageList);
                            }
                        }

                    } catch (NullPointerException e) {
                        Log.e(LOG_TAG, "onDataChange: NullPointerException: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(LOG_TAG, "message error is :" + databaseError);
            }
        });

        return messagesLiveData;
    }
    public MutableLiveData<List<ChatRoom>> RetrievedChatRoomies() {
       queryChat = reference.child(application.getString(R.string.dbnode_chatrooms));

        queryChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot:  dataSnapshot.getChildren()){
                    Log.d(LOG_TAG, "onDataChange: found chatroom: " + singleSnapshot.getValue());

                    ChatRoom chatroom = new ChatRoom();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    chatroom.setChatroom_id(objectMap.get(application.getString(R.string.field_chatroom_id)).toString());
                    chatroom.setChatroom_name(objectMap.get(application.getString(R.string.field_chatroom_name)).toString());
                    chatroom.setCreator_id(objectMap.get(application.getString(R.string.field_creator_id)).toString());
                    chatroom.setSecurity_level(objectMap.get(application.getString(R.string.field_security_level)).toString());

                    Log.d(LOG_TAG, "The object HashMap is: "+ objectMap.get(application.getString((R.string.field_chatroom_id))).toString());

                    //get the chatrooms messages
                    ArrayList<ChatMessage> messagesList = new ArrayList<ChatMessage>();
                    for(DataSnapshot snapshot: singleSnapshot
                            .child(application.getString(R.string.field_chatroom_messages)).getChildren()){
                        ChatMessage message = new ChatMessage();
                        message.setTimestamp(snapshot.getValue(ChatMessage.class).getTimestamp());
                        message.setUser_id(snapshot.getValue(ChatMessage.class).getUser_id());
                        message.setMessage(snapshot.getValue(ChatMessage.class).getMessage());
                        messagesList.add(message);
                    }
                    chatroom.setChatroom_messages(messagesList);
                    mChatList.add(chatroom);
                    chatLiveData.postValue(mChatList);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(LOG_TAG, "THE error is " + databaseError);
                mChatError.postValue(databaseError.getMessage());
            }
        });

        return chatLiveData;
    }

    public LiveData<String> getErrorUpdates() { return mChatError; }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return sdf.format(new Date());
    }

}
