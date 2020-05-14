package muchbeer.raum.com.challengeandela.chatviewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import muchbeer.raum.com.challengeandela.models.ChatMessage;
import muchbeer.raum.com.challengeandela.models.ChatRoom;
import muchbeer.raum.com.challengeandela.repository.AuthRepository;


public class ChatViewModel extends AndroidViewModel {

    AuthRepository authRepository;

    public ChatViewModel(@NonNull Application application) {
        super(application);

        authRepository = new AuthRepository(application);
    }

    public LiveData<List<ChatRoom>> getAllChatRoomies() { return authRepository.RetrievedChatRoomies(); }

    public LiveData<List<ChatMessage>> getAllMessage(String chatroom_id) {
                return authRepository.RetrievedChatMessages(chatroom_id); }

    public LiveData<String> getErrorUpdates() { return authRepository.getErrorUpdates(); }

    public LiveData<String> getSecurityLevel() { return authRepository.getSecurityLevel(); }

    public void createNewChatroomie(String roomName, String securityLevel) {
        authRepository.createNewChatroom(roomName, securityLevel);   }

    public void createNewMessages(String messages, String chatroom_id) {
        authRepository.createNewMessage(messages, chatroom_id);
    }

}
