package com.example.cats_catch_mice;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class FirebaseManagerTest {

    private FirebaseManager firebaseManager;
    private Context context;

    @Before
    public void setUp() throws Exception {
        // 获取 Android 上下文
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // 初始化 Firebase
        FirebaseApp.initializeApp(context);
        firebaseManager = new FirebaseManager();

    }

    @Test
    public void addPlayerData() throws InterruptedException {
        Log.d(TAG, "In addPlayerData function: start");
        String playerId = "UUID11111";
        String roomId = "roomId12345";
        double lat = 37.7750;
        double lng = -122.4200;
        int item1 = 10;
        int item2 = 22;

        // firebaseManager.printWholeDatabase();


        // 使用 CountDownLatch 等待异步操作完成
        CountDownLatch latch = new CountDownLatch(1);

        firebaseManager.addPlayerData(playerId, lat, lng, item1, item2, roomId);

        // 确保数据写入成功并等待完成
        CompletableFuture<Map<String, Object>> future = firebaseManager.getPlayerDataAsync(playerId, roomId);
        future.thenAccept(memberData -> {
            // print memberData
            Log.d(TAG, "In addplayerData function: in Accept and check the memberData");
            System.out.println("memberData: " + memberData);

            latch.countDown(); // 通知操作完成
        });

        latch.await(); // 等待写入完成
    }




    @Test
    public void getPlayerDataAsync() {

        Log.d(TAG, "getPlayerDataAsync: start");
        String playerId = "UUID12345";  // 预先存在的玩家ID
        String roomId = "roomId12345";  // 房间ID

        // 调用异步方法获取玩家数据
        CompletableFuture<Map<String, Object>> future = firebaseManager.getPlayerDataAsync(playerId, roomId);
        try {
            Log.d(TAG, "getPlayerDataAsync: in try, waiting for the future");
            Map<String, Object> memberData = future.get();  // 等待异步结果
            assertNotNull("Member data should not be null", memberData);
            assertEquals(37.7749, memberData.get("lat"));
            assertEquals(-122.4194, memberData.get("lng"));
            assertEquals(0, memberData.get("item1"));
            assertEquals(1, memberData.get("item2"));
        } catch (ExecutionException | InterruptedException e) {
            fail("Failed to fetch data: " + e.getMessage());
        }
    }
}