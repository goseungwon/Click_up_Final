package com.example.click_up_final.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.click_up_final.ChatActivity;
import com.example.click_up_final.Model.ChatModel;
import com.example.click_up_final.Model.UserModel;
import com.example.click_up_final.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChatWithFriendFragment extends Fragment {
    public static ChatWithFriendFragment newInstance(){
        return new ChatWithFriendFragment();
    }

    private ViewGroup rootView;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_chat_friend, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.chat_friend_recylerview);
        recyclerView.setAdapter(new ChatFriendAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        return rootView;
    }

    class ChatFriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<ChatModel> chatModels = new ArrayList<>();
        private String uid;
        private ArrayList<String> destinationUsers = new ArrayList<>();

        public ChatFriendAdapter() {
            uid = auth.getCurrentUser().getUid();

            database.getReference().child("chatrooms").orderByChild("users/" + uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    chatModels.clear();

                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        chatModels.add(item.getValue(ChatModel.class));
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // 실패
                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final CustomViewHolder customViewHolder = ((CustomViewHolder)holder);

            String destinationUid = null;

            // 채팅방에 있는 유저를 일일히 체크
            for(String user : chatModels.get(position).users.keySet()) {
                if (!user.equals(uid)) {
                    destinationUid = user;
                    destinationUsers.add(destinationUid);
                }
            }
            database.getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);

                    Glide.with(customViewHolder.itemView.getContext())
                            .load(userModel.userprofileImageURL)
                            .apply(new RequestOptions().circleCrop())
                            .into(customViewHolder.imageView);

                    customViewHolder.textView_title.setText(userModel.userNickname);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            // 메시지를 내림차순으로 정렬 후 마지막 메시지의 키값을 가져옴
            Map<String,ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
            commentMap.putAll(chatModels.get(position).comments);
            String lastMessageKey = (String) commentMap.keySet().toArray()[0];
            customViewHolder.textView_last_message.setText(chatModels.get(position).comments.get(lastMessageKey).message);

            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ChatActivity.class);
                    intent.putExtra("destination_uid", destinationUsers.get(holder.getAdapterPosition()));

                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView textView_title;
            public TextView textView_last_message;

            public CustomViewHolder(View view) {
                super(view);

                imageView = (ImageView) view.findViewById(R.id.chatitem_imageview);
                textView_title = (TextView) view.findViewById(R.id.chatitem_textview_title);
                textView_last_message = (TextView) view.findViewById(R.id.chatitem_textview_lastMessage);
            }
        }
    }
}
