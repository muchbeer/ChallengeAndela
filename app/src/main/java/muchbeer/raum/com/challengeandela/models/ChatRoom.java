package muchbeer.raum.com.challengeandela.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.firebase.ui.auth.data.model.User;

import java.util.List;

public class ChatRoom implements Parcelable {


    private String chatroom_name;
    private String creator_id;
    private String security_level;
    private String chatroom_id;
    private List<ChatMessage> chatroom_messages;
    private List<Users> users;


    public List<Users> getUsers() {
        return users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }


    public ChatRoom(String chatroom_name, String creator_id, String security_level, String chatroom_id, List<ChatMessage> chatroom_messages) {
        this.chatroom_name = chatroom_name;
        this.creator_id = creator_id;
        this.security_level = security_level;
        this.chatroom_id = chatroom_id;
        this.chatroom_messages = chatroom_messages;
    }

    public ChatRoom() {
    }

    public String getChatroom_name() {
        return chatroom_name;
    }

    public void setChatroom_name(String chatroom_name) {
        this.chatroom_name = chatroom_name;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public String getSecurity_level() {
        return security_level;
    }

    public void setSecurity_level(String security_level) {
        this.security_level = security_level;
    }

    public String getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(String chatroom_id) {
        this.chatroom_id = chatroom_id;
    }

    public List<ChatMessage> getChatroom_messages() {
        return chatroom_messages;
    }

    public void setChatroom_messages(List<ChatMessage> chatroom_messages) {
        this.chatroom_messages = chatroom_messages;
    }

    protected ChatRoom(Parcel in) {
        chatroom_name = in.readString();
        creator_id = in.readString();
        security_level = in.readString();
        chatroom_id = in.readString();
    }

    public static final Creator<ChatRoom> CREATOR = new Creator<ChatRoom>() {
        @Override
        public ChatRoom createFromParcel(Parcel in) {
            return new ChatRoom(in);
        }

        @Override
        public ChatRoom[] newArray(int size) {
            return new ChatRoom[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Chatroom{" +
                "chatroom_name='" + chatroom_name + '\'' +
                ", creator_id='" + creator_id + '\'' +
                ", security_level='" + security_level + '\'' +
                ", chatroom_id='" + chatroom_id + '\'' +
                ", chatroom_messages=" + chatroom_messages +
                '}';
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(chatroom_name);
        dest.writeString(creator_id);
        dest.writeString(security_level);
        dest.writeString(chatroom_id);
    }
}
