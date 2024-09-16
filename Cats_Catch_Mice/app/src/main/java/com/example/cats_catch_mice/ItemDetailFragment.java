package com.example.cats_catch_mice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cats_catch_mice.databinding.FragmentItemDetailBinding;

public class ItemDetailFragment extends Fragment {

    private FragmentItemDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItemDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);





        // 设置其他按钮点击事件
        binding.buttonCancel.setOnClickListener(v -> {
            // 自定义返回逻辑
            ((AppCompatActivity) requireActivity()).getSupportFragmentManager().popBackStack();


        });

        binding.buttonUse.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Item used!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
