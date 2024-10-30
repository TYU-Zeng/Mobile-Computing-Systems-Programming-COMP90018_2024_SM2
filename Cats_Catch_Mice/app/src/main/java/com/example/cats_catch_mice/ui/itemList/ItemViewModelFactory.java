package com.example.cats_catch_mice.ui.itemList;

import com.example.cats_catch_mice.FirebaseManager;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ItemViewModelFactory implements ViewModelProvider.Factory {

    private final FirebaseManager firebaseManager;

    public ItemViewModelFactory(FirebaseManager firebaseManager) {
        this.firebaseManager = firebaseManager;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ItemViewModel.class)) {
            return (T) new ItemViewModel(firebaseManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
