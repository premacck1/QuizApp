package com.prembros.programming.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class FirstScreen extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);

        int SPLASH_DISPLAY_LENGTH = 1500;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                imageButton.startAnimation(AnimationUtils.loadAnimation(FirstScreen.this, R.anim.float_in_from_below));
                startActivity(new Intent(FirstScreen.this, MainActivity.class));
                FirstScreen.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
