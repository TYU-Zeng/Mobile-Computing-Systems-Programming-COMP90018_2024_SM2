package com.example.cats_catch_mice.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cats_catch_mice.R;
import com.example.cats_catch_mice.databinding.FragmentMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class HomeFragment extends Fragment implements OnMapReadyCallback{

    private static final String LOG_TAG = "MapFragment";
    private static final int USER_LOCATION_ZOOM = 18;
    private static final int DEFAULT_ZOOM = 16;
    private static final LatLngBounds UNIMELB_BOUNDARY= new LatLngBounds(
            new LatLng(-37.802506, 144.956938),
            new LatLng(-37.796215, 144.965135)
    );

    private FragmentMapBinding binding;
    private GoogleMap map;
    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private ActivityResultLauncher<String> resultPermissionLauncher;
    private FusedLocationProviderClient fusedLocationClient;

    // TODO: create attribute for last known setting
    // camera view, map tile, user location, etc.

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        HomeViewModel homeViewModel =
//                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        resultPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if(isGranted){
                Log.d("debugging", "granted");
                locationPermissionGranted = true;
                updateLocationUI();
            }else{
                Log.d("debugging", "not granted");
                locationPermissionGranted = false;
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null){
            mapFragment.getMapAsync(this);
        }

        return root;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        showUnimelb();
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showUnimelb(){
        // setting the boundary of Unimelb
        map.addMarker(new MarkerOptions()
                .position(UNIMELB_BOUNDARY.getCenter())
                .title("Marker"));
        map.setLatLngBoundsForCameraTarget(UNIMELB_BOUNDARY);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(UNIMELB_BOUNDARY.getCenter(), DEFAULT_ZOOM));
        Log.d("debugging", "unimelb map rendered");
    }

    private void updateLocationUI(){
        Log.d("debugging", "updating UI");
        if(this.map == null)
            return;
        try{
            if(this.locationPermissionGranted) {
                this.map.setMyLocationEnabled(true);
                this.map.getUiSettings().setMyLocationButtonEnabled(true);
            }else{
                this.map.setMyLocationEnabled(false);
                this.map.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }catch(SecurityException e){
            Log.e(LOG_TAG, "Exception occur when configuring map");
        }
    }

    private void getDeviceLocation(){
        try{
            if(this.locationPermissionGranted){
                fusedLocationClient.getLastLocation().addOnSuccessListener(this.getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location!= null){
                            Log.d("debugging", "last location got");
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(), location.getLongitude()), USER_LOCATION_ZOOM)
                            );
                        }
                    }
                });
            }
        }catch(SecurityException e){
            Log.e(LOG_TAG, "Error occurred when getting device location");
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("debugging", "check self permission");
            locationPermissionGranted = true;
        } else {
            Log.d("debugging", "request permission");
            resultPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
}