package com.example.a_say_it;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    // 각 fragment 클래스의 변수 선언
    PronounceFragment Fragment1;
    WordsFragment Fragment2;
    MyWordsFragment Fragment3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fragment1 = new PronounceFragment();
        Fragment2 = new WordsFragment();
        Fragment3 = new MyWordsFragment();

        // 하단바 메뉴 변수 선언
        BottomNavigationView bottomMenu = findViewById(R.id.bottom_menu);

        // 기본 시작 화면 지정
        getSupportFragmentManager().beginTransaction().replace(R.id.container, Fragment1).commit();
        bottomMenu.setSelectedItemId(R.id.menu_pronounce);
        bottomMenu.setItemIconTintList(null);

        bottomMenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_pronounce:
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, Fragment1).commit();
                                return true;
                            case R.id.menu_randWords:
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, Fragment2).commit();
                                return true;
                            case R.id.menu_myWords:
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, Fragment3).commit();
                                return true;
                        }
                        return false;
                    }
                }
        );
    }
}
