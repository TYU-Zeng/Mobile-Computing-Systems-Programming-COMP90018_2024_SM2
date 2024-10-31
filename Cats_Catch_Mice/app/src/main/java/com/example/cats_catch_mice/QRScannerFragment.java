package com.example.cats_catch_mice;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

public class QRScannerFragment extends Fragment {

    private CompoundBarcodeView barcodeView;
    private FirebaseManager firebaseManager;
    private boolean cameraPermissionGranted = false;
    private ActivityResultLauncher<String> cameraPermissionLauncher;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化 FirebaseManager
        firebaseManager = new ViewModelProvider(requireActivity()).get(FirebaseManager.class);

        // 设置权限请求的结果处理器
        setCameraPermissionLauncher();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_q_r_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化 BarcodeView
        barcodeView = view.findViewById(R.id.barcode_scanner);

        // 检查并请求相机权限
        getCameraPermission();
    }

    private void initializeScanner() {
        // 配置 BarcodeView
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null) {
                    String scannedData = result.getText();
                    if (scannedData != null && scannedData.startsWith("roomId")) {

                        Log.d("Scanner", "barcodeResult: " + scannedData);

                        // 设置玩家的房间ID
                        firebaseManager.setRoomId(scannedData);

                        Log.d("Scanner", "barcodeResult: firebaseManager set room id");

                        firebaseManager.addPlayerData(firebaseManager.getPlayerId(), 0d, 0d, 0, 0, true, scannedData);
                        Toast.makeText(getContext(), "Joined room. Room Id: " + scannedData.substring(6), Toast.LENGTH_SHORT).show();
                        firebaseManager.setOwnerFlag(false);
                        NavController navController = NavHostFragment.findNavController(QRScannerFragment.this);
                        navController.navigateUp();

                    } else {
                        // 显示错误信息
                        Toast.makeText(getActivity(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
                    }

                    // 使用 NavController 返回上一界面
                    NavController navController = NavHostFragment.findNavController(QRScannerFragment.this);
                    navController.navigateUp();
                }
            }
        });

        barcodeView.resume();  // 启动相机预览
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();  // 暂停相机预览
        }  // Pause camera preview
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeView != null && cameraPermissionGranted) {
            barcodeView.resume();  // 恢复相机预览
        }  // Resume camera preview
    }

    private void getCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraPermissionGranted = true;
            initializeScanner();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void setCameraPermissionLauncher() {
        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                cameraPermissionGranted = true;
                initializeScanner();
            } else {
                cameraPermissionGranted = false;
                showCameraRequireDialog();
            }
        });
    }

    private void showCameraRequireDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Camera Permission Required")
                .setMessage("This app needs camera permission to scan QR codes. Please grant the permission.")
                .setPositiveButton("Go to Settings", (dialog, which) -> openAppSettings())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    // 可以在此处理用户拒绝权限的情况，例如返回上一界面
                    NavController navController = NavHostFragment.findNavController(QRScannerFragment.this);
                    navController.navigateUp();
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
        startActivity(intent);
    }



}

