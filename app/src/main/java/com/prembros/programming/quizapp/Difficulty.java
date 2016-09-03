package com.prembros.programming.quizapp;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Difficulty extends Fragment {

    static String ARG_POSITION = "PositionArgs";
    static String fieldPosition;
    public static int BACK_FROM_RESULTS = 0;
    private OnFragmentInteractionListener mListener;
    private boolean isFragmentActive = false;

    @Override
    public void onAttach(Context context) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onAttach(context);
        BACK_FROM_RESULTS = 0;
        if (getArguments() != null) {
            Bundle args;
            args = this.getArguments();
            fieldPosition = (String) args.get(ARG_POSITION);
            setUpActionBar();
        }
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        BACK_FROM_RESULTS = 0;
        super.onResume();
        if (getArguments() != null) {
            Bundle args;
            args = this.getArguments();
            fieldPosition = (String) args.get(ARG_POSITION);
            setUpActionBar();
        }
    }

    public void setUpActionBar(){
//        SET UP ACTION BAR
        android.support.v7.app.ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowHomeEnabled(false);
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setTitle(R.string.app_name);
            ab.setSubtitle(fieldPosition);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!Questions.wannaGoToHome) {
            // Inflate the layout for this fragment
            View rootView = inflater.inflate(R.layout.fragment_difficulty, container, false);

            String[] choices = {
                    "Rookie",
                    "Apprentice",
                    "Pro",
                    "Hitman"
            };

            List<String> allFields = new ArrayList<>(Arrays.asList(choices));

            ArrayAdapter<String> difficulty = new ArrayAdapter<>(
                    getContext(),
                    R.layout.choice,
                    R.id.choice_button,
                    allFields);

            ListView listView = (ListView) rootView.findViewById(R.id.listView1);
            listView.setAdapter(difficulty);
            if (!isFragmentActive) {
                isFragmentActive = true;
                listView.setLayoutAnimation(
                        new LayoutAnimationController(
                                AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right),
                                0.2F
                        )
                );
            } else
                listView.setLayoutAnimation(
                        new LayoutAnimationController(
                                AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left),
                                0.2F
                        )
                );
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mListener.onFragmentInteraction(fieldPosition, getDifficulty(position));
                    fieldPosition = null;
                }
            });
            return rootView;
        }
        else{
            Questions.wannaGoToHome = false;
            return null;
        }
//        return inflater.inflate(R.layout.fragment_difficulty, container, false);
    }

    public String getDifficulty(int position){
        switch (position){
            case 0:
                return "Rookie";
            case 1:
                return "Apprentice";
            case 2:
                return "Pro";
            case 3:
                return "Hitman";
            default:
                return null;
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String selection,String difficulty);
    }
}