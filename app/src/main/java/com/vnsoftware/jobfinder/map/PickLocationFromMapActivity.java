package com.vnsoftware.jobfinder.map;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.util.ApiAddress;

import java.io.IOException;
import java.util.List;

public class PickLocationFromMapActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private MapView mapView;
    private EditText edtAddress;
    private Button btnSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btnSelect = (Button) findViewById(R.id.btnSelect);
        edtAddress = (EditText) findViewById(R.id.edtAddress);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(null);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView.onResume();
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng hanoi = new LatLng(21.028511, 105.804817);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 14.0f));

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                btnSelect.setVisibility(View.VISIBLE);
                Geocoder geocoder = new Geocoder(PickLocationFromMapActivity.this);

                try {
                    List<Address> addressList = geocoder.getFromLocation(
                            mMap.getCameraPosition().target.latitude,
                            mMap.getCameraPosition().target.longitude,
                            1
                    );

                    if (addressList.size() > 0) {
                        String address = addressList.get(0).getAddressLine(0);
                        String city = addressList.get(0).getLocality();
                        String state = addressList.get(0).getAdminArea();
                        String country = addressList.get(0).getCountryName();
                        String postalCode = addressList.get(0).getPostalCode();
                        String knownName = addressList.get(0).getFeatureName();
                        System.out.println(address + " " + city + " " + state
                                + " " + country + " " + postalCode + " " + knownName);
                        edtAddress.setText(address);
                    }
                } catch (IOException e) {
                    System.out.println("IOException");
                    e.printStackTrace();
                }
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                btnSelect.setVisibility(View.GONE);
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (getAddress() != null) {
                        Intent data = new Intent();
                        data.putExtra("address", getAddress());
                        setResult(RESULT_OK, data);
                        finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private ApiAddress getAddress() throws IOException{
        Geocoder geocoder = new Geocoder(PickLocationFromMapActivity.this);
        List<Address> addressList = geocoder.getFromLocation(
                mMap.getCameraPosition().target.latitude,
                mMap.getCameraPosition().target.longitude,
                1
        );

        if (addressList.size() > 0) {
            String address = addressList.get(0).getAddressLine(0);
            ApiAddress apiAddress = new ApiAddress();
            apiAddress.setFullAddress(address);
            apiAddress.setLatitude(addressList.get(0).getLatitude());
            apiAddress.setLongitude(addressList.get(0).getLongitude());
            return apiAddress;
        }
        return null;
    }
}