package com.example.cats_catch_mice.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cats_catch_mice.DatabaseManager;
import com.example.cats_catch_mice.FirebaseManager;
import com.example.cats_catch_mice.R;
import com.example.cats_catch_mice.databinding.FragmentMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private static final String LOG_TAG = "MapFragment";
    private static final int USER_LOCATION_ZOOM = 18;
    private static final int DEFAULT_ZOOM = 16;
    private static final LatLngBounds UNIMELB_BOUNDARY = new LatLngBounds(
            new LatLng(-37.802506, 144.956938),
            new LatLng(-37.796215, 144.965135)
    );
    private static final long TRIGGER_INTERVAL = 5000; //milliseconds
    private static final float MOUSE_ICON_SCALE = 0.1f;

    private FragmentMapBinding binding;
    private GoogleMap map;
    private boolean locationPermissionGranted;
    private ActivityResultLauncher<String> resultPermissionLauncher;
    private FusedLocationProviderClient fusedLocationClient;

    // looper for retrieving player's location
    private Handler triggerHandler = new Handler(Looper.getMainLooper());
    private Runnable locationUpdateTrigger;

    // flag for thread scheduling
    private boolean updating = true;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private FirebaseManager firebaseManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        firebaseManager = new ViewModelProvider(requireActivity()).get(FirebaseManager.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // callback for location permission request
        setResultPermissionLauncher();

        if (this.getActivity() != null)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());

        // links map to fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // set up and start the trigger to get device location
        setLocationUpdateCallback();
        startUpdatingLocation();

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        showUnimelb();
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        startUpdatingLocation();
    }

    private void setResultPermissionLauncher() {
        resultPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Log.d("debugging", "granted");
                locationPermissionGranted = true;
                updateLocationUI();
                getDeviceLocation();
                startUpdatingLocation();
            } else {
                Log.d("debugging", "not granted");
                locationPermissionGranted = false;
            }
        });
    }

    public void startUpdatingLocation() {
        locationUpdateTrigger = new Runnable() {
            public void run() {
                if (updating){
                    Log.d("debugging", "start triggering");
                    getDeviceLocation();
                    triggerHandler.postDelayed(this, TRIGGER_INTERVAL);
                }
            }
        };
        triggerHandler.post(locationUpdateTrigger);
    }

    public void stopUpdatingLocation() {
        Log.d("debugging", "stop triggering");
        triggerHandler.removeCallbacks(locationUpdateTrigger);
    }

    // setter
    public void setUpdating(boolean updating){
        this.updating = updating;
    }

    private BitmapDescriptor getScaledIcon(int resourceID, float scale){
        Bitmap icon = BitmapFactory.decodeResource(getResources(), resourceID);
        int height = Math.round(icon.getHeight()*scale);
        int width= Math.round(icon.getWidth()*scale);
        Bitmap scaledIcon = Bitmap.createScaledBitmap(icon, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(scaledIcon);
    }

    private void showUnimelb() {
        map.addMarker(new MarkerOptions()
                .position(UNIMELB_BOUNDARY.getCenter())
                .title("Marker").icon(getScaledIcon(R.drawable.mouse, MOUSE_ICON_SCALE)).flat(true));
        map.setLatLngBoundsForCameraTarget(UNIMELB_BOUNDARY);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(UNIMELB_BOUNDARY.getCenter(), DEFAULT_ZOOM));
        Log.d("debugging", "unimelb map rendered");
    }

    private void updateLocationUI() {
        Log.d("debugging", "updating UI");
        if (this.map == null)
            return;
        try {
            if (this.locationPermissionGranted) {
                this.map.setMyLocationEnabled(true);
                this.map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                this.map.setMyLocationEnabled(false);
                this.map.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Exception occur when configuring map");
        }
    }

    private void getDeviceLocation() {
        if (this.getActivity() == null) {
            Log.e(LOG_TAG, "Error when getting activity");
            return;
        }
        try {
            if (this.locationPermissionGranted) {
                Log.d("debugging", "getting device location");
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(
                        requireActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                 Log.d("debugging", "last location got");
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(location.getLatitude(), location.getLongitude()), USER_LOCATION_ZOOM)
                                    );
                                }
                            }
                        });
    private void setLocationUpdateCallback(){
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d(LOG_TAG, "Error: location is null");
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Log.d("debugging", "last location got");
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), USER_LOCATION_ZOOM)
                    );
                    Log.d("debugging", "Lat: " + location.getLatitude() +
                            ", Long: " + location.getLongitude());
                }
            }
        };
    }

    private void getLocationPermission() {
        if (this.getContext() == null) {
            Log.e(LOG_TAG, "Error when getting context");
            return;
        }
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