package com.vnsoftware.jobfinder.map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.util.ApiAddress;
import com.vnsoftware.jobfinder.util.MapTextSearchAPI;
import com.vnsoftware.jobfinder.util.VietNameAdministrativeDivisionAPI;

import java.util.ArrayList;
import java.util.List;

public class SearchPlaceActivity extends AppCompatActivity {

    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private TextView txtMap;
    private EditText edtAddress;
    private RecyclerView recAddressList;
    private CountDownTimer countDownTimer;
    private List<ApiAddress> addressList = new ArrayList<>();
    private ActivityResultLauncher<Intent> getLocationFromMapActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_autocomplete);

        txtMap = (TextView) findViewById(R.id.txtMap);
        edtAddress = (EditText) findViewById(R.id.edtAddress);
        recAddressList = (RecyclerView) findViewById(R.id.recAddressList);

        this.getLocationFromMapActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    setResult(result.getResultCode(), result.getData());
                    finish();
                }
        );

        txtMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchPlaceActivity.this, PickLocationFromMapActivity.class);
                getLocationFromMapActivityLauncher.launch(intent);
            }
        });

        AddressListRecyclerViewAdapter adapter = new AddressListRecyclerViewAdapter(
                this, addressList,
                new AddressListRecyclerViewAdapter.OnItemClickListener<ApiAddress>() {
                    @Override
                    public void onClick(ApiAddress apiAddress) {
                        Intent data = new Intent();
                        data.putExtra("address", apiAddress);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                }
        );
        recAddressList.setLayoutManager(new LinearLayoutManager(this));
        recAddressList.setAdapter(adapter);

        countDownTimer = new CountDownTimer(50, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                String target = edtAddress.getText().toString();
                MapTextSearchAPI.search(target, SearchPlaceActivity.this,
                        new VietNameAdministrativeDivisionAPI.OnApiResult<List<ApiAddress>>() {
                            @Override
                            public void onResult(List<ApiAddress> result) {
                                addressList.clear();
                                addressList.addAll(result);
                                adapter.notifyDataSetChanged();
                            }
                        });
            }
        };

        edtAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                countDownTimer.cancel();
                if (!edtAddress.getText().toString().isEmpty()) {
                    countDownTimer.start();
                } else {
                    addressList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}