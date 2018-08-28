package safetybread.hanium.sangeun.safetybread;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by sangeun on 2018-08-18.
 */

public class ServiceAreaFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {
    private String TAG = "ServiceFragment";

    private Realm realm = MainActivity.realm;
    private RealmResults<ServiceArea> results;

    private GoogleMap mMap;

    private FusedLocationProviderClient mLocationProviderClient;

    private BottomSheetBehavior bottomSheetBehavior;

    private TextView tv_serviceArea;
    private TextView tv_distance;
    private TextView tv_routeName;
    private TextView tv_time;
    private TextView tv_tel;
    private TextView tv_food;
    private TextView tv_etc;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_service_area, container, false);
        results = realm.where(ServiceArea.class).findAll();

        ConstraintLayout bottomSheet = rootView.findViewById(R.id.rest_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());

        tv_serviceArea = rootView.findViewById(R.id.serviceArea);
        tv_distance = rootView.findViewById(R.id.distance);
        tv_routeName = rootView.findViewById(R.id.tv_routeName);
        tv_time = rootView.findViewById(R.id.tv_time);
        tv_tel = rootView.findViewById(R.id.tv_tel);
        tv_food = rootView.findViewById(R.id.tv_food);
        tv_etc = rootView.findViewById(R.id.restInfo);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationClickListener(ServiceAreaFragment.this);
                mMap.setOnMyLocationButtonClickListener(ServiceAreaFragment.this);

                final List<Marker> markers = setMarker();

                if (markers != null) {
                    OnUserLocationSuccess onUserLocationSuccess = new OnUserLocationSuccess() {
                        @Override
                        public void OnSuccess(LatLng userLocation) {
                            Double shortest = null;
                            Marker nearestMarker = null;

                            //가장 가까운 휴게소 거리 계산
                            for (Marker marker : markers) {
                                LatLng to = marker.getPosition();
                                Double distance = SphericalUtil.computeDistanceBetween(userLocation, to);

                                if (shortest == null || shortest > distance) {
                                    shortest = distance;
                                    nearestMarker = marker;
                                }
                            }
                            //계산된 가까운 휴게소 지도에 마커 표시
                            final Marker finalNearestMarker = nearestMarker;
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nearestMarker.getPosition(), 16), new GoogleMap.CancelableCallback() {
                                @Override
                                public void onFinish() {
                                    if (finalNearestMarker != null) {
                                        finalNearestMarker.showInfoWindow();
                                        handleMarkerClick(finalNearestMarker);
                                    }
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                        }
                    };
                    getUserLocation(onUserLocationSuccess);
                }
            }
        });
        return rootView;
    }

    //파싱된 휴게소 정보를 지도에 표시하기 위한 마커 세팅
    private List<Marker> setMarker() {
        List<Marker> markers = new ArrayList<>();

        realm.beginTransaction();
        Log.d("setMarker() : ", results.first().toString());

        for (ServiceArea area : results) {
            LatLng temp = new LatLng(area.latitude, area.longitude);
            Marker marker = mMap.addMarker(new MarkerOptions().position(temp).title(area.areaName));
            area.markerId = marker.getId();
            markers.add(marker);
        }
        realm.commitTransaction();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                handleMarkerClick(marker);
                return false;
            }
        });

        return markers;
    }

    //휴게소 마커 클릭 시 상세 정보 표시
    private void handleMarkerClick(final Marker marker) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        tv_serviceArea.setText(marker.getTitle());

        ServiceArea area = results.where().equalTo("markerId", marker.getId()).findFirst();
        StringBuilder etc = new StringBuilder();

        tv_serviceArea.setText(area.areaName);

        tv_routeName.setText(area.routeName + "|" + area.routeType);
        tv_time.setText(area.startServiceAt + "~" + area.endServiceAt);
        tv_tel.setText(area.tel);
        tv_food.setText(area.signatureFood);

        tv_etc.setText("제공 편의 시설 : ");
        if (area.hasStore) {
            etc.append("매점 \n");
        }
        if (area.hasCafeteria) {
            etc.append("\t\t 음식점 \n");
        }
        if (area.hasElectricCarCharge) {
            etc.append("\t\t 전기차 충전소 \n");
        }
        if (area.hasFeedingRoom) {
            etc.append("\t\t 수유실 \n");
        }
        if (area.hasToilet) {
            etc.append("\t\t 화장실 \n");
        }
        if (area.hasPharmacy) {
            etc.append("\t\t 약국 \n");
        }
        if (area.hasMaintenance) {
            etc.append("\t\t 경정비 가능 \n");
        }
        if (area.hasLPGCharge) {
            etc.append("\t\t LPG 충전소 \n");
        }
        if (area.hasGasolineCharge) {
            etc.append("\t\t 주유소 \n");
        }
        if (area.hasRestPlace) {
            etc.append("\t\t 쉼터 \n");
        }

        Log.d(TAG, etc.toString());
        tv_etc.setText(etc.toString());
        etc = null;

        OnUserLocationSuccess onUserLocationSuccess = new OnUserLocationSuccess() {
            @Override
            public void OnSuccess(LatLng userLocation) {
                //현재 위치와 클릭된 휴게소와의 거리를 다시 측정
                Double distance = SphericalUtil.computeDistanceBetween(userLocation, marker.getPosition());
                distance = (double) Math.round(distance * 0.001);
                tv_distance.setText(distance.toString() + "km");
            }
        };
        getUserLocation(onUserLocationSuccess);
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation(final OnUserLocationSuccess onUserLocationSuccess) {
        mLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                onUserLocationSuccess.OnSuccess(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getActivity().getApplicationContext(), location.toString(), Toast.LENGTH_LONG);
    }

    private interface OnUserLocationSuccess {
        void OnSuccess(LatLng userLocation);
    }
}
