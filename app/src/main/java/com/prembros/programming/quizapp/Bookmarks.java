package com.prembros.programming.quizapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.kobakei.ratethisapp.RateThisApp;

import java.util.ArrayList;
import java.util.List;

public class Bookmarks extends AppCompatActivity {

    public static ViewPager mViewPager;
    private InterstitialAd mInterstitialAd;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        /*
      The {@link android.support.v4.view.PagerAdapter} that will provide
      fragments for each of the sections. We use a
      {@link FragmentPagerAdapter} derivative, which will keep every
      loaded fragment in memory. If this becomes too memory intensive, it
      may be best to switch to a
      {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.bookmarks);
        }

        // Set up the ViewPager with the sections adapter.
        /*
      The {@link ViewPager} that will host the section contents.
     */
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //Set up ads
        mInterstitialAd = new InterstitialAd(this);
        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.int_add_full_bookmark));

        requestNewInterstitial();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showInterstitial();
            }
        }, 5000);

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bookmarks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_get_pro:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.get_pro);
                alert.setMessage(R.string.get_pro_content);
                alert.setPositiveButton("Get Pro", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(Bookmarks.this, R.string.get_pro_redirect, Toast.LENGTH_LONG).show();
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
            case R.id.action_rate_this_app:
                RateThisApp.showRateDialog(this);
                break;
            case R.id.action_clear_bookmarks:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete all");
                builder.setMessage("Sure to clear everything here?\n\nThis will delete ALL bookmarks!");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mViewPager.removeAllViews();
                        DatabaseHolder db = new DatabaseHolder(Bookmarks.this);
                        db.open();
                        db.deleteAllQuestions();
                        db.close();
                        Toast.makeText(Bookmarks.this, "All bookmarks deleted!", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                break;
            case R.id.action_about:
                if(About.isFragmentActive){
                    About.isFragmentActive = false;
                    getSupportFragmentManager().beginTransaction().remove(
                            getSupportFragmentManager().findFragmentByTag("about")).commit();
                }
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new About(), "about").commit();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //noinspection ConstantConditions
                        getSupportActionBar().hide();
                    }
                }, 350);
                break;
            case R.id.action_help:
                if(Help.isFragmentActive){
                    Help.isFragmentActive = false;
                    getSupportFragmentManager().beginTransaction().remove(
                            getSupportFragmentManager().findFragmentByTag("help")).commit();
                }
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new Help(), "help").commit();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //noinspection ConstantConditions
                        getSupportActionBar().hide();
                    }
                }, 350);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    public void removeQuestion(int position) {
//        mViewPager.removeViewAt(position);
//    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onBackPressed() {
        if (About.isFragmentActive){
            About.isFragmentActive = false;
            About.rootView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fragment_anim_out));
            getSupportActionBar().show();
            getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("about")).commit();
            return;
        }
        if (Help.isFragmentActive){
            Help.isFragmentActive = false;
            Help.rootView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fragment_anim_out));
            getSupportActionBar().show();
            getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("help")).commit();
            return;
        }
        super.onBackPressed();
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        static Context staticContext;
        static ListView listView;
        static List<String> listDataHeader;
        static List<String> listDataChild;
        View rootView;
        TextView textView;
        static int sectionNumber;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_bookmarks, container, false);
            staticContext = getContext();
            sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

            textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(String.valueOf(sectionNumber));

            listView = (ListView) rootView.findViewById(R.id.bookmarks_listView);
            listItemPopulate();
            return rootView;
        }

        public void listItemPopulate() {
            List<String> listHeader;
            List<String> listChild;
            listHeader = new ArrayList<>();
//            listDataHeader.clear();
            listChild = new ArrayList<>();
//            listDataChild.clear();
            DatabaseHolder dbHandler = new DatabaseHolder(staticContext);
            dbHandler.open();
            Cursor bookmarkedQuestions = dbHandler.returnBookmarkedQuestion(getField(getArguments().getInt(ARG_SECTION_NUMBER)));
            bookmarkedQuestions.moveToFirst();

//            int location = 0;

//            POPULATING THE listView
            while (!bookmarkedQuestions.isAfterLast()) {
                // Adding header data (location +1 ) + ": " +
                listHeader.add(bookmarkedQuestions.getString(bookmarkedQuestions.getColumnIndex("question")));
                listChild.add("Answer: " + bookmarkedQuestions.getString(bookmarkedQuestions.getColumnIndex("answer")));
//                location++;
                bookmarkedQuestions.moveToNext();
            }
            dbHandler.close();

            listDataHeader = listHeader;
            listDataChild = listChild;
            ListAdapter listAdapter;

            if (listHeader.size() > 0) {
                listAdapter = new BookmarksAdapter(staticContext, listHeader, listChild);
                listView.setAdapter(listAdapter);
                listView.setLayoutAnimation(
                        new LayoutAnimationController(
                                AnimationUtils.loadAnimation(staticContext, R.anim.fragment_anim_in)
                                , 0.25F
                        )
                );
            }
            else {
                String hText = "No Bookmark added.";
                String cText = "To add a bookmark click the bookmark icon on the top right corner during the quiz.";

                List<String> headerText = new ArrayList<>();
                List<String> childText = new ArrayList<>();
                headerText.add(hText);
                childText.add(cText);

                listAdapter = new BookmarksAdapter(staticContext, headerText, childText);

                listView.setAdapter(listAdapter);
            }
        }

        public static void listItemInvalidate(){
            String hText = "No Bookmark added.";
            String cText = "To add a bookmark click the bookmark icon on the top right corner during the quiz.";

            List<String> headerText = new ArrayList<>();
            List<String> childText = new ArrayList<>();
            headerText.add(hText);
            childText.add(cText);

            ListAdapter listAdapter = new BookmarksAdapter(staticContext, headerText, childText);

            listView.setAdapter(listAdapter);
        }

        public static String getField(int n) {
            String field = null;
            switch (n) {
                case 1:
                    field = "iOS";
                    break;
                case 2:
                    field = "Java";
                    break;
                case 3:
                    field = "HTML";
                    break;
                case 4:
                    field = "JavaScript";
                    break;
                default:
                    break;
            }
            return field;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "iOS";
                case 1:
                    return "Java";
                case 2:
                    return "HTML";
                case 3:
                    return "JavaScript";
                default:
                    return null;
            }
        }
    }
}