package com.uet.fwork.firebasehelper;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.uet.fwork.Constants;
import com.uet.fwork.database.model.post.PostApplyModel;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CloudMessagingHelper {
    private static CloudMessagingHelper INSTANCE = null;
    private Context applicationContext = null;
    private static final String LOG_TAG = "CloudMessagingHelper";

    private CloudMessagingHelper(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * You must call this method before call getInstance() method
     */
    public static void initialize(Context context) {
        if (INSTANCE != null) {
            Log.d("CloudMessagingHelper", "CloudMessagingHelper has been initialized already.");
        } else {
            Log.d("CloudMessagingHelper", "CloudMessagingHelper initialize successful.");
            INSTANCE = new CloudMessagingHelper(context.getApplicationContext());
        }
    }

    /**
     * You must call initialize method before call this method
     * If method initialize has not been called yet
     * @return null
     * Else
     * @return INSTANCE
     */
    public static CloudMessagingHelper getInstance() {
        if (INSTANCE == null) {
            Log.d("CloudMessagingHelper", "CloudMessagingHelper has not been initialized yet.");
            return null;
        }

        return INSTANCE;
    }

    public void sendPostApplicationAcceptNotify(PostApplyModel postApply) {
        RequestQueue requestQueue = Volley.newRequestQueue(applicationContext);
        String apiUrl = Constants.SERVER_URL + "post/accept/notify";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl, response -> {
            response = new String(
                    response.getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8);
            Log.d(LOG_TAG, "Volley: Request response " + response);
        }, error -> {
            error.printStackTrace();
            Log.d(LOG_TAG, "Volley: Send request failed " + apiUrl);
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userId", postApply.getUserId());
                params.put("postId", postApply.getPostId());
                return params;
            }
        };
        Log.d(LOG_TAG, "Volley: Add request to queue");
        requestQueue.add(stringRequest);
    }

    public void sendPostApplicationRejectNotify(PostApplyModel postApply) {
        RequestQueue requestQueue = Volley.newRequestQueue(applicationContext);
        String apiUrl = Constants.SERVER_URL + "post/reject/notify";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl, response -> {
            response = new String(
                    response.getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8);
            Log.d(LOG_TAG, "Volley: Request response " + response);
        }, error -> {
            error.printStackTrace();
            Log.d(LOG_TAG, "Volley: Send request failed " + apiUrl);
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userId", postApply.getUserId());
                params.put("postId", postApply.getPostId());
                return params;
            }
        };
        Log.d(LOG_TAG, "Volley: Add request to queue");
        requestQueue.add(stringRequest);
    }
}
