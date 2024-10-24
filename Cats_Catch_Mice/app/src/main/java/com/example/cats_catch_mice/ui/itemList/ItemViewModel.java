package com.example.cats_catch_mice.ui.itemList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import com.example.cats_catch_mice.R;

//public class ItemViewModel {
//}


public class ItemViewModel extends ViewModel {

    private MutableLiveData<List<Item>> itemListLiveData;

    public ItemViewModel() {
        itemListLiveData = new MutableLiveData<>();
        loadDummyData(); // Load dummy data for testing
    }

    // Expose LiveData to observe in the Activity/Fragment
    public LiveData<List<Item>> getItemListLiveData() {
        return itemListLiveData;
    }

    // Load dummy data for testing
    private void loadDummyData() {
        List<Item> itemList = new ArrayList<>();

        // Add some dummy items
        itemList.add(new Item("Health Potion", "Sample description", 5, R.drawable.itemicon_item1_demo));
        itemList.add(new Item("Health Potion", "Sample description", 5, R.drawable.itemicon_item1_demo));
        itemList.add(new Item("Health Potion", "Sample description", 5, R.drawable.itemicon_item1_demo));
//        itemList.add(new Item("Mana Potion", 3, "https://example.com/mana_potion.png"));
//        itemList.add(new Item("Sword", 1, "https://example.com/sword.png"));
//        itemList.add(new Item("Shield", 2, "https://example.com/shield.png"));

        // Post the dummy data to the LiveData
        itemListLiveData.postValue(itemList);
    }

    // Simulate using an item by decreasing its count
    public void useItem(String itemName, int newCount) {
        List<Item> currentList = itemListLiveData.getValue();
        if (currentList != null) {
            for (Item item : currentList) {
                if (item.getName().equals(itemName)) {
                    item.setCount(newCount); // Update the item count
                    break;
                }
            }
            itemListLiveData.postValue(currentList); // Update LiveData with new item list
        }
    }
}
