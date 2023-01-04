package com.gsw.click_up_final;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gsw.click_up_final.Model.ChatroomModel;
import com.gsw.click_up_final.Model.OpenChatModel;
import com.gsw.click_up_final.Model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendChatActivity extends AppCompatActivity {
    private ImageButton friendChat_send;
    private EditText friendChat_input;
    private RecyclerView friendChat_recyclerView;
    private Toolbar friendChat_toolbar;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String myUID;
    private String makeUserUID;
    private String friendChatUID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendchat);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        myUID = auth.getCurrentUser().getUid();
        makeUserUID = getIntent().getStringExtra("makeUser");

        friendChat_toolbar = (Toolbar) findViewById(R.id.friendChat_toolbar);
        setSupportActionBar(friendChat_toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        friendChat_send = (ImageButton) findViewById(R.id.friendChat_send);
        friendChat_input = (EditText) findViewById(R.id.friendChat_input);
        friendChat_recyclerView = (RecyclerView) findViewById(R.id.friendChat_recyclerView);

        friendChat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenChatModel openChatModel = new OpenChatModel();
                openChatModel.users.put(myUID, true);

                String input = friendChat_input.getText().toString().trim();

                if (friendChatUID == null) {
                    friendChat_send.setEnabled(false);
                    database.getReference("friend_chat").child(makeUserUID)
                            .push().setValue(openChatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            checkFriendChatRoom();
                        }
                    });
                } else {
                    if (input.length() > 0) {
                        database.getReference("users").child(myUID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                UserModel userModel = snapshot.getValue(UserModel.class);

                                String nick = userModel.userNickname;
                                String url = userModel.userprofileImageURL;

                                OpenChatModel.Comment comment = new OpenChatModel.Comment();

                                comment.uid = myUID;
                                comment.message = friendChat_input.getText().toString();
                                comment.nick = nick;
                                comment.url = url;

                                database.getReference("friend_chat").child(makeUserUID).child("messages")
                                        .push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        friendChat_input.setText("");
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        Toast.makeText(FriendChatActivity.this, "내용을 입력하세요", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        checkFriendChatRoom();
    }

    void checkFriendChatRoom() {
        database.getReference("friend_chat").child(makeUserUID).orderByChild("users/" + makeUserUID)
                .equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    ChatroomModel chatroomModel = item.getValue(ChatroomModel.class);

                    if (chatroomModel.users.containsKey(makeUserUID)) {
                        friendChatUID = item.getKey();
                        friendChat_send.setEnabled(true);
                        friendChat_recyclerView.setLayoutManager(new LinearLayoutManager(FriendChatActivity.this));
                        friendChat_recyclerView.setAdapter(new FriendChatReadViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    class FriendChatReadViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<ChatroomModel.Comment> comments;
        String roomTitle;

        public FriendChatReadViewAdapter() {
            comments = new ArrayList<>();

            database.getReference("friend_chat").child(makeUserUID).child("messages")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            comments.clear();

                            for (DataSnapshot item : snapshot.getChildren()) {
                                ChatroomModel.Comment comment = item.getValue(ChatroomModel.Comment.class);
                                comments.add(comment);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

            return new FriendChatReadViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            FriendChatReadViewHolder friendChatReadViewHolder = ((FriendChatReadViewHolder) holder);

            roomTitle = getIntent().getStringExtra("roomtitle");
            getSupportActionBar().setTitle(roomTitle);

            if (comments.get(position).uid.equals(myUID)) {
                friendChatReadViewHolder.tv_message.setBackgroundResource(R.drawable.rightbubble);
                friendChatReadViewHolder.tv_message.setText(comments.get(position).message);
                friendChatReadViewHolder.tv_message.setTextSize(16);
                friendChatReadViewHolder.linearLayout_others_message.setVisibility(View.INVISIBLE);
                friendChatReadViewHolder.linearlayout_my_message.setGravity(Gravity.RIGHT);
            } else {
                friendChatReadViewHolder.tv_message.setBackgroundResource(R.drawable.leftbubble);
                friendChatReadViewHolder.tv_message.setText(comments.get(position).message);
                friendChatReadViewHolder.tv_message.setTextSize(16);
                friendChatReadViewHolder.linearLayout_others_message.setVisibility(View.VISIBLE);
                friendChatReadViewHolder.linearlayout_my_message.setGravity(Gravity.LEFT);
                friendChatReadViewHolder.tv_name.setText(comments.get(position).nick);
                Glide.with(holder.itemView.getContext()).load(comments.get(position).url)
                        .apply(new RequestOptions().circleCrop()).into(friendChatReadViewHolder.iv_user);
            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class FriendChatReadViewHolder extends RecyclerView.ViewHolder {
            public TextView tv_message;
            public TextView tv_name;
            public ImageView iv_user;
            public LinearLayout linearLayout_others_message;
            public LinearLayout linearlayout_my_message;
            public Toolbar toolbar;

            public FriendChatReadViewHolder(@NonNull View view) {
                super(view);
                tv_message = (TextView) view.findViewById(R.id.messageItem_message);
                tv_name = (TextView) view.findViewById(R.id.messageItem_textview_name);
                iv_user = (ImageView) view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_others_message = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_destination);
                linearlayout_my_message = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_main);
                toolbar = (Toolbar) view.findViewById(R.id.friendChat_toolbar);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
