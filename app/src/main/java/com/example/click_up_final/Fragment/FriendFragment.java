package com.example.click_up_final.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.click_up_final.FriendInfoActivity;
import com.example.click_up_final.Model.UserModel;
import com.example.click_up_final.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendFragment extends Fragment {

    private RecyclerView recyclerView;
    private ViewGroup view;

    private List<UserModel> userModelList = new ArrayList<>();

    private EditText edtSearchUser;
    private ImageButton btnSearch;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    String searchName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = (ViewGroup) inflater.inflate(R.layout.fragment_friend, container, false);

        edtSearchUser = (EditText) view.findViewById(R.id.edtSearchUser);
        btnSearch = (ImageButton) view.findViewById(R.id.btnSearch);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_friend_recyclerview);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        CustomAdapter customAdapter = new CustomAdapter();
        recyclerView.setAdapter(customAdapter);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchName = edtSearchUser.getText().toString();

                userModelList.clear();
                customAdapter.notifyDataSetChanged();
            }
        });

        database.getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
            final String myUid = auth.getCurrentUser().getUid();

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                btnSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchName = edtSearchUser.getText().toString().trim();

                        userModelList.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            UserModel userModel = snapshot.getValue(UserModel.class);

                            if (userModel.userUID.equals(myUid)) {
                                continue;
                            }
                            if (userModel.userNickname.contains(searchName))
                                userModelList.add(userModel);
                        }
                        customAdapter.notifyDataSetChanged();
                    }
                });

                userModelList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    if (userModel.userUID.equals(myUid)) {
                        continue;
                    }
                    userModelList.add(userModel);
                }
                customAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FriendFragment", String.valueOf(error.toException()));
            }

        });
        return view;
    }

    class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

            return new CustomFriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            CustomFriendViewHolder customFriendViewHolder = ((CustomFriendViewHolder) holder);

            customFriendViewHolder.textView.setText(userModelList.get(position).getUserNickname());

            Glide.with(customFriendViewHolder.itemView.getContext())
                    .load(userModelList.get(position).getUserprofileImageURL())
                    .apply(new RequestOptions().circleCrop())
                    .into(customFriendViewHolder.imageView);
        }

        @Override
        public int getItemCount() {
            return userModelList.size();
        }

        public class CustomFriendViewHolder extends RecyclerView.ViewHolder {

            ImageView imageView;
            TextView textView;

            public CustomFriendViewHolder(@NonNull View itemView) {
                super(itemView);
                this.imageView = itemView.findViewById(R.id.frienditem_imageview);
                this.textView = itemView.findViewById(R.id.frienditem_textview);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
                        intent.putExtra("friend_nickname", userModelList.get(getAdapterPosition()).userNickname);
                        intent.putExtra("friend_imageURL", userModelList.get(getAdapterPosition()).userprofileImageURL);
                        intent.putExtra("friend_comment", userModelList.get(getAdapterPosition()).userComment);
                        intent.putExtra("destinationUid", userModelList.get(getAdapterPosition()).userUID);
                        startActivity(intent);


                    }
                });
            }
        }
    }
}
