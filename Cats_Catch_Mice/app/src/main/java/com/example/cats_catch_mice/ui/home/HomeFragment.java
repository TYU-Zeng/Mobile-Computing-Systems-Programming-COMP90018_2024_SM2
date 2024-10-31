package com.example.cats_catch_mice.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private static final String LOG_TAG = "MapFragment";
    private static final int USER_LOCATION_ZOOM = 18;
    private static final int DEFAULT_ZOOM = 16;
    private static final LatLngBounds UNIMELB_BOUNDARY = new LatLngBounds(
            new LatLng(-37.802506, 144.956938),
            new LatLng(-37.796215, 144.965135)
    );
    private static final long TRIGGER_INTERVAL = 5000; //milliseconds
    private static final float ICON_SCALE = 0.08f;
    private static final long CATCH_BUTTON_COOLDOWN_PERIOD = 30000L; // 30 seconds in milliseconds

    // nfc coordinates (fixed)
    private static final LatLng CHEST1_COOR = new LatLng(-37.7996, 144.9618);
    private static final LatLng CHEST2_COOR = new LatLng(-37.7973, 144.9602);
    private static final LatLng CHEST3_COOR = new LatLng(-37.8017, 144.960198);
    private static final LatLng CHEST4_COOR = new LatLng(-37.7999, 144.963312);
    private static final LatLng CHEST5_COOR = new LatLng(-37.7968, 144.9628);


    private FragmentMapBinding binding;
    private GoogleMap map;
    private boolean locationPermissionGranted;
    private ActivityResultLauncher<String> resultPermissionLauncher;
    private boolean catchButtonCooldown = false;

    private HomeViewModel homeViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private FirebaseManager firebaseManager;
    private Handler handler;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> roomCheckTask;
    private static final long ROOM_CHECK_INTERVAL = 5;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        firebaseManager = new ViewModelProvider(requireActivity()).get(FirebaseManager.class);
        handler = new Handler(Looper.getMainLooper());

        // callback for location permission request
        setResultPermissionLauncher();

        // set up and start the trigger to get device location
        setLocationUpdateCallback();

        // set single thread to check if player joins room
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button catchMouseButton = view.findViewById(R.id.catch_mouse);
        String platerId = firebaseManager.getPlayerId();



        // Check if cooldown is active and update button text accordingly
        long currentTime = System.currentTimeMillis();
        if (currentTime < homeViewModel.catchButtonCooldownEndTime) {
            startCooldownTimer(catchMouseButton, homeViewModel.catchButtonCooldownEndTime - currentTime);
        } else {
            catchMouseButton.setText("Catch");
        }


        // Check if cooldown is active and update button text accordingly

        catchMouseButton.setOnClickListener(v -> {

            long currentTime1 = System.currentTimeMillis();
            if (currentTime1 < homeViewModel.catchButtonCooldownEndTime) {
                long remainingTime = (homeViewModel.catchButtonCooldownEndTime - currentTime1) / 1000; // in seconds

                return;
            }

            // Proceed with the button's action
            String roomId = firebaseManager.getRoomId(); // Get the actual room ID
            if (roomId == null) {
                Toast.makeText(getContext(), "You are not in a room.", Toast.LENGTH_SHORT).show();
                return;
            }

            homeViewModel.catchButtonCooldownEndTime = currentTime1 + CATCH_BUTTON_COOLDOWN_PERIOD;
            startCooldownTimer(catchMouseButton, CATCH_BUTTON_COOLDOWN_PERIOD);

            firebaseManager.getFullRoomDataAsync(roomId)
                    .thenAccept(roomSnapshot -> {
                        if (roomSnapshot != null) {
                            // Get the cat's coordinate
                            Pair<Double, Double> catCoordinate = getCatCoordinate(roomSnapshot);
                            if (catCoordinate == null) {
                                Log.e("HomeFragment", "Cat coordinate not found");
                                return;
                            }

                            // Get the mice's IDs and coordinates
                            List<Map<String, Pair<Double, Double>>> mouseDataList = getMouseCoordinatesWithId(roomSnapshot);
                            if (mouseDataList.isEmpty()) {
                                Log.e("HomeFragment", "No mouse data found");
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "No mice to catch.", Toast.LENGTH_SHORT).show();
                                });
                                return;
                            }

                            // Find the caught mouse
                            String caughtMouseId = findCaughtMouse(catCoordinate, mouseDataList);

                            if (caughtMouseId != null) {
                                // Mouse is within 15 meters
                                Log.d("HomeFragment", "Caught Mouse ID: " + caughtMouseId);

                                // You can update the mouse's status in Firebase if needed
                                // For example:
                                // firebaseManager.updateMouseCaught(roomId, caughtMouseId);

                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Mouse caught!" , Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                // No mouse within 15 meters
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "No mouse catched...", Toast.LENGTH_SHORT).show();
                                });
                            }
                        } else {
                            Log.e("HomeFragment", "Room data is null");
                        }
                    })
                    .exceptionally(throwable -> {
                        Log.e("HomeFragment", "Error retrieving room data: " + throwable.getMessage());
                        return null;
                    });

        });
    }

    private void startCooldownTimer(Button button, long cooldownDuration) {
        new CountDownTimer(cooldownDuration, 1000) {

            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                button.setText("Cool down: " + secondsRemaining + " s");
            }

            public void onFinish() {
                button.setText("Catch");
            }
        }.start();
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (this.getActivity() != null)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());

        // links map to fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        if (joinedRoom()) {
            startUpdatingLocation();
        } else {
            Toast.makeText(getContext(), "You need to join a room first.", Toast.LENGTH_SHORT).show();
        }

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }

    /* Periodic check if player joins room */

    private void startJoinRoomCheck() {
        if (roomCheckTask == null || roomCheckTask.isCancelled()) {
            roomCheckTask = executor.scheduleWithFixedDelay(
                    this::checkRoomSchedule,
                    0,
                    ROOM_CHECK_INTERVAL,
                    TimeUnit.SECONDS
            );
        }
    }

    private void checkRoomSchedule() {
        if (joinedRoom()) {
            mainHandler.post(() -> {
                onMapReady(this.map);
                if (roomCheckTask != null && !roomCheckTask.isCancelled()) {
                    roomCheckTask.cancel(true);
                }
            });
        }
    }

    /* Map */

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        if (this.map == null) {
            this.map = map;
        }
        showUnimelb();
        showChest();

        if (!joinedRoom()) {
            Toast.makeText(getContext(), "You need to join a room first.", Toast.LENGTH_SHORT).show();
            startJoinRoomCheck();
            return;
        }

        getLocationPermission();
        updateLocationUI();
        startUpdatingLocation();
    }


    private boolean joinedRoom() {
        return firebaseManager.getRoomId() != null;
    }

    private void showUnimelb() {
        map.setLatLngBoundsForCameraTarget(UNIMELB_BOUNDARY);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(UNIMELB_BOUNDARY.getCenter(), DEFAULT_ZOOM));
        Log.d("debugging", "unimelb map rendered");
    }

    private void showChest() {
        map.addMarker(new MarkerOptions().position(CHEST1_COOR).title("Marker").icon(getScaledIcon(R.drawable.chest, ICON_SCALE)).flat(true));
        map.addMarker(new MarkerOptions().position(CHEST2_COOR).title("Marker").icon(getScaledIcon(R.drawable.chest, ICON_SCALE)).flat(true));
        map.addMarker(new MarkerOptions().position(CHEST3_COOR).title("Marker").icon(getScaledIcon(R.drawable.chest, ICON_SCALE)).flat(true));
        map.addMarker(new MarkerOptions().position(CHEST4_COOR).title("Marker").icon(getScaledIcon(R.drawable.chest, ICON_SCALE)).flat(true));
        map.addMarker(new MarkerOptions().position(CHEST5_COOR).title("Marker").icon(getScaledIcon(R.drawable.chest, ICON_SCALE)).flat(true));
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

    // helper for icon rendering on map
    private BitmapDescriptor getScaledIcon(int resourceID, float scale) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), resourceID);
        int height = Math.round(icon.getHeight() * scale);
        int width = Math.round(icon.getWidth() * scale);
        Bitmap scaledIcon = Bitmap.createScaledBitmap(icon, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(scaledIcon);
    }

    /* Periodic map update*/

    private void startUpdatingLocation() {
        if (!locationPermissionGranted) {
            return;
        }
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY).setIntervalMillis(TRIGGER_INTERVAL).build();
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.d(LOG_TAG, "Error when request location updates");
        }
    }

    private void updateMap() {
        map.clear();

        showChest();

        firebaseManager.getFullRoomDataAsync(firebaseManager.getRoomId())
                .thenAccept(roomSnapshot -> {
                    if (roomSnapshot != null) {

                        // update cat icon on map if player is the owner (cat)
                        if (firebaseManager.isOwner()) {
                            Pair<Double, Double> catCoordinate = getCatCoordinate(roomSnapshot);
                            if (catCoordinate != null) {
                                map.addMarker(new MarkerOptions()
                                        .position(new LatLng(catCoordinate.first, catCoordinate.second))
                                        .title("Marker").icon(getScaledIcon(R.drawable.cat1, ICON_SCALE)).flat(true));
                            }
                        }

                        // update mice on map
                        List<Pair<Double, Double>> mouseDataList = getMouseCoordinates(roomSnapshot);
                        if (!mouseDataList.isEmpty()) {
                            for (Pair<Double, Double> mouseCoordinate : mouseDataList) {
                                map.addMarker(new MarkerOptions()
                                        .position(new LatLng(mouseCoordinate.first, mouseCoordinate.second))
                                        .title("Marker").icon(getScaledIcon(R.drawable.mouse1, ICON_SCALE)).flat(true));
                            }
                        }
                    }
                })
                .exceptionally(throwable -> {
                    return null;
                });
    }

    private void setLocationUpdateCallback() {
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
                    Log.d("debugging", "Lat: " + location.getLatitude() +
                            ", Long: " + location.getLongitude());
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    firebaseManager.updateLocation(firebaseManager.getPlayerId(), lat, lng, firebaseManager.getRoomId());
                    updateMap();
                }
            }
        };
    }


    /* Location permission */

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

    private void setResultPermissionLauncher() {
        resultPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Log.d("debugging", "granted");
                locationPermissionGranted = true;
                updateLocationUI();
                startUpdatingLocation();
            } else {
                Log.d("debugging", "not granted");
                locationPermissionGranted = false;
                showLocationRequireDialog();
            }
        });
    }

    /* Location permission dialog when permission denied */

    // show dialog when user denies location permission
    private void showLocationRequireDialog() {
        new AlertDialog.Builder(this.getContext())
                .setTitle("Location permission required")
                .setMessage("The game needs location permission to function properly. Please turn it on.")
                .setPositiveButton("Go to Settings", (dialog, which) -> openSettings())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .create()
                .show();
    }

    // open Settings on user's phone
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private List<Pair<Double, Double>> getMouseCoordinates(DataSnapshot roomSnapshot) {
        List<Pair<Double, Double>> mouseCoordinates = new ArrayList<>();

        if (roomSnapshot == null) {
            Log.e("HomeFragment", "Room snapshot is null");
            return mouseCoordinates; // Return empty list
        }

        // Retrieve the owner ID (cat's player ID)
        String ownerId = roomSnapshot.child("owner").getValue(String.class);
        Log.d("HomeFragment", "Owner ID: " + ownerId);
        if (ownerId == null) {
            Log.e("HomeFragment", "Owner ID not found in room data");
            return mouseCoordinates; // Return empty list
        }

        // Get the 'members' snapshot
        DataSnapshot membersSnapshot = roomSnapshot.child("members");
        if (!membersSnapshot.exists()) {
            Log.e("HomeFragment", "Members data does not exist");
            return mouseCoordinates; // Return empty list
        }

        Log.d("HomeFragment", "Number of members: " + membersSnapshot.getChildrenCount());

        // Iterate through each member
        for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
            String memberId = memberSnapshot.getKey();
            Log.d("HomeFragment", "Processing member ID: " + memberId);

            // Skip the owner (cat)
            if (memberId.equals(ownerId)) {
                Log.d("HomeFragment", "Skipping owner");
                continue;
            }

            // Get the 'visible' field
            Boolean visible = memberSnapshot.child("visible").getValue(Boolean.class);
            Log.d("HomeFragment", "Member " + memberId + " visible: " + visible);

            // Temporarily ignore 'visible' check for debugging
            // if (visible == null || !visible) {
            //     Log.d("HomeFragment", "Skipping member " + memberId + " due to visibility");
            //     continue;
            // }

            // Get latitude and longitude
            Double lat = memberSnapshot.child("lat").getValue(Double.class);
            Double lng = memberSnapshot.child("lng").getValue(Double.class);
            Log.d("HomeFragment", "Member " + memberId + " coordinates: Lat=" + lat + ", Lng=" + lng);

            // Ensure lat and lng are not null
            if (lat != null && lng != null) {
                mouseCoordinates.add(new Pair<>(lat, lng));
            } else {
                Log.e("HomeFragment", "Invalid coordinates for member: " + memberId);
            }
        }

        return mouseCoordinates;
    }

    private String findCaughtMouse(
            Pair<Double, Double> catCoordinate,
            List<Map<String, Pair<Double, Double>>> mouseDataList
    ) {
        if (catCoordinate == null || mouseDataList == null || mouseDataList.isEmpty()) {
            return null;
        }

        Location catLocation = new Location("cat");
        catLocation.setLatitude(catCoordinate.first);
        catLocation.setLongitude(catCoordinate.second);

        String closestMouseId = null;
        float minDistance = Float.MAX_VALUE;

        for (Map<String, Pair<Double, Double>> mouseData : mouseDataList) {
            for (Map.Entry<String, Pair<Double, Double>> entry : mouseData.entrySet()) {
                String mouseId = entry.getKey();
                Pair<Double, Double> mouseCoordinate = entry.getValue();

                Location mouseLocation = new Location("mouse");
                mouseLocation.setLatitude(mouseCoordinate.first);
                mouseLocation.setLongitude(mouseCoordinate.second);

                float distance = catLocation.distanceTo(mouseLocation);
                if (distance <= 15 && distance < minDistance) {
                    minDistance = distance;
                    closestMouseId = mouseId;
                }
            }
        }

        return closestMouseId;
    }


    private List<Map<String, Pair<Double, Double>>> getMouseCoordinatesWithId(DataSnapshot roomSnapshot) {
        List<Map<String, Pair<Double, Double>>> mouseDataList = new ArrayList<>();

        if (roomSnapshot == null) {
            Log.e("HomeFragment", "Room snapshot is null");
            return mouseDataList; // Return empty list
        }

        // Retrieve the owner ID (cat's player ID)
        String ownerId = roomSnapshot.child("owner").getValue(String.class);
        if (ownerId == null) {
            Log.e("HomeFragment", "Owner ID not found in room data");
            return mouseDataList; // Return empty list
        }

        // Get the 'members' snapshot
        DataSnapshot membersSnapshot = roomSnapshot.child("members");
        if (!membersSnapshot.exists()) {
            Log.e("HomeFragment", "Members data does not exist");
            return mouseDataList; // Return empty list
        }

        // Iterate through each member
        for (DataSnapshot memberSnapshot : membersSnapshot.getChildren()) {
            String memberId = memberSnapshot.getKey();

            // Skip the owner (cat)
            if (memberId.equals(ownerId)) {
                continue;
            }

            // Get latitude and longitude
            Double lat = memberSnapshot.child("lat").getValue(Double.class);
            Double lng = memberSnapshot.child("lng").getValue(Double.class);

            // Ensure lat and lng are not null
            if (lat != null && lng != null) {
                Map<String, Pair<Double, Double>> mouseData = new HashMap<>();
                mouseData.put(memberId, new Pair<>(lat, lng));
                mouseDataList.add(mouseData);
            } else {
                Log.e("HomeFragment", "Invalid coordinates for member: " + memberId);
            }
        }

        return mouseDataList;
    }

    private Pair<Double, Double> getCatCoordinate(DataSnapshot roomSnapshot) {
        if (roomSnapshot == null) {
            Log.e("HomeFragment", "Room snapshot is null");
            return null;
        }

        // Retrieve the owner ID (cat's player ID)
        String ownerId = roomSnapshot.child("owner").getValue(String.class);
        if (ownerId == null) {
            Log.e("HomeFragment", "Owner ID not found in room data");
            return null;
        }

        // Get the owner's data from 'members'
        DataSnapshot ownerSnapshot = roomSnapshot.child("members").child(ownerId);
        if (!ownerSnapshot.exists()) {
            Log.e("HomeFragment", "Owner data does not exist in members");
            return null;
        }

        // Get latitude and longitude
        Double lat = ownerSnapshot.child("lat").getValue(Double.class);
        Double lng = ownerSnapshot.child("lng").getValue(Double.class);

        // Ensure lat and lng are not null
        if (lat != null && lng != null) {
            return new Pair<>(lat, lng);
        } else {
            Log.e("HomeFragment", "Invalid coordinates for owner");
            return null;
        }
    }
}


