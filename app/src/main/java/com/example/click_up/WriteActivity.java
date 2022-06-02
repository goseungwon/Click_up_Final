package com.example.click_up;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;
import androidx.appcompat.widget.Toolbar;

import com.example.click_up.Model.UserModel;
import com.example.click_up.Model.WriteDTO;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WriteActivity extends AppCompatActivity {
    private final int GALLERY_CODE = 10;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private ImageView uploadImg;
    private EditText edtTitle, edtContent;
    private Button btnPost;
    private Toolbar toolbar;
    private String imagePath;
    private int CAPTURE_IMAGE =2;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        uploadImg = (ImageView)findViewById(R.id.UploadImg);
        edtTitle = (EditText)findViewById(R.id.edtTitle);
        edtContent = (EditText)findViewById(R.id.edtContent);
        btnPost = (Button)findViewById(R.id.btnPost);

        toolbar = (Toolbar) findViewById(R.id.write_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("게시물 작성");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.UploadImg).setOnClickListener(onClickListener);
        findViewById(R.id.btnPost).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.UploadImg:
                    loadGallery();
                    break;
                case R.id.btnPost:
                    upload(imagePath);
                    break;
            }
        }
    };

    private void loadGallery() {
        final CharSequence[] PhotoModels = {"갤러리", "카메라"};
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("사진 업로드");

        ad.setItems(PhotoModels, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0 ){
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    startActivityForResult(intent, GALLERY_CODE);
                }else{
                    takePictureFromCameraIntent();
                    Log.d("camera", "1번");
                }
            }
        });
        ad.show();
    }

    private void takePictureFromCameraIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.d("camera", "2번");

        if (takePictureIntent.resolveActivity(getPackageManager()) !=null){
            File f = null;
            try{
                f = createImageFile();
            }catch (IOException ex){
                Log.d("camera", "카메라 파일생성 오류");
            }
            if (f != null){
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.click_up_final.fileprovider", f);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE);
            }
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, ".jpg", storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE) {
            imagePath = getPath(data.getData());
            File f = new File(getPath(data.getData()));
            uploadImg.setImageURI(Uri.fromFile(f));
        }

        if(requestCode == CAPTURE_IMAGE){
            File f = new File(mCurrentPhotoPath);
            imagePath = mCurrentPhotoPath;
            uploadImg.setImageURI(Uri.fromFile(f));
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
        final StorageReference riversRef = storageRef.child("postImages/" + file.getLastPathSegment());
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
                    String userUID = auth.getCurrentUser().getUid();

                    Long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat createdAt = new SimpleDateFormat("yy/MM/dd, hh:mm aaa");

                    database.getReference("users").child(userUID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = snapshot.getValue(UserModel.class);

                            String nickname = userModel.userNickname;
                            Uri downloadUri = task.getResult();

                            WriteDTO writeDTO = new WriteDTO();
                            writeDTO.imageURL = downloadUri.toString();
                            writeDTO.title = edtTitle.getText().toString();
                            writeDTO.content = edtContent.getText().toString();
                            writeDTO.uid = auth.getCurrentUser().getUid();
                            writeDTO.userid = nickname;
                            writeDTO.latitude = String.valueOf(getIntent().getDoubleExtra("latitude", 0));
                            writeDTO.longigude = String.valueOf(getIntent().getDoubleExtra("longitude", 0));
                            writeDTO.likecount = 0;
                            writeDTO.createdAt = createdAt.format(date);

                            String key = database.getReference().child("posts").child(userUID).push().getKey();
                            writeDTO.key = key;

                            database.getReference().child("posts").child(userUID).push().setValue(writeDTO);
                            Toast.makeText(WriteActivity.this, "게시물이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    Toast.makeText(WriteActivity.this, "게시물이 등록되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}