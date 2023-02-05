package com.vnsoftware.jobfinder.database.repository;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public abstract class QueryTask<T> {
    private T result;
    private OnSuccessListener<T> onSuccessListener = null;
    private OnFailureListener onFailedListener = null;
    private OnCanceledListener onCancelledListener = null;
    private OnExecuteListener onExecuteListener = null;

    public QueryTask() {
    }

    public QueryTask(T result) {
        this.result = result;
    }

    public abstract void execute();

    public QueryTask<T> addOnSuccessListener(OnSuccessListener<T> onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
        return this;
    }

    public QueryTask<T> addOnFailedListener(OnFailureListener onFailedListener) {
        this.onFailedListener = onFailedListener;
        return this;
    }

    public QueryTask<T> addOnCancelledListener(OnCanceledListener onCancelledListener) {
        this.onCancelledListener = onCancelledListener;
        return this;
    }

    public OnSuccessListener<T> getOnSuccessListener() {
        return onSuccessListener;
    }

    public OnFailureListener getOnFailedListener() {
        return onFailedListener;
    }

    public OnCanceledListener getOnCancelledListener() {
        return onCancelledListener;
    }

    public void onSuccess(T result) {
        if (onSuccessListener != null) {
            onSuccessListener.onSuccess(result);
        }
    }

    public void onFailed(Exception e) {
        if (onFailedListener != null) {
            onFailedListener.onFailure(e);
        }
    }

    public void onCancelled() {
        if (onCancelledListener != null) {
            onCancelledListener.onCanceled();
        }
    }

    public T getResult() {
        return result;
    }

    public T get() {
        return result;
    }

    public interface OnExecuteListener {
        void onExecute();
    }
}
