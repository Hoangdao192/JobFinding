package com.vnsoftware.jobfinder.landingpage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.view.View;

import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.account.login.LoginActivity;

public class LandingPage2 extends AppCompatActivity {
    private ImageView nextPage2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page_2);

        nextPage2 = (ImageView) findViewById(R.id.nextPage2);
        nextPage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingPage2.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}