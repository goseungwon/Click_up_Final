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
import com.example.click_up_final.FriendChatActivity;
import com.example.click_up_final.Model.ChatroomModel;
import com.example.click_up_final.Model.FriendDTO;
import com.example.click_up_final.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatSecondFragment extends Fragment {
    private ViewGroup rootView;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String uid;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_chat_second, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        uid = auth.getCurrentUser().getUid();

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.chat_second_recylerview);
        recyclerView.setAdapter(new ChatSecondAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        return rootView;
    }

    class ChatSecondAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<ChatroomModel> chatroomModels = new ArrayList<>();
        private List<FriendDTO> friendDTOList = new ArrayList<>();
        private String makeuserUID, roomTitle;

        public ChatSecondAdapter() {
            database.getReference("friend_chat").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatroomModels.clear();

                    for (DataSnapshot item : snapshot.getChildren()) {
                        chatroomModels.add(item.getValue(ChatroomModel.class));
                    }

                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_everychat, parent, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final CustomViewHolder customViewHolder = ((CustomViewHolder) holder);

            Glide.with(customViewHolder.itemView.getContext())
                    .load(chatroomModels.get(position).makeUserImage)
                    .apply(new RequestOptions().circleCrop())
                    .into(customViewHolder.imageView);


            customViewHolder.friendchat_title.setText(chatroomModels.get(position).openChat_Title);
            customViewHolder.friendchat_memo.setText(chatroomModels.get(position).openChat_Memo);
            customViewHolder.friendchat_hashtag.setText(chatroomModels.get(position).hashTag);
        }

        @Override
        public int getItemCount() {
            return chatroomModels.size();
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView friendchat_title;
            public TextView friendchat_memo;
            public TextView friendchat_hashtag;

            public CustomViewHolder(@NonNull View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.everychat_imageview);
                friendchat_title = (TextView) view.findViewById(R.id.everychat_title);
                friendchat_memo = (TextView) view.findViewById(R.id.everychat_memo);
                friendchat_hashtag = (TextView) view.findViewById(R.id.everychat_hashtag);


                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        makeuserUID = chatroomModels.get(getAdapterPosition()).makeUserUID;
                        roomTitle = chatroomModels.get(getAdapterPosition()).openChat_Title;

                        Intent intent = new Intent(view.getContext(), FriendChatActivity.class);
                        intent.putExtra("makeUser", makeuserUID);
                        intent.putExtra("roomtitle", roomTitle);

                        startActivity(intent);
                    }
                });
            }
        }
    }
}
