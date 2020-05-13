package muchbeer.raum.com.challengeandela.chatviewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import muchbeer.raum.com.challengeandela.models.ChatRoom;
import muchbeer.raum.com.challengeandela.repository.AuthRepository;

public class ChatViewModel extends AndroidViewModel {

    AuthRepository authRepository;

    public ChatViewModel(@NonNull Application application) {
        super(application);

        authRepository = new AuthRepository(application);
    }

    public LiveData<List<ChatRoom>> getAllChatRoomies() { return authRepository.RetrievedCountries(); }

    public LiveData<String> getErrorUpdates() { return authRepository.getErrorUpdates(); }

}
