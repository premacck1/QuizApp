package com.prembros.programming.quizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.applovin.sdk.AppLovinSdk;
import com.chartboost.sdk.Chartboost;
import com.inmobi.sdk.InMobiSdk;
import com.jirbo.adcolony.AdColony;

public class FirstScreen extends AppCompatActivity {

    boolean startedBefore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_first_screen);

//        Initialize third party ad SDKs
        AdColony.configure(this, "version:1.11,store:google", "app37e76e4fec5f493591", "vz6039927ae3ee42ed8e");
        Chartboost.startWithAppId(this, "57d270c943150f1f694f7316", "a5c83b4430cca0bd826a41b6e23c75c939d8efa1");
        Chartboost.onCreate(this);
        AppLovinSdk.initializeSdk(this);
        InMobiSdk.init(this, "12735c59690342e4bf61cc443503807e");

        //  Declare a new thread to do a preference check
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    startActivity(new Intent(FirstScreen.this, FirstIntro.class));
                    startedBefore = false;

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();

                    // Finish This Activity
                    FirstScreen.this.finish();
                }

                //  If the activity has been started before...
                else {
                    startedBefore = true;
                }
            }
        });

        // Start the thread
        t.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (startedBefore) {
                    startActivity(new Intent(FirstScreen.this, MainActivity.class));
                    FirstScreen.this.finish();
                }
            }
        }, 2000);
    }
}