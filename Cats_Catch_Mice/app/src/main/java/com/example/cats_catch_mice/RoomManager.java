package com.example.cats_catch_mice;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RoomManager {


    private RoomData roomData;

    public RoomManager(RoomData roomData) {

        this.roomData = roomData;
    }



    // 创建房间
    public String createRoom(double lat, double lng, String ownerId) {
        // 生成房间 ID
        String roomId = UUID.randomUUID().toString();


    // 创建一个新的 RoomData 对象，并设置房主 ID
        RoomData roomData = new RoomData(ownerId);

        // 添加房主为房间成员，并设置其位置信息
        Map<String, Object> ownerData = new HashMap<>();
        ownerData.put("lat", lat);
        ownerData.put("lng", lng);
        roomData.addMember(ownerId, ownerData);

        // 将房间数据写入 Firebase

        // 需要返回data
        // TODO: 返回roomdata
        return roomId;
    }


    // TODO: 退出房间的 function
    public void leaveRoom(String roomId, String userId) {
        roomData.removeMember(userId);
    }



    // create a QR code for the room
    // TODO: 还没有展示二维码的功能 UI
    public Bitmap createQRCode(String roomId) {
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
