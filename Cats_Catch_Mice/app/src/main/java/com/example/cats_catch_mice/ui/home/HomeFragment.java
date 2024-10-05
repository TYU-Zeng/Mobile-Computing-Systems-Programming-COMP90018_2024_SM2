package com.example.cats_catch_mice.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cats_catch_mice.R;
import com.example.cats_catch_mice.databinding.FragmentMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null){
            mapFragment.getMapAsync(this);
        }

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // setting the location at Unimelb
        LatLng unimelb = new LatLng(-37.79, 144.96);
        googleMap.addMarker(new MarkerOptions()
                .position(unimelb)
                .title("Marker"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(unimelb));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}