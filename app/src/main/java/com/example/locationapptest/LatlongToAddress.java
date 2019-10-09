package com.example.locationapptest;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class LatlongToAddress extends AppCompatActivity {

    EditText edtLatitude, edtLongtitude;
    Button btnGet;
    TextView tvAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latlong_to_address);

        edtLatitude = findViewById(R.id.editTextLat);
        edtLongtitude = findViewById(R.id.editTextLong);
        tvAddress = findViewById(R.id.textViewAddr);

        btnGet = findViewById(R.id.buttonGET);
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Float Latitude = Float.parseFloat(edtLatitude.getText().toString());
                Float Longtitude = Float.parseFloat(edtLongtitude.getText().toString());

                LatLng latLng = new LatLng(Latitude, Longtitude);

                tvAddress.setText(getAddress(latLng));
            }
        });
    }

    private String getAddress(LatLng latLng) {
        /*
         * 1 - Create a Geocoder object to specify address from location and reverse.
         * (Tạo đối tượng Geocoder để xác định địa chỉ từ vị trí và ngược lại)
         * */
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        Address address;
        String addressText = "";
        try {
            /*
             * 2 - Ask Geocoder to get address from location
             * (Hỏi Geocoder để lấy địa chỉ từ vị trí)
             * */
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            /*
             * 3 - if result have address infomation, get address string.
             * (Nếu kết quả có chứa thông tin địa chỉ, thực hiện lấy chuỗi địa chỉ)
             * */
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses.get(0);
                if (address.getMaxAddressLineIndex() > 0) {
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addressText += (i == 0) ? address.getAddressLine(i) : "\n" + address.getAddressLine(i);
                    }
                } else {
                    addressText = address.getAddressLine(0);
                }
            }
        } catch (IOException e) {
            Log.e("MainActivity", e.getLocalizedMessage());
        }
        return addressText;
    }
}
