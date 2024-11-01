package com.example.cats_catch_mice.ui.itemList;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.cats_catch_mice.FirebaseManager;
import androidx.navigation.Navigation;

import androidx.lifecycle.ViewModelProvider;
import com.example.cats_catch_mice.databinding.FragmentItemDetailBinding; // Import binding class



public class ItemDetailFragment extends Fragment {

    private FirebaseManager itemViewModel;
    private FragmentItemDetailBinding binding; // Declare binding
    private String itemName;
    private String itemDescription;
    private int itemCount;
    private int imageResId;
    private FirebaseManager firebaseManager;
    private boolean beCaught;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseManager = new ViewModelProvider(requireActivity()).get(FirebaseManager.class);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout using View Binding
        binding = FragmentItemDetailBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Get the arguments passed from ItemListFragment
        if (getArguments() != null) {
            itemName = getArguments().getString("itemName");
            itemDescription = getArguments().getString("itemDescription");
            itemCount = getArguments().getInt("itemCount");
            imageResId = getArguments().getInt("itemImage");

            // Populate UI with item details using binding
            binding.itemNameTextView.setText(itemName);
            binding.itemCountTextView.setText("Count: " + String.valueOf(itemCount));
            binding.itemDetailDescription.setText(itemDescription);
            binding.itemImageView.setImageResource(imageResId);
        }

        // Get ViewModel
        itemViewModel = new ViewModelProvider(requireActivity()).get(FirebaseManager.class);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        beCaught = firebaseManager.getBeCaughtFlag();
        Log.d("Item Detail", "onViewCreated: " + beCaught);

        if (!beCaught) {
            firebaseManager.fetchBeCaughtFlag(firebaseManager.getRoomId(), firebaseManager.getPlayerId());
            beCaught = firebaseManager.getBeCaughtFlag();
            Log.d("Item Detail", "onViewCreated: set becaught flag: " + beCaught);
        }

        binding.buttonCancel.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // TODO: call item function
        binding.useButton.setOnClickListener(v -> {

            if (!beCaught) {

            }
            Log.d("USE ITEM", "onViewCreated: check beCaught when click: " + beCaught);

            if (beCaught) {
                Toast.makeText(getContext(), "You are caught! You cannot use item!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (itemCount > 0) {
                itemCount = Math.max(itemCount - 1, 0);
                binding.itemCountTextView.setText(String.valueOf(itemCount));

                // Update the item count in Firestore
                itemViewModel.decreaseItemCount(itemViewModel.getPlayerId(), itemName, itemViewModel.getRoomId());
                userItem(itemName);
                Toast.makeText(getContext(), "Item used!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No item left!", Toast.LENGTH_SHORT).show();
            }


        });
    }

    private void userItem(String itemName) {

        if(itemName.toLowerCase().contains("decoy")) {
            itemViewModel.startDecoyWithTimer();
        }else if(itemName.toLowerCase().contains("invisible")) {
            itemViewModel.startInvisibleWithTimer();
        }
    }

    // Clean up the binding reference when the fragment is destroyed
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
