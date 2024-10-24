package com.example.cats_catch_mice;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Intent;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jetbrains.annotations.Nullable;


public class QRScannerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_q_r_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 初始化二维码扫描功能
        initScanner();
    }

    private void initScanner() {
        // 使用 ZXing 初始化二维码扫描
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR Code");
        integrator.setCameraId(0); // 使用后置摄像头
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    // 处理扫描结果
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getActivity(), "取消扫描", Toast.LENGTH_LONG).show();
            } else {
                // 处理扫描得到的结果
                String scannedData = result.getContents();
                Toast.makeText(getActivity(), "扫描结果: " + scannedData, Toast.LENGTH_LONG).show();

                // 可以在此处理扫描结果，例如跳转到其他界面或返回数据
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}