package com.prembros.programming.quizapp;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * Created by Prem $ on 7/4/2016.
 */
public class QuestionBean implements Parcelable {

    String difficulty = "";
    String question = "";
    String option1 = "";
    String option2 = "";
    String option3 = "";
    String option4 = "";
    String answer = "";

    public QuestionBean()
    {

    }
    public QuestionBean(Parcel input)
    {
        difficulty = input.readString();
        question = input.readString();
        option1 = input.readString();
        option2 = input.readString();
        option3 = input.readString();
        option4 = input.readString();
        answer = input.readString();
    }
//    public String getDifficulty() {
//        return difficulty;
//    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public String getOption4() {
        return option4;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(difficulty);
        dest.writeString(question);
        dest.writeString(option1);
        dest.writeString(option2);
        dest.writeString(option3);
        dest.writeString(option4);
        dest.writeString(answer);

    }
    public static final Creator<QuestionBean> CREATOR
            = new Creator<QuestionBean>() {
        public QuestionBean createFromParcel(Parcel in) {
            return new QuestionBean(in);
        }

        public QuestionBean[] newArray(int size) {
            return new QuestionBean[size];
        }
    };
}