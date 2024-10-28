// QRCodeFragment.java
package com.example.cats_catch_mice;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRTest extends Fragment {

    private static final String ARG_ROOM_ID = "room_id";
    private String roomId;

    public QRTest() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomId = getArguments().getString("room_id");
            // If using Safe Args:
            // QRTestArgs args = QRTestArgs.fromBundle(getArguments());
            // roomId = args.getRoomId();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_q_r_test, container, false);

        ImageView qrCodeImageView = view.findViewById(R.id.qrCodeImageView);

        if (roomId != null) {
            Bitmap qrCodeBitmap = createQRCode(roomId);
            if (qrCodeBitmap != null) {
                qrCodeImageView.setImageBitmap(qrCodeBitmap);
            }
        }

        return view;
    }

    private Bitmap createQRCode(String roomId) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(roomId, BarcodeFormat.QR_CODE, 500, 500);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bmp;

        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }
}

