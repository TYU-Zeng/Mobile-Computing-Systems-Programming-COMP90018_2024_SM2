package com.example.cats_catch_mice.ui.itemList;

//public class ItemListFragment {
//}
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.cats_catch_mice.databinding.FragmentItemListBinding; // Import binding class

import com.example.cats_catch_mice.R;

import java.util.ArrayList;

public class ItemListFragment extends Fragment {

    private ItemViewModel itemViewModel;
    private ItemAdapter itemAdapter;
    private FragmentItemListBinding binding; // Declare binding

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentItemListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Set up the RecyclerView using the binding
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

//        itemAdapter = new ItemAdapter(item -> {
//            // When an item is clicked, navigate to the ItemDetailFragment and pass the item details
//            Bundle bundle = new Bundle();
//            bundle.putString("itemName", item.getName());
//            bundle.putInt("itemCount", item.getCount());
////            bundle.putString("itemImageUrl", item.getImageUrl());
//            bundle.putInt("itemImage", item.getImageResId());
//            Navigation.findNavController(view).navigate(R.id.action_itemListFragment_to_itemDetailFragment, bundle);
//        });
        // Set up Adapter (assuming you already have a list of items)
        itemAdapter = new ItemAdapter(new ArrayList<>()); // You can pass dummy data here
        binding.recyclerView.setAdapter(itemAdapter);

        binding.recyclerView.setAdapter(itemAdapter);

        // Get the ViewModel and observe the item list
        itemViewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
        itemViewModel.getItemListLiveData().observe(getViewLifecycleOwner(), items -> {
            itemAdapter.setItems(items);
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
