package com.prembros.programming.quizapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

public class Results extends Fragment implements OnChartValueSelectedListener {

    private PieChart mChart;
    private boolean pieError = false;
    private DatabaseHolder dbHandler;
    private OnFragmentInteractionListenerInResults mListener;
    private RelativeLayout baseRelativeLayout;
    private ActionBar ab;
    public static boolean isFragmentActive = false;
    public static ResultsInDetail resultsInDetail;
    int correctAnswers = Questions.CORRECT_ANSWERS;
    int incorrectAnswers = Questions.INCORRECT_ANSWERS;
    int questionCount = Questions.QUESTION_COUNT;
    int skippedAnswers = Questions.SKIPPED_ANSWERS;

    @Override
    public void onAttach(Context context) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onAttach(context);
        if (correctAnswers < 0 || incorrectAnswers < 0 || skippedAnswers < 0 || questionCount == 0) {
            pieError = true;
            pieDisplayError(correctAnswers, incorrectAnswers, skippedAnswers, questionCount);
        }
        setHasOptionsMenu(true);
        if (context instanceof OnFragmentInteractionListenerInResults) {
            mListener = (OnFragmentInteractionListenerInResults) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        isFragmentActive = true;
        if (!pieError) {
            View rootView = inflater.inflate(R.layout.fragment_results, container, false);

            mListener.onFragmentInteractionInResults("submitScore");

            mListener.onFragmentInteractionInResults("unlockAchievements");

            baseRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.result_page);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String fieldDisplay = "nothing";
                    if (Questions.selections != null) {
                        fieldDisplay = Questions.selections[1] + " : " + Questions.selections[0];
                    }
                    CustomTextViewSemiLight fieldText = (CustomTextViewSemiLight) baseRelativeLayout.findViewById(R.id.field_text);
                    CustomTextViewLight scorePoints = (CustomTextViewLight) baseRelativeLayout.findViewById(R.id.score_points);
                    String scorePointText = "Score: " + Questions.SCORE;
                    if (correctAnswers == questionCount) {
                        baseRelativeLayout.findViewById(R.id.app_name_bottom).setVisibility(View.GONE);
                        String fullScore = "Wow! you're the master of " + fieldDisplay + "!\nNow try another quiz and master that too!";
                        fieldText.setTextSize(16);
                        fieldText.setText(fullScore);
                        scorePoints.setText(scorePointText);
                        fieldText.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.zoom_in));
                        scorePoints.setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                    }
                    else if (skippedAnswers == questionCount){
                        baseRelativeLayout.findViewById(R.id.app_name_bottom).setVisibility(View.GONE);
                        String skip = "I think I should be SkipApp instead of QuizApp!";
                        fieldText.setTextSize(16);
                        fieldText.setText(skip);
                        scorePoints.setText(scorePointText);
                        fieldText.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.zoom_in));
                        scorePoints.setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                    }
                    else if (incorrectAnswers == questionCount){
                        baseRelativeLayout.findViewById(R.id.app_name_bottom).setVisibility(View.GONE);
                        String ohMyGod = "Can anyone be more bummed than you?";
                        fieldText.setTextSize(16);
                        fieldText.setText(ohMyGod);
                        scorePoints.setText(scorePointText);
                        fieldText.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.zoom_in));
                        scorePoints.setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                    }
                    else {
                        CustomTextViewSemiLight scoreText = (CustomTextViewSemiLight) baseRelativeLayout.findViewById(R.id.score_marks);
                        fieldText.setText(fieldDisplay);
                        String scoreDisplay = Questions.CORRECT_ANSWERS + "/" + Questions.QUESTION_COUNT;
                        scoreText.setText(scoreDisplay);
                        scorePoints.setText(scorePointText);
                        fieldText.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                        scoreText.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                        scorePoints.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
                    }
                }
            }, 2000);

//        SET UP ACTION BAR
            ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
            if (ab != null) {
                ab.setDisplayShowHomeEnabled(false);
                ab.setDisplayHomeAsUpEnabled(false);
                ab.setTitle(R.string.results);
                ab.setSubtitle("");
            }

            Difficulty.BACK_FROM_RESULTS = 2;
            dbHandler = new DatabaseHolder(getContext());

//        CREATE THE RESULT PIE CHART
            mChart = (PieChart) baseRelativeLayout.findViewById(R.id.resultPieChart);
            mChart.setVisibility(View.INVISIBLE);
            createPieChart();
            mChart.setMinimumHeight(mChart.getWidth());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mChart.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fragment_anim_in));
                    mChart.setVisibility(View.VISIBLE);
                }
            }, 400);
        }
        return baseRelativeLayout;
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
        mChart.setEntryLabelTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/seguil.ttf"));
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
        data.setValueTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/seguisl.ttf"));
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    public void pieDisplayError(int correctAnswers, int incorrectAnswers, int skippedAnswers, int questionCount){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                mListener.onFragmentInteractionInResults("finishResultsFragment");
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
        resultsInDetail = new ResultsInDetail();
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
                ab.hide();
                mChart.highlightValues(null);
            }
        }, 150);
//        show Results in detail
        mListener.onFragmentInteractionInResults("showResultsInDetail");
    }

    @Override
    public void onNothingSelected() {
    }

    @Override
    public void onDestroyView() {
        isFragmentActive = false;
        resetFlags();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_results, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_account:
                startActivity(new Intent(getContext(), LoginActivity.class));
                break;
            case R.id.action_share:
                Bitmap bm = getScreenshot(baseRelativeLayout);
                File imageFile = store(bm, "QuizResult.jpeg");
                shareImage(imageFile);
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
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            Uri fileUri = FileProvider.getUriForFile(getContext(),
                    "com.prembros.programming.quizapp.myfileprovider", file);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            getContext().startActivity(Intent.createChooser(shareIntent, "Share your QuizResult"));
        }
        else {
            Uri uri = Uri.fromFile(file);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");

            intent.putExtra(Intent.EXTRA_SUBJECT, "Have you tried QuizApp?");
            intent.putExtra(Intent.EXTRA_TEXT,
                    "I just took a " + Questions.selections[1] + " " + Questions.selections[0] + " quiz on QuizApp - Programming." + "QuizApp comes with great programming quizzes," +
                            "\nGet the app here: https://goo.gl/f8QABD \n");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            try {
                startActivity(Intent.createChooser(intent, "Share your QuizResult"));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "No app available!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public interface OnFragmentInteractionListenerInResults {
        void onFragmentInteractionInResults(String action);
    }
}