package com.example.click_up;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.click_up.Model.CommentDTO;
import com.example.click_up.Model.UserModel;
import com.example.click_up.Model.WriteDTO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class PostActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private RecyclerView post_read_view, read_comment_view;
    private Toolbar post_toolbar;
    private String uid, post_user_uid;
    private String key, uidkey;
    private List<CommentDTO> commentDTOS = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_delete:
                removePost();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        uid = auth.getCurrentUser().getUid();
        key = getIntent().getStringExtra("key");
        post_user_uid = getIntent().getStringExtra("post_uid");
        uidkey = getIntent().getStringExtra("uidkey");

        post_read_view = (RecyclerView) findViewById(R.id.read_post_view);
        post_read_view.setLayoutManager(new LinearLayoutManager(this));
        ReadAdapter readAdapter = new ReadAdapter();
        post_read_view.setAdapter(readAdapter);

        read_comment_view = (RecyclerView) findViewById(R.id.read_comment_view);
        read_comment_view.setLayoutManager(new LinearLayoutManager(this));
        CommentAdapter commentAdapter = new CommentAdapter();
        read_comment_view.setAdapter(commentAdapter);

        post_toolbar = (Toolbar) findViewById(R.id.post_toolbar);
        setSupportActionBar(post_toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database.getReference().child("post_comments").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentDTOS.clear();

                for (DataSnapshot item : snapshot.getChildren()) {
                    CommentDTO commentDTO = item.getValue(CommentDTO.class);
                    commentDTOS.add(commentDTO);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removePost() {
        String key = getIntent().getStringExtra("key");
        String postuser_uid = getIntent().getStringExtra("post_uid");

        if (postuser_uid.contentEquals(uid)) {

            finish();

            database.getReference().child("posts").child(uid).child(uidkey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(PostActivity.this, "삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, "실패", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(PostActivity.this, "자신의 게시물만 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    class ReadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<WriteDTO> writeDTOS = new ArrayList<>();

        public ReadAdapter() {
            database.getReference("posts").child(post_user_uid).child(uidkey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    writeDTOS.clear();

                    WriteDTO writeDTO = snapshot.getValue(WriteDTO.class);
                    writeDTOS.add(writeDTO);

                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_readpost, parent, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((CustomViewHolder) holder).post_content.setText(getIntent().getStringExtra("post_content"));
            ((CustomViewHolder) holder).post_nick.setText(getIntent().getStringExtra("post_nick"));
            ((CustomViewHolder) holder).post_time.setText(getIntent().getStringExtra("post_time"));
            ((CustomViewHolder) holder).post_imageURL = getIntent().getStringExtra("post_image");

            getSupportActionBar().setTitle(getIntent().getStringExtra("post_title"));

            Glide.with(holder.itemView.getContext()).load(((CustomViewHolder) holder).post_imageURL)
                    .into(((CustomViewHolder) holder).post_imageView);

            ((CustomViewHolder) holder).btn_comment_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    database.getReference().child("users").child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = snapshot.getValue(UserModel.class);

                            String nick = userModel.userNickname;
                            String url = userModel.userprofileImageURL;

                            CommentDTO comments = new CommentDTO();
                            comments.nick = nick;
                            comments.url = url;
                            comments.comments = ((CustomViewHolder) holder).edt_comment.getText().toString();

                            if (comments.comments.length() == 0) {
                                Toast.makeText(PostActivity.this, "댓글을 입력해 주세요", Toast.LENGTH_SHORT).show();
                            } else {
                                database.getReference().child("post_comments").child(key).push().setValue(comments)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                ((CustomViewHolder) holder).edt_comment.setText("");
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                }
            });

            ((CustomViewHolder) holder).post_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postLike(database.getReference("posts").child(post_user_uid).child(uidkey));
                }
            });

            if (writeDTOS.get(position).likemembers.containsKey(uid)) {
                ((CustomViewHolder) holder).post_like.setImageResource(R.drawable.heart_full);
            } else {
                ((CustomViewHolder) holder).post_like.setImageResource(R.drawable.heart_blank);
            }


            ((CustomViewHolder) holder).post_location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent PostIntent = new Intent(getApplication(), PostMapActivity.class);

                    String lati = getIntent().getStringExtra("post_lati");
                    String longi = getIntent().getStringExtra("post_longi");

                    PostIntent.putExtra("latitude_popup", lati);
                    PostIntent.putExtra("longitude_popup", longi);

                    startActivity(PostIntent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        void postLike(DatabaseReference postRef) {
            postRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    WriteDTO writeDTO = currentData.getValue(WriteDTO.class);

                    if (writeDTO == null) {
                        return Transaction.success(currentData);
                    }

                    if (writeDTO.likemembers.containsKey(uid)) {
                        writeDTO.likecount = writeDTO.likecount - 1;
                        writeDTO.likemembers.remove(uid);
                    } else {
                        writeDTO.likecount = writeDTO.likecount + 1;
                        writeDTO.likemembers.put(uid, true);
                    }

                    currentData.setValue(writeDTO);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                }
            });
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public TextView post_content, post_nick, post_time;
            public ImageView post_imageView, post_like;
            private ImageButton post_location;
            private String post_imageURL;
            private LinearLayout linear_post_comment;
            private EditText edt_comment;
            private Button btn_comment_send;
            public Toolbar post_toolbar;

            public CustomViewHolder(View view) {
                super(view);
                this.post_content = view.findViewById(R.id.post_read_content);
                this.post_nick = view.findViewById(R.id.post_read_nick);
                this.post_time = view.findViewById(R.id.post_read_time);
                this.post_imageView = view.findViewById(R.id.post_read_imageView);
                this.post_like = (ImageView) view.findViewById(R.id.post_read_like);
                this.post_location = view.findViewById(R.id.post_read_loaction);
                this.edt_comment = view.findViewById(R.id.edt_inputComment);
                this.btn_comment_send = view.findViewById(R.id.btn_comment_send);
                post_toolbar = (Toolbar) view.findViewById(R.id.post_toolbar);
            }
        }
    }

    class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comments, parent, false);

            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((CommentViewHolder) holder).text_nick.setText(commentDTOS.get(position).nick);
            ((CommentViewHolder) holder).text_comment.setText(commentDTOS.get(position).comments);

            Glide.with(holder.itemView.getContext()).load(commentDTOS.get(position).url)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CommentViewHolder) holder).imageView);
        }

        @Override
        public int getItemCount() {
            return commentDTOS.size();
        }

        private class CommentViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            private TextView text_nick, text_comment;

            public CommentViewHolder(View view) {
                super(view);
                this.imageView = view.findViewById(R.id.item_comment_image);
                this.text_nick = view.findViewById(R.id.item_comment_nick);
                this.text_comment = view.findViewById(R.id.item_comment_comment);
            }
        }
    }
}
