package com.vnsoftware.jobfinder.account.register;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.firebasehelper.FirebaseAuthHelper;

public class RegisterActivity extends AppCompatActivity {

    public CallbackManager callbackManager;
    private FirebaseAuthHelper firebaseAuthHelper;

    public RegisterActivity() {
        super();
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    //  Hủy focus một EditText khi touch ra ngoài EditText đó
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    view.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}