package com.example.locationapptest;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Toolbar toolbar;

    private GoogleMap mMap;
    ArrayList<LatLng> arrayList;
    LatLng mylocation;

    private static final int REQUEST_LOCATION = 1000;
    private long UPDATE_INTERVAL = 5 * 1000;  /* 5 secs */
    private long FASTEST_INTERVAL = 3000; /* 3 sec */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        arrayList = new ArrayList<>();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLastLocation();

        // return your location
        mMap.setMyLocationEnabled(true);
        // add zoom button
        mMap.getUiSettings().setZoomControlsEnabled(true);

        /* Set button select map type */
        final ImageView select = findViewById(R.id.imageViewSelect);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerForContextMenu(select);
            }
        });

        /* Click to addMarker on map */
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                String address = getAddress(latLng);

                mMap.clear();
                arrayList.add(latLng);

                mMap.addMarker(new MarkerOptions().position(latLng).title(address));
            }
        });

        /* direction 2 point */
        ImageView direct = findViewById(R.id.imageView2);
        direct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();

                direction2Point();
            }
        });

    }

    /* Create menu map type select*///--------------------------------------------------------------
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Map type");
        menu.add(0, v.getId(), 0, "NORMAL");
        menu.add(0, v.getId(), 0, "HYBRID");
        menu.add(0, v.getId(), 0, "TERRAIN");
    }

    /* Set select map type *///---------------------------------------------------------------------
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getTitle().toString()) {
            case "HYBRID":
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case "NORMAL":
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "TERRAIN":
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }

        return true;
    }

    /* permission check and get last location*///---------------------------------------------------
    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION && grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getLastLocation() {
        if (checkPermissions())
            requestPermissions();
        else {
            FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
            locationClient.getLastLocation()
                    // lay toa do (location)
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {

                                LatLng locationNow = new LatLng(location.getLatitude(),
                                        location.getLongitude());

                                mylocation = new LatLng(location.getLatitude(),
                                        location.getLongitude());

                                arrayList.add(mylocation);

                                String address = getAddress(locationNow);

                                mMap.addMarker(new MarkerOptions().position(locationNow).title(address));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationNow, 13));
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MapDemoActivity", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        }
    }

    /* get address from location *///---------------------------------------------------------------
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

    /* direction 2 point *///-----------------------------------------------------------------------
    private void direction2Point() {
        try {

            // url original: https://maps.googleapis.com/maps/api/directions/
            // json?origin=137E%20Nguyen%20Chi%20Thanh&destination=227%20Nguyen%20Van%20Cu&
            // language=vi&key=AIzaSyCSNQCX6UYnoiq-BSoaHRdQvmPovWRQeSY
            String firstpoint = "137E Nguyen Chi Thanh";

            // convert space key "%20"
            // url direction api
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + URLEncoder.encode(firstpoint, "UTF-8")
                    + "&destination=227%20Nguyen%20Van%20Cu&language=vi&key=AIzaSyCSNQCX6UYnoiq-BSoaHRdQvmPovWRQeSY";

            RequestQueue requestQueue = Volley.newRequestQueue(MapsActivity.this);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    parseJSon(response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            requestQueue.add(jsonObjectRequest);

        }catch (Exception ex){

        }
    }


    // Parse Json
    private void parseJSon(String data) {
        if (data == null)
            return;


        try {
            JSONObject jsonData = new JSONObject(data);
            JSONArray jsonRoutes = jsonData.getJSONArray("routes");
            for (int i = 0; i < jsonRoutes.length(); i++) {
                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);


                JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
                JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
                JSONObject jsonLeg = jsonLegs.getJSONObject(0);
                JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
                JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
                JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

                List<LatLng> arrayList = decodePolyLine(overview_polylineJson.getString("points"));

                // get first point and end point
                mMap.addMarker(new MarkerOptions().position(arrayList.get(0)));
                mMap.addMarker(new MarkerOptions().position(arrayList.get(arrayList.size() - 1)));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(arrayList.get(0), 16));

                mMap.addPolyline(new PolylineOptions()
                        .color(Color.RED)
                        .width(5)
                        .addAll(arrayList));


            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    // google map direction
    // draw line from start point to end point
    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
