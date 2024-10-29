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

    private Bitmap qrCodeBitmap = null;

    public RoomManager() {

    }

     /*
    sync data to firebase
    这里只需要写入数据的子节点 rooms的数据进入database
    数据格式如下：

    "rooms": {
    "roomId12345": {
      "owner": "UUID12345",
      "members": {
        "UUID12345": {
          "lat": 37.7749,
          "lng": -122.4194,
          "item1": 0,
          "item2": 1
        },
        "UUID67890": {
          "lat": 37.7750,
          "lng": -122.4200,
          "item1": 2,
          "item2": 3
        }
      }
    }


      */
    // 创建房间 返回当前房间的id给主机
    // 需要主机的位置信息



    public String createRoom(String ownerId) {
        // 生成房间 ID
        this.roomData = new RoomData(ownerId);
        String uuidPart = UUID.randomUUID().toString().substring(0, 8);

        // 生成一个 5 位随机数字
        int randomFiveDigitNumber = (int) (Math.random() * 90000) + 10000;

        // 将 UUID 和 5 位数字组合成最终的 roomId
        String roomId = "roomId" + uuidPart + randomFiveDigitNumber;

        // 添加房主为房间成员，并设置其位置信息
        // TODO: get current location
        // ownerData.put("lat", lat);
        // ownerData.put("lng", lng);

        Map<String, Object> ownerData = createMember(ownerId, 0, 0);
        roomData.addMember(ownerId, ownerData);
        // 写入数据到房间

        // 生成二维码
        Bitmap qrCode = createQRCode(roomId);

        return roomId;
    }

    public void joinRoom(RoomData file, String playerId) {
        // 添加用户为房间成员，并设置其位置信息
        this.roomData = file;
        Map<String, Object> memberData = createMember(playerId, 0, 0);
        roomData.addMember(playerId, memberData);
    }

    private Map<String, Object> createMember(String userId, double lat, double lng) {
        Map<String, Object> memberData = new HashMap<>();
        memberData.put("lat", lat);
        memberData.put("lng", lng);
        memberData.put("item1", 0);
        memberData.put("item2", 0);
        return memberData;
    }

    public void leaveRoom(String userId) {
        roomData.removeMember(userId);

        // TODO: 当前主机需要关掉线程 然后删除主机的房间id信息，重置roomdata信息

        // TODO: 如果主机要退出房间
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

    public Bitmap getQrCodeBitmap() {
        return qrCodeBitmap;
    }
}
