package com.example.click_up_final;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.click_up_final.Model.ChatModel;
import com.example.click_up_final.Model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private String destinationUid;
    private ImageButton btn_message_send;
    private EditText edt_inputMsg;
    private RecyclerView recyclerView_chatting;
    private Toolbar my_toolbar;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private String uid;
    private String chatRoomUid;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    int peopleCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        uid = auth.getCurrentUser().getUid();
        destinationUid = getIntent().getStringExtra("destination_uid");
        btn_message_send = (ImageButton) findViewById(R.id.btn_message_send);
        edt_inputMsg = (EditText) findViewById(R.id.edt_inputMsg);

        my_toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView_chatting = (RecyclerView) findViewById(R.id.recyclerView_chatting);

        btn_message_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel chatModel = new ChatModel();
                chatModel.users.put(uid, true);
                chatModel.users.put(destinationUid, true);

                if (chatRoomUid == null) {
                    btn_message_send.setEnabled(false);
                    database.getReference().child("chatrooms")
                            .push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            checkChatRoom();
                        }
                    });
                } else {
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = edt_inputMsg.getText().toString();
                    database.getReference().child("chatrooms").child(chatRoomUid).child("comments")
                            .push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            edt_inputMsg.setText("");
                        }
                    });
                }
            }
        });
        checkChatRoom();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    void checkChatRoom() {
        database.getReference().child("chatrooms").orderByChild("users/" + uid)
                .equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {

                    ChatModel chatModel = item.getValue(ChatModel.class);

                    if (chatModel.users.containsKey(destinationUid)) {
                        chatRoomUid = item.getKey();
                        btn_message_send.setEnabled(true);
                        recyclerView_chatting.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                        recyclerView_chatting.setAdapter(new RecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ChatModel.Comment> comments;
        UserModel userModel;

        public RecyclerViewAdapter() {
            comments = new ArrayList<>();

            database.getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userModel = dataSnapshot.getValue(UserModel.class);
                    getMessageList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        void getMessageList() {
            databaseReference = database.getReference().child("chatrooms").child(chatRoomUid).child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();
                    Map<String, Object> readUsersMap = new HashMap<>();

                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_read = item.getValue(ChatModel.Comment.class);
                        comment_read.readUsers.put(uid, true);

                        readUsersMap.put(key, comment_read);
                        comments.add(comment_origin);
                    }

                    if (comments.get(comments.size() - 1).readUsers.containsKey(uid)) {

                        database.getReference().child("chatrooms").child(chatRoomUid).child("comments")
                                .updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                notifyDataSetChanged();
                                recyclerView_chatting.scrollToPosition(comments.size() - 1);
                            }
                        });
                    } else {
                        notifyDataSetChanged();
                        recyclerView_chatting.scrollToPosition(comments.size() - 1);
                    }
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

            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder) holder);
            getSupportActionBar().setTitle(userModel.userNickname);

            if (comments.get(position).uid.equals(uid)) {
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
                messageViewHolder.textView_message.setTextSize(16);
                messageViewHolder.linearlayout_main.setGravity(Gravity.RIGHT);
                setReadCounter(position, messageViewHolder.textView_readCounter);
            } else {
                Glide.with(holder.itemView.getContext()).load(userModel.userprofileImageURL)
                        .apply(new RequestOptions().circleCrop()).into(messageViewHolder.imageView_profile);
                messageViewHolder.textView_name.setText(userModel.userNickname);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setTextSize(16);
                messageViewHolder.linearlayout_main.setGravity(Gravity.LEFT);
                setReadCounter(position, messageViewHolder.textView_readCounter);
            }
        }

        void setReadCounter(final int position, TextView textView) {
            if (peopleCount == 0) {
                database.getReference().child("chatrooms").child(chatRoomUid).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();

                        peopleCount = users.size() - 1;
                        int count = peopleCount - comments.get(position).readUsers.size();

                        if (count > 0) {
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(String.valueOf(count));
                        } else {
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            } else {
                int count = peopleCount - comments.get(position).readUsers.size();

                if (count > 0) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(String.valueOf(count));
                } else {
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearlayout_main;
            public TextView textView_readCounter;

            public Toolbar my_toolbar;

            public MessageViewHolder(View view) {
                super(view);
                textView_message = (TextView) view.findViewById(R.id.messageItem_message);
                textView_name = (TextView) view.findViewById(R.id.messageItem_textview_name);
                imageView_profile = (ImageView) view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_destination);
                linearlayout_main = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_main);
                textView_readCounter = (TextView) view.findViewById(R.id.messageItem_readCounter);
                my_toolbar = (Toolbar) view.findViewById(R.id.my_toolbar);
            }
        }
    }

    @Override
    public void onBackPressed() {
        databaseReference.removeEventListener(valueEventListener);
        finish();
    }
}
