    package com.example.cats_catch_mice.ui.itemList;

    import static android.content.ContentValues.TAG;

    import android.os.Bundle;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.fragment.app.Fragment;
    import androidx.lifecycle.ViewModelProvider;
    import androidx.navigation.NavController;
    import androidx.navigation.Navigation;

    import com.example.cats_catch_mice.R;
    import com.example.cats_catch_mice.databinding.FragmentItemListBinding;


    public class ItemFragment extends Fragment {

        private FragmentItemListBinding binding;

        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            ItemListViewModel itemListViewModel =
                    new ViewModelProvider(this).get(ItemListViewModel.class);

            binding = FragmentItemListBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            binding.sampleItem1.setOnClickListener(v -> {
                // 使用 NavController 导航到 ItemDetailFragment
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Log.d(TAG, "navigate to item detail");
                navController.navigate(R.id.itemDetailFragment);
            });

            final TextView textView = binding.textDashboard;
            itemListViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
            return root;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }
    }