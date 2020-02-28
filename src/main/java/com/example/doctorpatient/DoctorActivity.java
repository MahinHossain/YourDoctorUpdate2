package com.example.doctorpatient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

public class DoctorActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private Button  btnGetnearByRequest;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ListView listView;
    private ArrayList<String> nearBypatientList;
    private ArrayAdapter adapter;

    private ArrayList<Double> patientLongitude;
    private ArrayList<Double> patientLatitude;

    private ArrayList<String> requestDocUseranme;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        btnGetnearByRequest = findViewById(R.id.btngetNearByRequestOId);
        btnGetnearByRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onmapredy();


            }
        });




        listView = findViewById(R.id.listviewId);

        nearBypatientList = new ArrayList<>();
        patientLatitude = new ArrayList<>();
        patientLongitude = new ArrayList<>();

        requestDocUseranme = new ArrayList<>();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearBypatientList);
        listView.setAdapter(adapter);

        nearBypatientList.clear();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(DoctorActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            iniTializationLocationListener();


        }

        listView.setOnItemClickListener(this);

    }

    private void onmapredy() {


        if (Build.VERSION.SDK_INT < 23) {

            Location curentDoctorLoacaation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            updateRequestListView(curentDoctorLoacaation);

        } else if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(DoctorActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DoctorActivity.this, new String[]
                        {Manifest.permission.ACCESS_FINE_LOCATION}, 1000);

            } else {
                // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location curentDoctorLoacaation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                updateRequestListView(curentDoctorLoacaation);
            }
        }
    }

    private void updateRequestListView(Location doctorlation) {

        if (doctorlation != null) {

            savedoctrLocation(doctorlation);
            //to stop multiple requestby same person


            final ParseGeoPoint doctorCurrentLocation = new ParseGeoPoint(doctorlation.getLatitude(), doctorlation.getLongitude());

            final ParseQuery<ParseObject> requestPatientquery = ParseQuery.getQuery("RequestOfPatient");
            requestPatientquery.whereNear("PassengerLocation", doctorCurrentLocation);
            requestPatientquery.whereDoesNotExist("DoctorOfMe");
            requestPatientquery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null) {
                        if (objects.size() > 0) {

                            if (nearBypatientList.size() > 0) {
                                nearBypatientList.clear();

                            }

                            if (patientLongitude.size() > 0) {
                                patientLongitude.clear();
                            }
                            if (patientLatitude.size() > 0) {
                                patientLatitude.clear();
                            }
                            if (requestDocUseranme.size() > 0) {
                                requestDocUseranme.clear();
                            }


                            for (ParseObject nearRequest : objects) {

                                ParseGeoPoint pLoaction = (ParseGeoPoint) nearRequest.get("PassengerLocation");

                                Double KilometerDistanceToPatient = doctorCurrentLocation.
                                        distanceInKilometersTo(pLoaction);

                                float roundedValue = Math.round(KilometerDistanceToPatient * 10) / 10;

                                nearBypatientList.add("There are " + roundedValue + " km to " + nearRequest.get("username"));

                                patientLatitude.add(pLoaction.getLatitude());
                                patientLongitude.add(pLoaction.getLongitude());


                                requestDocUseranme.add(nearRequest.get("username") + "");


                            }

                        } else {
                            FancyToast.makeText(DoctorActivity.this, "There is No Request", FancyToast.
                                    LENGTH_LONG, FancyToast.INFO, true).show();

                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(DoctorActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                iniTializationLocationListener();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location curentDoctorLoacaation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(curentDoctorLoacaation);
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        FancyToast.makeText(DoctorActivity.this, "Click", FancyToast.
                LENGTH_LONG, FancyToast.INFO, true).show();


        if (ContextCompat.checkSelfPermission(DoctorActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location currentdoctorLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (currentdoctorLocation != null) {

                Intent intent = new Intent(this, ViewBothLocation.class);

                intent.putExtra("docLatitude", currentdoctorLocation.getLatitude());
                intent.putExtra("docLongitude", currentdoctorLocation.getLongitude());

                intent.putExtra("patLatitude", patientLatitude.get(position));
                intent.putExtra("patLongitude", patientLongitude.get(position));

                intent.putExtra("patUsername", requestDocUseranme.get(position));


                startActivity(intent);
            }

        }
    }

    void iniTializationLocationListener() {

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

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

    }

    void savedoctrLocation(Location location) {
        ParseUser doctor = ParseUser.getCurrentUser();
        ParseGeoPoint doctorLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        doctor.put("DoctorLocation", doctorLocation);
        doctor.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    FancyToast.makeText(DoctorActivity.this, "Location Saved", FancyToast.
                            LENGTH_LONG, FancyToast.INFO, true).show();

                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menulogoutId:
                // code logout
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {

                            FancyToast.makeText(DoctorActivity.this, " Logout successfull"
                                    , FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();

                            finish();
                        }
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
