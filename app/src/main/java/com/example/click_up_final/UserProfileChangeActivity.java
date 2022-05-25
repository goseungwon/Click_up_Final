package com.example.click_up_final;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.content.CursorLoader;

import com.example.click_up_final.Model.UserModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class UserProfileChangeActivity extends AppCompatActivity {
    private final int GALLERY_CODE = 10;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseDatabase database;

    private ImageView imvChangeUserProfile;
    private EditText edtUserComment;
    private Button btnProfileChage;
    private Toolbar toolbar;

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfochange);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        imvChangeUserProfile = (ImageView) findViewById(R.id.imvChangeUserProfile);
        edtUserComment = (EditText) findViewById(R.id.edtUserComment);
        btnProfileChage = (Button) findViewById(R.id.btnProfileChage);

        toolbar = (Toolbar) findViewById(R.id.change_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("프로필 변경");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.imvChangeUserProfile).setOnClickListener(onClickListener);
        findViewById(R.id.btnProfileChage).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imvChangeUserProfile:
                    loadGallery();
                    break;
                case R.id.btnProfileChage:
                    upload(imagePath);
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

    private void loadGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, GALLERY_CODE);
    }

    public void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE) {

            imagePath = getPath(data.getData());
            File f = new File(getPath(data.getData()));
            imvChangeUserProfile.setImageURI(Uri.fromFile(f));

        }
    }

    public String getPath(Uri uri) {
        String [] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
    }

    private void upload(String uri) {
        final StorageReference storageRef = storage.getReferenceFromUrl("gs://clickup-87b0d.appspot.com");

        Uri file = Uri.fromFile(new File(uri));
        final StorageReference riversRef = storageRef.child("userProfileImages/" + file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {

                    Uri downloadUri = task.getResult();
                    String userUID = auth.getCurrentUser().getUid();

                    database.getReference("users").child(userUID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = new UserModel();
                            UserModel userModel2 = snapshot.getValue(UserModel.class);

                            userModel.userprofileImageURL = downloadUri.toString();
                            userModel.userComment = edtUserComment.getText().toString();
                            userModel.userNickname = userModel2.userNickname;
                            userModel.userUID = userUID;
                            userModel.userName = userModel2.userName;

                            database.getReference().child("users").child(userUID).setValue(userModel);
                            Toast.makeText(UserProfileChangeActivity.this, "프로필이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else {
                    Toast.makeText(UserProfileChangeActivity.this, "포르필이 변경되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
