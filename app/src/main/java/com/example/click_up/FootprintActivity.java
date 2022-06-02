package com.example.click_up;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.click_up.Model.WriteDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

public class FootprintActivity extends AppCompatActivity {
    private String destinationUID;
    private String destinationNick;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private MapView mapView;
    private MapPOIItem marker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_footprint);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        toolbar = (Toolbar) findViewById(R.id.foot_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        mapView = new MapView(this);
        mapView.setZoomLevel(14, true);
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.footprint_view);
        viewGroup.addView(mapView);

        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);

        Intent getintent = getIntent();
        destinationUID = getintent.getStringExtra("destination_uid");
        destinationNick = getintent.getStringExtra("friend__nickname");

        getSupportActionBar().setTitle(destinationNick + " 님의 발자취 ");

        foot_print();
    }

    private void foot_print() {
        database.getReference("posts").child(destinationUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    WriteDTO writeDTO = item.getValue(WriteDTO.class);

                    Double latitude = Double.parseDouble(writeDTO.latitude);
                    Double longitude = Double.parseDouble(writeDTO.longigude);

                    marker = new MapPOIItem();
                    marker.setItemName("발자취");
                    marker.setTag(0);
                    marker.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude));
                    marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
                    marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                    mapView.addPOIItem(marker);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
