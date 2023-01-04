package com.gsw.click_up_final;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import androidx.drawerlayout.widget.DrawerLayout;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OpenChatActivity extends AppCompatActivity {
    private ImageButton openChat_send;
    private EditText openChat_input;
    private RecyclerView openChat_recyclerView, sideRec;
    private Toolbar open_toolbar;
    private ImageView img_menu;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String myUID;
    private String makeUserUID;
    private String openChatUID;
    private LinearLayout linearLayout;
    private Button btn_chatin, btn_nochat;
    private DrawerLayout drawerLayout;
    private View drawerView;

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
        img_menu = (ImageView) findViewById(R.id.img_menu);

        linearLayout = (LinearLayout) findViewById(R.id.bbb);
        linearLayout.bringToFront();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerView = (View) findViewById(R.id.drawerView);
        img_menu = (ImageView) findViewById(R.id.img_menu);

        sideRec = (RecyclerView) findViewById(R.id.sideRec);
        sideRec.setLayoutManager(new LinearLayoutManager(this));
        SideViewAdapter sideViewAdapter = new SideViewAdapter();
        sideRec.setAdapter(sideViewAdapter);

        img_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(drawerView);
            }
        });

        btn_chatin = (Button) findViewById(R.id.btn_chatin);
        btn_nochat = (Button) findViewById(R.id.btn_nochat);

        btn_chatin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memberRoonIn(database.getReference("every_chat").child(makeUserUID).child(openChatUID));
            }
        });

        btn_nochat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

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

                String input = openChat_input.getText().toString().trim();

                if (openChatUID == null) {
                    openChat_send.setEnabled(false);
                    database.getReference("every_chat").child(makeUserUID)
                            .push().setValue(openChatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            checkOpenChatRoom();
                        }
                    });
                } else {
                    if (input.length() > 0) {
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

                                database.getReference("every_chat").child(makeUserUID).child("messages")
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
                    } else {
                        Toast.makeText(OpenChatActivity.this, "내용을 입력하세요", Toast.LENGTH_SHORT).show();
                    }
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

    void memberRoonIn(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                OpenChatModel openChatModel = currentData.getValue(OpenChatModel.class);

                if (openChatModel == null) {
                    return Transaction.success(currentData);
                }

                if (openChatModel.members.containsKey(myUID)) {
                    linearLayout.setVisibility(View.INVISIBLE);
                } else {
                    openChatModel.count = openChatModel.count + 1;
                    openChatModel.members.put(myUID, true);
                }

                currentData.setValue(openChatModel);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                memberProfile();
            }
        });
    }

    void memberProfile() {
        database.getReference("users").child(myUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UserModel userModel = snapshot.getValue(UserModel.class);
                UserModel usermodel2 = new UserModel();

                usermodel2.userNickname = userModel.userNickname;
                usermodel2.userprofileImageURL = userModel.userprofileImageURL;
                usermodel2.userUID = userModel.userUID;

                database.getReference("every_chat").child(makeUserUID).child("member_profile")
                        .push().setValue(usermodel2);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void checkOpenChatRoom() {
        database.getReference("every_chat").child(makeUserUID).orderByChild("users/" + makeUserUID)
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

    class SideViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<UserModel> userModelList;

        public SideViewAdapter() {
            userModelList = new ArrayList<>();

            database.getReference().child("every_chat").child(makeUserUID)
                    .child("member_profile").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userModelList.clear();

                    for (DataSnapshot item : snapshot.getChildren()) {
                        UserModel userModel = item.getValue(UserModel.class);
                        userModelList.add(userModel);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_side, parent, false);

            return new SideViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            String url = userModelList.get(position).userprofileImageURL;

            ((SideViewHolder) holder).textView.setText(userModelList.get(position).userNickname);
            Glide.with(OpenChatActivity.this).load(url)
                    .apply(new RequestOptions().circleCrop()).into(((SideViewHolder) holder).imageView);

            if (userModelList.get(position).userUID.contains(myUID)) {
                linearLayout.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return userModelList.size();
        }

        private class SideViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            private TextView textView;

            public SideViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.side_image);
                textView = view.findViewById(R.id.side_nick);
            }
        }
    }

    class OpenChatReadViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<ChatroomModel.Comment> comments;
        String roomTitle;

        public OpenChatReadViewAdapter() {
            comments = new ArrayList<>();

            database.getReference("every_chat").child(makeUserUID).child("messages")
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
