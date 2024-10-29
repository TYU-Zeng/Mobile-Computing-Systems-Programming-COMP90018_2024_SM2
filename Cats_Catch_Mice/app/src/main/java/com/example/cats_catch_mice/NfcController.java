package com.example.cats_catch_mice;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.content.Intent;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;


public class NfcController {

    private Activity activity;
    private String playerId;
    private String roomId;
    private FirebaseManager firebaseManager;


    // 构造函数，传入 Activity
    public NfcController(Activity activity, String playerId, FirebaseManager firebaseManager, String roomId) {
        this.activity = activity;
        this.playerId = playerId;
        this.roomId = roomId;
        this.firebaseManager = firebaseManager;
    }

    // 处理 NFC 标签的逻辑
    public void handleTag(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            Log.d("NFC", "handleTag: fubd tag");
            // toast
            readFromTag(tag);
        } else {
            Log.d("NFC", "No NFC tag found.");
        }
    }

    private void readFromTag(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            try {
                ndef.connect();
                NdefMessage ndefMessage = ndef.getNdefMessage();
                if (ndefMessage != null) {
                    NdefRecord[] records = ndefMessage.getRecords();
                    for (NdefRecord record : records) {
                        if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                                Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                            String itemId = readText(record);
                            Toast.makeText(activity, "Read content: " + itemId, Toast.LENGTH_SHORT).show();

                            firebaseManager.incrementItemCount(playerId, itemId, roomId);

                            Log.d("NFC", "Read content: " + itemId);
                        }
                    }
                }
                ndef.close();
            } catch (Exception e) {
                Log.e("NFC", "Failed to read NDEF message.", e);
            }
        } else {
            Log.e("NFC", "Tag doesn't support NDEF.");
        }
    }


    private String readText(NdefRecord record) throws UnsupportedEncodingException {
        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0x3F;

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

    // 工具方法：将字节数组转换为十六进制字符串
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public void writeTag(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {

            Log.d("NfcController", "writeTag: trying to write tag");
            // Create an NDEF record with the text data
            NdefRecord ndefRecord = NdefRecord.createTextRecord(null, "item2");

            // Create an NDEF message containing the record
            NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});

            // Write the NDEF message to the tag
            writeNdefMessage(tag, ndefMessage);

            Toast.makeText(activity, "NFC Tag written!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("NFC", "No NFC tag found.");
        }
    }

    // Helper method to write NDEF message to the tag
    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage) {
        try {
            Ndef ndef = Ndef.get(tag);

            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    Toast.makeText(activity, "NFC tag is not writable!", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;
                }

                int size = ndefMessage.toByteArray().length;
                if (ndef.getMaxSize() < size) {
                    Toast.makeText(activity, "NFC tag doesn't have enough space.", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
            } else {
                // Tag is not NDEF formatted, try to format it
                NdefFormatable ndefFormatable = NdefFormatable.get(tag);
                if (ndefFormatable != null) {
                    try {
                        ndefFormatable.connect();
                        ndefFormatable.format(ndefMessage);
                        ndefFormatable.close();
                    } catch (Exception e) {
                        Log.e("NFC", "Failed to format tag.", e);
                    }
                } else {
                    Log.e("NFC", "Tag doesn't support NDEF.");
                }
            }
        } catch (Exception e) {
            Log.e("NFC", "Failed to write NDEF message.", e);
        }
    }



}
