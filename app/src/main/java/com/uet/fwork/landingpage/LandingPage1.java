package com.uet.fwork.landingpage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.view.View;

import com.uet.fwork.R;

public class LandingPage1 extends AppCompatActivity {
    private ImageView nextPage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page_1);

        nextPage = (ImageView) findViewById(R.id.nextPage);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingPage1.this, LandingPage2.class);
                startActivity(intent);
            }
        });
    }
}