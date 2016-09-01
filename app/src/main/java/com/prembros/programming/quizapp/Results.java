package com.prembros.programming.quizapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.achievement.Achievements;
import com.kobakei.ratethisapp.RateThisApp;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

public class Results extends LoginActivity implements OnChartValueSelectedListener,
        ResultsInDetail.OnFragmentInteractionListener {

    private PieChart mChart;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean pieError = false;
    private DatabaseHolder dbHandler;
    RelativeLayout rootView;
    int correctAnswers = Questions.CORRECT_ANSWERS;
    int incorrectAnswers = Questions.INCORRECT_ANSWERS;
    int questionCount = Questions.QUESTION_COUNT;
    int skippedAnswers = (questionCount - (correctAnswers + incorrectAnswers));

    @Override
    protected void onResume() {
        super.onResume();
        if (correctAnswers < 0 || incorrectAnswers < 0 || skippedAnswers < 0 || questionCount == 0) {
            pieError = true;
            pieDisplayError(correctAnswers, incorrectAnswers, skippedAnswers, questionCount);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        if (progress_dialog!=null) progress_dialog.dismiss();

        if (!pieError) {
            setContentView(R.layout.activity_results);

            /*
            * Submit score to leaderboard
            */
            if (isStoreVersion(getApplicationContext())) {
                if (google_api_client != null && google_api_client.isConnected() && Questions.SCORE > 0)
                    Games.Leaderboards.submitScore(google_api_client,
                            getLeaderboardID(Questions.selections[0], Questions.selections[1]), Questions.SCORE);
                else Toast.makeText(Results.this, "Can't upload score", Toast.LENGTH_SHORT).show();
            } else Toast.makeText(Results.this,
                    "Download this app from Play Store to Participate in Leaderboards", Toast.LENGTH_LONG).show();

            /*
            * Unlock leaderboard if any
            */
            Achievements achievements = Games.Achievements;
            if (correctAnswers == questionCount){
                achievements.unlock(google_api_client, "CgkIl-nPp9wBEAIQAg");           //achievement_beginners_luck
                achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQAw", 1);     //achievement_streak_of_5
                achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQBA", 1);     //achievement_streak_of_15
                achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQBQ", 1);     //achievement_streak_of_30
                achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQIg", 1);     //achievement_streak_of_50
                achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQIw", 1);     //achievement_streak_of_100
                achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQJA", 1);     //achievement_streak_if_150
                achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQJQ", 1);     //achievement_streak_of_200
            }
            else if (incorrectAnswers == questionCount){
                achievements.unlock(google_api_client, "CgkIl-nPp9wBEAIQBg");           //achievement_bummer_star
                achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQBw", 1);     //achievement_bummer_king
                achievements.increment(google_api_client, "CgkIl-nPp9wBEAIQCA", 1);     //achievement_emperor_of_bummerville
            }
            else if (correctAnswers == (questionCount/2)){
                achievements.unlock(google_api_client, "CgkIl-nPp9wBEAIQJg");           //achievement_positive_halfsies
            }
            else if (incorrectAnswers == (questionCount/2)){
                achievements.unlock(google_api_client, "CgkIl-nPp9wBEAIQJw");           //achievement_negative_halfsies
            }
            else if (skippedAnswers == questionCount){
                achievements.unlock(google_api_client, "CgkIl-nPp9wBEAIQIQ");           //achievement_skippy
            }

            rootView = (RelativeLayout) findViewById(R.id.result_page);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String fieldDisplay = "nothing";
                    if (Questions.selections != null) {
                        fieldDisplay = Questions.selections[1] + " : " + Questions.selections[0];
                    }
                    CustomTextViewSemiLight fieldText = (CustomTextViewSemiLight) rootView.findViewById(R.id.field_text);
                    CustomTextViewLight scorePoints = (CustomTextViewLight) rootView.findViewById(R.id.score_points);
                    String scorePointText = "Score: " + Questions.SCORE;
                    if (correctAnswers == questionCount) {
                        String fullScore = "Wow! you're the master of " + fieldDisplay + "!\nNow try another quiz and master that too!";
                        fieldText.setTextSize(16);
                        fieldText.setText(fullScore);
                        scorePoints.setText(scorePointText);
                        fieldText.startAnimation(AnimationUtils.loadAnimation(Results.this, R.anim.zoom_in));
                        scorePoints.setAnimation(AnimationUtils.loadAnimation(Results.this, android.R.anim.fade_in));
                    }
                    else if (skippedAnswers == questionCount){
                        String skip = "I think I should be SkipApp instead of QuizApp!";
                        fieldText.setTextSize(16);
                        fieldText.setText(skip);
                        scorePoints.setText(scorePointText);
                        fieldText.startAnimation(AnimationUtils.loadAnimation(Results.this, R.anim.zoom_in));
                        scorePoints.setAnimation(AnimationUtils.loadAnimation(Results.this, android.R.anim.fade_in));
                    }
                    else if (incorrectAnswers == questionCount){
                        String ohMyGod = "Can anyone be more bummed than you?";
                        fieldText.setTextSize(16);
                        fieldText.setText(ohMyGod);
                        scorePoints.setText(scorePointText);
                        fieldText.startAnimation(AnimationUtils.loadAnimation(Results.this, R.anim.zoom_in));
                        scorePoints.setAnimation(AnimationUtils.loadAnimation(Results.this, android.R.anim.fade_in));
                    }
                    else {
                        CustomTextViewSemiLight scoreText = (CustomTextViewSemiLight) rootView.findViewById(R.id.score_marks);
                        fieldText.setText(fieldDisplay);
                        String scoreDisplay = Questions.CORRECT_ANSWERS + "/" + Questions.QUESTION_COUNT;
                        scoreText.setText(scoreDisplay);
                        scorePoints.setText(scorePointText);
                        fieldText.startAnimation(AnimationUtils.loadAnimation(Results.this, android.R.anim.fade_in));
                        scoreText.startAnimation(AnimationUtils.loadAnimation(Results.this, android.R.anim.fade_in));
                        scorePoints.startAnimation(AnimationUtils.loadAnimation(Results.this, android.R.anim.fade_in));
                    }
                }
            }, 2000);

            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.results);

            Difficulty.BACK_FROM_RESULTS = 2;
            dbHandler = new DatabaseHolder(getApplicationContext());

//        CREATE THE RESULT PIE CHART
            mChart = (PieChart) findViewById(R.id.resultPieChart);
            mChart.setVisibility(View.INVISIBLE);
            createPieChart();
            mChart.setMinimumHeight(mChart.getWidth());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mChart.startAnimation(AnimationUtils.loadAnimation(Results.this, R.anim.fragment_anim_in));
                    mChart.setVisibility(View.VISIBLE);
                }
            }, 400);
        }
    }

    public static boolean isStoreVersion(Context context) {
        String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        return !TextUtils.isEmpty(installer);
    }

    public void createPieChart(){
//        sets the description of the chart in the bottom right corner:
        mChart.setDescription("");
//        mChart.setDescription("Total questions: " + Questions.QUESTION_COUNT);
//        mChart.setDescriptionColor(Color.WHITE);

        mChart.setExtraOffsets(2, 2, 2, 2);
        mChart.setDragDecelerationFrictionCoef(0.98f);

//        CENTER HOLE SETTINGS
        mChart.setTransparentCircleAlpha(0);
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleRadius(20f);
        mChart.setHoleColor(Color.argb(0, 0, 0, 0));

        mChart.setRotationAngle(0);
//        enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        setData();

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);

//        Legend l = mChart.getLegend();
//        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
//        l.setXEntrySpace(7f);
//        l.setYEntrySpace(0f);
//        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTypeface(Typeface.createFromAsset(getAssets(), "fonts/seguil.ttf"));
        mChart.setEntryLabelTextSize(14f);

//        CODE FOR DISPLAYING CENTER TEXT(commented out):

//        mChart.setCenterTextTypeface(Typeface.createFromAsset(getAssets(), "fonts/seguisl.ttf"));
//        mChart.setCenterText(generateCenterSpannableText());

//        mChart.setDrawHoleEnabled(true);
//        mChart.setHoleColor(Color.WHITE);

//        mChart.setHoleRadius(58f);
//        mChart.setTransparentCircleRadius(61f);

//        mChart.setDrawCenterText(true);
    }

    private void setData() {

        ArrayList<PieEntry> entries = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of the chart.
        if (correctAnswers > 0) {
            if (correctAnswers == questionCount){
                entries.add(new PieEntry(correctAnswers, "All correct!"));
            }
            else entries.add(new PieEntry(Questions.CORRECT_ANSWERS, "Correct"));
        }
        if (incorrectAnswers > 0) {
            if (incorrectAnswers == questionCount){
                entries.add(new PieEntry(incorrectAnswers, "All incorrect!"));
            }
            else entries.add(new PieEntry(Questions.INCORRECT_ANSWERS, "Incorrect"));
        }
        if (skippedAnswers > 0) {
            if (skippedAnswers == questionCount){
                entries.add(new PieEntry(skippedAnswers, "All skipped!"));
            }
            else entries.add(new PieEntry(skippedAnswers, "Skipped"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "QuizResult");
        dataSet.setSliceSpace(20f);
        dataSet.setSelectionShift(15f);

//         add colors
        ArrayList<Integer> colors = new ArrayList<>();
//         order: correct, incorrect, skipped

        if (correctAnswers != 0 && incorrectAnswers != 0 && skippedAnswers != 0){
            colors.add(Color.argb(255, 156, 204, 101));         //for correct answers
            colors.add(Color.argb(255, 239, 83, 80));           //for incorrect answers
            colors.add(Color.argb(255, 7, 107, 112));           //for skipped answers
        }
        else if (correctAnswers == 0){
            if (incorrectAnswers == 0){
                colors.add(Color.argb(255, 7, 107, 112));           //for skipped answers
            }
            else {
                colors.add(Color.argb(255, 239, 83, 80));           //for incorrect answers
                colors.add(Color.argb(255, 7, 107, 112));           //for skipped answers
            }
        }
        else if (incorrectAnswers == 0){
            if (skippedAnswers == 0){
                colors.add(Color.argb(255, 156, 204, 101));         //for correct answers
            }
            else {
                colors.add(Color.argb(255, 156, 204, 101));         //for correct answers
                colors.add(Color.argb(255, 7, 107, 112));           //for skipped answers
            }
        }
        else if (skippedAnswers == 0){
            if (correctAnswers == 0){
                colors.add(Color.argb(255, 239, 83, 80));           //for incorrect answers
            }
            else {
                colors.add(Color.argb(255, 156, 204, 101));         //for correct answers
                colors.add(Color.argb(255, 239, 83, 80));           //for incorrect answers
            }
        }
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new DefaultValueFormatter(0));
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(Typeface.createFromAsset(getAssets(), "fonts/seguisl.ttf"));
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    public void pieDisplayError(int correctAnswers, int incorrectAnswers, int skippedAnswers, int questionCount){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ERROR!");
        builder.setMessage("Sorry but the results couldn't be loaded. Please take the quiz again." +
                "\nIf problem persists, contact us with these details:\n" +
                "\nTotal Questions = " + questionCount +
                "\nCorrect answers = " + correctAnswers +
                "\nIncorrect answers = " + incorrectAnswers +
                "\nSkipped answers = " + skippedAnswers +
                "\n\nYou can contact us via email given in about section."
        );
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Results.this.finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void resetFlags() {
        Questions.QUESTION_COUNT = 0;
        Questions.CORRECT_ANSWERS = 0;
        Questions.INCORRECT_ANSWERS = 0;
        dbHandler.open();
        dbHandler.resetTables();
        dbHandler.close();
    }

/*

    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString("Your result");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, 14, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
        s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
        return s;
    }
*/

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.i("VAL SELECTED", "Value: " + e.getY()
                + ", index: " + h.getX()
                + ", DataSet index: " + h.getDataSetIndex());
        ResultsInDetail resultsInDetail = new ResultsInDetail();
        Bundle args = new Bundle();
        if (correctAnswers == 0) {                                                ////INCORRECT AND SKIPPED
            if (incorrectAnswers == 0){
                args.putInt(ResultsInDetail.ARG_ENTRY, ((int) h.getX() + 2));       //skipped clicked
                resultsInDetail.setArguments(args);
            }
            else {
                args.putInt(ResultsInDetail.ARG_ENTRY, (int) h.getX() + 1);         //Skipped or incorrect clicked
                resultsInDetail.setArguments(args);
            }
        }
        else if (incorrectAnswers == 0){                                          ////CORRECT AND SKIPPED
            if (h.getX() != 1) {
                args.putInt(ResultsInDetail.ARG_ENTRY, (int) h.getX());             //correct clicked
                resultsInDetail.setArguments(args);
            }
            else {
                args.putInt(ResultsInDetail.ARG_ENTRY, ((int) h.getX() + 1));       //skipped clicked
                resultsInDetail.setArguments(args);
            }
        }
        else{
            args.putInt(ResultsInDetail.ARG_ENTRY, (int) h.getX());             ////ALL THREE AVAILABLE
            resultsInDetail.setArguments(args);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //noinspection ConstantConditions
                getSupportActionBar().hide();
                mChart.highlightValues(null);
            }
        }, 150);

        if(ResultsInDetail.isFragmentActive){
            ResultsInDetail.isFragmentActive = false;
            getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("resultsInDetail")).commit();
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, resultsInDetail, "resultsInDetail").commit();
        // undo all highlights
    }

    @Override
    public void onNothingSelected() {
    }

    @Override
    protected void onDestroy() {
        resetFlags();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_results, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_account:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.action_share:
                Bitmap bm = getScreenshot(rootView);
                File imageFile = store(bm, "QuizResult.jpeg");
                shareImage(imageFile);
                break;
            case R.id.action_bookmark:
                startActivity(new Intent(this, Bookmarks.class));
                break;
            case R.id.action_get_pro:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.get_pro);
                alert.setMessage(R.string.get_pro_content);
                alert.setPositiveButton("Get Pro", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(Results.this, R.string.get_pro_redirect, Toast.LENGTH_LONG).show();
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
            case R.id.action_leaderboard:
                getAndRemoveActiveFragment(LEADERBOARD_TEXT);
                loadFragment(LEADERBOARD_TEXT);
                break;
            case R.id.action_achievements:
                loadFragment(ACHIEVEMENTS_TEXT);
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
        }
        return true;
    }

//    public static Bitmap takeScreenShot(Activity activity)
//    {
//        View view = activity.getWindow().getDecorView();
//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap b1 = view.getDrawingCache();
//        Rect frame = new Rect();
//        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
//        int statusBarHeight = frame.top;
//        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
//        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
//
//        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height  - statusBarHeight);
//        view.destroyDrawingCache();
//        return b;
//    }

    //    CAPTURE THE View
    public static Bitmap getScreenshot(View view){
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    //    STORE THE BITMAP INTO SD CARD
    public File store(Bitmap bitmapImage, String filename){
        String dateString = (android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", new Date())).toString();
        dateString = dateString.replace(":","_");

        final String dirPath = Environment.getExternalStorageDirectory(). getAbsolutePath() + "/QuizApp";
        File screenshotFile = new File(dirPath);

        if (!screenshotFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            screenshotFile.mkdirs();
        }
        File file = new File(dirPath, dateString + "_" + filename);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    //    SHARE THE IMAGE OF CURRENT ACTIVITY
    private void shareImage(File file){
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(Intent.EXTRA_SUBJECT, "Have you tried QuizApp?");
        intent.putExtra(Intent.EXTRA_TEXT,
                "I just took a " + Questions.selections[1] + " " + Questions.selections[0] + " quiz on QuizApp - Programming." + "QuizApp comes with great programming quizzes," +
                        "\nGet the app here: https://goo.gl/f8QABD \n");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try{
            startActivity(Intent.createChooser(intent, "Share your QuizResult"));
        } catch (ActivityNotFoundException e){
            Toast.makeText(Results.this, "No app available!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Difficulty.BACK_FROM_RESULTS = 2;
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
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();

        if(backStackCount >= 1){
            //noinspection ConstantConditions
            getSupportActionBar().show();
            getSupportFragmentManager().popBackStackImmediate();
            return;
        }
        if(ResultsInDetail.isFragmentActive){
            onFragmentInteraction();
        }
        else {
            //Place Ads

            ResultsInDetail.isFragmentActive = false;
            if (doubleBackToExitPressedOnce) {
                resetFlags();
                super.onBackPressed();
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
    }

    @Override
    public void onFragmentInteraction() {
        ResultsInDetail.isFragmentActive = false;
        ResultsInDetail.rootView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fragment_anim_out));
        //noinspection ConstantConditions
        getSupportActionBar().show();
        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("resultsInDetail")).commit();
    }
}