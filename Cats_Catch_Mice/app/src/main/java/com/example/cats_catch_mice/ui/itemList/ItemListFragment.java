package com.example.cats_catch_mice.ui.itemList;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.cats_catch_mice.databinding.FragmentItemListBinding; // Import binding class

import java.util.ArrayList;
import com.example.cats_catch_mice.FirebaseManager;

public class ItemListFragment extends Fragment {

    private ItemAdapter itemAdapter;
    private FragmentItemListBinding binding;
    private FirebaseManager itemViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemViewModel = new ViewModelProvider(requireActivity()).get(FirebaseManager.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        // Inflate the layout using View Binding
        binding = FragmentItemListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Set up the RecyclerView using the binding
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        itemAdapter = new ItemAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(itemAdapter);

        // Update the item list
        itemViewModel.getItemListLiveData().observe(getViewLifecycleOwner(), items -> {
            itemAdapter.setItems(items);
            itemAdapter.notifyDataSetChanged();
        });

        return view;
    }

    // Clean up the binding reference when the fragment is destroyed
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
