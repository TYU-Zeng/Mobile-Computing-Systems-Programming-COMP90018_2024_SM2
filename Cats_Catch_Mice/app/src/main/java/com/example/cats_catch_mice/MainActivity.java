package com.example.cats_catch_mice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cats_catch_mice.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DatabaseReference databaseReference;
    private RoomManager roomManager;




    // current room id for map sharing
    private String currentRoomId = null;
    private Bitmap qrCodeBitmap = null;
    private String userId = null;
    private RoomData roomData = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        //TODO: 创建所有的manager实例和database实例
        // initial Firebase
        FirebaseApp.initializeApp(this);
        initialFirebase("example_reference");

        roomManager = new RoomManager(roomData);


        // TODO: generate user id
        // TODO: get the data from firebase
        // 先从数据库拿出数据 等待选择是创建房间或者加入房间 等待房间id被设置好

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // set navigation detail
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        // set AppBarConfiguration
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)
                .build();
        // set ActionBar and NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


        // test Firebase -----------------------------------
        Button writeButton = findViewById(R.id.writeButton);
        writeButton.setOnClickListener(v -> {
            writeToFirebase("example_reference", "Hello, Firebase!");
        });




    }

    /**
     * Create top menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }


    /**
     * Menu item click event
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_quit) {
            // TODO: click "Quit" function
            Toast.makeText(this, "Quit clicked", Toast.LENGTH_SHORT).show();

            // TODO: double check 是否退出
            // 如果退出直接roommanager.leaveRoom(currentRoomId, userId);
            // 停止所有的thread
            // 删除roomid
            // 跳转到初始化面

        } else if (item.getItemId() == R.id.menu_createRoom) {


            //TODO: 创建房间的逻辑
            //检测房间id是否为空

            //如果房间id不是空的
            //检测二维码是不是空的
            //二维码是空的 就用房间id生成二维码 并且跳转到展示二维码的activity
            //二维码不是空的 就直接跳转到展示二维码的activity

            //如果房间id是空的
            //创建房间，跳转到展示二维码的activity



//            // 如果当前房间 ID 为空
//            if (currentRoomId == null) {
//
//
//                // 获取当前设备的位置信息（假设是硬编码值）
//                double currentLat = 37.7749;
//                double currentLng = -122.4194;
//
//                // 创建房间并获取房间 ID
//                currentRoomId = roomManager.createRoom(currentLat, currentLng, userId);
//
//                // 写入房间数据到 Firebase
//                writeRoomToFirebase(currentRoomId, currentLat, currentLng);
//
//                // 生成二维码
//                qrCodeBitmap = roomManager.createQRCode(currentRoomId);roomManager.createQRCode(currentRoomId);
//                Toast.makeText(MainActivity.this, "Room created with ID: " + currentRoomId, Toast.LENGTH_SHORT).show();
//
//
//            } else {
//                // 如果已经创建了房间
//                Toast.makeText(MainActivity.this, "Room already created with ID: " + currentRoomId, Toast.LENGTH_SHORT).show();
//            }
        }
        return true;
    }

    /**
     * Click event for button
     *
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }


    // test Firebase
    private void initialFirebase(String referenceName){
        FirebaseDatabase firebaseDatabase =  FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(referenceName);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String inputText = snapshot.getValue(String.class);
                binding.messageDisplay.setText(inputText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // test Firebase
    // simple text write to Firebase
    private void writeToFirebase(String referenceName, String data) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference(referenceName);

        // 写入数据
        databaseReference.setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Data written successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed to write data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // test Firebase
    // 写入房间信息到 Firebase
    private void writeRoomToFirebase(String roomId, double lat, double lng) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference roomRef = firebaseDatabase.getReference("rooms");

        // 创建房间数据
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("owner", "YourOwnerIdHere");
        roomData.put("lat", lat);
        roomData.put("lng", lng);

        // 将房间数据写入 Firebase
        roomRef.child(roomId).setValue(roomData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Room data written to Firebase!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed to write room data.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}