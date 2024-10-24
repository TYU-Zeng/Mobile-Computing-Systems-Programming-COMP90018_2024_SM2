package com.example.cats_catch_mice.ui.itemList;

//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.Fragment;
//import androidx.navigation.fragment.NavHostFragment;
//
//import com.example.cats_catch_mice.databinding.FragmentItemDetailBinding;
//
//public class ItemDetailFragment extends Fragment {
//
//    private FragmentItemDetailBinding binding;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        binding = FragmentItemDetailBinding.inflate(inflater, container, false);
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // TODO: add button function
//        binding.buttonCancel.setOnClickListener(v -> {
//            ((AppCompatActivity) requireActivity()).getSupportFragmentManager().popBackStack();
//        });
//
//        // TODO: call item function
//        binding.buttonUse.setOnClickListener(v -> {
//            Toast.makeText(getContext(), "Item used!", Toast.LENGTH_SHORT).show();
//        });
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//}
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
//import com.squareup.picasso.Picasso;
import com.example.cats_catch_mice.databinding.FragmentItemDetailBinding; // Import binding class



public class ItemDetailFragment extends Fragment {

    private ItemViewModel itemViewModel;
    private FragmentItemDetailBinding binding; // Declare binding
    private String itemName;
    private int itemCount;
//    private String itemImageUrl;
    private int imageResId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentItemDetailBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Get the arguments passed from ItemListFragment
        if (getArguments() != null) {
            itemName = getArguments().getString("itemName");
            itemCount = getArguments().getInt("itemCount");
//            itemImageUrl = getArguments().getString("itemImageUrl");
            imageResId = getArguments().getInt("itemImage");

            // Populate UI with item details using binding
            binding.itemNameTextView.setText(itemName);
            binding.itemCountTextView.setText(String.valueOf(itemCount));
//            Picasso.get().load(itemImageUrl).into(binding.itemImageView);
            binding.itemImageView.setImageResource(imageResId);
        }

        // Get ViewModel
        itemViewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);

        // Handle "use" button click using binding
        binding.useButton.setOnClickListener(v -> {
            if (itemCount > 0) {
                itemCount--;
                binding.itemCountTextView.setText(String.valueOf(itemCount));

                // Update the item count in Firestore through ViewModel
                itemViewModel.useItem(itemName, itemCount); // Assume itemName is used as the item ID
            }
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
