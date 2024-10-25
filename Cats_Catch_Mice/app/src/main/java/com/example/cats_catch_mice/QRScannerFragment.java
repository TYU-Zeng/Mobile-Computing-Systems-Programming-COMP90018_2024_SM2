package com.example.cats_catch_mice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

public class QRScannerFragment extends Fragment {

    private CompoundBarcodeView barcodeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_q_r_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化相机预览
        barcodeView = view.findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                // 获取扫描结果
                if (result != null) {
                    String scannedData = result.getText();
                    Toast.makeText(getActivity(), "扫描结果: " + scannedData, Toast.LENGTH_LONG).show();

                    // 返回上一个界面并传递扫描结果
                    Bundle bundle = new Bundle();
                    bundle.putString("scanned_qr_code", scannedData);
                    getParentFragmentManager().setFragmentResult("requestKey", bundle);
                    getParentFragmentManager().popBackStack(); // 返回上一个fragment
                }
            }
        });
        barcodeView.resume();  // 启动相机预览
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();  // 暂停相机预览
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();  // 恢复相机预览
    }
}
