package com.example.click_up_final;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

public class SettingActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private Button setting_logout, friend_request, password_reset, ask_btn;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        auth = FirebaseAuth.getInstance();
        setting_logout = (Button) findViewById(R.id.setting_logout);
        friend_request = (Button) findViewById(R.id.friend_request);
        password_reset = (Button) findViewById(R.id.password_reset);
        ask_btn = (Button) findViewById(R.id.ask_btn);

        toolbar = (Toolbar) findViewById(R.id.set_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("설정");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.setting_logout).setOnClickListener(onClickListener);
        findViewById(R.id.friend_request).setOnClickListener(onClickListener);
        findViewById(R.id.password_reset).setOnClickListener(onClickListener);
        findViewById(R.id.ask_btn).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.setting_logout:
                    logout();
                    break;

                case R.id.friend_request:
                    request();
                    break;

                case R.id.password_reset:
                    password_reset();
                    break;

                case R.id.ask_btn:
                    user_ask();
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

    void logout() {
        AlertDialog.Builder ad = new AlertDialog.Builder(SettingActivity.this);
        ad.setTitle("Click Up");
        ad.setMessage("로그아웃 하시겠습니까?");

        ad.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                auth.signOut();
                finish();
                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                Toast.makeText(SettingActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                startActivity(intent);
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

    void request() {
        Intent intent = new Intent(SettingActivity.this, RequestFriendActivity.class);
        startActivity(intent);
    }

    void password_reset() {
        Intent intent = new Intent(SettingActivity.this, PasswordResetActivity.class);
        startActivity(intent);
    }

    void user_ask() {
        Intent intent = new Intent(SettingActivity.this, ReportActivity.class);
        startActivity(intent);
    }
}
