package com.example.cats_catch_mice;

import static android.content.ContentValues.TAG;

import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.security.SecureRandom;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cats_catch_mice.ui.itemList.Item;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FirebaseManager extends ViewModel {

    /*
    sync data to firebase
    这里只需要写入数据的子节点 rooms的数据进入database
    数据格式如下：

    "rooms": {
    "roomId12345": {
      
        "UUID12345":"owner": "UUID12345",
      "members": { {
          "lat": 37.7749,
          "lng": -122.4194,
          "item1": 0,
          "item2": 1
        },
        "UUID67890": {
          "lat": 37.7750,
          "lng": -122.4200,
          "item1": 2,
          "item2": 3
        }
      }
    }


      */
    private static final int CORE_THREADS = 5;
    private static final int MAX_THREADS = 10;
    private static final int THREAD_LIFE = 30;
    private static final int QUEUE_CAP = 10;
    private static final int MIN_NUM_ITEMS = 0;
    private static final int MAX_NUM_ITEMS = 2;
    private static final String ROOM_ID_PREFIX = "roomId";
    private static final int RANDOM_NUMBER_BOUND = 100000;
    private static final int DECOY_TIMER = 30000;
    private static final int INVISIBLE_TIMER = 20000;

    private final ThreadPoolExecutor executor;
    private SecureRandom secureRandom;
    private FirebaseDatabase database;
    private String roomId;
    private String playerId;
    private boolean isOwner;
    private String roomOwnerId;

    private double lastLat;
    private double lastLng;
    private boolean enableDecoy;
    private Pair<Double, Double> decoyPosition;

    private MutableLiveData<List<Item>> itemListLiveData = new MutableLiveData<>();;

    public FirebaseManager() {
        database = FirebaseDatabase.getInstance();
        executor = new ThreadPoolExecutor(
                CORE_THREADS,
                MAX_THREADS,
                THREAD_LIFE,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(QUEUE_CAP),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        secureRandom = new SecureRandom();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    public void printWholeDatabase() {

        Log.d(TAG, "printWholeDatabase function first log");
        DatabaseReference reference = database.getReference();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "in printWholeDatabase onDataChange");
                System.out.println(snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "in printWholeDatabase onCancelled");
                System.err.println("Failed to read data from Firebase.");
            }
        });
    }

    public void updateLocation(String playerId, double lat, double lng, String roomId){

        // set last known coordinate
        lastLat = lat;
        lastLng = lng;

        executor.execute(() -> {
            DatabaseReference memberRef = database.getReference("rooms").child(roomId).child("members").child(playerId);
            CompletableFuture<Map<String, Object>> future = getPlayerDataAsync(playerId, roomId);
            try{
                Map<String, Object> oldData = future.get();
                oldData.replace("lat", lat);
                oldData.replace("lng", lng);

                memberRef.setValue(oldData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("debugging", "Data written successfully to Firebase.");
                    } else {
                        Log.e("debugging", "Failed to write data to Firebase.");
                    }
                });
            }catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }catch (NullPointerException e){
                Log.e("debugging", "Null object reference");
            }
        });
    }

    public CompletableFuture<String> getRoomOwnerAsync(String roomId) {
        CompletableFuture<String> future = new CompletableFuture<>();

        DatabaseReference ownerRef = FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId)
                .child("owner");

        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String ownerId = snapshot.getValue(String.class);
                    future.complete(ownerId);
                } else {
                    future.complete(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Error when reading owner data"));
            }
        });
        return future;
    }

    public CompletableFuture<ArrayList<Pair<Double, Double>>> getLocations(String roomId){
        CompletableFuture<ArrayList<Pair<Double, Double>>> locationsFuture = new CompletableFuture<>();

        executor.execute(()-> {
            CompletableFuture<Map<String, Object>> future = getRoomDataAsync(roomId);
            try{
                Map<String, Object> membersData = future.get();
                Log.d("debugging", membersData.toString());

                ArrayList<Pair<Double, Double>> locations = new ArrayList<>();

                for(Map.Entry<String, Object> member: membersData.entrySet()){
                    Map<String, Object> memberData = (Map<String, Object>) member.getValue();

                    if (!(Boolean) memberData.get("visible")) continue;

                    Double lat = (Double) memberData.get("lat");
                    Double lng = (Double) memberData.get("lng");

                    Pair<Double, Double> location = new Pair<>(lat, lng);
                    locations.add(location);
                }
                locationsFuture.complete(locations);
            }catch (ExecutionException | InterruptedException e) {
                locationsFuture.completeExceptionally(e);
            }catch (Exception e){
                locationsFuture.completeExceptionally(e);
            }
        });

        return locationsFuture;
    }

    public void updateItemNum(String playerId, int number, String itemId, String roomId){
        DatabaseReference memberRef = database.getReference("rooms").child(roomId).child("members").child(playerId);
        CompletableFuture<Map<String, Object>> future = getPlayerDataAsync(playerId, roomId);
        try{
            Map<String, Object> oldData = future.get();
            oldData.replace(itemId, number);

            memberRef.setValue(oldData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    System.out.println("Data written successfully to Firebase.");
                } else {
                    System.err.println("Failed to write data to Firebase.");
                }
            });
        }catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void generatePlayerId() {
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        int randomFiveDigitNumber = (int) (Math.random() * 90000) + 10000;
        String playerId = "UUID" + randomId + randomFiveDigitNumber;
        setPlayerId(playerId);
    }

    public void addPlayerData(String playerId, double lat, double lng, int item1, int item2, boolean visible, String roomId) {
        DatabaseReference memberRef = database.getReference("rooms").child(roomId).child("members").child(playerId);

        // 直接写入数据到这个特定的 child
        Map<String, Object> memberData = new HashMap<>();
        memberData.put("lat", lat);
        memberData.put("lng", lng);
        memberData.put("item1", item1);
        memberData.put("item2", item2);
        memberData.put("visible", visible);
        memberData.put("beCaught", false);

        // 确保数据写入成功后调用
        memberRef.setValue(memberData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("Data written successfully to Firebase.");
            } else {
                System.err.println("Failed to write data to Firebase.");
            }
        });
    }

    public void updateMouseCaught(String roomId, String playerId) {
        executor.execute(() -> {
            DatabaseReference beCaughtRef = database.getReference("rooms")
                    .child(roomId)
                    .child("members")
                    .child(playerId)
                    .child("beCaught");

            beCaughtRef.setValue(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("FirebaseManager", "beCaught field updated to true for player: " + playerId);
                } else {
                    Log.e("FirebaseManager", "Failed to update beCaught field for player: " + playerId, task.getException());
                }
            });
        });

    }



    public CompletableFuture<Map<String, Object>> getPlayerDataAsync(String playerId, String roomId) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        // 获取指定的成员子节点
        DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId)
                .child("members")
                .child(playerId);

        // 监听 Firebase 中该成员的数据
        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> memberData = (Map<String, Object>) snapshot.getValue();
                    future.complete(memberData);  // 完成并返回数据
                    Log.d("getPlayerDataAsync", "future completed with memberData");
                } else {
                    future.complete(null);  // 没有数据时返回 null
                    Log.d("getPlayerDataAsync", "future completed with null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Failed to read data for userId " + playerId));
            }
        });

        return future;
    }

    public String createRoom(String ownerId) {
        UUID uuid = UUID.randomUUID();
        String surfixId = uuid.toString().replace("-", "").substring(0, 5);

        isOwner = true;

        roomId = ROOM_ID_PREFIX + surfixId;
        setFirebaseRoomData(roomId, ownerId);

        return roomId;
    }

    public void setFirebaseRoomData(String roomId, String roomOwnerId) {
        DatabaseReference roomRefs = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> memberData = new HashMap<>();
        memberData.put("lat", 0.0d);
        memberData.put("lng", 0.0d);
        memberData.put("item1", 0);
        memberData.put("item2", 0);
        memberData.put("visible", true);
        memberData.put("beCaught", false);

        Map<String, Object> members = new HashMap<>();
        members.put(roomOwnerId, memberData);

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("members", members);
        roomData.put("owner", roomOwnerId);

        roomRefs.child("rooms").child(roomId).setValue(roomData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("debugging", "Data written successfully to Firebase.");
            } else {
                Log.d("debugging", "Failed to write data to Firebase.");
            }
        });

    }

    /*
        Updates the item list LiveData
     */

    public LiveData<List<Item>> getItemListLiveData() {
        List<Item> itemList = new ArrayList<>();

//        dummy for testing
        String playerId = "UUID2018b95f70569";  // 预先存在的玩家ID
        String roomId = "roomIddummy1111";
//        TODO: null handling
        if (this.getPlayerId() == null ){
            Log.d("itemListLiveData", "Error: playerId is null");
        } else if ( this.getRoomId() == null){
            Log.d("itemListLiveData", "Error: roomId is null");
        }
        else{
            playerId = this.getPlayerId();
            roomId = this.getRoomId();
        }
        CompletableFuture<Map<String, Object>> future = getPlayerDataAsync(playerId, roomId);
        future.whenComplete((memberData, throwable) -> {
            if (throwable != null) {
                // Handle any errors, load dummy data if needed
                Log.d("itemListLiveData", "Failed to get player data, loading dummy data");
                itemList.add(new Item("Health Potion", "Sample description", 5, R.drawable.itemicon_item1_demo));
                itemList.add(new Item("Health Potion", "Sample description", 5, R.drawable.itemicon_item1_demo));
                itemList.add(new Item("Health Potion", "Sample description", 5, R.drawable.itemicon_item1_demo));
            } else {
                // Process memberData and populate the itemList
                Log.d("itemListLiveData", "Player data acquired, processing data");
                int item1Count = ((Number) memberData.getOrDefault("item1", 0)).intValue();
                int item2Count = ((Number) memberData.getOrDefault("item2", 0)).intValue();

                // TODO: Item names, descriptions and corresponding icons need to be manually editted here as there's only count data from firebase
                itemList.add(new Item("Invisible cloak", "Wear the invisible cloak for 20 seconds so that no one can see you!", item1Count, R.drawable.itemicon_item1_demo));
                itemList.add(new Item("Decoy", "Place a decoy at the current position. It will only last 30 seconds!", item2Count, R.drawable.mouse));
            }
            itemListLiveData.postValue(itemList);
        });
        return itemListLiveData;
    }


    public CompletableFuture<Map<String, Object>> getRoomDataAsync(String roomId) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId)
                .child("members");

        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> allMembersData = (Map<String, Object>) snapshot.getValue();
                    future.complete(allMembersData);
                } else {
                    future.complete(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Error when reading members data"));
            }
        });

        return future;
    }

    public void incrementItemCount(String playerId, String itemId, String roomId) {
        executor.execute(() -> {
            DatabaseReference itemRef = database.getReference("rooms")
                    .child(roomId)
                    .child("members")
                    .child(playerId)
                    .child(itemId);

            itemRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Integer currentValue = currentData.getValue(Integer.class);
                    if (currentValue == null) {
                        currentData.setValue(0);
                    } else {
                        currentData.setValue(Math.min(currentValue + 1, MAX_NUM_ITEMS));
                    }
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                    if (committed) {
                        Log.d("FirebaseManager", "Item count incremented successfully.");
                    } else {
                        Log.e("FirebaseManager", "Failed to increment item count.", databaseError.toException());
                    }
                }
            });
        });
    }

    public void decreaseItemCount(String playerId, String itemId, String roomId) {
        executor.execute(() -> {
            DatabaseReference itemRef = database.getReference("rooms")
                    .child(roomId)
                    .child("members")
                    .child(playerId)
                    .child(itemId);

            itemRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Integer currentValue = currentData.getValue(Integer.class);
                    if (currentValue == null) {
                        currentData.setValue(0);
                    } else {
                        currentData.setValue(Math.max(currentValue - 1, MIN_NUM_ITEMS));
                    }
                    return Transaction.success(currentData);
                }
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                    if (committed) {
                        Log.d("FirebaseManager", "Item count incremented successfully.");
                    } else {
                        Log.e("FirebaseManager", "Failed to increment item count.", databaseError.toException());
                    }
                }

            });
        });
    }

    public void setSelfVisibility(boolean visibility) {
        DatabaseReference itemRef = database.getReference("rooms")
                .child(this.roomId)
                .child("members")
                .child(this.playerId)
                .child("visible");

        itemRef.setValue(visibility).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Log.d("FirebaseManager", "Visible successfully set to false");
            }else {
                Log.d("FirebaseManager", "Failed to set visible to false");
            }
        });
    }


    /*
    debugging purpose only: check thread pool status
     */
    public void printThreadInfo(){
        String info = String.format("Active threads: %s -- Current pool size: %s -- Task count: %s -- ",
                executor.getActiveCount(),
                executor.getPoolSize(),
                executor.getTaskCount()
        );
        Log.d("debugging", info);
    }

    public void setOwnerFlag(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public Set<String> getAllExistingIdsFromDatabase() {
        DatabaseReference reference = database.getReference("rooms");
        Set<String> ids = null;
        try {
            DataSnapshot snapshot = reference.get().getResult();
            ids = (Set<String>) snapshot.getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    public CompletableFuture<DataSnapshot> getFullRoomDataAsync(String roomId) {
        CompletableFuture<DataSnapshot> future = new CompletableFuture<>();

        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomId);

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    future.complete(snapshot);
                } else {
                    future.completeExceptionally(new RuntimeException("Room data not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Error when reading room data", error.toException()));
            }
        });

        return future;
    }

    public void startDecoyWithTimer() {


        setDecoyPosition(new Pair<>(lastLat, lastLng));

        new Thread(() -> {
            // put decoy and start timer
            setDecoy(true);
            try {
                Thread.sleep(DECOY_TIMER);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e("debugging", "Thread was interrupted: " + e.getMessage());
            }
            // remove decoy when time's up
            setDecoy(false);
        }).start();
    }

    public void startInvisibleWithTimer() {

        new Thread(() -> {
            // put on invisible cloak and start timer
            setSelfVisibility(false);
            Log.d("debugging", "wear invisible");
            try {
                Thread.sleep(INVISIBLE_TIMER);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e("debugging", "Thread was interrupted: " + e.getMessage());
            }
            // take off the invisible cloak
            Log.d("debugging", "take off invisible");
            setSelfVisibility(true);
        }).start();
    }


    // setter
    public void setRoomId(String id){
        Log.d("FirebaseManager", "setRoomId to: " + id);
        this.roomId = id;
    }

    public void setPlayerId(String id){
        Log.d(TAG, "setPlayerId: " + id);
        this.playerId = id;
    }

    public void setDecoy(boolean flag) {
        this.enableDecoy = flag;
    }

    public boolean hasDecoy() {
        return this.enableDecoy;
    }

    public void setDecoyPosition(Pair<Double,Double> newPosition) {
        decoyPosition = new Pair<>(newPosition.first, newPosition.second);
    }

    // getter
    public String getRoomId(){
        Log.d("FirebaseManager", "getRoomId: " + roomId);
        return this.roomId;
    }

    public String getPlayerId(){
        return this.playerId;
    }

    public boolean getOwnerFlag() {
        return this.isOwner;
    }

    public boolean isOwner() {
        return this.isOwner;
    }

    public Pair<Double, Double> getDecoyPosition() {
        return new Pair<>(decoyPosition.first, decoyPosition.second);
    }

}
