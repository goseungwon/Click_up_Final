package com.gsw.click_up.Fragment;

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
import com.gsw.click_up.Model.ChatroomModel;
import com.gsw.click_up.OpenChatActivity;
import com.gsw.click_up.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatWithOpenchatFragment extends Fragment {
    public static ChatWithOpenchatFragment newInstance() {
        return new ChatWithOpenchatFragment();
    }
    private ViewGroup rootView;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_chat_openchat, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.chat_open_recylerview);
        recyclerView.setAdapter(new ChatOpenAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        return rootView;
    }

    class ChatOpenAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<ChatroomModel> chatroomModels = new ArrayList<>();
        private String makeuserUID, roomTitle;

        public ChatOpenAdapter() {
            database.getReference().child("every_chat").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    chatroomModels.clear();

                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        ChatroomModel chatroomModel = item.getValue(ChatroomModel.class);
                        chatroomModels.add(chatroomModel);
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        @NonNull
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

            customViewHolder.everychat_title.setText(chatroomModels.get(position).openChat_Title);
            customViewHolder.everychat_memo.setText(chatroomModels.get(position).openChat_Memo);
            customViewHolder.everychat_hashtag.setText(chatroomModels.get(position).hashTag);
        }

        @Override
        public int getItemCount() {
            return chatroomModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView everychat_title;
            public TextView everychat_memo;
            public TextView everychat_hashtag;

            public CustomViewHolder(View view) {
                super(view);

                imageView = (ImageView) view.findViewById(R.id.everychat_imageview);
                everychat_title = (TextView) view.findViewById(R.id.everychat_title);
                everychat_memo = (TextView) view.findViewById(R.id.everychat_memo);
                everychat_hashtag = (TextView) view.findViewById(R.id.everychat_hashtag);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        makeuserUID = chatroomModels.get(getAdapterPosition()).makeUserUID;
                        roomTitle = chatroomModels.get(getAdapterPosition()).openChat_Title;

                        Intent intent = new Intent(view.getContext(), OpenChatActivity.class);
                        intent.putExtra("makeUser", makeuserUID);
                        intent.putExtra("roomtitle", roomTitle);

                        startActivity(intent);
                    }
                });
            }
        }
    }
}

