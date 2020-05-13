package muchbeer.raum.com.challengeandela.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.models.ChatMessage;
import muchbeer.raum.com.challengeandela.models.ChatRoom;

public class AuthRepository {

    private static final String LOG_TAG = AuthRepository.class.getSimpleName();
    private ArrayList<ChatRoom> mChatList = new ArrayList<>();
    private Application application;
    DatabaseReference reference;
    Query queryChat;
    private MutableLiveData<List<ChatRoom>> chatLiveData=new MutableLiveData<>();
    private MutableLiveData<String> mChatError = new MutableLiveData<>();

    public AuthRepository(Application application) {
        this.application = application;
        reference = FirebaseDatabase.getInstance().getReference();
    }

    public MutableLiveData<List<ChatRoom>> RetrievedCountries() {
        //   Log.d(LOG_TAG, "the MutableLiveData: here has entered here");

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

    public LiveData<List<ChatRoom>> getAllChatRooms() { return chatLiveData;  }

    public LiveData<String> getErrorUpdates() { return mChatError; }

}
