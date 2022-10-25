package com.uet.fwork;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class CreateCompanyProfileFragment extends Fragment {
    private EditText edtCompanyName, edtPhoneNumber, edtContactEmail, edtDetailAddress;
    private Spinner spnProvince, spnDistrict, spnWard;
    private final static String API = "https://provinces.open-api.vn/api/";

    public CreateCompanyProfileFragment() {
        super(R.layout.fragment_enter_profile_company);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtCompanyName = view.findViewById(R.id.edtCompanyName);
        edtContactEmail = view.findViewById(R.id.edtWorkEmail);
        edtDetailAddress = view.findViewById(R.id.edtDetailAddress);
        edtPhoneNumber = view.findViewById(R.id.edtPhoneNumber);

        spnProvince = view.findViewById(R.id.spnProvince);
        spnDistrict = view.findViewById(R.id.spnDistrict);
        spnWard = view.findViewById(R.id.spnWard);
        loadProvince();
    }

    private void loadProvince() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(API + "?depth=1");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream responseBody = connection.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(responseBody, "UTF-8");
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        Scanner scanner = new Scanner(responseBody);
                        String response = "";
                        while (scanner.hasNext()) {
                            response += scanner.nextLine();
                        }

                        List<Pair<Integer, String>> provinceMap = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            provinceMap.add(Pair.create(jsonObject.getInt("code"), jsonObject.getString("name")));
                        }

                        ArrayAdapter<Pair<Integer, String>> adapter
                                = new ArrayAdapter<>(
                                        getContext(),
                                        R.layout.custom_spinner_dropdown,
                                        a
                                );
                        spnProvince.setAdapter(adapter);

                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void test() {

    }
}
