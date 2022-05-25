package com.example.click_up_final.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.click_up_final.Model.UserModel;
import com.example.click_up_final.Model.WriteDTO;
import com.example.click_up_final.MyFriendActivity;
import com.example.click_up_final.PostActivity;
import com.example.click_up_final.R;
import com.example.click_up_final.SettingActivity;
import com.example.click_up_final.UserProfileChangeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyinfoFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private ViewGroup rootView;
    private RecyclerView recyclerView;
    private CircleImageView profileImage;
    private TextView tvuserNickname, userComment;
    private Button btnUserInfoChange, btnUsersFriend, btnUserSetting;

    private List<WriteDTO> writeDTOs = new ArrayList<>();
    private List<String> uidLists = new ArrayList<>();
    private FirebaseDatabase database;
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);

    private final int GALLERY_CODE = 10; // 갤러리 접근 코드
    private String imagePath;
    private FirebaseStorage storage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_myinfo, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        String userUID = firebaseAuth.getCurrentUser().getUid();

        profileImage = (CircleImageView) rootView.findViewById(R.id.profileImageView);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.myinfo_PostRecyclerView);
        tvuserNickname = (TextView) rootView.findViewById(R.id.tvuserNickname);
        userComment = (TextView) rootView.findViewById(R.id.userComment);

        btnUserInfoChange = (Button) rootView.findViewById(R.id.btnUserInfoChange);
        btnUsersFriend = (Button) rootView.findViewById(R.id.btnUsersFriend);
        btnUserSetting = (Button) rootView.findViewById(R.id.btnUserSetting);

        btnUserInfoChange.setBackgroundColor(Color.rgb(220,220,220));
        btnUsersFriend.setBackgroundColor(Color.rgb(220,220,220));
        btnUserSetting.setBackgroundColor(Color.rgb(220,220,220));

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        PostRecyclerViewAdapter postRecyclerViewAdapter = new PostRecyclerViewAdapter();
        recyclerView.setAdapter(postRecyclerViewAdapter);

        rootView.findViewById(R.id.btnUserInfoChange).setOnClickListener(onClickListener);
        rootView.findViewById(R.id.btnUsersFriend).setOnClickListener(onClickListener);
        rootView.findViewById(R.id.btnUserSetting).setOnClickListener(onClickListener);

        database.getReference("users").child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                tvuserNickname.setText(userModel.userNickname);
                userComment.setText(userModel.userComment);

                String url = userModel.userprofileImageURL;
                Glide.with(getActivity()).load(url).into(profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        database.getReference().child("posts").child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                writeDTOs.clear();
                uidLists.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WriteDTO writeDTO = snapshot.getValue(WriteDTO.class);
                    writeDTOs.add(writeDTO);

                    String uidkey = snapshot.getKey();
                    uidLists.add(uidkey);
                }
                postRecyclerViewAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return rootView;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnUserInfoChange:
                    Intent intentInfo = new Intent(getActivity(), UserProfileChangeActivity.class);
                    startActivity(intentInfo);
                    break;

                case R.id.btnUsersFriend:
                    Intent intentFriend = new Intent(getActivity(), MyFriendActivity.class);
                    startActivity(intentFriend);
                    break;

                case R.id.btnUserSetting:
                    Intent intentSetting = new Intent(getActivity(), SettingActivity.class);
                    startActivity(intentSetting);
                    break;
            }
        }
    };

    class PostRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post2, parent, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext()).load(writeDTOs.get(position).imageURL)
                    .into(((CustomViewHolder) holder).imageView);
        }

        @Override
        public int getItemCount() {
            return writeDTOs.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            private ImageView imageView;

            public CustomViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.item_post_image2);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), PostActivity.class);
                        intent.putExtra("post_title", writeDTOs.get(getAdapterPosition()).title);
                        intent.putExtra("post_content", writeDTOs.get(getAdapterPosition()).content);
                        intent.putExtra("post_nick", writeDTOs.get(getAdapterPosition()).userid);
                        intent.putExtra("post_time", writeDTOs.get(getAdapterPosition()).createdAt);
                        intent.putExtra("post_lati", writeDTOs.get(getAdapterPosition()).latitude);
                        intent.putExtra("post_longi", writeDTOs.get(getAdapterPosition()).longigude);
                        intent.putExtra("post_like", writeDTOs.get(getAdapterPosition()).likecount);
                        intent.putExtra("post_image", writeDTOs.get(getAdapterPosition()).imageURL);
                        intent.putExtra("post_imagename", writeDTOs.get(getAdapterPosition()).imageName);
                        intent.putExtra("key", writeDTOs.get(getAdapterPosition()).key);
                        intent.putExtra("post_uid", writeDTOs.get(getAdapterPosition()).uid);
                        intent.putExtra("uidkey", uidLists.get(getAdapterPosition()));
                        startActivity(intent);
                    }
                });
                WriteDTO writeDTO = new WriteDTO();
            }
        }
    }
}