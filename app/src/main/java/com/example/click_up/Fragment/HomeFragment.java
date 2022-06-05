package com.example.click_up.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.click_up.LoginActivity;
import com.example.click_up.Model.ChatroomModel;
import com.example.click_up.Model.FriendDTO;
import com.example.click_up.Model.WriteDTO;
import com.example.click_up.OpenChatMakeActivity;
import com.example.click_up.R;
import com.example.click_up.WriteActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {
    private ImageView img_make_openchat, img_make_post;

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

    public Double my_lati;
    public Double my_longi;

    public String marker_nick;

    private MapView mapView;
    private MapPOIItem marker;
    private MapCircle circle;
    private Bitmap bitmap;
    private Dialog dialog;
    private String uid;

    public Double lat3, lon3;


    List<String> friend_list = new ArrayList<>();


    @Override
    public void onStart() {
        super.onStart();
        get_friend();
        Log.d("거리 M단위", String.valueOf(distance(37.560959, 127.193786, 37.564769, 127.192161))+ "M");
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);

        if (firebaseAuth.getCurrentUser() == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }

        //circle_OpenChat();

        mapView = new MapView(getActivity());
        ViewGroup mapViewContainer = (ViewGroup) v.findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);



        if (!checkLocationServiceStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        mapView.setShowCurrentLocationMarker(false);

        /*
        Timer timer = new Timer();

        TimerTask Trkoff = new TimerTask() {
            @Override
            public void run() {
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
            }
        };
        timer.schedule(Trkoff, 5000, 2999);

        TimerTask Trkon = new TimerTask() {
            @Override
            public void run() {
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            }
        };
        timer.schedule(Trkon, 1000, 1000 * 60 * 5);

        mapView.setShowCurrentLocationMarker(false);

         */

        img_make_openchat = (ImageView) v.findViewById(R.id.img_make_openchat);
        img_make_post = (ImageView) v.findViewById(R.id.img_make_post);

        v.findViewById(R.id.img_make_post).setOnClickListener(onClickListener);
        v.findViewById(R.id.img_make_openchat).setOnClickListener(onClickListener);

        return v;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.img_make_post:
                    writePost();
                    break;

                case R.id.img_make_openchat:
                    makeChat();
                    break;
            }
        }
    };

    void writePost() {
        Toast.makeText(getActivity(), "지도를 클릭해 주세요", Toast.LENGTH_SHORT).show();

        mapView.setMapViewEventListener(new MapView.MapViewEventListener() {
            @Override
            public void onMapViewInitialized(MapView mapView) {

            }

            @Override
            public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

            }

            @Override
            public void onMapViewZoomLevelChanged(MapView mapView, int i) {

            }

            @Override
            public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

                Button yesbtn = dialog.findViewById(R.id.btn_yes);
                Button nobtn = dialog.findViewById(R.id.btn_no);
                TextView text_dialog = dialog.findViewById(R.id.text_dialog);

                text_dialog.setText("게시물을 작성하시겠습니까?");
                dialog.show();

                yesbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getActivity(), "작성 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                        Intent intent = new Intent(getActivity(), WriteActivity.class);
                        intent.putExtra("latitude", mapPoint.getMapPointGeoCoord().latitude);
                        intent.putExtra("longitude", mapPoint.getMapPointGeoCoord().longitude);
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

            @Override
            public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

            }

            @Override
            public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

            }

            @Override
            public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

            }

            @Override
            public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

            }

            @Override
            public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

            }
        });
    }

    void makeChat() {
        Button yesbtn = dialog.findViewById(R.id.btn_yes);
        Button nobtn = dialog.findViewById(R.id.btn_no);
        TextView text_dialog = dialog.findViewById(R.id.text_dialog);

        text_dialog.setText("채팅방을 개설하시겠습니까?");
        dialog.show();

        yesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapView.setCurrentLocationEventListener(new MapView.CurrentLocationEventListener() {
                    @Override
                    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
                        my_lati = mapPoint.getMapPointGeoCoord().latitude;
                        my_longi = mapPoint.getMapPointGeoCoord().longitude;

                        Toast.makeText(getActivity(), "개설 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                        Intent intent = new Intent(getActivity(), OpenChatMakeActivity.class);
                        intent.putExtra("my_lati", my_lati);
                        intent.putExtra("my_longi", my_longi);
                        startActivity(intent);
                    }

                    @Override
                    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
                    }

                    @Override
                    public void onCurrentLocationUpdateFailed(MapView mapView) {
                    }

                    @Override
                    public void onCurrentLocationUpdateCancelled(MapView mapView) {
                    }
                });
            }
        });

        nobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }


    /**
     * 거리 계산 메서드
     *
     * @param lat1 지점 1 위도
     * @param lon1 지점 1 경도
     * @param lat2 지점 2 위도
     * @param lon2 지점 2 경도
     * @return
     */
    private static double distance(double lat1, double lon1, double lat2, double lon2) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1609.344;



        return (dist);
    }


    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private void marker_Post() {
        database.getReference().child("posts").child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    WriteDTO writeDTO = item.getValue(WriteDTO.class);

                    Double lat_ = Double.parseDouble(writeDTO.latitude);
                    Double long_ = Double.parseDouble(writeDTO.longigude);
                    String nick = writeDTO.userid;
                    String url = writeDTO.imageURL;

                    marker = new MapPOIItem();
                    marker.setItemName(url);
                    marker.setUserObject(nick);
                    marker.setTag(0);
                    marker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat_, long_));
                    marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                    marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                    mapView.addPOIItem(marker);

                    mapView.setCalloutBalloonAdapter(new DialogAdapter());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }



    private void get_friend() {
        database.getReference("friends_").child(uid)
                .orderByChild("friends/" + uid).equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    FriendDTO friendDTO = item.getValue(FriendDTO.class);
                    int i = 0;
                    friend_list.add(i, friendDTO.uid);
                    i++;
                }
                friend_marker();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void friend_marker() {
        for (int i = 0; i<friend_list.size(); i++) {
            database.getReference().child("posts").child(friend_list.get(i)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot item : snapshot.getChildren()) {
                        WriteDTO writeDTO = item.getValue(WriteDTO.class);

                        Double lat = Double.parseDouble(writeDTO.latitude);
                        Double lon = Double.parseDouble(writeDTO.longigude);
                        String nick = writeDTO.userid;
                        String url = writeDTO.imageURL;

                        marker = new MapPOIItem();
                        marker.setItemName(url);
                        marker.setUserObject(nick);
                        marker.setTag(0);
                        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat,lon));
                        marker.setMarkerType(MapPOIItem.MarkerType.RedPin);
                        marker.setSelectedMarkerType(MapPOIItem.MarkerType.BluePin);
                        mapView.addPOIItem(marker);

                        mapView.setCalloutBalloonAdapter(new DialogAdapter());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            marker_Post();
        }
    }






    private void circle_OpenChat() {
        database.getReference().child("openchat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    ChatroomModel chatroomModel = item.getValue(ChatroomModel.class);

                    Double lat1 = Double.parseDouble(chatroomModel.openChat_latitude);
                    Double lon1 = Double.parseDouble(chatroomModel.openChat_longitude);

                    circle = new MapCircle(
                            MapPoint.mapPointWithGeoCoord(lat1, lon1), // center
                            200, //
                            Color.argb(128, 255, 0, 0),
                            Color.argb(128, 200, 200, 0)
                    );
                    circle.setTag(1234);

                    mapView.addCircle(circle);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkRunTimePermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(getActivity(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                if (checkLocationServiceStatus()) {
                    if (checkLocationServiceStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    private boolean checkLocationServiceStatus() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    class DialogAdapter implements CalloutBalloonAdapter {
        private View dialog;

        public DialogAdapter() {
            dialog = getLayoutInflater().inflate(R.layout.item_dialog, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem mapPOIItem) {

            getBitmap(mapPOIItem.getItemName());
            ((ImageView) dialog.findViewById(R.id.dialog_image)).setImageBitmap(bitmap);
            ((TextView) dialog.findViewById(R.id.dialog_title)).setText(mapPOIItem.getUserObject().toString());

            return dialog;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem mapPOIItem) {

            return dialog;
        }
    }




    public Bitmap getBitmap(String imgPath) {
        Thread imgThread = new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(imgPath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                } catch (IOException e) {
                    Log.d("비트맵 변환 스레드", e.toString());

                }
            }
        };
        imgThread.start();
        try {
            imgThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            return bitmap;
        }
    }
}