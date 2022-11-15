package com.uet.fwork.landingpage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.uet.fwork.MainActivity;
import com.uet.fwork.R;

import java.util.ArrayList;
import java.util.List;

public class FirstLaunchActivity extends AppCompatActivity {

    private IntroViewPagerAdapter adapter;
    private final List<Integer> layoutList;
    private ViewPager viewPager;
    private ImageButton imgBtnNext;

    public FirstLaunchActivity() {
        super();
        layoutList = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.uet.fwork.R.layout.activity_first_launch);

        viewPager = findViewById(R.id.viewPager);
        imgBtnNext = findViewById(R.id.imgBtnNext);

        layoutList.add(R.layout.intro_page_1);
        layoutList.add(R.layout.intro_page_2);
        adapter = new IntroViewPagerAdapter(layoutList, this);
        viewPager.setAdapter(adapter);

        imgBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() < layoutList.size() - 1) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                } else {
                    startActivity(new Intent(FirstLaunchActivity.this, MainActivity.class));
                }
            }
        });
    }
}