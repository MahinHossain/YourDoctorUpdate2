package com.example.doctorpatient;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PatientActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Handler handler;
    private GoogleMap mMap;
    Button RequestAdoctor, btnBeepStatus, logoutbtn;
    private boolean isRequestcancelled = true;
    private boolean isDoctorReady = false;
    Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((R.layout.activity_patient));

        btnBeepStatus = findViewById(R.id.BeepStatusId);
        RequestAdoctor = findViewById(R.id.btnrequestingAdoctor);
        logoutbtn = findViewById(R.id.btnlogoutId);

        RequestAdoctor.setOnClickListener(this);
        handler = new Handler();

        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /////
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            finish();
                        }
                    }
                });
            }
        });
        btnBeepStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doctorUpdates();
            }
        });
        ParseQuery<ParseObject> doctorrequest = ParseQuery.getQuery("RequestOfPatient");
        doctorrequest.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        doctorrequest.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size() > 0 && e == null) {

                    isRequestcancelled = false;

                    // isRequestcancelled = true;

                    RequestAdoctor.setText("Cancel the Request");
                    doctorUpdates();
                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateCamerapassengrlocation(location);


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(PatientActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PatientActivity.this, new String[]
                        {Manifest.permission.ACCESS_FINE_LOCATION}, 1000);

            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location curentpassengerLoacaation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                try {
                    updateCamerapassengrlocation(curentpassengerLoacaation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(PatientActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location curentpassengerLoacaation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCamerapassengrlocation(curentpassengerLoacaation);
            }

        }
    }

    private void updateCamerapassengrlocation(Location pLocation) {

        if (isDoctorReady == false) {
            LatLng passengerloction = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerloction, 15));

            // mMap.addMarker(new MarkerOptions().position(passengerloction).title("you are Here"));
            // mMap.addMarker(new MarkerOptions().position(passengerloction).title("you are Here"));

            MarkerOptions marker = new MarkerOptions().position(passengerloction).title("you are Here");


// Changing marker icon
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.icc));

// adding marker
            mMap.addMarker(marker);
        }
    }


    @Override
    public void onClick(View v) {
        //cheking that aalready permission is done or not


        if (isRequestcancelled) {
            if (ContextCompat.checkSelfPermission(PatientActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location passengerCurrentLoaction = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (passengerCurrentLoaction != null) {

                    ParseObject requestAcar = new ParseObject("RequestOfPatient");
                    requestAcar.put("username", ParseUser.getCurrentUser().getUsername());
                    ParseGeoPoint userLoaction = new ParseGeoPoint(passengerCurrentLoaction.getLatitude(), passengerCurrentLoaction.getLongitude());

                    requestAcar.put("PassengerLocation", userLoaction);
                    requestAcar.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            if (e == null) {
                                FancyToast.makeText(PatientActivity.this, " Doctor request is sent ",
                                        FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();

                                RequestAdoctor.setText("Cancel Request");
                                isRequestcancelled = false;


                            }
                        }
                    });

                } else {
                    FancyToast.makeText(PatientActivity.this, " Unknown error", FancyToast.
                            LENGTH_LONG, FancyToast.ERROR, true).show();


                }
            }
        } else {
            ParseQuery<ParseObject> doctorrequest = ParseQuery.getQuery("RequestOfPatient");
            doctorrequest.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            doctorrequest.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> Requestlist, ParseException e) {
                    if (Requestlist.size() > 0 && e == null) {


                        isRequestcancelled = true;

                        RequestAdoctor.setText("Request A new Doctor");


                        for (ParseObject Doctorrequest : Requestlist) {
                            Doctorrequest.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        FancyToast.makeText(PatientActivity.this, " Doctor request deleted",
                                                FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();


                                    }
                                }
                            });
                        }
                    }

                }
            });
        }

    }

    void doctorUpdates() {
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                ParseQuery<ParseObject> patientObjectParseQuery = ParseQuery.getQuery("RequestOfPatient");
                try {
                    patientObjectParseQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());

                } catch (Exception e) {

                }
                patientObjectParseQuery.whereEqualTo("RequestedAccepted", true);
                patientObjectParseQuery.whereExists("DoctorOfMe");
                patientObjectParseQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {


                        if (objects.size() > 0 && e == null) {
                            isDoctorReady = true;

                            for (final ParseObject requestObject : objects) {
                                ParseQuery<ParseUser> doctorQuery = ParseUser.getQuery();
                                doctorQuery.whereEqualTo("username", requestObject.getString("DoctorOfMe"));
                                doctorQuery.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> doctor, ParseException e) {
                                        if (doctor.size() > 0 && e == null) {
                                            for (ParseUser doctrOfRequest : doctor) {
                                                ParseGeoPoint doctorRequestlocation = doctrOfRequest.getParseGeoPoint("DoctorLocation");


                                                if (ContextCompat.checkSelfPermission(PatientActivity.this,
                                                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                    Location patientlocaton = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                                    ParseGeoPoint PatLocAsParseGeoPoint = new ParseGeoPoint(patientlocaton.getLatitude(),
                                                            patientlocaton.getLongitude());
                                                    double mileDistaance = doctorRequestlocation.distanceInKilometersTo(PatLocAsParseGeoPoint);
                                                    //kase asle notify korbe r oi reequest delete hoy jbae
                                                    if (mileDistaance < .05) {

                                                        requestObject.deleteInBackground(new DeleteCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if (e == null) {
                                                                    FancyToast.makeText(PatientActivity.this,
                                                                            " Doctor " + ParseUser.getCurrentUser().getUsername() +
                                                                                    " is Vey near to You ",
                                                                            FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();

                                                                    isDoctorReady = false;
                                                                    isRequestcancelled = true;
                                                                    RequestAdoctor.setText("Doctor is With You");
                                                                }
                                                            }
                                                        });


                                                    } else {

                                                        float rounderDistance = Math.round(mileDistaance * 1000) / 1000;

                                                        FancyToast.makeText(PatientActivity.this, " Yes " +
                                                                        doctrOfRequest.get("DoctorOfMe") +
                                                                        "  is  " + rounderDistance + " KM away",
                                                                FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();


                                                        //for both location in one map as like viewBothlocation actity map

                                                        LatLng drLocation = new LatLng(doctorRequestlocation.getLatitude(),
                                                                doctorRequestlocation.getLongitude());


                                                        LatLng patientLocation = new LatLng(PatLocAsParseGeoPoint.getLatitude(),
                                                                PatLocAsParseGeoPoint.getLongitude());


                                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                                                        Marker docMarker = mMap.addMarker(new MarkerOptions().position(drLocation).
                                                                title("Doctor Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.icc)));
                                                        docMarker.isInfoWindowShown();

                                                        Marker patientMarker = mMap.addMarker(new MarkerOptions().
                                                                position(patientLocation).title("Patient Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.icc)));
                                                        patientMarker.isInfoWindowShown();
                                                        ArrayList<Marker> mymarker = new ArrayList<>();

                                                        mymarker.add(docMarker);
                                                        mymarker.add(patientMarker);

                                                        for (Marker marker : mymarker) {
                                                            builder.include(marker.getPosition());
                                                        }

                                                        LatLngBounds bounds = builder.build();


                                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 80);
                                                        mMap.animateCamera(cameraUpdate);


                                                    }
                                                }


                                            }


                                        }
                                    }
                                });


                            }


                        } else {
                            isDoctorReady = false;
                        }
                    }
                });
            }
        }, 0, 10000);


    }


}
