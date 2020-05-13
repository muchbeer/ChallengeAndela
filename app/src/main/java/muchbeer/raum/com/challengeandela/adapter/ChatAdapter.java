package muchbeer.raum.com.challengeandela.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.listener.ChatRecyclerClickListener;
import muchbeer.raum.com.challengeandela.models.ChatRoom;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>    {

     private static final String LOG_TAG = ChatAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<ChatRoom> listChat;
    private ChatRecyclerClickListener mClickListener;

    public ChatAdapter(Context mContext, ArrayList<ChatRoom> listChat, ChatRecyclerClickListener mClickListener) {
        this.mContext = mContext;
       this.listChat = listChat;
        this.mClickListener = mClickListener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chatroom_listitem,
                                parent,
                        false);
        final ChatViewHolder holder = new ChatViewHolder(view, mClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.creator_name.setText(listChat.get(position).getChatroom_id());
        holder.number_chatmessages.setText("12");
        holder.name.setText(listChat.get(position).getChatroom_name());

        Picasso.get()
                .load(listChat.get(position).getCreator_id())
                .placeholder(R.drawable.ic_android)
                .error(R.drawable.ic_android)
                .fit()
                .into(holder.profile_image);
    }

    @Override
    public int getItemCount() {
        return listChat.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView creator_name, number_chatmessages, name;
        ImageView profile_image;
        ChatRecyclerClickListener mClickListener;

        public ChatViewHolder(@NonNull View itemView, ChatRecyclerClickListener clickListener) {
            super(itemView);

            creator_name = itemView.findViewById(R.id.creator_name);
            number_chatmessages = itemView.findViewById(R.id.number_chatmessages);
            profile_image = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.name);

            mClickListener = clickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) { mClickListener.click(listChat.get(getAdapterPosition())); }
    }
}
