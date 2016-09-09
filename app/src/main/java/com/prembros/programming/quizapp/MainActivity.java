package com.prembros.programming.quizapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.applovin.sdk.AppLovinSdk;
import com.chartboost.sdk.Chartboost;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.achievement.Achievements;
import com.jirbo.adcolony.AdColony;
import com.kobakei.ratethisapp.RateThisApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends LoginActivity
        implements Field.OnFragmentInteractionListener,
        Difficulty.OnFragmentInteractionListener,
        Questions.OnFragmentInteractionListener,
        Results.OnFragmentInteractionListenerInResults,
        ResultsInDetail.OnFragmentInteractionListener {

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private String JSONString = null;
    boolean doubleBackToExitPressedOnce = false;
    protected static ArrayList<QuestionBean> QUESTION = null;
    private String version;
    private boolean isRewardedVideoAdiOSLoading = false,
            isRewardedVideoAdJavaLoading = false,
            isRewardedVideoAdHTMLLoading = false,
            isRewardedVideoAdJSLoading = false;
    private DatabaseHolder dbHandler;
    public ProgressDialog progressDialogMainActivity;
    private InterstitialAd mInterstitialAd3;
    private RewardedVideoAd mRewardedVideoAdiOS, mRewardedVideoAdJava, mRewardedVideoAdHTML, mRewardedVideoAdJS;

    @Override
    public boolean releaseInstance() {
        return super.releaseInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Monitor launch times and interval from installation
        RateThisApp.onStart(this);
        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            return;
        }

        AdColony.configure(this, "version:1.11,store:google", "app37e76e4fec5f493591", "vz6039927ae3ee42ed8e");
        Chartboost.startWithAppId(this, "57d270c943150f1f694f7316", "a5c83b4430cca0bd826a41b6e23c75c939d8efa1");
        Chartboost.onCreate(this);
        AppLovinSdk.initializeSdk(this);

        //set up & load rewarded ads
        setUpRewardedAds();
        loadRewardedVideoAds();

        //Set up ads
        mInterstitialAd3 = new InterstitialAd(this);
        // set the ad unit ID
        mInterstitialAd3.setAdUnitId(getString(R.string.int_add_full2));

        requestNewInterstitial();
        mInterstitialAd3.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        if (progress_dialog!=null) progress_dialog.dismiss();

        if (isConnected() && !explicitlySignedOut)
            gPlusSignIn();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(R.string.app_name);

        Field mField = new Field();
        mField.setArguments(getIntent().getExtras());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, R.anim.slide_out_left,
                android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.add(R.id.main_fragment_container, mField).commit();

//        APP-RATING DIALOG CODE
//        Custom criteria: 3 days and 5 launches
        RateThisApp.Config config = new RateThisApp.Config(3, 5);
//        Custom title
        config.setTitle(R.string.rate_us);
        RateThisApp.init(config);

//        initializing db handler
        dbHandler = new DatabaseHolder(this);
        dbHandler.open();
        Cursor versionCursor = dbHandler.getQuestionVersion();
        versionCursor.moveToFirst();
        if (!versionCursor.isAfterLast()) {
            version = versionCursor.getString(versionCursor.getColumnIndex("version"));
        }
        dbHandler.close();

//        FIRST GET THE VERSION
        if(isConnected()){
            new HttpAsyncTask().execute("http://json-956.appspot.com/version.txt");
        }

        try{
            String string = readFromFile();
            if(string != null)
                JSONString = string;
            else{
                if(isConnected()){
//                 call AsyncTask to perform network operation on separate thread
//                new HttpAsyncTask().execute("http://json-956.appspot.com/version.txt");
                    new HttpAsyncTask().execute("https://json-956.appspot.com/json-1.txt");
                }
                else {
                    JSONString = readFromExternalFile();
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
            JSONString = readFromExternalFile();
        }
    }

    public String readFromExternalFile(){
        String ret = null;

        try {
            InputStream inputStream = openFileInput("GeneratedJSON.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("main activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("main activity", "Can not read file: " + e.toString());
        }
        return ret;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onResumeFragments() {
        if (Difficulty.BACK_FROM_RESULTS == 2){
            if (!Results.isFragmentActive)
                fragmentManager.popBackStack();
        }
        super.onResumeFragments();
    }
    //    Write to JSON file in Internal Memory
    public void writeToExternalFile(String string){
        FileOutputStream fos;
        try {
            fos = openFileOutput("GeneratedJSON.txt", Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    Read from JSON file in Internal Memory
    public String readFromFile() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        getAssets().open("json.txt")
                )
        );
        String read;
        StringBuilder builder = new StringBuilder("");

        while((read = bufferedReader.readLine()) != null){
            builder.append(read);
        }
        bufferedReader.close();
        return builder.toString();
    }

    public static String getStringFromInputStream(InputStream inputStream){
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try{
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (bufferedReader !=null){
                try{
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

    public static String getVersionFromInputStream(InputStream inputStream){
        BufferedReader bufferedReader = null;
        String versionStr = "";
        String line;
        try{
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            while((line = bufferedReader.readLine()) != null){
                versionStr = line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (bufferedReader !=null){
                try{
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return versionStr;
    }

    public String GET(String URLString){
        InputStream inputStream = null;
        String result = null;
        try{
            URL url = new URL(URLString);
//           create HttpClient
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            receive response as inputStream
            try {
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
            }catch (Exception uhe){
                uhe.printStackTrace();
            }
            if (inputStream !=null) {
                if (URLString.contains("version")) {
                    result = getVersionFromInputStream(inputStream);
                    if (result.codePointAt(0) == 0xfeff) {
                        result = result.substring(1, result.length());
                    }
                } else if (URLString.contains("json")){
                    result = getStringFromInputStream(inputStream);
                }
                else{
                    result = readFromFile();
                }
            }
            else{
                result = "0";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean isConnected() {
        ConnectivityManager conman = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conman.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String result = null;
            if(!(version.equals(GET(params[0]).trim()))) {
                // DB Update with new Version
                dbHandler.open();
                dbHandler.updateVersion(GET(params[0].trim()),"1");
                dbHandler.close();
                return GET("https://json-956.appspot.com/json-1.txt");
            }
            else {
                try {
                    result = readFromFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null){
                writeToExternalFile(s);
                JSONString = s;
            }
        }
    }

    //        Doing parsing of JSON data
    public ArrayList<QuestionBean> doInBackground(String JSONString,String field, String difficulty){
        if (JSONString == null){
            return null;
        }
        else {
            ArrayList<QuestionBean> fieldList;
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(JSONString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /** Getting the parsed data as a List construct */
            fieldList = new QuestionJSONParser().parse(jsonObject, field, difficulty);
            return fieldList;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!Questions.isFragmentActive && !Results.isFragmentActive) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        else return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case android.R.id.home:
                if (fragmentManager.getBackStackEntryCount() == 2){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("More or less");
                    builder.setMessage("Go back and change difficulty?");
                    builder.setPositiveButton("Yes please", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Difficulty.BACK_FROM_RESULTS = 1;
                            Questions.QUESTION_COUNT = 0;
                            Questions.CORRECT_ANSWERS = 0;
                            Questions.INCORRECT_ANSWERS = 0;
                            dbHandler.open();
                            dbHandler.resetTables();
                            dbHandler.close();
                            fragmentManager.popBackStackImmediate();
                        }
                    });
                    builder.setNegativeButton("I can take it", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Difficulty.BACK_FROM_RESULTS = 0;
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
                return true;
            case R.id.action_tutorial:
                //  Launch app intro
                startActivity(new Intent(this, FirstIntro.class));
                this.finish();
                return true;
            case R.id.action_get_pro:
                getPro();
                return true;
            case R.id.action_bookmark:
                getPro();
                return true;
            case R.id.action_leaderboard:
                getAndRemoveActiveFragment(LEADERBOARD_TEXT);
                loadFragment(LEADERBOARD_TEXT);
                return true;
            case R.id.action_achievements:
                loadFragment(ACHIEVEMENTS_TEXT);
                return true;
            case R.id.action_rate_this_app:
                RateThisApp.showRateDialog(this);
                return true;
            case R.id.action_about:
                getAndRemoveActiveFragment(ABOUT_TEXT);
                loadFragment(ABOUT_TEXT);
                return true;
            case R.id.action_help:
                getAndRemoveActiveFragment(HELP_TEXT);
                loadFragment(HELP_TEXT);
                return true;
            default:
                return false;
        }
    }

    public void getPro(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.get_pro);
        String message = getString(R.string.get_pro_content) + getString(R.string.get_pro_coming_soon);
        alert.setMessage(message);
        alert.setPositiveButton("Get Pro", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                        Toast.makeText(LoginActivity.this, R.string.get_pro_redirect, Toast.LENGTH_LONG).show();
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Prem+Bros")));
//                            }
//                        }, 2000);
            }
        });
//                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
        alert.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                SHOW ADS
                showInterstitial3();
            }
        }, 1000);
    }

    private void showInterstitial3() {
        if (mInterstitialAd3.isLoaded()) {
            mInterstitialAd3.show();
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        // Load ads into Interstitial Ads
        mInterstitialAd3.loadAd(adRequest);
    }

    public void setUpRewardedAds(){
        mRewardedVideoAdiOS = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAdiOS.setRewardedVideoAdListener(setRewardedVideoAdListeners("iOS"));
        mRewardedVideoAdJava = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAdJava.setRewardedVideoAdListener(setRewardedVideoAdListeners("Java"));
        mRewardedVideoAdHTML = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAdHTML.setRewardedVideoAdListener(setRewardedVideoAdListeners("HTML"));
        mRewardedVideoAdJS = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAdJS.setRewardedVideoAdListener(setRewardedVideoAdListeners("JS"));
    }

    public RewardedVideoAdListener setRewardedVideoAdListeners(String field){
        RewardedVideoAdListener rewardedVideoAdListener;
        switch (field){
            case "iOS":
                rewardedVideoAdListener = new RewardedVideoAdListener() {
                    @Override
                    public void onRewardedVideoAdLoaded() {
                        isRewardedVideoAdiOSLoading = true;
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdiOSLoaded", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdOpened() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdiOSOpened", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoStarted() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoiOSStarted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdClosed() {
                        isRewardedVideoAdiOSLoading = false;
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdiOSClosed", Toast.LENGTH_SHORT).show();
                        loadRewardedVideoAds();
                    }

                    @Override
                    public void onRewarded(RewardItem rewardItem) {
                        Toast.makeText(MainActivity.this, "Unlocked " + rewardItem.getAmount() + " " + rewardItem.getType() , Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdLeftApplication() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdiOSLeftApplication", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdFailedToLoad(int i) {
                        isRewardedVideoAdiOSLoading = false;
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdiOSFailedToLoad", Toast.LENGTH_SHORT).show();
                    }
                };
                return rewardedVideoAdListener;
            case "Java":
                rewardedVideoAdListener = new RewardedVideoAdListener() {
                    @Override
                    public void onRewardedVideoAdLoaded() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdJavaLoaded", Toast.LENGTH_SHORT).show();
                        isRewardedVideoAdJavaLoading = true;
                    }

                    @Override
                    public void onRewardedVideoAdOpened() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdJavaOpened", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoStarted() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoJavaStarted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdClosed() {
                        isRewardedVideoAdJavaLoading = false;
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdJavaClosed", Toast.LENGTH_SHORT).show();
                        loadRewardedVideoAds();
                    }

                    @Override
                    public void onRewarded(RewardItem rewardItem) {
                        Toast.makeText(MainActivity.this, "Unlocked " + rewardItem.getAmount() + " " + rewardItem.getType() , Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdLeftApplication() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdJavaLeftApplication", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdFailedToLoad(int i) {
                        isRewardedVideoAdJavaLoading = false;
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdJavaFailedToLoad", Toast.LENGTH_SHORT).show();
                    }
                };
                return rewardedVideoAdListener;
            case "HTML":
                rewardedVideoAdListener = new RewardedVideoAdListener() {
                    @Override
                    public void onRewardedVideoAdLoaded() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdHTMLLoaded", Toast.LENGTH_SHORT).show();
                        isRewardedVideoAdHTMLLoading = true;
                    }

                    @Override
                    public void onRewardedVideoAdOpened() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdHTMLOpened", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoStarted() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoHTMLStarted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdClosed() {
                        isRewardedVideoAdHTMLLoading = false;
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdHTMLClosed", Toast.LENGTH_SHORT).show();
                        loadRewardedVideoAds();
                    }

                    @Override
                    public void onRewarded(RewardItem rewardItem) {
                        Toast.makeText(MainActivity.this, "Unlocked " + rewardItem.getAmount() + " " + rewardItem.getType() , Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onRewardedVideoAdLeftApplication() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdHTMLLeftApplication", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdFailedToLoad(int i) {
                        isRewardedVideoAdHTMLLoading = false;
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdHTMLFailedToLoad", Toast.LENGTH_SHORT).show();
                    }
                };
                return rewardedVideoAdListener;
            case "JS":
                rewardedVideoAdListener = new RewardedVideoAdListener() {
                    @Override
                    public void onRewardedVideoAdLoaded() {
                        isRewardedVideoAdJSLoading = false;
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdJSLoaded", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdOpened() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdJSOpened", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoStarted() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoJSStarted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdClosed() {
                        isRewardedVideoAdJSLoading = false;
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdJSClosed", Toast.LENGTH_SHORT).show();
                        loadRewardedVideoAds();
                    }

                    @Override
                    public void onRewarded(RewardItem rewardItem) {
                        Toast.makeText(MainActivity.this, "Unlocked " + rewardItem.getAmount() + " " + rewardItem.getType() , Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdLeftApplication() {
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdJSLeftApplication", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRewardedVideoAdFailedToLoad(int i) {
                        isRewardedVideoAdJSLoading = false;
                        Toast.makeText(MainActivity.this, "onRewardedVideoAdJSFailedToLoad", Toast.LENGTH_SHORT).show();
                    }
                };
                return rewardedVideoAdListener;
            default:
                return null;
        }
    }

    public void loadRewardedVideoAds(){
        synchronized (new Object()){
            Bundle extras = new Bundle();
            extras.putBoolean("_noRefresh", true);
            AdRequest adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
            if (!isRewardedVideoAdiOSLoading && !mRewardedVideoAdiOS.isLoaded()) {
                isRewardedVideoAdiOSLoading = true;
                mRewardedVideoAdiOS.loadAd("ca-app-pub-0057541165660355/6958584285", adRequest);
            }
            if (!isRewardedVideoAdJavaLoading && !mRewardedVideoAdJava.isLoaded()) {
                isRewardedVideoAdJavaLoading = true;
                mRewardedVideoAdJava.loadAd("ca-app-pub-0057541165660355/2388783888", adRequest);
            }
            if (!isRewardedVideoAdHTMLLoading && !mRewardedVideoAdHTML.isLoaded()) {
                isRewardedVideoAdHTMLLoading = true;
                mRewardedVideoAdHTML.loadAd("ca-app-pub-0057541165660355/3865517083", adRequest);
            }
            if (!isRewardedVideoAdJSLoading && !mRewardedVideoAdJS.isLoaded()) {
                isRewardedVideoAdJSLoading = true;
                mRewardedVideoAdJS.loadAd("ca-app-pub-0057541165660355/5342250285", adRequest);
            }
        }
    }

    public void showRewardedVideoAD(String field){
        switch (field){
            case "iOS":
                if (mRewardedVideoAdiOS.isLoaded()) mRewardedVideoAdiOS.show();
                break;
            case "Java":
                if (mRewardedVideoAdJava.isLoaded()) mRewardedVideoAdJava.show();
                break;
            case "HTML":
                if (mRewardedVideoAdHTML.isLoaded()) mRewardedVideoAdHTML.show();
                break;
            case "JS":
                if (mRewardedVideoAdJS.isLoaded()) mRewardedVideoAdJS.show();
                break;
            default:
                break;
        }
    }

    //    onFragmentInteraction of Field fragment
    @Override
    public void onFragmentInteractionInField(final String field) {
        Difficulty.BACK_FROM_RESULTS = 0;
        Difficulty mdifficulty = new Difficulty();
        Bundle args = new Bundle();
        args.putString(Difficulty.ARG_POSITION, field);
        mdifficulty.setArguments(args);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        transaction.replace(R.id.main_fragment_container, mdifficulty);
        transaction.addToBackStack("DifficultyLaunched");
        transaction.commit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (field){
                    case "iOS":
                        showRewardedVideoAD("iOS");
                        break;
                    case "Java":
                        showRewardedVideoAD("Java");
                        break;
                    case "HTML":
                        showRewardedVideoAD("HTML");
                        break;
                    case "JavaScript":
                        showRewardedVideoAD("JS");
                        break;
                    default:
                        break;
                }
            }
        }, 500);
    }

    //        onFragmentInteraction of Difficulty fragment
    @Override
    public void onFragmentInteraction(String selection, String difficulty) {
        if (Difficulty.BACK_FROM_RESULTS == 1 || Difficulty.BACK_FROM_RESULTS == 2){
            fragmentManager.popBackStack();
        }
        else {
            Questions mQuestions = new Questions();
            Bundle args = new Bundle();
            QUESTION = doInBackground(JSONString, selection, difficulty);
            if (QUESTION != null) {
                progressDialogMainActivity = new ProgressDialog(MainActivity.this);
                progressDialogMainActivity.setIndeterminate(false);
                progressDialogMainActivity.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialogMainActivity.setMessage("Loading Questions...");
                progressDialogMainActivity.show();
                args.putString(Questions.FIELD_ARG, selection);
                args.putString(Questions.DIFFICULTY_ARG, difficulty);
                args.putParcelableArrayList("Question", QUESTION);
                mQuestions.setArguments(args);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                transaction.replace(R.id.main_fragment_container, mQuestions);
                transaction.addToBackStack("QuestionLaunched");
                transaction.commit();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Sorry, but the questions couldn't be loaded.");
                builder.setCancelable(false);
                builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    //        onFragmentInteraction of Question fragment
    @Override
    public void onFragmentInteraction(String action) {
        switch (action){
            case "gotoHome":
                onBackPressed();
                break;
            case "dismiss":
                if (progressDialogMainActivity != null)
                    progressDialogMainActivity.dismiss();
                break;
            case "launchResults":
                Questions.wannaGoToHome = true;
                fragmentManager.popBackStackImmediate();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_anim_in, android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                transaction.replace(R.id.main_fragment_container, new Results());
                transaction.addToBackStack("ResultsLaunched");
                transaction.commit();
                break;
            default:
                break;
        }
    }

    public static boolean isStoreVersion(Context context) {
        String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        return !TextUtils.isEmpty(installer);
    }

    //        onFragmentInteraction of Results fragment
    @Override
    public void onFragmentInteractionInResults(String action) {
        switch (action){
            case "submitScore":
                /*
                * Submit score to leaderboard
                */
                if (isStoreVersion(this)) {
                    if (google_api_client != null && google_api_client.isConnected()) {
                        if (Questions.SCORE > 0) {
                            Games.Leaderboards.submitScore(google_api_client,
                                    getLeaderboardID(Questions.selections[0], Questions.selections[1]), Questions.SCORE);
                        }
                        else Toast.makeText(this, "Score above zero to be in leaderboards", Toast.LENGTH_LONG).show();
                    }
                    else Toast.makeText(this, "Play Games Error! Can't upload score", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(this,
                        "Download this app from Play Store to Participate in Leaderboards", Toast.LENGTH_LONG).show();
                break;
            case "unlockAchievements":
                /*
                * Unlock achievements if any
                */
                if (google_api_client != null && google_api_client.isConnected()) {
                    Achievements achievements = Games.Achievements;
                    if (Questions.CORRECT_ANSWERS == Questions.QUESTION_COUNT) {
                        achievements.unlock(google_api_client, "CgkIl-nPp9wBEAIQAg");           //achievement_beginners_luck
                        achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQAw", 1);     //achievement_streak_of_5
                        achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQBA", 1);     //achievement_streak_of_15
                        achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQBQ", 1);     //achievement_streak_of_30
                        achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQIg", 1);     //achievement_streak_of_50
                        achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQIw", 1);     //achievement_streak_of_100
                        achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQJA", 1);     //achievement_streak_if_150
                        achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQJQ", 1);     //achievement_streak_of_200
                    } else if (Questions.INCORRECT_ANSWERS == Questions.QUESTION_COUNT) {
                        achievements.unlock(google_api_client, "CgkIl-nPp9wBEAIQBg");           //achievement_bummer_star
                        achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQBw", 1);     //achievement_bummer_king
                        achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQCA", 1);     //achievement_emperor_of_bummerville
                    } else if (Questions.CORRECT_ANSWERS == (Questions.QUESTION_COUNT / 2)) {
                        achievements.unlock(google_api_client, "CgkIl-nPp9wBEAIQJg");           //achievement_positive_halfsies
                    } else if (Questions.INCORRECT_ANSWERS == (Questions.QUESTION_COUNT / 2)) {
                        achievements.unlock(google_api_client, "CgkIl-nPp9wBEAIQJw");           //achievement_negative_halfsies
                    } else if (Questions.SKIPPED_ANSWERS == Questions.QUESTION_COUNT) {
                        achievements.unlock(google_api_client, "CgkIl-nPp9wBEAIQIQ");           //achievement_skippy
                    }
                }
                break;
            case "showResultsInDetail":
//                getAndRemoveActiveFragment("resultsInDetail");
                loadFragment("resultsInDetail");
                break;
            case "finishResultsFragment":
                fragmentManager.popBackStackImmediate();
                break;
            default:
                break;
        }
    }


    //        onFragmentInteraction of ResultsInDetail fragment
    @Override
    public void onFragmentInteraction() {
        getAndRemoveActiveFragment("resultsInDetail");
    }

    public void resetFlags(){
        Questions.QUESTION_COUNT = 0;
        Questions.CORRECT_ANSWERS = 0;
        Questions.INCORRECT_ANSWERS = 0;
        dbHandler.open();
        dbHandler.resetTables();
        dbHandler.close();
    }

    @Override
    public void onBackPressed() {
        if (Help.isFragmentActive){
            getAndRemoveActiveFragment(HELP_TEXT);
            return;
        }
        if (About.isFragmentActive){
            getAndRemoveActiveFragment(ABOUT_TEXT);
            return;
        }
        if (Leaderboard.isFragmentActive){
            getAndRemoveActiveFragment(LEADERBOARD_TEXT);
            return;
        }
        if(ResultsInDetail.isFragmentActive){
            getAndRemoveActiveFragment("resultsInDetail");
            return;
        }
        int backStackCount = fragmentManager.getBackStackEntryCount();

        switch (backStackCount){
            case 1:
//            in Difficulty fragment
                fragmentManager.popBackStackImmediate();
                break;
//            in Questions fragment
            case 2:
                if (!Results.isFragmentActive) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Sure to exit the current quiz?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Difficulty.BACK_FROM_RESULTS = 2;
                            resetFlags();
                            Questions.wannaGoToHome = true;
                            fragmentManager.popBackStackImmediate();
                            fragmentManager.popBackStackImmediate();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Difficulty.BACK_FROM_RESULTS = 0;
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
                else {
//            in Results fragment
                    //Place Ads

                    ResultsInDetail.isFragmentActive = false;
                    if (doubleBackToExitPressedOnce) {
                        doubleBackToExitPressedOnce = false;
                        resetFlags();
                        Questions.wannaGoToHome = true;
                        fragmentManager.popBackStackImmediate();
                        fragmentManager.popBackStackImmediate();
                        return;
                    }

                    this.doubleBackToExitPressedOnce = true;
                    Toast.makeText(this, "Don't forget to share your QuizResult!\nIf you have, hit back again to goto home", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doubleBackToExitPressedOnce = false;
                        }
                    }, 2000);
                }
                break;
            default:
                if (doubleBackToExitPressedOnce) {
                    doubleBackToExitPressedOnce = false;
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Hit back again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
                break;
        }
    }
}