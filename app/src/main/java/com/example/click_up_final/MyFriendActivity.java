package com.example.click_up_final;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.click_up_final.Model.FriendDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyFriendActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String uid;

    private RecyclerView my_friend_recyclerview;
    private Toolbar toolbar;
    private Dialog dialog;

    private List<FriendDTO> friendDTOs = new ArrayList<>();
    private List<String> uidLists = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myfriend);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        uid = auth.getCurrentUser().getUid();

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);

        my_friend_recyclerview = (RecyclerView) findViewById(R.id.my_friend_recyclerview);
        my_friend_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        MyFriendAdapter myFriendAdapter = new MyFriendAdapter();
        my_friend_recyclerview.setAdapter(myFriendAdapter);

        toolbar = (Toolbar) findViewById(R.id.myf_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("친구 목록");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database.getReference().child("friends").child(uid).orderByChild("friends/" + uid)
                .equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendDTOs.clear();
                uidLists.clear();

                for (DataSnapshot item : snapshot.getChildren()) {
                    FriendDTO friendDTO = item.getValue(FriendDTO.class);
                    friendDTOs.add(friendDTO);

                    String uidkey = item.getKey();
                    uidLists.add(uidkey);
                }

                myFriendAdapter.notifyDataSetChanged();
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

    class MyFriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_myfriends, parent, false);

            return new MyFriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            String f_nick = friendDTOs.get(position).nickname;
            String f_url = friendDTOs.get(position).url;
            String f_uid = friendDTOs.get(position).uid;
            String key = uidLists.get(position);

            ((MyFriendViewHolder)holder).myfrienditem_TextView.setText(f_nick);

            Glide.with(holder.itemView.getContext()).load(f_url)
                    .apply(new RequestOptions().circleCrop())
                    .into(((MyFriendViewHolder)holder).myfrienditem_imageView);

            ((MyFriendViewHolder)holder).friend_delete.setOnClickListener(new View.OnClickListener() {
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
                            database.getReference().child("friends").child(uid)
                                    .child(key).removeValue();

                            dialog.dismiss();
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
            return friendDTOs.size();
        }

        private class MyFriendViewHolder extends RecyclerView.ViewHolder {
            public ImageButton friend_delete;
            public TextView myfrienditem_TextView;
            public ImageView myfrienditem_imageView;

            public MyFriendViewHolder(View view) {
                super(view);
                this.friend_delete = view.findViewById(R.id.friend_delete);
                this.myfrienditem_TextView = view.findViewById(R.id.myfrienditem_TextView);
                this.myfrienditem_imageView = view.findViewById(R.id.myfrienditem_imageView);
            }
        }
    }
}
