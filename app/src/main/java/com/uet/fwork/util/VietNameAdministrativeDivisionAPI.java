package com.uet.fwork.util;

import android.content.Context;
import android.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VietNameAdministrativeDivisionAPI {

    private final static String API = "https://provinces.open-api.vn/api/";

    private static VietNameAdministrativeDivisionAPI INSTANCE = null;

    public static final VietNameAdministrativeDivisionAPI getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new VietNameAdministrativeDivisionAPI();
        }
        return INSTANCE;
    }

    private VietNameAdministrativeDivisionAPI() {}

    public void getProvinceList(Context context, OnApiResult<List<Pair<Integer, String>>> onApiResult) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = API + "?depth=1";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    response = new String(
                            response.getBytes(StandardCharsets.ISO_8859_1),
                            StandardCharsets.UTF_8);
                    System.out.println(response);
                    try {
                        List<Pair<Integer, String>> provinceMap =
                                parseAdministrativeDivisionList(new JSONArray(response));
                        onApiResult.onResult(provinceMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }, error -> {
                    error.printStackTrace();
                    onApiResult.onResult(null);
                });
        requestQueue.add(stringRequest);
    }

    public void getDistrictList(
            Context context, int provinceCode,
            OnApiResult<List<Pair<Integer, String>>> onApiResult) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = API + "p/" + provinceCode + "?depth=2";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    response = new String(
                            response.getBytes(StandardCharsets.ISO_8859_1),
                            StandardCharsets.UTF_8);
                    System.out.println(response);
                    try {
                        JSONObject provinceObject = new JSONObject(response);
                        JSONArray jsonArray = provinceObject.getJSONArray("districts");
                        List<Pair<Integer, String>> districtMap =
                                parseAdministrativeDivisionList(jsonArray);
                        onApiResult.onResult(districtMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }, error -> {
            error.printStackTrace();
            onApiResult.onResult(null);
        });
        requestQueue.add(stringRequest);
    }

    public void getWardList(
            Context context, int districtCode,
            OnApiResult<List<Pair<Integer, String>>> onApiResult) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = API + "d/" + districtCode + "?depth=2";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    response = new String(
                            response.getBytes(StandardCharsets.ISO_8859_1),
                            StandardCharsets.UTF_8);
                    System.out.println(response);
                    try {
                        JSONObject provinceObject = new JSONObject(response);
                        JSONArray jsonArray = provinceObject.getJSONArray("wards");
                        List<Pair<Integer, String>> districtMap =
                                parseAdministrativeDivisionList(jsonArray);
                        onApiResult.onResult(districtMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }, error -> {
            error.printStackTrace();
            onApiResult.onResult(null);
        });
        requestQueue.add(stringRequest);
    }

    /**
     * Convert Json Array (Chứa thông tin các đơn vị hành chính) được trả về thành List
     * @param jsonArray
     * @return
     * @throws JSONException
     */
    private List<Pair<Integer, String>> parseAdministrativeDivisionList(JSONArray jsonArray) throws JSONException {
        List<Pair<Integer, String>> divisionMap = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            divisionMap.add(
                    Pair.create(
                            jsonObject.getInt("code"),
                            jsonObject.getString("name"))
            );
        }
        return divisionMap;
    }

    public interface OnApiResult<T> {
        void onResult(T result);
    }
}
