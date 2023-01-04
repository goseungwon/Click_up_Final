package com.gsw.click_up_final;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

public class PostMapActivity extends Activity {
    private Double post_lati, post_longi;
    Button btn_close;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.postmap);

        MapView postMapView = new MapView(this);
        ViewGroup postMapViewContainer = (ViewGroup) findViewById(R.id.postMapview);
        postMapViewContainer.addView(postMapView);

        Intent postIntent = getIntent();
        post_lati = Double.parseDouble(postIntent.getStringExtra("latitude_popup"));
        post_longi = Double.parseDouble(postIntent.getStringExtra("longitude_popup"));

        postMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(post_lati,post_longi), true);
        postMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);

        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("게시글 위치");
        marker.setTag(0);
        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(post_lati,post_longi));
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        postMapView.addPOIItem(marker);

        btn_close = (Button) findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent();
                it.putExtra("close", true);
                setResult(RESULT_OK,it);
                finish();
            }
        });
    }
}