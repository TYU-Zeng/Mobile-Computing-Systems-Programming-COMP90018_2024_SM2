package com.example.cats_catch_mice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.google.zxing.ResultPoint;
import java.util.List;

public class QrScannerFragment extends Fragment {

    private DecoratedBarcodeView barcodeScannerView;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                String qrCodeValue = result.getText();
                // Handle the scanned QR code
                Toast.makeText(getActivity(), "QR Code: " + qrCodeValue, Toast.LENGTH_LONG).show();

                // Optionally, navigate back to the previous fragment
                // or perform any action with the QR code value
                // For example, communicate with MainActivity
                ((MainActivity) getActivity()).onQrCodeScanned(qrCodeValue);

                // Pop the fragment from the back stack to return to previous screen
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // Optional: handle possible result points
        }
    };

    public QrScannerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_qr_scanner, container, false);

        // Initialize the barcode scanner view
        barcodeScannerView = view.findViewById(R.id.barcode_scanner);
        barcodeScannerView.decodeContinuous(callback);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }
}
