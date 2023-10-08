package com.uet.fwork.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapTextSearchAPI {
    private static final String LOG_TAG = "Map Api";
    public static final String API_KEY = "cbVKu6q9fnsJYNWvEFvoWlO4gHsTKEMJuE5xgTLs";
    private static final String PLACE_SEARCH_URL = "https://rsapi.goong.io/Place/AutoComplete";
    private static final String PLACE_DETAIL_URL = "https://rsapi.goong.io/Place/Detail";

    public static void search(
            String target,
            Context context,
            VietNameAdministrativeDivisionAPI.OnApiResult<List<ApiAddress>> listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = PLACE_SEARCH_URL + "?api_key=" + API_KEY + "&input=" + target;

        Log.d(LOG_TAG, "Volley: Send request " + url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
//                    response = new String(
//                            response.getBytes(StandardCharsets.ISO_8859_1),
//                            StandardCharsets.UTF_8);
                    Log.d(LOG_TAG, "Volley: " + url + ", response: " + response);
                    List<ApiAddress> addressList = new ArrayList<>();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray addressArray = jsonObject.getJSONArray("predictions");
                        for (int i = 0; i < addressArray.length(); ++i) {
                            JSONObject addressJson = addressArray.getJSONObject(i);
                            ApiAddress apiAddress = new ApiAddress(
                                    addressJson.getString("place_id"),
                                    addressJson.getString("description")
                            );
                            addressList.add(apiAddress);
                        }
                        listener.onResult(addressList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, "JSON: parse object failed " + response);
                    }
                }, error -> {
            error.printStackTrace();
        });
        requestQueue.add(stringRequest);
    }

    public static void getPlace(
            String placeId,
            Context context,
            VietNameAdministrativeDivisionAPI.OnApiResult<ApiAddress> listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = PLACE_DETAIL_URL + "?api_key=" + API_KEY + "&place_id=" + placeId;

        Log.d(LOG_TAG, "Volley: Send request " + url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
//                    response = new String(
//                            response.getBytes(StandardCharsets.ISO_8859_1),
//                            StandardCharsets.UTF_8);
//                    Log.d(LOG_TAG, "Volley: " + url + ", response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject placeResult = jsonObject.getJSONObject("result");
                        JSONObject jsonGeometry = placeResult.getJSONObject("geometry");
                        JSONObject jsonLocation = jsonGeometry.getJSONObject("location");
                        ApiAddress apiAddress = new ApiAddress(
                                placeResult.getString("place_id"),
                                placeResult.getString("formatted_address"),
                                jsonLocation.getDouble("lat"),
                                jsonLocation.getDouble("lng")
                        );
                        apiAddress.setPlaceName(placeResult.getString("name"));
                        listener.onResult(apiAddress);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, "JSON: parse object failed " + response);
                    }
                }, error -> {
            error.printStackTrace();
        });
        requestQueue.add(stringRequest);
    }
}
