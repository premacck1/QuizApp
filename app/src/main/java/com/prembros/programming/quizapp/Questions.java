package com.prembros.programming.quizapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.Stack;

public class Questions extends Fragment implements OnClickListener, OnLongClickListener {

    public static int CORRECT_ANSWERS = 0,
            INCORRECT_ANSWERS = 0,
            QUESTION_COUNT = 0,
            SKIPPED_ANSWERS = 0;
    public static long SCORE = 0;
    public static String[] selections;
    public static boolean isFragmentActive = false;
    public static boolean wannaGoToHome = false;
    static String FIELD_ARG = "fieldSelection";
    static String DIFFICULTY_ARG = "difficultySelection";
    private OnFragmentInteractionListener mListener;
    private boolean previousPressed = false;
    private boolean doubleBackToSkip = false;
    private String selectedOption, answer;
    private Stack<QuestionBean> previousQuestion = new Stack<>();
    private Stack<String> previousAnswer = new Stack<>();
    private DatabaseHolder dbHandler;
    private ArrayList<QuestionBean> questionList;
    private QuestionBean questionBean;
    private TextView question;
    private ImageButton fabPrevious, fabSkip, fabNext;
    //    private CustomTextViewLight timer;
    private ToggleButton addBookmark;
    private CheckedTextView option1;
    private CheckedTextView option2;
    private CheckedTextView option3;
    private CheckedTextView option4;
    private CheckedTextView[] allCheckedTextViews;
    private ProgressBar questionProgressBar, timeProgressBar;
//    private InterstitialAd mInterstitialAd3;
    private InterstitialAd mInterstitialAd1;
    private InterstitialAd mInterstitialAd2;
    private View rootView;
    private CountDownTimer countDownTimer;
    private int timeTaken = 1;
    private int totalTime;

    public Questions() {
    }

    @Override
    public void onResume() {
        setHasOptionsMenu(true);
        resumeBookmarkAction(addBookmark);
        super.onResume();
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
        rootView = inflater.inflate(R.layout.fragment_questions, container, false);
//        GET THE SELECTED FIELD AND DIFFICULTY.
        Bundle b = this.getArguments();
        selections = new String[]{String.valueOf(b.get(FIELD_ARG)), String.valueOf(b.get(DIFFICULTY_ARG))};

//        GET THE QUESTIONS CORRESPONDING TO THE SELECTED FIELD AND DIFFICULTY
        questionList = b.getParcelableArrayList("Question");
        if (questionList != null) {
            rootView = inflater.inflate(R.layout.fragment_questions, container, false);

//        INSTANTIATE ALL THE VIEWS IN THIS ACTIVITY.
            instantiate();

            Difficulty.BACK_FROM_RESULTS = 0;
            dbHandler = new DatabaseHolder(getContext());

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    countDownTimer.start();
                }
            }, 1000);

            questionBean = questionList.get(QUESTION_COUNT);
            if (questionProgressBar != null) {
                questionProgressBar.setMax(questionList.size());
            }

            populate();

//            Setting the total time according to difficulty. Higher the difficulty, less the total time.
            switch (selections[1]){
                case "Rookie":
                    totalTime = questionList.size() * 30000;
                    break;
                case "Apprentice":
                    totalTime = questionList.size() * 20000;
                    break;
                case "Pro":
                    totalTime = questionList.size() * 10000;
                    break;
                case "Hitman":
                    totalTime = questionList.size() * 5000;
                    break;
                default:
                    totalTime = questionList.size() * 20000;
                    break;
            }
            if (timeProgressBar != null) {
                timeProgressBar.setMax(totalTime);
            }

            countDownTimer = new CountDownTimer(totalTime, 100){

                @Override
                public void onTick(long timeElapsed) {
                    timeTaken = ((int) (totalTime - timeElapsed));
                    timeProgressBar.setProgress(timeTaken);
                }

                @Override
                public void onFinish() {
                    onCompletion(false);
                }
            };

//        SET UP ACTION BAR
            android.support.v7.app.ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
            if (ab != null) {
                ab.setDisplayShowHomeEnabled(true);
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setTitle(selections[0]);
                ab.setSubtitle(selections[1]);
            }

            answer = questionBean.getAnswer();

            allCheckedTextViews = new CheckedTextView[]{option1, option2, option3, option4};

            //Set up ads
            mInterstitialAd1 = new InterstitialAd(getContext());
            // set the ad unit ID
            mInterstitialAd1.setAdUnitId(getString(R.string.int_add_full));

            //Set up ads
            mInterstitialAd2 = new InterstitialAd(getContext());
            // set the ad unit ID
            mInterstitialAd2.setAdUnitId(getString(R.string.int_add_full1));

/*            //Set up ads
            mInterstitialAd3 = new InterstitialAd(getContext());
            // set the ad unit ID
            mInterstitialAd3.setAdUnitId(getString(R.string.int_add_full2));*/

            requestNewInterstitial();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showInterstitial1();
                }
            }, 10000);

//        SET ON CLICK AND ON LONG CLICK LISTENERS
            setClickListeners();

           /* mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                }
            });*/
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Sorry, but the questions couldn't be loaded.");
            builder.setCancelable(false);
            builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mListener.onFragmentInteraction("dismiss");
        super.onViewCreated(view, savedInstanceState);
    }

    public void setClickListeners(){
        option1.setOnClickListener(this);
        option2.setOnClickListener(this);
        option3.setOnClickListener(this);
        option4.setOnClickListener(this);
        addBookmark.setOnClickListener(this);
        fabNext.setOnClickListener(this);
        fabPrevious.setOnClickListener(this);
        fabSkip.setOnClickListener(this);
        addBookmark.setOnLongClickListener(this);
        fabNext.setOnLongClickListener(this);
        fabPrevious.setOnLongClickListener(this);
        fabSkip.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.checked_choice_button1:
                clickAction(option1);
                break;
            case R.id.checked_choice_button2:
                clickAction(option2);
                break;
            case R.id.checked_choice_button3:
                clickAction(option3);
                break;
            case R.id.checked_choice_button4:
                clickAction(option4);
                break;
            case R.id.addBookmark:
                bookmarkAction(addBookmark);
                break;
            case R.id.fabNext:
                if (selectedOption != null){
                    dbHandler.open();
                    if (selectedOption.equalsIgnoreCase(answer)){
                        dbHandler.insertCorrectAnswer(question.getText().toString(), selectedOption);
                    }
                    else {
                        dbHandler.insertIncorrectAnswer(question.getText().toString(), selectedOption, answer);
                    }
                    dbHandler.close();

                    showNextQuestion();
                }
                else Toast.makeText(getContext(), "Select an answer first.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fabSkip:
                if (doubleBackToSkip) {
                    dbHandler.open();
                    dbHandler.insertSkippedAnswer(question.getText().toString(), answer);
                    dbHandler.close();
                    showNextQuestion();
                    fabSkip.setAlpha(0.6F);
                    return;
                }

                fabSkip.setAlpha(1.0F);
                doubleBackToSkip = true;
                Toast.makeText(getContext(), "Hit again to skip this question", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fabSkip.setAlpha(0.6F);
                        doubleBackToSkip = false;
                    }
                }, 2000);
                break;
            case R.id.fabPrevious:
                if (previousQuestion.size()>0) {
                    previousPressed = true;
                    questionBean = previousQuestion.pop();
                    populate();
                    answer = questionBean.getAnswer();
                    selectedOption = previousAnswer.pop();
                    getSelectedAnswer(selectedOption);
                    String currentQuestion = question.getText().toString();

                    dbHandler.open();
                    int isQuestionPresentInAnswersTable = dbHandler.isQuestionPresentInAnswersTable(currentQuestion);
                    if (isQuestionPresentInAnswersTable < 0)
                        dbHandler.deleteQuestion(-1, currentQuestion);
                    else if (isQuestionPresentInAnswersTable > 0)
                        dbHandler.deleteQuestion(1, currentQuestion);
                    else dbHandler.deleteQuestion(0, currentQuestion);
                    dbHandler.close();
                }
                else{
                    Toast.makeText(getContext(), "This is the first Question", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    //        HELPER TOASTS IF USER LONG PRESSES ON THE NAVIGATION BUTTONS
    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()){
            case R.id.addBookmark:
                Toast.makeText(getContext(), "Add / Remove bookmark", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.fabNext:
                Toast.makeText(getContext(), "Next question", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.fabPrevious:
                Toast.makeText(getContext(), "Previous question", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.fabSkip:
                Toast.makeText(getContext(), "Skip this question", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }

    public void resetFlags() {
        QUESTION_COUNT = 0;
        CORRECT_ANSWERS = 0;
        INCORRECT_ANSWERS = 0;
        dbHandler.open();
        dbHandler.resetTables();
        dbHandler.close();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void getSelectedAnswer(String selectedOption){
        if (selectedOption != null) {
            if (selectedOption.equals(option1.getText().toString())) {
                clickAction(option1);
            } else if (selectedOption.equals(option2.getText().toString())) {
                clickAction(option2);
            } else if (selectedOption.equals(option3.getText().toString())) {
                clickAction(option3);
            } else if (selectedOption.equals(option4.getText().toString())) {
                clickAction(option4);
            }
        }
    }

    public void instantiate(){
        questionProgressBar = (ProgressBar) rootView.findViewById(R.id.question_progressBar);
        timeProgressBar = (ProgressBar) rootView.findViewById(R.id.time_progressBar);
        addBookmark = (ToggleButton) rootView.findViewById(R.id.addBookmark);
        question = (TextView) rootView.findViewById(R.id.question_textView);
        fabPrevious = (ImageButton) rootView.findViewById(R.id.fabPrevious);
        fabSkip = (ImageButton) rootView.findViewById(R.id.fabSkip);
        fabNext = (ImageButton) rootView.findViewById(R.id.fabNext);
        option1 = (CheckedTextView) rootView.findViewById(R.id.checked_choice_button1);
        option2 = (CheckedTextView) rootView.findViewById(R.id.checked_choice_button2);
        option3 = (CheckedTextView) rootView.findViewById(R.id.checked_choice_button3);
        option4 = (CheckedTextView) rootView.findViewById(R.id.checked_choice_button4);
    }

    public void populate(){
        if (previousPressed) {
            questionProgressBar.setProgress(QUESTION_COUNT);
            previousPressed = false;
        }
        else questionProgressBar.setProgress(QUESTION_COUNT+1);
        question.setText(questionBean.getQuestion().trim());
        option1.setText(questionBean.getOption1().trim());
        option1.setChecked(false);
        option1.setTextColor(Color.parseColor("#000000"));
        option2.setText(questionBean.getOption2().trim());
        option2.setChecked(false);
        option2.setTextColor(Color.parseColor("#000000"));
        option3.setText(questionBean.getOption3().trim());
        option3.setChecked(false);
        option3.setTextColor(Color.parseColor("#000000"));
        option4.setText(questionBean.getOption4().trim());
        option4.setChecked(false);
        option4.setTextColor(Color.parseColor("#000000"));
        addBookmark.setChecked(false);
        isBookmarked();
    }

    public void clickAction(View v) {
        CheckedTextView temp = (CheckedTextView) v;
        if(temp !=null) {
            if (!temp.isChecked()) {
                for (CheckedTextView item : allCheckedTextViews) {
                    item.setChecked(false);
                    item.setTextColor(Color.parseColor("#000000"));
                }
                temp.setChecked(true);
                temp.setTextColor(Color.parseColor("#FFFFFF"));
                selectedOption = temp.getText().toString();
                temp.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_selected));
            } else {
                temp.setChecked(false);
                temp.setTextColor(Color.parseColor("#000000"));
                selectedOption = null;
                temp.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_deselected));
            }
        }
    }

    public boolean isBookmarked(){
        dbHandler.open();
        boolean areQuestionsEqual = false;
        Cursor questionToBeCompared = dbHandler.returnQuestion();
        questionToBeCompared.moveToNext();
        if (!questionToBeCompared.isAfterLast()) {
            while (!questionToBeCompared.isAfterLast()) {
                String questionInDb = questionToBeCompared.getString(questionToBeCompared.getColumnIndex("question"));
                String questionOnScreen = question.getText().toString();

//            REPLACE THE SINGLE QUOTES (THAT RESULT IN SQLiteException)
/*          if (questionInDb.contains("'"))
                questionInDb = questionInDb.replace("'", "\'");
            if(questionOnScreen.contains("'"))
                questionOnScreen = questionOnScreen.replace("'", "\'");
*/

//            CHECK IF THE QUESTION IS ALREADY BOOKMARKED
                areQuestionsEqual = questionOnScreen.equalsIgnoreCase(questionInDb);
                if (areQuestionsEqual) {
                    questionToBeCompared.moveToLast();
                    questionToBeCompared.moveToNext();
                }
                questionToBeCompared.moveToNext();
            }
        }
        if (areQuestionsEqual) {
            addBookmark.setChecked(true);
            dbHandler.close();
            return true;
        } else {
            addBookmark.setChecked(false);
            dbHandler.close();
            return false;
        }
    }

    public void resumeBookmarkAction(ToggleButton t) {
        if(isBookmarked()) {
            t.setChecked(true);
        }
        else t.setChecked(false);
    }

    public void bookmarkAction(ToggleButton t) {
        if(t !=null) {
//            IF THE QUESTION IS BOOKMARKED
            if (!t.isChecked()) {
                t.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_deselected));
                dbHandler.open();
                dbHandler.deleteData(question.getText().toString());
                dbHandler.close();
                Toast.makeText(getContext(), "Removed from bookmarks", Toast.LENGTH_SHORT).show();
            }
//            IF THE QUESTION IS NOT BOOKMARKED
            else {
                t.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_selected));
                dbHandler.open();
                dbHandler.insertData(selections[0], selections[1],
                        question.getText().toString(),
                        option1.getText().toString(),
                        option2.getText().toString(),
                        option3.getText().toString(),
                        option4.getText().toString(),
                        answer);

                dbHandler.close();
                Toast.makeText(getContext(), "Added to bookmarks", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showNextQuestion(){
        previousQuestion.push(questionBean);
        previousAnswer.push(selectedOption);
//        QUESTION_COUNT++;

        if (QUESTION_COUNT < questionList.size()){
            selectedOption = null;
            addBookmark.setChecked(false);
            questionBean = questionList.get(QUESTION_COUNT);
            populate();
            answer = questionBean.getAnswer();
        }
        else{
            onCompletion(true);
        }
    }

//    ALERTS

    public void onCompletion(boolean reallyCompleted){
        // Display Ad
        showInterstitial2();

//        PremPrateek Formula to calculate the score!
        long score = (totalTime * QUESTION_COUNT * ((20 * CORRECT_ANSWERS) - (5 * INCORRECT_ANSWERS))) / timeTaken;
        SCORE = score;
        countDownTimer.cancel();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (reallyCompleted) {
            builder.setMessage("You have completed the quiz!" +
                    "\nScore: " + score);
        } else builder.setMessage("Sorry, but the time's up!" +
                "\nScore: " + score);
        builder.setCancelable(false);
        builder.setPositiveButton("Results", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SKIPPED_ANSWERS = QUESTION_COUNT-( CORRECT_ANSWERS + INCORRECT_ANSWERS );
                if (CORRECT_ANSWERS < 0 || INCORRECT_ANSWERS < 0 || SKIPPED_ANSWERS < 0 || QUESTION_COUNT == 0) {
                    pieDisplayError(CORRECT_ANSWERS, INCORRECT_ANSWERS, SKIPPED_ANSWERS, QUESTION_COUNT);
                }else {
                    mListener.onFragmentInteraction("launchResults");
                }
            }
        });
        builder.setNegativeButton("Take another quiz", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Difficulty.BACK_FROM_RESULTS = 2;
                wannaGoToHome = true;
                resetFlags();
                onBackPressed();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

//        SHOW ADS
//        showInterstitial3();
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
                Difficulty.BACK_FROM_RESULTS = 2;
                resetFlags();
                onBackPressed();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void changeDifficulty(){
    }

    //    OPTIONS MENU
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_questions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onBackPressed() {
        if (Help.isFragmentActive){
            Help.isFragmentActive = false;
            Help.rootView.startAnimation(AnimationUtils.loadAnimation(
                    getContext(), R.anim.fragment_anim_out));

            getActivity().getSupportFragmentManager().beginTransaction().remove(
                    getActivity().getSupportFragmentManager().findFragmentByTag("help")).commit();
            return;
        }
        if (About.isFragmentActive){
            About.isFragmentActive = false;
            About.rootView.startAnimation(AnimationUtils.loadAnimation(
                    getContext(), R.anim.fragment_anim_out));
            getActivity().getSupportFragmentManager().beginTransaction().remove(
                    getActivity().getSupportFragmentManager().findFragmentByTag("about")).commit();
            return;
        }
        int backStackCount = getActivity().getSupportFragmentManager().getBackStackEntryCount();

        if(backStackCount >= 1){
            //noinspection ConstantConditions
            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        }
        switch (Difficulty.BACK_FROM_RESULTS){
//            CALLED TO CONFIRM EXIT (BACK BUTTON PRESS)
            case 0:
                countDownTimer.cancel();
                mListener.onFragmentInteraction("gotoHome");
                break;
//            CALLED IF "TAKE ANOTHER QUIZ" SELECTED FROM onCompletion() // OR CONFIRMED EXIT FROM gotoHome()
            case 2:
                getActivity().getSupportFragmentManager().popBackStackImmediate();
                getActivity().getSupportFragmentManager().popBackStackImmediate();
                break;
//            CALLED ON ACTION BAR UP BUTTON PRESS (CHANGE THE DIFFICULTY)
            case 3:
                changeDifficulty();
                break;
//            DEFAULT EXIT CALL, GENERALLY WHEN Difficulty.BACK_FROM_RESULTS == 1
            default:
                Difficulty.BACK_FROM_RESULTS = 0;
                getActivity().finish();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        isFragmentActive = false;
        countDownTimer.cancel();
        super.onDestroyView();
    }

    private void showInterstitial1() {
        if (mInterstitialAd1.isLoaded()) {
            mInterstitialAd1.show();
        }
    }

    private void showInterstitial2() {
        if (mInterstitialAd2.isLoaded()) {
            mInterstitialAd2.show();
        }
    }

/*    private void showInterstitial3() {
        if (mInterstitialAd3.isLoaded()) {
            mInterstitialAd3.show();
        }*/
//    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        // Load ads into Interstitial Ads
        mInterstitialAd1.loadAd(adRequest);
        mInterstitialAd2.loadAd(adRequest);
//        mInterstitialAd3.loadAd(adRequest);
    }

    @Override
    public void onAttach(Context context) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        super.onAttach(context);
        setHasOptionsMenu(true);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String action);
    }
}