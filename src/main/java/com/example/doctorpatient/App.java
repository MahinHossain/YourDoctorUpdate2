package com.example.doctorpatient;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("1lofkJs5fdfKKLqbXUbWk2i5xyvVdAe3dMLQyy5Z")
                // if defined
                .clientKey("RHjvBExFPuDhElmeyQUAPV89SgxPfFJuNp64NVSv")
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}