package com.example.cats_catch_mice.ui.waitingRoom;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.cats_catch_mice.DatabaseManager;
import com.example.cats_catch_mice.R;
import com.example.cats_catch_mice.databinding.FragmentWaitingRoomBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class WaitingRoomFragment extends Fragment {

    private static final String LOG_TAG = "WaitingRoomFragment";
    private FragmentWaitingRoomBinding binding;
    private boolean isHost; // 表示房主状态的标记
    private DatabaseManager databaseManager;
    private Handler handler = new Handler(Looper.getMainLooper());

    // 玩家动物和名字数组
    private String[] animals = {"Cat", "Dog", "Rabbit", "Mouse"};
    private String[] names = {"Alice", "Bob", "Charlie", "Dave"};

    // QR Code 相关
    private Bitmap qrCodeBitmap;
    private ImageView qrCodeImageView;

    // Firebase
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWaitingRoomBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        View view = null;

        databaseManager = new ViewModelProvider(requireActivity()).get(DatabaseManager.class);

        // 初始化 Firebase 参考
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("rooms");

        // 获取 QR Code 的 ImageView
        qrCodeImageView = binding.qrCodeImage;

        // 根据是房主还是玩家显示不同 UI
        isHost = checkIfHost(); // 你可以自己实现 checkIfHost() 方法来判断是否为房主
        if (isHost) {
            binding.startGameButton.setVisibility(View.VISIBLE); // 房主才显示开始游戏按钮
            binding.startGameButton.setOnClickListener(v -> startGame());
        } else {
            binding.startGameButton.setVisibility(View.GONE); // 非房主隐藏按钮
        }

        // 显示二维码（模拟生成的二维码）
        displayQRCode();

        // 随机分配动物和名字
        String randomAnimal = getRandomAnimal();
        String randomName = getRandomName();
        Toast.makeText(getContext(), "You are " + randomAnimal + " named " + randomName, Toast.LENGTH_SHORT).show();

        return root;
    }

    // 模拟生成二维码的函数（你可以用 ZXing 或其他库生成二维码）
    private void displayQRCode() {
        qrCodeBitmap = generateQRCode(); // 这里调用二维码生成函数
        qrCodeImageView.setImageBitmap(qrCodeBitmap);
    }

    // 开始游戏逻辑，房主点击后通知所有玩家游戏开始
    private void startGame() {
        Log.d(LOG_TAG, "Game started by host");
        databaseReference.child("rooms").child("gameStarted").setValue(true); // 通知 Firebase 房间状态
        navigateToMap(); // 导航到地图界面
    }

    // 当房主点击开始游戏时，跳转到地图界面
    private void navigateToMap() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.action_waitingRoomFragment_to_homeFragment);
    }

    // 获取随机动物
    private String getRandomAnimal() {
        Random random = new Random();
        return animals[random.nextInt(animals.length)];
    }

    // 获取随机名字
    private String getRandomName() {
        Random random = new Random();
        return names[random.nextInt(names.length)];
    }

    // 模拟生成二维码的函数，你可以使用二维码生成库，如 ZXing
    private Bitmap generateQRCode() {
        // 使用第三方库生成二维码的逻辑，比如 ZXing
        // 这里只是一个占位符函数，需要你实现
        return Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888); // 模拟的空白二维码
    }

    // destory View
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // 模拟判断是否是房主的方法
    private boolean checkIfHost() {
        // 这里可以根据业务逻辑判断当前用户是否为房主
        return true; // 暂时假设当前用户是房主
    }
}
