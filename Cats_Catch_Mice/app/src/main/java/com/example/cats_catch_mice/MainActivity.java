package com.example.cats_catch_mice;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cats_catch_mice.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DatabaseReference databaseReference;
    private RoomManager roomManager;

    // NFC variables
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private NfcController nfcController;

    // current room id for map sharing
    private String currentRoomId = null;
    private Bitmap qrCodeBitmap = null;
    private String userId = null;
    private RoomData roomData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize NfcController
        nfcController = new NfcController(this);

        // Initialize NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            // Device doesn't support NFC
            Toast.makeText(this, "This device does not support NFC.", Toast.LENGTH_SHORT).show();
        } else if (!nfcAdapter.isEnabled()) {
            // NFC is disabled
            showNfcSettingsDialog();
        } else {
            // NFC is enabled
            // Create PendingIntent with appropriate flags
            int flags = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                flags = PendingIntent.FLAG_MUTABLE;
            } else {
                flags = 0;
            }

            pendingIntent = PendingIntent.getActivity(
                    this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    flags
            );
        }



        BottomNavigationView navView = findViewById(R.id.nav_view);
        // set navigation detail
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        // set AppBarConfiguration
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)
                .build();
        // set ActionBar and NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        FirebaseManager firebaseManager = new FirebaseManager();

    }

    /**
     * Create top menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }


    /**
     * Menu item click event
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_createRoom) {

        } else if (item.getItemId() == R.id.menu_scanner) {
            // 点击后跳转到 QRScannerFragment
            Log.d(TAG, "onOptionsItemSelected: join room button clicked");
            // TODO: QRScannerFragment 创建实例
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.action_global_navigation_scanner);
            // toast
            Toast.makeText(this, "Join Room clicked", Toast.LENGTH_SHORT).show();



        }else if (item.getItemId() == R.id.menu_quit) {
            // TODO: click "Quit" function
            Toast.makeText(this, "Quit clicked", Toast.LENGTH_SHORT).show();

            // TODO: double check 是否退出
            // 如果退出直接roommanager.leaveRoom(currentRoomId, userId);
            // 停止所有的thread
            // 删除roomid
            // 跳转到初始化面

        }
        return true;
    }

    /**
     * Click event for button
     *
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                // NFC is disabled
                showNfcSettingsDialog();
            } else {
                // Enable foreground dispatch
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null) {
            // Disable foreground dispatch
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    // Handle NFC intents
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && (
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) ||
                        NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) ||
                        NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())
        )) {
            Log.d("NFC", "NFC Tag discovered!");
            nfcController.handleTag(intent);
        }
    }

    private void showNfcSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("NFC is disabled")
                .setMessage("NFC is required to use this feature. Do you want to enable NFC?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Open NFC settings
                    startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User chose not to enable NFC
                    Toast.makeText(MainActivity.this, "NFC is required for this feature.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

}