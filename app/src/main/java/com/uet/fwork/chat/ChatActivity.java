package com.uet.fwork.chat;

import androidx.appcompat.app.AppCompatActivity;
import com.uet.fwork.R;
import android.os.Bundle;

public class ChatActivity extends AppCompatActivity {

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static String currentChatChanelId = "";
    public static boolean activityVisible = false;

    @Override
    protected void onResume() {
        super.onResume();
        activityVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityVisible = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }
}