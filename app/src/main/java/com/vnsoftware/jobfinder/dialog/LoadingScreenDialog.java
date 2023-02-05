package com.vnsoftware.jobfinder.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.vnsoftware.jobfinder.R;

public class LoadingScreenDialog extends Dialog {
    public LoadingScreenDialog(@NonNull Context context) {
        super(context);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);

        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null);
        setContentView(view);
    }
}
