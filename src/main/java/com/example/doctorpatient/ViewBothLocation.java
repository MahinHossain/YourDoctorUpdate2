package com.example.doctorpatient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

public class ViewBothLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button btnacceptRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_both_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btnacceptRequest = findViewById(R.id.btnaccept);


        btnacceptRequest.setText("Ok I am accepting " + getIntent().getStringExtra("patUsername") + " Request");
        btnacceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ParseQuery<ParseObject> patRequest = ParseQuery.getQuery("RequestOfPatient");

                patRequest.whereEqualTo("username", getIntent().getStringExtra("patUsername"));

                patRequest.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {


                        if (objects.size() > 0 && e == null) {


                            for (ParseObject DocRequest : objects) {


                                DocRequest.put("RequestedAccepted", true);
                                DocRequest.put("DoctorOfMe", ParseUser.getCurrentUser().getUsername());

                                DocRequest.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {


                                            Intent googleMap = new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("http://maps.google.com/maps?saddr=" +
                                                            getIntent().getDoubleExtra("docLatitude", 0) +
                                                            "," +
                                                            getIntent().getDoubleExtra("docLongitude", 0) + "&daddr=" +
                                                            getIntent().getDoubleExtra("patLatitude", 0) + "," +
                                                            getIntent().getDoubleExtra("patLongitude", 0)));


                                            startActivity(googleMap);

                                        }
                                    }
                                });
                            }

                        }
                    }
                });

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        Double docLat = getIntent().getDoubleExtra("docLatitude", 0);
        Double docLon = getIntent().getDoubleExtra("docLongitude", 0);
        //patien lat long
        Double patLat = getIntent().getDoubleExtra("patLatitude", 0);
        Double patLong = getIntent().getDoubleExtra("patLongitude", 0);


        String patusername = getIntent().getStringExtra("patUsername");


        // Add a marker in Sydney and move the camera
        LatLng drLocation = new LatLng(docLat, docLon);


        LatLng patientLocation = new LatLng(patLat, patLong);


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//  loction of doc mark
        Marker docMarker = mMap.addMarker(new MarkerOptions().position(drLocation).
                title(ParseUser.getCurrentUser().getUsername() + "").icon(BitmapDescriptorFactory.fromResource(R.drawable.icc)));
        docMarker.showInfoWindow();

        Marker patientMarker = mMap.addMarker(new MarkerOptions().
                position(patientLocation).title(patusername + "").icon(BitmapDescriptorFactory.fromResource(R.drawable.st)));

        patientMarker.showInfoWindow();

        ArrayList<Marker> mymarker = new ArrayList<>();

        mymarker.add(docMarker);
        mymarker.add(patientMarker);

        for (Marker marker : mymarker) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

       // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cameraUpdate);

        try {


        } catch (Exception e) {
            FancyToast.makeText(this, e + "", FancyToast.
                    LENGTH_LONG, FancyToast.ERROR, true).show();

        }


    }
}
