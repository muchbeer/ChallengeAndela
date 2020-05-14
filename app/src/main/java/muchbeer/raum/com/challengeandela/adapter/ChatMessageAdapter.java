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

import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.listener.ChatMessageRecyclerClick;
import muchbeer.raum.com.challengeandela.models.ChatMessage;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {

    private static final String LOG_TAG = ChatMessageAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<ChatMessage> listChat;
    private ChatMessageRecyclerClick mClickListener;

    public ChatMessageAdapter(Context mContext, ArrayList<ChatMessage> listChat, ChatMessageRecyclerClick mClickListener) {
        this.mContext = mContext;
        this.listChat = listChat;
        this.mClickListener = mClickListener;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chatmessage_listitem,
                parent,
                false);
        final ChatMessageViewHolder holder = new ChatMessageViewHolder(view, mClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        holder.message.setText(listChat.get(position).getMessage());
        holder.name.setText(listChat.get(position).getName());
        holder.profile_image.setImageResource(R.drawable.ic_android);

       /* Picasso.get()
                .load(listChat.get(position).getProfile_image())
                .placeholder(R.drawable.ic_android)
                .error(R.drawable.ic_android)
                .fit()
                .into(holder.profile_image);*/
    }

    @Override
    public int getItemCount() {
        return listChat.size();
    }

    public class ChatMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name, message;
        ImageView profile_image;
        ChatMessageRecyclerClick mClickListener;
        public ChatMessageViewHolder(@NonNull View itemView, ChatMessageRecyclerClick mClickListener) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            profile_image = itemView.findViewById(R.id.profile_image);

            this.mClickListener = mClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mClickListener.click(listChat.get(getAdapterPosition()));
        }
    }
}
