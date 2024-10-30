package com.example.cats_catch_mice;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cats_catch_mice.databinding.FragmentItemDetailBinding;

public class ItemDetailFragment extends Fragment {

    private FragmentItemDetailBinding binding;
    private FirebaseManager firebaseManager;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentItemDetailBinding.inflate(inflater, container, false);
        firebaseManager = new ViewModelProvider(requireActivity()).get(FirebaseManager.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assert getArguments() != null;
        String itemId = getArguments().getString("item_id");
        Log.d("Item Detail", "onViewCreated: get item id: " + itemId);

        // TODO: add button function
        binding.buttonCancel.setOnClickListener(v -> {
            ((AppCompatActivity) requireActivity()).getSupportFragmentManager().popBackStack();
        });

        // TODO: call item function
        binding.buttonUse.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Item used!", Toast.LENGTH_SHORT).show();
            firebaseManager.decreaseItemCount("UUID11111", itemId, "roomId12345");


        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
