package com.prembros.programming.quizapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.kobakei.ratethisapp.RateThisApp;

import java.io.InputStream;

public class LoginActivity extends AppCompatActivity implements OnConnectionFailedListener,
        View.OnClickListener, ConnectionCallbacks, Leaderboard.OnFragmentInteractionListener {

    public static Bitmap dp = null;
    final String LEADERBOARD_TEXT = "leaderboard",
            ABOUT_TEXT = "about",
            HELP_TEXT = "help",
            ACHIEVEMENTS_TEXT = "achievements";
    GoogleApiClient google_api_client;
    GoogleApiAvailability google_api_availability;
    SignInButton signIn_btn;
    public boolean explicitlySignedOut = false;
    private static final int SIGN_IN_CODE = 0;
    //    private static final int PROFILE_PIC_SIZE = 120;
    private ConnectionResult connection_result;
    private boolean doubleBackToDisconnect = false;
    private boolean is_intent_inprogress;
    private boolean is_signInBtn_clicked;
    private boolean is_signOutBtn_clicked = false;
    private int request_code;
    public ProgressDialog progress_dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildNewGoogleApiClient();

        if (this.toString().contains("LoginActivity")) {
            setContentView(R.layout.activity_login);
            if (isConnected()) {
//        SET UP ACTION BAR
                android.support.v7.app.ActionBar ab = this.getSupportActionBar();
                assert ab != null;
                ab.setDisplayShowHomeEnabled(true);
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setTitle(R.string.title_activity_login);

                if (checkPlayServices() && !explicitlySignedOut) {
//        Customize sign-in button.a red button may be displayed when Google+ scopes are requested
                    customizeSignBtn();
                    setBtnClickListeners();
                    progress_dialog = new ProgressDialog(this);
                    progress_dialog.setMessage("Loading....");
                    progress_dialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("Sorry, but we couldn't find Google Play Services on your devices, make sure you have the latest one from play store and try again.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            } else {
                Snackbar.make(getWindow().getDecorView(), "No working internet connection found" +
                        "\nGoogle play games connection failed!", Snackbar.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Go online to get extra benefits of QuizApp", Toast.LENGTH_SHORT).show();
                        if (findViewById(R.id.login_form) != null) {
                            LoginActivity.this.finish();
                        }
                    }
                }, 3000);
            }
        }
    }

    private boolean isConnected() {
        ConnectivityManager conman = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conman.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private boolean checkPlayServices() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    @SuppressWarnings("ConstantConditions")
    public void prepareUI() {
        if (google_api_client != null && google_api_client.isConnected()) {
            // signed in. Show the "sign out" button and explanation.
            changeUI(true);
        } else {
            // not signed in. Show the "sign in" button and explanation.
            changeUI(false);
        }

        if (dp != null && findViewById(R.id.profile_pic) != null) {
            ((ImageView) findViewById(R.id.profile_pic)).setImageBitmap(dp);
        }
    }

    /*
     Show and hide of the Views according to the user login status
     */
    @SuppressWarnings("ConstantConditions")
    private void changeUI(boolean signedIn) {
        if (signedIn) {
            if (progress_dialog != null)
                progress_dialog.dismiss();
            if (findViewById(R.id.login_intro)!=null) {
                findViewById(R.id.login_intro).setVisibility(View.INVISIBLE);
                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                findViewById(R.id.userID).setVisibility(View.VISIBLE);
                findViewById(R.id.username).setVisibility(View.VISIBLE);
                findViewById(R.id.userLevel).setVisibility(View.VISIBLE);
                findViewById(R.id.profile_pic).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
                findViewById(R.id.leaderboard_rippleView).setVisibility(View.VISIBLE);
                findViewById(R.id.achievements_rippleView).setVisibility(View.VISIBLE);
            }
        }
        else {
            if (progress_dialog != null)
                progress_dialog.dismiss();
            if (findViewById(R.id.login_intro)!=null) {
                dp = Bitmap.createBitmap(new int[]{Color.argb(0, 255, 255, 255)}, 1, 1, Bitmap.Config.ALPHA_8);
                ((ImageView) findViewById(R.id.profile_pic)).setImageBitmap(dp);
                findViewById(R.id.userID).setVisibility(View.GONE);
                findViewById(R.id.username).setVisibility(View.GONE);
                findViewById(R.id.userLevel).setVisibility(View.GONE);
                findViewById(R.id.profile_pic).setVisibility(View.GONE);
                findViewById(R.id.sign_out_button).setVisibility(View.GONE);
                findViewById(R.id.leaderboard_rippleView).setVisibility(View.GONE);
                findViewById(R.id.achievements_rippleView).setVisibility(View.GONE);
                findViewById(R.id.login_intro).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            }
        }
    }

    /*
    create and  initialize GoogleApiClient object to use Google Plus Api.
    While initializing the GoogleApiClient object, request the Plus.SCOPE_PLUS_LOGIN scope.
    */
    private void buildNewGoogleApiClient(){
        if (checkPlayServices()) {
            google_api_client = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                    .build();
        } else
            Toast.makeText(LoginActivity.this, "Play Services not present.\nGoogle API client not created", Toast.LENGTH_SHORT).show();
    }

    /*
      Customize sign-in button. The sign-in button can be displayed in
      multiple sizes and color schemes. It can also be contextually
      rendered based on the requested scopes. For example. a red button may
      be displayed when Google+ scopes are requested, but a white button
      may be displayed when only basic profile is requested. Try adding the
      Plus.SCOPE_PLUS_LOGIN scope to see the  difference.
    */
    @SuppressWarnings("ConstantConditions")
    private void customizeSignBtn(){
//        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
//        findViewById(R.id.disconnect_button).setVisibility(View.GONE);
        signIn_btn = (SignInButton) findViewById(R.id.sign_in_button);
        signIn_btn.setSize(SignInButton.SIZE_STANDARD);
        //noinspection deprecation
        signIn_btn.setScopes(new Scope[]{Games.SCOPE_GAMES});
    }

    /*
      Set on click Listeners on the sign-in sign-out and disconnect buttons
     */
    @SuppressWarnings("ConstantConditions")
    private void setBtnClickListeners(){
        // Button listeners
        signIn_btn.setOnClickListener(this);
        if (findViewById(R.id.sign_in_button) != null
                && findViewById(R.id.sign_out_button) != null
                && findViewById(R.id.show_leaderboard) != null
                && findViewById(R.id.show_achievements) != null) {
            findViewById(R.id.sign_in_button).setOnClickListener(this);
            findViewById(R.id.sign_out_button).setOnClickListener(this);
            findViewById(R.id.show_leaderboard).setOnClickListener(this);
            findViewById(R.id.show_achievements).setOnClickListener(this);
        }
    }

    protected void onStart() {
        super.onStart();
        if (isConnected() && google_api_client!=null) {
            google_api_client.connect();
        }
    }

    protected void onStop() {
        super.onStop();
        if (isConnected() && google_api_client != null) {
            google_api_client.disconnect();
        }
    }

    protected void onResume(){
        super.onResume();
        if (isConnected() && google_api_client != null) {
            google_api_client.connect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getAndRemoveActiveFragment("whatever");
                else this.finish();
                break;
            case R.id.action_rate_this_app:
                RateThisApp.showRateDialog(this);
                break;
            case R.id.action_about:
                getAndRemoveActiveFragment(ABOUT_TEXT);
                loadFragment(ABOUT_TEXT);
                break;
            case R.id.action_help:
                getAndRemoveActiveFragment(HELP_TEXT);
                loadFragment(HELP_TEXT);
                break;
            case R.id.action_get_pro:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.get_pro);
                alert.setMessage(R.string.get_pro_content);
                alert.setPositiveButton("Get Pro", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(LoginActivity.this, R.string.get_pro_redirect, Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Prem+Bros")));
                            }
                        }, 2000);
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alert.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
//        Toast.makeText(LoginActivity.this, "onConnectionFailed()", Toast.LENGTH_SHORT).show();
        if (!result.hasResolution()) {
            google_api_availability.getErrorDialog(this, result.getErrorCode(),request_code).show();
            return;
        }
        if (!is_intent_inprogress) {

            connection_result = result;

            if (is_signInBtn_clicked) {

                resolveSignInError();
            }
        }
        changeUI(false);
    }

    /*
      Will receive the activity result and check which request we are responding to

     */
    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        // Check which request we're responding to
        if (requestCode == SIGN_IN_CODE) {
            request_code = requestCode;
            if (responseCode != RESULT_OK) {
                is_signInBtn_clicked = false;
//                progress_dialog.dismiss();

            }

            is_intent_inprogress = false;

            if (!google_api_client.isConnecting() && google_api_client != null) {
                google_api_client.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Get user's information and set it into the layout
        is_signInBtn_clicked = false;
        getProfileInfo();
        if (!is_signOutBtn_clicked) {
            getProfileInfo();
            prepareUI();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        if (google_api_client != null)
            google_api_client.connect();
        changeUI(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                explicitlySignedOut = false;
                gPlusSignIn();
//                Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.sign_out_button:
                if (doubleBackToDisconnect) {
                    explicitlySignedOut = true;
//                    Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show();
                    gPlusSignOut();
                    return;
                }
                doubleBackToDisconnect = true;
                Toast.makeText(this, "Press again to successfully sign out", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToDisconnect = false;
                    }
                }, 2000);
                break;
            case R.id.show_leaderboard:
                getAndRemoveActiveFragment(LEADERBOARD_TEXT);
                loadFragment(LEADERBOARD_TEXT);
                break;
            case R.id.show_achievements:
                loadFragment(ACHIEVEMENTS_TEXT);
                break;
        }
    }

    public void loadFragment(String fragmentName){
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (fragmentName){
            case ABOUT_TEXT:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //noinspection ConstantConditions
                        getSupportActionBar().hide();
                    }
                }, 350);

                fragmentManager.beginTransaction().add(R.id.fragment_container, new About(), ABOUT_TEXT).commit();
                break;
            case LEADERBOARD_TEXT:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //noinspection ConstantConditions
                        getSupportActionBar().hide();
                    }
                }, 350);
                fragmentManager.beginTransaction().add(R.id.fragment_container, Leaderboard.newInstance(), LEADERBOARD_TEXT).commit();
                break;
            case HELP_TEXT:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //noinspection ConstantConditions
                        getSupportActionBar().hide();
                    }
                }, 350);
                fragmentManager.beginTransaction().add(R.id.fragment_container, new Help(), HELP_TEXT).commit();
                break;
            case ACHIEVEMENTS_TEXT:
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage("Achievements feature coming soon!.");
                builder.setCancelable(false);
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
//                if (google_api_client != null) {
//                    google_api_client.connect();
//                    if (google_api_client.isConnected()) {
//                        startActivityForResult(Games.Achievements.getAchievementsIntent(google_api_client), 16);
//                    } else Toast.makeText(this, "ERROR! Not Connected!", Toast.LENGTH_SHORT).show();
//                } else Toast.makeText(this, "No Google API client found!\n" +
//                        "Is Google Play Games missing?", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    public void getAndRemoveActiveFragment(String fragmentName){
        //noinspection ConstantConditions
        getSupportActionBar().show();
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (fragmentName){
            case ABOUT_TEXT:
                if (fragmentManager.findFragmentByTag(ABOUT_TEXT) != null && About.isFragmentActive) {
                    About.isFragmentActive = false;
                    About.rootView.startAnimation(AnimationUtils.loadAnimation(
                            getApplicationContext(), R.anim.fragment_anim_out));

                    fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(ABOUT_TEXT)).commit();
                }
                break;
            case HELP_TEXT:
                if (fragmentManager.findFragmentByTag(HELP_TEXT) != null && Help.isFragmentActive) {
                    Help.isFragmentActive = false;
                    Help.rootView.startAnimation(AnimationUtils.loadAnimation(
                            getApplicationContext(), R.anim.fragment_anim_out));

                    fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(HELP_TEXT)).commit();
                }
                break;
            case LEADERBOARD_TEXT:
                if (fragmentManager.findFragmentByTag(LEADERBOARD_TEXT) != null && Leaderboard.isFragmentActive) {
                    Leaderboard.isFragmentActive = false;
                    Leaderboard.rootView.startAnimation(AnimationUtils.loadAnimation(
                            getApplicationContext(), R.anim.fragment_anim_out));

                    fragmentManager.beginTransaction().remove(getSupportFragmentManager().findFragmentByTag(LEADERBOARD_TEXT)).commit();
                }
                break;
            default:
                int backStackCount = fragmentManager.getBackStackEntryCount();
                if (backStackCount > 0){
                    fragmentManager.popBackStackImmediate();
                }
                break;
        }
    }

    /*
      Sign-in into the Google + account
     */
    public void gPlusSignIn() {
        if (google_api_client != null) {
            google_api_client.connect();
            if (google_api_client.isConnecting()) {
                is_signInBtn_clicked = true;
//            progress_dialog.show();
                resolveSignInError();
                is_signOutBtn_clicked = false;
            } else
                Toast.makeText(this, "Make sure the device is connected to internet", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "No Google API client found!\n" +
                    "Is Google Play Games missing?", Toast.LENGTH_SHORT).show();
    }

    /*
      Method to resolve any signin errors
     */
    private void resolveSignInError() {
        if (connection_result != null) {
            if (connection_result.hasResolution()) {
                try {
                    is_intent_inprogress = true;
                    connection_result.startResolutionForResult(this, SIGN_IN_CODE);
                    Log.d("resolve error", "sign in error resolved");
                } catch (SendIntentException e) {
                    is_intent_inprogress = false;
                    if (google_api_client != null)
                        google_api_client.connect();
                }
            }
        }
    }

    /*
      Sign-out from Google+ account
     */

    private void gPlusSignOut() {
        if (google_api_client.isConnected()) {
            Games.signOut(google_api_client);
            //noinspection deprecation
//            Plus.AccountApi.clearDefaultAccount(google_api_client);
//            Plus.AccountApi.revokeAccessAndDisconnect(google_api_client);
            google_api_client.disconnect();
//            google_api_client.clearDefaultAccountAndReconnect();
//            google_api_client.connect();
            is_signOutBtn_clicked = true;
            dp = null;
            prepareUI();
        }
        else
            Toast.makeText(LoginActivity.this, "Make sure the device is connected to internet", Toast.LENGTH_SHORT).show();
    }

    /*
     Revoking access from Google+ account
     */
//    private void gPlusRevokeAccess() {
//        if (google_api_client.isConnected()) {
//            Plus.AccountApi.clearDefaultAccount(google_api_client);
//            Plus.AccountApi.revokeAccessAndDisconnect(google_api_client)
//                    .setResultCallback(new ResultCallback<Status>() {
//                        @Override
//                        public void onResult(@NonNull Status arg0) {
//                            Log.d("MainActivity", "User access revoked!");
//                            buildNewGoogleApiClient();
//                            google_api_client.connect();
//                            changeUI(false);
//                        }
//
//                    });
//            dp = null;
//        }
//        else
//            Toast.makeText(LoginActivity.this, "Make sure the device is connected to internet", Toast.LENGTH_SHORT).show();
//    }

    /*
     get user's information name and profile pic
     */
    @SuppressWarnings("deprecation")
    private void getProfileInfo() {

        try {
            Player currentPlayer = Games.Players.getCurrentPlayer(google_api_client);
            // Update the UI after signin
            if (currentPlayer != null) {
                changeUI(true);
                setPersonalInfo(currentPlayer);
            } else {
                Toast.makeText(getApplicationContext(),
                        "No Personal information mentioned", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
     set the User information into the views defined in the layout
     */

    private void setPersonalInfo(Player currentPlayer){

        String playerName = currentPlayer.getName();
        String playerID = "*__  " + currentPlayer.getDisplayName() + "  __*";
        String playerLevel = "Level: " + currentPlayer.getLevelInfo().getCurrentLevel().getLevelNumber() +
                ", XP: " + currentPlayer.getLevelInfo().getCurrentXpTotal() + " / " +
                currentPlayer.getLevelInfo().getCurrentLevel().getMaxXp();
        //noinspection deprecation
        String playerPhotoUrl = currentPlayer.getHiResImageUrl();
        TextView user_name = (TextView) findViewById(R.id.username);
        TextView user_ID = (TextView) findViewById(R.id.userID);
        TextView user_level = (TextView) findViewById(R.id.userLevel);
        if (user_name != null && user_ID != null) {
            user_name.setText(playerName);
            user_ID.setText(playerID);
            user_level.setText(playerLevel);
            setProfilePic(playerPhotoUrl);
        }
//        progress_dialog.dismiss();
//        Toast.makeText(this, "Person information is shown!", Toast.LENGTH_LONG).show();
    }

    /*
     By default the profile pic url gives 50x50 px image.
     If you need a bigger image we have to change the query parameter value from 50 to the size you want
    */

    public void setProfilePic(String profile_pic){
//        profile_pic = profile_pic.substring(0,
//                profile_pic.length() - 2)
//                + PROFILE_PIC_SIZE;
        ImageView user_picture = (ImageView)findViewById(R.id.profile_pic);
        if (user_picture != null) {
            new LoadProfilePic(user_picture).execute(profile_pic);
        }
    }

    /*
     Perform background operation asynchronously, to load user profile picture with new dimensions from the modified url
     */
    private class LoadProfilePic extends AsyncTask<String, Void, Bitmap> {
        public ImageView bitmap_img;

        public LoadProfilePic(ImageView bitmap_img) {
            this.bitmap_img = bitmap_img;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap new_icon = null;
            try {
                InputStream in_stream = new java.net.URL(url).openStream();
                new_icon = BitmapFactory.decodeStream(in_stream);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return new_icon;
        }

        protected void onPostExecute(Bitmap result_img) {

            bitmap_img.setImageBitmap(result_img);
            dp = result_img;
        }
    }

    @Override
    public void onBackPressed() {
        if (Leaderboard.isFragmentActive) {
            getAndRemoveActiveFragment(LEADERBOARD_TEXT);
            return;
        }
        if (About.isFragmentActive){
            getAndRemoveActiveFragment(ABOUT_TEXT);
            return;
        }
        if (Help.isFragmentActive){
            getAndRemoveActiveFragment(HELP_TEXT);
            return;
        }
        super.onBackPressed();
    }

    public String getLeaderboardID(String field, String difficulty){
        String leaderboardID = null;
        switch (field){
            case "iOS":
                switch (difficulty){
                    case "Rookie":
                        leaderboardID = "CgkIl-nPp9wBEAIQHA";
                        break;
                    case "Apprentice":
                        leaderboardID = "CgkIl-nPp9wBEAIQHw";
                        break;
                    case "Pro":
                        leaderboardID = "CgkIl-nPp9wBEAIQFA";
                        break;
                    case "Hitman":
                        leaderboardID = "CgkIl-nPp9wBEAIQGA";
                        break;
                }
                break;
            case "Java":
                switch (difficulty){
                    case "Rookie":
                        leaderboardID = "CgkIl-nPp9wBEAIQHQ";
                        break;
                    case "Apprentice":
                        leaderboardID = "CgkIl-nPp9wBEAIQEA";
                        break;
                    case "Pro":
                        leaderboardID = "CgkIl-nPp9wBEAIQFQ";
                        break;
                    case "Hitman":
                        leaderboardID = "CgkIl-nPp9wBEAIQGQ";
                        break;
                }
                break;
            case "HTML":
                switch (difficulty){
                    case "Rookie":
                        leaderboardID = "CgkIl-nPp9wBEAIQHg";
                        break;
                    case "Apprentice":
                        leaderboardID = "CgkIl-nPp9wBEAIQIA";
                        break;
                    case "Pro":
                        leaderboardID = "CgkIl-nPp9wBEAIQFg";
                        break;
                    case "Hitman":
                        leaderboardID = "CgkIl-nPp9wBEAIQGg";
                        break;
                }
                break;
            case "JavaScript":
                switch (difficulty){
                    case "Rookie":
                        leaderboardID = "CgkIl-nPp9wBEAIQDg";
                        break;
                    case "Apprentice":
                        leaderboardID = "CgkIl-nPp9wBEAIQEg";
                        break;
                    case "Pro":
                        leaderboardID = "CgkIl-nPp9wBEAIQFw";
                        break;
                    case "Hitman":
                        leaderboardID = "CgkIl-nPp9wBEAIQGw";
                        break;
                }
                break;
        }
        return leaderboardID;
    }

    @Override
    public void onFragmentInteraction(int resultCode) {
        if (google_api_client != null) {
            if (google_api_client.isConnected()) {
                switch (resultCode) {
                    case 0:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQHA"), 0);
                        break;
                    case 1:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQHw"), 1);
                        break;
                    case 2:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQFA"), 2);
                        break;
                    case 3:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQGA"), 3);
                        break;
                    case 4:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQHQ"), 4);
                        break;
                    case 5:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQEA"), 5);
                        break;
                    case 6:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQFQ"), 6);
                        break;
                    case 7:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQGQ"), 7);
                        break;
                    case 8:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQHg"), 8);
                        break;
                    case 9:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQIA"), 9);
                        break;
                    case 10:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQFg"), 10);
                        break;
                    case 11:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQGg"), 11);
                        break;
                    case 12:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQDg"), 12);
                        break;
                    case 13:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQEg"), 13);
                        break;
                    case 14:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQFw"), 14);
                        break;
                    case 15:
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(google_api_client,
                                "CgkIl-nPp9wBEAIQGw"), 15);
                        break;
                    default:
                        Toast.makeText(LoginActivity.this, "Cannot find leaderboard", Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                Toast.makeText(this, "Cannot load leaderboard. Please try again or restart app.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Cannot load leaderboard. No Google API found.", Toast.LENGTH_SHORT).show();
        }
    }
}