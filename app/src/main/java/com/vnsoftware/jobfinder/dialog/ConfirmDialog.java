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

public class ConfirmDialog extends Dialog {
    private Button btnApply, btnCancel;
    private TextView txvTitle, txvMessage;

    public ConfirmDialog(
            @NonNull Context context, String title, String message,
            OnEventListener listener) {
        super(context);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null);
        btnApply = view.findViewById(R.id.btnApply);
        btnCancel = view.findViewById(R.id.btnCancel);
        txvTitle = view.findViewById(R.id.txtTitle);
        txvMessage = view.findViewById(R.id.txtMessage);

        txvTitle.setText(title);
        txvMessage.setText(message);

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onConfirm();
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel();
                dismiss();
            }
        });

        setContentView(view);
    }

    public interface OnEventListener {
        void onConfirm();
        void onCancel();
    }
}
