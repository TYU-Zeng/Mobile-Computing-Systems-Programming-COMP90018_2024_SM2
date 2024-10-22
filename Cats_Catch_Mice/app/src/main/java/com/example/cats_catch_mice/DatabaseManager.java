package com.example.cats_catch_mice;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class DatabaseManager{

    private final FirebaseDatabase database;

    public DatabaseManager() {
        this.database = FirebaseDatabase.getInstance();
    }


    // TODO: only update the database
    public void writeData(String path, Object data) {
        DatabaseReference reference = database.getReference(path);
        reference.setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("Data written successfully to: " + path);
            } else {
                System.err.println("Failed to write data to: " + path);
            }
        });
    }


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
    private void syncRoomDataToFirebase(String roomId, RoomData roomData) {

    }


}
