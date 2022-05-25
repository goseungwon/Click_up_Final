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
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.click_up_final.Model.ChatroomModel;
import com.example.click_up_final.Model.OpenChatModel;
import com.example.click_up_final.Model.UserModel;
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

public class OpenChatActivity extends AppCompatActivity {
    private ImageButton openChat_send;
    private EditText openChat_input;
    private RecyclerView openChat_recyclerView;
    private Toolbar open_toolbar;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private String myUID;
    private String makeUserUID;
    private String openChatUID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_openchat);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        myUID = auth.getCurrentUser().getUid();
        makeUserUID = getIntent().getStringExtra("makeUser");

        openChat_send = (ImageButton) findViewById(R.id.openChat_send);
        openChat_input = (EditText) findViewById(R.id.openChat_input);

        openChat_recyclerView = (RecyclerView) findViewById(R.id.openChat_recyclerView);

        open_toolbar = (Toolbar) findViewById(R.id.openChat_toolbar);
        setSupportActionBar(open_toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        openChat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenChatModel openChatModel = new OpenChatModel();
                openChatModel.users.put(myUID, true);

                if (openChatUID == null) {
                    openChat_send.setEnabled(false);
                    database.getReference("openchat").child(makeUserUID)
                            .push().setValue(openChatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) { checkOpenChatRoom(); }
                    });
                } else {
                    database.getReference().child("users").child(myUID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModelk = snapshot.getValue(UserModel.class);

                            String nick = userModelk.userNickname;
                            String url = userModelk.userprofileImageURL;

                            OpenChatModel.Comment comment = new OpenChatModel.Comment();

                            comment.uid = myUID;
                            comment.message = openChat_input.getText().toString();
                            comment.nick = nick;
                            comment.url = url;

                            database.getReference("openchat").child(makeUserUID).child(openChatUID).child("messages")
                                    .push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    openChat_input.setText("");
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        });
        checkOpenChatRoom();
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

    void checkOpenChatRoom() {
        database.getReference("openchat").child(makeUserUID).orderByChild("users/" + makeUserUID)
                .equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {

                    ChatroomModel chatroomModel = item.getValue(ChatroomModel.class);

                    if (chatroomModel.users.containsKey(makeUserUID)) {
                        openChatUID = item.getKey();
                        openChat_send.setEnabled(true);
                        openChat_recyclerView.setLayoutManager(new LinearLayoutManager(OpenChatActivity.this));
                        openChat_recyclerView.setAdapter(new OpenChatReadViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    class OpenChatReadViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ChatroomModel.Comment> comments;
        UserModel userModel;
        String roomTitle;

        public OpenChatReadViewAdapter() {
            comments = new ArrayList<>();

            database.getReference("openchat").child(makeUserUID).child(openChatUID).child("messages")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            comments.clear();

                            for (DataSnapshot item : snapshot.getChildren()) {
                                ChatroomModel.Comment comment = item.getValue(ChatroomModel.Comment.class);
                                comments.add(comment);
                            }

                            notifyDataSetChanged();
                            // openChat_recyclerView.scrollToPosition(comments.size() - 1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);

            return new OpenChatReadViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            OpenChatReadViewHolder openChatReadViewHolder = ((OpenChatReadViewHolder) holder);

            roomTitle = getIntent().getStringExtra("roomtitle");
            getSupportActionBar().setTitle(roomTitle);

            if (comments.get(position).uid.equals(myUID)) {
                openChatReadViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);
                openChatReadViewHolder.textView_message.setText(comments.get(position).message);
                openChatReadViewHolder.linearLayout_others_message.setVisibility(View.INVISIBLE);
                openChatReadViewHolder.textView_message.setTextSize(16);
                openChatReadViewHolder.linearlayout_my_message.setGravity(Gravity.RIGHT);
            } else {
                openChatReadViewHolder.textView_message.setText(comments.get(position).message);
                openChatReadViewHolder.textView_message.setTextSize(16);
                openChatReadViewHolder.linearLayout_others_message.setVisibility(View.VISIBLE);
                openChatReadViewHolder.linearlayout_my_message.setGravity(Gravity.LEFT);
                openChatReadViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);


                openChatReadViewHolder.textView_name.setText(comments.get(position).nick);
                Glide.with(holder.itemView.getContext()).load(comments.get(position).url)
                        .apply(new RequestOptions().circleCrop()).into(openChatReadViewHolder.imageView_profile);

            }

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class OpenChatReadViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_others_message;
            public LinearLayout linearlayout_my_message;
            public Toolbar toolbar;

            public OpenChatReadViewHolder(View view) {
                super(view);
                textView_message = (TextView) view.findViewById(R.id.messageItem_message);
                textView_name = (TextView) view.findViewById(R.id.messageItem_textview_name);
                imageView_profile = (ImageView) view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_others_message = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_destination);
                linearlayout_my_message = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_main);
                toolbar = (Toolbar) view.findViewById(R.id.openChat_toolbar);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
