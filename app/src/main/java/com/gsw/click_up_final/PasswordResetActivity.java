package com.gsw.click_up_final;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class PasswordResetActivity extends AppCompatActivity {
    private TextView tv_email;
    private Button btn_email;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwordreset);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        tv_email = (TextView) findViewById(R.id.tv_emailcheck);
        btn_email = (Button) findViewById(R.id.btnReset);
        tv_email.setText(auth.getCurrentUser().getEmail());


        toolbar = (Toolbar) findViewById(R.id.mail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("비밀번호 변경");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.btnReset).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnReset:
                    send_email();
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

    void send_email() {
        AlertDialog.Builder ad = new AlertDialog.Builder(PasswordResetActivity.this);
        ad.setTitle("Click Up");
        ad.setMessage("이메일을 전송하시겠습니까?");

        ad.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email = auth.getCurrentUser().getEmail();

                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(PasswordResetActivity.this,
                                            "이메일을 전송했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        ad.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        ad.show();
    }
}
