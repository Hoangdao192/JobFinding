package com.uet.fwork.database.repository;

public abstract class QueryTask<T> {
    private T result;
    private OnSuccessListener<T> onSuccessListener = null;
    private OnFailedListener onFailedListener = null;
    private OnCancelledListener onCancelledListener = null;
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

    public QueryTask<T> addOnFailedListener(OnFailedListener onFailedListener) {
        this.onFailedListener = onFailedListener;
        return this;
    }

    public QueryTask<T> addOnCancelledListener(OnCancelledListener onCancelledListener) {
        this.onCancelledListener = onCancelledListener;
        return this;
    }

    public void callOnSuccess(T result) {
        if (onSuccessListener != null) {
            onSuccessListener.onSuccess(result);
        }
    }

    public void callOnFailed(Exception e) {
        if (onFailedListener != null) {
            onFailedListener.onFailed(e);
        }
    }

    public void callOnCancelled() {
        if (onCancelledListener != null) {
            onCancelledListener.onCancelled();
        }
    }

    public T get() {
        return result;
    }

    public interface OnExecuteListener {
        void onExecute();
    }

    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }

    public interface OnFailedListener {
        void onFailed(Exception exception);
    }

    public interface OnCancelledListener {
        void onCancelled();
    }
}
