package com.gsw.click_up;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.gsw.click_up.Model.FriendDTO;
import com.gsw.click_up.Model.FriendRequsetDTO;
import com.gsw.click_up.Model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequestFriendActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String uid;
    private Dialog dialog;
    private Toolbar toolbar;
    private RecyclerView friend_request_recyclerview;
    private List<FriendRequsetDTO> friendRequsetDTOS = new ArrayList<>();
    private List<String> uidLists = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requestfriend);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        uid = auth.getCurrentUser().getUid();

        toolbar = (Toolbar) findViewById(R.id.request_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("친구 요청");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);

        friend_request_recyclerview = (RecyclerView) findViewById(R.id.friend_request_recyclerview);
        friend_request_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        RequestAdapter requestAdapter = new RequestAdapter();
        friend_request_recyclerview.setAdapter(requestAdapter);

        database.getReference().child("friends_request").child(uid).orderByChild("friends/" + uid)
                .equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendRequsetDTOS.clear();
                uidLists.clear();

                for (DataSnapshot item : snapshot.getChildren()) {
                    UserModel userModel = item.getValue(UserModel.class);
                    FriendRequsetDTO friendRequsetDTO = item.getValue(FriendRequsetDTO.class);

                    if (userModel.friends.containsKey(uid)) {
                        friendRequsetDTOS.add(friendRequsetDTO);

                        String uidkey = item.getKey();
                        uidLists.add(uidkey);
                    }
                }
                requestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_requestfriend, parent, false);

            return new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            String f_nick = friendRequsetDTOS.get(position).request_nickname;
            String f_url = friendRequsetDTOS.get(position).request_url;
            String f_uid = friendRequsetDTOS.get(position).request_uid;
            String key = uidLists.get(position);

            ((RequestViewHolder) holder).requestitem_textView.setText(f_nick);

            Glide.with(holder.itemView.getContext()).load(f_url)
                    .apply(new RequestOptions().circleCrop())
                    .into(((RequestViewHolder) holder).requestitem_imageView);

            ((RequestViewHolder) holder).myuid = auth.getCurrentUser().getUid();
            ((RequestViewHolder) holder).friendsuid = f_uid;

            ((RequestViewHolder) holder).request_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button yesbtn = dialog.findViewById(R.id.btn_yes);
                    Button nobtn = dialog.findViewById(R.id.btn_no);
                    TextView text_dialog = dialog.findViewById(R.id.text_dialog);

                    text_dialog.setText("요청을 수락하시겠습니까?");
                    dialog.show();

                    yesbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            database.getReference().child("friends_request").child(uid)
                                    .child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.getReference().child("users").child(uid).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            UserModel userModel = snapshot.getValue(UserModel.class);
                                            FriendDTO friendDTO_my = new FriendDTO();
                                            FriendDTO friendDTO_other = new FriendDTO();

                                            friendDTO_my.friends.put(uid, true);
                                            friendDTO_my.friends.put(f_uid, true);

                                            friendDTO_my.nickname = f_nick;
                                            friendDTO_my.url = f_url;
                                            friendDTO_my.uid = f_uid;

                                            friendDTO_other.friends.put(uid, true);
                                            friendDTO_other.friends.put(f_uid, true);

                                            friendDTO_other.nickname = userModel.userNickname;
                                            friendDTO_other.url = userModel.userprofileImageURL;
                                            friendDTO_other.uid = userModel.userUID;

                                            database.getReference().child("friends_").child(uid).push().setValue(friendDTO_my);
                                            database.getReference().child("friends_").child(f_uid).push().setValue(friendDTO_other);
                                            dialog.dismiss();

                                            Toast.makeText(RequestFriendActivity.this, "요청을 수락하였습니다.", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            dialog.dismiss();
                                        }
                                    });
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
            });

            ((RequestViewHolder) holder).request_reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button yesbtn = dialog.findViewById(R.id.btn_yes);
                    Button nobtn = dialog.findViewById(R.id.btn_no);
                    TextView text_dialog = dialog.findViewById(R.id.text_dialog);

                    text_dialog.setText("요청을 거절하시겠습니까?");
                    dialog.show();

                    yesbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            database.getReference().child("friends_request").child(uid)
                                    .child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    dialog.dismiss();
                                    Toast.makeText(RequestFriendActivity.this, "요청을 거절하였습니다.", Toast.LENGTH_SHORT).show();
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
            });
        }

        @Override
        public int getItemCount() {
            return friendRequsetDTOS.size();
        }

        private class RequestViewHolder extends RecyclerView.ViewHolder {
            public ImageButton request_accept, request_reject;
            public ImageView requestitem_imageView;
            public TextView requestitem_textView;
            public String url;
            public String myuid, friendsuid;

            public RequestViewHolder(View view) {
                super(view);
                this.request_accept = view.findViewById(R.id.request_accept);
                this.request_reject = view.findViewById(R.id.request_reject);
                this.requestitem_imageView = view.findViewById(R.id.requestitem_imageView);
                this.requestitem_textView = view.findViewById(R.id.requestitem_TextView);
            }
        }
    }
}
