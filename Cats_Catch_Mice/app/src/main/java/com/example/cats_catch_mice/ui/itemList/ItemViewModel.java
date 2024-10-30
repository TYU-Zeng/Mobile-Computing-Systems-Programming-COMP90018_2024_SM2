package com.example.cats_catch_mice.ui.itemList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import static android.content.ContentValues.TAG;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;



import com.example.cats_catch_mice.FirebaseManager;
import com.example.cats_catch_mice.R;

//public class ItemViewModel {
//}


public class ItemViewModel extends ViewModel {

    private static final String LOG_TAG = "ItemViewModel";
    private MutableLiveData<List<Item>> itemListLiveData;

    private final FirebaseManager firebaseManager;

    public ItemViewModel(FirebaseManager firebaseManager) {
        this.firebaseManager = firebaseManager;
        itemListLiveData = new MutableLiveData<>();
//        loadDummyData(); // Load dummy data for testing
        loadItemData();
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

    private void loadItemData(){

        List<Item> itemList = new ArrayList<>();

        String playerId = "UUID12345";  // 预先存在的玩家ID
        String roomId = "roomId12345";

//        assert firebaseManager != null;
        if (firebaseManager.getPlayerId() == null ){
            Log.d(LOG_TAG, "Error: PlayerId is null, using 12345");
        } else if ( firebaseManager.getRoomId() == null){
            Log.d(LOG_TAG, "Error: RoomId is null, using 12345");
        }
        else{
            playerId = firebaseManager.getPlayerId();  // 预先存在的玩家ID
            roomId = firebaseManager.getRoomId();
        }
        CompletableFuture<Map<String, Object>> future = firebaseManager.getPlayerDataAsync(playerId,roomId);
        try {
            Log.d(LOG_TAG, "getPlayerDataAsync: in try, waiting for the future");
            Map<String, Object> memberData = future.get();  // 等待异步结果
            Log.d(LOG_TAG, "item data accquired");
            int item1Count = (int) memberData.get("item1");
            int item2Count = (int) memberData.get("item2");
            itemList.add(new Item("item1", "item1 description", item1Count, R.drawable.itemicon_item1_demo));
            itemList.add(new Item("item2", "item2 description", item2Count, R.drawable.mouse));
            itemListLiveData.postValue(itemList);
        } catch (ExecutionException | InterruptedException e) {
            Log.d(LOG_TAG, "failed to get player data, load dummy data");
            loadDummyData();
        }

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
