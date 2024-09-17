package com.example.cats_catch_mice;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.ActionBar;
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
        // set navigation detail
        // 设置导航控制器和 AppBarConfiguration
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // 配置 AppBarConfiguration，使其处理顶级目的地（导航图中的顶层目的地，不会显示返回按钮）
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)  // 顶级目的地
                .build();

        // 设置 ActionBar 与 NavController 关联
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // 绑定 BottomNavigationView 和 NavController
        NavigationUI.setupWithNavController(binding.navView, navController);
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


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }


}