package com.gsw.click_up;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gsw.click_up.Model.ReportModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private EditText report_title, report_user, report_memo;
    private Button report_make;
    private RadioGroup rgReport;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        report_make = (Button) findViewById(R.id.report_make);
        report_user = (EditText) findViewById(R.id.report_user);
        report_title = (EditText) findViewById(R.id.report_title);
        report_memo = (EditText) findViewById(R.id.report_memo);
        rgReport = (RadioGroup) findViewById(R.id.rgReport);

        findViewById(R.id.report_make).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.report_make:
                    goReport();
                    break;
            }
        }
    };

    private void goReport() {
        Long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat createdAt = new SimpleDateFormat("yy-MM-dd, hh:mm aaa");

        int rbid = rgReport.getCheckedRadioButtonId();
        RadioButton rb = (RadioButton) findViewById(rbid);
        String classify = rb.getText().toString();

        ReportModel reportModel = new ReportModel();
        reportModel.userid = report_user.getText().toString();
        reportModel.classify = classify;
        reportModel.title = report_title.getText().toString();
        reportModel.memo = report_memo.getText().toString();
        reportModel.createdAt = createdAt.format(date);
        reportModel.uid = auth.getCurrentUser().getUid();

        if (classify.equals("  문의")) {
            database.getReference().child("userAsk").child("ask").push().setValue(reportModel);
            Toast.makeText(ReportActivity.this, "제출 완료", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            database.getReference().child("userAsk").child("report").push().setValue(reportModel);
            Toast.makeText(ReportActivity.this, "제출 완료", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
