package com.example.cats_catch_mice;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cats_catch_mice.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        //set top menu
        //setContentView(R.layout.activity_main);



        // 找到ImageView
        ImageView imageView = findViewById(R.id.sample_item1);

        // 设置点击事件监听器
        imageView.setOnClickListener(v -> {
            // 在点击事件中启动新的Activity
            Intent intent = new Intent(MainActivity.this, ItemDetail.class);
            startActivity(intent);
        });
    }


    //
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 创建菜单的文件和 指定菜单项添加到哪个菜单上
        getMenuInflater().inflate(R.menu.top_menu, menu);
        // 允许显示菜单
        return true;
    }
    //
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId() == R.id.menu_quit) {
            // 用户点击了 "Quit"
            Toast.makeText(this, "Quit clicked", Toast.LENGTH_SHORT).show();

        } else if (item.getItemId() == R.id.menu_about) {
            // 用户点击了 "About"
            Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show();

        }
        return true;

    }


}