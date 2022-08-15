package com.gsw.click_up;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

public class SettingActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private Button btnUserInfoChange, btnUsersFriend, setting_logout, friend_request, password_reset, ask_btn;
    private Toolbar toolbar;
    private Dialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        auth = FirebaseAuth.getInstance();
        btnUserInfoChange = (Button) findViewById(R.id.btnUserInfoChange);
        btnUsersFriend = (Button) findViewById(R.id.btnUsersFriend);
        setting_logout = (Button) findViewById(R.id.setting_logout);
        friend_request = (Button) findViewById(R.id.friend_request);
        password_reset = (Button) findViewById(R.id.password_reset);
        ask_btn = (Button) findViewById(R.id.ask_btn);

        dialog = new Dialog(SettingActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);

        toolbar = (Toolbar) findViewById(R.id.set_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("설정");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.setting_logout).setOnClickListener(onClickListener);
        findViewById(R.id.friend_request).setOnClickListener(onClickListener);
        findViewById(R.id.password_reset).setOnClickListener(onClickListener);
        findViewById(R.id.btnUserInfoChange).setOnClickListener(onClickListener);
        findViewById(R.id.btnUsersFriend).setOnClickListener(onClickListener);
        findViewById(R.id.ask_btn).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnUserInfoChange:
                    Intent intentInfo = new Intent(SettingActivity.this, UserProfileChangeActivity.class);
                    startActivity(intentInfo);
                    break;

                case R.id.btnUsersFriend:
                    Intent intentFriend = new Intent(SettingActivity.this, MyFriendActivity.class);
                    startActivity(intentFriend);
                    break;

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
        Button yesbtn = dialog.findViewById(R.id.btn_yes);
        Button nobtn = dialog.findViewById(R.id.btn_no);
        TextView text_dialog = dialog.findViewById(R.id.text_dialog);

        text_dialog.setText("App에서 로그아웃합니다");
        dialog.show();

        yesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                dialog.dismiss();
                finish();
                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                Toast.makeText(SettingActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });

        nobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
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
