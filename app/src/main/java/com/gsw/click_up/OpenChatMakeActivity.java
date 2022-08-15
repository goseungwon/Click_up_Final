package com.gsw.click_up;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gsw.click_up.Model.ChatroomModel;
import com.gsw.click_up.Model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OpenChatMakeActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private EditText edtOpenchat_title, edtOpenchat_memo, edtOpenchat_hashtag, edtOpenchat_people;
    private Button btnOpenchat_make;
    private RadioGroup rgChat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makeopenchat);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        btnOpenchat_make = (Button) findViewById(R.id.btnOpenchat_make);
        edtOpenchat_title = (EditText) findViewById(R.id.edtOpenchat_title);
        edtOpenchat_memo = (EditText) findViewById(R.id.edtOpenchat_memo);
        edtOpenchat_hashtag = (EditText) findViewById(R.id.edtOpenchat_hashtag);
        edtOpenchat_people = (EditText) findViewById(R.id.edtOpenchat_people);
        rgChat = (RadioGroup) findViewById(R.id.rgChat);

        findViewById(R.id.btnOpenchat_make).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnOpenchat_make:
                    makeopenchat();
                    break;
            }
        }
    };

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeopenchat() {
        String userUID = auth.getCurrentUser().getUid();

        database.getReference().child("users").child(userUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);

                Long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat createdAt = new SimpleDateFormat("yy-MM-dd, hh:mm aaa");

                Intent thirdintent = getIntent();
                String open_latitude = String.valueOf(thirdintent.getDoubleExtra("my_lati", 0));
                String open_longitude = String.valueOf(thirdintent.getDoubleExtra("my_longi", 0));

                int rbid = rgChat.getCheckedRadioButtonId();
                RadioButton rb = (RadioButton) findViewById(rbid);
                String classify = rb.getText().toString();

                ChatroomModel chatroomModel = new ChatroomModel();
                chatroomModel.openChat_Title = edtOpenchat_title.getText().toString();
                chatroomModel.openChat_Memo = edtOpenchat_memo.getText().toString();
                chatroomModel.openChat_latitude = open_latitude;
                chatroomModel.openChat_longitude = open_longitude;
                chatroomModel.openChat_createdAt = createdAt.format(date);
                chatroomModel.makeUserImage = userModel.userprofileImageURL;
                chatroomModel.makeUserNickname = userModel.userNickname;
                chatroomModel.makeUserUID = userModel.userUID;
                chatroomModel.peopleCount = Integer.parseInt(String.valueOf(edtOpenchat_people.getText()));
                chatroomModel.hashTag = edtOpenchat_hashtag.getText().toString();
                chatroomModel.classify = classify;

                if (classify.equals("  프렌드챗")) {
                    database.getReference().child("friend_chat").child(userUID).setValue(chatroomModel);
                    Toast.makeText(OpenChatMakeActivity.this, "프렌드챗 개설", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    database.getReference().child("every_chat").child(userUID).setValue(chatroomModel);
                    Toast.makeText(OpenChatMakeActivity.this, "에브리챗 개설", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
