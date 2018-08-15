package safetybread.hanium.sangeun.safetybread;

import android.annotation.SuppressLint;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import safetybread.hanium.sangeun.safetybread.Models.ServiceArea;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ServiceAreaActivity extends PermissionActivity implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {
    private static final String TAG = ServiceAreaActivity.class.getSimpleName();
    private static final int ACTIVITY_NUM = 1;

    private GoogleMap mMap;
    private Realm realm = MainActivity.realm;

    private FusedLocationProviderClient mLocationProviderClient;

    private BottomSheetBehavior bottomSheetBehavior;
    private TextView unitName;
    private TextView distanceText;
    private TextView restInfo;

    private RealmResults<ServiceArea> savedArea = MainActivity.savedAreas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate : strating");
        setContentView(R.layout.activity_service_area);
        setUpBottomNavigationView();

        ConstraintLayout bottomSheet = (ConstraintLayout) findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        unitName = bottomSheet.findViewById(R.id.serviceArea);
        distanceText = bottomSheet.findViewById(R.id.distance);
        restInfo = bottomSheet.findViewById(R.id.restInfo);

        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        settingPermissions();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(ServiceAreaActivity.this);
                mMap.setOnMyLocationClickListener(ServiceAreaActivity.this);
                final List<Marker> markers = setMarker();
                OnUserLocationSuccess onUserLocationSuccess = new OnUserLocationSuccess() {
                    @Override
                    public void onSuccess(LatLng userLocation) {
                        Double shortest = null;
                        Marker nearestMarker = null;

                        for (Marker marker : markers) {
                            LatLng to = marker.getPosition();
                            Double distance = SphericalUtil.computeDistanceBetween(userLocation, to);

                            if (shortest == null || shortest > distance) {
                                shortest = distance;
                                nearestMarker = marker;
                            }
                        }
                        final Marker finalNearestMarker = nearestMarker;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nearestMarker.getPosition(), 16), new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                finalNearestMarker.showInfoWindow();
                                handleMarkerClick(finalNearestMarker);
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                    }
                };
                getUserLocation(onUserLocationSuccess);
            }
        });

    }

    private void settingPermissions() {
        addPermission(ACCESS_COARSE_LOCATION);
        addPermission(ACCESS_FINE_LOCATION);
        checkAndRequestPermissions();
    }

    private void setUpBottomNavigationView() {
        BottomNavigationView navigationView = findViewById(R.id.navigator);
        BottomNavigationViewHelper.disableShiftMode(navigationView);
        BottomNavigationViewHelper.selectMenu(getApplicationContext(), navigationView);
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation(final OnUserLocationSuccess onUserLocationSuccess) {
        mLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                onUserLocationSuccess.onSuccess(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });
    }

    private List<Marker> setMarker() {
        List<Marker> markers = new ArrayList<>();

        realm.beginTransaction();
        for (ServiceArea area: savedArea) {
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

    private void handleMarkerClick(final Marker marker) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        unitName.setText(marker.getTitle());
        ServiceArea area = savedArea.where().equalTo("markerId", marker.getId()).findFirst();
        restInfo.setText(area.toString());
        OnUserLocationSuccess onUserLocationSuccess = new OnUserLocationSuccess() {
            @Override
            public void onSuccess(LatLng userLocation) {
                Double distance = SphericalUtil.computeDistanceBetween(userLocation, marker.getPosition());
                distance = (double) Math.round(distance * 0.001);
                distanceText.setText(distance.toString());
            }
        };
        getUserLocation(onUserLocationSuccess);
    }


    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getApplicationContext(), location.toString(), Toast.LENGTH_LONG);
    }

    private interface OnUserLocationSuccess {
        void onSuccess(LatLng userLocation);
    }
}


