package com.example.cats_catch_mice;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;


public class NfcController {

    private Activity activity;

    // 构造函数，传入 Activity
    public NfcController(Activity activity) {
        this.activity = activity;
    }

    // 处理 NFC 标签的逻辑
    public void handleTag(Intent intent) {
        // 从 Intent 获取 NFC 标签
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            // 打印标签的唯一 ID（例如，标签的字节数组转换成十六进制字符串）
            byte[] tagId = tag.getId();
            Log.d("NFC", "Tag ID: " + bytesToHex(tagId));

            // 打印标签的技术列表
            String[] techList = tag.getTechList();
            for (String tech : techList) {
                Log.d("NFC", "Tag supports: " + tech);
            }

            // 提示用户已检测到标签
            Toast.makeText(activity, "NFC Tag detected!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("NFC", "No NFC tag found.");
        }
    }

    // 工具方法：将字节数组转换为十六进制字符串
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
