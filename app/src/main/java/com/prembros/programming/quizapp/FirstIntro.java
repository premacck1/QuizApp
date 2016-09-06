package com.prembros.programming.quizapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import static android.graphics.Color.rgb;

/*
 * Created by Prem $ on 8/29/2016.
 */
public class FirstIntro extends AppIntro2 {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFadeAnimation();

//        Add slides
        addSlide(AppIntroFragment.newInstance("Hey There!", "Welcome to QuizApp,\nYou're about to start\nQuizApp - Programming",
                R.drawable.app_icon_big, rgb(92,107,192)));

        addSlide(AppIntroFragment.newInstance("QuizApp", "QuizApp isn't just about the name." +
                "\n\nIt is Integrated with Google so that you can keep track of your high scores and compete with your friends",
                R.drawable.game_controller, rgb(7, 107, 112)));

        addSlide(AppIntroFragment.newInstance("Bookmarks", "The Pro version comes with bookmarks!." +
                        "\n\nBookmark questions during quiz, and review them later in the Bookmarks section.",
                R.drawable.bookmarks_see, rgb(74,20,140)));

        addSlide(AppIntroFragment.newInstance("All caught up!", "So now that you're familiar with the features, let's get started!",
                R.drawable.ic_action_start_big, rgb(12, 152, 149)));
    }

    public void loadMainActivity(){
        startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        AlertDialog.Builder builder = new AlertDialog.Builder(FirstIntro.this);
        builder.setMessage("Are you sure you want to skip the Introduction?");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadMainActivity();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        loadMainActivity();
    }
}
