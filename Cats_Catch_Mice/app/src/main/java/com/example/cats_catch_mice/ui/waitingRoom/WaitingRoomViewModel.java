package com.example.cats_catch_mice.ui.waitingRoom;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class WaitingRoomViewModel extends ViewModel {

    private MutableLiveData<List<String>> players; // 玩家列表
    private MutableLiveData<Boolean> isGameStarted; // 游戏是否已经开始
    private DatabaseReference roomReference; // Firebase 房间引用

    public WaitingRoomViewModel() {
        players = new MutableLiveData<>();
        isGameStarted = new MutableLiveData<>(false);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        roomReference = firebaseDatabase.getReference("rooms");
    }

    // 获取玩家列表
    public LiveData<List<String>> getPlayers() {
        return players;
    }

    // 设置玩家列表
    public void setPlayers(List<String> newPlayers) {
        players.setValue(newPlayers);
    }

    // 获取游戏是否开始的状态
    public LiveData<Boolean> isGameStarted() {
        return isGameStarted;
    }

    // 设置游戏开始状态
    public void startGame() {
        isGameStarted.setValue(true);
        roomReference.child("gameStarted").setValue(true); // 更新 Firebase 房间状态
    }

    // 添加新玩家到房间
    public void addPlayer(String playerName) {
        List<String> currentPlayers = players.getValue();
        if (currentPlayers != null) {
            currentPlayers.add(playerName);
            players.setValue(currentPlayers);
            // 更新 Firebase 中的玩家列表
            roomReference.child("players").setValue(currentPlayers);
        }
    }

    // 删除玩家（当玩家退出房间时）
    public void removePlayer(String playerName) {
        List<String> currentPlayers = players.getValue();
        if (currentPlayers != null) {
            currentPlayers.remove(playerName);
            players.setValue(currentPlayers);
            // 更新 Firebase 中的玩家列表
            roomReference.child("players").setValue(currentPlayers);
        }
    }
}
