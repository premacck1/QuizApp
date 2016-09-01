package com.prembros.programming.quizapp;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/*
** Created by Prem $ on 7/20/2016.
*/
public class ResultsInDetail extends Fragment {

    public static String ARG_ENTRY = "entry number";
    ResultsInDetailAdapter listAdapter;
    ListView listView;
    List<String> listDataHeader;
    List<String> listDataChild;
    DatabaseHolder dbHandler;
    private int entry_number;
    private OnFragmentInteractionListener mListener;
    public static View rootView;
    public static boolean isFragmentActive;
    private CustomTextViewSemiLight headerText;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getArguments() != null) {
            Bundle args;
            args = this.getArguments();
            entry_number = (int) args.get(ARG_ENTRY);
        }
        else entry_number = -1;

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_results_in_detail, container, false);
        isFragmentActive = true;

        if (entry_number != -1) {

            rootView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fragment_anim_in));

            headerText = (CustomTextViewSemiLight) rootView.findViewById(R.id.detailedResultHeader);
            // get the listview
            listView = (ListView) rootView.findViewById(R.id.detailedResultListView);

            // preparing list data
            prepareListData();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Sorry, but the questions couldn't be loaded.");
            builder.setCancelable(false);
            builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    resetFlags();
                    mListener.onFragmentInteraction();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        return rootView;
    }
    /*
             * Preparing the list data
             */
    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new ArrayList<>();
        Cursor detailedResult;
        int location;

        dbHandler = new DatabaseHolder(getContext());
        dbHandler.open();
        switch(entry_number){
//            PIE SECTION OF CORRECT ANSWERS
            case 0: {
                setUpListAdapter();
                detailedResult = dbHandler.returnCorrectAnswers();
                detailedResult.moveToFirst();
                location = 0;

                headerText.setText(R.string.your_correct_answers);

//            POPULATING THE expandableListView with the correct answers
                while (!detailedResult.isAfterLast()) {
                    // Adding header data
                    listDataHeader.add((location + 1) + ": " + detailedResult.getString(detailedResult.getColumnIndex("question")));

                    // Adding child data
                    listDataChild.add("Answer: " + detailedResult.getString(detailedResult.getColumnIndex("answer")));

                    location++;
                    detailedResult.moveToNext();
                }
                dbHandler.close();
                break;
            }
//            PIE SECTION OF INCORRECT ANSWERS
            case 1: {
                setUpListAdapter();
                detailedResult = dbHandler.returnIncorrectAnswers();
                detailedResult.moveToFirst();
                location = 0;

                headerText.setText(R.string.your_incorrect_answers);

//            POPULATING THE listView with the incorrect answers
                while (!detailedResult.isAfterLast()) {
                    // Adding header data
                    listDataHeader.add((location + 1) + ": " + detailedResult.getString(detailedResult.getColumnIndex("question")));

                    // Adding child data
                    listDataChild.add("Your Answer: " + detailedResult.getString(detailedResult.getColumnIndex("givenAnswer")) +
                            "\nCorrect Answer: " + detailedResult.getString(detailedResult.getColumnIndex("correctAnswer")));

                    location++;
                    detailedResult.moveToNext();
                }
                dbHandler.close();
                break;
            }
//            PIE SECTION OF SKIPPED ANSWERS
            case 2: {
                setUpListAdapter();
                detailedResult = dbHandler.returnSkippedAnswers();
                detailedResult.moveToFirst();
                location = 0;

                headerText.setText(R.string.your_skipped_answers);

//            POPULATING THE expandableListView with the skipped answers
                while (!detailedResult.isAfterLast()) {
                    // Adding header data
                    listDataHeader.add((location + 1) + ": " + detailedResult.getString(detailedResult.getColumnIndex("question")));

                    // Adding child data
                    listDataChild.add("Answer: " + detailedResult.getString(detailedResult.getColumnIndex("answer")));

                    location++;
                    detailedResult.moveToNext();
                }
                dbHandler.close();
                break;
            }
            default:
                dbHandler.close();
                break;
        }
        dbHandler.close();
    }

    public void resetFlags() {
        Questions.QUESTION_COUNT = 0;
        Questions.CORRECT_ANSWERS = 0;
        Questions.INCORRECT_ANSWERS = 0;
        dbHandler.open();
        dbHandler.resetTables();
        dbHandler.close();
    }

    public void setUpListAdapter(){
        // setting list adapter
        listAdapter = new ResultsInDetailAdapter(getContext(), listDataHeader, listDataChild);
        listView.setAdapter(listAdapter);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }
}