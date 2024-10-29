package com.example.cats_catch_mice;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

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
import java.util.Map;
import java.util.Set;
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
      "owner": "UUID12345",
      "members": {
        "UUID12345": {
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

    private FirebaseDatabase database;
    private final ThreadPoolExecutor executor;
    private static final int CORE_THREADS = 5;
    private static final int MAX_THREADS = 10;
    private static final int THREAD_LIFE = 30;
    private static final int QUEUE_CAP = 10;

    private static final int MAX_NUM_ITEMS = 2;

    private String roomId;
    private String playerId;

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

    public void addPlayerData(String playerId, double lat, double lng, int item1, int item2, String roomId) {
        DatabaseReference memberRef = database.getReference("rooms").child(roomId).child("members").child(playerId);

        // 直接写入数据到这个特定的 child
        Map<String, Object> memberData = new HashMap<>();
        memberData.put("lat", lat);
        memberData.put("lng", lng);
        memberData.put("item1", item1);
        memberData.put("item2", item2);

        // 确保数据写入成功后调用
        memberRef.setValue(memberData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("Data written successfully to Firebase.");
            } else {
                System.err.println("Failed to write data to Firebase.");
            }
        });
    }

    /*
    // 定义一个 memberData 变量来存储异步结果
    Map<String, Object> memberData = new HashMap<>();

    // 调用异步方法，并使用 thenAccept 来接住结果
    getPlayerDataAsync("UUID12345", "owner123", "roomId12345").thenAccept(data -> {
        if (data != null) {
            memberData.putAll(data);  // 将异步获取的数据存入 memberData
            System.out.println("Member data: " + memberData);
        } else {
            System.out.println("Member data not found.");
        }
    });

    // 注意：此时 memberData 可能还没有赋值完成，因为这是异步操作

     */
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
                } else {
                    future.complete(null);  // 没有数据时返回 null
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Failed to read data for userId " + playerId));
            }
        });

        return future;
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
                        currentData.setValue(1);
                    } else {
                        currentData.setValue(currentValue + 1);
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

    // setter
    public void setRoomId(String id){
        Log.d("FirebaseManager", "setRoomId to: " + id);
        this.roomId = id;
    }

    public void setPlayerId(String id){
        this.playerId = id;
    }

    // getter
    public String getRoomId(){
        Log.d("FirebaseManager", "getRoomId: " + roomId);
        return this.roomId;
    }

    public String getPlayerId(){
        return this.playerId;
    }


}
