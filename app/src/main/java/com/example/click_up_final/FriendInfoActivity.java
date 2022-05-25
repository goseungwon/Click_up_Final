package com.example.click_up_final;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.click_up_final.Model.FriendRequsetDTO;
import com.example.click_up_final.Model.UserModel;
import com.example.click_up_final.Model.WriteDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendInfoActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private RecyclerView read_friend_view, read_friend_post;
    private Button btn_chatting_with_user, btn_add_friend, btn_footprint;
    private Dialog dialog;

    private List<UserModel> userDTOs = new ArrayList<>();
    private List<WriteDTO> writeDTOS = new ArrayList<>();
    private List<String> uidLists = new ArrayList<>();

    private String destinationUID;
    private String uid;
    private String destinationNick;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendinfo);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        uid = auth.getCurrentUser().getUid();

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);

        btn_chatting_with_user = (Button) findViewById(R.id.btn_chatting_with_user);
        findViewById(R.id.btn_chatting_with_user).setOnClickListener(onClickListener);

        btn_add_friend = (Button) findViewById(R.id.btn_add_friend);
        findViewById(R.id.btn_add_friend).setOnClickListener(onClickListener);

        btn_footprint = (Button) findViewById(R.id.btn_footprint);
        findViewById(R.id.btn_footprint).setOnClickListener(onClickListener);

        read_friend_view = (RecyclerView) findViewById(R.id.read_friend_view);
        read_friend_view.setLayoutManager(new LinearLayoutManager(this));
        FriendAdapter friendAdapter = new FriendAdapter();
        read_friend_view.setAdapter(friendAdapter);

        read_friend_post = (RecyclerView) findViewById(R.id.read_friend_post);
        read_friend_post.setLayoutManager(new GridLayoutManager(this, 3));
        FriendPostAdapter friendPostAdapter = new FriendPostAdapter();
        read_friend_post.setAdapter(friendPostAdapter);

        destinationUID = getIntent().getStringExtra("destinationUid");


        database.getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userDTOs.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    userDTOs.add(userModel);
                }
                friendAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference("posts").child(destinationUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                writeDTOS.clear();
                uidLists.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WriteDTO writeDTO = snapshot.getValue(WriteDTO.class);
                    writeDTOS.add(writeDTO);

                    String uidkey = snapshot.getKey();
                    uidLists.add(uidkey);
                }
                friendPostAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_chatting_with_user:
                    goChat();
                    break;
                case R.id.btn_add_friend:
                    addFriend();
                    break;
                case R.id.btn_footprint:
                    footprint();
                    break;
            }
        }
    };

    private void goChat() {
        Intent intent = new Intent(FriendInfoActivity.this, ChatActivity.class);
        destinationUID = getIntent().getStringExtra("destinationUid");

        intent.putExtra("destination_uid", destinationUID);
        startActivity(intent);
    }

    private void addFriend() {
        Button yesbtn = dialog.findViewById(R.id.btn_yes);
        Button nobtn = dialog.findViewById(R.id.btn_no);
        TextView text_dialog = dialog.findViewById(R.id.text_dialog);

        text_dialog.setText("친구 요청을 보내시겠습니까?");
        dialog.show();
        
        yesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.getReference().child("users").child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        FriendRequsetDTO friendRequsetDTO = new FriendRequsetDTO();

                        friendRequsetDTO.friends.put(destinationUID, true);
                        friendRequsetDTO.request_nickname = userModel.userNickname;
                        friendRequsetDTO.request_url = userModel.userprofileImageURL;
                        friendRequsetDTO.request_uid = userModel.userUID;

                        database.getReference().child("friends_request").child(destinationUID).push().setValue(friendRequsetDTO);

                        dialog.dismiss();
                        Toast.makeText(FriendInfoActivity.this, "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        nobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    void footprint() {
        Intent intent = new Intent(FriendInfoActivity.this, FootprintActivity.class);
        destinationUID = getIntent().getStringExtra("destinationUid");
        destinationNick = getIntent().getStringExtra("friend_nickname");

        intent.putExtra("friend__nickname", destinationNick);
        intent.putExtra("destination_uid", destinationUID);
        startActivity(intent);
    }


    class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_readfriend, parent, false);

            return new ReadFriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((ReadFriendViewHolder) holder).friend_tvNickname.setText(getIntent().getStringExtra("friend_nickname"));
            ((ReadFriendViewHolder) holder).friend_Comment.setText(getIntent().getStringExtra("friend_comment"));
            ((ReadFriendViewHolder) holder).friend_imageURL = getIntent().getStringExtra("friend_imageURL");

            Glide.with(holder.itemView.getContext()).load(((ReadFriendViewHolder) holder).friend_imageURL)
                    .into(((ReadFriendViewHolder) holder).friend_profileImageView);
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        private class ReadFriendViewHolder extends RecyclerView.ViewHolder {
            public CircleImageView friend_profileImageView;
            public String friend_imageURL;
            public TextView friend_tvNickname, friend_Comment;

            public ReadFriendViewHolder(@NonNull View view) {
                super(view);
                this.friend_profileImageView = view.findViewById(R.id.friend_profileImageView);
                this.friend_tvNickname = view.findViewById(R.id.friend_tvNickname);
                this.friend_Comment = view.findViewById(R.id.friend_Comment);
            }
        }
    }

    class FriendPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post2, parent, false);

            return new ReadFriendPostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext()).load(writeDTOS.get(position).imageURL)
                    .into(((ReadFriendPostViewHolder) holder).imageView);
        }

        @Override
        public int getItemCount() {
            return writeDTOS.size();
        }

        private class ReadFriendPostViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;

            public ReadFriendPostViewHolder(View view) {
                super(view);
                this.imageView = view.findViewById(R.id.item_post_image2);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FriendInfoActivity.this, PostActivity.class);
                        intent.putExtra("post_title", writeDTOS.get(getAdapterPosition()).title);
                        intent.putExtra("post_content", writeDTOS.get(getAdapterPosition()).content);
                        intent.putExtra("post_nick", writeDTOS.get(getAdapterPosition()).userid);
                        intent.putExtra("post_time", writeDTOS.get(getAdapterPosition()).createdAt);
                        intent.putExtra("post_lati", writeDTOS.get(getAdapterPosition()).latitude);
                        intent.putExtra("post_longi", writeDTOS.get(getAdapterPosition()).longigude);
                        intent.putExtra("post_like", writeDTOS.get(getAdapterPosition()).likecount);
                        intent.putExtra("post_image", writeDTOS.get(getAdapterPosition()).imageURL);
                        intent.putExtra("post_imagename", writeDTOS.get(getAdapterPosition()).imageName);
                        intent.putExtra("key", writeDTOS.get(getAdapterPosition()).key);
                        intent.putExtra("post_uid", writeDTOS.get(getAdapterPosition()).uid);
                        intent.putExtra("uidkey", uidLists.get(getAdapterPosition()));
                        startActivity(intent);
                    }
                });
            }
        }
    }
}
