package com.example.click_up.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.click_up.Model.ChatroomModel;
import com.example.click_up.OpenChatActivity;
import com.example.click_up.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EveryChatFrgment extends Fragment {
    private ViewGroup v;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private EditText edtSearch;
    private ImageButton btnSearch;
    String inputText;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = (ViewGroup) inflater.inflate(R.layout.fragment_everychatlist, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        edtSearch = (EditText) v.findViewById(R.id.edtSearchEverychat);
        btnSearch = (ImageButton) v.findViewById(R.id.btnSearchEverychat);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.chat_every_recylerview);
        recyclerView.setAdapter(new ChatAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        return v;
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1609.344;

        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<ChatroomModel> chatroomModels = new ArrayList<>();
        private String makeuserUID, roomTitle;
        private double lat1, lat2, lon1, lon2, dist_chat;

        public ChatAdapter() {
            if (getArguments() != null) {
                lat1 = getArguments().getDouble("m_latitude");
                lon1 = getArguments().getDouble("m_longitude");
            }


            database.getReference().child("every_chat").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    btnSearch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            inputText = edtSearch.getText().toString();;

                            chatroomModels.clear();

                            if (inputText.length() != 0) {

                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    ChatroomModel chatroomModel = snapshot.getValue(ChatroomModel.class);

                                    if (chatroomModel.hashTag.contains(inputText)) {
                                        chatroomModels.add(chatroomModel);
                                    }
                                    notifyDataSetChanged();
                                }
                            }

                        }
                    });

                    chatroomModels.clear();

                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        ChatroomModel chatroomModel = item.getValue(ChatroomModel.class);

                        lat2 = Double.parseDouble(chatroomModel.openChat_latitude);
                        lon2 = Double.parseDouble(chatroomModel.openChat_longitude);
                        dist_chat = distance(lat1, lon1, lat2, lon2);

                        int dist = Integer.parseInt(String.valueOf(Math.round(dist_chat)));

                        if (dist < 200) {
                            chatroomModels.add(chatroomModel);
                        }
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

            public CustomViewHolder(@NonNull View view) {
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

