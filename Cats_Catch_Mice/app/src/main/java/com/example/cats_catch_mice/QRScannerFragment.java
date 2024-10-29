package com.example.cats_catch_mice;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

public class QRScannerFragment extends Fragment {

    private CompoundBarcodeView barcodeView;
    private String roomId;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_q_r_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize camera preview
        barcodeView = view.findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null) {
                    String scannedData = result.getText();
                    if (scannedData != null && scannedData.startsWith("roomId")) {

                        Log.d("Scanner" ,"barcodeResult: " + scannedData);
                        roomId = scannedData;


                        Bundle bundle = new Bundle();
                        bundle.putString("scanned_qr_code", scannedData);

                        getParentFragmentManager().setFragmentResult("requestKey", bundle);

                        NavController navController = NavHostFragment.findNavController(QRScannerFragment.this);
                        navController.navigateUp();

                    } else {
                        // Show an error message
                        Toast.makeText(getActivity(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
                    }

                    // Return to the previous fragment and pass the scan result
                    Bundle bundle = new Bundle();
                    bundle.putString("scanned_qr_code", scannedData);
                    getParentFragmentManager().setFragmentResult("requestKey", bundle);

                    // Navigate back using NavController
                    NavController navController = NavHostFragment.findNavController(QRScannerFragment.this);
                    navController.navigateUp();
                }
            }
        });
        barcodeView.resume();  // Start camera preview
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();  // Pause camera preview
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();  // Resume camera preview
    }
}

