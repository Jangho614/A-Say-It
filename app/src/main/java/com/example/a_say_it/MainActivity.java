package com.example.a_say_it;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    // 각 fragment 클래스의 변수 선언
    PronounceFragment Fragment1;
    WordsFragment Fragment2;
    MyWordFragment Fragment3;

    private MenuItem lastSelectedItem;  // 이전에 선택된 메뉴 아이템

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 기기 하단 네비게이션 바 가리기
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hide the navigation bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // Hide the status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        Fragment1 = new PronounceFragment();
        Fragment2 = new WordsFragment();
        Fragment3 = new MyWordFragment();

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
                        // 아이콘 애니메이션 적용
                        animateIcon(item);

                        // 이전 아이템의 타이틀과 배경색 복원
                        resetBottomNavTitles(bottomMenu);

                        // 선택된 아이템의 배경색 변경
                        changeSelectedItemBackground(item);

                        // 현재 선택한 아이템의 타이틀 숨기기
                        item.setTitle("");

                        // 프래그먼트 전환
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

    // 아이콘 애니메이션 메서드
    private void animateIcon(MenuItem item) {
        // 현재 클릭된 아이템의 뷰를 가져옴
        View currentView = findViewById(item.getItemId());

        // 이전에 선택된 아이템이 있으면 크기를 원래대로 되돌림
        if (lastSelectedItem != null && lastSelectedItem != item) {
            View lastView = findViewById(lastSelectedItem.getItemId());
            if (lastView != null) {
                ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(lastView, "scaleX", 1.0f);
                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(lastView, "scaleY", 1.0f);
                scaleDownX.setDuration(200);  // 애니메이션 시간 설정
                scaleDownY.setDuration(200);
                scaleDownX.start();
                scaleDownY.start();
            }
        }

        // 현재 선택된 아이템의 크기를 키움
        if (currentView != null) {
            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(currentView, "scaleX", 1.25f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(currentView, "scaleY", 1.25f);
            scaleUpX.setDuration(200);  // 애니메이션 시간 설정
            scaleUpY.setDuration(200);
            scaleUpX.start();
            scaleUpY.start();
        }

        // 마지막 선택된 아이템을 현재 아이템으로 갱신
        lastSelectedItem = item;
    }

    // 모든 아이템의 타이틀을 복원하는 메서드
    private void resetBottomNavTitles(BottomNavigationView bottomMenu) {
        // 각 아이템의 타이틀을 원래 텍스트로 복구하고 배경색 초기화
        for (int i = 0; i < bottomMenu.getMenu().size(); i++) {
            MenuItem menuItem = bottomMenu.getMenu().getItem(i);
            View itemView = findViewById(menuItem.getItemId());

            // 타이틀 복원
            switch (menuItem.getItemId()) {
                case R.id.menu_pronounce:
                    menuItem.setTitle("발음연습하기");
                    break;
                case R.id.menu_randWords:
                    menuItem.setTitle("오늘의 단어");
                    break;
                case R.id.menu_myWords:
                    menuItem.setTitle("내 단어장");
                    break;
            }

            // 배경색 초기화
            if (itemView != null) {
                itemView.setBackgroundColor(Color.TRANSPARENT);  // 배경색 원래대로
            }
        }
    }

    // 선택된 아이템의 배경색을 변경하는 메서드
    private void changeSelectedItemBackground(MenuItem selectedItem) {
        View selectedView = findViewById(selectedItem.getItemId());

        // 선택된 아이템의 배경색 변경
        if (selectedView != null) {
            selectedView.setBackgroundColor(Color.parseColor("#f7f5a3"));
        }
    }
}
