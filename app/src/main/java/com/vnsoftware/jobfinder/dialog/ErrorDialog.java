package com.vnsoftware.jobfinder.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vnsoftware.jobfinder.R;

public class ErrorDialog extends Dialog {
    private Button btnDismiss;
    private TextView txvTitle, txvMessage;

    public ErrorDialog(@NonNull Context context, String title, String message) {
        super(context);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_error, null);
        btnDismiss = view.findViewById(R.id.btnDismiss);
        txvTitle = view.findViewById(R.id.txtTitle);
        txvMessage = view.findViewById(R.id.txtMessage);

        txvTitle.setText(title);
        txvMessage.setText(message);
        btnDismiss.setOnClickListener(button -> {
            dismiss();
        });

        setContentView(view);
    }
}
